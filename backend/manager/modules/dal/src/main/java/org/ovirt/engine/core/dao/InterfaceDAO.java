package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>InterfaceDAO</code> defines a type for performing CRUD operations on instances of {@link Interface}.
 *
 *
 */
public interface InterfaceDAO extends DAO {
    /**
     * Saves the specified statistics
     *
     * @param stats
     *            the statistics
     */
    void saveStatisticsForVds(VdsNetworkStatistics stats);

    /**
     * Saves the specified VDS interface.
     *
     * @param iface
     *            the interface
     */
    void saveInterfaceForVds(VdsNetworkInterface iface);

    /**
     * Updates the statistics.
     *
     * @param stats
     *            the statistics
     */
    void updateStatisticsForVds(VdsNetworkStatistics stats);

    /**
     * Updates the given collection of vds network statistics using a more efficient method to update all of them at
     * once, rather than each at a time.
     *
     * @param statistics
     *            The collection of statistics to update.
     */
    void massUpdateStatisticsForVds(Collection<VdsNetworkStatistics> statistics);

    /**
     * Updates the specified VDS interface.
     *
     * @param iface
     *            the interface
     */
    void updateInterfaceForVds(VdsNetworkInterface iface);

    /**
     * Retrieves all interfaces for the given VDS id.
     *
     * @param id
     *            the VDS id
     * @return the list of interfaces
     */
    List<VdsNetworkInterface> getAllInterfacesForVds(Guid id);

    /**
     * Removes the specified statistics.
     *
     * @param id
     *            the statistics
     */
    void removeStatisticsForVds(Guid id);

    /**
     * Removes the VDS interface.
     *
     * @param id
     *            the interface
     */
    void removeInterfaceFromVds(Guid id);
}
