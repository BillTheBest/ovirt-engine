package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.event_notification_methods;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class EventSubscriptionCommandBase<T extends EventSubscriptionParametesBase> extends
        AdminOperationCommandBase<T> {
    protected EventSubscriptionCommandBase(T parameters) {
        super(parameters);
    }

    /**
     * Validates the notification method.
     *
     * @param event_notification_methods
     *            The event_notification_methods.
     * @param event_subscriber
     *            The event_subscriber.
     * @param user
     *            The user.
     * @return
     */
    protected boolean ValidateNotificationMethod(java.util.List<event_notification_methods> event_notification_methods,
                                                 event_subscriber event_subscriber, DbUser user) {
        boolean retValue = true;
        EventNotificationMethods notificationMethod = event_notification_methods.get(0).getmethod_type();

        switch (notificationMethod) {
        case EMAIL:
            String mailAdress = (StringHelper.isNullOrEmpty(event_subscriber.getmethod_address())) ? user.getemail()
                    : event_subscriber.getmethod_address();

            if (StringHelper.isNullOrEmpty(mailAdress) || !ValidatMailAddress(mailAdress)) {
                addCanDoActionMessage(VdcBllMessages.USER_DOES_NOT_HAVE_A_VALID_EMAIL);
                retValue = false;
            }
            break;
        default:
            addCanDoActionMessage(VdcBllMessages.EN_UNKNOWN_NOTIFICATION_METHOD);
            retValue = false;
            break;
        }
        return retValue;
    }

    /**
     * Validates the notification method and tag.
     *
     * @param event_notification_methods
     *            The event_notification_methods.
     * @param event_subscriber
     *            The event_subscriber.
     * @param user
     *            The user.
     * @return
     */
    protected boolean ValidateAdd(List<event_notification_methods> event_notification_methods,
                                  event_subscriber event_subscriber, DbUser user) {
        String tagName = event_subscriber.gettag_name();
        // validate notification method
        boolean retValue = ValidateNotificationMethod(event_notification_methods, event_subscriber, user);

        // validate tag name if exists
        if (retValue && !StringHelper.isNullOrEmpty(tagName)) {
            retValue = ValidateTag(tagName);
        }
        return retValue;
    }

    protected boolean ValidateRemove(List<event_notification_methods> event_notification_methods,
                                     event_subscriber event_subscriber, DbUser user) {
        boolean retValue = false;
        // check if user is subscribed to the event
        List<event_subscriber> list = DbFacade.getInstance()
                .getEventDAO()
                .getAllForSubscriber(event_subscriber.getsubscriber_id());
        if (list.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.EN_NOT_SUBSCRIBED);
        } else {
            if (!ValidateSubscription(list, event_subscriber)) {
                addCanDoActionMessage(VdcBllMessages.EN_NOT_SUBSCRIBED);
            } else {
                String tagName = event_subscriber.gettag_name();
                // validate notification method
                retValue = ValidateNotificationMethod(event_notification_methods, event_subscriber, user);

                // validate tag name if exists
                if (retValue && !StringHelper.isNullOrEmpty(tagName)) {
                    retValue = ValidateTag(tagName);
                }
            }
        }
        return retValue;
    }

    /**
     * Validates the tag.
     *
     * @param tagName
     *            Name of the tag.
     * @return
     */
    protected boolean ValidateTag(String tagName) {
        boolean retValue = true;
        tags tag = DbFacade.getInstance().getTagDAO().getByName(tagName);
        if (tag == null) {

            addCanDoActionMessage(VdcBllMessages.EN_UNKNOWN_TAG_NAME);
            retValue = false;
        }

        return retValue;
    }

    /**
     * Determines whether [is valid email] [the specified input email].
     *
     * @param inputEmail
     *            The input email.
     * @return <c>true</c> if [is valid email] [the specified input email];
     *         otherwise, <c>false</c>.
     */
    protected static boolean ValidatMailAddress(String inputEmail) {
        final String strRegex = "^[\\w-]+(?:\\.[\\w-]+)*@(?:[\\w-]+\\.)+[a-zA-Z]{2,7}$";
        Regex re = new Regex(strRegex);
        return re.IsMatch(inputEmail);
    }

    private static boolean ValidateSubscription(Iterable<event_subscriber> subscriptions, event_subscriber current) {
        boolean retValue = false;
        for (event_subscriber event_subscriber : subscriptions) {
            if (event_subscriber.getsubscriber_id().equals(current.getsubscriber_id())
                    && StringHelper.EqOp(event_subscriber.getevent_up_name(), current.getevent_up_name())
                    && event_subscriber.getmethod_id() == current.getmethod_id()) {
                retValue = true;
                break;
            }

        }
        return retValue;
    }
}
