package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetAvailableStoragePoolVersionsParameters;
import org.ovirt.engine.core.common.queries.StoragePoolQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Version;

import static org.easymock.EasyMock.expect;

import static org.ovirt.engine.api.restapi.resource.BackendDataCentersResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendDataCentersResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendDataCentersResourceTest.verifyModelSpecific;

public class BackendDataCenterResourceTest
        extends AbstractBackendSubResourceTest<DataCenter, storage_pool, BackendDataCenterResource> {

    public BackendDataCenterResourceTest() {
        super(new BackendDataCenterResource(GUIDS[0].toString(), new BackendDataCentersResource()));
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendDataCenterResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpVersionExpectations();
        setUpGetEntityExpectations(1);
        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpVersionExpectations();
        setUpGetEntityExpectations(2);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateStoragePool,
                                           StoragePoolManagementParameter.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(1);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateStoragePool,
                                           StoragePoolManagementParameter.class,
                                           new String[] {},
                                           new Object[] {},
                                           canDo,
                                           success));

        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        control.replay();

        DataCenter model = getModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetStoragePoolById,
                                       StoragePoolQueryParametersBase.class,
                                       new String[] { "StoragePoolId" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : getEntity(0));
        }
    }

    protected void setUpVersionExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetAvailableStoragePoolVersions,
                                   GetAvailableStoragePoolVersionsParameters.class,
                                   new String[] { "StoragePoolId" },
                                   new Object[] { GUIDS[0] },
                                   getVersions());
    }

    @Override
    protected storage_pool getEntity(int index) {
        return setUpEntityExpectations(control.createMock(storage_pool.class), index);
    }

    protected List<Version> getVersions() {
        Version version = control.createMock(Version.class);
        expect(version.getMajor()).andReturn(2);
        expect(version.getMinor()).andReturn(3);
        List<Version> versions = new ArrayList<Version>();
        versions.add(version);
        return versions;
    }

    @Override
    protected void verifyModel(DataCenter model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
    }
}
