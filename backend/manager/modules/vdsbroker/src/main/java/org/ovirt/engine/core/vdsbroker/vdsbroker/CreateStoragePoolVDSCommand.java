package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class CreateStoragePoolVDSCommand<P extends CreateStoragePoolVDSCommandParameters> extends VdsBrokerCommand<P> {
    public CreateStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        // LINQ SP140
        // string[] ids = CreateParameters.DomainsIdList.ConvertAll<string>(a =>
        // a.toString()).ToArray();
        Guid[] guids = getParameters().getDomainsIdList().toArray(new Guid[0]);
        String[] ids = new String[guids.length];
        for (int i = 0; i < guids.length; i++) {
            ids[i] = guids[i].toString();
        }

        status = getBroker().createStoragePool(getParameters().getStorageType().getValue(),
                getParameters().getStoragePoolId().toString(), getParameters().getStoragePoolName(),
                getParameters().getMasterDomainId().toString(), ids, getParameters().getMasterVersion(),
                Config.<String> GetValue(ConfigValues.LockPolicy),
                Config.<Integer> GetValue(ConfigValues.LockRenewalIntervalSec),
                Config.<Integer> GetValue(ConfigValues.LeaseTimeSec),
                Config.<Integer> GetValue(ConfigValues.IoOpTimeoutSec),
                Config.<Integer> GetValue(ConfigValues.LeaseRetries));
        ProceedProxyReturnValue();
    }

    @Override
    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        // fail the operation without throwing exception
        case StorageDomainAccessError:
            getVDSReturnValue().setSucceeded(false);
            VDSError tempVar = new VDSError();
            tempVar.setCode(VdcBllErrors.StorageDomainAccessError);
            tempVar.setMessage(getReturnStatus().mMessage);
            getVDSReturnValue().setVdsError(tempVar);
            break;

        default:
            super.ProceedProxyReturnValue();
            InitializeVdsError(returnStatus);
            break;
        }
    }
}
