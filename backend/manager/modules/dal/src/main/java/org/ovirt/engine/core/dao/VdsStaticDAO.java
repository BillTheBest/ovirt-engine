package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VdsStaticDAO</code> defines a type that performs CRUD operations on instances of {@link VDS}.
 *
 *
 */
public interface VdsStaticDAO extends GenericDao<VdsStatic, Guid> {
    /**
     * Finds the instance with the specified name.
     *
     * @param name
     *            the name
     * @return the instance
     */
    VdsStatic get(String name);

    /**
     * Retrieves all instances for the given host.
     *
     * @param host
     *            the host
     * @return the list of instances
     */
    List<VdsStatic> getAllForHost(String host);

    /**
     * Finds all instances with the given ip address.
     *
     * @param address
     *            the ip address
     * @return the list of instances
     */
    List<VdsStatic> getAllWithIpAddress(String address);

    /**
     * Retrieves all instances associated with the specified VDS group.
     *
     * @param vdsGroup
     *            the group id
     * @return the list of instances
     */
    List<VdsStatic> getAllForVdsGroup(Guid vdsGroup);
 }
