package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetStoragePoolsByStorageDomainIdQuery<P extends StorageDomainQueryParametersBase>
        extends QueriesCommandBase<P> {
    public GetStoragePoolsByStorageDomainIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance()
                        .getStoragePoolDAO()
                        .getAllForStorageDomain(
                                getParameters().getStorageDomainId()));
    }
}
