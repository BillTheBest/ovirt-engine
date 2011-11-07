package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;

public class VmNetworkInterfaceDAOTest extends BaseDAOTestCase {
    private static final Guid TEMPLATE_ID = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");
    private static final Guid INTERFACE_ID = new Guid("e2817b12-f873-4046-b0da-0098293c14fd");
    private static final Guid VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");

    private VmNetworkInterfaceDAO dao;

    private VmNetworkInterface newVmInterface;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getVmNetworkInterfaceDAO());

        newVmInterface = new VmNetworkInterface();
        newVmInterface.setStatistics(new VmNetworkStatistics());
        newVmInterface.setId(Guid.NewGuid());
        newVmInterface.setName("eth77");
        newVmInterface.setNetworkName("enginet");
        newVmInterface.setSpeed(1000);
        newVmInterface.setType(3);
        newVmInterface.setMacAddress("01:C0:81:21:71:17");
    }

    /**
     * Ensures null is returned.
     */
    @Test
    public void testGetWithNonExistingId() {
        VmNetworkInterface result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that the network interface is returned.
     */
    @Test
    public void testGet() {
        VmNetworkInterface result = dao.get(INTERFACE_ID);

        assertNotNull(result);
        assertEquals(INTERFACE_ID, result.getId());
    }

    /**
     * Ensures that an empty collection is returned.
     */
    @Test
    public void testGetAllForTemplateWithInvalidTemplate() {
        List<VmNetworkInterface> result = dao.getAllForTemplate(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that interfaces are returned.
     */
    @Test
    public void testGetAllForTemplate() {
        List<VmNetworkInterface> result = dao.getAllForTemplate(TEMPLATE_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmNetworkInterface iface : result) {
            assertEquals(TEMPLATE_ID, iface.getVmTemplateId());
        }
    }

    /**
     * Ensures an empty collection is returned.
     */
    @Test
    public void testGetAllInterfacesForVmWithInvalidVm() {
        List<VmNetworkInterface> result = dao.getAllForVm(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that a collection of interfaces related the specified VM are returned.
     */
    @Test
    public void testGetAllInterfacesForVm() {
        List<VmNetworkInterface> result = dao.getAllForVm(VM_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (VmNetworkInterface iface : result) {
            assertEquals(VM_ID, iface.getVmId());
        }
    }

    /**
     * Ensures that saving an interface works as expected.
     */
    @Test
    public void testSave() {
        newVmInterface.setVmId(VM_ID);

        dao.save(newVmInterface);

        VmNetworkInterface savedInterface = dao.get(newVmInterface.getId());

        assertNotNull(savedInterface);
        assertEquals(newVmInterface.getName(), savedInterface.getName());
    }

    /**
     * Ensures updating an interface works.
     */
    @Test
    public void testUpdate() {
        List<VmNetworkInterface> before = dao.getAllForVm(VM_ID);
        VmNetworkInterface iface = before.get(0);

        iface.setName(iface.getName().toUpperCase());

        dao.update(iface);

        List<VmNetworkInterface> after = dao.getAllForVm(VM_ID);
        boolean found = false;

        for (VmNetworkInterface ifaced : after) {
            found |= ifaced.getName().equals(iface.getName());
        }

        assertTrue(found);
    }

    /**
     * Ensures that the specified VM's interfaces are deleted.
     */
    @Test
    public void testRemove() {
        assertNotNull(dao.get(INTERFACE_ID));

        dao.remove(INTERFACE_ID);

        assertNull(dao.get(INTERFACE_ID));
    }

    @Test(expected = NotImplementedException.class)
    public void testGetAll() throws Exception {
        dao.getAll();
    }
}
