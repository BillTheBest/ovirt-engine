package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.queries.NetworkNonOperationalQueryParamenters;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetNonOperationalVdsQuery<P extends NetworkNonOperationalQueryParamenters> extends QueriesCommandBase<P> {
    public GetNonOperationalVdsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        java.util.ArrayList<VdsStatic> retVal = new java.util.ArrayList<VdsStatic>();
        List<VdsStatic> vdsList = DbFacade.getInstance().getVdsStaticDAO().getAllForVdsGroup(
                getParameters().getVdsGroupId());

        for (VdsStatic vds : vdsList) {
            List<VdsNetworkInterface> interfaces = DbFacade.getInstance()
                    .getInterfaceDAO().getAllInterfacesForVds(vds.getId());
            // if (true) //LINQ 31899 interfaces.FirstOrDefault(i =>
            // i.network_name == NetworkParamaters.Network.name) == null)
            if (LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
                @Override
                public boolean eval(VdsNetworkInterface i) {
                    return StringHelper.EqOp(i.getNetworkName(), getParameters().getNetwork().getname());
                }
            }) == null) {
                retVal.add(vds);
            }
        }

        getQueryReturnValue().setReturnValue(retVal);
    }
}
