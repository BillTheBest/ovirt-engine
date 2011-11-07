package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.command.utils.StorageDomainSpaceChecker;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.HibernateVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HibernateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class HibernateVmCommand<T extends HibernateVmParameters> extends VmOperationCommandBase<T> {
    private boolean isHibernateVdsProblematic = false;
    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected HibernateVmCommand(Guid commandId) {
        super(commandId);
    }

    public HibernateVmCommand(T parameters) {
        super(parameters);
        super.setStoragePoolId(getVm().getstorage_pool_id());
        parameters.setEntityId(getVm().getvm_guid());
    }

    private Guid _storageDomainId = Guid.Empty;

    @Override
    public NGuid getStorageDomainId() {
        if (_storageDomainId.equals(Guid.Empty) && getVm() != null) {
            VmHandler.updateDisksFromDb(getVm());
            if (getVm().getDiskMap().size() > 0) {
                // LINQ 29456
                // _storageDomainId =
                // Vm.DiskMap.Values.First().storage_id.Value;
                _storageDomainId = LinqUtils.first(getVm().getDiskMap().values()).getstorage_id().getValue();
            } else {
                List<storage_domain_static> domainsInPool = DbFacade.getInstance()
                        .getStorageDomainStaticDAO().getAllForStoragePool(getVm().getstorage_pool_id());
                if (domainsInPool.size() > 0) {
                    for (storage_domain_static currDomain : domainsInPool) {
                        if (currDomain.getstorage_domain_type().equals(StorageDomainType.Master)
                                || currDomain.getstorage_domain_type().equals(StorageDomainType.Data)) {
                            _storageDomainId = currDomain.getId();
                            break;
                        }
                    }
                }
            }
        }
        return _storageDomainId;
    }

    @Override
    protected void Perform() {
        // Set the VM to null, to fetch it again from the DB ,instead from the cache.
        // We want to get the VM state from the DB, to avoid multi requests for VM hibernation.
        setVm(null);
        if (VM.isStatusUp(getVm().getstatus())) {

            TransactionSupport.executeInNewTransaction(
                    new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            getCompensationContext().snapshotEntityStatus(getVm().getDynamicData(), getVm().getstatus());

                            // Set the VM to SavingState to lock the VM,to avoid situation of multi VM hibernation.
                            getVm().setstatus(VMStatus.SavingState);

                            Backend.getInstance()
                                    .getResourceManager()
                                    .RunVdsCommand(VDSCommandType.UpdateVmDynamicData,
                                            new UpdateVmDynamicDataVDSCommandParameters(getVdsId(),
                                                    getVm().getDynamicData()));
                            getCompensationContext().stateChanged();
                            return null;
                        }
                    });

            Guid image1GroupId = Guid.NewGuid();
            // this is temp code until SPM will implement the new verb that does
            // it for us:

            Guid hiberVol1 = Guid.NewGuid();
            VDSReturnValue ret1 =
                    Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.CreateImage,
                                    new CreateImageVDSCommandParameters(
                                            getVm().getstorage_pool_id(),
                                            getStorageDomainId().getValue(),
                                            image1GroupId,
                                            getImageSizeInBytes(),
                                            getVolumeType(),
                                            VolumeFormat.RAW,
                                            DiskType.Data,
                                            hiberVol1,
                                            "",
                                            getStoragePool().getcompatibility_version().toString()));

            if (!ret1.getSucceeded()) {
                return;
            }
            Guid guid1 = CreateTask(ret1.getCreationInfo(), VdcActionType.HibernateVm);
            getReturnValue().getTaskIdList().add(guid1);

            // second vol should be 10kb
            Guid image2GroupId = Guid.NewGuid();

            Guid hiberVol2 = Guid.NewGuid();
            VDSReturnValue ret2 =
                    Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.CreateImage,
                                    new CreateImageVDSCommandParameters(getVm().getstorage_pool_id(),
                                            getStorageDomainId()
                                                    .getValue(),
                                            image2GroupId,
                                            getMetaDataSizeInBytes(),
                                            VolumeType.Sparse,
                                            VolumeFormat.COW,
                                            DiskType.Data,
                                            hiberVol2,
                                            "",
                                            getStoragePool().getcompatibility_version()
                                                    .toString()));

            if (!ret2.getSucceeded()) {
                return;
            }
            Guid guid2 = CreateTask(ret2.getCreationInfo(), VdcActionType.HibernateVm);
            getReturnValue().getTaskIdList().add(guid2);

            // this is the new param that should be passed to the hibernate
            // command
            getVm().sethibernation_vol_handle(
                    String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s", getStorageDomainId().toString(), getVm()
                            .getstorage_pool_id().toString(), image1GroupId.toString(), hiberVol1.toString(),
                            image2GroupId.toString(), hiberVol2.toString()));
            // end of temp code

            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.UpdateVmDynamicData,
                            new UpdateVmDynamicDataVDSCommandParameters(getVdsId(),
                                    getVm().getDynamicData()));

            getParameters().setTaskIds(new java.util.ArrayList<Guid>());
            getParameters().getTaskIds().add(guid1);
            getParameters().getTaskIds().add(guid2);

            setSucceeded(true);
        }
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                getParameters()));
        p.setEntityId(getParameters().getEntityId());
        Guid taskID = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.createVolume, p, false);

        return taskID;
    }

    protected HibernateVmParameters getHibernateVmParams() {
        VdcActionParametersBase tempVar = getParameters();
        return (HibernateVmParameters) ((tempVar instanceof HibernateVmParameters) ? tempVar : null);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getHibernateVmParams().getAutomaticSuspend() ? getSucceeded() ? AuditLogType.AUTO_SUSPEND_VM
                    : AuditLogType.AUTO_FAILED_SUSPEND_VM : getSucceeded() ? AuditLogType.USER_SUSPEND_VM
                    : AuditLogType.USER_FAILED_SUSPEND_VM;

        case END_SUCCESS:
            return getHibernateVmParams().getAutomaticSuspend() ? getSucceeded() ? AuditLogType.AUTO_SUSPEND_VM_FINISH_SUCCESS
                    : AuditLogType.AUTO_SUSPEND_VM_FINISH_FAILURE
                    : getSucceeded() ? AuditLogType.USER_SUSPEND_VM_FINISH_SUCCESS
                            : isHibernateVdsProblematic ? AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE_WILL_TRY_AGAIN : AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE;

        default:
            return getHibernateVmParams().getAutomaticSuspend() ? AuditLogType.AUTO_SUSPEND_VM_FINISH_FAILURE
                    : isHibernateVdsProblematic ? AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE_WILL_TRY_AGAIN : AuditLogType.USER_SUSPEND_VM_FINISH_FAILURE;
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getVm() == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        // else if (IrsClusterMonitor.Instance.DiskFreePercent <
        // Config.FreeSpaceLow)
        // {
        // retValue = false;
        // ReturnValue.CanDoActionMessages.Add(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW.toString());
        // }
        else if (getStorageDomainId().equals(Guid.Empty)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
            retValue = false;
        } else {
            if (getVm().getstatus() == VMStatus.WaitForLaunch || getVm().getstatus() == VMStatus.NotResponding) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
            } else if (getVm().getstatus() != VMStatus.Up) {
                retValue = false;
                getReturnValue().getCanDoActionMessages()
                        .add(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_UP.toString());
            } else {
                if (AsyncTaskManager.getInstance().EntityHasTasks(getVmId())) {
                    retValue = false;
                    addCanDoActionMessage(VdcBllMessages.VM_CANNOT_SUSPENDE_HAS_RUNNING_TASKS);
                }
                if (retValue) {
                    // check if vm has stateless images in db in case vm was run once as stateless
                    // (then is_stateless is false)
                    if (getVm().getis_stateless() ||
                            !DbFacade.getInstance()
                                    .getDiskImageDAO()
                                    .getAllStatelessVmImageMapsForVm(getVmId())
                                    .isEmpty()) {
                        retValue = false;
                        addCanDoActionMessage(VdcBllMessages.VM_CANNOT_SUSPEND_STATELESS_VM);
                    } else if (DbFacade.getInstance().getVmPoolDAO().getVmPoolMapByVmGuid(getVmId()) != null) {
                        retValue = false;
                        addCanDoActionMessage(VdcBllMessages.VM_CANNOT_SUSPEND_VM_FROM_POOL);
                    }

                    // Check storage before trying to create Images for hibernation.
                    storage_domains domain =
                            DbFacade.getInstance().getStorageDomainDAO().get(getStorageDomainId().getValue());
                    if (retValue
                            && !StorageDomainSpaceChecker.hasSpaceForRequest(domain, (getImageSizeInBytes()
                                    + getMetaDataSizeInBytes())/BYTES_IN_GB)) {
                        retValue = false;
                        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW);
                    }
                }
            }
        }

        if (!retValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__HIBERNATE);
        }
        return retValue;
    }

    @Override
    protected void EndSuccessfully() {
        if (getVm() != null) {
            if (getVm().getstatus() != VMStatus.SavingState && getVm().getstatus() != VMStatus.Up) {
                // If the Vm is not in SavingState/Up status, we shouldn't
                // perform Hibernate on it,
                // since if the Vm is in another status, something might have
                // happend to it
                // that might prevent it from being hibernated.

                // NOTE: We don't remove the 2 volumes because we don't want to
                // start here
                // another tasks.

                log.warnFormat(
                        "HibernateVmCommand::EndSuccessfully: Vm '{0}' is not in 'SavingState'/'Up' status, but in '{1}' status - not performing Hibernate.",
                        getVm().getvm_name(),
                        getVm().getstatus());
                getReturnValue().setEndActionTryAgain(false);
            }

            else if (getVm().getrun_on_vds() == null) {
                log.warnFormat(
                        "HibernateVmCommand::EndSuccessfully: Vm '{0}' doesn't have 'run_on_vds' value - cannot Hibernate.",
                        getVm().getvm_name());
                getReturnValue().setEndActionTryAgain(false);
            }

            else {
                String hiberVol = getVm().gethibernation_vol_handle();
                if (hiberVol != null) {
                    try {
                        Backend.getInstance()
                                .getResourceManager()
                                .RunVdsCommand(
                                        VDSCommandType.Hibernate,
                                        new HibernateVDSCommandParameters(new Guid(getVm().getrun_on_vds().toString()),
                                                getVmId(), getVm().gethibernation_vol_handle()));
                    } catch (VdcBLLException e) {
                        isHibernateVdsProblematic = true;
                        throw e;
                    }
                    setSucceeded(true);
                } else {
                    log.errorFormat("hibernation volume of VM '{0}', is not initialized.", getVm().getvm_name());
                    EndWithFailure();
                }
            }
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("HibernateVmCommand::EndSuccessfully: Vm is null - not performing full EndAction.");
            setSucceeded(true);
        }
    }

    @Override
    protected void EndWithFailure() {
        if (getVm() != null) {
            RevertTasks();
            if (getVm().getrun_on_vds() != null) {
                getVm().sethibernation_vol_handle(null);
                getVm().setstatus(VMStatus.Up);

                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.UpdateVmDynamicData,
                                new UpdateVmDynamicDataVDSCommandParameters(
                                        new Guid(getVm().getrun_on_vds().toString()), getVm().getDynamicData()));

                setSucceeded(true);
            }

            else {
                log.warnFormat(
                        "HibernateVmCommand::EndWithFailure: Vm '{0}' doesn't have 'run_on_vds' value - not clearing 'hibernation_vol_handle' info.",
                        getVm().getvm_name());

                getReturnValue().setEndActionTryAgain(false);
            }
        }

        else {
            setCommandShouldBeLogged(false);
            log.warn("HibernateVmCommand::EndWithFailure: Vm is null - not performing full EndAction.");
            setSucceeded(true);
        }
    }

    /**
     * Returns whether to use Sparse or Preallocation. If the storage type is file system devices ,it would be more
     * efficient to use Sparse allocation. Otherwise for block devices we should use Preallocated for faster allocation.
     *
     * @return - VolumeType of allocation type to use.
     */
    private VolumeType getVolumeType() {
        return (getStoragePool().getstorage_pool_type() == StorageType.NFS || getStoragePool().getstorage_pool_type() == StorageType.LOCALFS) ? VolumeType.Sparse
                : VolumeType.Preallocated;
    }

    /**
     * Returns the memory size should be allocated in the storage.
     *
     * @return - Memory size for allocation in bytes.
     */
    private long getImageSizeInBytes() {
        return (long) (getVm().getvm_mem_size_mb() + 200 + (64 * getVm().getnum_of_monitors())) * 1024 * 1024;
    }

    /**
     * Returns the meta data that should be allocated when saving state of image.
     *
     * @return - Meta data size for allocation in bytes.
     */
    private long getMetaDataSizeInBytes() {
        return (long) 10 * 1024;
    }

    private static LogCompat log = LogFactoryCompat.getLog(HibernateVmCommand.class);
}
