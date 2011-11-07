package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class StartVdsCommand<T extends FenceVdsActionParameters> extends FenceVdsBaseCommand<T> {
    public StartVdsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = super.canDoAction();
        VDS vds = getVds();
        VDSStatus vdsStatus = vds.getstatus();
        if (vdsStatus == VDSStatus.Problematic) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VDS_INTERMITENT_CONNECTIVITY);

        } else if (!legalStatusForStartingVds(vdsStatus)) {
            addCanDoActionMessage(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_START);
            retValue = false;
            log.errorFormat("VDS status for vds {0}:{1} is {2}", vds.getvds_id(), vds.getvds_name(), vdsStatus);
        }
        return retValue;
    }

    protected boolean legalStatusForStartingVds(VDSStatus status) {
        return status == VDSStatus.Down || status == VDSStatus.NonResponsive || status == VDSStatus.Reboot || status == VDSStatus.Maintenance;
    }

    @Override
    protected void setStatus() {
        setStatus(VDSStatus.NonResponsive);
    }

    @Override
    protected void HandleError() {
        log.errorFormat("Failed to run StartVdsCommand on vds :{0}", getVdsName());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCanDoActionMessage(VdcBllMessages.VDS_FENCING_OPERATION_FAILED);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__START);

        return getSucceeded() ? AuditLogType.USER_VDS_START : AuditLogType.USER_FAILED_VDS_START;
    }

    @Override
    protected void handleSpecificCommandActions() {
        RestartVdsVms();
    }

    @Override
    protected int getRerties() {
        return Config.<Integer> GetValue(ConfigValues.FenceStartStatusRetries);
    }

    @Override
    protected int getDelayInSeconds() {
        return Config.<Integer> GetValue(ConfigValues.FenceStartStatusDelayBetweenRetriesInSec);
    }

    private static LogCompat log = LogFactoryCompat.getLog(StartVdsCommand.class);
}
