package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class StorageDomainManagementCommandBase<T extends StorageDomainManagementParameter> extends
        StorageDomainCommandBase<T> {
    public StorageDomainManagementCommandBase(T parameters) {
        super(parameters);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected StorageDomainManagementCommandBase(Guid commandId) {
        super(commandId);
    }

    @Override
    public storage_domains getStorageDomain() {
        if (super.getStorageDomain() == null) {
            super.setStorageDomain(new storage_domains());
        }
        super.getStorageDomain().setStorageStaticData(getParameters().getStorageDomain());
        return super.getStorageDomain();
    }

    protected boolean IsStorageWithSameNameExists() {
        return DbFacade.getInstance().getStorageDomainStaticDAO().getByName(getStorageDomain().getstorage_name()) != null;
    }
}
