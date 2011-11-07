package org.ovirt.engine.api.restapi.types;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.common.util.TimeZoneMapping;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.CpuTopology;
import org.ovirt.engine.api.model.CustomProperties;
import org.ovirt.engine.api.model.CustomProperty;
import org.ovirt.engine.api.model.Display;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.GuestInfo;
import org.ovirt.engine.api.model.HighAvailability;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.IP;
import org.ovirt.engine.api.model.OperatingSystem;
import org.ovirt.engine.api.model.OsType;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmAffinity;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.model.VmMemoryPolicy;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.model.VmStatus;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;

import static org.ovirt.engine.core.compat.NGuid.createGuidFromString;

public class VmMapper {

    private static final String RHEV = "rhev";
    private static final String ENGINE = "engine";
    private static final int BYTES_PER_MB = 1024 * 1024;
    // REVISIT retrieve from configuration
    private static final int DEFAULT_MEMORY_SIZE = 10 * 1024;

    // REVISIT once #712661 implemented by BE
    @Mapping(from = VmTemplate.class, to = VmStatic.class)
    public static VmStatic map(VmTemplate entity, VmStatic template) {
        VmStatic staticVm = template != null ? template : new VmStatic();
        staticVm.setId(NGuid.Empty);
        staticVm.setvmt_guid(entity.getId());
        staticVm.setdomain(entity.getdomain());
        staticVm.setvds_group_id(entity.getvds_group_id());
        staticVm.setmem_size_mb(entity.getmem_size_mb());
        staticVm.setos(entity.getos());
        staticVm.setnice_level(entity.getnice_level());
        staticVm.setfail_back(entity.getfail_back());
        staticVm.setauto_startup(entity.getauto_startup());
        staticVm.setis_stateless(entity.getis_stateless());
        staticVm.setauto_startup(entity.getauto_startup());
        staticVm.setdefault_boot_sequence(entity.getdefault_boot_sequence());
        staticVm.setvm_type(entity.getvm_type());
        entity.setdefault_display_type(entity.getdefault_display_type());
        staticVm.setiso_path(entity.getiso_path());
        staticVm.setnum_of_sockets(entity.getnum_of_sockets());
        staticVm.setcpu_per_socket(entity.getcpu_per_socket());
        staticVm.setkernel_url(entity.getkernel_url());
        staticVm.setkernel_params(entity.getkernel_params());
        staticVm.setinitrd_url(entity.getinitrd_url());
        staticVm.sethypervisor_type(entity.gethypervisor_type());
        staticVm.settime_zone(entity.gettime_zone());
        staticVm.setnum_of_monitors(entity.getnum_of_monitors());
        staticVm.setpriority(entity.getpriority());
        staticVm.setusb_policy(entity.getusb_policy());
        return staticVm;
    }
    @Mapping(from = VM.class, to = VmStatic.class)
    public static VmStatic map(VM vm, VmStatic template) {
        VmStatic staticVm = template != null ? template : new VmStatic();
        if (vm.isSetName()) {
            staticVm.setvm_name(vm.getName());
        }
        if (vm.isSetId()) {
            staticVm.setId(new Guid(vm.getId()));
        }
        if (vm.isSetDescription()) {
            staticVm.setdescription(vm.getDescription());
        }
        if (vm.isSetMemory()) {
            staticVm.setmem_size_mb((int) (vm.getMemory() / BYTES_PER_MB));
        } else {
            staticVm.setmem_size_mb(DEFAULT_MEMORY_SIZE);
        }
        if (vm.isSetTemplate() && vm.getTemplate().getId() != null) {
            staticVm.setvmt_guid(new Guid(vm.getTemplate().getId()));
        }
        if (vm.isSetCluster() && vm.getCluster().getId() != null) {
            staticVm.setvds_group_id(new Guid(vm.getCluster().getId()));
        }
        if (vm.isSetCpu() && vm.getCpu().isSetTopology()) {
            if (vm.getCpu().getTopology().isSetCores()) {
                staticVm.setcpu_per_socket(vm.getCpu().getTopology().getCores());
            }
            if (vm.getCpu().getTopology().isSetSockets()) {
                staticVm.setnum_of_sockets(vm.getCpu().getTopology().getSockets());
            }
        }
        if (vm.isSetOs()) {
            if (vm.getOs().isSetType()) {
                OsType osType = OsType.fromValue(vm.getOs().getType());
                if (osType != null) {
                    staticVm.setos(map(osType, null));
                }
            }
            if (vm.getOs().isSetBoot() && vm.getOs().getBoot().size() > 0) {
                staticVm.setdefault_boot_sequence(map(vm.getOs().getBoot(), null));
            }
            if (vm.getOs().isSetKernel()) {
                staticVm.setkernel_url(vm.getOs().getKernel());
            }
            if (vm.getOs().isSetInitrd()) {
                staticVm.setinitrd_url(vm.getOs().getInitrd());
            }
            if (vm.getOs().isSetCmdline()) {
                staticVm.setkernel_params(vm.getOs().getCmdline());
            }
        }
        if (vm.isSetType()) {
            VmType vmType = VmType.fromValue(vm.getType());
            if (vmType != null) {
                staticVm.setvm_type(map(vmType, null));
            }
        }
        if (vm.isSetStateless()) {
            staticVm.setis_stateless(vm.isStateless());
        }
        if (vm.isSetHighAvailability()) {
            HighAvailability ha = vm.getHighAvailability();
            if (ha.isSetEnabled()) {
                staticVm.setauto_startup(ha.isEnabled());
            }
            if (ha.isSetPriority()) {
                staticVm.setpriority(ha.getPriority());
            }
        }
        if (vm.isSetOrigin()) {
            staticVm.setorigin(map(vm.getOrigin(), (OriginType)null));
        }
        if (vm.isSetDisplay()) {
            if (vm.getDisplay().isSetType()) {
                DisplayType displayType = DisplayType.fromValue(vm.getDisplay().getType());
                if (displayType != null) {
                    staticVm.setdefault_display_type(map(displayType, null));
                }
            }
            if (vm.getDisplay().isSetMonitors()) {
                staticVm.setnum_of_monitors(vm.getDisplay().getMonitors());
            }
        }
        if (vm.isSetPlacementPolicy() && vm.getPlacementPolicy().isSetAffinity()) {
            VmAffinity vmAffinity = VmAffinity.fromValue(vm.getPlacementPolicy().getAffinity());
            if (vmAffinity!=null) {
                staticVm.setMigrationSupport(map(vmAffinity, null));
            }
        }
        if (vm.isSetPlacementPolicy() && vm.getPlacementPolicy().isSetHost()) {
            staticVm.setdedicated_vm_for_vds(createGuidFromString(vm.getPlacementPolicy().getHost().getId()));
        }
        if (vm.isSetDomain() && vm.getDomain().isSetName()) {
            staticVm.setdomain(vm.getDomain().getName());
        }
        if (vm.isSetMemoryPolicy() && vm.getMemoryPolicy().isSetGuaranteed()) {
            Long memGuaranteed = vm.getMemoryPolicy().getGuaranteed() / BYTES_PER_MB;
            staticVm.setMinAllocatedMem(memGuaranteed.intValue());
        }
        if (vm.isSetTimezone()) {
            staticVm.settime_zone(TimeZoneMapping.getWindows(vm.getTimezone()));
        }
        if (vm.isSetCustomProperties() && vm.getCustomProperties().isSetCustomProperty()) {
            staticVm.setCustomProperties(CustomPropertiesParser.parse(vm.getCustomProperties().getCustomProperty()));
        }
        if (vm.isSetUsb() && vm.getUsb().isSetEnabled()) {
            staticVm.setusb_policy(vm.getUsb().isEnabled() ? UsbPolicy.Enabled : UsbPolicy.Disabled);
        }
        return staticVm;
    }

