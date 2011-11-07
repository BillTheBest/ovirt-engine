package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqSearchParams;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetDbUserByUserIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.NGuid;

public class BackendUsersResourceTest
    extends AbstractBackendCollectionResourceTest<User, DbUser, BackendUsersResource> {

    static final String GROUPS =
        "Schema Admins@Maghreb/Users,Group Policy Creator Owners@Maghreb/Users,Enterprise Admins@Maghreb/Users";
    static final String[] PARSED_GROUPS =
        { "Schema Admins@Maghreb/Users", "Group Policy Creator Owners@Maghreb/Users", "Enterprise Admins@Maghreb/Users" };

    protected static final String SEARCH_QUERY = "Users : name=s* AND id=*0 and usrname != \"\"";
    protected static final String QUERY = "Users : usrname != \"\"";

    public BackendUsersResourceTest() {
        super(new BackendUsersResource(), SearchType.DBUser, "Users : ");
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveUser,
                                           AdElementParametersBase.class,
                                           new String[] { "AdElementId" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        collection.remove(GUIDS[0].toString());
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(VdcQueryType.GetDbUserByUserId,
                GetDbUserByUserIdParameters.class,
                new String[] { "UserId" },
                new Object[] { NON_EXISTANT_GUID },
                null);
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetDbUserByUserId,
                GetDbUserByUserIdParameters.class,
                new String[] { "UserId" },
                new Object[] { GUIDS[0] },
                getEntity(0));
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveUser,
                                           AdElementParametersBase.class,
                                           new String[] { "AdElementId" },
                                           new Object[] { GUIDS[0] },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddUser() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations("ADUSER@" + DOMAIN + ": allnames=" + NAMES[0],
                                   SearchType.AdUser,
                                   getAdUser(0));
        setUpCreationExpectations(VdcActionType.AddUser,
                                  AddUserParameters.class,
                                  new String[] { "VdcUser.UserId" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetDbUserByUserId,
                                  GetDbUserByUserIdParameters.class,
                                  new String[] { "UserId" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        User model = new User();
        model.setUserName(NAMES[0]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof User);
        verifyModel((User) response.getEntity(), 0);
    }

    @Override
    protected List<User> getCollection() {
        return collection.list().getUsers();
    }

    @Override
    protected DbUser getEntity(int index) {
        DbUser entity = new DbUser();
        entity.setuser_id(GUIDS[index]);
        entity.setusername(NAMES[index]);
        entity.setgroups(GROUPS);
        entity.setdomain(DOMAIN);
        return entity;
    }

    protected AdUser getAdUser(int index) {
        AdUser adUser = new AdUser();
        adUser.setUserId(GUIDS[index]);
        adUser.setUserName(NAMES[index]);
        adUser.setDomainControler(DOMAIN);
        return adUser;
    }

    @Override
    protected void verifyModel(User model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getUserName());
        assertNotNull(model.getDomain());
        assertEquals(new NGuid(DOMAIN.getBytes(),true).toString(), model.getDomain().getId());
        assertTrue(model.isSetGroups());
        assertEquals(PARSED_GROUPS.length, model.getGroups().getGroups().size());
        for (int i = 0 ; i < PARSED_GROUPS.length ; i++) {
            Group group = model.getGroups().getGroups().get(i);
            assertEquals(PARSED_GROUPS[i], group.getName());
        }
        verifyLinks(model);
    }

    public static AdUser setUpEntityExpectations(AdUser entity, int index) {
        expect(entity.getUserId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getDomainControler()).andReturn(DOMAIN).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        return entity;
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        SearchParameters params = new SearchParameters(query, searchType);
        expect(queryResult.getSucceeded()).andReturn(failure == null).anyTimes();
        if (failure == null) {
            List<DbUser> entities = new ArrayList<DbUser>();
            for (int i = 0; i < NAMES.length; i++) {
                entities.add(getEntity(i));
            }
            expect(queryResult.getReturnValue()).andReturn(entities).anyTimes();
        } else {
            if (failure instanceof String) {
                expect(queryResult.getExceptionString()).andReturn((String) failure).anyTimes();
                setUpL10nExpectations((String)failure);
            } else if (failure instanceof Exception) {
                expect(queryResult.getExceptionString()).andThrow((Exception) failure).anyTimes();
            }
        }
        expect(backend.RunQuery(eq(VdcQueryType.Search), eqSearchParams(params))).andReturn(
                queryResult);
        control.replay();
    }

    @Override
    protected void setUpQueryExpectations(String query) throws Exception {
        setUpQueryExpectations(query, null);
    }

    @Test
    public void testQuery() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(SEARCH_QUERY);

        setUpQueryExpectations(SEARCH_QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpQueryExpectations(QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testListFailure() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpQueryExpectations(QUERY, FAILURE);
        collection.setUriInfo(uriInfo);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertTrue(wae.getResponse().getEntity() instanceof Fault);
            assertEquals(mockl10n(FAILURE), ((Fault) wae.getResponse().getEntity()).getDetail());
        }
    }

    @Test
    public void testListCrash() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        Throwable t = new RuntimeException(FAILURE);
        setUpQueryExpectations(QUERY, t);
        collection.setUriInfo(uriInfo);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, t);
        }
    }

    @Test
    public void testListCrashClientLocale() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        locales.add(CLIENT_LOCALE);

        Throwable t = new RuntimeException(FAILURE);
        setUpQueryExpectations(QUERY, t);
        collection.setUriInfo(uriInfo);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_CLIENT_LOCALE, t);
        } finally {
            locales.clear();
        }
    }
}
