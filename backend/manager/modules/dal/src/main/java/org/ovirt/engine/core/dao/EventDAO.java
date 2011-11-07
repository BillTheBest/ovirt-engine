package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.event_map;
import org.ovirt.engine.core.common.businessentities.event_notification_methods;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>EventDAO</code> defines a type for performing CRUD operations on instances of {@link event_subscriber}.
 *
 *
 */
public interface EventDAO extends DAO {
    /**
     * Returns all event subscribers.
     *
     * @return the list of all event subscribers
     */
    List<event_subscriber> getAll();

    /**
     * Retrieves all event subscriptions for the given subscriber id.
     *
     * @param id
     *            the subscriber id
     * @return the subscriptions
     */
    List<event_subscriber> getAllForSubscriber(Guid id);

    /**
     * Retrieves the list of all event notification methods.
     *
     * @return the event notificationet methods
     */
    List<event_notification_methods> getAllEventNotificationMethods();

    List<event_notification_methods> getEventNotificationMethodsById(int method_id);

    List<event_notification_methods> getEventNotificationMethodsByType(String method_type);

    /**
     * Saves the provided subscriber.
     *
     * @param subscriber
     *            the subscriber
     */
    void subscribe(event_subscriber subscriber);

    /**
     * Updates the provided subscriber.
     *
     * @param subscriber
     *            the subscriber
     * @param oldMethodId
     *            TODO
     */
    void update(event_subscriber subscriber, int oldMethodId);

    /**
     * Removes the specified subscriber.
     *
     * @param subscriber
     *            the subscriber
     */
    void unsubscribe(event_subscriber subscriber);

    List<event_map> getEventMapByName(String event_up_name);

    List<event_map> getAllEventMaps();
}