    @Mapping(from = VmAffinity.class, to = MigrationSupport.class)
    public static MigrationSupport map(VmAffinity vmAffinity, MigrationSupport template) {
        if(vmAffinity!=null){
            switch (vmAffinity) {
            case MIGRATABLE:
                return MigrationSupport.MIGRATABLE;
            case USER_MIGRATABLE:
                return MigrationSupport.IMPLICITLY_NON_MIGRATABLE;
            case PINNED:
                return MigrationSupport.PINNED_TO_HOST;
            default:
                return null;
            }
        }
        return null;
    }

    @Mapping(from = MigrationSupport.class, to = VmAffinity.class)
    public static VmAffinity map(MigrationSupport migrationSupport, VmAffinity template) {
        if(migrationSupport!=null){
            switch (migrationSupport) {
            case MIGRATABLE:
                return VmAffinity.MIGRATABLE;
            case IMPLICITLY_NON_MIGRATABLE:
                return VmAffinity.USER_MIGRATABLE;
            case PINNED_TO_HOST:
                return VmAffinity.PINNED;
            default:
                return null;
            }
        }
        return null;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.VM.class, to = org.ovirt.engine.api.model.VM.class)
    public static VM map(org.ovirt.engine.core.common.businessentities.VM entity, VM template) {
        VM model = template != null ? template : new VM();
        model.setId(entity.getvm_guid().toString());
        model.setName(entity.getvm_name());
        model.setDescription(entity.getvm_description());
        model.setMemory((long) entity.getmem_size_mb() * BYTES_PER_MB);
        if (entity.getvmt_guid() != null) {
            model.setTemplate(new Template());
            model.getTemplate().setId(entity.getvmt_guid().toString());
        }
        if (entity.getstatus() != null) {
            model.setStatus(StatusUtils.create(map(entity.getstatus(), null)));
            if (entity.getstatus()==VMStatus.Paused) {
                model.getStatus().setDetail(entity.getVmPauseStatus().name().toLowerCase());
            }
        }
        if (entity.getvm_os() != null ||
            entity.getboot_sequence() != null ||
            entity.getkernel_url() != null ||
            entity.getinitrd_url() != null ||
            entity.getkernel_params() != null) {
            OperatingSystem os = new OperatingSystem();
            if (entity.getvm_os() != null) {
                OsType osType = VmMapper.map(entity.getos(), null);
                if (osType != null) {
                    os.setType(osType.value());
                }
            }
            if (entity.getboot_sequence() != null) {
                for (Boot boot : map(entity.getdefault_boot_sequence(), null)) {
                    os.getBoot().add(boot);
                }
            }
            os.setKernel(entity.getkernel_url());
            os.setInitrd(entity.getinitrd_url());
            os.setCmdline(entity.getkernel_params());
            model.setOs(os);
        }
        if (entity.getvds_group_id() != null) {
            Cluster cluster = new Cluster();
            cluster.setId(entity.getvds_group_id().toString());
            model.setCluster(cluster);
        }
        CpuTopology topology = new CpuTopology();
        topology.setSockets(entity.getnum_of_sockets());
        topology.setCores(entity.getnum_of_cpus() / entity.getnum_of_sockets());
        model.setCpu(new CPU());
        model.getCpu().setTopology(topology);
        if (entity.getVmPoolId() != null) {
            VmPool pool = new VmPool();
            pool.setId(entity.getVmPoolId().toString());
            model.setVmPool(pool);
        }
        if (entity.getrun_on_vds() != null) {
            model.setHost(new Host());
            model.getHost().setId(entity.getrun_on_vds().toString());
        }
        if (entity.getdisplay_type() != null) {
            model.setDisplay(new Display());
            model.getDisplay().setType(map(entity.getdisplay_type(), null));
            model.getDisplay().setAddress(entity.getdisplay_ip());
            Integer displayPort = entity.getdisplay();
            model.getDisplay().setPort(displayPort==null || displayPort==-1 ? null : displayPort);
            Integer displaySecurePort = entity.getdisplay_secure_port();
            model.getDisplay().setSecurePort(displaySecurePort==null || displaySecurePort==-1 ? null : displaySecurePort);
            model.getDisplay().setMonitors(entity.getnum_of_monitors());
        }
        model.setType(map(entity.getvm_type(), null));
        model.setStateless(entity.getis_stateless());
        model.setHighAvailability(new HighAvailability());
        model.getHighAvailability().setEnabled(entity.getauto_startup());
        model.getHighAvailability().setPriority(entity.getpriority());
        if (entity.getorigin() != null) {
            model.setOrigin(map(entity.getorigin(), null));
        }
        if (entity.getvm_creation_date() != null) {
            model.setCreationTime(DateMapper.map(entity.getvm_creation_date(), null));
        }
        if (entity.getelapsed_time() != null) {
            model.setStartTime(DateMapper.map(new BigDecimal(entity.getelapsed_time()), null));
        }
        model.setPlacementPolicy(new VmPlacementPolicy());
        if(entity.getdedicated_vm_for_vds() !=null){
            model.getPlacementPolicy().setHost(new Host());
            model.getPlacementPolicy().getHost().setId(entity.getdedicated_vm_for_vds().toString());
        }
        VmAffinity vmAffinity = map(entity.getMigrationSupport(),null);
        if(vmAffinity !=null){
            model.getPlacementPolicy().setAffinity(vmAffinity.value());
        }
        if (entity.getvm_domain()!=null && !entity.getvm_domain().isEmpty()) {
            Domain domain = new Domain();
            domain.setName(entity.getvm_domain());
            model.setDomain(domain);
        }
        if (entity.getvm_ip()!=null && !entity.getvm_ip().isEmpty()) {
            IP ip = new IP();
            ip.setAddress(entity.getvm_ip());
            GuestInfo guestInfo = new GuestInfo();
            guestInfo.setIp(ip);
            model.setGuestInfo(guestInfo);
        }
        VmMemoryPolicy policy = new VmMemoryPolicy();
        policy.setGuaranteed(new Long(entity.getMinAllocatedMem()) * BYTES_PER_MB);
        model.setMemoryPolicy(policy);
        model.setTimezone(TimeZoneMapping.getJava(entity.gettime_zone()));
        if (!StringHelper.isNullOrEmpty(entity.getCustomProperties())) {
            CustomProperties hooks = new CustomProperties();
            hooks.getCustomProperty().addAll(CustomPropertiesParser.parse(entity.getCustomProperties(), false));
            model.setCustomProperties(hooks);
        }
        if (entity.getusb_policy()!=null) {
            Usb usb = new Usb();
            usb.setEnabled(entity.getusb_policy()==UsbPolicy.Enabled ? true : false);
            model.setUsb(usb);
        }
        return model;
    }

