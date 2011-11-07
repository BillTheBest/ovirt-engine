package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.GetAllChildVlanInterfacesQueryParameters;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;

/**
 * This query get interface and return all it's interface vlans, i.e input: eth2
 * return: eth2.4 eth2.5
 */
public class GetAllChildVlanInterfacesQuery<P extends GetAllChildVlanInterfacesQueryParameters>
        extends QueriesCommandBase<P> {
    public GetAllChildVlanInterfacesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        java.util.ArrayList<VdsNetworkInterface> retVal = new java.util.ArrayList<VdsNetworkInterface>();
        if (((VdsNetworkInterface) getParameters().getInterface()).getVlanId() == null) {
            List<VdsNetworkInterface> vdsInterfaces = DbFacade.getInstance()
                    .getInterfaceDAO().getAllInterfacesForVds(getParameters().getVdsId());
            for (int i = 0; i < vdsInterfaces.size(); i++) {
                if (vdsInterfaces.get(i).getVlanId() != null) {
                    if (StringHelper.EqOp(getParameters().getInterface().getName(),
                            NetworkUtils.StripVlan(vdsInterfaces.get(i).getName()))) {
                        retVal.add(vdsInterfaces.get(i));
                    }
                }
            }
        }
        getQueryReturnValue().setReturnValue(retVal);
    }
}
