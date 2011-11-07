package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class DestroyStoragePoolVDSCommand<P extends IrsBaseVDSCommandParameters> extends IrsBrokerCommand<P> {
    public DestroyStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        VDS vds = DbFacade.getInstance().getVdsDAO().get(this.getCurrentIrsProxyData().getCurrentVdsId());
        status = getIrsProxy().destroyStoragePool(getParameters().getStoragePoolId().toString(),
                vds.getvds_spm_id(), getParameters().getStoragePoolId().toString());
        ProceedProxyReturnValue();
        RemoveIrsProxy();
    }
}
