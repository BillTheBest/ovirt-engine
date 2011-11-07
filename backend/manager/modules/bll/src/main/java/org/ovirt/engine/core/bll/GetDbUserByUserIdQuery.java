package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetDbUserByUserIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetDbUserByUserIdQuery<P extends GetDbUserByUserIdParameters>
        extends QueriesCommandBase<P> {
    public GetDbUserByUserIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getDbUserDAO()
                        .get((getParameters()).getUserId()));
    }
}
