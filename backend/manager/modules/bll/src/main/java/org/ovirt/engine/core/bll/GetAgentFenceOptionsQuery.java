package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.queries.*;

public class GetAgentFenceOptionsQuery<P extends VdcQueryParametersBase> extends FencingQueryBase<P> {

    public GetAgentFenceOptionsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VdsFencingOptions options = new VdsFencingOptions();
        ValueObjectMap map = options.getFencingOptionMappingMap();
        getQueryReturnValue().setReturnValue(map);
        getQueryReturnValue().setSucceeded(map.asMap().size() > 0);
    }
}
