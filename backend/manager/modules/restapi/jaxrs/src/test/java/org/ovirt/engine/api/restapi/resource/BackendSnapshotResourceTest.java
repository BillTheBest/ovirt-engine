package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.easymock.classextension.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.BackendSnapshotsResourceTest.mangle;
import static org.ovirt.engine.api.restapi.resource.BackendSnapshotsResourceTest.verifyModelSpecific;
import static org.ovirt.engine.api.restapi.resource.BackendSnapshotsResourceTest.IMAGE_IDS;
import static org.ovirt.engine.api.restapi.resource.BackendSnapshotsResourceTest.SNAPSHOT_IDS;
import static org.ovirt.engine.api.restapi.resource.BackendSnapshotsResourceTest.VM_ID;

public class BackendSnapshotResourceTest
        extends AbstractBackendSubResourceTest<Snapshot, DiskImage, BackendSnapshotResource> {

    protected static final String BASE_HREF = "vms/" + VM_ID + "/snapshots/" + SNAPSHOT_IDS[0];

    public BackendSnapshotResourceTest() {
        super(new BackendSnapshotResource(SNAPSHOT_IDS[0].toString(), VM_ID, new BackendSnapshotsResource(VM_ID)));
    }

    protected void init() {
        super.init();
        initResource(resource.getCollection());
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendSnapshotResource("foo", null, null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(VdcQueryType.GetAllDisksByVmId,
                                   GetAllDisksByVmIdParameters.class,
                                   new String[] { "VmId" },
                                   new Object[] { VM_ID },
                                   new ArrayList<DiskImage>());
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
        setUpGetEntityExpectations();
        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testRestore() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.RestoreAllSnapshots,
                                           RestoreAllSnapshotsParameters.class,
                                           new String[] { "VmId", "DstSnapshotId" },
                                           new Object[] { VM_ID, SNAPSHOT_IDS[0] }));

        verifyActionResponse(resource.restore(new Action()), BASE_HREF, false);
    }

    @Test
    public void testRestoreAsyncPending() throws Exception {
        doTestRestoreAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testRestoreAsyncInProgress() throws Exception {
        doTestRestoreAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testRestoreAsyncFinished() throws Exception {
        doTestRestoreAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestRestoreAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.RestoreAllSnapshots,
                                           RestoreAllSnapshotsParameters.class,
                                           new String[] { "VmId", "DstSnapshotId" },
                                           new Object[] { VM_ID, SNAPSHOT_IDS[0] },
                                           asList(GUIDS[1]),
                                           asList(new AsyncTaskStatus(asyncStatus))));

        Response response = resource.restore(new Action());
        verifyActionResponse(response, BASE_HREF, true, null, null);
        Action action = (Action)response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(actionStatus.value(), action.getStatus().getState());

    }

    @Override
    protected DiskImage getEntity(int index) {
        DiskImage image = control.createMock(DiskImage.class);
        expect(image.getId()).andReturn(IMAGE_IDS[index]).anyTimes();
        ArrayList<DiskImage> snapshots = new ArrayList<DiskImage>();
        Guid parentId = Guid.Empty.getValue();
        for (int i = 0 ; i < NAMES.length ; i++) {
            DiskImage snapshot = control.createMock(DiskImage.class);
            Guid snapshotImageId = mangle(IMAGE_IDS[index], i);
            expect(snapshot.getId()).andReturn(snapshotImageId).anyTimes();
            expect(snapshot.getvm_snapshot_id()).andReturn(SNAPSHOT_IDS[i]).anyTimes();
            expect(snapshot.getParentId()).andReturn(parentId).anyTimes();
            expect(snapshot.getdescription()).andReturn(DESCRIPTIONS[i]).anyTimes();
            snapshots.add(snapshot);
            parentId = snapshotImageId;
        }
        expect(image.getSnapshots()).andReturn(snapshots).anyTimes();
        return image;
    }

    protected void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetAllDisksByVmId,
                                   GetAllDisksByVmIdParameters.class,
                                   new String[] { "VmId" },
                                   new Object[] { VM_ID },
                                   asList(getEntity(0)));
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values,
                                              ArrayList<Guid> asyncTasks,
                                              ArrayList<AsyncTaskStatus> asyncStatuses) {
        String uri = BASE_HREF + "/action";
        return setUpActionExpectations(task, clz, names, values, true, true, null, asyncTasks, asyncStatuses, uri, true);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    protected void verifyModel(Snapshot model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

}
