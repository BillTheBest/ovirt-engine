package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAllEventSubscribersQuery<P extends VdcQueryParametersBase>
        extends QueriesCommandBase<P> {
    public GetAllEventSubscribersQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getEventDAO().getAll());
    }
}
