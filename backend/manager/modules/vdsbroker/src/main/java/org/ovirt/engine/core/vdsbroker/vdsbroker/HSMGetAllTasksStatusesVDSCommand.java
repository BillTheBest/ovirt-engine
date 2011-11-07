package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

@Logged(executionLevel = LogLevel.DEBUG)
public class HSMGetAllTasksStatusesVDSCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    private TaskStatusListReturnForXmlRpc _result;

    public HSMGetAllTasksStatusesVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getAllTasksStatuses();
        ProceedProxyReturnValue();
        setReturnValue(ParseTaskStatusList(_result.TaskStatusList));
    }

    protected AsyncTaskStatus ParseTaskStatus(TaskStatusForXmlRpc taskStatus) {
        AsyncTaskStatus task = new AsyncTaskStatus();
        task.setStatus((taskStatus != null && taskStatus.mTaskState != null) ? (AsyncTaskStatusEnum
                .valueOf(taskStatus.mTaskState)) : AsyncTaskStatusEnum.unknown);

        if (task.getStatus() == AsyncTaskStatusEnum.finished) {
            UpdateReturnStatus(taskStatus);

            try {
                ProceedProxyReturnValue();
            }

            catch (RuntimeException ex) {
                task.setException(ex);
            }

            task.setResult((AsyncTaskResultEnum.valueOf(taskStatus.mTaskResult)));

            // Normally, when the result is not 'success', there is an
            // exception.
            // Just in case, we check the result here and if there is no
            // exception,
            // we throw a special one here:
            if (task.getResult() != AsyncTaskResultEnum.success && task.getException() == null) {
                task.setException(new VDSTaskResultNotSuccessException(String.format(
                        "TaskState contained successful return code, but a non-success result ('%1$s').",
                        taskStatus.mTaskResult)));
            }
        }
        return task;
    }

    protected java.util.HashMap<Guid, AsyncTaskStatus> ParseTaskStatusList(XmlRpcStruct taskStatusList) {
        java.util.HashMap<Guid, AsyncTaskStatus> result = new java.util.HashMap<Guid, AsyncTaskStatus>(
                taskStatusList.getCount());
        for (java.util.Map.Entry<String, ?> entry : taskStatusList.getEntries()) {
            try {
                Guid taskGuid = new Guid(entry.getKey().toString());
                java.util.Map xrsTaskStatusAsMAp = (java.util.Map)  entry.getValue();
                XmlRpcStruct xrsTaskStatus = (xrsTaskStatusAsMAp != null) ? new XmlRpcStruct(xrsTaskStatusAsMAp) : null;
                TaskStatusForXmlRpc tempVar = new TaskStatusForXmlRpc();
                tempVar.mCode = Integer.parseInt(xrsTaskStatus.getItem("code").toString());
                tempVar.mMessage = xrsTaskStatus.getItem("message").toString();
                tempVar.mTaskResult = xrsTaskStatus.getItem("taskResult").toString();
                tempVar.mTaskState = xrsTaskStatus.getItem("taskState").toString();
                TaskStatusForXmlRpc taskStatus = tempVar;
                AsyncTaskStatus task = ParseTaskStatus(taskStatus);
                result.put(taskGuid, task);
            } catch (RuntimeException exp) {
                log.error(
                        String.format(
                                "HSMGetAllTasksStatusesVDSCommand: Error while parsing task status from list. key: %1$s, value: %2$s - ignoring.",
                                entry.getKey().toString(),
                                entry.getValue().toString()),
                        exp);
            }
        }
        return result;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    // overrides the value of the status that is being checked in the
    // ProceedProxyReturnValue method.
    // Used when multiple calls to ProceedProxyReturnValue are needed within
    // the same VDSCommand on different status values, for example, a regular
    // verb
    // execution status and an asynchronous task status.
    protected void UpdateReturnStatus(StatusForXmlRpc newReturnStatus) {
        _result.mStatus = newReturnStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }

    private static LogCompat log = LogFactoryCompat.getLog(HSMGetAllTasksStatusesVDSCommand.class);
}
