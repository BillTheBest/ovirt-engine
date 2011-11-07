package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.errors.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class SpmStopVDSCommand<P extends SpmStopVDSCommandParameters> extends VdsBrokerCommand<P> {
    public SpmStopVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        try {
            if (canVdsBeReached()) {
                boolean performSpmStop = true;
                try {
                    VDSReturnValue vdsReturnValue = ResourceManager
                            .getInstance()
                            .runVdsCommand(VDSCommandType.HSMGetAllTasksStatuses,
                                    new VdsIdVDSCommandParametersBase(getVds().getvds_id()));
                    performSpmStop =
                            vdsReturnValue.getReturnValue() != null ? ((java.util.HashMap<Guid, AsyncTaskStatus>) vdsReturnValue.getReturnValue()).isEmpty()
                                    : true;
                    getVDSReturnValue().setSucceeded(vdsReturnValue.getSucceeded());
                    getVDSReturnValue().setVdsError(vdsReturnValue.getVdsError());
                } catch (java.lang.Exception e2) {
                    log.infoFormat("SpmStopVDSCommand::Could not get tasks on vds {0} stopping SPM",
                            getVds().getvds_name());
                }
                if (performSpmStop) {
                    log.infoFormat("SpmStopVDSCommand::Stopping SPM on vds {0}, pool id {1}", getVds().getvds_name(),
                            getParameters().getStoragePoolId());
                    status = getBroker().spmStop(getParameters().getStoragePoolId().toString());
                    ProceedProxyReturnValue();
                } else if (getVDSReturnValue().getVdsError() == null) {
                    getVDSReturnValue().setSucceeded(false);
                    VDSError error = new VDSError();
                    error.setCode(VdcBllErrors.TaskInProgress);
                    getVDSReturnValue().setVdsError(error);
                } else if (getVDSReturnValue().getVdsError().getCode() == VdcBllErrors.VDS_NETWORK_ERROR) {
                    log.infoFormat(
                            "SpmStopVDSCommand::Could not get tasks on vds {0} - network exception, not stopping spm! pool id {1}",
                            getVds().getvds_name(),
                            getParameters().getStoragePoolId());
                }
            } else {
                log.infoFormat("SpmStopVDSCommand:: vds {0} is in {1} status - not performing spm stop, pool id {2}",
                        getVds().getvds_name(), getVds().getstatus(), getParameters().getStoragePoolId());
                getVDSReturnValue().setVdsError(new VDSError(VdcBllErrors.VDS_NETWORK_ERROR,
                        "Vds is in incorrect status"));
                getVDSReturnValue().setSucceeded(false);
            }
        } catch (RuntimeException exp) {
            log.warnFormat("could not stop spm of pool {0} on vds {1} - reason: {2}", getParameters()
                    .getStoragePoolId(), getParameters().getVdsId(), exp.toString());
            getVDSReturnValue().setExceptionObject(exp);
            getVDSReturnValue().setSucceeded(false);
        }
    }

    /**
     * Checks if the VDS is in a state where it can be reached or not, since if it can't be reached we don't want to
     * try to stop the SPM because the command won't work.
     * @return Can the VDS be reached or not?
     */
    private boolean canVdsBeReached() {
        VDSStatus vdsStatus = getVds().getstatus();
        if (vdsStatus == VDSStatus.Down || vdsStatus == VDSStatus.Reboot) {
            vdsStatus = getVds().getprevious_status();
        }
        return vdsStatus != VDSStatus.NonResponsive && getVds().getstatus() != VDSStatus.Problematic;
    }

    @Override
    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case StoragePoolUnknown:
        case SpmStatusError:
            // ignore this, the parser can handle the empty result.
            break;
        case TaskInProgress:
            getVDSReturnValue().setVdsError(new VDSError(returnStatus, getReturnStatus().mMessage));
            getVDSReturnValue().setSucceeded(false);
            break;
        default:
            super.ProceedProxyReturnValue();
            InitializeVdsError(returnStatus);
            break;
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(SpmStopVDSCommand.class);
}
