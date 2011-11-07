package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class VmMonitorCommandVDSCommand<P extends VmMonitorCommandVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Guid mVmId = new Guid();
    private String mMonitorCommand;

    public VmMonitorCommandVDSCommand(P parameters) {
        super(parameters);
        mVmId = parameters.getVmId();
        mMonitorCommand = parameters.getCommand();
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().monitorCommand(mVmId.toString(), mMonitorCommand);
        ProceedProxyReturnValue();
    }
}
