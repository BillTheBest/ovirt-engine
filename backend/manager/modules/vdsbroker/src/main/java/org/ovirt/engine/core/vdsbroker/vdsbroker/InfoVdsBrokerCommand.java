package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;

public abstract class InfoVdsBrokerCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    protected InfoVdsBrokerCommand(P parameters, VDS vds) {
        super(parameters, vds);
    }

    protected VDSInfoReturnForXmlRpc infoReturn;

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return infoReturn.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return infoReturn;
    }
}
