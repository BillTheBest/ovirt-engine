package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmsComparer;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVdsVMsClearedVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class ClearNonResponsiveVdsVmsCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected ClearNonResponsiveVdsVmsCommand(Guid commandId) {
        super(commandId);
    }

    public ClearNonResponsiveVdsVmsCommand(T parameters) {
        super(parameters);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_CLEAR_UNKNOWN_VMS : AuditLogType.USER_FAILED_CLEAR_UNKNOWN_VMS;
    }

    @Override
    protected void executeCommand() {
        List<VM> vms = DbFacade.getInstance().getVmDAO().getAllRunningForVds(getVdsId());
        Collections.sort(vms, Collections.reverseOrder(new VmsComparer()));
        java.util.ArrayList<VdcActionParametersBase> runVmParamsList =
                new java.util.ArrayList<VdcActionParametersBase>();
        for (VM vm : vms) {
            if (vm.getauto_startup()) {
                runVmParamsList.add(new RunVmParams(vm.getvm_guid()));
            }
            VDSReturnValue returnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SetVmStatus,
                            new SetVmStatusVDSCommandParameters(vm.getvm_guid(), VMStatus.Down));
            // Write that this VM was shut down by host rebbot or manual fence
            if (returnValue != null && returnValue.getSucceeded()) {
                LogSettingVmToDown(getVds().getvds_id(), vm.getvm_guid());
            }
            VmPoolHandler.ProcessVmPoolOnStopVm(vm.getvm_guid());
        }

        Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.UpdateVdsVMsCleared,
                        new UpdateVdsVMsClearedVDSCommandParameters(getVdsId()));
        if (runVmParamsList.size() > 0) {
            Backend.getInstance().runInternalMultipleActions(VdcActionType.RunVm, runVmParamsList);
        }
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        if (getVds() == null) {
            addCanDoActionMessage(VdcBllMessages.VDS_INVALID_SERVER_ID);
            returnValue = false;

        } else if (hasVMs() && getVds().getstatus() != VDSStatus.NonResponsive) {
            addCanDoActionMessage(VdcBllMessages.VDS_CANNOT_CLEAR_VMS_WRONG_STATUS);
            returnValue = false;
        }
        return returnValue;
    }

    private boolean hasVMs() {
        List<VM> vms = DbFacade.getInstance().getVmDAO().getAllRunningForVds(getVdsId());
        return (vms.size() > 0);
    }
}
