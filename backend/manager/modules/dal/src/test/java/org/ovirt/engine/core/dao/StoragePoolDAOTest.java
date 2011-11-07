package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class StoragePoolDAOTest extends BaseDAOTestCase {
    private StoragePoolDAO dao;
    private storage_pool existingPool;
    private Guid vds;
    private Guid vdsGroup;
    private Guid storageDomain;
    private storage_pool newPool;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getStoragePoolDAO());

        existingPool = dao
                .get(new Guid("6d849ebf-755f-4552-ad09-9a090cda105d"));
        existingPool.setstatus(StoragePoolStatus.Up);
        vds = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
        vdsGroup = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
        storageDomain = new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab");

        newPool = new storage_pool();
        newPool.setname("newPoolDude");
        newPool.setcompatibility_version(new Version("3.0"));

    }

    /**
     * Ensures that an invalid id results in a null pool.
     */
    @Test
    public void testGetWithInvalidId() {
        storage_pool result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures the right object is returned by id.
     */
    @Test
    public void testGet() {
        storage_pool result = dao.get(existingPool.getId());

        assertNotNull(result);
        assertEquals(existingPool, result);
    }

    /**
     * Ensures an invalid name returns null.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        storage_pool result = dao.getByName("farkle");

        assertNull(result);
    }

    /**
     * Ensures retrieving by name works as expected.
     */
    @Test
    public void testGetByName() {
        storage_pool result = dao.getByName(existingPool.getname());

        assertNotNull(result);
        assertEquals(existingPool, result);
    }

    /**
     * Ensures the right pool is retrieves for the given VDS.
     */
    @Test
    public void testGetForVds() {
        storage_pool result = dao.getForVds(vds);

        assertNotNull(result);
    }

    /**
     * Ensures the right pool is returned.
     */
    @Test
    public void testGetForVdsGroup() {
        storage_pool result = dao.getForVdsGroup(vdsGroup);

        assertNotNull(result);
    }

    /**
     * Ensures that a collection of pools are returned.
     */
    @Test
    public void testGetAll() {
        List<storage_pool> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that all storage pools for the given domain are returned.
     */
    @Test
    public void testGetAllForStorageDomain() {
        List<storage_pool> result = dao.getAllForStorageDomain(storageDomain);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that an empty collection is returned for a type that's not in the database.
     */
    @Test
    public void testGetAllOfTypeForUnrepresentedType() {
        List<storage_pool> result = dao.getAllOfType(StorageType.UNKNOWN);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that only pools of the given type are returned.
     */
    @Test
    public void testGetAllOfType() {
        List<storage_pool> result = dao.getAllOfType(StorageType.ISCSI);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (storage_pool pool : result) {
            assertEquals(StorageType.ISCSI, pool.getstorage_pool_type());
        }
    }

    @Test
    public void testSave() {
        dao.save(newPool);

        storage_pool result = dao.getByName(newPool.getname());

        assertNotNull(result);
        assertEquals(newPool, result);
    }

    /**
     * Ensures that updating a storage pool works as expected.
     */
    @Test
    public void testUpdate() {
        existingPool.setdescription("Farkle");
        existingPool.setStoragePoolFormatType(StorageFormatType.V1);

        dao.update(existingPool);

        storage_pool result = dao.get(existingPool.getId());

        assertNotNull(result);
        assertEquals(existingPool, result);
    }

    /**
     * Ensures that partial updating a storage pool works as expected.
     */
    @Test
    public void testPartialUpdate() {
        existingPool.setdescription("NewFarkle");

        dao.updatePartial(existingPool);

        storage_pool result = dao.get(existingPool.getId());

        assertNotNull(result);
        assertEquals(existingPool, result);
    }

    /**
     * Ensures that updating a storage pool status works as expected.
     */
    @Test
    public void testUpdateStatus() {
        dao.updateStatus(existingPool.getId(), StoragePoolStatus.NotOperational);
        existingPool.setstatus(StoragePoolStatus.NotOperational);

        storage_pool result = dao.get(existingPool.getId());

        assertNotNull(result);
        assertEquals(existingPool, result);
    }

    /**
     * Ensures that removing a storage pool works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingPool.getId());

        storage_pool result = dao.get(existingPool.getId());

        assertNull(result);
    }
}
