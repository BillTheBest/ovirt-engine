package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.ReadOnlyDeviceResource;

import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendReadOnlyDisksResourceTest
        extends AbstractBackendDisksResourceTest<BackendReadOnlyDisksResource> {

    public BackendReadOnlyDisksResourceTest() {
        super(new BackendReadOnlyDisksResource(PARENT_ID,
                                               VdcQueryType.GetVmTemplatesDisks,
                                               new GetVmTemplatesDisksParameters(PARENT_ID)),
              VdcQueryType.GetVmTemplatesDisks,
              new GetVmTemplatesDisksParameters(PARENT_ID),
              "Id");
    }

    @Test
    public void testSubResourceLocator() throws Exception {
        control.replay();
        Object subResource = collection.getDeviceSubResource(GUIDS[0].toString());
        assertFalse(subResource instanceof DeviceResource);
        assertTrue(subResource instanceof ReadOnlyDeviceResource);
    }

    @Test
    public void testSubResourceLocatorBadGuid() throws Exception {
        control.replay();
        try {
            collection.getDeviceSubResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    @Override
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpEntityQueryExpectations(1, null);
        setUpGetStorageDomainsQueryExpectations(1);
        control.replay();
        collection.setUriInfo(uriInfo);
        List<Disk> disks = getCollection();
        for (Disk disk : disks) {
            assertNotNull(disk.getStorageDomains());
            List<StorageDomain> storageDomains = disk.getStorageDomains().getStorageDomains();
            assertEquals(storageDomains.size(), 2);
            assertEquals(storageDomains.get(0).getId(), GUIDS[2].toString());
            assertEquals(storageDomains.get(1).getId(), GUIDS[3].toString());
        }
        verifyCollection(disks);
    }

    @Test
    public void testListFailure() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpEntityQueryExpectations(1, FAILURE);
        setUpGetStorageDomainsQueryExpectations(1);
        control.replay();
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
        setUpEntityQueryExpectations(1, t);
        setUpGetStorageDomainsQueryExpectations(1);
        control.replay();
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
        setUpEntityQueryExpectations(1, t);
        setUpGetStorageDomainsQueryExpectations(1);
        control.replay();
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

    private int setUpGetStorageDomainsQueryExpectations(int times) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainsByVmTemplateId,
                    GetStorageDomainsByVmTemplateIdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { PARENT_ID },
                                         getStorageDomains(),
                                         null);
        }
        return times;
    }

        protected List<storage_domains> getStorageDomains() {
            List<storage_domains> storageDomains = new ArrayList<storage_domains>();
            storage_domains storageDomain = new storage_domains();
            storageDomain.setid(GUIDS[2]);
            storageDomains.add(storageDomain);
            storageDomain = new storage_domains();
            storageDomain.setid(GUIDS[3]);
            storageDomains.add(storageDomain);
            return storageDomains;
        }
}
