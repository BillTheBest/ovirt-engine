package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.vdsbroker.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class ActivateStorageDomainVDSCommand<P extends ActivateStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    private StorageStatusReturnForXmlRpc _result;

    public ActivateStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        _result = getIrsProxy().activateStorageDomain(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString());
        ProceedProxyReturnValue();
        setReturnValue(_result.mStorageStatus);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
