/**
 *
 */
package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetVdsHooksByIdParameters;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.vdshooks.VdsHooksParser;

/**
 * Query for returning VDS hooks by vds ID The returned object is a map of
 * folder/event names to an inner map of script names to an inner map of
 * property names and values
 */
public class GetVdsHooksByIdQuery<P extends GetVdsHooksByIdParameters> extends QueriesCommandBase<P> {

    public GetVdsHooksByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {

        VDS vds = DbFacade.getInstance().getVdsDAO().get(getParameters().getVdsId());
        ValueObjectMap result = new ValueObjectMap();
        if (vds != null) {
            result = VdsHooksParser.parseHooks(vds.getHooksStr());
        }
        getQueryReturnValue().setReturnValue(result);
    }
}
