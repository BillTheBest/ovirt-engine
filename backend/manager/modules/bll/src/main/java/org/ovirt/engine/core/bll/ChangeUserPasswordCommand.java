package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapChangeUserPasswordParameters;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeUserPasswordParameters;
import org.ovirt.engine.core.compat.Guid;

public class ChangeUserPasswordCommand<T extends ChangeUserPasswordParameters> extends CommandBase<T> {
    public ChangeUserPasswordCommand(T parameters) {
        super(parameters);
    }

    @Override
    public String getUserName() {
        return getParameters().getUserName();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_PASSWORD_CHANGED : AuditLogType.USER_PASSWORD_CHANGE_FAILED;
    }

    @Override
    protected void executeCommand() {
        setSucceeded(LdapFactory
                .getInstance(getParameters().getDomain())
                .RunAdAction(
                        AdActionType.ChangeUserPassword,
                        new LdapChangeUserPasswordParameters(getParameters().getDomain(), getUserName(), getParameters()
                                .getUserPassword(), getParameters().getNewPassword())).getSucceeded());
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyMap();
    }
}
