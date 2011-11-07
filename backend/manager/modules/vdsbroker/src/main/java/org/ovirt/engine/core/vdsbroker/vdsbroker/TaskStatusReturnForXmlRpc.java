package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;

public final class TaskStatusReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String TASK_STATUS = "taskStatus";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("taskStatus")]
    public TaskStatusForXmlRpc TaskStatus;

    @SuppressWarnings("unchecked")
    public TaskStatusReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = (Object) innerMap.get(TASK_STATUS);
        if (temp != null) {
            TaskStatus = new TaskStatusForXmlRpc((Map<String, Object>) temp);
        }
    }

}
