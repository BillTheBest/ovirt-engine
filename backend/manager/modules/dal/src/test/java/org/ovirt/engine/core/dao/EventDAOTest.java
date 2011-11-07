package org.ovirt.engine.core.dao;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.common.businessentities.event_map;
import org.ovirt.engine.core.common.businessentities.event_notification_hist;
import org.ovirt.engine.core.common.businessentities.event_notification_methods;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.compat.Guid;

public class EventDAOTest extends BaseDAOTestCase {
    private static final int FREE_AUDIT_LOG_ID = 44295;
    private static final int EVENT_MAP_COUNT = 1;
    private static final int NOTIFICATION_METHOD_COUNT = 3;
    private EventDAO dao;
    private Guid existingSubscriber;
    private Guid newSubscriber;
    private event_subscriber newSubscription;
    private event_notification_hist newHistory;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getEventDAO());
        existingSubscriber = new Guid("9bf7c640-b620-456f-a550-0348f366544a");
        newSubscriber = new Guid("9bf7c640-b620-456f-a550-0348f366544b");
        newSubscription = new event_subscriber();
        newSubscription.setsubscriber_id(newSubscriber);
        newSubscription.setmethod_id(1);
        newSubscription.setevent_up_name("TestRun");
        newSubscription.settag_name("farkle");

        newHistory = new event_notification_hist();
        newHistory.setaudit_log_id(FREE_AUDIT_LOG_ID);
        newHistory.setevent_name("Failure");
        newHistory.setmethod_type("Email");
        newHistory.setreason("Dunno");
        newHistory.setsent_at(new Date());
        newHistory.setstatus(false);
        newHistory.setsubscriber_id(existingSubscriber);
    }

    /**
     * Ensures that retrieving all subscribers works as expected.
     */
    @Test
    public void testGetAll() {
        List<event_subscriber> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    /**
     * Ensures an empty collection is returned when the user has no subscriptions.
     */
    @Test
    public void testGetAllForSubscriberWithNoSubscriptions() {
        List<event_subscriber> result = dao.getAllForSubscriber(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that all subscriptions are returned.
     */
    @Test
    public void testGetAllForSubscriber() {
        List<event_subscriber> result = dao
                .getAllForSubscriber(existingSubscriber);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (event_subscriber subscription : result) {
            assertEquals(existingSubscriber,
                    subscription.getsubscriber_id());
        }
    }

    @Test
    public void testGetAllEventNotificationMethods() {
        List<event_notification_methods> result = dao.getAllEventNotificationMethods();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(NOTIFICATION_METHOD_COUNT, result.size());
    }

    @Test
    public void testGetEventNotificationMethodsById() {
        List<event_notification_methods> result = dao.getEventNotificationMethodsById(1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    public void testGetEventNotificationMethodsByType() {
        String target = "Email";

        List<event_notification_methods> result =
                dao.getEventNotificationMethodsByType(target);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (event_notification_methods method : result) {
            assertEquals(EventNotificationMethods.EMAIL, method.getmethod_type());
        }
    }

    /**
     * Ensures that subscribing a user works as expected.
     */
    @Test
    public void testSubscribe() {
        dao.subscribe(newSubscription);

        List<event_subscriber> result = dao.getAllForSubscriber(newSubscription
                .getsubscriber_id());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (event_subscriber subscription : result) {
            assertEquals(newSubscriber, subscription.getsubscriber_id());
        }
    }

    /**
     * Ensures that updating a subscription works as expected.
     */
    @Test
    public void testUpdate() {
        event_subscriber before = dao
                .getAllForSubscriber(existingSubscriber).get(0);

        int oldMethodId = before.getmethod_id();
        before.setmethod_id(2);

        dao.update(before, oldMethodId);

        event_subscriber after = dao
                .getAllForSubscriber(existingSubscriber).get(0);

        assertNotNull(after);
        assertEquals(before, after);
    }

    /**
     * Ensures that unsubscribing a user works as expected.
     */
    @Test
    public void testUnsubscribe() {
        List<event_subscriber> before = dao
                .getAllForSubscriber(existingSubscriber);

        // ensure we have subscriptions
        assertFalse(before.isEmpty());
        for (event_subscriber subscriber : before) {
            dao.unsubscribe(subscriber);
        }

        List<event_subscriber> after = dao
                .getAllForSubscriber(existingSubscriber);

        assertNotNull(after);
        assertTrue(after.isEmpty());
    }

    @Test
    public void testGetEventMapByNameWithInvalidName() {
        List<event_map> result = dao.getEventMapByName("farkle");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetEventMapByName() {
        List<event_map> result = dao.getEventMapByName("TestRun");

        assertNotNull(result);
        for (event_map mapping : result) {
            assertEquals("TestRun", mapping.getevent_up_name());
        }
    }

    @Test
    public void testGetAllEventMaps() {
        List<event_map> result = dao.getAllEventMaps();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(EVENT_MAP_COUNT, result.size());
    }

}
