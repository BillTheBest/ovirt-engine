package org.ovirt.engine.core.vdsbroker;

import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.dal.dbbroker.*;
import org.ovirt.engine.core.common.businessentities.*;

public abstract class VdsIdVDSCommandBase<P extends VdsIdVDSCommandParametersBase> extends VDSCommandBase<P> {
    protected VdsManager _vdsManager;

    public VdsIdVDSCommandBase(P parameters) {
        super(parameters);
        _vdsManager = ResourceManager.getInstance().GetVdsManager(parameters.getVdsId());
    }

    protected Guid getVdsId() {
        return getParameters().getVdsId();
    }

    private VDS _vds;

    protected VDS getVds() {
        if (_vds == null) {
            _vds = DbFacade.getInstance().getVdsDAO().get(getVdsId());
        }
        return _vds;
    }

    @Override
    protected void ExecuteVDSCommand() {
        if (_vdsManager != null) {
            synchronized (_vdsManager.getLockObj()) {
                ExecuteVdsIdCommand();
            }
        } else {
            ExecuteVdsIdCommand();
        }
    }

    protected abstract void ExecuteVdsIdCommand();

    // protected void SaveVmDynamicToDBThreaded(VM vm)
    // {
    // ThreadPoolCompat.QueueUserWorkItem(delegate
    // {
    // for (int i = 1; i < 6; i++)
    // {
    // try
    // {
    // DbFacade.Instance.SaveVmDynamic(vm.DynamicData);
    // return;
    // }
    // catch (Exception ex)
    // {
    // log.infoFormat("ResourceManager::Failed save vm dynamic to DB, try number {4}. vm: {0} in vds = {1} : {2} error = {3}",
    // vm.vm_name, _vdsManager.Vds.vds_id, _vdsManager.Vds.vds_name, ex.Message,
    // i);
    // ThreadCompat.Sleep(1000);
    // }
    // }
    // log.errorFormat("ResourceManager::Failed save vm dynamic to DB. vm: {0} in vds = {1} : {2}. command name: {3}.",
    // vm.vm_name, _vdsManager.Vds.vds_id, _vdsManager.Vds.vds_name,
    // CommandName);
    // });
    // }

    protected void SaveVdsDynamicToDBThreaded(VDS vds, VM vm) {
        // TODO should use thread poo and not timer
        Class<?>[] inputTypes = new Class[] { VDS.class, VM.class };
        Object[] inputParams = new Object[] { vds, vm };
        SchedulerUtilQuartzImpl.getInstance().scheduleAOneTimeJob(this, "saveVdsDynamicToDBOnTimer", inputTypes,
                inputParams, 0, TimeUnit.MILLISECONDS);
    }

    @OnTimerMethodAnnotation("saveVdsDynamicToDBOnTimer")
    public void saveVdsDynamicToDBOnTimer(VDS vds, VM vm) {
        try {
            _vdsManager.UpdateDynamicData(vds.getDynamicData());
        } catch (RuntimeException ex) {
            log.errorFormat(
                    "ResourceManager::Failed save vds dynamic to DB. vm: {0} in vds = {1} : {2} error = {3}. command name: {3}.",
                    vm.getvm_name(),
                    vds.getvds_id(),
                    vds.getvds_name(),
                    ex.getMessage(),
                    getCommandName());
        }
    }

    private static LogCompat log = LogFactoryCompat.getLog(VdsIdVDSCommandBase.class);
}
