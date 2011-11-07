package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainStaticDAOTest extends BaseDAOTestCase {
    private static final Guid EXISTING_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");

    private StorageDomainStaticDAO dao;
    private StorageDomainDynamicDAO dynamicDao;
    private DiskImageDAO imageDao;
    private storage_domain_static existingDomain;
    private storage_domain_static newStaticDomain;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getStorageDomainStaticDAO());
        dynamicDao = prepareDAO(dbFacade.getStorageDomainDynamicDAO());
        imageDao = prepareDAO(dbFacade.getDiskImageDAO());
        existingDomain = dao.get(new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab"));

        newStaticDomain = new storage_domain_static();
        newStaticDomain.setstorage_name("NewStorageDomain");
        newStaticDomain.setstorage("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePl");
    }

    /**
     * Ensures that null is returned when the id is invalid.
     */
    @Test
    public void testGetWithInvalidId() {
        storage_domain_static result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that retrieving the static domain works as expected.
     */
    @Test
    public void testGet() {
        storage_domain_static result = dao.get(existingDomain.getId());

        assertNotNull(result);
        assertEquals(existingDomain.getId(), result.getId());
    }

    /**
     * Ensures that get all is not implemented.
     */
    @Test
    public void testGetAll() {
        List<storage_domain_static> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that null is returned when the name is invalid.
     */
    @Test
    public void testGetByNameWithInvalidName() {
        storage_domain_static result = dao.getByName("farkle");

        assertNull(result);
    }

    /**
     * Ensures the right instance is returned.
     */
    @Test
    public void testGetByName() {
        storage_domain_static result = dao.getByName(existingDomain
                .getstorage_name());

        assertNotNull(result);
        assertEquals(existingDomain.getId(), result.getId());
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllForStoragePoolWithInvalidPool() {
        List<storage_domain_static> result = dao
                .getAllForStoragePool(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right collection of domains are returned.
     */
    @Test
    public void testGetAllForStoragePool() {
        List<storage_domain_static> result = dao.getAllForStoragePool(EXISTING_POOL_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures the right set is returned.
     */
    @Test
    public void testGetAllForStoragePoolOfStorageType() {
        List<storage_domain_static> result = dao.getAllForStoragePoolOfStorageType(StorageType.ISCSI, EXISTING_POOL_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (storage_domain_static domain : result) {
            assertEquals(StorageType.ISCSI, domain.getstorage_type());
        }
    }

    /**
     * Ensures that an empty collection is returned when no static domains of the specified type exist.
     */
    @Test
    public void testGetAllOfStorageTypeWithInvalidType() {
        List<storage_domain_static> result = dao.getAllOfStorageType(StorageType.FCP);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right collection of domains is returned.
     */
    @Test
    public void testGetAllOfStorageType() {
        List<storage_domain_static> result = dao
                .getAllOfStorageType(StorageType.ISCSI);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (storage_domain_static domain : result) {
            assertEquals(StorageType.ISCSI, domain.getstorage_type());
        }
    }

    @Test
    public void testGetAllIdsForNonExistingStoragePoolId() throws Exception {
        List<Guid> result = dao.getAllIds(Guid.NewGuid(), StorageDomainStatus.Active);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllIdsForNonExistingStatus() throws Exception {
        List<Guid> result = dao.getAllIds(EXISTING_POOL_ID, StorageDomainStatus.Unknown);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllIds() throws Exception {
        List<Guid> result = dao.getAllIds(EXISTING_POOL_ID, StorageDomainStatus.Active);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (Guid id : result) {
            assertTrue(!Guid.Empty.equals(id));
        }
    }

    /**
     * Ensures that saving a domain works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newStaticDomain);

        storage_domain_static result = dao.get(newStaticDomain.getId());

        assertNotNull(result);
    }

    /**
     * Ensures that updating the static and dynamic portions works as expected.
     */
    @Test
    public void testUpdate() {
        existingDomain.setstorage_name("UpdatedName");
        dao.update(existingDomain);

        storage_domain_static after = dao.get(existingDomain.getId());

        assertEquals(after, existingDomain);
    }

    /**
     * Ensures that removing a storage domain works as expected.
     */
    @Test
    public void testRemove() {
        dynamicDao.remove(existingDomain.getId());
        for (DiskImage image : imageDao.getAllSnapshotsForStorageDomain(existingDomain.getId())) {
            imageDao.remove(image.getId());
        }
        dao.remove(existingDomain.getId());

        storage_domain_static domainResult = dao.get(existingDomain.getId());

        assertNull(domainResult);
    }

}
