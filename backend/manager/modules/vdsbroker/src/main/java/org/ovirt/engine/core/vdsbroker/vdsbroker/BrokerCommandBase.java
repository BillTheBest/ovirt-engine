package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.vdsbroker.VDSCommandBase;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSErrorException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSNoMasterDomainException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSNonOperationalException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IRSUnicodeArgumentException;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsOperationFailedNoFailoverException;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public abstract class BrokerCommandBase<P extends VDSParametersBase> extends VDSCommandBase<P> {
    public BrokerCommandBase(P parameters) {
        super(parameters);
    }

    protected StatusOnlyReturnForXmlRpc status;

    protected StatusForXmlRpc getReturnStatus() {
        return status.mStatus;
    }

    protected void InitializeVdsError(VdcBllErrors returnStatus) {
        VDSError tempVar = new VDSError();
        tempVar.setCode(returnStatus);
        tempVar.setMessage(getReturnStatus().mMessage);
        getVDSReturnValue().setVdsError(tempVar);
    }

    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        VDSExceptionBase outEx;
        switch (returnStatus) {
        case Done:
            return;
        case recovery:
            outEx = new VDSRecoveringException(returnStatus, getReturnStatus().mMessage);
            break;
        case SpmStatusError:
            outEx = new IRSNonOperationalException(getReturnStatus().mMessage);
            break;
        case StoragePoolMasterNotFound:
        case StoragePoolTooManyMasters:
        case StoragePoolWrongMaster:
        case StoragePoolHasPotentialMaster:
        case StorageDomainMasterError:
            outEx = new IRSNoMasterDomainException(getReturnStatus().mMessage);
            break;
        case UnicodeArgumentException:
            outEx = new IRSUnicodeArgumentException(getReturnStatus().mMessage);
            break;
        case TooManyDomainsInStoragePoolError:
        case StorageDomainAlreadyAttached:
        case StorageDomainDescriptionTooLongError:
        case TooManyPVsInVG:
        case createIllegalVolumeSnapshotError:
        case prepareIllegalVolumeError:
        case createVolumeRollbackError:
        case InvalidParameterException:
        case InvalidDefaultExceptionException:
        case NotImplementedException:
        case OperationInProgress:
        case MiscDirCleanupFailure:
        case createVolumeSizeError:
        case IncorrectFormat:
        case VolumeIsBusy:
        case VolumeImageHasChildren:
        case VolumeUnlinkError:
        case OrphanVolumeError:
        case VolumeAlreadyExists:
        case VolumeNonWritable:
        case VolumeNonShareable:
        case VolumeCannotGetParent:
        case SharedVolumeNonWritable:
        case InternalVolumeNonWritable:
        case CannotDeleteSharedVolume:
        case NonLeafVolumeNotWritable:
        case ImagesActionError:
        case ImageValidationError:
        case ImageDeleteError:
        case ImageIsNotEmpty:
        case ImageIsNotLegalChain:
        case OverwriteImageError:
        case MoveTemplateImageError:
        case StoragePoolDisconnectionError:
        case StoragePoolAlreadyExists:
        case IsoCannotBeMasterDomain:
        case CannotConnectMultiplePools:
        case BackupCannotBeMasterDomain:
        case StoragePoolConnected:
        case StoragePoolDescriptionTooLongError:
        case StorageDomainNotInPool:
        case StorageDomainNotEmpty:
        case StorageDomainMetadataCreationError:
        case StorageDomainMetadataFileMissing:
        case StorageDomainMetadataNotFound:
        case StorageDomainAlreadyExists:
        case StorageDomainMasterUnmountError:
        case BlockStorageDomainMasterFSCKError:
        case StorageDomainLayoutError:
        case StorageDomainTypeError:
        case StorageDomainNotMemberOfPool:
        case StorageDomainStatusError:
        case StorageDomainCheckError:
        case StorageDomainTypeNotBackup:
        case StorageDomainStateTransitionIllegal:
        case StorageDomainActive:
        case CannotDetachMasterStorageDomain:
        case StorageDomainInsufficientPermissions:
        case StorageDomainClassError:
        case StorageDomainIsMadeFromTooManyPVs:
        case InvalidTask:
        case UnknownTask:
        case TaskClearError:
        case TaskNotFinished:
        case InvalidTaskType:
        case AddTaskError:
        case TaskInProgress:
        case TaskStateError:
        case TaskAborted:
        case TaskPersistError:
        case InvalidJob:
        case InvalidRecovery:
        case InvalidTaskMng:
        case TaskStateTransitionError:
        case TaskHasRefs:
        case VolumeGroupSizeError:
        case VolumeGroupAlreadyExistsError:
        case VolumeGroupUninitialized:
        case VolumeGroupHasDomainTag:
        case CannotDeactivateLogicalVolume:
        case CannotActivateLogicalVolume:
        case LogicalVolumePermissionsError:
        case LogicalVolumeAlreadyExists:
        case PartitionedPhysDev:
        case DomainAlreadyLocked:
        case DomainLockDoesNotExist:
        case MetaDataKeyError:
        case MetaDataSealIsBroken:
        case MetaDataValidationError:
        case MetaDataMappingError:
        case MetaDataParamError:
        case MetadataOverflowError:
        case ImportUnknownType:
        case ExportError:
        case MergeVolumeRollbackError:
        case ActionStopped:
        case FAILED_CHANGE_CD_IS_MOUNTED:
        case UnsupportedDomainVersion:
        case CurrentVersionTooAdvancedError:
        case iSCSILogoutError:
        case iSCSIDiscoveryError:
        case ISCSI_LOGIN_AUTH_ERROR:
        case PoolUpgradeInProgress:
        case MixedSDVersionError:
        case NoSpaceLeftOnDomain:
        case ImageDoesNotExistInDomainError:
        case NO_IMPLEMENTATION:
        case VOLUME_WAS_NOT_PREPARED_BEFORE_TEARDOWN:
        case IMAGES_NOT_SUPPORTED_ERROR:
        case GET_FILE_LIST_ERROR:
        case STORAGE_DOMAIN_REFRESH_ERROR:
        case VOLUME_GROUP_BLOCK_SIZE_ERROR:
        case MIGRATION_DEST_INVALID_HOSTNAME:
        case DEVICE_BLOCK_SIZE_NOT_SUPPORTED:
            if (this instanceof IrsBrokerCommand) {
                outEx = new IrsOperationFailedNoFailoverException(getReturnStatus().mMessage);
            } else {
                outEx = new VDSErrorException(String.format("Failed in vdscommand to %1$s, error = %2$s",
                        getCommandName(), getReturnStatus().mMessage));
            }
            break;
        case ERR_BAD_ADDR:
            outEx = new VDSNetworkException(getReturnStatus().mMessage);
            break;
        default:
            log.errorFormat("Failed in {0} method", getCommandName());
            if (this instanceof IrsBrokerCommand) {
                outEx = new IRSErrorException(String.format("Failed to %1$s, error = %2$s", getCommandName(),
                        getReturnStatus().mMessage));
            } else {
                outEx = new VDSErrorException(String.format("Failed to %1$s, error = %2$s", getCommandName(),
                        getReturnStatus().mMessage));
            }
            log.errorFormat("Error code {0} and error message {1}", returnStatus, outEx.getMessage());
            break;
        }
        VDSError tempVar = new VDSError();
        tempVar.setCode(returnStatus);
        tempVar.setMessage(getReturnStatus().mMessage);
        outEx.setVdsError(tempVar);
        throw outEx;
    }

    protected VdcBllErrors GetReturnValueFromStatus(StatusForXmlRpc status) {
        try {
            VdcBllErrors bllErrors = VdcBllErrors.forValue(status.mCode);
            if (bllErrors == null) {
                log.warn("Weird return value: " + status);
                bllErrors = VdcBllErrors.unexpected;
            }
            return bllErrors;
        } catch (java.lang.Exception e) {
            return VdcBllErrors.unexpected;
        }
    }

    protected Object getReturnValueFromBroker() {
        return status;
    }

    protected boolean getIsPrintReturnValue() {
        return true;
    }

    protected String getAdditionalInformation() {
        return "";
    }

    protected void PrintReturnValue() {
        if (getReturnValueFromBroker() != null && getIsPrintReturnValue()) {
            String returnValue;
            StringBuilder builder = new StringBuilder();
            if (getReturnValueFromBroker() instanceof XmlRpcStruct) {
                XmlRpcObjectDescriptor.ToStringBuilder((XmlRpcStruct) getReturnValueFromBroker(), builder);
                returnValue = builder.toString();
            } else {
                returnValue = getReturnValueFromBroker().toString();
            }
            log.infoFormat("Command {0} return value \n {1}", getClass().getName(), returnValue);
            if (!StringHelper.isNullOrEmpty(getAdditionalInformation())) {
                log.info(getAdditionalInformation());
            }
        } else {
            // log.InfoFormat("Cannot print return value of command {0}",GetType().Name);
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(BrokerCommandBase.class);
}
