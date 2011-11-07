package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmPoolSimpleUserParameters;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemovePermissionCommand<T extends PermissionsOperationsParametes> extends PermissionsCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RemovePermissionCommand(Guid commandId) {
        super(commandId);
    }

    public RemovePermissionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue=true;
        permissions p = DbFacade.getInstance().getPermissionDAO().get(getParameters().getPermission().getId());
        if (p.getad_element_id().equals(PredefinedUsers.ADMIN_USER.getId()) &&
           (p.getrole_id().equals(PredefinedRoles.SUPER_USER.getId()))) {
            addCanDoActionMessage(VdcBllMessages.USER_CANNOT_REMOVE_ADMIN_USER);
            returnValue = false;
        }
        if(MultiLevelAdministrationHandler.isLastSuperUserPermission(p.getrole_id())) {
           getReturnValue().getCanDoActionMessages().add(VdcBllMessages.ERROR_CANNOT_REMOVE_LAST_SUPER_USER_ROLE.toString());;
           returnValue=false;
        }
        if (returnValue && p.getRoleType().equals(RoleType.ADMIN) && !isSystemSuperUser()) {
            addCanDoActionMessage(VdcBllMessages.PERMISSION_REMOVE_FAILED_ONLY_SYSTEM_SUPER_USER_CAN_REMOVE_ADMIN_ROLES);
            returnValue = false;
        }
        return returnValue;
    }

    @Override
    protected void executeCommand() {
        permissions perms = getParameters().getPermission();
        Guid userId = perms.getad_element_id();

        // if removing engine user permission from vm,
        // check if vm is from pool and detach it
        if (perms.getObjectType().equals(VdcObjectType.VM)
                && perms.getrole_id().equals(PredefinedRoles.ENGINE_USER.getId())) {
            VM vm = DbFacade.getInstance().getVmDAO().get(perms.getObjectId());
            if (vm != null && vm.getVmPoolId() != null) {
                Backend.getInstance().runInternalAction(VdcActionType.DetachUserFromVmFromPool,
                        new VmPoolSimpleUserParameters(vm.getVmPoolId(), userId));
            }
        }

        DbFacade.getInstance().getPermissionDAO().remove(perms.getId());
        DbFacade.getInstance().updateLastAdminCheckStatus(userId);
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_PERMISSION : AuditLogType.USER_REMOVE_PERMISSION_FAILED;
    }
}
