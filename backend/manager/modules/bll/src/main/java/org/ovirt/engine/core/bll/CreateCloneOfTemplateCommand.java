package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.CreateCloneOfTemplateParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

/**
 * This command responsible to creating a copy of template image. Usually it
 * will be called during Create Vm From Template.
 */
// C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET
// attributes:
@InternalCommandAttribute
public class CreateCloneOfTemplateCommand<T extends CreateCloneOfTemplateParameters> extends
        CreateSnapshotFromTemplateCommand<T> {
    public CreateCloneOfTemplateCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected DiskImage CloneDiskImage(Guid newImageGuid) {
        DiskImage returnValue = super.CloneDiskImage(newImageGuid);
        returnValue.setstorage_id(getDestinationStorageDomainId());
        // override to have no template
        returnValue.setParentId(VmTemplateHandler.BlankVmTemplateId);
        returnValue.setit_guid(VmTemplateHandler.BlankVmTemplateId);
        if (getParameters().getDiskImageBase() != null) {
            returnValue.setvolume_type(getParameters().getDiskImageBase().getvolume_type());
            returnValue.setvolume_format(getParameters().getDiskImageBase().getvolume_format());
        }
        return returnValue;
    }

    @Override
    protected void CheckImageValidity() {
        // dont do nothing, overriding base to avoid this check
        // fails when creating vm from template on domain that the template
        // doesnt exist on
    }

    @Override
    protected boolean CreateSnapshotInIrsServer() {
        setDestinationImageId(Guid.NewGuid());
        mNewCreatedDiskImage = CloneDiskImage(getDestinationImageId());
        Guid storagePoolID = mNewCreatedDiskImage.getstorage_pool_id() != null ? mNewCreatedDiskImage
                .getstorage_pool_id().getValue() : Guid.Empty;

        try {
            VDSReturnValue vdsReturnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.CopyImage,
                            new CopyImageVDSCommandParameters(storagePoolID, getDiskImage().getstorage_id().getValue(),
                                    getVmTemplateId(), getDiskImage().getimage_group_id().getValue(), getImage()
                                            .getId(), getImageGroupId(), getDestinationImageId(),
                                    CalculateImageDescription(), getDestinationStorageDomainId(),
                                    CopyVolumeType.LeafVol, mNewCreatedDiskImage.getvolume_format(),
                                    mNewCreatedDiskImage.getvolume_type(), getDiskImage().getwipe_after_delete(),
                                    false, getStoragePool().getcompatibility_version().toString()));

            if (vdsReturnValue.getSucceeded()) {
                getReturnValue().getInternalTaskIdList().add(
                        CreateTask(vdsReturnValue.getCreationInfo(), VdcActionType.AddVmFromTemplate));
            } else {
                return false;
            }
        } catch (java.lang.Exception e) {
            log.errorFormat(
                    "CreateCloneOfTemplateCommand::CreateSnapshotInIrsServer::Failed creating snapshot from image id -'{0}'",
                    getImage().getId());
            throw new VdcBLLException(VdcBllErrors.VolumeCreationError);
        }

        return true;
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                getParameters()));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.copyImage, p, false);

        return ret;
    }

    private static LogCompat log = LogFactoryCompat.getLog(CreateCloneOfTemplateCommand.class);
}
