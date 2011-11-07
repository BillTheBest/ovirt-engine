package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.MarkPoolInReconstructModeVDSCommandParameters;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

public class MarkPoolInReconstructModeVDSCommand<P extends MarkPoolInReconstructModeVDSCommandParameters>
        extends IrsBrokerCommand<P> {

    public MarkPoolInReconstructModeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVDSCommand() {
        try {
            IrsProxyData proxyData = getCurrentIrsProxyData();
            switch (getParameters().getReconstructMarkAction()) {
            case ClearJobs:
                proxyData.clearPoolTimers();
                break;
            case ClearCache:
                proxyData.clearCache();
                break;
            default:
                break;
            }
        } catch (Exception e) {
            log.error("Could not change timers for pool " + getParameters().getStoragePoolId(), e);
        }
        getVDSReturnValue().setSucceeded(true);
    }

    private static LogCompat log = LogFactoryCompat.getLog(MarkPoolInReconstructModeVDSCommand.class);
}
