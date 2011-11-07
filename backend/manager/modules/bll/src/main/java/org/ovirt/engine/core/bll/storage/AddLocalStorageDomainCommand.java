package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSType;
//import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddLocalStorageDomainCommand<T extends StorageDomainManagementParameter> extends AddStorageDomainCommon<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddLocalStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddLocalStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean retVal = super.canDoAction();

        if (retVal) {
            storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().getForVds(getParameters().getVdsId());

            if (storagePool == null) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_CLUSTER_HAVE_NOT_EXISTING_DATA_CENTER_NETWORK);
                retVal = false;
            } else {
                setStoragePool(storagePool);
            }

            if (retVal && storagePool.getstorage_pool_type() != StorageType.LOCALFS) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_IS_NOT_LOCAL);
                retVal = false;
            }

            if (retVal && storagePool.getstatus() != StoragePoolStatus.Uninitialized) {
                retVal = CheckMasterDomainIsUp();
            }

            // we limit RHEV-H local storage to its persistence mount - /data/images/rhev/
            if (retVal && this.getVds().getvds_type() == VDSType.oVirtNode) {

                storage_server_connections conn =
                        DbFacade.getInstance().getStorageServerConnectionDAO().get(getParameters().getStorageDomain()
                                .getstorage());

                if (!conn.getconnection().equals(Config.<String> GetValue(ConfigValues.RhevhLocalFSPath))) {
                    addCanDoActionMessage(VdcBllMessages.RHEVH_LOCALFS_WRONG_PATH_LOCATION);
                    addCanDoActionMessage(String.format("$path %1$s",
                            Config.<String> GetValue(ConfigValues.RhevhLocalFSPath)));
                    retVal = false;
                }
            }
        }
        return retVal;
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        if (getSucceeded()) {
            VdcReturnValueBase returnValue = Backend.getInstance()
                    .runInternalAction(
                            VdcActionType.AttachStorageDomainToPool,
                            new StorageDomainPoolParametersBase(getStorageDomain().getid(), getStoragePool().getId()));
            if(!returnValue.getSucceeded()) {
                getReturnValue().setSucceeded(false);
                getReturnValue().setFault(returnValue.getFault());
            }
        }
    }
}
