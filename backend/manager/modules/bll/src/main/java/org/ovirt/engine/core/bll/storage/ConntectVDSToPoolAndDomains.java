package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class ConntectVDSToPoolAndDomains extends ActivateDeactivateSingleAsyncOperation {

    private static LogCompat log = LogFactoryCompat.getLog(ConntectVDSToPoolAndDomains.class);

    public ConntectVDSToPoolAndDomains(ArrayList<VDS> vdss, storage_domains domain, storage_pool storagePool) {
        super(vdss, domain, storagePool);
    }

    @Override
    public void Execute(int iterationId) {
        VDS vds = getVdss().get(iterationId);
        try {
            boolean isConnectSuccessed =
                    StorageHelperDirector.getInstance().getItem(getStorageDomain().getstorage_type())
                            .ConnectStorageToDomainByVdsId(getStorageDomain(), vds.getvds_id());
            if (isConnectSuccessed) {
                ResourceManager.getInstance().runVdsCommand(
                        VDSCommandType.ConnectStoragePool,
                        new ConnectStoragePoolVDSCommandParameters(vds.getvds_id(), getStoragePool().getId(), vds
                                .getvds_spm_id(), getStorageDomain().getid(), getStoragePool()
                                .getmaster_domain_version()));
            } else {
                log.errorFormat("Failed to connect host {0} to domain {1}",
                        vds.getvds_name(),
                        getStorageDomain().getstorage_name());
            }
        } catch (RuntimeException e) {
            log.errorFormat("Failed to connect host {0} to storage pool {1}. Exception: {3}",
                    vds.getvds_name(),
                    getStoragePool().getname(),
                    e);
        }
    }

}
