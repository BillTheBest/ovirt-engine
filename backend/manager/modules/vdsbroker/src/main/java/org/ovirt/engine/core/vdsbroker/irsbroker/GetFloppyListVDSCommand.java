package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.vdsbroker.vdsbroker.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class GetFloppyListVDSCommand<P extends IrsBaseVDSCommandParameters> extends GetIsoListVDSCommand<P> {
    protected IsoListReturnForXmlRpc _isoList;

    public GetFloppyListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        if (getCurrentIrsProxyData().getIsValid()) {
            _isoList = getIrsProxy().getFloppyList(getParameters().getStoragePoolId().toString());
            ProceedProxyReturnValue();
            if (_isoList.mVMList != null && _isoList.mVMList.length > 0) {
                setReturnValue(new java.util.ArrayList<String>(java.util.Arrays.asList(_isoList.mVMList)));
            } else {
                setReturnValue(new java.util.ArrayList<String>());
            }
        } else {
            setReturnValue(new java.util.ArrayList<String>());
        }
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _isoList;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _isoList.mStatus;
    }
}
