package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.errors.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class HSMClearTaskVDSCommand<P extends HSMTaskGuidBaseVDSCommandParameters> extends VdsBrokerCommand<P> {
    public HSMClearTaskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().clearTask(getParameters().getTaskId().toString());
        ProceedProxyReturnValue();
    }

    @Override
    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());

        switch (returnStatus) {
        case UnknownTask:
            log.error(String.format("Trying to remove unknown task: %1$s", getParameters().getTaskId()));
            return;
        case TaskStateError:
            InitializeVdsError(returnStatus);
            getVDSReturnValue().setSucceeded(false);
            return;
        }
        super.ProceedProxyReturnValue();
    }

    private static LogCompat log = LogFactoryCompat.getLog(HSMClearTaskVDSCommand.class);
}
