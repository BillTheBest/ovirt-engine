package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveTagCommand<T extends TagsActionParametersBase> extends TagsCommandBase<T> {
    public RemoveTagCommand(T parameters) {
        super(parameters);

    }

    @Override
    protected void executeCommand() {
        if (getTagId() != null) {
            String tagIdAndChildrenIds = TagsDirector.getInstance().GetTagIdAndChildrenIds(getTagId());
            TagsDirector.getInstance().RemoveTag(getTag().gettag_id());
            String[] IDsArray = tagIdAndChildrenIds.split("[,]", -1);
            for (String id : IDsArray) {
                id = id.replace("'", "");
                DbFacade.getInstance().getTagDAO().remove(new Guid(id));
            }
            setSucceeded(true);
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        if (getTagId() == null || DbFacade.getInstance().getTagDAO().get(getTagId()) == null) {
            addCanDoActionMessage(VdcBllMessages.TAGS_CANNOT_REMOVE_TAG_NOT_EXIST);
            returnValue = false;
        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_TAG : AuditLogType.USER_REMOVE_TAG_FAILED;
    }
}
