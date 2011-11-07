package org.ovirt.engine.api.restapi.types;

import java.math.BigDecimal;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostStatus;
import org.ovirt.engine.api.model.HostType;
import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.model.KSM;
import org.ovirt.engine.api.model.PowerManagement;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.model.TransparentHugePages;
import org.ovirt.engine.api.model.VmSummary;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.queries.ValueObjectPair;
import org.ovirt.engine.core.compat.Guid;

public class HostMapper {

    // REVISIT retrieve from configuration
    private static final int DEFAULT_VDSM_PORT = 54321;

    @Mapping(from = Host.class, to = VdsStatic.class)
    public static VdsStatic map(Host model, VdsStatic template) {
        VdsStatic entity = template != null ? template : new VdsStatic();
        if (model.isSetId()) {
            entity.setId(new Guid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setvds_name(model.getName());
        }
        if (model.isSetCluster() && model.getCluster().isSetId()) {
            entity.setvds_group_id(new Guid(model.getCluster().getId()));
        }
        if (model.isSetAddress()) {
            entity.sethost_name(model.getAddress());
        }
        if (model.isSetPort() && model.getPort() > 0) {
            entity.setport(model.getPort());
        } else {
            entity.setport(DEFAULT_VDSM_PORT);
        }
        if (model.isSetPowerManagement()) {
            entity = map(model.getPowerManagement(), entity);
        }
        return entity;
    }

    @Mapping(from = PowerManagement.class, to = VdsStatic.class)
    public static VdsStatic map(PowerManagement model, VdsStatic template) {
        VdsStatic entity = template != null ? template : new VdsStatic();
        if (model.isSetType()) {
            entity.setpm_type(model.getType());
        }
        if (model.isSetEnabled()) {
            entity.setpm_enabled(model.isEnabled());
        }
        if (model.isSetAddress()) {
            entity.setManagmentIp(model.getAddress());
        }
        if (model.isSetUsername()) {
            entity.setpm_user(model.getUsername());
        }
        if (model.isSetPassword()) {
            entity.setpm_password(model.getPassword());
        }
        if (model.isSetOptions()) {
            entity.setpm_options(map(model.getOptions(), null));
        }
        return entity;
    }

    @Mapping(from = Options.class, to = String.class)
    public static String map(Options model, String template) {
        StringBuilder buf = template != null ? new StringBuilder(template) : new StringBuilder();
        for (Option option : model.getOptions()) {
            String opt = map(option, null);
            if (opt != null) {
                if (buf.length() > 0) {
                    buf.append(",");
                }
                buf.append(opt);
            }
        }
        return buf.toString();
    }

    @Mapping(from = Option.class, to = String.class)
    public static String map(Option model, String template) {
        if (model.isSetName() && (!model.getName().isEmpty()) && model.isSetValue() && (!model.getValue().isEmpty())) {
            return model.getName() + "=" + model.getValue();
        } else {
            return template;
        }
    }

    @Mapping(from = VDS.class, to = Host.class)
    public static Host map(VDS entity, Host template) {
        Host model = template != null ? template : new Host();
        model.setId(entity.getvds_id().toString());
        model.setName(entity.getvds_name());
        if (entity.getvds_group_id() != null) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getvds_group_id().toString());
            model.setCluster(cluster);
        }
        model.setAddress(entity.gethost_name());
        if (entity.getport() > 0) {
            model.setPort(entity.getport());
        }
        HostStatus status = map(entity.getstatus(), null);
        model.setStatus(StatusUtils.create(status));
        if (status==HostStatus.NON_OPERATIONAL) {
            model.getStatus().setDetail(entity.getNonOperationalReason().name().toLowerCase());
        }
        model.setStorageManager(entity.getspm_status() == VdsSpmStatus.SPM);
        model.setKsm(new KSM());
        model.getKsm().setEnabled(Boolean.TRUE.equals(entity.getksm_state()));
        model.setTransparentHugepages(new TransparentHugePages());
        model.getTransparentHugepages().setEnabled(!(entity.getTransparentHugePagesState() == null ||
                                                     entity.getTransparentHugePagesState() == VdsTransparentHugePagesState.Never));
        if (entity.getIScsiInitiatorName() != null) {
            model.setIscsi(new IscsiDetails());
            model.getIscsi().setInitiator(entity.getIScsiInitiatorName());
        }
        model.setPowerManagement(map(entity, (PowerManagement)null));
        CPU cpu = new CPU();
        if (entity.getcpu_cores()!=null) {
            CpuTopology cpuTopology = new CpuTopology();
            cpuTopology.setCores(entity.getcpu_cores());
            cpu.setTopology(cpuTopology);
        }
        cpu.setName(entity.getcpu_model());
        if (entity.getcpu_speed_mh()!=null) {
            cpu.setSpeed(new BigDecimal(entity.getcpu_speed_mh()));
        }
        model.setCpu(cpu);
        VmSummary vmSummary = new VmSummary();
        vmSummary.setActive(entity.getvm_active());
        vmSummary.setMigrating(entity.getvm_migrating());
        vmSummary.setTotal(entity.getvm_count());
        model.setSummary(vmSummary);
        if (entity.getvds_type() != null) {
            HostType type = map(entity.getvds_type(), null);
            model.setType(type != null ? type.value() : null);
        }
        return model;
    }

    @Mapping(from = VDS.class, to = PowerManagement.class)
    public static PowerManagement map(VDS entity, PowerManagement template) {
        PowerManagement model = template != null ? template : new PowerManagement();
        model.setType(entity.getpm_type());
        model.setEnabled(entity.getpm_enabled());
        model.setAddress(entity.getManagmentIp());
        model.setUsername(entity.getpm_user());
        if (entity.getPmOptionsMap() != null) {
            model.setOptions(map(entity.getPmOptionsMap(), null));
        }
        return model;
    }

    @Mapping(from = ValueObjectMap.class, to = Options.class)
    public static Options map(ValueObjectMap entity, Options template) {
        Options model = template != null ? template : new Options();
        for (ValueObjectPair option : entity.getValuePairs()) {
            model.getOptions().add(map(option, null));
        }
        return model;
    }

    @Mapping(from = ValueObjectPair.class, to = Option.class)
    public static Option map(ValueObjectPair entity, Option template) {
        Option model = template != null ? template : new Option();
        model.setName((String)entity.getKey());
        model.setValue((String)entity.getValue());
        return model;
    }

    @Mapping(from = VDSStatus.class, to = HostStatus.class)
    public static HostStatus map(VDSStatus entityStatus, HostStatus template) {
        switch (entityStatus) {
        case Unassigned:
            return HostStatus.UNASSIGNED;
        case Down:
            return HostStatus.DOWN;
        case Maintenance:
            return HostStatus.MAINTENANCE;
        case Up:
            return HostStatus.UP;
        case NonResponsive:
            return HostStatus.NON_RESPONSIVE;
        case Error:
            return HostStatus.ERROR;
        case Installing:
            return HostStatus.INSTALLING;
        case InstallFailed:
            return HostStatus.INSTALL_FAILED;
        case Reboot:
            return HostStatus.REBOOT;
        case PreparingForMaintenance:
            return HostStatus.PREPARING_FOR_MAINTENANCE;
        case NonOperational:
            return HostStatus.NON_OPERATIONAL;
        case PendingApproval:
            return HostStatus.PENDING_APPROVAL;
        case Initializing:
            return HostStatus.INITIALIZING;
        case Problematic:
            return HostStatus.CONNECTING;
        default:
            return null;
        }
    }

    @Mapping(from = VDSType.class, to = HostType.class)
    public static HostType map(VDSType type, HostType template) {
        switch (type) {
            case VDS:
                return HostType.RHEL;
            case oVirtNode:
                return HostType.RHEV_H;
            default:
                return null;
        }
    }
}
