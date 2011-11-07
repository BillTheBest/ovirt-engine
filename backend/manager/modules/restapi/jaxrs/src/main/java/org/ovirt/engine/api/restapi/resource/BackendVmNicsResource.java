package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.common.util.LinkHelper;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.DevicesResource;

public class BackendVmNicsResource
             extends BackendNicsResource
             implements DevicesResource<NIC, Nics>{

    public BackendVmNicsResource(Guid parentId) {
        super(parentId,
              VdcQueryType.GetVmInterfacesByVmId,
              new GetVmByVmIdParameters(parentId),
              VdcActionType.AddVmInterface,
              VdcActionType.RemoveVmInterface,
              VdcActionType.UpdateVmInterface);
    }

    @Override
    protected ParametersProvider<NIC, VmNetworkInterface> getUpdateParametersProvider() {
        return new UpdateParametersProvider();
    }

    protected class UpdateParametersProvider implements ParametersProvider<NIC, VmNetworkInterface> {
        @Override
        public VdcActionParametersBase getParameters(NIC incoming, VmNetworkInterface entity) {
            return new AddVmInterfaceParameters(parentId, map(incoming, entity));
        }
    }

    @Override
    protected NIC populate(NIC model, VmNetworkInterface entity) {
        return addStatistics(model, entity, uriInfo, httpHeaders);
    }

    NIC addStatistics(NIC model, VmNetworkInterface entity, UriInfo ui, HttpHeaders httpHeaders) {
        if (DetailHelper.include(httpHeaders, "statistics")) {
            model.setStatistics(new Statistics());
            NicStatisticalQuery query = new NicStatisticalQuery(newModel(model.getId()));
            List<Statistic> statistics = query.getStatistics(entity);
            for (Statistic statistic : statistics) {
                LinkHelper.addLinks(ui, statistic, query.getParentType());
            }
            model.getStatistics().getStatistics().addAll(statistics);
        }
        return model;
    }

    @Override
    protected VdcActionParametersBase getAddParameters(VmNetworkInterface entity, NIC nic) {
        return new AddVmInterfaceParameters(parentId, setNetwork(nic, entity));
    }

    @Override
    protected VdcActionParametersBase getRemoveParameters(String id) {
        return new RemoveVmInterfaceParameters(parentId, asGuid(id));
    }

    @Override
    @SingleEntityResource
    public DeviceResource<NIC> getDeviceSubResource(String id) {
        return inject(new BackendVmNicResource(id,
                                             this,
                                             updateType,
                                             getUpdateParametersProvider(),
                                             getRequiredUpdateFields(),
                                             subCollections));
    }

    @Override
    protected VmNetworkInterface setNetwork(NIC device, VmNetworkInterface ni) {
        if (device.isSetNetwork()) {
            Guid clusterId = getClusterId();
            network net = lookupClusterNetwork(clusterId, device.getNetwork().isSetId() ? asGuid(device.getNetwork().getId()) : null, device.getNetwork().getName());
            if (net != null) {
                ni.setNetworkName(net.getname());
            }
        }
        return ni;
    }

    @Override
    protected Guid getClusterId() {
        Guid clusterId = getEntity(VM.class,
            VdcQueryType.GetVmByVmId,
            new GetVmByVmIdParameters(parentId), "id").getvds_group_id();
        return clusterId;
    }
}
