package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class DetachVmFromTagCommand<T extends AttachEntityToTagParameters> extends VmsTagMapBase<T> {

    public DetachVmFromTagCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        for (Guid vmGuid : getVmsList()) {
            if (getTagId() != null && DbFacade.getInstance().getTagDAO().getTagVmByTagIdAndByVmId(getTagId(), vmGuid) != null) {
                VM vm = DbFacade.getInstance().getVmDAO().getById(vmGuid);
                if (vm != null) {
                    AppendCustomValue("VmsNames", vm.getvm_name(), ", ");
                }
                DbFacade.getInstance().getTagDAO().detachVmFromTag(getTagId(), vmGuid);
                setSucceeded(true);
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DETACH_VM_FROM_TAG : AuditLogType.USER_DETACH_VM_FROM_TAG_FAILED;
    }
}
