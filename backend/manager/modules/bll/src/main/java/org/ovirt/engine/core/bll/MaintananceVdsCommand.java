package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.MaintananceVdsParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmsComparer;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;

public class MaintananceVdsCommand<T extends MaintananceVdsParameters> extends VdsCommand<T> {
    private final boolean _isInternal;
    private List<VM> vms;

    public MaintananceVdsCommand(T parameters) {
        super(parameters);
        _isInternal = parameters.getIsInternal();
    }

    @Override
    protected void executeCommand() {
        if (getVds().getstatus() == VDSStatus.Maintenance) {
            // nothing to do
            setSucceeded(true);
        } else {
            setSucceeded(MigrateAllVms());

            /**
             * if non responsive move directly to maintenance
             */
            if (getVds().getstatus() == VDSStatus.NonResponsive
                    || getVds().getstatus() == VDSStatus.Problematic
                    || getVds().getstatus() == VDSStatus.Down) {
                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.SetVdsStatus,
                                new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Maintenance));
            }
        }
    }

    protected void orderListOfRunningVmsOnVds(Guid vdsId) {
        vms = DbFacade.getInstance().getVmDAO().getAllRunningForVds(vdsId);
        Collections.sort(vms, Collections.reverseOrder(new VmsComparer()));
    }

    protected boolean MigrateAllVms() {
        return MigrateAllVms(false);
    }

    protected boolean MigrateAllVms(boolean HAOnly) {
        orderListOfRunningVmsOnVds(getVdsId());

        boolean succeeded = true;

        for (VM vm : vms) {
            // if HAOnly is true check that vm is HA (auto_startup should be
            // true)
            if (vm.getstatus() != VMStatus.MigratingFrom && (!HAOnly || (HAOnly && vm.getauto_startup()))) {
                MigrateVmParameters tempVar = new MigrateVmParameters(false, vm.getvm_guid());
                tempVar.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
                VdcReturnValueBase result = Backend.getInstance().runInternalAction(VdcActionType.InternalMigrateVm,
                        tempVar);
                if (!result.getCanDoAction() || !(((Boolean) result.getActionReturnValue()).booleanValue())) {
                    succeeded = false;
                    AppendCustomValue("failedVms", vm.getvm_name(), ",");
                    log.errorFormat("ResourceManager::vdsMaintenance - Failed migrating desktop '{0}'", vm.getvm_name());
                }
            }
        }
        return succeeded;
    }

    @Override
    protected boolean canDoAction() {
        return CanMaintananceVds(getVdsId(), getReturnValue().getCanDoActionMessages());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (_isInternal) {
            if (getSucceeded()) {
                return AuditLogType.VDS_MAINTENANCE;
            } else {
                return AuditLogType.VDS_MAINTENANCE_FAILED;
            }
        } else {
            if (getSucceeded()) {
                return AuditLogType.USER_VDS_MAINTENANCE;
            } else {
                return AuditLogType.USER_VDS_MAINTENANCE_MIGRATION_FAILED;
            }
        }
    }

    public boolean CanMaintananceVds(Guid vdsId, java.util.ArrayList<String> reasons) {
        boolean returnValue = true;
        // VDS vds = ResourceManager.Instance.getVds(vdsId);
        VDS vds = DbFacade.getInstance().getVdsDAO().get(vdsId);
        // we can get here when vds status was set already to Maintenance
        if ((vds.getstatus() != VDSStatus.Maintenance) && (vds.getstatus() != VDSStatus.NonResponsive)
                && (vds.getstatus() != VDSStatus.Up) && (vds.getstatus() != VDSStatus.Error)
                && (vds.getstatus() != VDSStatus.PreparingForMaintenance) && (vds.getstatus() != VDSStatus.Down)) {
            returnValue = false;
            reasons.add(VdcBllMessages.VDS_CANNOT_MAINTENANCE_VDS_IS_NOT_OPERATIONAL.toString());
        }

        orderListOfRunningVmsOnVds(vdsId);

        for (VM vm : vms) {
            if (vm.getMigrationSupport() != MigrationSupport.MIGRATABLE) {
                reasons.add(VdcBllMessages.VDS_CANNOT_MAINTENANCE_IT_INCLUDES_NON_MIGRATABLE_VM.toString());
                return false;
            }
        }

        return returnValue;
    }

    public static void ProcessStorageOnVdsInactive(VDS vds) {

        // Clear the problematic timers since the VDS is in maintenance so it doesn't make sense to check it
        // anymore.
        IrsBrokerCommand.clearVdsFromCache(vds.getstorage_pool_id(), vds.getvds_id(), vds.getvds_name());

        if (!vds.getstorage_pool_id().equals(Guid.Empty)
                && StoragePoolStatus.Uninitialized != DbFacade.getInstance()
                        .getStoragePoolDAO()
                        .get(vds.getstorage_pool_id())
                        .getstatus()
                && Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.DisconnectStoragePool,
                                new DisconnectStoragePoolVDSCommandParameters(vds.getvds_id(),
                                        vds.getstorage_pool_id(), vds.getvds_spm_id())).getSucceeded()) {
            StoragePoolParametersBase tempVar = new StoragePoolParametersBase(vds.getstorage_pool_id());
            tempVar.setVdsId(vds.getvds_id());
            tempVar.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            Backend.getInstance().runInternalAction(VdcActionType.DisconnectHostFromStoragePoolServers, tempVar);
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(MaintananceVdsCommand.class);
}
