package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.Pair;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.INFO)
public class GetExistingStorageDomainListQuery<P extends GetExistingStorageDomainListParameters>
        extends QueriesCommandBase<P> {
    public GetExistingStorageDomainListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        java.util.ArrayList<storage_domains> returnValue = new java.util.ArrayList<storage_domains>();
        VDSReturnValue vdsReturnValue = Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.HSMGetStorageDomainsList,
                        new HSMGetStorageDomainsListVDSCommandParameters(getParameters()
                                .getVdsId(), Guid.Empty, getParameters()
                                .getStorageType(), getParameters()
                                .getStorageDomainType(), getParameters().getPath()));
        if (vdsReturnValue.getSucceeded()) {
            java.util.ArrayList<Guid> guidsFromIrs = (java.util.ArrayList<Guid>) vdsReturnValue.getReturnValue();
            java.util.HashSet<Guid> guidsFromDb = new java.util.HashSet<Guid>();
            if (guidsFromIrs.size() > 0) {
                List<storage_domains> domainsInDb = DbFacade.getInstance().getStorageDomainDAO().getAll();
                for (storage_domains domain : domainsInDb) {
                    guidsFromDb.add(domain.getid());
                }
                for (Guid domainId : guidsFromIrs) {
                    if (!guidsFromDb.contains(domainId)) {
                        Pair<storage_domain_static, SANState> domainFromIrs =
                                (Pair<storage_domain_static, SANState>) Backend
                                        .getInstance()
                                        .getResourceManager()
                                        .RunVdsCommand(
                                                VDSCommandType.HSMGetStorageDomainInfo,
                                                new HSMGetStorageDomainInfoVDSCommandParameters(
                                                        getParameters().getVdsId(), domainId))
                                        .getReturnValue();
                        storage_domains domain = new storage_domains();
                        domain.setStorageStaticData(domainFromIrs.getFirst());
                        if (getParameters().getStorageFormatType() == null
                                || getParameters().getStorageFormatType() == domain.getStorageFormat()) {
                            returnValue.add(domain);
                        }
                    }
                }
            }
            getQueryReturnValue().setReturnValue(returnValue);
        }
    }
}
