package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class StopVmCommand<T extends StopVmParameters> extends StopVmCommandBase<T> {
    public StopVmCommand(T stopVmParams) {
        super(stopVmParams);
    }

    @Override
    protected void Perform() {
        Destroy();
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSuspendedVm()) {
            return getSucceeded() ? AuditLogType.USER_STOP_SUSPENDED_VM : AuditLogType.USER_STOP_SUSPENDED_VM_FAILED;
        } else {
            switch (getParameters().getStopVmType()) {
            case NORMAL:
                return getSucceeded() ? AuditLogType.USER_STOP_VM : AuditLogType.USER_FAILED_STOP_VM;

            case CANNOT_SHUTDOWN:
                return getSucceeded() ? AuditLogType.USER_STOPPED_VM_INSTEAD_OF_SHUTDOWN
                        : AuditLogType.USER_FAILED_STOPPING_VM_INSTEAD_OF_SHUTDOWN;

            default: // shouldn't get here:
                return AuditLogType.UNASSIGNED;
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean ret = super.canDoAction();
        if (!ret) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__STOP);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        }

        return ret;
    }
}
