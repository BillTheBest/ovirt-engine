package org.ovirt.engine.core.bll;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainDynamicDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, Backend.class, VmHandler.class, Config.class, VmTemplateHandler.class })
public class AddVmCommandTest {

    private final int REQUIRED_DISK_SIZE_GB = 10;
    private final int AVAILABLE_SPACE_GB = 11;
    private final int USED_SPACE_GB = 4;
    private final Guid STORAGE_POOL_ID = Guid.NewGuid();

    @Mock
    DbFacade db;

    @Mock
    VmDAO vmDao;

    @Mock
    StorageDomainDAO sdDAO;

    @Mock
    VmTemplateDAO vmTemplateDAO;

    @Mock
    DiskImageDAO diskImageDAO;

    @Mock
    StorageDomainDynamicDAO storageDomainDynamicDAO;

    @Mock
    BackendInternal backend;

    @Mock
    VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    VdsGroupDAO vdsGroupDAO;

    public AddVmCommandTest() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(Backend.class);
        mockStatic(VmHandler.class);
        mockStatic(Config.class);
        mockStatic(VmTemplateHandler.class);
    }

    @Before
    public void testSetup() {
        mockBackend();
        mockDbFacade();
    }

    @Test
    public void create10GBVmWith11GbAvailableAndA5GbBuffer() throws Exception {
        setupAllMocks();
        VM vm = createVm(REQUIRED_DISK_SIZE_GB);
        AddVmCommand<VmManagementParametersBase> cmd = createCommand(vm);
        mockStorageDomainDAOGet(AVAILABLE_SPACE_GB);
        mockUninterestingMethods(cmd);
        assertFalse("If the disk is too big, canDoAction should fail", cmd.canDoAction());
        assertTrue("canDoAction failed for the wrong reason",
                cmd.getReturnValue()
                        .getCanDoActionMessages()
                        .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void selectStorageDomainNoneUnderSpaceThreshold() {
        final int domainSpaceGB = 5;
        final VmTemplate template = setupSelectStorageDomainTests(domainSpaceGB, 10, 0);
        assertTrue("acceptable storage domains were found",
                Guid.Empty.equals(AddVmCommand.SelectStorageDomain(template)));
    }

    @Test
    public void selectStorageDomainNoneUnderPctThreshold() {
        final int sizeRequired = 0;
        final int pctRequired = 95;
        final int totalDiskGB = 50;
        final int domainSpaceGB = totalDiskGB - USED_SPACE_GB; // results in 92% free space
        final VmTemplate template = setupSelectStorageDomainTests(domainSpaceGB, sizeRequired, pctRequired);
        assertTrue("acceptable storage domains were found",
                Guid.Empty.equals(AddVmCommand.SelectStorageDomain(template)));
    }

    @Test
    public void selectStorageDomain() {
        final VmTemplate template = setupSelectStorageDomainTests(AVAILABLE_SPACE_GB, 0, 0);
        assertFalse("no acceptable storage domains were found",
                Guid.Empty.equals(AddVmCommand.SelectStorageDomain(template)));
    }

    @Test
    public void canAddVm() {
        ArrayList<String> reasons = new ArrayList<String>();
        final int domainSizeGB = 20;
        final int sizeRequired = 5;
        final int pctRequired = 10;
        AddVmCommand<VmManagementParametersBase> cmd = setupCanAddVmTests(domainSizeGB, sizeRequired, pctRequired);
        assertTrue("vm could not be added", cmd.CanAddVm(new Object(), reasons));
    }

    @Test
    public void canAddVmFailSpaceThreshold() {
        ArrayList<String> reasons = new ArrayList<String>();
        final int sizeRequired = 10;
        final int pctRequired = 0;
        final int domainSizeGB = 4;
        AddVmCommand<VmManagementParametersBase> cmd = setupCanAddVmTests(domainSizeGB, sizeRequired, pctRequired);
        assertFalse("vm could not be added", cmd.CanAddVm(new Object(), reasons));
        assertTrue("canDoAction failed for the wrong reason",
                reasons.contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void canAddVmFailPctThreshold() {
        ArrayList<String> reasons = new ArrayList<String>();
        final int sizeRequired = 0;
        final int pctRequired = 95;
        final int domainSizeGB = 10;
        AddVmCommand<VmManagementParametersBase> cmd = setupCanAddVmTests(domainSizeGB, sizeRequired, pctRequired);
        assertFalse("vm could not be added", cmd.CanAddVm(new Object(), reasons));
        assertTrue("canDoAction failed for the wrong reason",
                reasons.contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    private VmTemplate setupSelectStorageDomainTests(final int domainSpaceGB,
            final int sizeRequired,
            final int pctRequired) {
        mockDiskImageDAOGetSnapshotById();
        mockStorageDomainDAOGetForStoragePool(domainSpaceGB);
        mockGetImageDomainsListVdsCommand();
        mockConfig();
        mockConfigSizeRequirements(sizeRequired, pctRequired);
        VmTemplate template = new VmTemplate();
        template.setstorage_pool_id(Guid.NewGuid());
        DiskImageTemplate image = new DiskImageTemplate();
        template.addDiskImageTemplate(image);
        return template;
    }

    private AddVmCommand<VmManagementParametersBase> setupCanAddVmTests(final int domainSizeGB,
            final int sizeRequired,
            final int pctRequired) {
        mockVmTemplateDAOReturnVmTemplate();
        mockDiskImageDAOGetSnapshotById();
        mockStorageDomainDAOGetForStoragePool(domainSizeGB);
        mockStorageDomainDAOGet(domainSizeGB);
        mockStorageDomainDynamicDAOGet(domainSizeGB, USED_SPACE_GB);
        mockGetImageDomainsListVdsCommand();
        mockConfig();
        mockConfigSizeRequirements(sizeRequired, pctRequired);
        VM vm = createVm(8);
        AddVmCommand<VmManagementParametersBase> cmd = createCommand(vm);
        doReturn(Guid.NewGuid()).when(cmd).getStoragePoolId();
        doReturn(true).when(cmd).CanAddVm(Matchers.<Object> anyObject(), Matchers.<ArrayList> any(ArrayList.class),
                anyInt(), anyString(), Matchers.<Guid> any(Guid.class), anyInt());
        doReturn(STORAGE_POOL_ID).when(cmd).getStoragePoolId();
        return cmd;
    }

    private void setupAllMocks() {
        mockVmDAOGetById();
        mockStorageDomainDAOGetForStoragePool();
        mockVmTemplateDAOReturnVmTemplate();
        mockDiskImageDAOGetSnapshotById();
        mockStorageDomainDynamicDAOGet();
        mockVdsGroupDAOGet();
        mockGetImageDomainsListVdsCommand();
        mockVmHandler();
        mockConfig();
        mockConfigSizeDefaults();
    }

    private void mockBackend() {
        when(Backend.getInstance()).thenReturn(backend);
        VdcQueryReturnValue returnValue = new VdcQueryReturnValue();
        returnValue.setReturnValue(Boolean.FALSE);
        when(backend.runInternalQuery(Matchers.<VdcQueryType> any(VdcQueryType.class),
                Matchers.any(VdcQueryParametersBase.class))).thenReturn(returnValue);
        when(backend.getResourceManager()).thenReturn(vdsBrokerFrontend);
    }

    private void mockDbFacade() {
        when(db.getVmDAO()).thenReturn(vmDao);
        when(db.getStorageDomainDAO()).thenReturn(sdDAO);
        when(db.getVmTemplateDAO()).thenReturn(vmTemplateDAO);
        when(db.getDiskImageDAO()).thenReturn(diskImageDAO);
        when(db.getStorageDomainDynamicDAO()).thenReturn(storageDomainDynamicDAO);
        when(db.getVdsGroupDAO()).thenReturn(vdsGroupDAO);
        when(DbFacade.getInstance()).thenReturn(db);
    }

    private void mockVmDAOGetById() {
        when(vmDao.getById(any(Guid.class))).thenReturn(null);
    }

    private void mockStorageDomainDAOGetForStoragePool(int domainSpaceGB) {
        when(sdDAO.getForStoragePool(Matchers.<Guid> any(Guid.class), Matchers.<NGuid> any(NGuid.class))).thenReturn(createStorageDomain(domainSpaceGB));
    }

    private void mockStorageDomainDAOGet(int domainSpaceGB) {
        when(sdDAO.get(any(Guid.class))).thenReturn(createStorageDomain(domainSpaceGB));
    }

    private void mockStorageDomainDAOGetForStoragePool() {
        mockStorageDomainDAOGetForStoragePool(AVAILABLE_SPACE_GB);
    }

    private void mockVmTemplateDAOReturnVmTemplate() {
        when(vmTemplateDAO.get(Matchers.<Guid> any(Guid.class))).thenReturn(createVmTemplate());
    }

    private VmTemplate createVmTemplate() {
        VmTemplate template = new VmTemplate();
        template.setstorage_pool_id(STORAGE_POOL_ID);
        template.addDiskImageTemplate(createDiskImageTemplate());
        Map<String, DiskImage> diskImageMap = new HashMap<String, DiskImage>(1);
        diskImageMap.put("disk1", createDiskImage());
        template.setDiskImageMap(diskImageMap);
        return template;
    }

    private DiskImageTemplate createDiskImageTemplate() {
        DiskImageTemplate i = new DiskImageTemplate();
        i.setSizeInGigabyte(USED_SPACE_GB + AVAILABLE_SPACE_GB);
        i.setId(Guid.NewGuid());
        return i;
    }

    private void mockDiskImageDAOGetSnapshotById() {
        when(diskImageDAO.getSnapshotById(Matchers.<Guid> any(Guid.class))).thenReturn(createDiskImage());
    }

    private DiskImage createDiskImage() {
        DiskImage img = new DiskImage();
        img.setSizeInGigabytes(REQUIRED_DISK_SIZE_GB);
        img.setActualSize(REQUIRED_DISK_SIZE_GB);
        img.setimage_group_id(Guid.NewGuid());
        return img;
    }

    private void mockStorageDomainDynamicDAOGet(int freeSpace, int usedSpace) {
        when(storageDomainDynamicDAO.get(Matchers.<Guid> any(Guid.class))).thenReturn(createStorageDomainDynamic(freeSpace,
                usedSpace));
    }

    private void mockStorageDomainDynamicDAOGet() {
        mockStorageDomainDynamicDAOGet(AVAILABLE_SPACE_GB, USED_SPACE_GB);
    }

    public void mockVdsGroupDAOGet() {
        when(vdsGroupDAO.get(Matchers.<Guid> any(Guid.class))).thenReturn(new VDSGroup());
    }

    private storage_domain_dynamic createStorageDomainDynamic(final int freeSpace, final int usedSpace) {
        return new storage_domain_dynamic(freeSpace, Guid.NewGuid(), usedSpace);
    }

    private void mockGetImageDomainsListVdsCommand() {
        ArrayList<Guid> guids = new ArrayList<Guid>(1);
        guids.add(Guid.NewGuid());
        VDSReturnValue returnValue = new VDSReturnValue();
        returnValue.setReturnValue(guids);
        when(vdsBrokerFrontend.RunVdsCommand(eq(VDSCommandType.GetImageDomainsList),
                Matchers.<VDSParametersBase> any(VDSParametersBase.class))).thenReturn(returnValue);
    }

    private storage_domains createStorageDomain(int availableSpace) {
        storage_domains sd = new storage_domains();
        sd.setstorage_domain_type(StorageDomainType.Master);
        sd.setstatus(StorageDomainStatus.Active);
        sd.setavailable_disk_size(availableSpace);
        sd.setused_disk_size(USED_SPACE_GB);
        return sd;
    }

    private void mockVmHandler() {
        when(
                VmHandler.VerifyAddVm(
                        Matchers.<ArrayList> any(ArrayList.class),
                        anyInt(),
                        Matchers.<Object> anyObject(),
                        Matchers.<Guid> any(Guid.class),
                        Matchers.<Guid> any(Guid.class),
                        anyBoolean(),
                        anyBoolean(),
                        anyInt()
                        )).thenReturn(Boolean.TRUE);

    }

    private void mockConfig() {
        when(Config.<Object> GetValue(ConfigValues.PredefinedVMProperties, "3.0")).thenReturn("");
        when(Config.<Object> GetValue(ConfigValues.UserDefinedVMProperties, "3.0")).thenReturn("");
    }

    private void mockConfigSizeRequirements(int requiredSpaceBufferInGB, int requiredSpacePercent) {
        when(Config.<Object> GetValue(ConfigValues.FreeSpaceCriticalLowInGB)).thenReturn(requiredSpaceBufferInGB);
        when(Config.<Object> GetValue(ConfigValues.FreeSpaceLow)).thenReturn(requiredSpacePercent);
    }

    private void mockConfigSizeDefaults() {
        int requiredSpaceBufferInGB = 5;
        int requiredSpacePercent = 0;
        mockConfigSizeRequirements(requiredSpaceBufferInGB, requiredSpacePercent);
    }

    private VM createVm(int diskSize) {
        VM vm = new VM();
        vm.setDiskSize(diskSize);
        VmDynamic dynamic = new VmDynamic();
        VmStatic stat = new VmStatic();
        stat.setvmt_guid(Guid.NewGuid());
        stat.setvm_name("testVm");
        stat.setpriority(1);
        vm.setStaticData(stat);
        vm.setDynamicData(dynamic);
        return vm;
    }

    private AddVmCommand<VmManagementParametersBase> createCommand(VM vm) {
        VmManagementParametersBase param = new VmManagementParametersBase(vm);
        AddVmCommand<VmManagementParametersBase> concrete = new AddVmCommand<VmManagementParametersBase>(param);
        return spy(concrete);
    }

    private void mockUninterestingMethods(AddVmCommand<VmManagementParametersBase> spy) {
        doReturn(true).when(spy).isVmNameValidLength(Matchers.<VM> any(VM.class));
        doReturn(STORAGE_POOL_ID).when(spy).getStoragePoolId();
        doReturn(createVmTemplate()).when(spy).getVmTemplate();
        doReturn(true).when(spy).areParametersLegal(Matchers.<ArrayList> any(ArrayList.class));
        doReturn(Collections.<VmNetworkInterface> emptyList()).when(spy).getVmInterfaces();
        doReturn(Collections.<DiskImageBase> emptyList()).when(spy).getVmDisks();
        spy.setVmTemplateId(Guid.NewGuid());
    }
}
