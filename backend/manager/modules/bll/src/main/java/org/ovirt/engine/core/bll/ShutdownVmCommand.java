package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class ShutdownVmCommand<T extends ShutdownVmParameters> extends StopVmCommandBase<T> {
    public ShutdownVmCommand(T shutdownVmParamsData) {
        super(shutdownVmParamsData);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSuspendedVm()) {
            return getSucceeded() ? AuditLogType.USER_STOP_SUSPENDED_VM : AuditLogType.USER_STOP_SUSPENDED_VM_FAILED;
        } else {
            return getSucceeded() ? AuditLogType.USER_INITIATED_SHUTDOWN_VM : AuditLogType.USER_FAILED_SHUTDOWN_VM;
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean ret = super.canDoAction();
        if (!ret) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__SHUTDOWN);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        }

        return ret;
    }

    @Override
    protected void Perform() {
        log.infoFormat("VmHandler.cs:ShutdownVmCommand:Perform: entered (Desktop {0}).", getVm().getvm_name());

        VmHandler.UpdateVmGuestAgentVersion(getVm());
        boolean CanShutDown = (getVm().getstatus() == VMStatus.Up)
                && ((getVm().getacpi_enable() == null ? false : getVm().getacpi_enable()) || getVm().getHasAgent());

        if (CanShutDown)
        // shutting down desktop and waiting for it in a separate thread to
        // become 'down':
        {
            log.infoFormat("VmHandler.cs:ShutdownVmCommand:Perform: sending shutdown command for Desktop {0}.", getVm()
                    .getvm_name());

            int secondsToWait = getParameters().getWaitBeforeShutdown() ? Config
                    .<Integer> GetValue(ConfigValues.VmGracefulShutdownTimeout) : 0;

            // sending a shutdown command to the VM:
            setActionReturnValue(Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.DestroyVm,
                            new DestroyVmVDSCommandParameters(getVdsId(), getVmId(), false, true, secondsToWait))
                    .getReturnValue());
        } else // cannot shutdown -> send a StopVm command instead ('destroy'):
        {
            // don't log -> log will appear for the
            // StopVmCommand we are about to run:
            setCommandShouldBeLogged(false);

            log.infoFormat(
                    "VmHandler.cs:ShutdownVmCommand:Perform: Cannot shutdown Desktop {0}, status is not up. Stopping instead.",
                    getVm().getvm_name());

            StopVmParameters stopVmParams = new StopVmParameters(getVmId(), StopVmTypeEnum.CANNOT_SHUTDOWN);
            // stopVmParams.ParametersCurrentUser = CurrentUser;
            stopVmParams.setSessionId(getParameters().getSessionId());
            Backend.getInstance().runInternalAction(VdcActionType.StopVm, stopVmParams);
        }

        setSucceeded(true);
    }

    private static LogCompat log = LogFactoryCompat.getLog(ShutdownVmCommand.class);
}