    @Mapping(from = VM.class, to = RunVmOnceParams.class)
    public static RunVmOnceParams map(VM vm, RunVmOnceParams template) {
        RunVmOnceParams params = template != null ? template : new RunVmOnceParams();
        if (vm.isSetStateless() && vm.isStateless()) {
            params.setRunAsStateless(true);
        }
        if (vm.isSetDisplay() && vm.getDisplay().isSetType()) {
            DisplayType displayType = DisplayType.fromValue(vm.getDisplay().getType());
            if (displayType != null) {
                params.setUseVnc(displayType == DisplayType.VNC);
            }
        }
        if (vm.isSetOs() && vm.getOs().getBoot().size() > 0) {
            params.setBootSequence(map(vm.getOs().getBoot(), null));
        }
        if (vm.isSetCdroms() && vm.getCdroms().isSetCdRoms()) {
            String file = vm.getCdroms().getCdRoms().get(0).getFile().getId();
            if (file != null) {
                params.setDiskPath(file);
            }
        }
        if (vm.isSetFloppies() && vm.getFloppies().isSetFloppies()) {
            String file = vm.getFloppies().getFloppies().get(0).getFile().getId();
            if (file != null) {
                params.setFloppyPath(file);
            }
        }
        if (vm.isSetCustomProperties() && vm.getCustomProperties().isSetCustomProperty()) {
            params.setCustomProperties(CustomPropertiesParser.parse(vm.getCustomProperties().getCustomProperty()));
        }
        if (vm.isSetOs()) {
            if (vm.getOs().isSetBoot() && vm.getOs().getBoot().size() > 0) {
                params.setBootSequence(map(vm.getOs().getBoot(), null));
            }
            if (vm.getOs().isSetKernel()) {
                params.setkernel_url(vm.getOs().getKernel());
            }
            if (vm.getOs().isSetInitrd()) {
                params.setinitrd_url(vm.getOs().getInitrd());
            }
            if (vm.getOs().isSetCmdline()) {
                params.setkernel_params(vm.getOs().getCmdline());
            }
        }
        if (vm.isSetDomain() && vm.getDomain().isSetName()) {
            params.setSysPrepDomainName(vm.getDomain().getName());
            if (vm.getDomain().isSetUser()) {
                if (vm.getDomain().getUser().isSetUserName()) {
                    params.setSysPrepUserName(vm.getDomain().getUser().getUserName());
                }
                if (vm.getDomain().getUser().isSetPassword()) {
                    params.setSysPrepPassword(vm.getDomain().getUser().getPassword());
                }
            }
        }

        return params;
    }

