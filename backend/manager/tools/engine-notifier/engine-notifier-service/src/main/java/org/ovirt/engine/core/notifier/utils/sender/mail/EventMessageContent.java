package org.ovirt.engine.core.notifier.utils.sender.mail;

import org.ovirt.engine.core.common.businessentities.event_audit_log_subscriber;

/**
 * Creates a simple message subject and body using helper class {@linkplain MessageHelper} to determine <br>
 * the structure of the message subject and body
 */
public class EventMessageContent {
    private String subject;
    private String body;

    private void prepareMessageSubject(String hostName, event_audit_log_subscriber eventData) {
        subject = MessageHelper.prepareMessageSubject(eventData.getevent_type(), hostName, eventData.getmessage());
    }

    private void prepareMessageBody(event_audit_log_subscriber eventData, boolean isBodyHtml) {
        MessageBody messageBody = new MessageBody();
        messageBody.setUserInfo(eventData.getuser_name());
        messageBody.setVmInfo(eventData.getvm_name());
        messageBody.setHostInfo(eventData.getvds_name());
        messageBody.setTemplateInfo(eventData.getvm_template_name());
        messageBody.setDatacenterInfo(eventData.getstorage_pool_name());
        messageBody.setStorageDomainInfo(eventData.getstorage_domain_name());
        messageBody.setLogTime(eventData.getlog_time() != null ? eventData.getlog_time().toString() : "");
        messageBody.setSeverity(String.valueOf(eventData.getseverity()));
        messageBody.setMessage(eventData.getmessage());

        if (isBodyHtml) {
            this.body = MessageHelper.prepareHTMLMessageBody(messageBody);
        } else {
            this.body = MessageHelper.prepareMessageBody(messageBody);
        }
    }

    /**
     * returns a readable format of message body
     * @return a readable format of message body
     */
    public String getMessageBody() {
        return body;
    }

    /**
     * returns a readable format of message subject
     * @return a readable format of message subject
     */
    public String getMessageSubject() {
        return subject;
    }

    /**
     * Produces a readable message subject and body based on provided parameters<br>
     * The format of the subject and body are defined by {@linkplain MessageHelper}
     * @param hostName
     *            the host name on which the subject will refer to
     * @param eventData
     *            associated entry which the message will be created by
     * @param isBodyHtml
     *            defines the format of message body
     */
    public void prepareMessage(String hostName, event_audit_log_subscriber eventData, boolean isBodyHtml) {
        prepareMessageSubject(hostName, eventData);
        prepareMessageBody(eventData, isBodyHtml);
    }

}
