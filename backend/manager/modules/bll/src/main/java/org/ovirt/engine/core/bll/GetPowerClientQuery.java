package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetPowerClientByClientInfoParameters;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetPowerClientQuery<P extends GetPowerClientByClientInfoParameters> extends QueriesCommandBase<P> {
    public GetPowerClientQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(GetPowerClient(getParameters().getClientIp()));
    }

    private VDS GetPowerClient(String client_ip) {
        VDS powerClient = null;
        if (!StringHelper.isNullOrEmpty(client_ip)) {
            if (Config.<Boolean> GetValue(ConfigValues.PowerClientLogDetection)) {
                log.infoFormat("Checking if client is a power client. client IP={0}", client_ip);
            }

            List<VDS> targetVDS = DbFacade.getInstance().getVdsDAO().getAllForHostname(client_ip);
            // DbFacade.Instance.GetVdsByHost(client_ip);
            if (targetVDS.size() == 1 && targetVDS.get(0).getvds_type() == VDSType.PowerClient) {
                if (Config.<Boolean> GetValue(ConfigValues.PowerClientLogDetection)) {
                    log.infoFormat("Client is a power client. client IP={0}", client_ip);
                }
                powerClient = targetVDS.get(0); // DbFacade.Instance.GetVdsByVdsId(targetVDS.vds_id);
            }
        }
        return powerClient;
    }

    private static LogCompat log = LogFactoryCompat.getLog(GetPowerClientQuery.class);
}
