package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.BookmarksParametersBase;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveBookmarkCommand<T extends BookmarksParametersBase> extends BookmarkCommandBase<T> {
    public RemoveBookmarkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (getBookmark() == null) {
            AddInvalidIdErrorMessages(VdcBllMessages.VAR__ACTION__UPDATE);
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getBookmarkDAO()
                .remove(getBookmark().getbookmark_id());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_BOOKMARK : AuditLogType.USER_REMOVE_BOOKMARK_FAILED;
    }
}
