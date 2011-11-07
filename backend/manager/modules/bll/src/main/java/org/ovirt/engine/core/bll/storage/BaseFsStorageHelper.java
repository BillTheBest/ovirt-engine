package org.ovirt.engine.core.bll.storage;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.vdscommands.ConnectStorageServerVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class BaseFsStorageHelper extends StorageHelperBase {

    protected StorageType storageType;

    @Override
    protected boolean RunConnectionStorageToDomain(storage_domains storageDomain, Guid vdsId, int type) {
        boolean returnValue = false;
        storage_server_connections connection = DbFacade.getInstance().getStorageServerConnectionDAO().get(
                storageDomain.getstorage());
        if (connection != null) {
            returnValue = Backend
                    .getInstance()
                    .runInternalAction(VdcActionType.forValue(type),
                            new StorageServerConnectionParametersBase(connection, vdsId)).getSucceeded();
        } else {
            getLog().warn("Did not connect host: " + vdsId + " to storage domain: " + storageDomain.getstorage_name()
                    + " because connection for connectionId:" + storageDomain.getstorage() + " is null.");
        }
        return returnValue;
    }

    @Override
    public boolean ValidateStoragePoolConnectionsInHost(VDS vds, List<storage_server_connections> connections,
            Guid storagePoolId) {
        java.util.HashMap<String, String> validateConnections = (java.util.HashMap<String, String>) Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.ValidateStorageServerConnection,
                        new ConnectStorageServerVDSCommandParameters(vds.getvds_id(), storagePoolId, this.storageType,
                                connections)).getReturnValue();
        return IsConnectSucceeded(validateConnections, connections);
    }

    @Override
    public boolean IsConnectSucceeded(java.util.HashMap<String, String> returnValue,
            List<storage_server_connections> connections) {
        boolean result = true;
        for (Map.Entry<String, String> entry : returnValue.entrySet()) {
            if (!"0".equals(entry.getValue())) {
                String connectionField = addToAuditLogErrorMessage(entry.getKey(), entry.getValue(), connections);
                printLog(getLog(), connectionField, entry.getValue());
                result = false;
            }
        }

        return result;
    }

    @Override
    public List<storage_server_connections> GetStorageServerConnectionsByDomain(
            storage_domain_static storageDomain) {
        return new java.util.ArrayList<storage_server_connections>(
                java.util.Arrays.asList(new storage_server_connections[] { DbFacade.getInstance()
                        .getStorageServerConnectionDAO().get(storageDomain.getstorage()) }));
    }

    protected abstract LogCompat getLog();
}
