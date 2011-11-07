package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.SpmStatus;
import org.ovirt.engine.core.common.businessentities.SpmStatusResult;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.SpmStatusVDSCommandParameters;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSNonOperationalException;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

@Logged(executionLevel = LogLevel.DEBUG)
public class SpmStatusVDSCommand<P extends SpmStatusVDSCommandParameters> extends VdsBrokerCommand<P> {
    private SpmStatusReturnForXmlRpc _result;

    public SpmStatusVDSCommand(P parameters) {
        super(parameters, null);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().spmStatus(getParameters().getStoragePoolId().toString());
        ProceedProxyReturnValue();
        setReturnValue(ParseSpmStatusResult(_result.spmStatus));
        if (((SpmStatusResult) getReturnValue()).getSpmStatus() == SpmStatus.SPM_ERROR) {
            log.errorFormat("SPM {0} status returned SPM_ERROR on VDS {1}", getParameters().getStoragePoolId(),
                    getParameters().getVdsId());
            throw new IRSNonOperationalException("SPM status returned SPM_ERROR");
        }
    }

    @Override
    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case StoragePoolUnknown:
            // ignore this, the parser can handle the empty result.
            break;

        default:
            super.ProceedProxyReturnValue();
            InitializeVdsError(returnStatus);
            break;
        }
    }

    private SpmStatusResult ParseSpmStatusResult(XmlRpcStruct spmStatusResult) {
        try {
            // The parser handle an empty structure because of Mr. Frank
            // inability create a protocol that makes sense.
            SpmStatusResult statusResult = new SpmStatusResult();
            statusResult
                    .setSpmStatus((_result.spmStatus != null && _result.spmStatus.contains("spmStatus")) ? EnumUtils
                            .valueOf(SpmStatus.class, _result.spmStatus.getItem("spmStatus").toString(), true)
                            : SpmStatus.Unknown_Pool);

            // if no lver and id put -5 hardcoded because of VDSM inconsistency
            statusResult
                    .setSpmLVER((_result.spmStatus != null && _result.spmStatus.contains("spmLver")) ? _result.spmStatus
                            .getItem("spmLver").toString()
                            : "-5");
            statusResult.setSpmId((_result.spmStatus != null && _result.spmStatus.contains("spmId")) ? Integer
                    .parseInt(_result.spmStatus.getItem("spmId").toString()) : -5);
            return statusResult;
        } catch (RuntimeException exp) {
            log.errorFormat("Could not parse SPM Status: {0}", _result.spmStatus.toString());
            throw exp;
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }

    private static LogCompat log = LogFactoryCompat.getLog(SpmStatusVDSCommand.class);
}
