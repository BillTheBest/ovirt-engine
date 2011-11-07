package org.ovirt.engine.core.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.vds_spm_id_map;
import org.ovirt.engine.core.compat.Guid;

public class VdsSpmIdMapDAOTest extends BaseDAOTestCase {
    private static final Guid EXISTING_VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e7");
    private static final Guid FREE_VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final Guid EXISTING_STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105d");
    private static final Guid FREE_STORAGE_POOL_ID = new Guid("6d849ebf-755f-4552-ad09-9a090cda105e");
    private VdsDAO vdsDao;
    private VdsSpmIdMapDAO dao;
    private VDS existingVds;
    private vds_spm_id_map existingVdsSpmIdMap;
    private vds_spm_id_map newVdsSpmIdMap;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = prepareDAO(dbFacade.getVdsSpmIdMapDAO());
        vdsDao = prepareDAO(dbFacade.getVdsDAO());
        existingVds = vdsDao.get(EXISTING_VDS_ID);
        existingVdsSpmIdMap = dao.get(existingVds.getvds_id());
        newVdsSpmIdMap = new vds_spm_id_map(FREE_STORAGE_POOL_ID, FREE_VDS_ID, 1);
    }

    @Test
    public void testGet() {
        vds_spm_id_map result = dao.get(existingVdsSpmIdMap.getId());

        assertNotNull(result);
        assertEquals(existingVdsSpmIdMap, result);
    }

    @Test
    public void testSave() {
        dao.save(newVdsSpmIdMap);

        vds_spm_id_map result = dao.get(newVdsSpmIdMap.getId());

        assertNotNull(result);
        assertEquals(newVdsSpmIdMap, result);
    }

    @Test
    public void testRemove() {
        dao.remove(existingVdsSpmIdMap.getId());

        vds_spm_id_map result = dao.get(existingVdsSpmIdMap.getId());

        assertNull(result);
    }

    @Test
    public void testGetAll() {
        List<vds_spm_id_map> result = dao.getAll(EXISTING_STORAGE_POOL_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (vds_spm_id_map mapping : result) {
            assertEquals(EXISTING_STORAGE_POOL_ID, mapping.getstorage_pool_id());
        }
    }

    @Test
    public void testGetVdsSpmIdMapForStoragePoolAndVdsId() {
        vds_spm_id_map result =
                dao.get(existingVdsSpmIdMap.getstorage_pool_id(),
                        existingVdsSpmIdMap.getvds_spm_id());

        assertNotNull(result);
        assertEquals(existingVdsSpmIdMap, result);
    }
}
