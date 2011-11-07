package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.compat.Guid;

public class RoleDAOHibernateImpl extends BaseDAOHibernateImpl<roles, Guid> implements RoleDAO {
    public RoleDAOHibernateImpl() {
        super(roles.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<roles> getAllForAdElement(Guid id) {
        Guid[] ids = getUserAndGroupIdsForUser(id);

        if (ids.length == 0) {
            return new ArrayList<roles>();
        }

        Query query = getSession().createQuery("from roles role, permissions perms " +
                "where perms.roleId = role.id " +
                "and perms.adElementId in ( :ids )");

        query.setParameterList("ids", ids);

        return query.list();
    }

    /**
     * Retrieves the IDs for all groups for the supplied user.
     *
     * @param userId
     *            the user
     * @return a collection containing the ID of the user and all of his groups
     */
    private Guid[] getUserAndGroupIdsForUser(Guid userId) {
        Query query = getSession().createQuery("from DbUser where id = :id");

        query.setParameter("id", userId);

        DbUser user = (DbUser) query.uniqueResult();

        if (user == null) {
            return new Guid[0];
        }

        query = getSession().createQuery("from ad_groups where name in (:names)");
        query.setParameterList("names", user.getGroupsAsArray());

        @SuppressWarnings("unchecked")
        List<ad_groups> groups = query.list();

        Guid[] result = new Guid[groups.size() + 1];

        result[0] = userId;
        for (int index = 0; index < groups.size(); index++) {
            result[index + 1] = groups.get(index).getid();
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<roles> getForAdElement(Guid id) {
        Query query = getSession().createQuery("from roles role, permissions perms " +
                "where perms.roleId = role.id " +
                "and perms.adElementId = :id");

        query.setParameter("id", id);

        return query.list();
    }
}
