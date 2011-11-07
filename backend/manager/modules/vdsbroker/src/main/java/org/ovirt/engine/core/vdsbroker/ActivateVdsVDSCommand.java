package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class ActivateVdsVDSCommand<P extends ActivateVdsVDSCommandParameters> extends VdsIdVDSCommandBase<P> {
    public ActivateVdsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {
        if (_vdsManager != null) {
            _vdsManager.activate();
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }
}