    @Mapping(from = String.class, to = CustomProperties.class)
    public static CustomProperties map(String entity, CustomProperties template) {
        CustomProperties model = template != null ? template : new CustomProperties();
        if (entity != null) {
            for (String envStr : entity.split(";", -1)) {
                String[] parts = envStr.split("=", -1);
                if (parts.length >= 1) {
                    CustomProperty env = new CustomProperty();
                    env.setName(parts[0]);
                    if (parts.length == 1) {
                        env.setValue(parts[1]);
                    }
                    model.getCustomProperty().add(env);
                }
            }
        }
        return model;
    }

    @Mapping(from = CustomProperties.class, to = String.class)
    public static String map(CustomProperties model, String template) {
        StringBuilder buf = template != null ? new StringBuilder(template) : new StringBuilder();
        for (CustomProperty env : model.getCustomProperty()) {
            String envStr = map(env, null);
            if (envStr != null) {
                if (buf.length() > 0) {
                    buf.append(";");
                }
                buf.append(envStr);
            }
        }
        return buf.toString();
    }

    @Mapping(from = CustomProperty.class, to = String.class)
    public static String map(CustomProperty model, String template) {
        if (model.isSetName()) {
            String ret = model.getName() + "=";
            if (model.isSetValue()) {
                ret += model.getValue();
            }
            return ret;
        } else {
            return template;
        }
    }

