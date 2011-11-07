package org.ovirt.engine.core.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map_id;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;

public class StorageServerConnectionLunMapDAOTest extends BaseDAOTestCase {
    private static final String FREE_LUN_ID = "1IET_00180002";
    private static final String EXISTING_DOMAIN_STORAGE_NAME = "fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePk";

    private StorageServerConnectionLunMapDAO dao;
    private StorageServerConnectionDAO storageServerConnectionDao;
    private storage_server_connections newServerConnection;
    private storage_server_connections existingConnection;
    private LUN_storage_server_connection_map existingLUNStorageMap;
    private LUN_storage_server_connection_map newLUNStorageMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getStorageServerConnectionLunMapDAO());
        storageServerConnectionDao = prepareDAO(dbFacade.getStorageServerConnectionDAO());

        existingConnection = storageServerConnectionDao.get("0cc146e8-e5ed-482c-8814-270bc48c297f");

        newServerConnection = new storage_server_connections();
        newServerConnection.setid("0cc146e8-e5ed-482c-8814-270bc48c2980");
        newServerConnection.setconnection(EXISTING_DOMAIN_STORAGE_NAME);

        existingLUNStorageMap =
                dao.get(new LUN_storage_server_connection_map_id("1IET_00180001", existingConnection.getid()));
        newLUNStorageMap = new LUN_storage_server_connection_map(FREE_LUN_ID, existingConnection.getid());
    }

    @Test
    public void testGet() {
        LUN_storage_server_connection_map result = dao.get(existingLUNStorageMap.getId());

        assertNotNull(result);
        assertEquals(existingLUNStorageMap, result);
    }

    @Test
    public void testSave() {
        dao.save(newLUNStorageMap);

        LUN_storage_server_connection_map result = dao.get(newLUNStorageMap.getId());

        assertNotNull(result);
        assertEquals(newLUNStorageMap, result);
    }

    @Test
    public void testGetAll() {
        List<LUN_storage_server_connection_map> result =
                dao.getAll(existingLUNStorageMap.getId().lunId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (LUN_storage_server_connection_map mapping : result) {
            assertEquals(existingLUNStorageMap.getId().lunId, mapping.getId().lunId);
        }
    }
}
