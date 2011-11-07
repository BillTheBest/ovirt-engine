package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * This class implements IsUserAutorizedToRunAction() so only admin users can
 * execute it without explicit permissions given for users, Any command that can
 * be executed by administrators and there are no permission for should extend
 * this class for example - no permissions can be given on tags and only admin
 * users can manipulate tags
 *
 * 'admin user' logic is in MultiLevelAdministrationHandler.isAdminUser mathod
 *
 */
public abstract class AdminOperationCommandBase<T extends VdcActionParametersBase> extends CommandBase<T> {

    private static LogCompat log = LogFactoryCompat.getLog(AdminOperationCommandBase.class);

    protected AdminOperationCommandBase(T parameters) {
        super(parameters);
    }

    protected AdminOperationCommandBase() {
    }

    /**
     * Check if current user is admin according to
     * MultiLevelAdministrationHandler.isAdminUser
     *
     */
    @Override
    protected boolean IsUserAutorizedToRunAction() {
        if (isInternalExecution() || !Config.<Boolean> GetValue(ConfigValues.IsMultilevelAdministrationOn)) {
            if (log.isDebugEnabled()) {
                log.debugFormat(
                        "IsUserAutorizedToRunAction: Internal action or MLA is off - permission check skipped for action {0}",
                        getActionType());
            }
            return true;
        }

        if (getCurrentUser() != null) {
            if (MultiLevelAdministrationHandler.isAdminUser(getCurrentUser().getUserId())) {
                return true;
            }
            addCanDoActionMessage(VdcBllMessages.USER_NOT_AUTHORIZED_TO_PERFORM_ACTION);
            return false;
        } // user not logged in
        else {
            addCanDoActionMessage(VdcBllMessages.USER_IS_NOT_LOGGED_IN);
            return false;
        }
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyMap();
    }

}