    @Mapping(from = VmType.class, to = org.ovirt.engine.core.common.businessentities.VmType.class)
    public static org.ovirt.engine.core.common.businessentities.VmType map(VmType type,
                                      org.ovirt.engine.core.common.businessentities.VmType incoming) {
        switch (type) {
        case DESKTOP:
            return org.ovirt.engine.core.common.businessentities.VmType.Desktop;
        case SERVER:
            return org.ovirt.engine.core.common.businessentities.VmType.Server;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.VmType.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.VmType type, String incoming) {
        switch (type) {
        case Desktop:
            return VmType.DESKTOP.value();
        case Server:
            return VmType.SERVER.value();
        default:
            return null;
        }
    }

    @Mapping(from = DisplayType.class, to = org.ovirt.engine.core.common.businessentities.DisplayType.class)
    public static org.ovirt.engine.core.common.businessentities.DisplayType map(DisplayType type, org.ovirt.engine.core.common.businessentities.DisplayType incoming) {
        switch(type) {
        case VNC:
            return org.ovirt.engine.core.common.businessentities.DisplayType.vnc;
        case SPICE:
            return org.ovirt.engine.core.common.businessentities.DisplayType.qxl;
        default:
            return null;
        }
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.DisplayType.class, to = String.class)
    public static String map(org.ovirt.engine.core.common.businessentities.DisplayType type, String incoming) {
        switch(type) {
        case vnc:
            return DisplayType.VNC.value();
        case qxl:
            return DisplayType.SPICE.value();
        default:
            return null;
        }
    }

    @Mapping(from = String.class, to = OriginType.class)
    public static OriginType map(String type, OriginType incoming) {
        try {
            return type.equals(RHEV) ? OriginType.valueOf(ENGINE.toUpperCase()) : OriginType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Mapping(from = OriginType.class, to = String.class)
    public static String map(OriginType type, String incoming) {
        String typeStr = type.name().toLowerCase();
        return typeStr.equals(ENGINE) ? RHEV : typeStr;
    }

    @Mapping(from = VMStatus.class, to = VmStatus.class)
    public static VmStatus map(VMStatus entityStatus, VmStatus template) {
        switch (entityStatus) {
        case Unassigned:        return VmStatus.UNASSIGNED;
        case Down:              return VmStatus.DOWN;
        case Up:                return VmStatus.UP;
        case PoweringUp:        return VmStatus.POWERING_UP;
        case PoweredDown:       return VmStatus.POWERED_DOWN;
        case Paused:            return VmStatus.PAUSED;
        case MigratingFrom:     return VmStatus.MIGRATING;
        case MigratingTo:       return VmStatus.MIGRATING;
        case Unknown:           return VmStatus.UNKNOWN;
        case NotResponding:     return VmStatus.NOT_RESPONDING;
        case WaitForLaunch:     return VmStatus.WAIT_FOR_LAUNCH;
        case RebootInProgress:  return VmStatus.REBOOT_IN_PROGRESS;
        case SavingState:       return VmStatus.SAVING_STATE;
        case RestoringState:    return VmStatus.RESTORING_STATE;
        case Suspended:         return VmStatus.SUSPENDED;
        case ImageLocked:       return VmStatus.IMAGE_LOCKED;
        case PoweringDown:      return VmStatus.POWERING_DOWN;
        default:                return null;
        }
    }

    @Mapping(from = BootSequence.class, to = List.class)
    public static List<Boot> map(BootSequence bootSequence,
            List<Boot> template) {
        List<Boot> boots = template != null ? template
                : new ArrayList<Boot>();
        switch (bootSequence) {
        case C:
            boots.add(getBoot(BootDevice.HD));
            break;
        case DC:
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.HD));
            break;
        case N:
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case CDN:
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case CND:
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.CDROM));
            break;
        case DCN:
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case DNC:
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.HD));
            break;
        case NCD:
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.CDROM));
            break;
        case NDC:
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.HD));
            break;
        case CD:
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.CDROM));
            break;
        case D:
            boots.add(getBoot(BootDevice.CDROM));
            break;
        case CN:
            boots.add(getBoot(BootDevice.HD));
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case DN:
            boots.add(getBoot(BootDevice.CDROM));
            boots.add(getBoot(BootDevice.NETWORK));
            break;
        case NC:
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.HD));
            break;
        case ND:
            boots.add(getBoot(BootDevice.NETWORK));
            boots.add(getBoot(BootDevice.CDROM));
            break;
        }
        return boots;
    }

    private static Boot getBoot(BootDevice device) {
        Boot boot = new Boot();
        boot.setDev(device.value());
        return boot;
    }

    @Mapping(from = Boot.class, to = List.class)
    public static BootSequence map(List<Boot> boot, BootSequence template) {
        Set devSet = EnumSet.noneOf(BootDevice.class);
        for (Boot b : boot) {
            if (b.isSetDev()) {
                BootDevice dev = BootDevice.fromValue(b.getDev());
                if (dev != null) {
                    devSet.add(dev);
                }
            }
        }

        List<BootDevice> devs = new ArrayList<BootDevice>(devSet);
        if (devs.size() == 1) {
            switch (devs.get(0)) {
            case CDROM:
                return BootSequence.D;
            case HD:
                return BootSequence.C;
            case NETWORK:
                return BootSequence.N;
            }
        } else if (devs.size() == 2) {
            switch (devs.get(0)) {
            case CDROM:
                switch (devs.get(1)) {
                case HD:
                    return BootSequence.DC;
                case NETWORK:
                    return BootSequence.DN;
                }
                break;
            case HD:
                switch (devs.get(1)) {
                case CDROM:
                    return BootSequence.CD;
                case NETWORK:
                    return BootSequence.CN;
                }
                break;
            case NETWORK:
                switch (devs.get(1)) {
                case HD:
                    return BootSequence.NC;
                case CDROM:
                    return BootSequence.ND;
                }
                break;
            }
        } else if (devs.size() == 3) {
            switch (devs.get(0)) {
            case CDROM:
                switch (devs.get(1)) {
                case HD:
                    return BootSequence.DCN;
                case NETWORK:
                    return BootSequence.DNC;
                }
                break;
            case HD:
                switch (devs.get(1)) {
                case CDROM:
                    return BootSequence.CDN;
                case NETWORK:
                    return BootSequence.CND;
                }
                break;
            case NETWORK:
                switch (devs.get(1)) {
                case HD:
                    return BootSequence.NCD;
                case CDROM:
                    return BootSequence.NDC;
                }
                break;
            }
        }
        return null;
    }

    @Mapping(from = VmOsType.class, to = OsType.class)
    public static OsType map(VmOsType type, OsType incoming) {
        switch (type) {
        case Unassigned:
            return OsType.UNASSIGNED;
        case WindowsXP:
            return OsType.WINDOWS_XP;
        case Windows2003:
            return OsType.WINDOWS_2003;
        case Windows2008:
            return OsType.WINDOWS_2008;
        case Other:
            return OsType.OTHER;
        case OtherLinux:
            return OsType.OTHER_LINUX;
        case RHEL5:
            return OsType.RHEL_5;
        case RHEL4:
            return OsType.RHEL_4;
        case RHEL3:
            return OsType.RHEL_3;
        case Windows2003x64:
            return OsType.WINDOWS_2003X64;
        case Windows7:
            return OsType.WINDOWS_7;
        case Windows7x64:
            return OsType.WINDOWS_7X64;
        case RHEL5x64:
            return OsType.RHEL_5X64;
        case RHEL4x64:
            return OsType.RHEL_4X64;
        case RHEL3x64:
            return OsType.RHEL_3X64;
        case Windows2008x64:
            return OsType.WINDOWS_2008X64;
        case Windows2008R2x64:
            return OsType.WINDOWS_2008R2;
        case RHEL6:
            return OsType.RHEL_6;
        case RHEL6x64:
            return OsType.RHEL_6X64;

        default:
            return null;
        }
    }

    @Mapping(from = OsType.class, to = VmOsType.class)
    public static VmOsType map(OsType type, VmOsType incoming) {
        switch (type) {
        case UNASSIGNED:
            return VmOsType.Unassigned;
        case WINDOWS_XP:
            return VmOsType.WindowsXP;
        case WINDOWS_2003:
            return VmOsType.Windows2003;
        case WINDOWS_2008:
            return VmOsType.Windows2008;
        case OTHER:
            return VmOsType.Other;
        case OTHER_LINUX:
            return VmOsType.OtherLinux;
        case RHEL_5:
            return VmOsType.RHEL5;
        case RHEL_4:
            return VmOsType.RHEL4;
        case RHEL_3:
            return VmOsType.RHEL3;
        case WINDOWS_2003X64:
            return VmOsType.Windows2003x64;
        case WINDOWS_7:
            return VmOsType.Windows7;
        case WINDOWS_7X64:
            return VmOsType.Windows7x64;
        case RHEL_5X64:
            return VmOsType.RHEL5x64;
        case RHEL_4X64:
            return VmOsType.RHEL4x64;
        case RHEL_3X64:
            return VmOsType.RHEL3x64;
        case WINDOWS_2008X64:
            return VmOsType.Windows2008x64;
        case WINDOWS_2008R2:
            return VmOsType.Windows2008R2x64;
        case RHEL_6:
            return VmOsType.RHEL6;
        case RHEL_6X64:
            return VmOsType.RHEL6x64;

        default:
            return null;
        }
    }
}
