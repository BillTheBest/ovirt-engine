package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.common.util.ReflectionHelper.assignChildModel;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.Users;
import org.ovirt.engine.api.resource.UserResource;
import org.ovirt.engine.api.resource.UsersResource;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.common.queries.GetDbUserByUserIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

public class BackendUsersResource extends AbstractBackendCollectionResource<User, DbUser>
        implements UsersResource {

    static final String[] SUB_COLLECTIONS = { "permissions", "roles", "tags" };
    protected static final String AD_SEARCH_TEMPLATE = "ADUSER@{0}: ";
    private static final String USERS_SEARCH_PATTERN = "usrname != \"\"";
    private static final String AND_SEARCH_PATTERN = " and ";

    private BackendDomainResource parent;

    public BackendUsersResource() {
        super(User.class, DbUser.class, SUB_COLLECTIONS);
    }

    public BackendUsersResource(String id, BackendDomainResource parent) {
        super(User.class, DbUser.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    @Override
    public Users list() {
        if(parent==null)
            return mapDbUserCollection(getBackendCollection(SearchType.DBUser, getSearchPattern()));
        return mapDomainUserCollection(getUsersFromDomain());
    }

    private String getSearchPattern() {
        String user_defined_pattern = QueryHelper.getConstraint(getUriInfo(), "",  modelType);
        return user_defined_pattern.equals("Users : ") ?
               user_defined_pattern + USERS_SEARCH_PATTERN
               :
               user_defined_pattern + AND_SEARCH_PATTERN + USERS_SEARCH_PATTERN;
    }

    @Override
    public Response add(User user) {
        validateParameters(user, "userName");
        AdUser adUser = getEntity(AdUser.class,
                                  SearchType.AdUser,
                                  getSearchPattern(user.getUserName()));
        AddUserParameters newUser = new AddUserParameters();
        newUser.setVdcUser(map(adUser));
        return performCreation(VdcActionType.AddUser, newUser, new UserIdResolver(adUser.getUserId()));
    }

    protected String getSearchPattern(String param) {
        String constraint = QueryHelper.getConstraint(getUriInfo(), DbUser.class, false);
        StringBuffer sb = new StringBuffer();

        sb.append(MessageFormat.format(AD_SEARCH_TEMPLATE,
                  parent!=null?
                        parent.getDirectory().getName()
                        :
                        getCurrent().get(Principal.class).getDomain()));

         sb.append(StringHelper.isNullOrEmpty(constraint)?
                        "allnames=" + param
                        :
                        constraint);

         return sb.toString();
    }

    protected List<AdUser> getUsersFromDomain() {
        return asCollection(AdUser.class,
                getEntity(ArrayList.class,
                        SearchType.AdUser,
                        getSearchPattern("*")));

    }

    @Override
    public void performRemove(String id) {
        performAction(VdcActionType.RemoveUser, new AdElementParametersBase(asGuid(id)));
    }

    @Override
    @SingleEntityResource
    public UserResource getUserSubResource(String id) {
        return inject(new BackendUserResource(id, this));
    }

    protected Users mapDbUserCollection(List<DbUser> entities) {
        Users collection = new Users();
        for (DbUser entity : entities) {
            collection.getUsers().add(addLinks(modifyDomain(map(entity)),
                                               BaseResource.class));
        }
        return collection;
    }

    @Override
    protected User addParents(User user) {
        if(parent!=null){
            assignChildModel(user, User.class).setId(parent.get().getId());
        }
        return user;
    }

    protected Users mapDomainUserCollection(List<AdUser> entities) {
        Users collection = new Users();
        for (AdUser entity : entities) {
            collection.getUsers().add(addLinks(modifyDomain(mapAdUser(entity)),
                                               true));
        }
        return collection;
    }

    private User modifyDomain(User user) {
        if(user.getDomain()!=null)
            user.getDomain().setName(null);
        return user;
    }

    protected VdcUser map(AdUser adUser) {
        return getMapper(AdUser.class, VdcUser.class).map(adUser, null);
    }

    protected User mapAdUser(AdUser adUser) {
        return getMapper(AdUser.class, User.class).map(adUser, null);
    }

    public DbUser lookupUserById(Guid id) {
        return getEntity(DbUser.class,
                         VdcQueryType.GetDbUserByUserId,
                         new GetDbUserByUserIdParameters(id),
                         id.toString());
    }

    protected class UserIdResolver extends EntityIdResolver {

        private Guid id;

        UserIdResolver(Guid id) {
            this.id = id;
        }

        @Override
        public DbUser lookupEntity(Guid nullId) {
            return lookupUserById(id);
        }
    }
}
