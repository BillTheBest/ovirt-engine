package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.GetAllChildVlanInterfacesQueryParameters;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;

/**
 * This query get vlan parent nic input: eth2.2 return: eth2
 */
public class GetVlanParanetQuery<P extends GetAllChildVlanInterfacesQueryParameters> extends QueriesCommandBase<P> {
    public GetVlanParanetQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (((VdsNetworkInterface) getParameters().getInterface()).getVlanId() != null) {
            List<VdsNetworkInterface> vdsInterfaces = DbFacade.getInstance()
                    .getInterfaceDAO().getAllInterfacesForVds(getParameters().getVdsId());
            for (int i = 0; i < vdsInterfaces.size(); i++) {
                if (StringHelper.EqOp(NetworkUtils.StripVlan(getParameters().getInterface().getName()),
                        vdsInterfaces.get(i).getName())) {
                    getQueryReturnValue().setReturnValue(vdsInterfaces.get(i));
                    break;
                }
            }
        }
    }
}
