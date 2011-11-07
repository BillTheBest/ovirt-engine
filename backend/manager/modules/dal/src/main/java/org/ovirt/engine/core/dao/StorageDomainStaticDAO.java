package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.compat.Guid;

public interface StorageDomainStaticDAO extends GenericDao<storage_domain_static, Guid> {

    /**
     * Retrieves the instance with the specified name.
     *
     * @param name
     *            the domain name
     * @return the domain
     */
    storage_domain_static getByName(String name);

    /**
     * Retrieves all domains of the specified type for the specified pool.
     *
     * @param type
     *            the domain type
     * @param pool
     *            the storage pool
     * @return the list of domains
     */
    List<storage_domain_static> getAllForStoragePoolOfStorageType(
            StorageType type, Guid pool);

    /**
     * Retrieves all storage domains for the given storage pool.
     *
     * @param pool
     *            the pool
     * @return the list of domains
     */
    List<storage_domain_static> getAllForStoragePool(Guid pool);

    /**
     * Retrieves all domains for the given type.
     *
     * @param type
     *            the domain type
     * @return the list of domains
     */
    List<storage_domain_static> getAllOfStorageType(StorageType type);

    /**
     * Return all the domains of the given status which belong to the given pool.
     *
     * @param pool
     *            The pool id.
     * @param status
     *            The desired status.
     * @return The domain ids list (empty if none satisfy the terms).
     */
    List<Guid> getAllIds(Guid pool, StorageDomainStatus status);
}
