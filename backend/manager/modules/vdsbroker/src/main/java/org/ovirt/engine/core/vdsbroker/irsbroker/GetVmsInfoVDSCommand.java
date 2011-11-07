package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.vdsbroker.vdsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;
import org.ovirt.engine.core.common.vdscommands.*;

public class GetVmsInfoVDSCommand<P extends GetVmsInfoVDSCommandParameters> extends IrsBrokerCommand<P> {
    private GetVmsInfoReturnForXmlRpc _vmsInfo;

    public GetVmsInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        String storagePoolId = getParameters().getStoragePoolId().toString();
        String storageDomainId = getParameters().getStorageDomainId().toString();
        java.util.ArrayList<String> ids = new java.util.ArrayList<String>();

        if (getParameters().getVmIdList() != null) {
            for (Guid id : getParameters().getVmIdList()) {
                ids.add(id.toString());
            }
        }

        _vmsInfo = getIrsProxy().getVmsInfo(storagePoolId, storageDomainId, ids.toArray(new String[] {}));
        ProceedProxyReturnValue();

        XmlRpcStruct xmlRpcStruct = _vmsInfo.vmlist;
        java.util.ArrayList<String> retVal = new java.util.ArrayList<String>();
        for (String key : xmlRpcStruct.getKeys()) {
            retVal.add(xmlRpcStruct.getItem(key).toString());
        }
        setReturnValue(retVal);

    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _vmsInfo.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _vmsInfo;
    }
}
