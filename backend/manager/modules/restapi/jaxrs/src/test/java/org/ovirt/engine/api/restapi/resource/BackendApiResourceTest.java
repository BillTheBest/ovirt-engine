package org.ovirt.engine.api.restapi.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;

import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqQueryParams;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.api.common.security.auth.Principal;
import org.ovirt.engine.api.model.API;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.GetSystemStatisticsQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.api.restapi.logging.MessageBundle;
import org.ovirt.engine.api.restapi.util.SessionHelper;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { Config.class })
public class BackendApiResourceTest extends Assert {

    protected BackendApiResource resource;

    protected BackendLocal backend;
    protected Current current;
    protected Principal principal;
    protected SessionHelper sessionHelper;
    protected HttpHeaders httpHeaders;

    protected static final String USER = "Aladdin";
    protected static final String SECRET = "open sesame";
    protected static final String DOMAIN = "Maghreb";

    protected static final String URI_ROOT = "http://localhost:8099";
    protected static final String SLASH = "/";
    protected static final String BASE_PATH = "/api";
    protected static final String URI_BASE = URI_ROOT + BASE_PATH;
    protected static final String BUNDLE_PATH = "org/ovirt/engine/api/restapi/logging/Messages";

    protected static final int MAJOR = 11;
    protected static final int MINOR = 0;
    protected static final int BUILD = 99;
    protected static final int REVISION = 13;
    protected static final String SYSTEM_VERSION =
        Integer.toString(MAJOR) + "." +
        Integer.toString(MINOR) + "." +
        Integer.toString(BUILD) + "." +
        Integer.toString(REVISION);

    protected static int TOTAL_VMS = 123456;
    protected static int ACTIVE_VMS = 23456;
    protected static int TOTAL_HOSTS = 23456;
    protected static int ACTIVE_HOSTS = 3456;
    protected static int TOTAL_USERS = 3456;
    protected static int ACTIVE_USERS = 456;
    protected static int TOTAL_STORAGE_DOMAINS = 56;
    protected static int ACTIVE_STORAGE_DOMAINS = 6;

    private static final String[] relationships = {
        "capabilities",
        "clusters",
        "clusters/search",
        "datacenters",
        "datacenters/search",
        "events",
        "events/search",
        "hosts",
        "hosts/search",
        "networks",
        "roles",
        "storagedomains",
        "storagedomains/search",
        "tags",
        "templates",
        "templates/search",
        "users",
        "users/search",
        "groups",
        "groups/search",
        "domains",
        "vmpools",
        "vmpools/search",
        "vms",
        "vms/search",
    };

    private static final String[] hrefs = {
        BASE_PATH + "/capabilities",
        BASE_PATH + "/clusters",
        BASE_PATH + "/clusters?search={query}",
        BASE_PATH + "/datacenters",
        BASE_PATH + "/datacenters?search={query}",
        BASE_PATH + "/events",
        BASE_PATH + "/events?search={query}&from={event_id}",
        BASE_PATH + "/hosts",
        BASE_PATH + "/hosts?search={query}",
        BASE_PATH + "/networks",
        BASE_PATH + "/roles",
        BASE_PATH + "/storagedomains",
        BASE_PATH + "/storagedomains?search={query}",
        BASE_PATH + "/tags",
        BASE_PATH + "/templates",
        BASE_PATH + "/templates?search={query}",
        BASE_PATH + "/users",
        BASE_PATH + "/users?search={query}",
        BASE_PATH + "/groups",
        BASE_PATH + "/groups?search={query}",
        BASE_PATH + "/domains",
        BASE_PATH + "/vmpools",
        BASE_PATH + "/vmpools?search={query}",
        BASE_PATH + "/vms",
        BASE_PATH + "/vms?search={query}",
    };

    public BackendApiResourceTest() {
        resource = new BackendApiResource();
    }

    @Before
    public void setUp() {
        current = createMock(Current.class);
        principal = new Principal(USER, SECRET, DOMAIN);
        expect(current.get(Principal.class)).andReturn(principal).anyTimes();

        sessionHelper = new SessionHelper();
        sessionHelper.setCurrent(current);
        resource.setSessionHelper(sessionHelper);

        backend = createMock(BackendLocal.class);
        resource.setBackend(backend);

        MessageBundle messageBundle = new MessageBundle();
        messageBundle.setPath(BUNDLE_PATH);
        messageBundle.populate();
        resource.setMessageBundle(messageBundle);

        httpHeaders = createMock(HttpHeaders.class);
        List<Locale> locales = new ArrayList<Locale>();
        expect(httpHeaders.getAcceptableLanguages()).andReturn(locales).anyTimes();
        resource.setHttpHeaders(httpHeaders);
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testGet() {
        doTestGet(URI_BASE);
    }

    @Test
    public void testGetWithTrailingSlash() {
        doTestGet(URI_BASE + "/");
    }

    protected void doTestGet(String base) {
        resource.setUriInfo(setUpUriInfo(base));
        setUpGetSystemVersionExpectations();
        setUpGetSystemStatisticsExpectations();

        verifyResponse(resource.get());
    }

    protected HashMap<String, Integer> setUpStats() {
        HashMap<String, Integer> stats = new HashMap<String, Integer>();

        stats.put("total_vms", TOTAL_VMS);
        stats.put("active_vms", ACTIVE_VMS);
        stats.put("total_vds", TOTAL_HOSTS);
        stats.put("active_vds", ACTIVE_HOSTS);
        stats.put("total_users", TOTAL_USERS);
        stats.put("active_users", ACTIVE_USERS);
        stats.put("total_storage_domains", TOTAL_STORAGE_DOMAINS);
        stats.put("active_storage_domains", ACTIVE_STORAGE_DOMAINS);

        return stats;
    }

    protected void verifyResponse(Response response) {
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity() instanceof API);
        verifyApi((API)response.getEntity());
    }

