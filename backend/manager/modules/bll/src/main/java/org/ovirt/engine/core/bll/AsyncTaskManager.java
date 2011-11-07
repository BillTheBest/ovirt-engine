package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.exception.ExceptionUtils;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

//VB & C# TO JAVA CONVERTER NOTE: There is no Java equivalent to C# namespace aliases:
//using Timer = System.Timers.Timer;

/**
 * AsyncTaskManager: Singleton, manages all tasks in the system.
 */
public final class AsyncTaskManager {
    private static LogCompat log = LogFactoryCompat.getLog(AsyncTaskManager.class);

    // private TimerCompat _timer;
    private java.util.Map<Guid, SPMAsyncTask> _tasks;

    // Indication if _tasks has changed for logging process.
    private boolean logChangedMap = true;

    // private TimerCompat _cacheTimer;
    private final int _cacheTimeInMinutes;

    private static final AsyncTaskManager _taskManager = new AsyncTaskManager();

    public static AsyncTaskManager getInstance() {
        return _taskManager;
    }

    private AsyncTaskManager() {
        _tasks = new java.util.HashMap<Guid, SPMAsyncTask>();

        SchedulerUtil scheduler = SchedulerUtilQuartzImpl.getInstance();
        scheduler.scheduleAFixedDelayJob(this, "_timer_Elapsed", new Class[] {},
                new Object[] {}, Config.<Integer> GetValue(ConfigValues.AsyncTaskPollingRate),
                Config.<Integer> GetValue(ConfigValues.AsyncTaskPollingRate), TimeUnit.SECONDS);

        scheduler.scheduleAFixedDelayJob(this, "_cacheTimer_Elapsed", new Class[] {},
                new Object[] {}, Config.<Integer> GetValue(ConfigValues.AsyncTaskStatusCacheRefreshRateInSeconds),
                Config.<Integer> GetValue(ConfigValues.AsyncTaskStatusCacheRefreshRateInSeconds), TimeUnit.SECONDS);
        _cacheTimeInMinutes = Config.<Integer> GetValue(ConfigValues.AsyncTaskStatusCachingTimeInMinutes);
    }

    public void InitAsyncTaskManager() {

        log.info("AsyncTaskManager: Initialization of AsyncTaskManager completed successfully.");
    }

    @OnTimerMethodAnnotation("_timer_Elapsed")
    public synchronized void _timer_Elapsed() {
        if (ThereAreTasksToPoll()) {
            PollAndUpdateAsyncTasks();

            if (ThereAreTasksToPoll() && logChangedMap) {
                log.infoFormat("AsyncTaskManager::_timer_Elapsed: Finished polling Tasks, will poll again in {0} seconds.",
                               Config.<Integer> GetValue(ConfigValues.AsyncTaskPollingRate));

                // Set indication to false for not logging the same message next
                // time.
                logChangedMap = false;
            }

            // check for zombie tasks
            if (_tasks.size() > 0) {
                CleanZombieTasks();
            }
        }
    }

    @OnTimerMethodAnnotation("_cacheTimer_Elapsed")
    public synchronized void _cacheTimer_Elapsed() {
        RemoveOldAndCleanedTasks();
    }

    /**
     * Check if task should be cached or not. Task should not be cached , only
     * if the task has been rolled back (Cleared) or failed to be rolled back
     * (ClearedFailed), and been in that status for several minutes
     * (_cacheTimeInMinutes).
     *
     * @param task
     *            - Asynchronous task we check to cache or not.
     * @return - true for uncached object , and false when the object should be
     *         cached.
     */
    public synchronized boolean CachingOver(SPMAsyncTask task) {
        // Get time in milliseconds that the task should be cached
        long SubtractMinutesAsMills = TimeUnit.MINUTES
                .toMillis(_cacheTimeInMinutes);

        // check if task has been rolled back (Cleared) or failed to be rolled
        // back (ClearedFailed)
        // for SubtractMinutesAsMills of minutes.
        return (task.getState() == AsyncTaskState.Cleared || task.getState() == AsyncTaskState.ClearFailed)
                && task.getLastAccessToStatusSinceEnd() < (System
                        .currentTimeMillis() - SubtractMinutesAsMills);
    }

