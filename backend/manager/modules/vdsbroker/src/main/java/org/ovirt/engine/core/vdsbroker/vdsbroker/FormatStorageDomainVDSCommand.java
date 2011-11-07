package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class FormatStorageDomainVDSCommand<P extends FormatStorageDomainVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public FormatStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().formatStorageDomain(getParameters().getStorageDomainId().toString());
        ProceedProxyReturnValue();
    }
}
