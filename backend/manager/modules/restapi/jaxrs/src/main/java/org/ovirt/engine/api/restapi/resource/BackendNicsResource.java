package org.ovirt.engine.api.restapi.resource;


import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.resource.DevicesResource;

import org.ovirt.engine.api.restapi.resource.BaseBackendResource.WebFaultException;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendNicsResource
        extends AbstractBackendDevicesResource<NIC, Nics, VmNetworkInterface>
        implements DevicesResource<NIC, Nics> {

    static final String SUB_COLLECTIONS = "statistics";

    public BackendNicsResource(Guid parentId,
                               VdcQueryType queryType,
                               VdcQueryParametersBase queryParams,
                               VdcActionType addAction,
                               VdcActionType removeAction,
                               VdcActionType updateAction) {
        super(NIC.class,
              Nics.class,
              VmNetworkInterface.class,
              parentId,
              queryType,
              queryParams,
              addAction,
              removeAction,
              updateAction,
              SUB_COLLECTIONS);
    }

    @Override
    public Nics list() {
        Nics nics = new Nics();
        List<VmNetworkInterface> entities = getBackendCollection(queryType, queryParams);
        Guid clusterId = getClusterId();
        List<network> networks = getBackendCollection(network.class,
             VdcQueryType.GetAllNetworksByClusterId,
             new VdsGroupQueryParamenters(clusterId));
        for (VmNetworkInterface entity : entities) {
            network network = lookupClusterNetwork(clusterId, null, entity.getNetworkName(), networks);
            NIC nic = populate(map(entity), entity);
            if (network!=null && network.getId()!=null) {
                nic.getNetwork().setId(network.getId().toString());
                nic.getNetwork().setName(null);
            }
            if (validate(nic)) {
                nics.getNics().add(addLinks(nic));
            }
        }
        return nics;
    }

    protected abstract Guid getClusterId();

    @Override
    protected boolean matchEntity(VmNetworkInterface entity, Guid id) {
        return id != null && id.equals(entity.getId());
    }

    @Override
    protected boolean matchEntity(VmNetworkInterface entity, String name) {
        return name != null && name.equals(entity.getName());
    }

    @Override
    protected String[] getRequiredUpdateFields() {
        return new String[0];
    }

    @Override
    protected String[] getRequiredAddFields() {
        return new String[] { "name", "network.name|id" };
    }

    protected network lookupClusterNetwork(Guid clusterId, Guid id, String name, List<network> networks) {
        for (network network : networks) {
            if ((id != null && id.equals(network.getId())) ||
                (name != null && name.equals(network.getname()))) {
                return network;
            }
        }
        return null;
    }

    protected network lookupClusterNetwork(Guid clusterId, Guid id, String name) {
        for (network entity : getBackendCollection(network.class,
                                                   VdcQueryType.GetAllNetworksByClusterId,
                                                   new VdsGroupQueryParamenters(clusterId))) {
            if ((id != null && id.equals(entity.getId())) ||
                (name != null && name.equals(entity.getname()))) {
                return entity;
            }
        }
        throw new WebFaultException(null, "Network not found in cluster", Response.Status.BAD_REQUEST);
    }

    @Override
    public Response add(NIC device) {
        validateParameters(device, getRequiredAddFields());
        Response response = performCreation(addAction,
                                            getAddParameters(map(device), device),
                                            getEntityIdResolver(device.getName()));
        if (response!=null) {
            Object entity = response.getEntity();
            if (entity!=null) {
                NIC nic = (NIC)entity;
                setNetworkId(nic);
            }
        }
        return response;
    }

    protected void setNetworkId(NIC nic) {
        if ( (nic.isSetNetwork()) && (!nic.getNetwork().isSetId())) {
            Guid clusterId = getClusterId();
            network network = lookupClusterNetwork(clusterId, nic.getNetwork().getId()==null ? null : asGuid(nic.getNetwork().getId()), nic.getNetwork().getName());
            if (network!=null) {
                nic.getNetwork().setName(null);
                nic.getNetwork().setId(network.getId().toString());
            }
        }
    }

    protected abstract VmNetworkInterface setNetwork(NIC device, VmNetworkInterface ni);
}
