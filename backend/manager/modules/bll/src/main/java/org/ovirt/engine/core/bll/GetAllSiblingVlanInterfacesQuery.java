package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.GetAllChildVlanInterfacesQueryParameters;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;

/**
 * This query get vlan interface and return all it's siblings, i.e input: eth2.2
 * return: eth2.4 eth2.5 (without eth2.2)
 */
public class GetAllSiblingVlanInterfacesQuery<P extends GetAllChildVlanInterfacesQueryParameters>
        extends QueriesCommandBase<P> {
    public GetAllSiblingVlanInterfacesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        java.util.ArrayList<VdsNetworkInterface> retVal = new java.util.ArrayList<VdsNetworkInterface>();
        if (((VdsNetworkInterface) getParameters().getInterface()).getVlanId() != null) {
            List<VdsNetworkInterface> vdsInterfaces = DbFacade.getInstance()
                    .getInterfaceDAO().getAllInterfacesForVds(getParameters().getVdsId());
            for (int i = 0; i < vdsInterfaces.size(); i++) {
                if (vdsInterfaces.get(i).getVlanId() != null
                        && !StringHelper.EqOp(getParameters().getInterface().getName(), vdsInterfaces.get(i)
                                .getName())) {
                    if (StringHelper.EqOp(NetworkUtils.StripVlan(getParameters().getInterface().getName()),
                            NetworkUtils.StripVlan(vdsInterfaces.get(i).getName()))) {
                        retVal.add(vdsInterfaces.get(i));
                    }
                }
            }
        }
        getQueryReturnValue().setReturnValue(retVal);
    }
}
