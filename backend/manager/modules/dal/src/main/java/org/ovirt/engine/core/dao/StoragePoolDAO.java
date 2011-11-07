package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>StoragePoolDAO</code> defines a type for performing CRUD operations on instances of {@link storage_pool}.
 *
 *
 */
public interface StoragePoolDAO  extends GenericDao<storage_pool, Guid>, StatusAwareDao<Guid, StoragePoolStatus> {

    /**
     * Retrieves the storage pool with the given name.
     *
     * @param name
     *            the storage pool name
     * @return the storage pool
     */
    storage_pool getByName(String name);

    /**
     * Retrieves the storage pool for the specified VDS.
     *
     * @param vds
     *            the VDS
     * @return the storage pool
     */
    storage_pool getForVds(Guid vds);

    /**
     * Retrieves the storage pool for the specified VDS group.
     *
     * @param vdsGroup
     *            the VDS group
     * @return the storage pool
     */
    storage_pool getForVdsGroup(Guid vdsGroup);

    /**
     * Retrieves the list of all storage pools of a given type.
     *
     * @param type
     *            the storage pool type
     * @return the list of storage pool
     */
    List<storage_pool> getAllOfType(StorageType type);

    /**
     * Retrieves all storage pools for the given storage domain.
     *
     * @param storageDomain
     *            the storage domain
     * @return the list of storage pools
     */
    List<storage_pool> getAllForStorageDomain(Guid storageDomain);

    /**
     * Retrieves all storage pools that satisfy the given SQL query.
     *
     * @param query
     *            the query
     * @return the list of storage pools
     */
    List<storage_pool> getAllWithQuery(String query);

    /**
     * The following method should update only part of storage pool. It will update only name, description and
     * compatibility version fields
     * @param pool
     */
    void updatePartial(storage_pool pool);

    /**
     * Retrieves data centers containing clusters with permissions to perform the given action.
     *
     * @param userId
     * @param actionGroup
     * @return list of data centers
     */
    List<storage_pool> getDataCentersWithPermittedActionOnClusters(Guid userId, ActionGroup actionGroup);

}
