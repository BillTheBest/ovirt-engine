package org.ovirt.engine.api.restapi.resource;


import javax.ws.rs.core.Response;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.ReadOnlyDevicesResource;
import org.ovirt.engine.api.resource.TemplateResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.UpdateVmTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendVmsResource.SUB_COLLECTIONS;

public class BackendTemplateResource
    extends AbstractBackendActionableResource<Template, VmTemplate>
    implements TemplateResource {

    public BackendTemplateResource(String id) {
        super(id, Template.class, VmTemplate.class, SUB_COLLECTIONS);
    }

    @Override
    public Template get() {
        return performGet(VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(guid));
    }

    @Override
    public Template update(Template incoming) {
        return performUpdate(incoming,
                             new QueryIdResolver(VdcQueryType.GetVmTemplate, GetVmTemplateParameters.class),
                             VdcActionType.UpdateVmTemplate,
                             new UpdateParametersProvider());
    }

    @Override
    public Response export(Action action) {
        validateParameters(action, "storageDomain.id|name");

        MoveVmParameters params = new MoveVmParameters(guid, getStorageDomainId(action));

        if (action.isSetExclusive() && action.isExclusive()) {
            params.setForceOverride(true);
        }

        return doAction(VdcActionType.ExportVmTemplate, params, action);
    }

    protected Guid getStorageDomainId(Action action) {
        if (action.getStorageDomain().isSetId()) {
            return asGuid(action.getStorageDomain().getId());
        } else {
            return lookupStorageDomainIdByName(action.getStorageDomain().getName());
        }
    }

    protected Guid lookupStorageDomainIdByName(String name) {
        return getEntity(storage_domains.class, SearchType.StorageDomain, "Storage: name=" + name).getid();
    }

    @Override
    public ReadOnlyDevicesResource<CdRom, CdRoms> getCdRomsResource() {
        return inject(new BackendReadOnlyCdRomsResource<VmTemplate>
                                        (VmTemplate.class,
                                         guid,
                                         VdcQueryType.GetVmTemplate,
                                         new GetVmTemplateParameters(guid)));
    }

    @Override
    public ReadOnlyDevicesResource<Disk, Disks> getDisksResource() {
        return inject(new BackendReadOnlyDisksResource(guid,
                                                       VdcQueryType.GetVmTemplatesDisks,
                                                       new GetVmTemplatesDisksParameters(guid)));
    }

    @Override
    public ReadOnlyDevicesResource<NIC, Nics> getNicsResource() {
        return inject(new BackendTemplateNicsResource(guid));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             Template.class,
                                                             VdcObjectType.VmTemplate));
    }

    @Override
    public CreationResource getCreationSubresource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public ActionResource getActionSubresource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    protected class UpdateParametersProvider implements ParametersProvider<Template, VmTemplate> {
        @Override
        public VdcActionParametersBase getParameters(Template incoming, VmTemplate entity) {
            VmTemplate updated = getMapper(modelType, VmTemplate.class).map(incoming, entity);
            return new UpdateVmTemplateParameters(updated);
        }
    }
}
