package org.ovirt.engine.core.dao;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>AuditLogDAOTest</code> performs tests against the {@link AuditLogDAO} type.
 *
 * NOTE: Time-lease pools feature is currently not active and may be re-designed in future will need to add a test case
 * for time-lease pools.
 *
 */
public class AuditLogDAOTest extends BaseDAOTestCase {
    private static final Guid VDS_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    private static final long EXISTING_ENTRY_ID = 44291;
    private static final SimpleDateFormat EXPECTED_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int EXISTING_COUNT = 5;

    private AuditLogDAO dao;
    private AuditLog newAuditLog;
    private AuditLog existingAuditLog;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        dao = prepareDAO(dbFacade.getAuditLogDAO());

        // create some test data
        newAuditLog = new AuditLog();
        newAuditLog.setaudit_log_id(44000);
        newAuditLog.setuser_id(new Guid("9bf7c640-b620-456f-a550-0348f366544b"));
        newAuditLog.setuser_name("userportal3");
        newAuditLog.setvm_id(new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355"));
        newAuditLog.setvm_name("rhel5-pool-50");
        newAuditLog.setvm_template_id(new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79"));
        newAuditLog.setvm_template_name("1");
        newAuditLog.setvds_id(VDS_ID);
        newAuditLog.setvds_name("magenta-vdsc");
        newAuditLog.setlog_time(EXPECTED_DATE_FORMAT.parse("2010-12-22 14:00:00"));
        newAuditLog.setlog_type(AuditLogType.IRS_DISK_SPACE_LOW_ERROR);
        newAuditLog.setseverity(AuditLogSeverity.ERROR);
        newAuditLog.setmessage("Critical, Low disk space.  domain has 1 GB of free space");
        newAuditLog.setstorage_pool_id(new Guid("6d849ebf-755f-4552-ad09-9a090cda105d"));
        newAuditLog.setstorage_pool_name("rhel6.iscsi");
        newAuditLog.setstorage_domain_id(new Guid("72e3a666-89e1-4005-a7ca-f7548004a9ab"));
        newAuditLog.setstorage_domain_name("fDMzhE-wx3s-zo3q-Qcxd-T0li-yoYU-QvVePk");

        existingAuditLog = dao.get(EXISTING_ENTRY_ID);
    }

    /**
     * Ensures that if the id is invalid then no AuditLog is returned.
     */
    @Test
    public void testGetWithInvalidId() {
        AuditLog result = dao.get(7);

        assertNull(result);
    }

    /**
     * Ensures that, if the id is valid, then retrieving a AuditLog works as expected.
     */
    @Test
    public void testGet() {
        AuditLog result = dao.get(44291);

        assertNotNull(result);
        assertEquals(existingAuditLog, result);
    }

    /**
     * Ensures that finding all AuditLog works as expected.
     */
    @Test
    public void testGetAll() {
        List<AuditLog> result = dao.getAll();

        assertEquals(EXISTING_COUNT, result.size());
    }

    /**
     * Test date filtering
     *
     * @throws Exception
     */
    @Test
    public void testGetAllAfterDate()
            throws Exception {
        Date cutoff = EXPECTED_DATE_FORMAT.parse("2010-12-20 13:00:00");

        List<AuditLog> result = dao.getAllAfterDate(cutoff);

        assertNotNull(result);
        assertEquals(EXISTING_COUNT, result.size());

        cutoff = EXPECTED_DATE_FORMAT.parse("2010-12-20 14:00:00");

        result = dao.getAllAfterDate(cutoff);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Test query
     */
    @Test
    public void testGetAllWithQuery() {
        List<AuditLog> result = dao.getAllWithQuery("SELECT * FROM audit_log WHERE vds_name = 'magenta-vdsc'");

        assertEquals(EXISTING_COUNT, result.size());
    }

    @Test
    public void testRemoveAllBeforeDate()
            throws Exception {
        Date cutoff = EXPECTED_DATE_FORMAT.parse("2010-12-20 13:11:00");

        dao.removeAllBeforeDate(cutoff);

        // show be 1 left that was in event_notification_hist
        List<AuditLog> result = dao.getAll();

        assertEquals(3, result.size());
    }

    @Test
    public void testRemoveAllForVds()
            throws Exception {
        dao.removeAllForVds(VDS_ID, true);

        // show be 1 left that was in event_notification_hist
        List<AuditLog> result = dao.getAll();

        assertEquals(3, result.size());
    }

    @Test
    public void testRemoveAllOfTypeForVds()
            throws Exception {
        dao.removeAllOfTypeForVds(VDS_ID,
                AuditLogType.IRS_DISK_SPACE_LOW_ERROR.getValue());

        // show be 1 left that was in event_notification_hist
        List<AuditLog> result = dao.getAll();

        assertEquals(2, result.size());
    }

    /**
     * Ensures that saving a AuditLog works as expected.
     */
    @Test
    @Ignore
    public void testSave() {
        dao.save(newAuditLog);

        AuditLog result = dao.get(newAuditLog.getaudit_log_id());
        assertNotNull(result);
        assertEquals(newAuditLog, result);
    }

    /**
     * Ensures that saving a AuditLog with long message works as expected.
     */
    @Test
    @Ignore
    public void testLongMessageSave() {
        // generate a value that is longer than the max configured.
        char[] fill = new char[Config.<Integer>GetValue(ConfigValues.MaxAuditLogMessageLength) + 1];
        Arrays.fill(fill, '0');
        newAuditLog.setaudit_log_id(45000);
        newAuditLog.setmessage(new String(fill));
        dao.save(newAuditLog);

        AuditLog result = dao.get(newAuditLog.getaudit_log_id());
        assertNotNull(result);
        assertTrue(result.getmessage().endsWith("..."));
    }

    /**
     * Ensures that update a AuditLog works as expected.
     */
    @Test
    public void testUpdate() {
        existingAuditLog.setmessage(existingAuditLog.getmessage().toUpperCase());
        existingAuditLog.setseverity(AuditLogSeverity.ERROR);

        dao.update(existingAuditLog);
        AuditLog result = dao.get(existingAuditLog.getaudit_log_id());

        assertNotNull(result);
        assertEquals(existingAuditLog, result);
    }

    /**
     * Ensures that removing an AuditLog works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingAuditLog.getaudit_log_id());

        AuditLog result = dao.get(existingAuditLog.getaudit_log_id());

        assertNull(result);
    }
}
