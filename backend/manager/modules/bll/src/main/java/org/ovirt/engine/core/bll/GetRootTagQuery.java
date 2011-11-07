package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

public class GetRootTagQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetRootTagQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(TagsDirector.getInstance().GetRootTag());
    }
}
