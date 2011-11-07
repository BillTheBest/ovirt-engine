package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.FormatStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.RemoveVGVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveStorageDomainCommand<T extends RemoveStorageDomainParameters> extends StorageDomainCommandBase<T> {
    public RemoveStorageDomainCommand(T parameters) {
        super(parameters);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected void executeCommand() {
        storage_domains dom = getStorageDomain();
        VDS vds = getVds();
        boolean format = getParameters().getDoFormat();

        setSucceeded(false);

        if (isLocalFs(dom) && isDomainAttached(dom) && !detachStorage(dom)) {
            return;
        }

        if (!isISO(dom) && !isExport(dom) || format) {
            if (!ConnectStorage()) {
                return;
            }

            boolean failed = !formatStorage(dom, vds) || !removeStorage(dom, vds);

            DisconnectStorage();

            if (failed) {
                return;
            }
        }

        getStorageHelper(dom).StorageDomainRemoved(dom.getStorageStaticData());
        getDb().getStorageDomainDynamicDAO().remove(dom.getid());
        getDb().getStorageDomainStaticDAO().remove(dom.getid());

        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_STORAGE_DOMAIN
                : AuditLogType.USER_REMOVE_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        storage_domains dom = getStorageDomain();
        VDS vds = getVds();
        boolean format = getParameters().getDoFormat();

        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);

        if (!CheckStorageDomain() || !checkStorageDomainSharedStatusNotLocked(dom)) {
            return false;
        }

        if (!isLocalFs(dom) && !CheckStorageDomainNotInPool()) {
            return false;
        }

        if (isLocalFs(dom) && isDomainAttached(dom) && !canDetachDomain(getParameters().getDestroyingPool(), false, true)) {
            return false;
        }

        if (vds == null) {
            addCanDoActionMessage(VdcBllMessages.CANNOT_REMOVE_STORAGE_DOMAIN_INVALID_HOST_ID);
            return false;
        }

        if (isDataDomain(dom) && !format) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_REMOVE_STORAGE_DOMAIN_DO_FORMAT);
            return false;
        }

        return true;
    }

    private boolean ConnectStorage() {
        return getStorageHelper(getStorageDomain()).ConnectStorageToDomainByVdsId(getStorageDomain(),
                getVds().getvds_id());
    }

    private void DisconnectStorage() {
        getStorageHelper(getStorageDomain()).DisconnectStorageFromDomainByVdsId(getStorageDomain(),
                getVds().getvds_id());
    }

    protected DbFacade getDb() {
        return DbFacade.getInstance();
    }

    protected BackendInternal getBackend() {
        return Backend.getInstance();
    }

    protected VDSBrokerFrontend getVdsBroker() {
        return getBackend().getResourceManager();
    }

    protected IStorageHelper getStorageHelper(storage_domains storageDomain) {
        return StorageHelperDirector.getInstance().getItem(storageDomain.getstorage_type());
    }

    protected boolean isFCP(storage_domains dom) {
        return dom.getstorage_type() == StorageType.FCP;
    }

    protected boolean isISCSI(storage_domains dom) {
        return dom.getstorage_type() == StorageType.ISCSI;
    }

    protected boolean isLocalFs(storage_domains dom) {
        return dom.getstorage_type() == StorageType.LOCALFS;
    }

    protected boolean isDataDomain(storage_domains dom) {
        return dom.getstorage_domain_type() == StorageDomainType.Data;
    }

    protected boolean isISO(storage_domains dom) {
        return dom.getstorage_domain_type() == StorageDomainType.ISO;
    }

    protected boolean isExport(storage_domains dom) {
        return dom.getstorage_domain_type() == StorageDomainType.ImportExport;
    }

    protected boolean isDomainAttached(storage_domains storageDomain) {
        if (storageDomain.getstorage_pool_id() == null) {
            return false;
        }

        Guid storageDomainId = storageDomain.getid();
        Guid storagePoolId = storageDomain.getstorage_pool_id().getValue();

        return getDb().getStoragePoolIsoMapDAO()
                .get(new StoragePoolIsoMapId(storageDomainId, storagePoolId)) != null;
    }

    protected boolean detachStorage(storage_domains dom) {
        Guid domId = dom.getid();
        Guid poolId = dom.getstorage_pool_id().getValue();
        DetachStorageDomainFromPoolParameters params = new DetachStorageDomainFromPoolParameters(domId, poolId);
        params.setDestroyingPool(getParameters().getDestroyingPool());

        return getBackend()
                .runInternalAction(VdcActionType.DetachStorageDomainFromPool,
                               params).getSucceeded();
    }

    protected boolean formatStorage(storage_domains dom, VDS vds) {
        return getVdsBroker()
                .RunVdsCommand(VDSCommandType.FormatStorageDomain,
                           new FormatStorageDomainVDSCommandParameters(vds.getvds_id(), dom.getid())).getSucceeded();
    }

    protected boolean removeStorage(storage_domains dom, VDS vds) {
        if (!isFCP(dom) && !isISCSI(dom)) {
            return true;
        }
        return getVdsBroker()
                .RunVdsCommand(VDSCommandType.RemoveVG,
                           new RemoveVGVDSCommandParameters(vds.getvds_id(), dom.getstorage())).getSucceeded();
    }
}
