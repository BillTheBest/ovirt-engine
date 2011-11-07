package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.DisplayNetworkToVdsGroupParameters;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@CustomLogFields({ @CustomLogField("NetworkName") })
public class UpdateDisplayToVdsGroupCommand<T extends DisplayNetworkToVdsGroupParameters> extends
        VdsGroupCommandBase<T> {
    private network_cluster _networkCluster;
    private List<network_cluster> _allNetworkCluster;

    public UpdateDisplayToVdsGroupCommand(T parameters) {
        super(parameters);
    }

    public String getNetworkName() {
        return getParameters().getNetwork().getname();
    }

    @Override
    protected void executeCommand() {
        // network_cluster oldDisplay = null; //LINQ 31899
        // _allNetworkCluster.FirstOrDefault(n => n.is_display);
        network_cluster oldDisplay = LinqUtils.firstOrNull(_allNetworkCluster,
                new Predicate<network_cluster>() {
                    @Override
                    public boolean eval(network_cluster n) {
                        return n.getis_display();
                    }
                });
        if (oldDisplay != null) {
            oldDisplay.setis_display(false);
            DbFacade.getInstance().getNetworkClusterDAO().update(oldDisplay);
        }

        _networkCluster.setis_display(getParameters().getIsDisplay());
        DbFacade.getInstance().getNetworkClusterDAO().update(_networkCluster);

        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        _allNetworkCluster = DbFacade.getInstance().getNetworkClusterDAO().getAllForCluster(
                getParameters().getVdsGroupId());
        // _networkCluster = null; //LINQ 31899
        // _allNetworkCluster.FirstOrDefault(x => x.network_id ==
        // DisplayNetworkToVdsGroupParameter.Network.id);
        _networkCluster = LinqUtils.firstOrNull(_allNetworkCluster,
                new Predicate<network_cluster>() {
                    @Override
                    public boolean eval(network_cluster x) {
                        return x.getnetwork_id().equals(getParameters().getNetwork().getId());
                    }
                });

        return (_networkCluster != null);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_UPDATE_DISPLAY_TO_VDS_GROUP
                : AuditLogType.NETWORK_UPDATE_DISPLAY_TO_VDS_GROUP_FAILED;
    }
}
