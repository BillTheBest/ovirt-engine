package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class UpdateTagCommand<T extends TagsOperationParameters> extends TagsCommandOperationBase<T> {
    public UpdateTagCommand(T parameters) {
        super(parameters);

    }

    @Override
    protected void executeCommand() {
        TagsDirector.getInstance().UpdateTag(getTag());
        DbFacade.getInstance().getTagDAO().update(getTag());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        // we fetch by new name to see if it is in use
        tags tag = DbFacade.getInstance().getTagDAO()
                .getByName(getParameters().getTag().gettag_name());
        if (tag != null && !tag.gettag_id().equals(getParameters().getTag().gettag_id())) {
            addCanDoActionMessage(VdcBllMessages.TAGS_SPECIFY_TAG_IS_IN_USE);
            return false;
        }
        // we fetch by id to see if the tag is realy read-only
        tag = DbFacade.getInstance().getTagDAO().get(getParameters().getTag().gettag_id());
        if (tag.getIsReadonly() != null && tag.getIsReadonly()) {
            addCanDoActionMessage(VdcBllMessages.TAGS_CANNOT_EDIT_READONLY_TAG);
            return false;
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_TAG : AuditLogType.USER_UPDATE_TAG_FAILED;
    }
}
