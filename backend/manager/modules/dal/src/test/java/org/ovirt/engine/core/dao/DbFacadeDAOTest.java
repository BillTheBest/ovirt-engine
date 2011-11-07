package org.ovirt.engine.core.dao;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeLocator;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class DbFacadeDAOTest extends BaseDAOTestCase {
    private static final Guid ADMIN_ROLE_TYPE_FROM_FIXTURE_ID = new Guid("F5972BFA-7102-4D33-AD22-9DD421BFBA78");
    private static final Guid SYSTEM_OBJECT_ID = new Guid("AAA00000-0000-0000-0000-123456789AAA");
    private static final String STATIC_VM_NAME = "rhel5-pool-50";
    private static final int NUM_OF_VM_STATIC_IN_FIXTURES = 3;
    List<VmStatic> vmStatics;
    VmStatic[] vmStaticArrayInDescOrder;
    Guid[] guidsArrayToBeChecked;
    List<Guid> vmStaticGuidsInDb;
    private static final int NUM_OF_VM_IN_FIXTURES_WITH_STATUS_MIGRATING_FROM = 2;
    private static final int NUM_OF_USERS_IN_FIXTURES = 2;
    private static final Guid STORAGE_POOL_WITH_MASTER_UP = new Guid("386BFFD1-E7ED-4B08-BCE9-D7DF10F8C9A0");
    private static final Guid STORAGE_POOL_WITH_MASTER_DOWN = new Guid("72B9E200-F48B-4687-83F2-62828F249A47");
    private static final Guid VM_STATIC_GUID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private static final boolean INITIALIZED = true;
    private static final Guid USER_ID_WITH_BASIC_PERMISSIONS = new Guid("88D4301A-17AF-496C-A793-584640853D4B");
    private static final Guid VMT_ID = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Retrieve all three vmstatics that were defined by fixtures and put in an array
        vmStatics = dbFacade.getVmStaticDAO().getAllByName(STATIC_VM_NAME);
        vmStaticArrayInDescOrder = new VmStatic[NUM_OF_VM_STATIC_IN_FIXTURES];
        guidsArrayToBeChecked = new Guid[NUM_OF_VM_STATIC_IN_FIXTURES];
        vmStaticGuidsInDb = getListOfGuidFromListOfVmStatics(vmStatics);
    }

    /**
     * Restores the fixtures back to initial status
     *
     * @param vmStatics
     */
    @After
    public void restoreFixtures() {
        Iterator<VmStatic> vmStaticIterator = vmStatics.iterator();
        while (vmStaticIterator.hasNext()) {
            dbFacade.getVmStaticDAO().update((VmStatic) vmStaticIterator.next());
        }
    }

    /**
     * Ensures that the checkDBConnection method returns true when the connection is up
     */
    @Test
    public void testDBConnectionWithConnection() {
        assertTrue(dbFacade.CheckDBConnection());
    }

    /**
     * Ensures that the checkDBConnection method throws an Exception when connection is not valid
     */
    @Test
    public void testDBConnectionWithoutConnection() {
        // setup
        DataSource result = null;
        Properties properties = new Properties();
        Config.setConfigUtils(new DBConfigUtils(false));

        try {
            properties.load(super.getClass().getResourceAsStream(
                    "/test-database.properties"));
            ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty("database.driver"));
            result = new SingleConnectionDataSource(
                    properties.getProperty("database.url"),
                    // Deliberately puts a none existing user name, so an
                    // exception will be thrown when trying to check the
                    // connection
                    "no-such-username",
                    properties.getProperty("database.password"),
                    true
                    );
            DbFacade localDbFacade = new DbFacade();
            localDbFacade.setDbEngineDialect(DbFacadeLocator.loadDbEngineDialect());
            localDbFacade.setTemplate(localDbFacade.getDbEngineDialect().createJdbcTemplate(result));
            localDbFacade.CheckDBConnection();
            fail("Connection should be down since the DataSource has an invalid username");
            // If DataAccessException is thrown - the test has succeeded. Was unable to do
            // with "expected" annotation, presumably since we are using DbUnit
        } catch (DataAccessException desiredException) {
            assertTrue(true);
            // If this exception is thrown we fail the test
        } catch (Exception undesiredException) {
            fail();
        }
    }

    @Test
    public void testUpdateLastAdminCheckStatus() {

        // Getting a nonAdmin user that is defined in the fixtures
        DbUser nonAdminUser = dbFacade.getDbUserDAO().getByUsername("userportal2@testportal.redhat.com");

        assertNotNull(nonAdminUser);
        assertFalse(nonAdminUser.getLastAdminCheckStatus());

        // execute and validate when not admin
        dbFacade.updateLastAdminCheckStatus(nonAdminUser.getuser_id());
        nonAdminUser = dbFacade.getDbUserDAO().get(nonAdminUser.getuser_id());

        assertFalse(nonAdminUser.getLastAdminCheckStatus());

        permissions perms = new permissions();
        perms.setRoleType(RoleType.ADMIN);

        // An available role from the fixtures
        perms.setrole_id(ADMIN_ROLE_TYPE_FROM_FIXTURE_ID);
        perms.setad_element_id(nonAdminUser.getuser_id());
        perms.setObjectId(SYSTEM_OBJECT_ID);
        perms.setObjectType(VdcObjectType.System);

        // Save the permission to the DB and make sure it has been saved
        dbFacade.getPermissionDAO().save(perms);
        assertNotNull(dbFacade.getPermissionDAO().get(perms.getId()));

        // execute and validate when admin
        dbFacade.updateLastAdminCheckStatus(nonAdminUser.getuser_id());
        nonAdminUser = dbFacade.getDbUserDAO().get(nonAdminUser.getuser_id());

        assertTrue(nonAdminUser.getLastAdminCheckStatus());
    }

    /**
     * Checking if the function gets the VmStatics in correct order according to priority
     */
    @Test
    public void testGetOrderedVmGuidsForRunMultipleActionsByPriority() {
        assertNotNull(vmStatics);
        vmStaticArrayInDescOrder = initVmStaticsOrderedByPriority(vmStatics);

        // execute
        vmStaticGuidsInDb = dbFacade.getOrderedVmGuidsForRunMultipleActions(vmStaticGuidsInDb);
        assertNotNull(vmStaticGuidsInDb);
        guidsArrayToBeChecked = vmStaticGuidsInDb.toArray(guidsArrayToBeChecked);

        boolean result = compareGuidArrays(guidsArrayToBeChecked, vmStaticArrayInDescOrder);
        assertTrue(result);
    }

    /**
     * Checking if the function gets the VmStatics in correct order according to auto_startup
     */
    @Test
    public void testGetOrderedVmGuidsForRunMultipleActionsByAutoStartup() {
        assertNotNull(vmStatics);
        vmStaticArrayInDescOrder = initVmStaticsOrderedByAutoStartup(vmStatics);

        // execute
        vmStaticGuidsInDb = dbFacade.getOrderedVmGuidsForRunMultipleActions(vmStaticGuidsInDb);
        assertNotNull(vmStaticGuidsInDb);
        guidsArrayToBeChecked = vmStaticGuidsInDb.toArray(guidsArrayToBeChecked);

        boolean result = compareGuidArrays(guidsArrayToBeChecked, vmStaticArrayInDescOrder);
        assertTrue(result);
    }

    /**
     * Checking if the function gets the VmStatics in correct order according to MigrationSupport
     */
    @Test
    public void testGetOrderedVmGuidsForRunMultipleActionsByMigrationSupport() {
        assertNotNull(vmStatics);
        vmStaticArrayInDescOrder = initVmStaticsOrderedByMigrationSupport(vmStatics);

        // execute
        vmStaticGuidsInDb = dbFacade.getOrderedVmGuidsForRunMultipleActions(vmStaticGuidsInDb);
        assertNotNull(vmStaticGuidsInDb);
        guidsArrayToBeChecked = vmStaticGuidsInDb.toArray(guidsArrayToBeChecked);

        boolean result = compareGuidArrays(guidsArrayToBeChecked, vmStaticArrayInDescOrder);
        assertTrue(result);
    }

    /**
     * {@code initVmStaticsOrderedByAutoStartup(List)} is the first method in VMs order selection tests. The other init
     * methods: <br>{@code initVmStaticsOrderedByPriority} and {@code initVmStaticsOrderedByAutoStartup} are relying on each
     * other for creating an array of VM Static objects.<br>
     * Each of the methods modifies the VM static array according to the column which is being tested, started from the
     * least important column to the most.<br>
     * That way prioritizing a preceded column should be reflected in the selection and therefore to validate the order
     * is maintained.
     * @return an array of VmStatics, in descending order according to: auto_startup, priority, MigrationSupport.<br>
     *         The MigrationSupport is the one being checked.<br>
     */
    private VmStatic[] initVmStaticsOrderedByMigrationSupport(List<VmStatic> vmStatics) {
        VmStatic[] vmStaticArray = new VmStatic[NUM_OF_VM_STATIC_IN_FIXTURES];

        vmStaticArray = (VmStatic[]) vmStatics.toArray(vmStaticArray);

        // initialize the VMs with equal settings: non HA, priority 1 and MIGRATABLE
        for (int i = 0; i < vmStaticArray.length; i++) {
            vmStaticArray[i].setauto_startup(false);
            vmStaticArray[i].setpriority(1);
            vmStaticArray[i].setMigrationSupport(MigrationSupport.MIGRATABLE);
        }

        // set higher migration support value for the first VM
        vmStaticArray[0].setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        vmStaticArray[1].setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
        updateArrayOfVmStaticsInDb(vmStaticArray);
        return vmStaticArray;
    }

    /**
     * Creates an array of VM static which was initiated for MigrationSupport order, and modified the priority to
     * reflect the precedence of the priority column on top the MigrationSupport.
     * @return an array of VmStatics, in descending order according to: auto_startup, priority, MigrationSupport. The
     *         priority is the one being checked.
     */
    private VmStatic[] initVmStaticsOrderedByPriority(List<VmStatic> vmStatics) {
        VmStatic[] vmStaticArray = new VmStatic[NUM_OF_VM_STATIC_IN_FIXTURES];
        vmStaticArray = initVmStaticsOrderedByMigrationSupport(vmStatics);

        // Swapping the first two VmStatics
        VmStatic tempVmStatic = vmStaticArray[0];
        vmStaticArray[0] = vmStaticArray[1];
        vmStaticArray[1] = tempVmStatic;

        int arrayLength = vmStaticArray.length;

        // Setting the array in descending order due to their priorities to maintain its correctness
        for (int i = 0; i < arrayLength; i++) {
            vmStaticArray[i].setpriority(arrayLength - i + 1);
        }

        updateArrayOfVmStaticsInDb(vmStaticArray);
        return vmStaticArray;
    }

    /**
     * Creates an array of VM static which was initiated for Priority and MigrationSupport order, and modified the
     * auto-startup to reflect the precedence of the auto-startup column on top the Priority.
     * @return an array of VmStatics, in descending order according to: auto_startup, priority, MigrationSupport. The
     *         auto_startup is the one being checked
     */
    private VmStatic[] initVmStaticsOrderedByAutoStartup(List<VmStatic> vmStatics) {
        VmStatic[] vmStaticArray = new VmStatic[NUM_OF_VM_STATIC_IN_FIXTURES];
        vmStaticArray = initVmStaticsOrderedByPriority(vmStatics);

        // Swapping the first two VmStatics
        VmStatic tempVmStatic = vmStaticArray[0];
        vmStaticArray[0] = vmStaticArray[1];
        vmStaticArray[1] = tempVmStatic;

        // Maintaining the order correctness of the elements by incrementing the auto_startup of the first element
        vmStaticArray[0].setauto_startup(true);

        updateArrayOfVmStaticsInDb(vmStaticArray);
        return vmStaticArray;
    }

    /**
     * Converts a list of vmStatics to a list if Guids
     */
    private List<Guid> getListOfGuidFromListOfVmStatics(List<VmStatic> vmStatics) {
        List<Guid> listOfGuidToReturn = new ArrayList<Guid>();
        for (VmStatic vmStatic : vmStatics) {
            listOfGuidToReturn.add(vmStatic.getId());
        }
        return listOfGuidToReturn;
    }

    /**
     * Updates the given array of vmStatics in the Database
     */
    private void updateArrayOfVmStaticsInDb(VmStatic[] vmStaticArray) {
        for (int i = 0; i < vmStaticArray.length; i++) {
            dbFacade.getVmStaticDAO().update(vmStaticArray[i]);
        }
    }

    /**
     * Compares between the two given guid arrays, returns true if they are equal and false otherwise
     */
    private boolean compareGuidArrays(Guid[] guidsArrayToBeChecked, VmStatic[] vmStaticArrayInDescOrder) {
        boolean returnValue = true;
        if (guidsArrayToBeChecked.length == vmStaticArrayInDescOrder.length) {
            for (int i = 0; i < guidsArrayToBeChecked.length; i++) {
                if (!guidsArrayToBeChecked[i].equals(vmStaticArrayInDescOrder[i].getId())) {
                    returnValue = false;
                    break;
                }
            }
        }

        return returnValue;
    }

    @Test
    public void testGetSystemStatisticsValueWithSpecifiedStatus() {
        int numOfVmWithStatusMigratingFrom = dbFacade.GetSystemStatisticsValue("VM", Integer.toString(VMStatus.MigratingFrom.getValue()));
        assertTrue(numOfVmWithStatusMigratingFrom == NUM_OF_VM_IN_FIXTURES_WITH_STATUS_MIGRATING_FROM);
    }

    @Test
    public void testGetSystemStatisticsValueWithoutSpecifiedStatus() {
        int numOfUsers = dbFacade.GetSystemStatisticsValue("User", "");
        assertTrue(numOfUsers == NUM_OF_USERS_IN_FIXTURES);
    }

    @Test
    public void testIsStoragePoolMasterUpWhenDown() {
        storage_pool storagePoolToCheck = dbFacade.getStoragePoolDAO().get(STORAGE_POOL_WITH_MASTER_DOWN);
        assertNotNull(storagePoolToCheck);

        Guid masterStorageDomainGuid =
                dbFacade.getStorageDomainDAO().getMasterStorageDomainIdForPool(STORAGE_POOL_WITH_MASTER_DOWN);
        assertNotNull(masterStorageDomainGuid);

        storage_pool_iso_map storagePoolIsoMapToCheck = dbFacade.getStoragePoolIsoMapDAO().get(new StoragePoolIsoMapId(
                masterStorageDomainGuid, storagePoolToCheck.getId()));
        assertNotNull(storagePoolIsoMapToCheck);

        storagePoolIsoMapToCheck.setstatus(StorageDomainStatus.InActive);
        dbFacade.getStoragePoolIsoMapDAO().update(storagePoolIsoMapToCheck);
        assertFalse(dbFacade.IsStoragePoolMasterUp(STORAGE_POOL_WITH_MASTER_DOWN));
    }

    @Test
    public void testIsStoragePoolMasterUpWhenUp() {
        assertTrue(dbFacade.IsStoragePoolMasterUp(STORAGE_POOL_WITH_MASTER_UP));
    }

    @Test
    public void testGetEntityNameByIdAndType() {
        VmStatic vmStatic = dbFacade.getVmStaticDAO().get(VM_STATIC_GUID);
        assertNotNull(vmStatic);
        String nameOfVmStatic = vmStatic.getvm_name();
        assertTrue(nameOfVmStatic.equals(dbFacade.getEntityNameByIdAndType(VM_STATIC_GUID, VdcObjectType.VM)));
    }

    @Test
    public void testSaveIsInitialized(){
        // The vm starts out as initialized
        VmStatic vmStaticForTest = dbFacade.getVmStaticDAO().get(VM_STATIC_GUID);
        assertNotNull(vmStaticForTest);
        assertTrue(vmStaticForTest.getis_initialized());

        // Change it into uninitialized and make sure that the change succeeded
        dbFacade.SaveIsInitialized(vmStaticForTest.getId(), !INITIALIZED);
        vmStaticForTest = dbFacade.getVmStaticDAO().get(VM_STATIC_GUID);
        assertNotNull(vmStaticForTest);
        assertFalse(vmStaticForTest.getis_initialized());

        // Change it back to initialized and make sure that the change succeeded
        dbFacade.SaveIsInitialized(vmStaticForTest.getId(), INITIALIZED);
        vmStaticForTest = dbFacade.getVmStaticDAO().get(VM_STATIC_GUID);
        assertNotNull(vmStaticForTest);
        assertTrue(vmStaticForTest.getis_initialized());
    }

    @Test
    public void testGetEntityPermissions(){
            // Should not return null since the user has the relevant permission
            assertNotNull(dbFacade.getEntityPermissions(USER_ID_WITH_BASIC_PERMISSIONS, ActionGroup.VM_BASIC_OPERATIONS,
                    VMT_ID, VdcObjectType.VM));

            // Should return null since the user does not has the relevant permission
            assertNull(dbFacade.getEntityPermissions(USER_ID_WITH_BASIC_PERMISSIONS, ActionGroup.CREATE_TEMPLATE,
                    VMT_ID, VdcObjectType.VM));
    }
}
