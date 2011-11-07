package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddTagCommand<T extends TagsOperationParameters> extends TagsCommandOperationBase<T> {
    public AddTagCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand()

    {
        DbFacade.getInstance().getTagDAO().save(getTag());
        TagsDirector.getInstance().AddTag(getTag());

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        tags tag = DbFacade.getInstance().getTagDAO()
                .getByName(getParameters().getTag().gettag_name());
        if (tag != null) {
            addCanDoActionMessage(VdcBllMessages.TAGS_SPECIFY_TAG_IS_IN_USE);
            return false;
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_TAG : AuditLogType.USER_ADD_TAG_FAILED;
    }
}
