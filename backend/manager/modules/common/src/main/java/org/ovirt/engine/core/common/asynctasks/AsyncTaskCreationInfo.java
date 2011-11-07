package org.ovirt.engine.core.common.asynctasks;

import org.ovirt.engine.core.compat.*;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//    @XmlAccessorType(XmlAccessType.NONE)
//    @XmlType(name="AsyncTaskCreationInfo")
public class AsyncTaskCreationInfo {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // @XmlElement(name="TaskID")
    private Guid privateTaskID = new Guid();

    public Guid getTaskID() {
        return privateTaskID;
    }

    public void setTaskID(Guid value) {
        privateTaskID = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // @XmlElement(name="TaskType")
    private AsyncTaskType privateTaskType = AsyncTaskType.forValue(0);

    public AsyncTaskType getTaskType() {
        return privateTaskType;
    }

    public void setTaskType(AsyncTaskType value) {
        privateTaskType = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // @XmlElement(name="StoragePoolID")
    private Guid privateStoragePoolID = new Guid();

    public Guid getStoragePoolID() {
        return privateStoragePoolID;
    }

    public void setStoragePoolID(Guid value) {
        privateStoragePoolID = value;
    }

    public AsyncTaskCreationInfo(Guid taskID, AsyncTaskType taskType, Guid storagePoolID) {
        setTaskID(taskID);
        setTaskType(taskType);
        setStoragePoolID(storagePoolID);
    }

    public AsyncTaskCreationInfo() {
    }

}