    protected void verifyApi(API api) {
        assertNotNull(api);
        assertNotNull(api.getLinks());

        assertEquals(relationships.length, api.getLinks().size());
        for (int i = 0; i < relationships.length; i++) {
            Link l = api.getLinks().get(i);
            assertNotNull(l);
            assertEquals(relationships[i], l.getRel());
            assertEquals(hrefs[i], l.getHref());
        }

        assertNotNull(api.getProductInfo());
        assertNotNull(api.getProductInfo().getVersion());
        assertEquals(MAJOR,    api.getProductInfo().getVersion().getMajor());
        assertEquals(MINOR,    api.getProductInfo().getVersion().getMinor());
        assertEquals(BUILD,    api.getProductInfo().getVersion().getBuild());
        assertEquals(REVISION, api.getProductInfo().getVersion().getRevision());

        assertNotNull(api.getSummary());
        assertEquals(TOTAL_VMS,              api.getSummary().getVMs().getTotal());
        assertEquals(ACTIVE_VMS,             api.getSummary().getVMs().getActive());
        assertEquals(TOTAL_HOSTS,            api.getSummary().getHosts().getTotal());
        assertEquals(ACTIVE_HOSTS,           api.getSummary().getHosts().getActive());
        assertEquals(TOTAL_USERS,            api.getSummary().getUsers().getTotal());
        assertEquals(ACTIVE_USERS,           api.getSummary().getUsers().getActive());
        assertEquals(TOTAL_STORAGE_DOMAINS,  api.getSummary().getStorageDomains().getTotal());
        assertEquals(ACTIVE_STORAGE_DOMAINS, api.getSummary().getStorageDomains().getActive());
    }

    private static void assertEquals(long expected, Long actual) {
        assertEquals(expected, actual.longValue());
    }

    protected UriInfo setUpUriInfo(String base) {
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriBuilder.clone()).andReturn(uriBuilder).anyTimes();

        for (String rel : relationships) {
            UriBuilder colUriBuilder = createMock(UriBuilder.class);
            expect(colUriBuilder.build()).andReturn(URI.create(URI_ROOT + SLASH + rel+ "/")).anyTimes();
            if (rel.endsWith("/search")) {
                expect(uriBuilder.path(rel.replace("/search", ""))).andReturn(colUriBuilder);
            } else {
                expect(uriBuilder.path(rel)).andReturn(colUriBuilder);
            }
        }

        UriInfo uriInfo = createMock(UriInfo.class);
        expect(uriInfo.getBaseUri()).andReturn(URI.create(base)).anyTimes();
        expect(uriInfo.getBaseUriBuilder()).andReturn(uriBuilder);

        return uriInfo;
    }

    protected void setUpGetSystemVersionExpectations() {
        VdcQueryReturnValue queryResult = createMock(VdcQueryReturnValue.class);

        expect(backend.RunQuery(eq(VdcQueryType.GetConfigurationValue), queryVdcVersionParams())).andReturn(queryResult);

        expect(queryResult.getSucceeded()).andReturn(true).anyTimes();
        expect(queryResult.getReturnValue()).andReturn(SYSTEM_VERSION).anyTimes();
    }

    protected void setUpGetSystemStatisticsExpectations() {
        VdcQueryReturnValue queryResult = createMock(VdcQueryReturnValue.class);

        expect(backend.RunQuery(eq(VdcQueryType.GetSystemStatistics), queryParams())).andReturn(queryResult);

        expect(queryResult.getSucceeded()).andReturn(true).anyTimes();
        expect(queryResult.getReturnValue()).andReturn(setUpStats()).anyTimes();

        replayAll();
    }

    protected VdcQueryParametersBase queryVdcVersionParams() {
        return eqQueryParams(GetConfigurationValueParameters.class,
                             new String[] { "SessionId"},
                             new Object[] { getSessionId() });
    }

    protected VdcQueryParametersBase queryParams() {
        return eqQueryParams(GetSystemStatisticsQueryParameters.class,
                             new String[] { "SessionId" },
                             new Object[] { getSessionId() });
    }

    protected String getSessionId() {
        return sessionHelper.getSessionId(principal);
    }
}

