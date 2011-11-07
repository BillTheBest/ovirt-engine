package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendHostNicsResource.SUB_COLLECTIONS;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.resource.HostNicResource;
import org.ovirt.engine.api.resource.StatisticsResource;

import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.common.action.UpdateNetworkToVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.GetAllChildVlanInterfacesQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

public class BackendHostNicResource
    extends AbstractBackendActionableResource<HostNIC, VdsNetworkInterface>
    implements HostNicResource {

    private BackendHostNicsResource parent;

    public BackendHostNicResource(String id, BackendHostNicsResource parent) {
        super(id, HostNIC.class, VdsNetworkInterface.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    public BackendHostNicsResource getParent() {
        return parent;
    }

    @Override
    public HostNIC get() {
        return parent.lookupNic(id);
    }

    @Override
    protected HostNIC addParents(HostNIC nic) {
        return parent.addParents(nic);
    }

    protected Response doAttachAction(Action action, VdcActionType actionType) {
        validateParameters(action, "network.id|name");

        VdsNetworkInterface hostInterface = parent.lookupInterface(id);
        AttachNetworkToVdsParameters params = new AttachNetworkToVdsParameters(asGuid(parent.getHostId()),
                                                                               parent.lookupNetwork(action.getNetwork()),
                                                                               hostInterface);
        params.setBondingOptions(hostInterface.getBondOptions());
        params.setBootProtocol(hostInterface.getBootProtocol());
        params.setAddress(hostInterface.getAddress());
        params.setSubnet(hostInterface.getSubnet());

        return doAction(actionType, params, action);
    }

    @Override
    public Response attach(Action action) {
        validateParameters(action, "network.id|name");
        return doAttachAction(action, VdcActionType.AttachNetworkToVdsInterface);
    }

    @Override
    public Response detach(Action action) {
        validateParameters(action, "network.id|name");
        return doAttachAction(action, VdcActionType.DetachNetworkFromVdsInterface);
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        EntityIdResolver resolver = new EntityIdResolver() {
            @Override
            public VdsNetworkInterface lookupEntity(Guid guid) throws BackendFailureException {
                return parent.lookupInterface(id);
            }
        };
        HostNicStatisticalQuery query = new HostNicStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<HostNIC, VdsNetworkInterface>(entityType, guid, query));
    }

    @Override
    protected HostNIC populate(HostNIC model, VdsNetworkInterface entity) {
        return parent.addStatistics(model, entity, uriInfo, httpHeaders);
    }

    @SuppressWarnings("serial")
    @Override
    public HostNIC update(HostNIC nic) {
        VdsNetworkInterface originalInter = parent.lookupInterface(id);
        final VdsNetworkInterface inter = map(nic, originalInter);
        network oldNetwork = getOldNetwork(originalInter);
        network newNetwork = getNewNetwork(nic);
        UpdateNetworkToVdsParameters params =
            new UpdateNetworkToVdsParameters(Guid.createGuidFromString(parent.getHostId()),
                                             newNetwork!=null ? newNetwork : oldNetwork ,
                                             new ArrayList<VdsNetworkInterface>(){{add(inter);}});

        params.setOldNetworkName(oldNetwork!=null ? oldNetwork.getname() : null);
        if(nic.isSetName() && inter.getBonded() != null && inter.getBonded()){
            params.setBondName(nic.getName());
        }
        if(nic.isSetIp()){
            if(nic.getIp().isSetAddress()){
                params.setAddress(nic.getIp().getAddress());
            }
            if(nic.getIp().isSetNetmask()){
                params.setSubnet(nic.getIp().getNetmask());
            }
            if(nic.getIp().isSetGateway()){
                params.setGateway(nic.getIp().getGateway());
            }
        }
        if(nic.isSetBootProtocol()){
            BootProtocol bootProtocol = BootProtocol.fromValue(nic.getBootProtocol());
            if(bootProtocol != null){
                params.setBootProtocol(map(bootProtocol, null));
            }
        }else if(nic.isSetIp() && nic.getIp().isSetAddress() && !nic.getIp().getAddress().isEmpty()){
            params.setBootProtocol(NetworkBootProtocol.StaticIp);
        }
        if(nic.isSetBonding() && nic.getBonding().isSetOptions()){
           params.setBondingOptions(getBondingOptions(nic.getBonding().getOptions().getOptions()));
        }
        if(nic.isSetCheckConnectivity()){
            params.setCheckConnectivity(nic.isCheckConnectivity());
        }
        performAction(VdcActionType.UpdateNetworkToVdsInterface, params);

        return parent.lookupNic(id);
    }

    private network getNewNetwork(HostNIC nic) {
        network newNetwork = null;
        if(nic.isSetNetwork()){
            newNetwork = map(nic.getNetwork(), parent.lookupClusterNetwork(nic.getNetwork()));
        }
        return newNetwork;
    }

    private network getOldNetwork(VdsNetworkInterface originalInter) {
        String oldNetworkName = originalInter.getNetworkName();
        if (!StringHelper.isNullOrEmpty(oldNetworkName)) {
            return lookupAtachedNetwork(originalInter.getNetworkName());
        } else {
            GetAllChildVlanInterfacesQueryParameters params = new GetAllChildVlanInterfacesQueryParameters(
                                                                    asGuid(originalInter.getVdsId()),
                                                                    originalInter);
            List<VdsNetworkInterface> vlans = getBackendCollection(VdsNetworkInterface.class, VdcQueryType.GetAllChildVlanInterfaces, params);
            if (vlans!=null && !vlans.isEmpty()) {
                return lookupAtachedNetwork(vlans.get(0).getNetworkName());
            } else {
                return null;
            }
        }
    }

    private network lookupAtachedNetwork(String networkName) {
        if(!StringHelper.isNullOrEmpty(networkName)){
            for(network nwk : parent.getClusterNetworks()){
                if(nwk.getname().equals(networkName)) return nwk;
            }
        }
        return null;
    }

    private String getBondingOptions(List<Option> options) {
        StringBuffer bufOptions = new StringBuffer();
        for(Option opt : options){
            bufOptions.append(opt.getName() + "=" + opt.getValue() + " ");
        }
        return bufOptions.toString().substring(0, bufOptions.length() - 1);
    }

    private NetworkBootProtocol map(BootProtocol bootProtocol, NetworkBootProtocol template) {
        return getMapper(BootProtocol.class, NetworkBootProtocol.class).map(bootProtocol, template);
    }

    private network map(Network network, network template) {
        return getMapper(Network.class, network.class).map(network, template);
    }
}
