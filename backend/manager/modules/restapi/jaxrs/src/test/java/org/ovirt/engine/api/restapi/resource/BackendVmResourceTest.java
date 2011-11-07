package org.ovirt.engine.api.restapi.resource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Floppies;
import org.ovirt.engine.api.model.Floppy;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Ticket;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.HibernateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVdsByNameParameters;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.setUpStatisticalEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.verifyModelSpecific;

public class BackendVmResourceTest
        extends AbstractBackendSubResourceTest<VM, org.ovirt.engine.core.common.businessentities.VM, BackendVmResource> {

    private static final String ISO_ID = "foo.iso";
    private static final String FLOPPY_ID = "bar.vfd";

    public BackendVmResourceTest() {
        super(new BackendVmResource(GUIDS[0].toString(), new BackendVmsResource()));
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendVmResource("foo", new BackendVmsResource());
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
        setUpGetEntityExpectations(1);
        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testGetIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            setUriInfo(setUpBasicUriExpectations());
            setUpGetEntityExpectations(1);
            control.replay();

            VM vm = resource.get();
            assertTrue(vm.isSetStatistics());
            verifyModel(vm, 0);
        } finally {
            accepts.clear();
        }
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
        setUpGetEntityExpectations(2);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                                           VmManagementParametersBase.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateMovingCluster() throws Exception {
        setUpGetEntityExpectations(3);

        setUriInfo(setUpActionExpectations(VdcActionType.ChangeVMCluster,
                                           ChangeVMClusterParameters.class,
                                           new String[] {"ClusterId", "VmId"},
                                           new Object[] {GUIDS[1], GUIDS[0]},
                                           true,
                                           true,
                                           false));

        setUpActionExpectations(VdcActionType.UpdateVm,
                                VmManagementParametersBase.class,
                                new String[] {},
                                new Object[] {},
                                true,
                                true);

        VM model = getModel(0);
        model.setId(GUIDS[0].toString());
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[1].toString());
        verifyModelOnNewCluster(resource.update(model), 0);
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

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVm,
                                           VmManagementParametersBase.class,
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
        setUpGetEntityExpectations(1);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();

        VM model = getModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    @Test
    public void testStart() throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.start(new Action()));
    }

    @Test
    public void testStartWithPauseAndStateless() throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId", "RunAndPause", "RunAsStateless" },
                                           new Object[] { GUIDS[0], true, Boolean.TRUE }));

        Action action = new Action();
        action.setPause(true);
        action.setVm(new VM());
        action.getVm().setStateless(true);

        verifyActionResponse(resource.start(action));
    }

    @Test
    public void testStartWithVnc() throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId", "UseVnc" },
                                           new Object[] { GUIDS[0], Boolean.TRUE }));

        Action action = new Action();
        action.setVm(new VM());
        action.getVm().setDisplay(new Display());
        action.getVm().getDisplay().setType(DisplayType.VNC.value());

        verifyActionResponse(resource.start(action));
    }

    @Test
    public void testStartWithBootDev() throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId", "BootSequence" },
                                           new Object[] { GUIDS[0], BootSequence.N }));

        Action action = new Action();
        action.setVm(new VM());
        action.getVm().setOs(new OperatingSystem());
        action.getVm().getOs().getBoot().add(new Boot());
        action.getVm().getOs().getBoot().get(0).setDev(BootDevice.NETWORK.value());

        verifyActionResponse(resource.start(action));
    }

    @Test
    public void testStartWithCdRomAndFloppy() throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId", "DiskPath", "FloppyPath" },
                                           new Object[] { GUIDS[0], ISO_ID, FLOPPY_ID }));

        Action action = new Action();
        action.setVm(new VM());
        action.getVm().setCdroms(new CdRoms());
        action.getVm().getCdroms().getCdRoms().add(new CdRom());
        action.getVm().getCdroms().getCdRoms().get(0).setFile(new File());
        action.getVm().getCdroms().getCdRoms().get(0).getFile().setId(ISO_ID);
        action.getVm().setFloppies(new Floppies());
        action.getVm().getFloppies().getFloppies().add(new Floppy());
        action.getVm().getFloppies().getFloppies().get(0).setFile(new File());
        action.getVm().getFloppies().getFloppies().get(0).getFile().setId(FLOPPY_ID);

        verifyActionResponse(resource.start(action));
    }

    @Test
    public void testStartWithHostId() throws Exception {
        Host host = new Host();
        host.setId(GUIDS[1].toString());

        testStartWithHost(host, GUIDS[1]);
    }

    @Test
    public void testStartWithHostName() throws Exception {
        setUpGetHostIdExpectations(1);

        Host host = new Host();
        host.setName(NAMES[1]);

        testStartWithHost(host, GUIDS[1]);
    }

    protected void testStartWithHost(Host host, Guid hostId) throws Exception {
        setUpWindowsGetEntityExpectations(1, false);
        setUriInfo(setUpActionExpectations(VdcActionType.RunVmOnce,
                                           RunVmOnceParams.class,
                                           new String[] { "VmId", "DestinationVdsId" },
                                           new Object[] { GUIDS[0], hostId }));

        Action action = new Action();
        action.setVm(new VM());
        VmPlacementPolicy placementPolicy = new VmPlacementPolicy();
        placementPolicy.setHost(host);
        action.getVm().setPlacementPolicy(placementPolicy);

        verifyActionResponse(resource.start(action));
    }

    @Test
    public void testSuspend() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.HibernateVm,
                                           HibernateVmParameters.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.suspend(new Action()));
    }

    @Test
    public void testSuspendAsyncPending() throws Exception {
        doTestSuspendAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testSuspendAsyncInProgress() throws Exception {
        doTestSuspendAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testSuspendAsyncFinished() throws Exception {
        doTestSuspendAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestSuspendAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.HibernateVm,
                                                HibernateVmParameters.class,
                                                new String[] { "VmId" },
                                                new Object[] { GUIDS[0] },
                                                asList(GUIDS[1]),
                                                asList(new AsyncTaskStatus(asyncStatus))));

        Response response = resource.suspend(new Action());
        verifyActionResponse(response, "vms/" + GUIDS[0], true, null, null);
        Action action = (Action)response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(actionStatus.value(), action.getStatus().getState());

    }

    @Test
    public void testShutdown() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ShutdownVm,
                                           ShutdownVmParameters.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.shutdown(new Action()));
    }

    @Test
    public void testStop() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.StopVm,
                                           StopVmParameters.class,
                                           new String[] { "VmId", "StopVmType" },
                                           new Object[] { GUIDS[0], StopVmTypeEnum.NORMAL }));

        verifyActionResponse(resource.stop(new Action()));
    }

    @Test
    public void testDetach() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmFromPool,
                                           RemoveVmFromPoolParameters.class,
                                           new String[] { "VmId" },
                                           new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.detach(new Action()));
    }

    @Test
    public void testTicket() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.SetVmTicket,
                                           SetVmTicketParameters.class,
                                           new String[] { "VmId", "Ticket" },
                                           new Object[] { GUIDS[0], NAMES[1] }));

        Action action = new Action();
        action.setTicket(new Ticket());
        action.getTicket().setValue(NAMES[1]);
        verifyActionResponse(resource.ticket(action));
    }

    @Test
    public void testMigrateWithHostId() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.MigrateVmToServer,
                                           MigrateVmToServerParameters.class,
                                           new String[] { "VmId", "VdsId", "ForceMigrationForNonMigratableVM" },
                                           new Object[] { GUIDS[0], GUIDS[1], Boolean.FALSE }));

        Action action = new Action();
        action.setHost(new Host());
        action.getHost().setId(GUIDS[1].toString());
        verifyActionResponse(resource.migrate(action));
    }

    @Test
    public void testMigrateWithHostName() throws Exception {
        setUpGetHostIdExpectations(1);

        setUriInfo(setUpActionExpectations(VdcActionType.MigrateVmToServer,
                                           MigrateVmToServerParameters.class,
                                           new String[] { "VmId", "VdsId", "ForceMigrationForNonMigratableVM" },
                                           new Object[] { GUIDS[0], GUIDS[1], Boolean.FALSE }));

        Action action = new Action();
        action.setHost(new Host());
        action.getHost().setName(NAMES[1]);
        verifyActionResponse(resource.migrate(action));
    }

    @Test
    public void testMigrateNoHost() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.MigrateVm,
                MigrateVmParameters.class,
                new String[] { "VmId", "ForceMigrationForNonMigratableVM" },
                new Object[] { GUIDS[0], Boolean.FALSE }));

        verifyActionResponse(resource.migrate(new Action()));
    }

    @Test
    public void testExport() throws Exception {
        testExportWithStorageDomainId(false, false);
    }

    @Test
    public void testExportWithParams() throws Exception {
        testExportWithStorageDomainId(true, true);
    }

    @Test
    public void testExportWithStorageDomainName() throws Exception {
        setUpGetEntityExpectations("Storage: name=" + NAMES[2],
                                   SearchType.StorageDomain,
                                   getStorageDomain(2));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setName(NAMES[2]);

        doTestExport(storageDomain, false, false);
    }

    protected void testExportWithStorageDomainId(boolean exclusive, boolean discardSnapshots) throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        doTestExport(storageDomain, exclusive, discardSnapshots);
    }

    protected void doTestExport(StorageDomain storageDomain,
                                boolean exclusive,
                                boolean discardSnapshots) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ExportVm,
                                           MoveVmParameters.class,
                                           new String[] { "ContainerId", "StorageDomainId", "ForceOverride", "CopyCollapse" },
                                           new Object[] { GUIDS[0], GUIDS[2], exclusive, discardSnapshots }));

        Action action = new Action();
        action.setStorageDomain(storageDomain);
        if (exclusive) {
            action.setExclusive(exclusive);
        }
        if (discardSnapshots) {
            action.setDiscardSnapshots(discardSnapshots);
        }
        verifyActionResponse(resource.export(action));
    }

    @Test
    public void testIncompleteExport() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        try {
            control.replay();
            resource.export(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "export", "storageDomain.id|name");
        }
    }

    @Test
    public void testMoveWithStorageDomainId() throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        doTestMove(storageDomain);
    }

    @Test
    public void testMoveWithStorageDomainName() throws Exception {
        setUpGetEntityExpectations("Storage: name=" + NAMES[2],
                                   SearchType.StorageDomain,
                                   getStorageDomain(2));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setName(NAMES[2]);

        doTestMove(storageDomain);
    }

     @Test
    public void testIncompleteMove() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        try {
            control.replay();
            resource.move(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "move", "storageDomain.id|name");
        }
    }

    protected void doTestMove(StorageDomain storageDomain) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.MoveVm,
                       MoveVmParameters.class,
                       new String[] { "ContainerId", "StorageDomainId" },
                       new Object[] { GUIDS[0], GUIDS[2] }));

        Action action = new Action();
        action.setStorageDomain(storageDomain);
        verifyActionResponse(resource.move(action));
    }

    @Test
    public void testStatisticalQuery() throws Exception {
        org.ovirt.engine.core.common.businessentities.VM entity = setUpStatisticalExpectations();

        @SuppressWarnings("unchecked")
        BackendStatisticsResource<VM, org.ovirt.engine.core.common.businessentities.VM> statisticsResource =
            (BackendStatisticsResource<VM, org.ovirt.engine.core.common.businessentities.VM>)resource.getStatisticsResource();
        assertNotNull(statisticsResource);

        verifyQuery(statisticsResource.getQuery(), entity);
    }

    protected org.ovirt.engine.core.common.businessentities.VM setUpStatisticalExpectations() throws Exception {
        VmStatistics stats = control.createMock(VmStatistics.class);
        org.ovirt.engine.core.common.businessentities.VM entity =
            control.createMock(org.ovirt.engine.core.common.businessentities.VM.class);
        setUpStatisticalEntityExpectations(entity, stats);
        setUpGetEntityExpectations(1, false, entity);
        control.replay();
        return entity;
    }

    protected void verifyQuery(AbstractStatisticalQuery<VM, org.ovirt.engine.core.common.businessentities.VM> query,
                               org.ovirt.engine.core.common.businessentities.VM entity)
        throws Exception {
        assertEquals(VM.class, query.getParentType());
        assertSame(entity, query.resolve(GUIDS[0]));
        List<Statistic> statistics = query.getStatistics(entity);
        verifyStatistics(statistics,
                         new String[] {"memory.installed", "memory.used", "cpu.current.guest",
                                       "cpu.current.hypervisor", "cpu.current.total"},
                         new BigDecimal[] {asDec(10*Mb), asDec(2*Mb), asDec(30), asDec(40), asDec(70)});
        Statistic adopted = query.adopt(new Statistic());
        assertTrue(adopted.isSetVm());
        assertEquals(GUIDS[0].toString(), adopted.getVm().getId());
    }

    protected void setUpGetHostIdExpectations(int idx) throws Exception {
        VDS host = BackendHostsResourceTest.setUpEntityExpectations(control.createMock(VDS.class), idx);
        setUpGetEntityExpectations(VdcQueryType.GetVdsByName,
                                   GetVdsByNameParameters.class,
                                   new String[] { "Name" },
                                   new Object[] { NAMES[idx] },
                                   host);
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        setUpGetEntityExpectations(times, notFound, getEntity(0));
    }

    protected void setUpWindowsGetEntityExpectations(int times, boolean notFound) throws Exception {
        setUpGetEntityExpectations(times,
                                   notFound,
                                   new org.ovirt.engine.core.common.businessentities.VM(){{setvm_guid(GUIDS[0]);setvm_os(VmOsType.WindowsXP);}});
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound, org.ovirt.engine.core.common.businessentities.VM entity) throws Exception {

        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVmByVmId,
                                       GetVmByVmIdParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : entity);
        }
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
                                              Class<? extends VdcActionParametersBase> clz,
                                              String[] names,
                                              Object[] values,
                                              ArrayList<Guid> asyncTasks,
                                              ArrayList<AsyncTaskStatus> asyncStatuses) {
        String uri = "vms/" + GUIDS[0] + "/action";
        return setUpActionExpectations(task, clz, names, values, true, true, null, asyncTasks, asyncStatuses, uri, true);
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "vms/" + GUIDS[0], false);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.VM getEntity(int index) {
        return setUpEntityExpectations(
                control.createMock(org.ovirt.engine.core.common.businessentities.VM.class),
                control.createMock(VmStatistics.class),
                index);
    }

    protected void verifyModelOnNewCluster(VM model, int index) {
        assertNotNull(model.getCluster().getId());
        assertEquals(GUIDS[2].toString(), model.getCluster().getId());
        verifyModel(model, index);
    }

    protected void verifyModel(VM model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
    }

    protected storage_domains getStorageDomain(int idx) {
        storage_domains dom = new storage_domains();
        dom.setid(GUIDS[idx]);
        return dom;
    }
}
