package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainTemplatesResourceTest.setUpStoragePool;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainVmsResourceTest.setUpStorageDomain;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendVmsResourceTest.verifyModelSpecific;

public class BackendStorageDomainVmResourceTest
    extends AbstractBackendSubResourceTest<VM,
                                           org.ovirt.engine.core.common.businessentities.VM,
                                           BackendStorageDomainVmResource> {

    private static final Guid VM_ID = GUIDS[1];
    private static final Guid DATA_CENTER_ID = GUIDS[0];
    private static final Guid STORAGE_DOMAIN_ID = GUIDS[GUIDS.length-1];

    private static final String URL_BASE = "storagedomains/" + STORAGE_DOMAIN_ID + "/vms/" + VM_ID;

    public BackendStorageDomainVmResourceTest() {
        super(new BackendStorageDomainVmResource(new BackendStorageDomainVmsResource(STORAGE_DOMAIN_ID),
                                                 VM_ID.toString()));
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    protected void setUriInfo(UriInfo uriInfo) {
        super.setUriInfo(uriInfo);
        resource.getParent().setUriInfo(uriInfo);
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendStorageDomainVmResource(null, "foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetExportNotFound() throws Exception {
        setUpGetStorageDomainExpectations(StorageDomainType.ImportExport);
        setUpGetEntityExpectations(StorageDomainType.ImportExport, STORAGE_DOMAIN_ID, true);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetExport() throws Exception {
        testGet(StorageDomainType.ImportExport);
    }

    protected void testGet(StorageDomainType domainType) throws Exception {
        setUpGetStorageDomainExpectations(domainType);
        setUpGetEntityExpectations(domainType, STORAGE_DOMAIN_ID);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();

        verifyModel(resource.get(), 1);
    }

    @Test
    public void testImportNotFound() throws Exception {
        Action action = new Action();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(GUIDS[2].toString());
        action.setCluster(new Cluster());
        action.getCluster().setId(GUIDS[1].toString());
        setUpGetEntityExpectations(StorageDomainType.ImportExport, STORAGE_DOMAIN_ID, true);
        control.replay();
        try {
            resource.doImport(action);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testImport() throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        Cluster cluster = new Cluster();
        cluster.setId(GUIDS[1].toString());
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        doTestImport(storageDomain, cluster);
    }

    @Test
    public void testImportWithStorageDomainName() throws Exception {
        setUpGetEntityExpectations("Storage: name=" + NAMES[2],
                                   SearchType.StorageDomain,
                                   getStorageDomain(2));

        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setName(NAMES[2]);
        Cluster cluster = new Cluster();
        cluster.setId(GUIDS[1].toString());
        doTestImport(storageDomain, cluster);
    }

    @Test
    public void testImportWithClusterName() throws Exception {
        setUpGetEntityExpectations("Cluster: name=" + NAMES[1],
                                   SearchType.Cluster,
                                   getCluster(1));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        Cluster cluster = new Cluster();
        cluster.setName(NAMES[1]);
        setUpGetDataCenterByStorageDomainExpectations(STORAGE_DOMAIN_ID);
        doTestImport(storageDomain, cluster);
    }

    private void setUpGetDataCenterByStorageDomainExpectations(Guid id) {
        setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolsByStorageDomainId,
                StorageDomainQueryParametersBase.class,
                new String[] { "StorageDomainId" },
                new Object[] { id },
                setUpStoragePool());
    }

    public void doTestImport(StorageDomain storageDomain, Cluster cluster) throws Exception {
        setUpGetEntityExpectations(1, StorageDomainType.ImportExport, GUIDS[2]);
        setUriInfo(setUpActionExpectations(VdcActionType.ImportVm,
                                           ImportVmParameters.class,
                                           new String[] { "ContainerId", "StorageDomainId", "SourceDomainId", "DestDomainId", "StoragePoolId", "VdsGroupId" },
                                           new Object[] { VM_ID, GUIDS[2], STORAGE_DOMAIN_ID, GUIDS[2], DATA_CENTER_ID, GUIDS[1] }));

        Action action = new Action();
        action.setStorageDomain(storageDomain);
        action.setCluster(cluster);
        verifyActionResponse(resource.doImport(action));
    }

    @Test
    public void testImportAsyncPending() throws Exception {
        doTestImportAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testImportAsyncInProgress() throws Exception {
        doTestImportAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testImportAsyncFinished() throws Exception {
        doTestImportAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    private void doTestImportAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus actionStatus) throws Exception {
        setUpGetEntityExpectations(1, StorageDomainType.ImportExport, GUIDS[2]);

        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3]);

        setUriInfo(setUpActionExpectations(
                VdcActionType.ImportVm,
                ImportVmParameters.class,
                new String[] { "ContainerId", "StorageDomainId", "SourceDomainId", "DestDomainId", "StoragePoolId", "VdsGroupId" },
                new Object[] { VM_ID, GUIDS[2], STORAGE_DOMAIN_ID, GUIDS[2], DATA_CENTER_ID, GUIDS[1] },
                asList(GUIDS[1]),
                asList(new AsyncTaskStatus(asyncStatus))));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[2].toString());
        Cluster cluster = new Cluster();
        cluster.setId(GUIDS[1].toString());

        Action action = new Action();
        action.setStorageDomain(storageDomain);
        action.setCluster(cluster);

        Response response = resource.doImport(action);
        verifyActionResponse(response, URL_BASE, true, null, null);
        action = (Action)response.getEntity();
        assertTrue(action.isSetStatus());
        assertEquals(actionStatus.value(), action.getStatus().getState());
    }

    @Test
    public void testIncompleteImport() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        try {
            control.replay();
            resource.doImport(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Action", "doImport", "cluster.id|name", "storageDomain.id|name");
        }
    }

    protected void setUpGetStorageDomainExpectations(StorageDomainType domainType) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainById,
                                     StorageDomainQueryParametersBase.class,
                                     new String[] { "StorageDomainId" },
                                     new Object[] { STORAGE_DOMAIN_ID },
                                     setUpStorageDomain(domainType));
    }

    protected void setUpGetEntityExpectations(int times, StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(domainType, getStoragePoolsByStorageDomainId);
        }
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId) throws Exception {
        setUpGetEntityExpectations(domainType, getStoragePoolsByStorageDomainId, false);
    }

    protected void setUpGetEntityExpectations(StorageDomainType domainType, Guid getStoragePoolsByStorageDomainId, boolean notFound) throws Exception {
        switch (domainType) {
        case Data:
            setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                         GetVmByVmIdParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { VM_ID },
                                         notFound ? null : getEntity(1));
            break;
        case ImportExport:
            setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolsByStorageDomainId,
                                         StorageDomainQueryParametersBase.class,
                                         new String[] { "StorageDomainId" },
                                         new Object[] { getStoragePoolsByStorageDomainId },
                                         setUpStoragePool());
            setUpEntityQueryExpectations(VdcQueryType.GetVmsFromExportDomain,
                                         GetAllFromExportDomainQueryParamenters.class,
                                         new String[] { "StoragePoolId", "StorageDomainId" },
                                         new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID },
                                         setUpVms(notFound));
            break;
        default:
            break;
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
        String uri = URL_BASE + "/action";
        return setUpActionExpectations(task, clz, names, values, true, true, null, asyncTasks, asyncStatuses, uri, true);
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, URL_BASE, false);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.VM getEntity(int index) {
        return setUpEntityExpectations(control.createMock(org.ovirt.engine.core.common.businessentities.VM.class),
                                       control.createMock(VmStatistics.class),
                                       index);
    }

    protected List<org.ovirt.engine.core.common.businessentities.VM> setUpVms(boolean notFound) {
        List<org.ovirt.engine.core.common.businessentities.VM> ret =
            new ArrayList<org.ovirt.engine.core.common.businessentities.VM>();
        if (notFound) {
            return ret;
        }
        for (int i = 0; i < NAMES.length; i++) {
            ret.add(getEntity(i));
        }
        return ret;
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

    protected VDSGroup getCluster(int idx) {
        VDSGroup cluster = new VDSGroup();
        cluster.setID(GUIDS[idx]);
        return cluster;
    }
}
