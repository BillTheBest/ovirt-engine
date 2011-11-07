package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.ReadOnlyDeviceResource;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.businessentities.VM;

public class BackendReadOnlyCdRomsResourceTest
        extends AbstractBackendCdRomsResourceTest<BackendReadOnlyCdRomsResource<VM>> {

    public BackendReadOnlyCdRomsResourceTest() {
        super(new BackendReadOnlyCdRomsResource<VM>
                                    (VM.class,
                                     PARENT_ID,
                                     VdcQueryType.GetVmByVmId,
                                     new GetVmByVmIdParameters(PARENT_ID)),
              VdcQueryType.GetVmByVmId,
              new GetVmByVmIdParameters(PARENT_ID),
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
}
