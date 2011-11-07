package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmPoolWithVmsCommand<T extends AddVmPoolWithVmsParameters> extends CommonVmPoolWithVmsCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected AddVmPoolWithVmsCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmPoolWithVmsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = super.canDoAction();

        if (returnValue && VmTemplateHandler.BlankVmTemplateId.equals(getParameters().getVmStaticData().getvmt_guid())) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.VM_POOL_CANNOT_CREATE_FROM_BLANK_TEMPLATE);

        }

        if (!returnValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
        }

        return returnValue;
    }

    @Override
    protected Guid GetPoolId() {
        vm_pools vmPool = getVmPool();

        DbFacade.getInstance().getVmPoolDAO().save(vmPool);

        return vmPool.getvm_pool_id();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getAddVmsSucceded() ? AuditLogType.USER_ADD_VM_POOL_WITH_VMS
                : getSucceeded() ? AuditLogType.USER_ADD_VM_POOL_WITH_VMS_ADD_VDS_FAILED
                        : AuditLogType.USER_ADD_VM_POOL_WITH_VMS_FAILED;
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        Map<Guid, VdcObjectType> map = new HashMap<Guid, VdcObjectType>();
        map.put(getParameters().getVmStaticData().getvds_group_id(), VdcObjectType.VdsGroups);
        map.put(getVmTemplateId(), VdcObjectType.VmTemplate);
        return map;
    }
}
