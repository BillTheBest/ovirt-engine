package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class FenceVdsVDSCommand<P extends FenceVdsVDSCommandParameters> extends VdsBrokerCommand<P> {
    private FenceStatusReturnForXmlRpc _result;

    public FenceVdsVDSCommand(P parameters) {
        super(parameters);
    }

    /**
     * Alerts the specified log type.
     *
     * @param logType
     *            Type of the log.
     */
    private void Alert(AuditLogType logType) {
        AuditLogableBase alert = new AuditLogableBase();
        alert.setVdsId(getParameters().getTargetVdsID());
        AlertDirector.Alert(alert, logType);
    }

    /**
     * Alerts the specified log type.
     *
     * @param logType
     *            Type of the log.
     * @param reason
     *            The reason.
     */
    private void Alert(AuditLogType logType, String reason) {
        AuditLogableBase alert = new AuditLogableBase();
        alert.setVdsId(getParameters().getTargetVdsID());
        alert.AddCustomValue("Reason", reason);
        AlertDirector.Alert(alert, logType);
    }

    /**
     * Alerts if power management status failed.
     *
     * @param reason
     *            The reason.
     */
    protected void AlertPowerManagementStatusFailed(String reason) {
        Alert(AuditLogType.VDS_ALERT_FENCING_TEST_FAILED, reason);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        VdsFencingOptions vdsFencingOptions = new VdsFencingOptions(getParameters().getType(),
                getParameters().getOptions());
        String options = vdsFencingOptions.ToInternalString();
        // ignore starting already started host or stopping already stopped host.
        if (!isAlreadyInRequestedStatus(options)) {
            _result = getBroker().fenceNode(getParameters().getIp(), "",
                    getParameters().getType(), getParameters().getUser(),
                    getParameters().getPassword(), GetActualActionName(), "", options);

            ProceedProxyReturnValue();
            getVDSReturnValue().setSucceeded(false);
            if (getParameters().getAction() == FenceActionType.Status && _result.Power != null) {
                String stat = _result.Power.toLowerCase();
                String msg = _result.mStatus.mMessage;
                if (StringHelper.EqOp(stat, "on") || StringHelper.EqOp(stat, "off")) {
                    getVDSReturnValue().setSucceeded(true);
                } else {
                    if (!getParameters().getTargetVdsID().equals(Guid.Empty)) {
                        AlertPowerManagementStatusFailed(msg);
                    }

                }
                FenceStatusReturnValue fenceStatusReturnValue = new FenceStatusReturnValue(stat, msg);
                setReturnValue(fenceStatusReturnValue);
            } else {
                setReturnValue((_result.mStatus.mMessage != null) ? _result.mStatus.mMessage : "");
                getVDSReturnValue().setSucceeded(true);
            }
        } else {
            handleSkippedOperation();
        }
    }
    /**
    * Handles cases where fencing operation was skipped (host is already in requested state)
    */
    private void handleSkippedOperation() {
        FenceStatusReturnValue fenceStatusReturnValue = new FenceStatusReturnValue(FenceStatusReturnValue.SKIPPED,"");
        AuditLogableBase auditLogable = new AuditLogableBase();
        auditLogable.AddCustomValue("HostName", (DbFacade.getInstance().getVdsDAO().get(getParameters().getTargetVdsID())).getvds_name());
        auditLogable.AddCustomValue("AgentStatus", GetActualActionName());
        auditLogable.AddCustomValue("Operation", getParameters().getAction().toString());
        AuditLogDirector.log(auditLogable, AuditLogType.VDS_ALREADY_IN_REQUESTED_STATUS);
        getVDSReturnValue().setSucceeded(true);
        setReturnValue(fenceStatusReturnValue);
    }

    /**
     * Checks if Host is already in the requested status.
     * If Host is Down and a Stop command is issued or
     * if Host is Up and a Start command is issued
     * command should do nothing.
     *
     * @param options
     *            Fencing options passed to the agent
     * @return
     */
    private boolean isAlreadyInRequestedStatus(String options) {
        boolean ret = false;
        FenceActionType action = getParameters().getAction();
        _result = getBroker().fenceNode(getParameters().getIp(), "",
                getParameters().getType(), getParameters().getUser(),
                getParameters().getPassword(), "status", "", options);
        if (_result.Power != null) {
            String status = _result.Power.toLowerCase();
            if ((action == FenceActionType.Start && status.equals("on")) ||
                    action == FenceActionType.Stop && status.equals("off"))
                ret = true;
        }
        return ret;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return (_result.mStatus != null) ? _result.mStatus : new StatusForXmlRpc();
    }

    private String GetActualActionName() {
        String actualActionName;
        switch (getParameters().getAction()) {
        case Restart:
            actualActionName = "reboot";
            break;
        case Start:
            actualActionName = "on";
            break;
        case Stop:
            actualActionName = "off";
            break;
        default:
            actualActionName = "status";
            break;
        }
        return actualActionName;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
