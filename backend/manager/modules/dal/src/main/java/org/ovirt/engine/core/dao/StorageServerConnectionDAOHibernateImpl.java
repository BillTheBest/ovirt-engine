package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.compat.Guid;

public class StorageServerConnectionDAOHibernateImpl extends BaseDAOHibernateImpl<storage_server_connections, String> implements StorageServerConnectionDAO {
    private StorageServerConnectionLunMapDAOHibernateImpl mapDAO = new StorageServerConnectionLunMapDAOHibernateImpl();

    public StorageServerConnectionDAOHibernateImpl() {
        super(storage_server_connections.class);
    }

    @Override
    public void setSession(Session session) {
        super.setSession(session);

        mapDAO.setSession(session);
    }

    @Override
    public storage_server_connections getForIqn(String iqn) {
        return findOneByCriteria(Restrictions.eq("iqn", iqn));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_server_connections> getAllForStoragePool(Guid storagePoolId) {
        Query query = getSession().createQuery("select conn from " +
                "LUN_storage_server_connection_map map1, " +
                "LUNs lun, " +
                "storage_domain_static domain, " +
                "storage_server_connections conn, " +
                "storage_pool_iso_map map2, " +
                "storage_pool pool " +
                "where map1.id.lunId = lun.id " +
                "and lun.volumeGroupId = domain.storage " +
                "and map1.id.storageServerConnection = conn.id " +
                "and map2.id.storageId = domain.id " +
                "and pool.id = map2.id.storagePoolId " +
                "and (pool.id = :storage_pool_id)");

        query.setParameter("storage_pool_id", storagePoolId);

        return query.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_server_connections> getAllForVolumeGroup(String group) {
        Query query = getSession().createQuery("select conn from " +
                "LUN_storage_server_connection_map map1, " +
                "LUNs lun, " +
                "storage_domain_static domain, " +
                "storage_server_connections conn " +
                "where map1.id.lunId = lun.id " +
                "and lun.volumeGroupId = domain.storage " +
                "and map1.id.storageServerConnection = conn.id " +
                "and domain.storage = :volume_group_id");

        query.setParameter("volume_group_id", group);

        return query.list();
    }

    @Override
    public List<storage_server_connections> getAllForStorage(String storage) {
        return findByCriteria(Restrictions.eq("connection", storage));
    }

    @Override
    public List<storage_server_connections> getAllForConnection(storage_server_connections connection) {
        return findByCriteria(Restrictions.eq("connection", connection));
    }
    /* TODO - align with Storage Connection Lun Map split
    @Override
    public LUN_storage_server_connection_map getLUNStorageMapForLUNAndStorageServerConnection(String lunId,
            String storageServerConnection) {
        return mapDAO.get(new LUN_storage_server_connection_map_id(lunId, storageServerConnection));
    }

    @Override
    public void addLUNStorageMap(LUN_storage_server_connection_map map) {
        mapDAO.save(map);
    }

    @Override
    public List<LUN_storage_server_connection_map> getAllLUNStorageMapsForLUN(String lunId) {
        return mapDAO.getAllLUNStorageMapsForLun(lunId);
    }
    */
}
