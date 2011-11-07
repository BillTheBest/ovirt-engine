package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.compat.Guid;

public class PermissionMapper {

    @Mapping(from = Permission.class, to = permissions.class)
    public static permissions map(Permission model, permissions template) {
        permissions entity = template != null ? template : new permissions();
        if (model.isSetId()) {
            entity.setId(new Guid(model.getId()));
        }
        if (model.isSetRole() && model.getRole().isSetId()) {
            entity.setrole_id(new Guid(model.getRole().getId()));
        }
        if (model.isSetUser() && model.getUser().isSetId()) {
            entity.setad_element_id(new Guid(model.getUser().getId()));
        } else if (model.isSetGroup() && model.getGroup().isSetId()) {
            entity.setad_element_id(new Guid(model.getGroup().getId()));
        }
        entity.setObjectId(map(model, template != null ? template.getObjectId() : null));
        entity.setObjectType(map(model, template != null ? template.getObjectType() : null));
        return entity;
    }

    @Mapping(from = permissions.class, to = Role.class)
    public static Role map(permissions entity, Role template) {
        Role model = template != null ? template : new Role();
        model.setName(entity.getRoleName());
        model.setId(entity.getrole_id().toString());
        return model;
    }

    @Mapping(from = permissions.class, to = Permission.class)
    public static Permission map(permissions entity, Permission template) {
        Permission model = template != null ? template : new Permission();
        model.setId(entity.getId().toString());
        if (entity.getrole_id() != null) {
            model.setRole(new Role());
            model.getRole().setId(entity.getrole_id().toString());
        }
        if (entity.getad_element_id() != null && (template == null || !template.isSetGroup())) {
            model.setUser(new User());
            model.getUser().setId(entity.getad_element_id().toString());
        }
        if (entity.getObjectId() != null) {
            setObjectId(model, entity);
        }
        return model;
    }

    @Mapping(from = Permission.class, to = Guid.class)
    public static Guid map(Permission p, Guid template) {
        return p.isSetDataCenter() && p.getDataCenter().isSetId()
               ? new Guid(p.getDataCenter().getId())
               : p.isSetCluster() && p.getCluster().isSetId()
                 ? new Guid(p.getCluster().getId())
                 : p.isSetHost() && p.getHost().isSetId()
                   ? new Guid(p.getHost().getId())
                   : p.isSetStorageDomain() && p.getStorageDomain().isSetId()
                     ? new Guid(p.getStorageDomain().getId())
                     : p.isSetVm() && p.getVm().isSetId()
                       ? new Guid(p.getVm().getId())
                       : p.isSetVmpool() && p.getVmpool().isSetId()
                         ? new Guid(p.getVmpool().getId())
                         : p.isSetTemplate() && p.getTemplate().isSetId()
                           ? new Guid(p.getTemplate().getId())
                           : template;
    }

    @Mapping(from = Permission.class, to = VdcObjectType.class)
    public static VdcObjectType map(Permission p, VdcObjectType template) {
        return p.isSetDataCenter() && p.getDataCenter().isSetId()
               ? VdcObjectType.StoragePool
               : p.isSetCluster() && p.getCluster().isSetId()
                 ? VdcObjectType.VdsGroups
                 : p.isSetHost() && p.getHost().isSetId()
                   ? VdcObjectType.VDS
                   : p.isSetStorageDomain() && p.getStorageDomain().isSetId()
                     ? VdcObjectType.Storage
                     : p.isSetVm() && p.getVm().isSetId()
                       ? VdcObjectType.VM
                       : p.isSetVmpool() && p.getVmpool().isSetId()
                         ? VdcObjectType.VmPool
                         : p.isSetTemplate() && p.getTemplate().isSetId()
                           ? VdcObjectType.VmTemplate
                           : template;
    }

    /**
     * @pre completeness of "{entityType}.id" already validated
     */
    private static void setObjectId(Permission model, permissions entity) {
        String id = entity.getObjectId().toString();
        switch (entity.getObjectType()) {
        case StoragePool :
            model.setDataCenter(new DataCenter());
            model.getDataCenter().setId(id);
            break;
        case VdsGroups :
            model.setCluster(new Cluster());
            model.getCluster().setId(id);
            break;
        case VDS :
            model.setHost(new Host());
            model.getHost().setId(id);
            break;
        case Storage :
            model.setStorageDomain(new StorageDomain());
            model.getStorageDomain().setId(id);
            break;
        case VM :
            model.setVm(new VM());
            model.getVm().setId(id);
            break;
        case VmPool :
            model.setVmpool(new VmPool());
            model.getVmpool().setId(id);
            break;
        case VmTemplate :
            model.setTemplate(new Template());
            model.getTemplate().setId(id);
            break;
        default:
            assert false;
        }
    }
}
