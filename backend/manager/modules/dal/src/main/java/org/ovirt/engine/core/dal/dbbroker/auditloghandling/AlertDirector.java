package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.dal.dbbroker.*;

/**
 * AlertDirector
 */
public final class AlertDirector {
    /**
     * Alerts the specified audit logable.
     *
     * @param auditLogable
     *            The audit logable.
     * @param logType
     *            Type of the log.
     */
    public static void Alert(AuditLogableBase auditLogable, AuditLogType logType) {
        AuditLogDirector.log(auditLogable, logType);
    }

    /**
     * Removes the alert.
     *
     * @param vdsId
     *            The VDS id.
     * @param type
     *            The type.
     */
    public static void RemoveVdsAlert(Guid vdsId, AuditLogType type) {
        DbFacade.getInstance().getAuditLogDAO().removeAllOfTypeForVds(vdsId, type.getValue());
    }

    /**
     * Removes all alerts.
     *
     * @param vdsId
     *            The VDS id.
     * @param removeConfigAlerts
     *            if set to <c>true</c> [remove config alerts].
     */
    public static void RemoveAllVdsAlerts(Guid vdsId, boolean removeConfigAlerts) {
        DbFacade.getInstance().getAuditLogDAO().removeAllForVds(vdsId, removeConfigAlerts);
    }
}
