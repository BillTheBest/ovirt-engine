package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.action.AddVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmTemplateInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.DevicesResource;

public class BackendTemplateNicsResource
            extends BackendNicsResource
            implements DevicesResource<NIC, Nics>{

    public BackendTemplateNicsResource(Guid parentId) {
        super(parentId,
              VdcQueryType.GetTemplateInterfacesByTemplateId,
              new GetVmTemplateParameters(parentId),
              VdcActionType.AddVmTemplateInterface,
              VdcActionType.RemoveVmTemplateInterface,
              VdcActionType.UpdateVmTemplateInterface);
    }

    @Override
    protected ParametersProvider<NIC, VmNetworkInterface> getUpdateParametersProvider() {
        return new UpdateParametersProvider();
    }

    protected class UpdateParametersProvider implements ParametersProvider<NIC, VmNetworkInterface> {
        @Override
        public VdcActionParametersBase getParameters(NIC incoming, VmNetworkInterface entity) {
            return new AddVmTemplateInterfaceParameters(parentId, map(incoming, entity));
        }
    }

    @Override
    protected VdcActionParametersBase getAddParameters(VmNetworkInterface entity, NIC nic) {
        return new AddVmTemplateInterfaceParameters(parentId, setNetwork(nic, entity));
    }

    @Override
    protected VdcActionParametersBase getRemoveParameters(String id) {
        return new RemoveVmTemplateInterfaceParameters(parentId, asGuid(id));
    }

    @Override
    @SingleEntityResource
    public DeviceResource<NIC> getDeviceSubResource(String id) {
        return inject(new BackendTemplateNicResource(id,
                                                     this,
                                                     updateType,
                                                     getUpdateParametersProvider(),
                                                     getRequiredUpdateFields(),
                                                     subCollections));
    }

    @Override
    protected VmNetworkInterface setNetwork(NIC device, VmNetworkInterface ni) {
        if (device.isSetNetwork()) {
            Guid clusterId = getEntity(VmTemplate.class,
                                       VdcQueryType.GetVmTemplate,
                                       new GetVmTemplateParameters(parentId), "id").getvds_group_id();
            network net = lookupClusterNetwork(clusterId, device.getNetwork().isSetId() ? asGuid(device.getNetwork().getId()) : null, device.getNetwork().getName());
            if (net != null) {
                ni.setNetworkName(net.getname());
            }
        }
        return ni;
    }

    @Override
    public NIC addParents(NIC device) {
        device.setTemplate(new Template());
        device.getTemplate().setId(parentId.toString());
        return device;
    }

    @Override
    protected Guid getClusterId() {
        Guid clusterId = getEntity(VmTemplate.class,
                                   VdcQueryType.GetVmTemplate,
                                   new GetVmTemplateParameters(parentId), "id").getvds_group_id();
        return clusterId;
    }
}
