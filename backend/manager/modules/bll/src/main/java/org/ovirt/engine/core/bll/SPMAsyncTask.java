package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.config.ConfigValues.UknownTaskPrePollingLapse;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.SPMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class SPMAsyncTask {
    public SPMAsyncTask(AsyncTaskParameters parameters) {
        setParameters(parameters);
        setState(AsyncTaskState.Initializing);
        AddOrUpdateTaskInDB();
    }

    private AsyncTaskParameters privateParameters;

    public AsyncTaskParameters getParameters() {
        return privateParameters;
    }

    public void setParameters(AsyncTaskParameters value) {
        privateParameters = value;
    }

    public Guid getTaskID() {
        return getParameters().getTaskID();
    }

    public Guid getStoragePoolID() {
        return getParameters().getStoragePoolID();
    }

    private AsyncTaskState privateState = AsyncTaskState.forValue(0);

    public AsyncTaskState getState() {
        return privateState;
    }

    public void setState(AsyncTaskState value) {
        privateState = value;
    }

    public boolean getShouldPoll() {
        AsyncTaskState state = getState();
        return (state == AsyncTaskState.Polling || state == AsyncTaskState.Ended || state == AsyncTaskState.ClearFailed)
                && getLastTaskStatus().getStatus() != AsyncTaskStatusEnum.unknown
                && (getParameters().getEntityId() == null ? isTaskOverPrePollingLapse() : true);
    }

    private AsyncTaskStatus _lastTaskStatus = new AsyncTaskStatus(AsyncTaskStatusEnum.init);

    public AsyncTaskStatus getLastTaskStatus() {
        return _lastTaskStatus;
    }

    /**
     * Set the _lastTaskStatus with taskStatus.
     *
     * @param taskStatus
     *            - task status to set.
     */
    protected void setLastTaskStatus(AsyncTaskStatus taskStatus) {
        _lastTaskStatus = taskStatus;
    }

    /**
     * Update task last access date ,only for not active task.
     */
    public void setLastStatusAccessTime() {
        // Change access date to now , when task is not active.
        if (getState() == AsyncTaskState.Ended
                || getState() == AsyncTaskState.AttemptingEndAction
                || getState() == AsyncTaskState.ClearFailed
                || getState() == AsyncTaskState.Cleared) {
            _lastAccessToStatusSinceEnd = System.currentTimeMillis();
        }
    }

    // Indicates time in milliseconds when task status recently changed.
    protected long _lastAccessToStatusSinceEnd = System.currentTimeMillis();

    public long getLastAccessToStatusSinceEnd() {
        return _lastAccessToStatusSinceEnd;
    }

    public Object getContainerId() {
        return getParameters().getEntityId();
    }

    private void AddOrUpdateTaskInDB() {
        try {
            if (getParameters().getDbAsyncTask() != null) {
                if (DbFacade.getInstance().getAsyncTaskDAO().get(getTaskID()) == null) {
                    log.infoFormat("BaseAsyncTask::AddOrUpdateTaskInDB: Adding task {0} to DataBase", getTaskID());
                    DbFacade.getInstance().getAsyncTaskDAO().save(getParameters().getDbAsyncTask());
                } else {
                    DbFacade.getInstance().getAsyncTaskDAO().update(getParameters().getDbAsyncTask());
                }
            }
        } catch (RuntimeException e) {
            log.error(String.format(
                    "BaseAsyncTask::AddOrUpdateTaskInDB: Adding/Updating task %1$s to DataBase threw an exception.",
                    getTaskID()), e);
        }
    }

    public void UpdateAsyncTask() {
        AddOrUpdateTaskInDB();
    }

    public void StartPollingTask() {
        AsyncTaskState state = getState();
        if (state != AsyncTaskState.AttemptingEndAction
                && state != AsyncTaskState.Cleared
                && state != AsyncTaskState.ClearFailed) {
            log.infoFormat("BaseAsyncTask::StartPollingTask: Starting to poll task '{0}'.", getTaskID());
            ConcreteStartPollingTask();
        }
    }

    /**
     * Use this to hold unknown tasks from polling, to overcome bz673695 without a complete re-haul to the
     * AsyncTaskManager and CommandBase.
     * @TODO remove this and re-factor {@link AsyncTaskManager}
     * @return true when the time passed after creating the task is bigger than
     *         <code>ConfigValues.UknownTaskPrePollingLapse</code>
     * @see AsyncTaskManager
     * @see CommandBase
     * @since 3.0
     */
    boolean isTaskOverPrePollingLapse() {
        AsyncTaskParameters parameters = getParameters();
        long taskStartTime = parameters.getDbAsyncTask().getaction_parameters().getTaskStartTime();
        Integer prePollingPeriod = Config.<Integer> GetValue(UknownTaskPrePollingLapse);
        boolean idlePeriodPassed =
                System.currentTimeMillis() - taskStartTime > prePollingPeriod;

        log.infoFormat("task id {0} {1}. Pre-polling period is {2} millis. ",
                parameters.getTaskID(),
                idlePeriodPassed ? "has passed pre-polling period time and should be polled"
                        : "is in pre-polling  period and should not be polled", prePollingPeriod);
        return idlePeriodPassed;
    }

    protected void ConcreteStartPollingTask() {
        setState(AsyncTaskState.Polling);
    }

    /**
     * For each task set its updated status retrieved from VDSM.
     *
     * @param returnTaskStatus
     *            - Task status returned from VDSM.
     */
    public void UpdateTask(AsyncTaskStatus returnTaskStatus) {
        try {
            switch (getState()) {
            case Polling:
                // Get the returned task
                returnTaskStatus = CheckTaskExist(returnTaskStatus);
                if (returnTaskStatus.getStatus() != getLastTaskStatus().getStatus()) {
                    AddLogStatusTask(returnTaskStatus);
                }
                setLastTaskStatus(returnTaskStatus);

                if (!getLastTaskStatus().getTaskIsRunning()) {
                    HandleEndedTask();
                }
                break;

            case Ended:
                HandleEndedTask();
                break;

            // Try to clear task which failed to be cleared before SPM and DB
            case ClearFailed:
                ClearAsyncTask();
                break;
            }
        }

        catch (RuntimeException e) {
            log.error(
                    String.format(
                            "BaseAsyncTask::PollAndUpdateTask: Handling task '%1$s' (State: %2$s, Parent Command: %3$s, Parameters Type: %4$s) threw an exception",
                            getTaskID(),
                            getState(),
                            (VdcActionType) (getParameters().getDbAsyncTask()
                                    .getaction_type()),
                            getParameters()
                                    .getClass().getName()),
                    e);
        }
    }

    /**
     * Handle ended task operation. Change task state to Ended ,Cleared or
     * Cleared Failed , and log appropriate message.
     */
    private void HandleEndedTask() {
        // If task state is different from Ended chnage it to Ended and set the
        // last access time to now.
        if (getState() != AsyncTaskState.Ended) {
            setState(AsyncTaskState.Ended);
            setLastStatusAccessTime();
        }

        if (HasTaskEndedSuccessfully()) {
            OnTaskEndSuccess();
        }

        else if (HasTaskEndedInFailure()) {
            OnTaskEndFailure();
        }

        else if (!DoesTaskExist()) {
            OnTaskDoesNotExist();
        }
    }

    protected void RemoveTaskFromDB() {
        try {
            if (DbFacade.getInstance().getAsyncTaskDAO().get(getTaskID()) != null) {
                log.infoFormat("BaseAsyncTask::RemoveTaskFromDB: Removing task {0} from DataBase", getTaskID());
                DbFacade.getInstance().getAsyncTaskDAO().remove(getTaskID());
            }
        }

        catch (RuntimeException e) {
            log.error(String.format(
                    "BaseAsyncTask::RemoveTaskFromDB: Removing task %1$s from DataBase threw an exception.",
                    getTaskID()), e);
        }
    }

    private boolean HasTaskEndedSuccessfully() {
        return getLastTaskStatus().getTaskEndedSuccessfully();
    }

    private boolean HasTaskEndedInFailure() {
        return !getLastTaskStatus().getTaskIsRunning() && !getLastTaskStatus().getTaskEndedSuccessfully();
    }

    private boolean DoesTaskExist() {
        return getLastTaskStatus().getStatus() != AsyncTaskStatusEnum.unknown;
    }

    protected void OnTaskEndSuccess() {
        LogEndTaskSuccess();
        ClearAsyncTask();
    }

    protected void LogEndTaskSuccess() {
        log.infoFormat(
                "BaseAsyncTask::OnTaskEndSuccess: Task '{0}' (Parent Command {1}, Parameters Type {2}) ended successfully.",
                getTaskID(),
                (VdcActionType) (getParameters().getDbAsyncTask().getaction_type()),
                getParameters()
                        .getClass().getName());
    }

    protected void OnTaskEndFailure() {
        LogEndTaskFailure();
        ClearAsyncTask();
    }

    protected void LogEndTaskFailure() {
        log.errorFormat(
                "BaseAsyncTask::LogEndTaskFailure: Task '{0}' (Parent Command {1}, Parameters Type {2}) ended with failure:"
                        + "\r\n" + "-- Result: '{3}'" + "\r\n" + "-- Message: '{4}'," + "\r\n" + "-- Exception: '{5}'",
                getTaskID(),
                (VdcActionType) (getParameters().getDbAsyncTask().getaction_type()),
                getParameters()
                        .getClass().getName(),
                getLastTaskStatus().getResult(),
                (getLastTaskStatus().getMessage() == null ? "[null]" : getLastTaskStatus().getMessage()),
                (getLastTaskStatus()
                        .getException() == null ? "[null]" : getLastTaskStatus().getException().getMessage()));
    }

    protected void OnTaskDoesNotExist() {
        LogTaskDoesntExist();
        ClearAsyncTask();
    }

    protected void LogTaskDoesntExist() {
        log.errorFormat(
                "BaseAsyncTask::LogTaskDoesntExist: Task '{0}' (Parent Command {1}, Parameters Type {2}) does not exist.",
                getTaskID(),
                (VdcActionType) (getParameters().getDbAsyncTask().getaction_type()),
                getParameters()
                        .getClass().getName());
    }

    /**
     * Print log message, Checks if the cachedStatusTask is null, (indicating the task was not found in the SPM).
     * If so returns {@link AsyncTaskStatusEnum#running} status, otherwise returns the status as given.<br>
     * <br>
     * <b>Note:</b> The task is returned as running since we need to support a case where there is a change of SPM,
     * or the SPM is just recovering from crash, and the SPM might return that it doesn't know that this task exists,
     * but in actuality it exists. If in this case {@link AsyncTaskStatusEnum#unknown} is returned then the task
     * will become a permanent zombie task since it won't be polled, so take notice if you ever want to change this
     * behavior.
     *
     * @param cachedStatusTask The status from the SPM, or <code>null</code> is the task wasn't found in the SPM.
     * @return - Updated status task
     */
    protected AsyncTaskStatus CheckTaskExist(AsyncTaskStatus cachedStatusTask) {
        AsyncTaskStatus returnedStatusTask = null;

        // If the cachedStatusTask is null ,that means the task has not been found in the SPM.
        if (cachedStatusTask == null) {
            // Set to running in order to continue polling the task in case SPM hasn't loaded the tasks yet..
            returnedStatusTask = new AsyncTaskStatus(AsyncTaskStatusEnum.running);

            if (getLastTaskStatus().getStatus() != returnedStatusTask.getStatus()) {
                log.errorFormat("SPMAsyncTask::PollTask: Task '{0}' (Parent Command {1}, Parameters Type {2}) " +
                        "was not found in VDSM, will change its status to running.",
                        getTaskID(), (VdcActionType) (getParameters().getDbAsyncTask().getaction_type()),
                        getParameters().getClass().getName());
            }
        } else {
            returnedStatusTask = cachedStatusTask;
        }
        return returnedStatusTask;
    }

    /**
     * Prints a log message of the task status,
     *
     * @param cachedStatusTask
     *            - Status got from VDSM
     */
    protected void AddLogStatusTask(AsyncTaskStatus cachedStatusTask) {

        String formatString = "SPMAsyncTask::PollTask: Polling task '{0}' (Parent Command {1}, Parameters Type {2}) "
                + "returned status '{3}'{4}.";

        // If task doesn't exist (unknown) or has ended with failure (aborting)
        // , log warn.
        if (cachedStatusTask.getTaskIsInUnusualState()) {
            log.warnFormat(
                    formatString,
                    getTaskID(),
                    (VdcActionType) (getParameters().getDbAsyncTask()
                            .getaction_type()),
                    getParameters().getClass().getName(),
                    cachedStatusTask.getStatus(),
                    ((cachedStatusTask.getStatus() == AsyncTaskStatusEnum.finished) ? (String
                            .format(", result '%1$s'",
                                    cachedStatusTask.getResult())) : ("")));
        }

        else {
            log.infoFormat(
                    formatString,
                    getTaskID(),
                    (VdcActionType) (getParameters().getDbAsyncTask()
                            .getaction_type()),
                    getParameters().getClass().getName(),
                    cachedStatusTask.getStatus(),
                    ((cachedStatusTask.getStatus() == AsyncTaskStatusEnum.finished) ? (String
                            .format(", result '%1$s'",
                                    cachedStatusTask.getResult())) : ("")));
        }
    }

    protected AsyncTaskStatus PollTask() {
        AsyncTaskStatus returnValue = null;

        try {
            Object tempVar = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SPMGetTaskStatus,
                            new SPMTaskGuidBaseVDSCommandParameters(getStoragePoolID(), getTaskID())).getReturnValue();
            returnValue = (AsyncTaskStatus) ((tempVar instanceof AsyncTaskStatus) ? tempVar : null);
        }

        catch (RuntimeException e) {
            log.error(
                    String.format(
                            "SPMAsyncTask::PollTask: Polling task '%1$s' (Parent Command %2$s, Parameters Type %3$s) threw an exception, task is still considered running.",
                            getTaskID(),
                            (VdcActionType) (getParameters().getDbAsyncTask().getaction_type()),
                            getParameters().getClass().getName()),
                    e);
        }

        if (returnValue == null) {
            log.errorFormat(
                    "SPMAsyncTask::PollTask: Polling task '{0}' (Parent Command {1}, Parameters Type {2}) failed, task is still considered running.",
                    getTaskID(),
                    (VdcActionType) (getParameters().getDbAsyncTask().getaction_type()),
                    getParameters()
                            .getClass().getName());

            AsyncTaskStatus tempVar2 = new AsyncTaskStatus();
            tempVar2.setStatus(AsyncTaskStatusEnum.running);
            returnValue = tempVar2;
        }

        String formatString =
                "SPMAsyncTask::PollTask: Polling task '{0}' (Parent Command {1}, Parameters Type {2}) returned status '{3}'{4}.";

        if (returnValue.getTaskIsInUnusualState()) {
            log.warnFormat(
                    formatString,
                    getTaskID(),
                    (VdcActionType) (getParameters().getDbAsyncTask().getaction_type()),
                    getParameters().getClass().getName(),
                    returnValue.getStatus(),
                    ((returnValue.getStatus() == AsyncTaskStatusEnum.finished) ? (String.format(", result '%1$s'",
                            returnValue.getResult())) : ("")));
        }

        else {
            log.infoFormat(
                    formatString,
                    getTaskID(),
                    (VdcActionType) (getParameters().getDbAsyncTask().getaction_type()),
                    getParameters().getClass().getName(),
                    returnValue.getStatus(),
                    ((returnValue.getStatus() == AsyncTaskStatusEnum.finished) ? (String.format(", result '%1$s'",
                            returnValue.getResult())) : ("")));
        }

        return returnValue;
    }

    public void StopTask() {
        if (getState() != AsyncTaskState.AttemptingEndAction && getState() != AsyncTaskState.Cleared
                && getState() != AsyncTaskState.ClearFailed) {
            try {
                log.infoFormat(
                        "SPMAsyncTask::StopTask: Attempting to stop task '{0}' (Parent Command {1}, Parameters Type {2}).",
                        getTaskID(),
                        (VdcActionType) (getParameters().getDbAsyncTask().getaction_type()),
                        getParameters().getClass().getName());

                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.SPMStopTask,
                                new SPMTaskGuidBaseVDSCommandParameters(getStoragePoolID(), getTaskID()));
            } catch (RuntimeException e) {
                log.error(
                        String.format("SPMAsyncTask::StopTask: Stopping task '%1$s' threw an exception.", getTaskID()),
                        e);
            } finally {
                setState(AsyncTaskState.Polling);
            }
        }
    }

    public void ClearAsyncTask() {
        VDSReturnValue vdsReturnValue = null;

        try {
            log.infoFormat("SPMAsyncTask::ClearAsyncTask: Attempting to clear task '{0}'", getTaskID());
            vdsReturnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SPMClearTask,
                            new SPMTaskGuidBaseVDSCommandParameters(getStoragePoolID(), getTaskID()));
        }

        catch (RuntimeException e) {
            log.error(String.format("SPMAsyncTask::ClearAsyncTask: Clearing task '%1$s' threw an exception.",
                    getTaskID()), e);
        }

        if (!isTaskStateError(vdsReturnValue)) {
            if (vdsReturnValue == null || !vdsReturnValue.getSucceeded()) {
                setState(AsyncTaskState.ClearFailed);
                OnTaskCleanFailure();
            } else {
                setState(AsyncTaskState.Cleared);
            }

            // In any case, remove task from DB (we don't need its info
            // anymore):
            RemoveTaskFromDB();
        }
    }

    /**
     * Function return true if we got error 410 - which is SPM initializing and
     * we did not clear the task
     *
     * @param vdsReturnValue
     * @return
     */
    private boolean isTaskStateError(VDSReturnValue vdsReturnValue) {
        if (vdsReturnValue != null && vdsReturnValue.getVdsError() != null
                && vdsReturnValue.getVdsError().getCode() == VdcBllErrors.TaskStateError) {
            log.infoFormat(
                    "SPMAsyncTask::ClearAsyncTask: At time of attemp to clear task '{0}' the response code was {2} and message was {3}. Task will not be cleaned",
                    getTaskID(),
                    vdsReturnValue.getVdsError().getCode(),
                    vdsReturnValue.getVdsError().getMessage());
            return true;
        }
        return false;
    }

    protected void OnTaskCleanFailure() {
        LogTaskCleanFailure();
    }

    protected void LogTaskCleanFailure() {
        log.errorFormat("SPMAsyncTask::ClearAsyncTask: Clearing task '{0}' failed.", getTaskID());
    }

    private static LogCompat log = LogFactoryCompat.getLog(SPMAsyncTask.class);
}
