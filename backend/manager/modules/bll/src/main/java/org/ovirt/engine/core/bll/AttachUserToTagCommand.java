package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.tags_user_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AttachUserToTagCommand<T extends AttachEntityToTagParameters> extends UserTagMapBase<T> {
    public AttachUserToTagCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        tags_user_map map;
        if (getTagId() != null) {
            for (Guid userGuid : getUserList()) {
                DbUser user = DbFacade.getInstance().getDbUserDAO().get(userGuid);
                if (DbFacade.getInstance().getTagDAO().getTagUserByTagIdAndByuserId(getTagId(), userGuid) == null) {
                    map = new tags_user_map(getTagId(), userGuid);
                    DbFacade.getInstance().getTagDAO().attachUserToTag(map);
                    noActionDone = false;
                    if (user != null) {
                        AppendCustomValue("AttachUsersNames", user.getusername(), ", ");
                    }
                } else {
                    if (user != null) {
                        AppendCustomValue("AttachUsersNamesExists", user.getusername(), ", ");
                    }
                }
            }
            setSucceeded(true);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (noActionDone) {
            return AuditLogType.USER_ATTACH_TAG_TO_USER_EXISTS;
        }
        return getSucceeded() ? AuditLogType.USER_ATTACH_TAG_TO_USER : AuditLogType.USER_ATTACH_TAG_TO_USER_FAILED;
    }
}
