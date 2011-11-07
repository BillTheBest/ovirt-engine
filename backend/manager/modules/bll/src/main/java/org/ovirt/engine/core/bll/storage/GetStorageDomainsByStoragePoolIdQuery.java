package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetStorageDomainsByStoragePoolIdQuery<P extends StoragePoolQueryParametersBase>
        extends QueriesCommandBase<P> {
    public GetStorageDomainsByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance().
                getStorageDomainDAO().getAllForStoragePool(getParameters().getStoragePoolId()));
    }
}
