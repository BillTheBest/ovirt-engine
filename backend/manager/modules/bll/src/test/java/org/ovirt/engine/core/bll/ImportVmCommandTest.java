package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Config.class, ImportExportCommon.class })
public class ImportVmCommandTest {

    @Test
    public void insufficientDiskSpace() {
        final int lotsOfSpace = 1073741824;
        final int diskSpacePct = 0;
        final ImportVmCommand<ImportVmParameters> c = setupDiskSpaceTest(lotsOfSpace, diskSpacePct);
        assertFalse(c.canDoAction());
        assertTrue(c.getReturnValue()
                .getCanDoActionMessages()
                .contains(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString()));
    }

    @Test
    public void sufficientDiskSpace() {
        final int extraDiskSpaceRequired = 0;
        final int diskSpacePct = 0;
        final ImportVmCommand<ImportVmParameters> c = setupDiskSpaceTest(extraDiskSpaceRequired, diskSpacePct);
        assertTrue(c.canDoAction());
    }

    private ImportVmCommand<ImportVmParameters> setupDiskSpaceTest(final int diskSpaceRequired, final int diskSpacePct) {
        ConfigMocker cfgMocker = new ConfigMocker();
        cfgMocker.mockConfigLowDiskSpace(diskSpaceRequired);
        cfgMocker.mockConfigLowDiskPct(diskSpacePct);
        cfgMocker.mockLimitNumberOfNetworkInterfaces(Boolean.TRUE);
        mockImportExportCommonAlwaysTrue();
        return new TestHelperImportVmCommand(createParameters());
    }

    protected ImportVmParameters createParameters() {
        final VM v = createVM();
        v.setvm_name("testVm");
        final ImportVmParameters p =
                new ImportVmParameters(v, Guid.NewGuid(), Guid.NewGuid(), Guid.NewGuid(), Guid.NewGuid());
        return p;
    }

    protected VM createVM() {
        final VM v = new VM();
        v.setvm_guid(Guid.NewGuid());
        v.setDiskSize(2);
        return v;
    }

    protected static void mockImportExportCommonAlwaysTrue() {
        ImportExportCommonMocker mocker = new ImportExportCommonMocker();
        mocker.mockCheckStorageDomain(true);
        mocker.mockCheckStoragePool(true);
    }
}
