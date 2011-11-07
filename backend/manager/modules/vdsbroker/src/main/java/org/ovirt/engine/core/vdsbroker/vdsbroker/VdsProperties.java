package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.MigrationMethod;

//-----------------------------------------------------
//
//-----------------------------------------------------
//TODO: BrokerFactory, StatusForXmlRpc can be consolidated for all brokers
//      (currently in vdsBroker and irsBroker)
//public static class VdsBrokerFactory
//{
//    //TODO: fucked up because could not make real factory here
//    public static IVdsBroker create(string host, uint port)
//    {
//        return new VdsBroker(host, port);
//    }
//}

public final class VdsProperties {
    // vds configuration (i.e. VdsStatic)
    // vds runtime (i.e. VdsDynamic req getVdsCapabilities)
    public static final String time_zone = "timeZone";
    public static final String utc_diff = "timeOffset";
    public static final String cpu_flags = "cpuFlags";

    public static final String SpiceSecureChannels = "spiceSecureChannels";
    public static final String cpu_cores = "cpuCores";
    public static final String cpu_sockets = "cpuSockets";
    public static final String cpu_model = "cpuModel";
    public static final String cpu_speed_mh = "cpuSpeed";
    public static final String if_total_speed = "eth0Speed";
    public static final String kvm_enabled = "kvmEnabled";
    public static final String physical_mem_mb = "memSize";
    public static final String Protocol = "protocol";
    public static final String vm_types = "vmTypes"; // Currently not in use
    public static final String reservedMem = "reservedMem";
    // vds runtime (i.e. VdsDynamic req getVdsStats)
    public static final String netConfigDirty = "netConfigDirty";
    public static final String status = "status"; // in vm also
    public static final String cpu_idle = "cpuIdle";
    public static final String cpu_load = "cpuLoad";
    public static final String cpu_sys = "cpuSys"; // in vm also
    public static final String cpu_user = "cpuUser"; // in vm also
    public static final String destroy_rate = "destroyRate";
    public static final String destroy_total = "destroyTotal";
    public static final String elapsed_time = "elapsedTime"; // in vm also
    public static final String launch_rate = "launchRate";
    public static final String launch_total = "launchTotal";
    public static final String vds_usage_mem_percent = "memUsed";
    public static final String rx_dropped = "rxDropped"; // in vm also
    public static final String guestOverhead = "guestOverhead";
    public static final String rx_rate = "rxRate"; // in vm also
    public static final String tx_dropped = "txDropped"; // in vm also
    public static final String tx_rate = "txRate"; // in vm also
    public static final String iface_status = "state";
    public static final String vm_active = "vmActive";
    public static final String vm_count = "vmCount";
    public static final String vm_migrating = "vmMigrating";
    public static final String images_last_check = "imagesLastCheck";
    public static final String images_last_delay = "imagesLastDelay";
    public static final String network = "network";
    public static final String bootproto = "BOOTPROTO";
    public static final String stp = "STP";
    public static final String bonding_opts = "BONDING_OPTS";
    public static final String dhcp = "dhcp";
    public static final String force = "force";
    public static final String connectivityCheck = "connectivityCheck";
    public static final String connectivityTimeout = "connectivityTimeout";
    public static final String ipaddr = "IPADDR";
    public static final String netmask = "NETMASK";
    public static final String gateway = "GATEWAY";
    public static final String GLOBAL_GATEWAY = "gateway";
    public static final String displaynetwork = "displayNetwork";
    public static final String supported_cluster_levels = "clusterLevels";
    public static final String supported_engines = "supportedENGINEs";
    public static final String emulatedMachine = "emulatedMachine";
    public static final String host_os = "operatingSystem";
    public static final String packages = "packages";
    public static final String packages2 = "packages2";
    public static final String package_name = "name";
    public static final String package_version = "version";
    public static final String package_release = "release";
    public static final String kvmPackageName = "kvm";
    public static final String spicePackageName = "qspice-libs";
    public static final String kernelPackageName = "kernel";
    public static final String iSCSIInitiatorName = "ISCSIInitiatorName";
    public static final String qemuKvmPackageName = "qemu-kvm";
    public static final String spiceServerPackageName = "spice-server";

    public static final String mem_available = "memAvailable";
    public static final String mem_shared = "memShared";
    public static final String mem_usage = "memUsed";
    // swap
    public static final String swap_free = "swapFree";
    public static final String swap_total = "swapTotal";
    // ksm
    public static final String ksm_cpu_percent = "ksmCpu";
    public static final String ksm_pages = "ksmPages";
    public static final String ksm_state = "ksmState";
    public static final String transparent_huge_pages_state = "thpState";
    public static final String anonymous_transparent_huge_pages = "anonHugePages";
    public static final String transparent_huge_pages = "transparentHugePages";

