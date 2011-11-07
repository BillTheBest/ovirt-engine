package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ThreadUtils;

public abstract class FenceVdsBaseCommand<T extends FenceVdsActionParameters> extends VdsCommand<T> {
    private final int SLEEP_BEFORE_FIRST_ATTEMPT=5000;
    private static LogCompat log = LogFactoryCompat.getLog(FenceVdsBaseCommand.class);
    protected FencingExecutor _executor;
    protected List<VM> mVmList = null;
    private boolean privateFencingSucceeded;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected FenceVdsBaseCommand(Guid commandId) {
        super(commandId);
    }

    public FenceVdsBaseCommand(T parameters) {
        super(parameters);
        mVmList = DbFacade.getInstance().getVmDAO().getAllRunningForVds(getVdsId());
    }

    /**
     * Gets the number of times to retry a get status PM operation after stop/start PM operation.
     *
     * @return
     */
    protected abstract int getRerties();

    /**
     * Gets the number of seconds to delay between each retry.
     *
     * @return
     */
    protected abstract int getDelayInSeconds();

    protected boolean getFencingSucceeded() {
        return privateFencingSucceeded;
    }

    protected void setFencingSucceeded(boolean value) {
        privateFencingSucceeded = value;
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = false;
        String event;
        String runningPmOp;
        // get the event to look for , if we requested to start Host then we should look when we stopped it and vice versa.
        if (getParameters().getAction() == FenceActionType.Start) {
            event = AuditLogType.USER_VDS_STOP.name();
            runningPmOp=FenceActionType.Stop.name();
        }
        else {
            event = AuditLogType.USER_VDS_START.name();
            runningPmOp=FenceActionType.Start.name();
        }
        if (getVds().getpm_enabled()
                && IsPowerManagementLegal(getVds().getStaticData(), getVdsGroup().getcompatibility_version().toString())) {
            // check if we are in the interval of X seconds from startup
            // if yes , system is still initializing , ignore fencing operations
            java.util.Date waitTo =
                    Backend.getInstance()
                            .getStartedAt()
                            .AddSeconds((Integer) Config.GetValue(ConfigValues.DisableFenceAtStartupInSec));
            java.util.Date now = new java.util.Date();
            if (waitTo.before(now) || waitTo.equals(now)) {
                // Check Quiet time between PM operations, this is done only if parent command is not <Restart>
                int secondsLeftToNextPmOp = ((getParameters().getParentCommand() == VdcActionType.RestartVds))
                        ?
                        0
                        :
                        DbFacade.getInstance().getAuditLogDAO().getTimeToWaitForNextPmOp(getVds().getvds_name(), event);
                if (secondsLeftToNextPmOp <= 0) {
                    // try to get vds status
                    _executor = createExecutorForProxyCheck();
                    if (_executor.FindVdsToFence()) {
                        if (!(retValue = _executor.checkProxyHostConnectionToHost())) {
                            addCanDoActionMessage(VdcBllMessages.VDS_FAILED_FENCE_VIA_PROXY_CONNECTION);
                        }
                    } else {
                        addCanDoActionMessage(VdcBllMessages.VDS_NO_VDS_PROXY_FOUND);
                    }
                } else {
                    addCanDoActionMessage(VdcBllMessages.VDS_FENCE_DISABLED_AT_QUIET_TIME);
                    addCanDoActionMessage(String.format("$operation %1$s", runningPmOp));
                    addCanDoActionMessage(String.format("$seconds %1$s", secondsLeftToNextPmOp));
                }
            } else {
                addCanDoActionMessage(VdcBllMessages.VDS_FENCE_DISABLED_AT_SYSTEM_STARTUP_INTERVAL);
            }
        }
        if (!retValue) {
            HandleError();
        }
        getReturnValue().setSucceeded(retValue);
        return retValue;
    }

    @Override
    protected void executeCommand() {
        VDSStatus lastStatus = getVds().getstatus();
        VDSReturnValue vdsReturnValue = null;
        try {
            // Set status immediately to prevent a race (BZ 636950/656224)
            setStatus();
            _executor = new FencingExecutor(getVds(), getParameters().getAction());
            if (_executor.FindVdsToFence()) {
                vdsReturnValue = _executor.Fence();
                setFencingSucceeded(vdsReturnValue.getSucceeded());
                if (getFencingSucceeded()) {
                    _executor = new FencingExecutor(getVds(), FenceActionType.Status);
                    if (waitForStatus(getVds().getvds_name(), getParameters().getAction())) {
                        handleSpecificCommandActions();
                    }
                    else {
                        // We reach this if we wait for on/off status
                        // after start/stop as defined in configurable delay/retries and
                        // did not reach the desired on/off status.
                        // We assume that fencing operation didn't complete successfully
                        // Setting this flag will cause the appropriate Alert to pop
                        // and to restore host status to it's previous value as
                        // appears in the finally block.
                        setFencingSucceeded(false);
                    }
                } else {
                    if (!((FenceStatusReturnValue) (vdsReturnValue.getReturnValue())).getIsSkipped()) {
                        // Since this is a non-transactive command , restore last status
                        setSucceeded(false);
                        log.errorFormat("Failed to {0} VDS", getParameters().getAction()
                                .name()
                                .toLowerCase());
                        throw new VdcBLLException(VdcBllErrors.VDS_FENCING_OPERATION_FAILED);
                    } else {  //Fencing operation was skipped because Host is already in the requested state.
                        setStatus(lastStatus);
                    }
                }
            }
            setSucceeded(getFencingSucceeded());
        } finally {
            if (!getSucceeded()) {
                setStatus(lastStatus);
                AlertIfPowerManagementOperationFailed();
            }
        }
    }

