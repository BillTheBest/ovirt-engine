package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.UpdateSpmHostNameVDSCommandParameters;
import org.ovirt.engine.core.compat.StringHelper;

public class UpdateSpmHostNameVDSCommand<P extends UpdateSpmHostNameVDSCommandParameters> extends IrsBrokerCommand<P> {
    public UpdateSpmHostNameVDSCommand(P parameters) {
        super(parameters);
    }

    // overriding ExecuteVDSCommand in order not to wait in getIrsProxy locking
    @Override
    protected void ExecuteVDSCommand() {
        // only if hostName in IrsProxy cache is the same as sent hostName
        // update to new hostName
        if (StringHelper.EqOp(getCurrentIrsProxyData().getmCurrentIrsHost(), getParameters().getOldHostName())) {
            getCurrentIrsProxyData().setmCurrentIrsHost(getParameters().getNewHostName());
        }
    }
}
