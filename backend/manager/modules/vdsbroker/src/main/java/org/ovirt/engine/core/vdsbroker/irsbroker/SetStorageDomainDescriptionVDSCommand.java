package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class SetStorageDomainDescriptionVDSCommand<P extends SetStorageDomainDescriptionVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public SetStorageDomainDescriptionVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        status = getIrsProxy().setStorageDomainDescription(getParameters().getStorageDomainId().toString(),
                getParameters().getDescription());
        ProceedProxyReturnValue();
    }
}