    /**
     * Create the executor used in the can do action check. The executor created does not do retries to find a proxy
     * host, so that clients calling the can do action will get a quick response, and don't risk timing out.
     *
     * @return An executor used to check the availability of a proxy host.
     */
    protected FencingExecutor createExecutorForProxyCheck() {
        return new FencingExecutor(getVds(), FenceActionType.Status);
    }

    protected void DestroyVmOnDestination(VM vm) {
        if (vm.getstatus() == VMStatus.MigratingFrom) {
            try {
                if (vm.getmigrating_to_vds() != null) {
                    Backend.getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.DestroyVm,
                                    new DestroyVmVDSCommandParameters(new Guid(vm.getmigrating_to_vds().toString()), vm
                                            .getvm_guid(), true, false, 0));
                    log.infoFormat("Stopped migrating vm: {0} on vds: {1}", vm.getvm_name(), vm.getmigrating_to_vds());
                }
            } catch (RuntimeException ex) {
                log.infoFormat("Could not stop migrating vm: {0} on vds: {1}, Error: {2}", vm.getvm_name(),
                        vm.getmigrating_to_vds(), ex.getMessage());
                // intentionally ingnored
            }
        }
    }

    protected void RestartVdsVms() {
        java.util.ArrayList<VdcActionParametersBase> runVmParamsList =
                new java.util.ArrayList<VdcActionParametersBase>();
        // restart all running vms of a failed vds.
        for (VM vm : mVmList) {
            DestroyVmOnDestination(vm);
            VDSReturnValue returnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SetVmStatus,
                            new SetVmStatusVDSCommandParameters(vm.getvm_guid(), VMStatus.Down));
            // Write that this VM was shut down by host rebbot or manual fence
            if (returnValue != null && returnValue.getSucceeded()) {
                LogSettingVmToDown(getVds().getvds_id(), vm.getvm_guid());
            }
            // ResourceManager.Instance.removeRunningVm(vm.vm_guid, VdsId);
            setVmId(vm.getvm_guid());
            setVmName(vm.getvm_name());
            setVm(vm);
            // EINAV: TODO: The next commented line of code is performing an
            // asynchronous task
            // (RestoreAllSnapshots) in case of a stateless VM. need to take
            // care of that case.
            // VmPoolHandler.ProcessVmPoolOnStopVm(VmId);

            //Handle highly available VMs
            if (vm.getauto_startup()) {
                runVmParamsList.add(new RunVmParams(vm.getvm_guid(), true));
            }
        }
        if (runVmParamsList.size() > 0) {
            Backend.getInstance().runInternalMultipleActions(VdcActionType.RunVm, runVmParamsList);
        }
        setVm(null);
        setVmId(Guid.Empty);
        setVmName(null);
    }

    protected void setStatus() {
        Backend.getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.SetVdsStatus,
                        new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Reboot));
        RunSleepOnReboot();
    }

    protected void HandleError() {
    }

    protected boolean waitForStatus(String vdsName, FenceActionType actionType) {
        final String FENCE_CMD = (actionType == FenceActionType.Start) ? "on" : "off";
        final String ACTION_NAME = actionType.name().toLowerCase();
        int i = 1;
        boolean statusReached = false;
        log.infoFormat("Waiting for vds {0} to {1}", vdsName, ACTION_NAME);
        // Waiting before first attempt to check the host status.
        // This is done because if we will attempt to get host status immediately
        // in most cases it will not turn from on/off to off/on and we will need
        // to wait a full cycle for it.
        ThreadUtils.sleep(SLEEP_BEFORE_FIRST_ATTEMPT);
        while (!statusReached && i <= getRerties()) {
            log.infoFormat("Attempt {0} to get vds {1} status", i, vdsName);
            if (_executor.FindVdsToFence()) {
                VDSReturnValue returnValue = _executor.Fence();
                if (returnValue != null && returnValue.getReturnValue() != null) {
                    FenceStatusReturnValue value = (FenceStatusReturnValue) returnValue.getReturnValue();
                    if (FENCE_CMD.equalsIgnoreCase(value.getStatus())) {
                        statusReached = true;
                        log.infoFormat("vds {0} status is {1}", vdsName, FENCE_CMD);
                    } else {
                        i++;
                        if (i <= getRerties())
                            ThreadUtils.sleep(getDelayInSeconds() * 1000);
                    }
                } else {
                    log.errorFormat("Failed to get host {0} status.", vdsName);
                    break;
                }
            } else {
                break;
            }
        }
        if (!statusReached) {
            // Send an Alert
            String actionName = (getParameters().getParentCommand() == VdcActionType.RestartVds) ?
                    FenceActionType.Restart.name() : ACTION_NAME;
            AuditLogableBase auditLogable = new AuditLogableBase();
            auditLogable.AddCustomValue("Host", vdsName);
            auditLogable.AddCustomValue("Status", actionName);
            AuditLogDirector.log(auditLogable, AuditLogType.VDS_ALERT_FENCING_STATUS_VERIFICATION_FAILED);
            log.errorFormat("Failed to verify host {0} {1} status. Have retried {2} times with delay of {3} seconds between each retry.",
                    vdsName,
                    ACTION_NAME,
                    getRerties(),
                    getDelayInSeconds());

        }
        return statusReached;
    }

    protected void setStatus(VDSStatus status) {
        if (getVds().getstatus() != status) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SetVdsStatus,
                            new SetVdsStatusVDSCommandParameters(getVds().getvds_id(), status));
        }
    }

    protected abstract void handleSpecificCommandActions();
}
