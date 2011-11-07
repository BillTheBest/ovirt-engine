package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqActionParams;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqQueryParams;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.interfaces.ErrorTranslator;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetTasksStatusesByTasksIDsParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.api.restapi.logging.MessageBundle;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.api.restapi.types.MappingLocator;
import org.ovirt.engine.api.restapi.util.SessionHelper;

import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqSearchParams;

public abstract class AbstractBackendResourceTest<R extends BaseResource, Q /* extends IVdcQueryable */>
        extends Assert {

    protected static final Guid[] GUIDS = { new Guid("00000000-0000-0000-0000-000000000000"),
            new Guid("11111111-1111-1111-1111-111111111111"),
            new Guid("22222222-2222-2222-2222-222222222222"),
            new Guid("33333333-3333-3333-3333-333333333333") };
    protected static final Guid NON_EXISTANT_GUID = new Guid("99999999-9999-9999-9999-999999999999");
    protected static final String[] NAMES = { "sedna", "eris", "orcus" };
    protected static final String[] DESCRIPTIONS = { "top notch entity", "a fine example",
            "state of the art" };
    protected static final String URI_ROOT = "http://localhost:8088";
    protected static final String BASE_PATH = "/api";
    protected static final String URI_BASE = URI_ROOT + BASE_PATH;
    protected static final String BUNDLE_PATH = "org/ovirt/engine/api/restapi/logging/Messages";

    protected static final String CANT_DO = "circumstances outside our control";
    protected static final String FAILURE = "a fine mess";
    protected static final String BACKEND_FAILED_SERVER_LOCALE = "Ruckenende ist kaput";
    protected static final String BACKEND_FAILED_CLIENT_LOCALE = "Theip ar an obair";
    protected static final String INCOMPLETE_PARAMS_REASON_SERVER_LOCALE = "Unvollstandig Parameter";
    protected static final String INCOMPLETE_PARAMS_DETAIL_SERVER_LOCALE = " erforderlich fur ";
    protected static final Locale CLIENT_LOCALE = new Locale("ga", "IE");

    protected static int SERVER_ERROR = 500;
    protected static int BAD_REQUEST = 400;

    protected static final String USER = "Aladdin";
    protected static final String SECRET = "open sesame";
    protected static final String DOMAIN = "Maghreb.Maghreb.Maghreb.com";

    protected BackendLocal backend;
    protected Current current;
    protected Principal principal;
    protected SessionHelper sessionHelper;
    protected MappingLocator mapperLocator;
    protected Locale locale;
    protected HttpHeaders httpHeaders;
    protected List<Locale> locales;
    protected List<String> accepts;

    protected MessageBundle messageBundle;
    protected IMocksControl control;

    @Before
    public void setUp() {
        control = EasyMock.createNiceControl();
        backend = control.createMock(BackendLocal.class);
        current = control.createMock(Current.class);
        sessionHelper = new SessionHelper();
        sessionHelper.setCurrent(current);
        principal = new Principal(USER, SECRET, DOMAIN);
        expect(current.get(Principal.class)).andReturn(principal).anyTimes();
        httpHeaders = control.createMock(HttpHeaders.class);
        locales = new ArrayList<Locale>();
        expect(httpHeaders.getAcceptableLanguages()).andReturn(locales).anyTimes();
        accepts = new ArrayList<String>();
        expect(httpHeaders.getRequestHeader("Accept")).andReturn(accepts).anyTimes();
        mapperLocator = new MappingLocator();
        mapperLocator.populate();
        locale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
        messageBundle = new MessageBundle();
        messageBundle.setPath(BUNDLE_PATH);
        messageBundle.populate();
        init();
    }

    protected <F, T> Mapper<F, T> getMapper(Class<F> from, Class<T> to) {
        return mapperLocator.getMapper(from, to);
    }

    @After
    public void tearDown() {
        Locale.setDefault(locale);
        control.verify();
    }

    protected void initResource(AbstractBackendResource<R, Q> resource) {
        resource.setBackend(backend);
        resource.setMappingLocator(mapperLocator);
        resource.setSessionHelper(sessionHelper);
        resource.setMessageBundle(messageBundle);
        resource.setHttpHeaders(httpHeaders);
    }

    protected abstract void init();

    protected abstract Q getEntity(int index);

    protected void verifyModel(R model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        verifyLinks(model);
    }

    protected UriInfo setUpBasicUriExpectations() {
        UriInfo uriInfo = control.createMock(UriInfo.class);
        expect(uriInfo.getBaseUri()).andReturn(URI.create(URI_BASE)).anyTimes();
        return uriInfo;
    }

    protected <E> void setUpGetEntityExpectations(VdcQueryType query,
            Class<? extends VdcQueryParametersBase> clz, String[] names, Object[] values, E entity)
            throws Exception {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        expect(backend.RunQuery(eq(query), eqQueryParams(clz, addSession(names), addSession(values)))).andReturn(
                queryResult);
        expect(queryResult.getSucceeded()).andReturn(true).anyTimes();
        expect(queryResult.getReturnValue()).andReturn(entity).anyTimes();
    }

    protected <E> void setUpGetEntityExpectations(String query,
                                                  SearchType type,
                                                  E entity) throws Exception {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        SearchParameters params = new SearchParameters(query, type);
        expect(backend.RunQuery(eq(VdcQueryType.Search),
                                eqSearchParams(params))).andReturn(queryResult);
        expect(queryResult.getSucceeded()).andReturn(true).anyTimes();
        List<E> entities = new ArrayList<E>();
        entities.add(entity);
        expect(queryResult.getReturnValue()).andReturn(entities).anyTimes();
    }

    protected void setUpEntityQueryExpectations(VdcQueryType query,
                                                Class<? extends VdcQueryParametersBase> queryClass,
                                                String[] queryNames,
                                                Object[] queryValues,
                                                Object queryReturn) {
        setUpEntityQueryExpectations(query, queryClass, queryNames, queryValues, queryReturn, null);
    }

    protected void setUpEntityQueryExpectations(VdcQueryType query,
                                                Class<? extends VdcQueryParametersBase> queryClass,
                                                String[] queryNames,
                                                Object[] queryValues,
                                                Object queryReturn,
                                                Object failure) {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        expect(queryResult.getSucceeded()).andReturn(failure == null).anyTimes();
        if (failure == null) {
            expect(queryResult.getReturnValue()).andReturn(queryReturn).anyTimes();
        } else {
            if (failure instanceof String) {
                expect(queryResult.getExceptionString()).andReturn((String) failure).anyTimes();
                setUpL10nExpectations((String)failure);
            } else if (failure instanceof Exception) {
                expect(queryResult.getExceptionString()).andThrow((Exception) failure).anyTimes();
            }
        }
        expect(backend.RunQuery(eq(query),
                                eqQueryParams(queryClass,
                                              addSession(queryNames),
                                              addSession(queryValues)))).andReturn(queryResult);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz, String[] names, Object[] values,
            boolean canDo, boolean success) {
        return setUpActionExpectations(task, clz, names, values, canDo, success, null, true);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz, String[] names, Object[] values,
            boolean canDo, boolean success, boolean reply) {
        return setUpActionExpectations(task, clz, names, values, canDo, success, null, reply);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz, String[] names, Object[] values,
            boolean canDo, boolean success, Object taskReturn, boolean replay) {
        return setUpActionExpectations(task, clz, names, values, canDo, success, taskReturn, null, replay);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values,
                                              boolean canDo,
                                              boolean success,
                                              Object taskReturn,
                                              String baseUri,
                                              boolean replay) {
        return setUpActionExpectations(task, clz, names, values, canDo, success, taskReturn, null, null, baseUri, replay);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values,
                                              boolean canDo,
                                              boolean success,
                                              Object taskReturn,
                                              ArrayList<Guid> asyncTasks,
                                              ArrayList<AsyncTaskStatus> asyncStatuses,
                                              String baseUri,
                                              boolean replay) {
        VdcReturnValueBase result = control.createMock(VdcReturnValueBase.class);
        expect(result.getCanDoAction()).andReturn(canDo).anyTimes();
        if (canDo) {
            expect(result.getSucceeded()).andReturn(success).anyTimes();
            if (success) {
                if (taskReturn != null) {
                    expect(result.getActionReturnValue()).andReturn(taskReturn).anyTimes();
                }
            } else {
                expect(result.getExecuteFailedMessages()).andReturn(asList(FAILURE)).anyTimes();
                setUpL10nExpectations(asList(FAILURE));
            }
        } else {
            expect(result.getCanDoActionMessages()).andReturn(asList(CANT_DO)).anyTimes();
            setUpL10nExpectations(asList(CANT_DO));
        }
        expect(backend.RunAction(eq(task), eqActionParams(clz, addSession(names), addSession(values)))).andReturn(result);

        expect(result.getHasAsyncTasks()).andReturn(asyncTasks != null).anyTimes();
        if (asyncTasks != null) {
            expect(result.getTaskIdList()).andReturn(asyncTasks).anyTimes();
            VdcQueryReturnValue monitorResult = control.createMock(VdcQueryReturnValue.class);
            expect(monitorResult.getSucceeded()).andReturn(success).anyTimes();
            expect(monitorResult.getReturnValue()).andReturn(asyncStatuses).anyTimes();
            expect(backend.RunQuery(eq(VdcQueryType.GetTasksStatusesByTasksIDs),
                                    eqQueryParams(GetTasksStatusesByTasksIDsParameters.class,
                                                  addSession(new String[]{}),
                                                  addSession(new Object[]{})))).andReturn(monitorResult);
        }

        UriInfo uriInfo = setUpBasicUriExpectations();
        if (baseUri != null) {
            expect(uriInfo.getPath()).andReturn(baseUri).anyTimes();
        }

        if (replay) {
            control.replay();
        }
        return uriInfo;
    }

    protected void setUpL10nExpectations(String error) {
        ErrorTranslator translator = control.createMock(ErrorTranslator.class);
        IAnswer<String> answer = new IAnswer<String>() {
            public String answer() {
                return EasyMock.getCurrentArguments() != null && EasyMock.getCurrentArguments().length > 0
                       ? mockl10n((String)EasyMock.getCurrentArguments()[0])
                       : null;
            }
        };
        if (!locales.isEmpty()) {
            expect(translator.TranslateErrorTextSingle(eq(error), eq(locales.get(0)))).andAnswer(answer).anyTimes();
        } else {
            expect(translator.TranslateErrorTextSingle(eq(error))).andAnswer(answer).anyTimes();
        }
        expect(backend.getErrorsTranslator()).andReturn(translator).anyTimes();
    }

    protected void setUpL10nExpectations(ArrayList<String> errors) {
        ErrorTranslator errorTranslator = control.createMock(ErrorTranslator.class);
        if (!locales.isEmpty()) {
            expect(errorTranslator.TranslateErrorText(eq(errors), eq(locales.get(0)))).andReturn(mockl10n(errors)).anyTimes();
        } else {
            expect(errorTranslator.TranslateErrorText(eq(errors))).andReturn(mockl10n(errors)).anyTimes();
        }
        expect(backend.getErrorsTranslator()).andReturn(errorTranslator);
    }

    protected List<String> mockl10n(List<String> errors) {
        ArrayList<String> ret = new ArrayList<String>();
        for (String error : errors) {
            ret.add(mockl10n(error));
        }
        return ret;
    }

    protected String mockl10n(String s) {
        return s.startsWith("l10n...") ? s : "l10n..." + s;
    }

    protected void verifyLinks(BaseResource model) {
        assertNotNull(model.getHref());
        assertTrue(model.getHref().startsWith("/api"));
        for (Link link : model.getLinks()) {
            assertTrue(link.getHref().startsWith("/api"));
        }
    }

    protected void verifyFault(WebApplicationException wae, String detail) {
        verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, asList(mockl10n(detail)).toString(), BAD_REQUEST);
    }

    protected void verifyFault(WebApplicationException wae, String reason, String detail, int status) {
        assertEquals(status, wae.getResponse().getStatus());
        assertTrue(wae.getResponse().getEntity() instanceof Fault);
        Fault fault = (Fault) wae.getResponse().getEntity();
        assertEquals(reason, fault.getReason());
        assertEquals(detail, fault.getDetail());
    }

    protected void verifyFault(WebApplicationException wae, String reason, Throwable t) {
        assertEquals(SERVER_ERROR, wae.getResponse().getStatus());
        assertTrue(wae.getResponse().getEntity() instanceof Fault);
        Fault fault = (Fault) wae.getResponse().getEntity();
        assertEquals(reason, fault.getReason());
        assertNotNull(fault.getDetail());
        assertTrue("expected detail to include: " + t.getMessage(),
                   fault.getDetail().indexOf(t.getMessage()) != -1);
    }

    protected void verifyFault(Response response, String detail) {
        assertEquals(BAD_REQUEST, response.getStatus());
        assertTrue(response.getEntity() instanceof Fault);
        Fault fault = (Fault) response.getEntity();
        assertEquals(BACKEND_FAILED_SERVER_LOCALE, fault.getReason());
        assertEquals(asList(mockl10n(detail)).toString(), fault.getDetail());
    }

    protected void verifyIncompleteException(WebApplicationException wae, String type, String method, String... fields) {
        assertEquals(400, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals(INCOMPLETE_PARAMS_REASON_SERVER_LOCALE, fault.getReason());
        assertEquals(type + " " +  Arrays.asList(fields) + INCOMPLETE_PARAMS_DETAIL_SERVER_LOCALE + method, fault.getDetail());
    }

    protected void verifyNotFoundException(WebApplicationException wae) {
        assertEquals(404, wae.getResponse().getStatus());
    }

    protected <T> ArrayList<T> asList(T element) {
        ArrayList<T> list = new ArrayList<T>();
        list.add(element);
        return list;
    }

    protected String[] addSession(String... names) {
        String[] ret = new String[names.length + 1];
        for (int i = 0 ; i < names.length ; i++) {
            ret[i] = names[i];
        }
        ret[names.length] = "SessionId";
        return ret;
    }

    protected Object[] addSession(Object... values) {
        Object[] ret = new Object[values.length + 1];
        for (int i = 0 ; i < values.length ; i++) {
            ret[i] = values[i];
        }
        ret[values.length] = sessionHelper.getSessionId(principal);
        return ret;
    }
}