    public boolean HasTasksByStoragePoolId(Guid storagePoolID) {
        boolean retVal = false;
        if (_tasks != null) {
            for (SPMAsyncTask task : _tasks.values()) {
                if (task.getStoragePoolID().equals(storagePoolID)) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    private void CleanZombieTasks() {
        long maxTime = DateTime.getNow()
                .AddMinutes((-1) * Config.<Integer> GetValue(ConfigValues.AsyncTaskZombieTaskLifeInMinutes)).getTime();
        for (SPMAsyncTask task : _tasks.values()) {

            if (task.getParameters().getDbAsyncTask().getaction_parameters().getTaskStartTime() < maxTime) {
                AuditLogableBase logable = new AuditLogableBase();
                logable.AddCustomValue("CommandName", task.getParameters().getDbAsyncTask().getaction_type().toString());
                logable.AddCustomValue("Date", new java.util.Date(task.getParameters().getDbAsyncTask()
                        .getaction_parameters().getTaskStartTime()).toString());

                // if task is not finish and not unknown then it's in running
                // status
                if (task.getLastTaskStatus().getStatus() != AsyncTaskStatusEnum.finished
                        && task.getLastTaskStatus().getStatus() != AsyncTaskStatusEnum.unknown) {
                    AuditLogDirector.log(logable, AuditLogType.TASK_STOPPING_ASYNC_TASK);

                    log.infoFormat("AsyncTaskManager::CleanZombieTasks: Stoping async task {0} that started at {1}",
                            task.getParameters().getDbAsyncTask().getaction_type(), new java.util.Date(task
                                    .getParameters().getDbAsyncTask().getaction_parameters().getTaskStartTime()));

                    task.StopTask();
                } else {
                    AuditLogDirector.log(logable, AuditLogType.TASK_CLEARING_ASYNC_TASK);

                    log.infoFormat("AsyncTaskManager::CleanZombieTasks: Clearing async task {0} that started at {1}",
                            task.getParameters().getDbAsyncTask().getaction_type(), new java.util.Date(task
                                    .getParameters().getDbAsyncTask().getaction_parameters().getTaskStartTime()));

                    task.ClearAsyncTask();
                }
            }
        }
    }

    private int NumberOfTasksToPoll() {
        int retValue = 0;
        for (SPMAsyncTask task : _tasks.values()) {
            if (task.getShouldPoll()) {
                retValue++;
            }
        }

        return retValue;
    }

    private boolean ThereAreTasksToPoll() {
        for (SPMAsyncTask task : _tasks.values()) {
            if (task.getShouldPoll()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fetch all tasks statuses from each storagePoolId , and update the _tasks
     * map with the updated statuses.
     */
    private void PollAndUpdateAsyncTasks() {
        if (logChangedMap) {
            log.infoFormat("AsyncTaskManager::PollAndUpdateAsyncTasks: {0} tasks, {1} tasks to poll now",
                           _tasks.size(), NumberOfTasksToPoll());
        }

        // Fetch Set of pool id's
        Set<Guid> poolsOfActiveTasks = getPoolIdsTasks();

        // Get all tasks from all the SPMs.
        Map<Guid, Map<Guid, AsyncTaskStatus>> poolsAllTasksMap = getSPMsTasksStatuses(poolsOfActiveTasks);

        // For each task that found on each pool id
        updateTaskStatuses(poolsAllTasksMap);
    }

    /**
     * Update task status based on asyncTaskMap.
     *
     * @param asyncTaskMap
     *            - Task statuses Map fetched from VDSM.
     */
    private void updateTaskStatuses(
                                    Map<Guid, Map<Guid, AsyncTaskStatus>> poolsAllTasksMap) {
        for (SPMAsyncTask task : _tasks.values()) {
            if (task.getShouldPoll()) {
                Map<Guid, AsyncTaskStatus> asyncTasksForPoolMap = poolsAllTasksMap
                        .get(task.getStoragePoolID());

                // If the storage pool id exists
                if (asyncTasksForPoolMap != null) {
                    AsyncTaskStatus cachedAsyncTaskStatus = asyncTasksForPoolMap
                            .get(task.getTaskID());

                    // task found in VDSM.
                    task.UpdateTask(cachedAsyncTaskStatus);
                }
            }
        }

    }

    /**
     * Call VDSCommand for each pool id fetched from poolsOfActiveTasks , and
     * Initialize a map with each storage pool Id task statuses.
     *
     * @param poolsOfActiveTasks
     *            - Set of all the active tasks fetched from _tasks.
     * @return poolsAsyncTaskMap - Map which contains tasks for each storage
     *         pool id.
     */
    @SuppressWarnings("unchecked")
    private Map<Guid, Map<Guid, AsyncTaskStatus>> getSPMsTasksStatuses(Set<Guid> poolsOfActiveTasks) {
        Map<Guid, Map<Guid, AsyncTaskStatus>> poolsAsyncTaskMap = new HashMap<Guid, Map<Guid, AsyncTaskStatus>>();

        // For each pool Id (SPM) ,add its tasks to the map.
        for (Guid storagePoolID : poolsOfActiveTasks) {
            try {
                poolsAsyncTaskMap.put(storagePoolID, new HashMap<Guid, AsyncTaskStatus>(
                        (Map<Guid, AsyncTaskStatus>) Backend.getInstance().getResourceManager().RunVdsCommand(
                                VDSCommandType.SPMGetAllTasksStatuses,
                                new IrsBaseVDSCommandParameters(storagePoolID)).getReturnValue()));
            } catch (RuntimeException e) {
                if ((e instanceof VdcBLLException) &&
                        (((VdcBLLException) e).getErrorCode() == VdcBllErrors.VDS_NETWORK_ERROR)) {
                    log.debugFormat("AsyncTaskManager::getSPMsTasksStatuses: Calling Command {1}{2}, " +
                            "with storagePoolId {3}) threw an exception.",
                            VDSCommandType.SPMGetAllTasksStatuses, "VDSCommand", storagePoolID);
                } else {
                    log.debugFormat("AsyncTaskManager::getSPMsTasksStatuses: Calling Command {1}{2}, " +
                            "with storagePoolId {3}) threw an exception.",
                            VDSCommandType.SPMGetAllTasksStatuses, "VDSCommand", storagePoolID, e);
                }
            }
        }

        return poolsAsyncTaskMap;
    }

    /**
     * Get a Set of all the storage pool id's of tasks that should pool.
     *
     * @see org.ovirt.engine.core.bll.SPMAsyncTask#getShouldPoll()
     * @return - Set of active tasks.
     */
    private Set<Guid> getPoolIdsTasks() {
        Set<Guid> poolsOfActiveTasks = new HashSet<Guid>();

        for (SPMAsyncTask task : _tasks.values()) {
            if (task.getShouldPoll()) {
                poolsOfActiveTasks.add(task.getStoragePoolID());
            }
        }
        return poolsOfActiveTasks;
    }

    /**
     * get list of pools that have only cleared and old tasks (which don't exist
     * anymore in the manager):
     *
     * @return
     */
    private Set<Guid> removeClearedAndOldTasks() {
        Set<Guid> poolsOfActiveTasks = new HashSet<Guid>();
        Set<Guid> poolsOfClearedAndOldTasks = new HashSet<Guid>();
        Map<Guid, SPMAsyncTask> activeTaskMap = new java.util.HashMap<Guid, SPMAsyncTask>();
        for (SPMAsyncTask task : _tasks.values()) {
            if (!CachingOver(task)) {
                activeTaskMap.put(task.getTaskID(), task);
                poolsOfActiveTasks.add(task.getStoragePoolID());
            } else {
                poolsOfClearedAndOldTasks.add(task.getStoragePoolID());
            }
        }

        // Check if _tasks need to be updated with less tasks (activated tasks).
        SetNewMap(activeTaskMap);

        poolsOfClearedAndOldTasks.removeAll(poolsOfActiveTasks);
        return poolsOfClearedAndOldTasks;
    }

    private void RemoveOldAndCleanedTasks() {

        Set<Guid> poolsOfClearedAndOldTasks = removeClearedAndOldTasks();

        for (Guid storagePoolID : poolsOfClearedAndOldTasks) {
            log.infoFormat("AsyncTaskManager::RemoveOldAndCleanedTasks: Cleared all tasks of pool {0}.",
                    storagePoolID);
            storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().get(storagePoolID);
            if (storagePool != null && storagePool.getspm_vds_id() != null) {
                VDS vds = DbFacade.getInstance().getVdsDAO().get(storagePool.getspm_vds_id());
                if (vds != null && vds.getstatus() == VDSStatus.NonOperational) {
                    log.infoFormat(
                            "AsyncTaskManager::RemoveOldAndCleanedTasks: vds {0} is spm and non-operational, calling SetNonOperationalVds",
                            vds.getvds_name());
                    SetNonOperationalVdsParameters tempVar = new SetNonOperationalVdsParameters(vds.getvds_id(),
                            NonOperationalReason.GENERAL);
                    tempVar.setSaveToDb(true);
                    tempVar.setShouldBeLogged(false);
                    Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds, tempVar);
                } else {
                    log.info("AsyncTaskManager::RemoveOldAndCleanedTasks: could not find vds that is spm and non-operational.");
                }
            }
        }
    }

    private void AddTaskToManager(SPMAsyncTask task) {
        if (task == null) {
            log.error("AsyncTaskManager::AddTaskToManager: Cannot add a null task.");
        }

        else {
            if (!_tasks.containsKey(task.getTaskID())) {
                log.infoFormat(
                        "AsyncTaskManager::AddTaskToManager: Adding task '{0}' (Parent Command {1}, Parameters Type {2}), {3}.",
                        task.getTaskID(),
                        (task.getParameters().getDbAsyncTask().getaction_type()),
                        task.getParameters().getClass().getName(),
                        (task.getShouldPoll() ? "polling started."
                                : "polling hasn't started yet."));

                // Set the indication to true for logging _tasks status on next
                // quartz execution.
                AddTaskToMap(task.getTaskID(), task);
            }

            // Not needed (GREGM)
            // if (!_timer.getEnabled() && task.getShouldPoll())
            // {
            // log.info("AsyncTaskManager::AddTaskToManager: Added a task that we should poll - starting timer.");
            // _timer.setEnabled(true);
            // }
            else {
                SPMAsyncTask existingTask = _tasks.get(task.getTaskID());
                if (existingTask.getParameters().getDbAsyncTask().getaction_type() == VdcActionType.Unknown
                        && task.getParameters().getDbAsyncTask().getaction_type() != VdcActionType.Unknown) {
                    log.infoFormat(
                            "AsyncTaskManager::AddTaskToManager: Task '{0}' already exists with action type 'Unknown', now overriding it with action type '{1}'",
                            task.getTaskID(),
                            task.getParameters().getDbAsyncTask().getaction_type());

                    // Set the indication to true for logging _tasks status on
                    // next quartz execution.
                    AddTaskToMap(task.getTaskID(), task);
                }
            }
        }
    }

    private java.util.ArrayList<EntityAsyncTask> GetEntityTasks(Guid id) {
        java.util.ArrayList<EntityAsyncTask> returnValue = new java.util.ArrayList<EntityAsyncTask>();
        for (SPMAsyncTask task : _tasks.values()) {
            if (task instanceof EntityAsyncTask) {
                EntityAsyncTask entityTask = (EntityAsyncTask) task;
                if (id.equals(entityTask.getContainerId())) {
                    returnValue.add(entityTask);
                }
            }
        }

        return returnValue;
    }

    /**
     * Adds new task to _tasks map , and set the log status to true. We set the
     * indication to true for logging _tasks status on next quartz execution.
     *
     * @param guid
     *            - Key of the map.
     * @param asyncTask
     *            - Value of the map.
     */
    private void AddTaskToMap(Guid guid, SPMAsyncTask asyncTask) {
        _tasks.put(guid, asyncTask);
        logChangedMap = true;
    }

    /**
     * Check if the maps are equal , if not ,set asyncTaskMap as _tasks map. We
     * set the indication to true when _tasks map changes for logging _tasks
     * status on next quartz execution.
     *
     * @param asyncTaskMap
     *            - Map to copy to _tasks map.
     */
    private void SetNewMap(Map<Guid, SPMAsyncTask> asyncTaskMap) {
        // Check if maps representing the same mapping.
        if (!_tasks.equals(asyncTaskMap)) {
            // If not the same set _tasks to be as asyncTaskMap.
            _tasks = asyncTaskMap;

            // Set the indication to true for logging.
            logChangedMap = true;

            // Log tasks to poll now.
            log.infoFormat("AsyncTaskManager::SetNewMap: The map contains now {0} tasks", _tasks.size());
        }
    }

    public synchronized Guid CreateTask(AsyncTaskType taskType, AsyncTaskParameters taskParameters,
                                        boolean pollingEnabled) {
        SPMAsyncTask task = AsyncTaskFactory.Construct(taskType, taskParameters);
        AddTaskToManager(task);
        return task == null ? Guid.Empty : task.getTaskID();
    }

    public synchronized void UpdateTaskWithActionParameters(Guid taskID, VdcActionParametersBase actionParameters) {
        if (_tasks.containsKey(taskID)) {
            async_tasks currentDbAsyncTask = _tasks.get(taskID).getParameters().getDbAsyncTask();
            currentDbAsyncTask.setaction_parameters(actionParameters);
            _tasks.get(taskID).UpdateAsyncTask();
        }
    }

    public synchronized void StartPollingTask(Guid taskID) {
        if (_tasks.containsKey(taskID)) {
                _tasks.get(taskID).StartPollingTask();
        }
    }

    public synchronized java.util.ArrayList<AsyncTaskStatus> PollTasks(java.util.ArrayList<Guid> taskIdList) {
        java.util.ArrayList<AsyncTaskStatus> returnValue = new java.util.ArrayList<AsyncTaskStatus>();

        if (taskIdList != null && taskIdList.size() > 0) {
            for (Guid taskId : taskIdList) {
                if (_tasks.containsKey(taskId)) {
                    // task is still running or is still in the cache:
                    _tasks.get(taskId).setLastStatusAccessTime();
                    returnValue.add(_tasks.get(taskId).getLastTaskStatus());
                }

                else
                // task doesn't exist in the manager (shouldn't happen) ->
                // assume it has been ended successfully.
                {
                    log.warnFormat(
                            "AsyncTaskManager::PollTasks: task ID '{0}' doesn't exist in the manager -> assuming 'finished'.",
                            taskId);

                    AsyncTaskStatus tempVar = new AsyncTaskStatus();
                    tempVar.setStatus(AsyncTaskStatusEnum.finished);
                    tempVar.setResult(AsyncTaskResultEnum.success);
                    returnValue.add(tempVar);
                }
            }
        }

        return returnValue;
    }

    /**
     * Retrieves from the specified storage pool the tasks that exist on it and
     * adds them to the manager.
     *
     * @param sp
     *            the storage pool to retrieve running tasks from
     */
    public synchronized void AddStoragePoolExistingTasks(storage_pool sp) {
        java.util.ArrayList<AsyncTaskCreationInfo> currPoolTasks = null;
        try {
            currPoolTasks = (java.util.ArrayList<AsyncTaskCreationInfo>) Backend.getInstance().getResourceManager()
                    .RunVdsCommand(VDSCommandType.SPMGetAllTasksInfo, new IrsBaseVDSCommandParameters(sp.getId()))
                    .getReturnValue();
        } catch (RuntimeException e) {
            log.error(
                    String.format(
                            "AsyncTaskManager::AddStoragePoolExistingTasks: Getting existing tasks on Storage Pool %1$s failed.",
                            sp.getname()),
                    e);
        }

        if (currPoolTasks != null && currPoolTasks.size() > 0) {
            java.util.ArrayList<Guid> newlyAddedTaskIDs = new java.util.ArrayList<Guid>();

            for (AsyncTaskCreationInfo creationInfo : currPoolTasks) {
                creationInfo.setStoragePoolID(sp.getId());
                if (!_tasks.containsKey(creationInfo.getTaskID())) {
                    try {
                        SPMAsyncTask task = AsyncTaskFactory.Construct(creationInfo);
                        AddTaskToManager(task);
                        newlyAddedTaskIDs.add(task.getTaskID());
                    } catch (Exception e) {
                        log.errorFormat("Failed to load task of type {0} with id {1}, due to: {2}.",
                                       creationInfo.getTaskType(), creationInfo.getTaskID(),
                                       ExceptionUtils.getRootCauseMessage(e));
                    }
                }
            }

            for (Guid taskID : newlyAddedTaskIDs) {
                StartPollingTask(taskID);
            }

            log.infoFormat(
                    "AsyncTaskManager::AddStoragePoolExistingTasks: Discovered {0} tasks on Storage Pool '{1}', {2} added to manager.",
                    currPoolTasks.size(),
                    sp.getname(),
                    newlyAddedTaskIDs.size());
        }

        else {
            log.infoFormat("AsyncTaskManager::AddStoragePoolExistingTasks: Discovered no tasks on Storage Pool {0}",
                    sp.getname());
        }
    }

    /**
     * Retrieves all tasks from the specified storage pool and stops them.
     *
     * @param sp
     */
    public synchronized void StopStoragePoolTasks(final storage_pool sp) {
        log.infoFormat("AsyncTaskManager::StopStoragePoolTask: Attempting to get and stop tasks on storage pool '{0}'",
                sp.getname());

        AddStoragePoolExistingTasks(sp);

        // LINQ 29456
        // foreach (SPMAsyncTask asyncTask in _tasks.Values.Where(a =>
        // a.StoragePoolID == sp.id))
        // {
        // asyncTask.StopTask();
        // }

        List<SPMAsyncTask> list = LinqUtils.filter(_tasks.values(), new Predicate<SPMAsyncTask>() {
            @Override
            public boolean eval(SPMAsyncTask a) {
                return a.getStoragePoolID().equals(sp.getId());
            }
        });
        for (SPMAsyncTask task : list) {
            task.StopTask();
        }
    }

    /**
     * Stops all tasks, and set them to polling state, for clearing them up later.
     *
     * @param taskList
     *            - List of tasks to stop.
     */
    public synchronized void CancelTasks(List<Guid> taskList) {
        for (Guid taskID : taskList) {
            CancelTask(taskID);
        }
    }

    public synchronized void CancelTask(Guid taskID) {
        if (_tasks.containsKey(taskID)) {
            log.infoFormat("AsyncTaskManager::CancelTask: Attempting to cancel task '{0}'.", taskID);
            _tasks.get(taskID).StopTask();
            _tasks.get(taskID).ConcreteStartPollingTask();
            // Not needed (GREGM)
            // if (_tasks.get(taskID).getShouldPoll() && !_timer.getEnabled())
            // {
            // log.info("AsyncTaskManager::CancelTask: Starting timer");
            // _timer.setEnabled(true);
            // }
        }
    }

    public synchronized boolean EntityHasTasks(Guid id) {
        java.util.ArrayList<EntityAsyncTask> entityAsyncTasks = GetEntityTasks(id);
        for (EntityAsyncTask task : entityAsyncTasks) {
            if (task.getState() != AsyncTaskState.Cleared && task.getState() != AsyncTaskState.ClearFailed) {
                return true;
            }
        }

        return false;
    }

    public synchronized boolean StoragePoolHasUnclearedTasks(Guid storagePoolId) {
        for (SPMAsyncTask task : _tasks.values()) {
            if (task.getState() != AsyncTaskState.Cleared && task.getState() != AsyncTaskState.ClearFailed
                    && task.getStoragePoolID().equals(storagePoolId)) {
                return true;
            }
        }

        return false;
    }

}
