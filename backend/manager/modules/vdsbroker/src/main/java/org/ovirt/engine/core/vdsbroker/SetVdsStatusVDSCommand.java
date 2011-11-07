package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.dal.dbbroker.*;
import org.ovirt.engine.core.common.businessentities.*;

public class SetVdsStatusVDSCommand<P extends SetVdsStatusVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public SetVdsStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {
        SetVdsStatusVDSCommandParameters parameters = getParameters();

        if (_vdsManager != null) {

            VDS vds = getVds();
            updateVdsFromParameters(parameters, vds);
            _vdsManager.setStatus(parameters.getStatus(), vds);
            _vdsManager.UpdateDynamicData(vds.getDynamicData());
            _vdsManager.UpdateStatisticsData(vds.getStatisticsData());

            // In case the SPM status has changed during execution in the StoragePool table we have to fetch the VDS
            // (which is a view) again.
            vds = DbFacade.getInstance().getVdsDAO().get(parameters.getVdsId());
            if (vds.getspm_status() != VdsSpmStatus.None && parameters.getStatus() != VDSStatus.Up) {
                log.infoFormat("SetVdsStatusVDSCommand::VSD {0} is spm and moved from up calling ResetIrs.",
                        vds.getvds_name());
                // check if this host was spm and reset if do.
                getVDSReturnValue().setSucceeded(
                        ResourceManager
                                .getInstance()
                                .runVdsCommand(
                                        VDSCommandType.ResetIrs,
                                        new ResetIrsVDSCommandParameters(vds.getstorage_pool_id(), vds
                                                .gethost_name(), vds.getvds_id())).getSucceeded());
            }
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }

    private void updateVdsFromParameters(SetVdsStatusVDSCommandParameters parameters, VDS vds) {
        vds.getDynamicData().setNonOperationalReason(parameters.getNonOperationalReason());
    }

    private static LogCompat log = LogFactoryCompat.getLog(SetVdsStatusVDSCommand.class);
}
