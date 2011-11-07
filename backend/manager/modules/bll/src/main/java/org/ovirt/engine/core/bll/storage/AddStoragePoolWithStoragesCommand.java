package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolWithStoragesParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.CreateStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
@LockIdNameAttribute(fieldName = "StoragePoolId")
public class AddStoragePoolWithStoragesCommand<T extends StoragePoolWithStoragesParameter> extends
        UpdateStoragePoolCommand<T> {
    public AddStoragePoolWithStoragesCommand(T parameters) {
        super(parameters);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected AddStoragePoolWithStoragesCommand(Guid commandId) {
        super(commandId);
    }

    private storage_domains masterStorageDomain = null;
    VDSReturnValue retVal;

    @Override
    protected void executeCommand() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                if (UpdateStorageDomainsInDb()) {
                    // setting storage pool status to maintenance
                    storage_pool storagePool = getStoragePool();
                    getCompensationContext().snapshotEntity(storagePool);
                    TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            getStoragePool().setstatus(StoragePoolStatus.Maintanance);
                            getStoragePool().setStoragePoolFormatType(masterStorageDomain.getStorageFormat());
                            DbFacade.getInstance().getStoragePoolDAO().update(getStoragePool());
                            getCompensationContext().stateChanged();
                            StoragePoolStatusHandler.PoolStatusChanged(getStoragePool().getId(),
                                    getStoragePool().getstatus());
                            return null;
                        }
                    });

                    // Following code performs only read operations, therefore no need for new transaction
                    boolean returnValue =
                            TransactionSupport.executeInScope(TransactionScopeOption.Required,
                                    new TransactionMethod<Boolean>() {
                                        @Override
                                        public Boolean runInTransaction() {
                                            boolean result = false;
                                            retVal = null;
                                            for (VDS vds : getAllRunningVdssInPool()) {
                                                setVds(vds);
                                                for (Guid storageDomainId : getParameters().getStorages()) {
                                                    // now the domain should have the mapping
                                                    // with the pool in db
                                                    storage_domains storageDomain =
                                                            DbFacade.getInstance()
                                                                    .getStorageDomainDAO()
                                                                    .getForStoragePool(storageDomainId,
                                                                            getStoragePool().getId());
                                                    StorageHelperDirector.getInstance()
                                                            .getItem(storageDomain.getstorage_type())
                                                            .ConnectStorageToDomainByVdsId(storageDomain,
                                                                    getVds().getvds_id());
                                                }
                                                retVal = AddStoragePoolInIrs();
                                                if (!retVal.getSucceeded()
                                                        && retVal.getVdsError().getCode() == VdcBllErrors.StorageDomainAccessError) {
                                                    log.warnFormat("Error creating storage pool on vds {0} - continuing",
                                                            vds.getvds_name());
                                                    continue;
                                                } else {
                                                    // storage pool creation succeeded or failed
                                                    // but didn't throw exception
                                                    result = retVal.getSucceeded();
                                                    break;
                                                }
                                            }
                                            return result;
                                        }
                                    });

                    setSucceeded(returnValue);
                    if (!returnValue) {
                        if (retVal != null && retVal.getVdsError().getCode() != null) {
                            throw new VdcBLLException(retVal.getVdsError().getCode(), retVal.getVdsError().getMessage());
                        } else {
                            // throw exception to cause rollback and stop the
                            // command
                            throw new VdcBLLException(VdcBllErrors.ENGINE_ERROR_CREATING_STORAGE_POOL);
                        }
                    }
                }
                return null;
            }
        });

        // Create pool phase completed, no rollback is needed here, so compensation information needs to be cleared!
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getCompensationContext().resetCompensation();
                return null;
            }
        });
        // if create succeeded activate
        if (getSucceeded()) {
            ActivateStorageDomains();
        }
    }

    private boolean UpdateStorageDomainsInDb() {
        boolean result  = TransactionSupport.executeInNewTransaction(new TransactionMethod<Boolean>() {

            @Override
            public Boolean runInTransaction() {
                for (Guid storageDomainId : getParameters().getStorages()) {
                    storage_domains storageDomain = DbFacade.getInstance().getStorageDomainDAO().get(
                                storageDomainId);
                    if (storageDomain != null) {
                        storage_pool_iso_map mapFromDB =
                            DbFacade.getInstance()
                            .getStoragePoolIsoMapDAO()
                            .get(new StoragePoolIsoMapId(storageDomain.getid(), getStoragePool().getId()));
                        boolean existingInDb = mapFromDB != null;
                        if (existingInDb) {
                            getCompensationContext().snapshotEntity(mapFromDB);
                        }
                        storageDomain.setstorage_pool_id(getStoragePool().getId());
                        if (masterStorageDomain == null
                                && storageDomain.getstorage_domain_type() == StorageDomainType.Data) {
                            // increase master domain version - no need to snapshot, as we would like
                            // the master domain version to grow monotonously even if the wrapping transaction fails
                            getStoragePool().setmaster_domain_version(getStoragePool().getmaster_domain_version() + 1);
                            getCompensationContext().snapshotEntity(storageDomain.getStorageStaticData());
                            storageDomain.setstorage_domain_type(StorageDomainType.Master);
                            DbFacade.getInstance().getStorageDomainStaticDAO().update(storageDomain.getStorageStaticData());
                            masterStorageDomain = storageDomain;
                            // The update of storage pool should be without compensation,
                            // this is why we run it in a different SUPRESS transaction.
                            updateStoragePoolInDiffTransaction();
                        }
                        storageDomain.setstatus(StorageDomainStatus.Locked);
                        if (existingInDb) {
                            DbFacade.getInstance()
                                        .getStoragePoolIsoMapDAO()
                                        .update(storageDomain.getStoragePoolIsoMapData());
                        } else {
                            DbFacade.getInstance()
                                        .getStoragePoolIsoMapDAO()
                                        .save(storageDomain.getStoragePoolIsoMapData());
                            getCompensationContext().snapshotNewEntity(storageDomain.getStoragePoolIsoMapData());
                        }
                    } else {
                        return false;
                    }
                }
                getCompensationContext().stateChanged();
                return true;
            }
        });
        return result && masterStorageDomain != null;
    }

    /**
     * Save the master version out of the transaction
     */

    private VDSReturnValue AddStoragePoolInIrs() {
        return Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.CreateStoragePool,
                        new CreateStoragePoolVDSCommandParameters(getVds().getvds_id(), getStoragePool()
                                .getstorage_pool_type(), getStoragePool().getId(), getStoragePool().getname(),
                                masterStorageDomain.getid(), getParameters().getStorages(), getStoragePool()
                                        .getmaster_domain_version()));
    }

    private boolean ActivateStorageDomains() {
        boolean returnValue = true;
        for (final Guid storageDomainId : getParameters().getStorages()) {
            StorageDomainPoolParametersBase activateParameters = new StorageDomainPoolParametersBase(storageDomainId,
                    getStoragePool().getId());
            activateParameters.setSessionId(getParameters().getSessionId());
            activateParameters.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            returnValue = Backend.getInstance()
                    .runInternalAction(VdcActionType.ActivateStorageDomain, activateParameters).getSucceeded();

            // if activate domain failed then set domain status to inactive
            if (!returnValue) {
                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                    @Override
                    public Void runInTransaction() {
                        DbFacade.getInstance()
                                .getStoragePoolIsoMapDAO()
                                .updateStatus(
                                        new StoragePoolIsoMapId(storageDomainId, getStoragePool().getId()),
                                        StorageDomainStatus.InActive);
                        return null;
                    }
                });
            }
        }

        return returnValue;
    }

    private boolean checkStorageDomainsInPool() {
        if (!getParameters().getIsInternal()) {
            boolean _hasData = false;
            StorageFormatType storageFormat = null;
            for (Guid storageDomainId : getParameters().getStorages()) {
                storage_domains domain = DbFacade.getInstance().getStorageDomainDAO().get(storageDomainId);
                if (isStorageDomainNotNull(domain) && checkDomainCanBeAttached(domain)) {
                    if (domain.getstorage_domain_type() == StorageDomainType.Data) {
                        _hasData = true;
                        if (storageFormat == null) {
                            storageFormat = domain.getStorageFormat();
                        } else if (storageFormat != domain.getStorageFormat()) {
                            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ADD_STORAGE_POOL_WITH_DIFFERENT_STORAGE_FORMAT);
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
            if (!_hasData) {
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ADD_STORAGE_POOL_WITHOUT_DATA_AND_ISO_DOMAINS);
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = super.canDoAction() && !IsObjecteLocked() && CheckStoragePool()
                && CheckStoragePoolStatus(StoragePoolStatus.Uninitialized) && InitializeVds()
                && checkStorageDomainsInPool();
        return returnValue;
    }

    @Override
    public void Rollback() {
        super.Rollback();
        // try to set status of all domains in the pool that are locked back to inactive
        for (Guid storageDomainId : getParameters().getStorages()) {
            storage_pool_iso_map domainPoolMap =
                    DbFacade.getInstance()
                            .getStoragePoolIsoMapDAO()
                            .get(new StoragePoolIsoMapId(storageDomainId,
                                    getStoragePoolId().getValue()));
            if (domainPoolMap != null && domainPoolMap.getstatus() == StorageDomainStatus.Locked) {
                try {
                    domainPoolMap.setstatus(StorageDomainStatus.InActive);
                    DbFacade.getInstance()
                            .getStoragePoolIsoMapDAO()
                            .updateStatus(domainPoolMap.getId(), domainPoolMap.getstatus());
                } catch (Exception e) {
                    log.warnFormat("Could not set domain {0} status to inactive in pool {1} during rollback: {2}",
                            storageDomainId,
                            getStoragePoolId().getValue(),
                            e.getMessage());
                }
            }
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(AddStoragePoolWithStoragesCommand.class);
}
