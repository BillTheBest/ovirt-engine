package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetAvailableClusterVersionsByStoragePoolQuery<P extends GetAvailableClusterVersionsByStoragePoolParameters>
        extends QueriesCommandBase<P> {
    public GetAvailableClusterVersionsByStoragePoolQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getStoragePoolId() != null) {
            java.util.ArrayList<Version> result = new java.util.ArrayList<Version>();
            storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().get(
                    getParameters().getStoragePoolId().getValue());
            if (storagePool != null) {
                // return all versions that >= to the storage pool version
                for (Version supportedVer : Config
                        .<java.util.HashSet<Version>> GetValue(ConfigValues.SupportedClusterLevels)) {
                    // if version lower than current skip because cannot
                    // decrease version
                    if (supportedVer.compareTo(storagePool.getcompatibility_version()) < 0) {
                        continue;
                    }
                    result.add(supportedVer);
                }
            }
            getQueryReturnValue().setReturnValue(result);
        } else {
            getQueryReturnValue().setReturnValue(
                    new java.util.ArrayList<Version>(Config
                            .<java.util.HashSet<Version>> GetValue(ConfigValues.SupportedClusterLevels)));
        }
    }
}
