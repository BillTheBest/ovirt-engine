package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetAllVmPoolsAttachedToUserParameters;
import org.ovirt.engine.core.common.queries.GetUserVmsByUserIdAndGroupsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RunVmOnDedicatedVdsCommand<T extends RunVmParams> extends RunVmCommand<T> {
    public RunVmOnDedicatedVdsCommand(T runVmParams) {
        super(runVmParams);
        getVdsSelector().setCheckDestinationFirst(false);
    }

    @Override
    protected VDS getDestinationVds() {
        if (_destinationVds == null) {
            if (getParameters().getDestinationVdsId() != null) {
                VDS powerclient = DbFacade.getInstance().getVdsDAO().get(getParameters().getDestinationVdsId());
                if (powerclient != null && powerclient.getvds_type() == VDSType.PowerClient) {
                    if (Config.<Boolean> GetValue(ConfigValues.PowerClientLogDetection)) {
                        log.infoFormat("VdcBLL::RunVmCommand - Powerclient id= {0}, name = {1} evaluated",
                                powerclient.getvds_id(), powerclient.getvds_name());
                    }
                    _destinationVds = powerclient;
                } else {
                    if (Config.<Boolean> GetValue(ConfigValues.PowerClientLogDetection)) {
                        log.infoFormat("VdcBLL::RunVmCommand - Powerclient id= {0} could not been evaluated",
                                getParameters().getDestinationVdsId());
                    }
                }
            }
        }
        return _destinationVds;
    }

    @Override
    protected void HandleMemoryAdjustments() {
        if (getDestinationVds() != null) {
            AutoMemoryAdjust(getDestinationVds(), getVm());
        }
    }

    protected void AutoMemoryAdjust(VDS vds, VM vm) {
        if (!Config.<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemory)) {
            return;
        }

        int memory = -1;
        if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemoryBaseOnAvailableMemory)) {
            if (vds.getmem_available() != null) {
                memory = vds.getmem_available().intValue();
            }
        } else {
            if (vds.getphysical_mem_mb() != null) {
                memory = vds.getphysical_mem_mb();
            }
        }
        if (memory == -1) {
            log.errorFormat("VdcBll.RunVmCommand.AutoMemoryAdjust - {0} memory is null, Auto Adjust Memory", Config
                    .<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemoryBaseOnAvailableMemory) ? "available"
                    : "physical");
            return;
        }
        if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemoryLog)) {
            log.infoFormat("VdcBll.RunVmCommand.AutoMemoryAdjust - Basing on VDS {0} - {1}MB", Config
                    .<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemoryBaseOnAvailableMemory) ? "available"
                    : "physical", memory);
        }

        memory = memory - Config.<Integer> GetValue(ConfigValues.PowerClientAutoAdjustMemoryGeneralReserve);
        if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemoryLog)) {
            log.infoFormat("VdcBll.RunVmCommand.AutoMemoryAdjust - After reducing general reserve {0} - {1}MB",
                    Config.<Integer> GetValue(ConfigValues.PowerClientAutoAdjustMemoryGeneralReserve), memory);
        }

        if (Config.<Integer> GetValue(ConfigValues.PowerClientAutoAdjustMemorySpicePerSessionReserve) != 0) {
            if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemoryLog)) {
                log.infoFormat(
                        "VdcBll.RunVmCommand.AutoMemoryAdjust - Checking Spice per session reserve of {0}MB per session",
                        Config.<Integer> GetValue(ConfigValues.PowerClientAutoAdjustMemorySpicePerSessionReserve));
            }
            log.infoFormat("VdcBll.RunVmCommand.AutoMemoryAdjust - Checking number of VMs and Pools");
            if (getParameters().getRequestingUser() == null) {
                log.errorFormat("VdcBll.RunVmCommand.AutoMemoryAdjust - PowerClientAutoAdjustMemorySpicePerSessionReserve requested, but RunVmParameters.RequestingUser is null");
            } else {
                VdcQueryReturnValue returnValueVMs = Backend.getInstance().runInternalQuery(
                        VdcQueryType.GetUserVmsByUserIdAndGroups,
                        new GetUserVmsByUserIdAndGroupsParameters(getParameters().getRequestingUser().getUserId()));
                List<VM> vmList = returnValueVMs == null ? new java.util.ArrayList<VM>() : (List) returnValueVMs
                        .getReturnValue();
                VdcQueryReturnValue returnValuePools = Backend.getInstance().runInternalQuery(
                        VdcQueryType.GetAllVmPoolsAttachedToUser,
                        new GetAllVmPoolsAttachedToUserParameters(getParameters().getRequestingUser().getUserId()));
                List<vm_pools> vmPools = (returnValuePools != null) ? (List) returnValuePools.getReturnValue()
                        : new java.util.ArrayList<vm_pools>();
                // foreach (VM vm in vmList)
                // if (vm.DynamicData.guest_cur_user_id.HasValue)
                // {
                // log.infoFormat("VdcBll.RunVmCommand.AutoMemoryAdjust - VM: {0}, current user: {1}, requesting user: {2}",
                // vm.vm_name, vm.DynamicData.guest_cur_user_id,
                // RunVmParameters.RequestingUSer);
                // if
                // (vm.DynamicData.guest_cur_user_id.Value.Equals(RunVmParameters.RequestingUSer.UserName))
                // vmList.Remove(vm);
                // }
                memory = memory
                        - ((vmList.size() + vmPools.size()) * Config
                                .<Integer> GetValue(ConfigValues.PowerClientAutoAdjustMemorySpicePerSessionReserve));
                if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemoryLog)) {
                    log.infoFormat("VdcBll.RunVmCommand.AutoMemoryAdjust - Reducing {0}MB for {1} VMs and {2} Pools",
                            Config.<Integer> GetValue(ConfigValues.PowerClientAutoAdjustMemorySpicePerSessionReserve),
                            vmList.size(), vmPools.size());
                }
            }
        }

        if (Config.<Integer> GetValue(ConfigValues.PowerClientAutoAdjustMemorySpicePerMonitorReserve) != 0) {
            if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemoryLog)) {
                log.infoFormat(
                        "VdcBll.RunVmCommand.AutoMemoryAdjust - Reducing Spice per monitor reserve of {0}MB per monitor. {1} monitors",
                        Config.<Integer> GetValue(ConfigValues.PowerClientAutoAdjustMemorySpicePerMonitorReserve),
                        vm.getnum_of_monitors());
            }
            memory = memory
                    - (vm.getnum_of_monitors() * Config
                            .<Integer> GetValue(ConfigValues.PowerClientAutoAdjustMemorySpicePerMonitorReserve));
        }
        int maxMemory = Config.<Integer> GetValue(ConfigValues.PowerClientAutoAdjustMemoryMaxMemory);
        if (memory > maxMemory) {
            if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemoryLog)) {
                log.infoFormat("VdcBll.RunVmCommand.AutoMemoryAdjust - VM memory fixed to max allowed memory: {0}",
                        maxMemory);
            }
            memory = maxMemory;
        }

        int modulus = Config.<Integer> GetValue(ConfigValues.PowerClientAutoAdjustMemoryModulus);
        int modulusMemory = (memory / modulus) * modulus;
        if (modulusMemory != memory) {
            if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemoryLog)) {
                log.infoFormat("VdcBll.RunVmCommand.AutoMemoryAdjust - VM memory adjusted to modulus {0}: {1}",
                        modulus, modulusMemory);
            }
            memory = modulusMemory;
        }

        if (memory < vm.getStaticData().getmem_size_mb()) {
            if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemoryLog)) {
                log.infoFormat(
                        "VdcBll.RunVmCommand.AutoMemoryAdjust - VM memory adjusted to oriignal value as the result was lower than predefined value: {0}",
                        vm.getStaticData().getmem_size_mb());
            }
            memory = vm.getStaticData().getmem_size_mb();
        }

        if (Config.<Boolean> GetValue(ConfigValues.PowerClientAutoAdjustMemoryLog)) {
            log.infoFormat("VdcBll.RunVmCommand.AutoMemoryAdjust - VM memory will be: {0}", memory);
        }
        vm.getStaticData().setmem_size_mb(memory);
    }

    private static LogCompat log = LogFactoryCompat.getLog(RunVmOnDedicatedVdsCommand.class);
}
