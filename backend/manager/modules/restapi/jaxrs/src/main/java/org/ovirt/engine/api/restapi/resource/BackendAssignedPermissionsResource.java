package org.ovirt.engine.api.restapi.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Permissions;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.PermissionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByPermissionIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

public class BackendAssignedPermissionsResource
        extends AbstractBackendCollectionResource<Permission, permissions>
        implements AssignedPermissionsResource {

    private Guid targetId;
    private VdcQueryType queryType;
    private VdcQueryParametersBase queryParams;
    private Class<? extends BaseResource> suggestedParentType;
    private VdcObjectType objectType;

    public BackendAssignedPermissionsResource(Guid targetId,
                                              VdcQueryType queryType,
                                              VdcQueryParametersBase queryParams,
                                              Class<? extends BaseResource> suggestedParentType) {
        this(targetId, queryType, queryParams, suggestedParentType, null);
    }

    public BackendAssignedPermissionsResource(Guid targetId,
                                              VdcQueryType queryType,
                                              VdcQueryParametersBase queryParams,
                                              Class<? extends BaseResource> suggestedParentType,
                                              VdcObjectType objectType) {
        super(Permission.class, permissions.class);
        this.targetId = targetId;
        this.queryType = queryType;
        this.queryParams = queryParams;
        this.suggestedParentType = suggestedParentType;
        this.objectType = objectType;
    }

    @Override
    public Permissions list() {
        return mapCollection(getBackendCollection(queryType, queryParams));
    }

    @Override
    public Response add(Permission permission) {
        validateParameters(permission,
                           isPrincipalSubCollection()
                           ? new String[] {"role.id", "dataCenter|cluster|host|storageDomain|vm|vmpool|template.id"}
                           : new String[] {"role.id", "user|group.id"});
        permissions entity = map(permission, getPermissionsTemplate(permission));
        return performCreation(VdcActionType.AddPermission,
                               getPrincipal(entity, permission),
                               new QueryIdResolver(VdcQueryType.GetPermissionById,
                                                   MultilevelAdministrationByPermissionIdParameters.class));
    }

    @Override
    public void performRemove(String id) {
        performAction(VdcActionType.RemovePermission, new PermissionsOperationsParametes(getPermissions(id)));
    }

    @Override
    @SingleEntityResource
    public PermissionResource getPermissionSubResource(String id) {
        return inject(new BackendPermissionResource(id, this, suggestedParentType));
    }

    protected Permissions mapCollection(List<permissions> entities) {
        Permissions collection = new Permissions();
        Map<Guid, DbUser> users = getUsers();
        for (permissions entity : entities) {
            if (entity.getObjectType() != VdcObjectType.System) {
                Permission permission = map(entity, users.containsKey(entity.getad_element_id()) ? users.get(entity.getad_element_id()) : null);
                collection.getPermissions().add(addLinks(permission, permission.getUser() != null ? suggestedParentType : Group.class));
            }
        }
        return collection;
    }

    public Map<Guid, DbUser> getUsers() {
        HashMap<Guid, DbUser> users = new HashMap<Guid, DbUser>();
        for (DbUser user : asCollection(DbUser.class, getEntity(List.class, SearchType.DBUser, "users:"))) {
            users.put(user.getuser_id(), user);
        }
        return users;
    }

    /**
     * injects user/group base on permission owner type
     * @param entity the permission to map
     * @param user the permission owner
     * @return permission
     */
    public Permission map(permissions entity, DbUser user) {
        Permission template = new Permission();
        if (entity.getad_element_id() != null && user != null) {
            if (isUser(user)) {
                template.setUser(new User());
                template.getUser().setId(entity.getad_element_id().toString());
            } else if (entity.getad_element_id() != null) {
                template.setGroup(new Group());
                template.getGroup().setId(entity.getad_element_id().toString());
            }
        }
        return map(entity, template);
    }

    //REVISIT: fix once BE can distinguish between the user and group
    private boolean isUser(DbUser user) {
        return StringHelper.isNullOrEmpty(user.getusername()) ? false : true;
    }

    /**
     * @pre completeness of "user|group.id" already validated if not
     * user sub-collection
     */
    protected PermissionsOperationsParametes getPrincipal(permissions entity, Permission permission) {
        PermissionsOperationsParametes ret = null;
        if (isUserSubCollection() || permission.isSetUser()) {
            VdcUser user = new VdcUser();
            user.setUserId(isUserSubCollection()
                           ? targetId
                           : asGuid(permission.getUser().getId()));
            user.setDomainControler(getCurrent().get(Principal.class).getDomain());
            ret = new PermissionsOperationsParametes(entity, user);
        } else if (isGroupSubCollection() || permission.isSetGroup()) {
            ad_groups group = new ad_groups();
            group.setid(isGroupSubCollection()
                        ? targetId
                        : asGuid(permission.getGroup().getId()));
            group.setdomain(getCurrent().get(Principal.class).getDomain());
            ret = new PermissionsOperationsParametes(entity, group);
        }
        return ret;
    }

    @Override
    public Permission addParents(Permission permission) {
        // REVISIT for entity-level permissions we need an isUser
        // flag on the permissions entity in order to distinguish
        // between the user and group cases
        if (isGroupSubCollection() && permission.isSetUser() && permission.getUser().isSetId()) {
            permission.setGroup(new Group());
            permission.getGroup().setId(permission.getUser().getId());
            permission.setUser(null);
        }
        return permission;
    }

    protected permissions getPermissionsTemplate(Permission perm) {
        permissions permission = new permissions();
        // allow the target Id to be implicit in the client-provided
        // representation
        if (isPrincipalSubCollection()) {
            permission.setad_element_id(targetId);
            permission.setObjectId(getMapper(Permission.class, Guid.class).map(perm, null));
        } else {
            permission.setad_element_id(asGuid(perm.getUser().getId()));
            permission.setObjectId(targetId);
            permission.setObjectType(objectType);
        }
        return permission;
    }

    protected boolean isPrincipalSubCollection() {
        return isUserSubCollection() || isGroupSubCollection();
    }

    protected boolean isUserSubCollection() {
        return User.class.equals(suggestedParentType);
    }

    protected boolean isGroupSubCollection() {
        return Group.class.equals(suggestedParentType);
    }

    protected permissions getPermissions(String id) {
        return getEntity(permissions.class,
                         VdcQueryType.GetPermissionById,
                         new MultilevelAdministrationByPermissionIdParameters(new Guid(id)),
                         id);
    }
}
