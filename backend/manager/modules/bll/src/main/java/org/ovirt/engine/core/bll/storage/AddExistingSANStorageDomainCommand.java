package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.action.AddSANStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.Pair;

public class AddExistingSANStorageDomainCommand<T extends AddSANStorageDomainParameters> extends
        AddSANStorageDomainCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddExistingSANStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddExistingSANStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        AddStorageDomainInDb();
        ProceedVGLunsInDb();
        UpdateStorageDomainDynamicFromIrs();
        setSucceeded(true);
    }

    @Override
    protected boolean CanAddDomain() {
        return CheckExistingStorageDomain();
    }

    @Override
    protected boolean ConcreteCheckExistingStorageDomain(Pair<storage_domain_static, SANState> domainFromIrs) {
        boolean returnValue = false;
        if (!StringHelper.isNullOrEmpty(getStorageDomain().getStorageStaticData().getstorage())
                && !StringHelper.isNullOrEmpty(domainFromIrs.getFirst().getstorage())) {
            returnValue =
                    (StringHelper.EqOp(domainFromIrs.getFirst().getstorage(), getStorageDomain().getStorageStaticData()
                            .getstorage()));
        }
        if (!returnValue) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ADD_EXISTING_STORAGE_DOMAIN_CONNECTION_DATA_ILLEGAL);
        } else if (domainFromIrs.getSecond() != null && SANState.OK != domainFromIrs.getSecond()) {
            returnValue = false;
            getReturnValue().getCanDoActionMessages().add(
                    VdcBllMessages.ERROR_CANNOT_ADD_EXISTING_STORAGE_DOMAIN_LUNS_PROBLEM.toString());
        }

        return returnValue;
    }
}
