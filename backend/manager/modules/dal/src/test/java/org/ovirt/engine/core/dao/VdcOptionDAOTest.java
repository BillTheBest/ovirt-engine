package org.ovirt.engine.core.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.VdcOption;

public class VdcOptionDAOTest extends BaseDAOTestCase {
    private static final int OPTION_COUNT = 5;
    private VdcOptionDAO dao;
    private VdcOption existingOption;
    private VdcOption newOption;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getVdcOptionDAO());

        existingOption = dao.getByNameAndVersion("UserDefinedVmPropertiesKey1", "general");

        newOption = new VdcOption();
        newOption.setoption_name("option_name");
        newOption.setoption_value("option_value");
        newOption.setversion("general");
    }

    /**
     * Ensures the ID must be valid.
     */
    @Test
    public void testGetWithInvalidId() {
        VdcOption result = dao.get(717);

        assertNull(result);
    }

    /**
     * Ensures retrieving an option works as expected.
     */
    @Test
    public void testGet() {
        VdcOption result = dao.get(existingOption.getoption_id());

        assertNotNull(result);
        assertEquals(existingOption, result);
    }

    /**
     * Ensures the name must be valid.
     */
    @Test
    public void testGetByNameAndVersionWithInvalidName() {
        VdcOption result = dao.getByNameAndVersion("farkle", existingOption.getversion());

        assertNull(result);
    }

    /**
     * Ensures the version must be valid.
     */
    @Test
    public void testGetByNameAndVersionWithInvalidVersion() {
        VdcOption result = dao.getByNameAndVersion(existingOption.getoption_name(), "farkle");

        assertNull(result);
    }

    /**
     * Ensures retrieving an option by name and version works.
     */
    @Test
    public void testGetBynameAndVersion() {
        VdcOption result = dao.getByNameAndVersion(existingOption.getoption_name(), existingOption.getversion());

        assertNotNull(result);
        assertEquals(existingOption, result);
    }

    /**
     * Ensures that all options are returned.
     */
    @Test
    public void testGetAll() {
        List<VdcOption> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(OPTION_COUNT, result.size());
    }

    /**
     * Ensure saving an option works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newOption);

        VdcOption result = dao.getByNameAndVersion(newOption.getoption_name(), newOption.getversion());

        assertNotNull(result);
        assertEquals(newOption, result);
    }

    /**
     * Ensures updating an option works as expected.
     */
    @Test
    public void testUpdate() {
        existingOption.setoption_value("this is a new value");

        dao.update(existingOption);

        VdcOption result = dao.get(existingOption.getoption_id());

        assertEquals(existingOption, result);
    }

    /**
     * Ensures removing an option works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingOption.getoption_id());

        VdcOption result = dao.get(existingOption.getoption_id());

        assertNull(result);
    }
}
