package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.linq.DefaultMapper;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class VmCountVdsLoadBalancingAlgorithm extends VdsLoadBalancingAlgorithm {
    public VmCountVdsLoadBalancingAlgorithm(VDSGroup group) {
        super(group);
    }

    @Override
    protected void InitOverUtilizedList() {
        int vmCount = 0;
        switch (RunVmCommandBase.getDefaultSelectionAlgorithm()) {
        case EvenlyDistribute: {
            vmCount = Config.<Integer> GetValue(ConfigValues.HighUtilizationForEvenlyDistribute);
            break;
        }
        case PowerSave: {
            vmCount = Config.<Integer> GetValue(ConfigValues.HighUtilizationForPowerSave);
            break;
        }
        }
        // LINQ 29456
        // OverUtilizedServers = AllRelevantVdss.
        // Where(p => p.vm_count > vmCount * p.cpu_cores).
        // OrderByDescending(p=>p.vm_count).
        // ToDictionary(i => i.vds_id);

        final int vmCountTemp = vmCount;
        List<VDS> vdses = LinqUtils.filter(getAllRelevantVdss(), new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return p.getvm_count() > vmCountTemp * p.getcpu_cores();
            }
        });
        Collections.sort(vdses, new Comparator<VDS>() {
            @Override
            public int compare(VDS o1, VDS o2) {
                return o2.getvm_count() - o1.getvm_count();
            }
        });
        setOverUtilizedServers(LinqUtils.toMap(vdses, new DefaultMapper<VDS, Guid>() {
            @Override
            public Guid createKey(VDS vds) {
                return vds.getvds_id();
            }
        }));
    }

    @Override
    protected void InitUnderUtilizedList() {
        int vmCount = 0;
        switch (RunVmCommandBase.getDefaultSelectionAlgorithm()) {
        case EvenlyDistribute: {
            vmCount = Config.<Integer> GetValue(ConfigValues.LowUtilizationForEvenlyDistribute);
            break;
        }
        case PowerSave: {
            vmCount = Config.<Integer> GetValue(ConfigValues.LowUtilizationForPowerSave);
            break;
        }
        }
        // LINQ 29456
        // UnderUtilizedServers = AllRelevantVdss.
        // Where(p => p.vm_count < vmCount *p.cpu_cores).
        // OrderBy(p=>p.vm_count).
        // ToDictionary(i => i.vds_id);

        final int vmCountTemp = vmCount;
        List<VDS> vdses = LinqUtils.filter(getAllRelevantVdss(), new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return p.getvm_count() < vmCountTemp * p.getcpu_cores();
            }
        });
        Collections.sort(vdses, new Comparator<VDS>() {
            @Override
            public int compare(VDS o1, VDS o2) {
                return o1.getvm_count() - o2.getvm_count();
            }
        });
        setUnderUtilizedServers(LinqUtils.toMap(vdses, new DefaultMapper<VDS, Guid>() {
            @Override
            public Guid createKey(VDS vds) {
                return vds.getvds_id();
            }
        }));
    }

    @Override
    protected void InitReadyToMigrationList() {
        int highVdsCount = 0;
        int lowVdsCount = 0;
        int afterThreasholdInPercent = Config.<Integer> GetValue(ConfigValues.UtilizationThresholdInPercent);

        switch (RunVmCommandBase.getDefaultSelectionAlgorithm()) {
        case EvenlyDistribute: {
            highVdsCount = Math.min(
                    afterThreasholdInPercent
                            * Config.<Integer> GetValue(ConfigValues.HighUtilizationForEvenlyDistribute) / 100,
                    Config.<Integer> GetValue(ConfigValues.HighUtilizationForEvenlyDistribute) - 1);
            lowVdsCount = Config.<Integer> GetValue(ConfigValues.LowUtilizationForEvenlyDistribute);
            break;
        }
        case PowerSave: {
            highVdsCount = Math.min(
                    afterThreasholdInPercent * Config.<Integer> GetValue(ConfigValues.HighUtilizationForPowerSave)
                            / 100, Config.<Integer> GetValue(ConfigValues.HighUtilizationForPowerSave) - 1);
            lowVdsCount = Config.<Integer> GetValue(ConfigValues.LowUtilizationForPowerSave);
            break;
        }
        }
        // LINQ 29456
        // ReadyToMigrationServers = AllRelevantVdss.Where(p => p.vm_count <
        // highVdsCount * p.cpu_cores &&
        // p.vm_count >= lowVdsCount * p.cpu_cores).
        // ToDictionary(i => i.vds_id);

        final int highVdsCountTemp = highVdsCount;
        final int lowVdsCountTemp = lowVdsCount;
        List<VDS> vdses = LinqUtils.filter(getAllRelevantVdss(), new Predicate<VDS>() {
            @Override
            public boolean eval(VDS p) {
                return p.getvm_count() < highVdsCountTemp * p.getcpu_cores()
                        && p.getvm_count() >= lowVdsCountTemp * p.getcpu_cores();
            }
        });
        setReadyToMigrationServers(LinqUtils.toMap(vdses, new DefaultMapper<VDS, Guid>() {
            @Override
            public Guid createKey(VDS vds) {
                return vds.getvds_id();
            }
        }));
    }

    @Override
    protected VM getBestVmToMigrate(List<VM> vms, Guid vdsId) {
        return vms.get(0);
    }
}
