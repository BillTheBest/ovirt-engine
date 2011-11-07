package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.AddSANStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.vdscommands.CreateVGVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetVGInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class AddSANStorageDomainCommand<T extends AddSANStorageDomainParameters> extends AddStorageDomainCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddSANStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    public AddSANStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        InitializeStorageDomain();
        // save storage if got from parameters in order to save first empty
        // storage in db and use it later
        String storage = ((getStorageDomain().getstorage()) != null) ? getStorageDomain().getstorage() : "";
        // set domain storage to empty because not nullable in db and for shared
        // status to be locked
        getStorageDomain().setstorage("");
        AddStorageDomainInDb();
        getStorageDomain().setstorage(storage);
        if (StringHelper.isNullOrEmpty(getStorageDomain().getstorage())) {
            getStorageDomain().setstorage(CreateVG());
        }
        if (!StringHelper.isNullOrEmpty(getStorageDomain().getstorage()) && (AddStorageDomainInIrs())) {
            DbFacade.getInstance().getStorageDomainStaticDAO().update(getStorageDomain().getStorageStaticData());
            UpdateStorageDomainDynamicFromIrs();
            ProceedVGLunsInDb();
            setSucceeded(true);
        }
    }

    protected void ProceedVGLunsInDb() {
        final java.util.ArrayList<LUNs> luns = (java.util.ArrayList<LUNs>) Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.GetVGInfo,
                        new GetVGInfoVDSCommandParameters(getVds().getvds_id(), getStorageDomain().getstorage()))
                .getReturnValue();

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                for (LUNs lun : luns) {
                    ProceedLUNInDb(lun);
                }
                return null;
            }
        });

    }

    private String CreateVG() {
        VDSReturnValue returnValue = Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.CreateVG,
                        new CreateVGVDSCommandParameters(getVds().getvds_id(), getStorageDomain().getid(),
                                getParameters().getLunIds()));
        String volumeGroupId = (String) ((returnValue.getReturnValue() instanceof String) ? returnValue
                .getReturnValue() : null);
        return volumeGroupId;
    }

    @Override
    protected boolean CanAddDomain() {
        boolean returnValue = true;
        // !AddSANStorageDomainParametersValue.IsExistingStorageDomain &&
        if (((getParameters().getLunIds() == null || getParameters().getLunIds().isEmpty()) && StringHelper
                .isNullOrEmpty(getStorageDomain().getstorage()))) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_CREATE_STORAGE_DOMAIN_WITHOUT_VG_LV);
        }
        return returnValue;
    }
}
