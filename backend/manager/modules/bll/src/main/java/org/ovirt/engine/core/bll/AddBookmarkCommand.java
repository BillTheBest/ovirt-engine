package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddBookmarkCommand<T extends BookmarksOperationParameters> extends BookmarkOperationCommand<T> {
    public AddBookmarkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (DbFacade.getInstance().getBookmarkDAO()
                .getByName(getBookmark().getbookmark_name()) != null) {
            AddErrorMessages(
                    VdcBllMessages.VAR__ACTION__ADD,
                    VdcBllMessages.ACTION_TYPE_FAILED_BOOKMARK_NAME_ALREADY_EXISTS);
            return false;
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getBookmarkDAO().save(getBookmark());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_BOOKMARK : AuditLogType.USER_ADD_BOOKMARK_FAILED;
    }
}
