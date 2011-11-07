package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class DeactivateStorageDomainVDSCommand<P extends DeactivateStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public DeactivateStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        status = getIrsProxy().deactivateStorageDomain(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(),
                getParameters().getMasterStorageDomainId().toString(),
                getParameters().getMasterVersion());
        ProceedProxyReturnValue();
    }
}
