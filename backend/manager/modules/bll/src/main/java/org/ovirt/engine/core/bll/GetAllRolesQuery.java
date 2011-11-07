package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetAllRolesQuery<P extends MultilevelAdministrationsQueriesParameters> extends QueriesCommandBase<P> {
    public GetAllRolesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance().getRoleDAO().getAll());
    }
}
