package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.action_version_map;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>ActionGroupDAO</code> defines a type for performing CRUD operations on instances of {@link ActionGroup}.
 *
 *
 */
public interface ActionGroupDAO extends DAO {
    /**
     * Retrieves all action groups for the specified role.
     *
     * @param id
     *            the role id
     * @return the list of action groups
     */
    List<ActionGroup> getAllForRole(Guid id);

    // TODO APIs to be removed when Hibernate migration is completed

    action_version_map getActionVersionMapByActionType(VdcActionType action_type);

    void addActionVersionMap(action_version_map action_version_map);

    void removeActionVersionMap(VdcActionType action_type);
}
