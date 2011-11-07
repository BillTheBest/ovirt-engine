package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Fault;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetTasksStatusesByTasksIDsParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import org.junit.Test;

import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqActionParams;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqQueryParams;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqSearchParams;

import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;

public abstract class AbstractBackendCollectionResourceTest<R extends BaseResource, Q /* extends IVdcQueryable */, C extends AbstractBackendCollectionResource<R, Q>>
        extends AbstractBackendResourceTest<R, Q> {

    protected static final String QUERY = "name=s* AND id=*0";

    protected C collection;
    protected SearchType searchType;
    protected String prefix;

    protected AbstractBackendCollectionResourceTest(C collection, SearchType searchType,
            String prefix) {
        this.collection = collection;
        this.searchType = searchType;
        this.prefix = prefix;
    }

    protected abstract List<R> getCollection();

    protected void init() {
        initResource(collection);
    }

    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testQuery() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(QUERY);

        setUpQueryExpectations(QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testListFailure() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpQueryExpectations("", FAILURE);
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
        setUpQueryExpectations("", t);
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
        setUpQueryExpectations("", t);
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

    @SuppressWarnings("unchecked")
    @Test
    public void testSubResourceInjection() throws Exception {
        control.replay();
        // walk super-interface hierarchy to find non-inherited method annotations
        for (Class<?> resourceInterface : collection.getClass().getInterfaces()) {
            for (Method method : resourceInterface.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Path.class) && isSubResourceLocator(method)) {
                    Object rawSubResource = method.invoke(collection, getSubResourceId());
                    if (rawSubResource instanceof AbstractBackendResource) {
                        AbstractBackendResource<R, Q> subResource = (AbstractBackendResource<R, Q>)rawSubResource;
                        assertNotNull(subResource.getBackend());
                        assertNotNull(subResource.getSessionHelper());
                        assertNotNull(subResource.getMappingLocator());
                    }
                }
            }
        }
    }

    protected String getSubResourceId() {
        return GUIDS[3].toString();
    }

    protected boolean isSubResourceLocator(Method method) {
        return method.getName().startsWith("get")
               && method.getName().toLowerCase().endsWith("resource")
               && method.getParameterTypes().length == 1
               && String.class.equals(method.getParameterTypes()[0])
               && method.getReturnType() != null;
    }

    protected void setUriInfo(UriInfo uriInfo) {
        collection.setUriInfo(uriInfo);
    }

    @SuppressWarnings("unchecked")
    protected UriInfo setUpUriExpectations(String query) {
        UriInfo uriInfo = setUpBasicUriExpectations();
        MultivaluedMap<String, String> queries = control.createMock(MultivaluedMap.class);
        List<String> queryParam = new ArrayList<String>();
        if (!(query == null || "".equals(query))) {
            queryParam.add(QUERY);
        }
        expect(queries.get("search")).andReturn(queryParam).anyTimes();
        expect(uriInfo.getQueryParameters()).andReturn(queries).anyTimes();
        return uriInfo;
    }

    protected void setUpQueryExpectations(String query) throws Exception {
        setUpQueryExpectations(query, null);
    }

    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        SearchParameters params = new SearchParameters(prefix + query, searchType);
        expect(queryResult.getSucceeded()).andReturn(failure == null).anyTimes();
        if (failure == null) {
            List<Q> entities = new ArrayList<Q>();
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

    protected void setUpCreationExpectations(VdcActionType task,
                                             Class<? extends VdcActionParametersBase> taskClass,
                                             String[] taskNames,
                                             Object[] taskValues,
                                             boolean canDo,
                                             boolean success,
                                             Object taskReturn,
                                             VdcQueryType query,
                                             Class<? extends VdcQueryParametersBase> queryClass,
                                             String[] queryNames,
                                             Object[] queryValues,
                                             Object queryReturn) {
        setUpCreationExpectations(task,
                                  taskClass,
                                  taskNames,
                                  taskValues,
                                  canDo,
                                  success,
                                  taskReturn,
                                  null,
                                  null,
                                  query,
                                  queryClass,
                                  queryNames,
                                  queryValues,
                                  queryReturn);
    }

    protected void setUpCreationExpectations(VdcActionType task,
                                             Class<? extends VdcActionParametersBase> taskClass,
                                             String[] taskNames,
                                             Object[] taskValues,
                                             boolean canDo,
                                             boolean success,
                                             Object taskReturn,
                                             ArrayList<Guid> asyncTasks,
                                             ArrayList<AsyncTaskStatus> asyncStatuses,
                                             VdcQueryType query,
                                             Class<? extends VdcQueryParametersBase> queryClass,
                                             String[] queryNames,
                                             Object[] queryValues,
                                             Object queryReturn) {
        VdcReturnValueBase taskResult = control.createMock(VdcReturnValueBase.class);
        expect(taskResult.getCanDoAction()).andReturn(canDo).anyTimes();
        if (canDo) {
            expect(taskResult.getSucceeded()).andReturn(success).anyTimes();
            if (success) {
                expect(taskResult.getActionReturnValue()).andReturn(taskReturn).anyTimes();
            } else {
                expect(taskResult.getExecuteFailedMessages()).andReturn(asList(FAILURE)).anyTimes();
            }
        } else {
            expect(taskResult.getCanDoActionMessages()).andReturn(asList(CANT_DO)).anyTimes();
        }
        expect(taskResult.getHasAsyncTasks()).andReturn(asyncTasks != null).anyTimes();
        if (asyncTasks != null) {
            expect(taskResult.getTaskIdList()).andReturn(asyncTasks).anyTimes();
            VdcQueryReturnValue monitorResult = control.createMock(VdcQueryReturnValue.class);
            expect(monitorResult.getSucceeded()).andReturn(success).anyTimes();
            expect(monitorResult.getReturnValue()).andReturn(asyncStatuses).anyTimes();
            expect(backend.RunQuery(eq(VdcQueryType.GetTasksStatusesByTasksIDs),
                                    eqQueryParams(GetTasksStatusesByTasksIDsParameters.class,
                                                  addSession(new String[]{}),
                                                  addSession(new Object[]{})))).andReturn(monitorResult);
        }
        expect(backend.RunAction(eq(task), eqActionParams(taskClass, addSession(taskNames), addSession(taskValues))))
                .andReturn(taskResult);

        if (canDo && success && query != null) {
            setUpEntityQueryExpectations(query, queryClass, queryNames, queryValues, queryReturn);
        }
        control.replay();
    }

    protected void setUpHttpHeaderExpectations(String name, String value) {
        expect(httpHeaders.getRequestHeader(eq(name))).andReturn(asList(value));
    }

    protected VDSGroup setUpVDSGroup(Guid id) {
        VDSGroup cluster = control.createMock(VDSGroup.class);
        expect(cluster.getID()).andReturn(id).anyTimes();
        return cluster;
    }

    protected void verifyCollection(List<R> collection) throws Exception {
        assertNotNull(collection);
        assertEquals(NAMES.length, collection.size());
        for (int i = 0; i < NAMES.length; i++) {
            verifyModel(collection.get(i), i);
        }
    }
}
