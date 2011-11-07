package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VdsGroupDAO</code> defines a type that performs CRUD operations on instances of {@link VDSGroup}.
 *
 */
public interface VdsGroupDAO extends DAO {
    /**
     * Gets the group with the specified id.
     *
     * @param id
     *            the group id
     * @return the group
     */
    VDSGroup get(Guid id);

    /**
     * Returns the specified VDS group if it has running VMs.
     *
     * @param id
     *            the group id
     * @return the VDS group
     */
    VDSGroup getWithRunningVms(Guid id);

    /**
     * Retrieves the group with the specified name.
     *
     * @param name
     *            the group name
     * @return the group
     */
    VDSGroup getByName(String name);

    /**
     * Retrieves the list of groups associated with the given storage pool.
     *
     * @param id
     *            the storage pool
     * @return the list of groups
     */
    List<VDSGroup> getAllForStoragePool(Guid id);

    /**
     * Retrieves groups using a SQL query snippet.
     *
     * @param query
     *            the query
     * @return the list of groups
     */
    List<VDSGroup> getAllWithQuery(String query);

    /**
     * Retrieves all VDS groups.
     *
     * @return the list of groups
     */
    List<VDSGroup> getAll();

    /**
     * Saves the supplied group.
     *
     * @param group
     *            the group
     */
    void save(VDSGroup group);

    /**
     * Updates the group.
     *
     * @param group
     *            the group
     */
    void update(VDSGroup group);

    /**
     * Removes the VDS group with the given id.
     *
     * @param id
     *            the group id
     */
    void remove(Guid id);

    /**
     * Retries clusters which the given users has permission to perform the given action.
     *
     * @param userId
     * @param actionGroup
     * @return list of clusters
     */
    List<VDSGroup> getClustersWithPermittedAction(Guid userId, ActionGroup actionGroup);
}
