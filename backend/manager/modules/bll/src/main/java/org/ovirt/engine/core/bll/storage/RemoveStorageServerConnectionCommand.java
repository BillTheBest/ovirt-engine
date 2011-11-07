package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@InternalCommandAttribute
public class RemoveStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends
        StorageServerConnectionCommandBase<T> {
    public RemoveStorageServerConnectionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        setSucceeded(Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.DisconnectStorageServer,
                        new ConnectStorageServerVDSCommandParameters(getParameters().getVdsId(), getParameters()
                                .getStoragePoolId(), getParameters().getStorageServerConnection().getstorage_type(),
                                new java.util.ArrayList<storage_server_connections>(java.util.Arrays
                                        .asList(new storage_server_connections[] { getConnection() })))).getSucceeded());
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;

        if (getConnection().getstorage_type() == StorageType.LOCALFS) {
            storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().getForVds(getVds().getvds_id());
            if (storagePool == null || storagePool.getstorage_pool_type() != StorageType.LOCALFS) {
                returnValue = false;
                getReturnValue().getCanDoActionMessages().add(
                        VdcBllMessages.VDS_CANNOT_REMOVE_LOCAL_STORAGE_ON_NON_LOCAL_HOST.toString());
            }
        }

        return returnValue;
    }

    protected String getStorageConnection() {
        return getConnection().getconnection();
    }
}
