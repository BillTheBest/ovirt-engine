package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByConnectionParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Query to retrieve the storage domains which use the given connection (if none then an empty list is returned) in a
 * specific storage pool.
 */
public class GetStorageDomainsByConnectionQuery<P extends GetStorageDomainsByConnectionParameters>
        extends QueriesCommandBase<P> {
    public GetStorageDomainsByConnectionQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid storagePoolId = getParameters().getStoragePoolId();
        String connection = getParameters().getConnection();
        List<storage_domains> domainsList;

        if (storagePoolId != null) {
            domainsList =
                    DbFacade.getInstance()
                            .getStorageDomainDAO()
                            .getAllByStoragePoolAndConnection(storagePoolId, connection);
        } else {
            domainsList =
                    DbFacade.getInstance()
                            .getStorageDomainDAO()
                            .getAllForConnection(connection);
        }
        getQueryReturnValue().setReturnValue(domainsList);
    }
}
