package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.GetSystemStatisticsQueryParameters;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetSystemStatisticsQuery<P extends GetSystemStatisticsQueryParameters> extends QueriesCommandBase<P> {
    public GetSystemStatisticsQuery(P parameters) {
        super(parameters);
    }

    public java.util.HashMap<String, Integer> getSystemStatistics() {
        //
        // int max = (Parameters as GetSystemStatisticsQueryParameters).Max;
        // Dictionary<String, Int32> res = new Dictionary<String, Int32>();
        // QueriesCommandBase query;
        // List<IVdcQueryable> tmp;
        // query = CommandsFactory.CreateQueryCommand(
        // VdcQueryType.Search, new SearchParameters("vms:",
        // SearchType.VM){MaxCount = max});
        // query.Execute();
        // tmp = (List<IVdcQueryable>)query.QueryReturnValue.ReturnValue;
        // int total_vms = tmp.Count;
        // query = CommandsFactory.CreateQueryCommand(
        // VdcQueryType.Search, new SearchParameters("vms: status != down",
        // SearchType.VM) { MaxCount = max });
        // query.Execute();
        // tmp = (List<IVdcQueryable>)query.QueryReturnValue.ReturnValue;
        // int active_vms = tmp.Count;
        // List<DbUser> users = DbFacade.Instance.GetAllFromUsers();
        // int total_users = users.Count, active_users = 0;
        // foreach (DbUser user in users)
        // {
        // if (user.IsLogedin)
        // {
        // active_users++;
        // }
        // }
        // query = CommandsFactory.CreateQueryCommand(
        // VdcQueryType.Search, new SearchParameters("Hosts", SearchType.VDS) {
        // MaxCount = max });
        // query.Execute();
        // tmp = (List<IVdcQueryable>)query.QueryReturnValue.ReturnValue;
        // int total_vds = tmp.Count, active_vds = 0;
        // foreach (VDS vds in tmp)
        // {
        // if ((vds.status == VDSStatus.Up) || (vds.status ==
        // VDSStatus.PreparingForMaintenance))
        // {
        // active_vds++;
        // }
        // }
        //

        java.util.HashMap<String, Integer> res = new java.util.HashMap<String, Integer>();

        // VMs:
        int total_vms = DbFacade.getInstance().GetSystemStatisticsValue("VM", "");
        String[] activeVmStatuses = { (new Integer(VMStatus.Up.getValue())).toString(),
                (new Integer(VMStatus.PoweringUp.getValue())).toString(),
                (new Integer(VMStatus.PoweredDown.getValue())).toString(),
                (new Integer(VMStatus.MigratingTo.getValue())).toString(),
                (new Integer(VMStatus.WaitForLaunch.getValue())).toString(),
                (new Integer(VMStatus.RebootInProgress.getValue())).toString(),
                (new Integer(VMStatus.PoweringDown.getValue())).toString(),
                (new Integer(VMStatus.Paused.getValue())).toString(),
                (new Integer(VMStatus.Unknown.getValue())).toString() };
        int active_vms = DbFacade.getInstance()
                .GetSystemStatisticsValue("VM", StringHelper.join(",", activeVmStatuses));

        int down_vms = (total_vms - active_vms) < 0 ? 0 : (total_vms - active_vms);

        // Hosts:
        int total_vds = DbFacade.getInstance().GetSystemStatisticsValue("HOST", "");

        String[] activeVdsStatuses = { (new Integer(VDSStatus.Up.getValue())).toString(),
                (new Integer(VDSStatus.PreparingForMaintenance.getValue())).toString() };
        int active_vds = DbFacade.getInstance().GetSystemStatisticsValue("HOST",
                StringHelper.join(",", activeVdsStatuses));
        int maintenance_vds = DbFacade.getInstance().GetSystemStatisticsValue("HOST",
                (new Integer(VDSStatus.Maintenance.getValue())).toString());
        int down_vds = (total_vds - active_vds - maintenance_vds) < 0 ? 0 : (total_vds - active_vds - maintenance_vds);

        // Users:
        int total_users = DbFacade.getInstance().GetSystemStatisticsValue("USER", "");
        int active_users = DbFacade.getInstance().GetSystemStatisticsValue("USER", "1");

        // Storage Domains:
        int total_storage_domains = DbFacade.getInstance().GetSystemStatisticsValue("TSD", "");
        int active_storage_domains = DbFacade.getInstance().GetSystemStatisticsValue("ASD",
                (Integer.toString(StorageDomainStatus.Active.getValue())));

        res.put("total_vms", total_vms);
        res.put("active_vms", active_vms);
        res.put("down_vms", down_vms);
        res.put("total_vds", total_vds);
        res.put("active_vds", active_vds);
        res.put("maintenance_vds", maintenance_vds);
        res.put("down_vds", down_vds);
        res.put("total_users", total_users);
        res.put("active_users", active_users);
        res.put("total_storage_domains", total_storage_domains);
        res.put("active_storage_domains", active_storage_domains);

        return res;
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getSystemStatistics());
    }
}
