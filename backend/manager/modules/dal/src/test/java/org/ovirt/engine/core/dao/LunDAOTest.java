package org.ovirt.engine.core.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.LUNs;

public class LunDAOTest extends BaseDAOTestCase {
    private static final String STORAGE_SERVER_CONNECTION_ID = "0cc146e8-e5ed-482c-8814-270bc48c297f";
    private LunDAO dao;
    private LUNs existingLUN;
    private LUNs newLUN;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getLunDAO());

        existingLUN = dao.get("1IET_00180001");

        newLUN = new LUNs();
        newLUN.setLUN_id("oicu812");
    }

    /**
     * Ensures that the id must be valid.
     */
    @Test
    public void testGetWithInvalidId() {
        LUNs result = dao.get("farkle");

        assertNull(result);
    }

    /**
     * Ensures that retrieving by ID works as expected.
     */
    @Test
    public void testGet() {
        LUNs result = dao.get(existingLUN.getLUN_id());

        assertNotNull(result);
        assertEquals(existingLUN, result);
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllForStorageServerConnectionWithNoLuns() {
        List<LUNs> result = dao.getAllForStorageServerConnection("farkle");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that LUNs are returned for the connection.
     */
    @Test
    public void testGetAllForStorageServerConnection() {
        List<LUNs> result = dao.getAllForStorageServerConnection(STORAGE_SERVER_CONNECTION_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForVolumeGroupWithNoLuns() {
        List<LUNs> result = dao.getAllForVolumeGroup("farkle");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures the right set of LUNs are returned.
     */
    @Test
    public void testGetAllForVolumeGroup() {
        List<LUNs> result = dao.getAllForVolumeGroup(existingLUN.getvolume_group_id());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (LUNs lun : result) {
            assertEquals(existingLUN.getvolume_group_id(), lun.getvolume_group_id());
        }
    }

    /**
     * Ensures saving a LUN works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newLUN);

        LUNs result = dao.get(newLUN.getLUN_id());

        assertNotNull(result);
        assertEquals(newLUN, result);
    }

    /**
     * Ensures removing a LUN works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingLUN.getLUN_id());

        LUNs result = dao.get(existingLUN.getLUN_id());

        assertNull(result);
    }
}
