package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.tags_vm_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AttachVmsToTagCommand<T extends AttachEntityToTagParameters> extends VmsTagMapBase<T> {

    public AttachVmsToTagCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        if (getTagId() != null) {
            for (Guid vmGuid : getVmsList()) {
                VM vm = DbFacade.getInstance().getVmDAO().getById(vmGuid);
                if (DbFacade.getInstance().getTagDAO().getTagVmByTagIdAndByVmId(getTagId(), vmGuid) == null) {
                    if (vm != null) {
                        AppendCustomValue("VmsNames", vm.getvm_name(), ", ");
                    }
                    tags_vm_map map = new tags_vm_map(getTagId(), vmGuid);
                    DbFacade.getInstance().getTagDAO().attachVmToTag(map);
                    noActionDone = false;
                } else {
                    if (vm != null) {
                        AppendCustomValue("VmsNamesExists", vm.getvm_name(), ", ");
                    }
                }
            }
        }
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (noActionDone) {
            return AuditLogType.USER_ATTACH_TAG_TO_VM_EXISTS;
        }
        return getSucceeded() ? AuditLogType.USER_ATTACH_TAG_TO_VM : AuditLogType.USER_ATTACH_TAG_TO_VM_FAILED;
    }
}