    public static final String vm_network = "network";
    public static final String if_name = "name";
    public static final String if_speed = "speed";
    public static final String exit_code = "exitCode";
    public static final String exit_message = "exitMessage";
    public static final String multimedia_ports = "multimediaPorts";
    public static final String monitorResponse = "monitorResponse";

    // Network related
    public static final String network_nics = "nics";
    public static final String network_vlans = "vlans";
    public static final String network_networks = "networks";
    public static final String network_bondings = "bondings";
    public static final String network_lastclientinterface = "lastClientIface";

    // Disks usage configuration
    public static final String VM_DISKS_USAGE = "disksUsage";

    // Disks configuration
    public static final String vm_disks = "disks";
    public static final String vm_disk_name = "name";
    public static final String vm_disk_read_rate = "readRate";
    public static final String vm_disk_write_rate = "writeRate";
    public static final String vm_disk_read_latency = "readLatency";
    public static final String vm_disk_write_latency = "writeLatency";
    public static final String vm_disk_flush_latency = "flushLatency";
    public static final String disk_actual_size = "actualsize";
    public static final String disk_true_size = "truesize";
    public static final String image_group_id = "imageID";
    // vm configuration (i.e. VmStatic)
    public static final String mem_size_mb = "memSize";
    public static final String nic_type = "nicModel";
    public static final String bridge = "bridge";
    public static final String num_of_monitors = "spiceMonitors";
    public static final String num_of_cpus = "smp";
    public static final String cores_per_socket = "smpCoresPerSocket";
    public static final String vm_name = "vmName";
    // vm configuration (i.e. VmDynamic)
    public static final String vm_guid = "vmId";
    public static final String guest_cur_user_name = "username";
    public static final String vm_ip = "guestIPs";
    public static final String vm_usage_mem_percent = "memUsage";
    public static final String vm_if_id = "ifid"; // currently not in use
    public static final String vm_if_name = "name";
    public static final String vm_line_rate = "speed";
    public static final String mac_addr = "macAddr";
    public static final String vm_host = "guestName";
    public static final String app_list = "appsList";
    public static final String guest_os = "guestOs";
    public static final String display = "display";
    public static final String display_port = "displayPort";
    public static final String display_secure_port = "displaySecurePort";
    public static final String displayType = "displayType";
    public static final String displayIp = "displayIp";
    public static final String vm_pid = "pid";
    public static final String vm_type = "vmType";
    public static final String guest_last_login_time = "lastLogin";
    public static final String guest_last_logout_time = "lastLogout";
    public static final String launch_paused_param = "launchPaused";
    public static final String session = "session";
    public static final String spiceSslCipherSuite = "spiceSslCipherSuite";

    public static final String DriveC = "hda"; // drive C:
    public static final String DriveE = "hdb"; // drive E: (D: is the CD-ROM)
    public static final String DriveF = "hdc"; // drive F:
    public static final String DriveG = "hdd"; // drive G:

    public static final String kvmEnable = "kvmEnable"; // Optional
    public static final String acpiEnable = "acpiEnable"; // Optional
    public static final String win2kHackEnable = "win2kHackEnable"; // Optional
    public static final String initFromFloppy = "initFromFloppy"; // Optional
    public static final String sysprepInf = "sysprepInf"; // for the binary sys
                                                          // prep
    public static final String Boot = "boot"; // Optional
    public static final String CDRom = "cdrom"; // Optional
    public static final String Floppy = "floppy"; // Optional
    public static final String Snapshot = "snapshotFile"; // Optional
    public static final String soundDevice = "soundDevice";
    public static final String cpuType = "cpuType";
    public static final String niceLevel = "nice";
    public static final String hiberVolHandle = "hiberVolHandle";
    public static final String pauseCode = "pauseCode";
    public static final String KeyboardLayout = "keyboardLayout";
    public static final String TabletEnable = "tabletEnable";
    public static final String PitReinjection = "pitReinjection";
    public static final String InitrdUrl = "initrd";
    public static final String KernelUrl = "kernel";
    public static final String KernelParams = "kernelArgs";
    public static final String Custom = "custom";

    public static final String clientIp = "clientIp";
    // migration
    public static final String src = "src";
    public static final String dst = "dst";
    public static final String method = "method";
    public static final String offline = "offline";
    public static final String online = "online";
    public static final String domains = "storageDomains";
    public static final String hooks = "hooks";

    // storage domains
    public static final String code = "code";
    public static final String lastCheck = "lastCheck";
    public static final String delay = "delay";

    public static String MigrationMethostoString(MigrationMethod method) {
        if (method == MigrationMethod.OFFLINE) {
            return offline;
        } else if (method == MigrationMethod.ONLINE) {
            return online;
        } else {
            return "";
        }
    }

    // properties for ServerConnectionListReturnForXmlRpc
    public static final String serverType = "serverType";
    public static final String target = "target";
}
