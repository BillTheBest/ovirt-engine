package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>AuditLogDAO</code> defines a type for performing CRUD operations on instances of {@link AuditLog}.
 *
 *
 */
public interface AuditLogDAO extends DAO {
    /**
     * Retrieves the entry with the given id.
     *
     * @param id
     *            the entry id
     * @return the entry
     */
    AuditLog get(long id);

    /**
     * Finds all entries created after the specified cutoff date
     *
     * @param cutoff
     *            the cutoff date
     * @return the list of entries
     */
    List<AuditLog> getAllAfterDate(Date cutoff);

    /**
     * Finds all entries using a supplied SQL query.
     *
     * @param query
     *            the query
     * @return the list of entries
     */
    List<AuditLog> getAllWithQuery(String query);

    /**
     * Retrieves all audit log entries.
     *
     * @return the list of entries
     */
    List<AuditLog> getAll();

    /**
     * Saves the provided audit log
     *
     * @param entry
     *            the entry
     */
    void save(AuditLog entry);

    /**
     * Updates the provided audit log entry.
     *
     * @param entry
     *            the entry
     */
    void update(AuditLog entry);

    /**
     * Removes the entry with the given id.
     *
     * @param id
     *            the entry id
     */
    void remove(long id);

    /**
     * Removes all entries before the specified cutoff date
     *
     * @param cutoff
     *            the cutoff date
     */
    void removeAllBeforeDate(Date cutoff);

    /**
     * Removes all entries for the given VDS id.
     *
     * @param id
     *            the vds id
     * @param configAlerts
     *            if <code>true</code> then include config alerts
     */
    void removeAllForVds(Guid id, boolean configAlerts);

    /**
     * Removes entries of the specified type for the given VDS id.
     *
     * @param id
     *            the VDS id
     * @param type
     *            the entry type
     */
    void removeAllOfTypeForVds(Guid id, int type);
    /**
     * Get time to wait in seconds before another PM operation is allowed on the given Host
     * @param vdsName Host name
     * @param event [USER_VDS_STOP | USER_VDS_START | USER_VDS_RESTART]
     * @return number of seconds (0 or negative value if we can perform operation immediately)
     */
    public int getTimeToWaitForNextPmOp(String vdsName, String event);
}
