package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.DestroyVmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVmDynamicDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class StopVmCommandBase<T extends VmOperationParameterBase> extends VmOperationCommandBase<T> {
    private boolean privateSuspendedVm;

    public StopVmCommandBase(T parameters) {
        super(parameters);
    }

    protected boolean getSuspendedVm() {
        return privateSuspendedVm;
    }

    private void setSuspendedVm(boolean value) {
        privateSuspendedVm = value;
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getVm() == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        } else if (!VM.isStatusUp(getVm().getstatus()) && getVm().getstatus() != VMStatus.Paused
                && getVm().getstatus() != VMStatus.NotResponding && getVm().getstatus() != VMStatus.Suspended) {
            if (getVm().getstatus() == VMStatus.SavingState || getVm().getstatus() == VMStatus.RestoringState) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_SAVING_RESTORING);
            } else {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_RUNNING);
            }
        }

        return retValue;
    }

    protected void Destroy() {
        if (getVm().getstatus() == VMStatus.MigratingFrom && getVm().getmigrating_to_vds() != null) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DestroyVm,
                            new DestroyVmVDSCommandParameters(new Guid(getVm().getmigrating_to_vds().toString()),
                                    getVmId(), true, false, 0));
        }

        setActionReturnValue(Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(VDSCommandType.DestroyVm,
                        new DestroyVmVDSCommandParameters(getVdsId(), getVmId(), false, false, 0)).getReturnValue());
    }

    @Override
    protected void ExecuteVmCommand() {
        getParameters().setEntityId(getVm().getvm_guid());
        if (getVm().getstatus() == VMStatus.Suspended
                || !StringHelper.isNullOrEmpty(getVm().gethibernation_vol_handle())) {
            setSuspendedVm(true);
            setSucceeded(StopSuspendedVm());
        } else {
            super.ExecuteVmCommand();
        }
    }

    /**
     * Start stopping operation for suspended VM, by deleting its storage image groups (Created by hibernation process
     * which indicated its saved memory), and set the VM status to image locked.
     *
     * @return True - Operation succeeded <BR/>
     *         False - Operation failed.
     */
    private boolean StopSuspendedVm() {
        boolean returnVal = false;

        // Set the Vm to null, for getting the recent VM from the DB, instead from the cache.
        setVm(null);
        VMStatus vmStatus = getVm().getstatus();

        // Check whether stop VM procedure didn't started yet (Status is not imageLocked), by another transaction.
        if (getVm().getstatus() != VMStatus.ImageLocked) {
            // Set the VM to image locked to decrease race condition.
            getVm().setstatus(VMStatus.ImageLocked);
            UpdateVmData(getVm().getDynamicData());
            if (!StringHelper.isNullOrEmpty(getVm().gethibernation_vol_handle())
                    && HandleHibernatedVm(getActionType(), false)) {
                returnVal = true;
            } else {
                getVm().setstatus(vmStatus);
                UpdateVmData(getVm().getDynamicData());
            }
        }
        return returnVal;
    }

    /**
     * Update Vm dynamic data in the DB.<BR/>
     * If VM is active in the VDSM (not suspended/stop), we will use UpdateVmDynamicData VDS command, for preventing
     * over write in the DB, otherwise , update directly to the DB.
     */
    private void UpdateVmData(VmDynamic vmDynamicData) {
        if (getVm().getrun_on_vds() != null) {
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.UpdateVmDynamicData,
                            new UpdateVmDynamicDataVDSCommandParameters(getVm().getrun_on_vds().getValue(),
                                    vmDynamicData));
        } else {
            DbFacade.getInstance().getVmDynamicDAO().update(vmDynamicData);
        }
    }

    @Override
    protected void EndVmCommand() {
        setCommandShouldBeLogged(false);

        if (getVm() != null) {
            getVm().setstatus(VMStatus.Down);
            getVm().sethibernation_vol_handle(null);

            DbFacade.getInstance().getVmDynamicDAO().update(getVm().getDynamicData());
        }

        else {
            log.warn("StopVmCommandBase::EndVmCommand: Vm is null - not performing full EndAction");
        }

        setSucceeded(true);
    }

    private static LogCompat log = LogFactoryCompat.getLog(StopVmCommandBase.class);
}
