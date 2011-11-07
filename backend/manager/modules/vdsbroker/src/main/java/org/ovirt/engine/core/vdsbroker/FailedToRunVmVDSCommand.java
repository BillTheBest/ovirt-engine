package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class FailedToRunVmVDSCommand<P extends FailedToRunVmVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public FailedToRunVmVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {
        if (_vdsManager != null) {
            _vdsManager.failedToRunVm(getVds());
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }
}
