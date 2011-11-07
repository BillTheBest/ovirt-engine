package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.Roles;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.AssignedRolesResource;
import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByAdElementIdParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByPermissionIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * Role assignments to an individual user are mapped to system permissions.
 */
public class BackendAssignedRolesResource
        extends AbstractBackendCollectionResource<Role, permissions>
        implements AssignedRolesResource {

    private Guid principalId;

    protected BackendAssignedRolesResource(Guid principalId) {
        super(Role.class, permissions.class);
        this.principalId = principalId;
    }

    @Override
    public Response add(Role role) {
        // REVISIT support specifying role by-name
        validateParameters(role, "id");
        return performCreation(VdcActionType.AddSystemPermission,
                               new PermissionsOperationsParametes(newPermission(role.getId())),
                               new QueryIdResolver(VdcQueryType.GetPermissionById,
                                                   MultilevelAdministrationByPermissionIdParameters.class));
    }

    @Override
    @SingleEntityResource
    public RoleResource getRoleSubResource(String id) {
        return inject(new BackendRoleResource(id, principalId));
    }

    @Override
    public Roles list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetPermissionsByAdElementId,
                                                  new MultilevelAdministrationByAdElementIdParameters(principalId)));
    }

    @Override
    public void performRemove(String id) {
        performAction(VdcActionType.RemovePermission,
                      new PermissionsOperationsParametes(getPermission(id)));
    }

    protected Roles mapCollection(List<permissions> entities) {
        Roles collection = new Roles();
        for (permissions entity : entities) {
            if (entity.getObjectType() == VdcObjectType.System) {
                collection.getRoles().add(addLinks(map(entity)));
            }
        }
        return collection;
    }

    @Override
    protected Role addParents(Role role) {
        role.setUser(new User());
        role.getUser().setId(principalId.toString());
        return role;
    }

    protected permissions newPermission(String roleId) {
        permissions permission = new permissions();
        permission.setad_element_id(principalId);
        permission.setrole_id(new Guid(roleId));
        return permission;
    }

    protected permissions getPermission(String roleId) {
        List<permissions> permissions =
            asCollection(getEntity(ArrayList.class,
                                   VdcQueryType.GetPermissionsByAdElementId,
                                   new MultilevelAdministrationByAdElementIdParameters(principalId),
                                   principalId.toString()));
        for (permissions p : permissions) {
            if (principalId.equals(p.getad_element_id())
                && roleId.equals(p.getrole_id().toString())
                && p.getObjectType() == VdcObjectType.System) {
                return p;
            }
        }
        return handleError(new EntityNotFoundException(roleId), true);
    }
}
