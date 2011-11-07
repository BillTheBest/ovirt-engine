package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class DisconnectStoragePoolVDSCommand<P extends DisconnectStoragePoolVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public DisconnectStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().disconnectStoragePool(getParameters().getStoragePoolId().toString(),
                getParameters().getvds_spm_id(), getParameters().getStoragePoolId().toString());
        ProceedProxyReturnValue();
    }
}
