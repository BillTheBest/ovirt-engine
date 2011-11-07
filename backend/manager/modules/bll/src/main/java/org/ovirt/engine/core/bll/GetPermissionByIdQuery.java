package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetPermissionByIdQuery<P extends MultilevelAdministrationByPermissionIdParameters>
        extends QueriesCommandBase<P> {
    public GetPermissionByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue()
                .setReturnValue(DbFacade.getInstance().getPermissionDAO().get(getParameters().getPermissionId()));
    }
}
