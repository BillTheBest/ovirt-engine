package org.ovirt.engine.core.common.businessentities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsDynamic")
@Entity
@Table(name = "vds_dynamic")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class VdsDynamic implements BusinessEntity<Guid> {
    private static final long serialVersionUID = -6010035855157006935L;

    @Id
    // @GeneratedValue(generator = "system-uuid")
    // @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "Id")
    @Type(type = "guid")
    private Guid id;

    @Column(name = "status")
    private VDSStatus status = VDSStatus.Unassigned;

    @Column(name = "cpu_cores")
    private Integer cpu_cores;

    @Column(name = "cpu_model")
    private String cpu_model;

    @Column(name = "cpu_speed_mh", scale = 18, precision = 0)
    private BigDecimal cpu_speed_mh = BigDecimal.valueOf(0.0);

    @Column(name = "if_total_speed")
    private String if_total_speed;

    @Column(name = "kvm_enabled")
    private Boolean kvm_enabled;

    @Column(name = "physical_mem_mb")
    private Integer physical_mem_mb;

    @Column(name = "mem_commited")
    private Integer mem_commited;

    @Column(name = "vm_active")
    private Integer vm_active;

    @Column(name = "vm_count")
    private int vm_count;

    @Column(name = "vm_migrating")
    private Integer vm_migrating;

    @Column(name = "reserved_mem")
    private Integer reserved_mem ;

    @Column(name = "guest_overhead")
    private Integer guest_overhead;

    @Column(name = "software_version")
    private String softwareVersion;

    @Column(name = "version_name")
    private String versionName;

    @Column(name = "build_name")
    private String buildName;

    @Column(name = "previous_status")
    private VDSStatus previous_status = VDSStatus.Unassigned;

    @Column(name = "cpu_flags")
    private String cpu_flags;

    @Column(name = "cpu_over_commit_time_stamp")
    private Date cpu_over_commit_time_stamp;

    @Column(name = "hypervisor_type")
    private HypervisorType hypervisor_type = HypervisorType.KVM;

    @Column(name = "vms_cores_count")
    private Integer vms_cores_count;

    @Column(name = "pending_vcpus_count")
    private Integer pending_vcpus_count;

    @Column(name = "cpu_sockets")
    private Integer cpu_sockets;

    @Column(name = "net_config_dirty")
    private Boolean net_config_dirty;

    @XmlElement(name = "supported_cluster_levels")
    @Column(name = "supported_cluster_levels")
    private String supported_cluster_levels;

    @XmlElement(name = "supported_engines")
    @Column(name = "supported_engines")
    private String supported_engines;

    @Column(name = "host_os")
    private String host_os;

    @Column(name = "kvm_version")
    private String kvm_version;

    @Column(name = "spice_version")
    private String spice_version;

    @Column(name = "kernel_version")
    private String kernel_version;

    @Column(name = "iscsi_initiator_name")
    private String iScsiInitiatorName;

    @Column(name = "transparent_hugepages_state")
    private VdsTransparentHugePagesState transparentHugePagesState = VdsTransparentHugePagesState.Never;
    @Column(name = "anonymous_hugepages")
    private int anonymousHugePages;

    @Column(name = "hooks")
    private String hooksStr;

    @XmlElement(name = "NonOperationalReason")
    @Column(name = "non_operational_reason")
    private NonOperationalReason nonOperationalReason = NonOperationalReason.NONE;

    @Transient
    private Integer pending_vmem_size;

    @Transient
    private VdsVersion mVdsVersion;

    @Transient
    private java.util.HashSet<Version> _supportedClusterVersionsSet;

    @Transient
    private java.util.HashSet<Version> _supportedENGINESVersionsSet;

    @XmlElement(name = "Version")
    public void setVersion(VdsVersion value) {
        mVdsVersion = value;
    }

    public VdsVersion getVersion() {
        return mVdsVersion;
    }

    public VdsDynamic() {
        mVdsVersion = new VdsVersion();
        mem_commited = 0;
        reserved_mem = 1024;
        pending_vcpus_count = 0;
        pending_vmem_size = 0;
        transparentHugePagesState = VdsTransparentHugePagesState.Never;
    }

    public VdsDynamic(Integer cpu_cores, String cpu_model, Double cpu_speed_mh, String if_total_speed,
                      Boolean kvm_enabled, Integer mem_commited, Integer physical_mem_mb, int status, Guid vds_id,
                      Integer vm_active, int vm_count, Integer vm_migrating, Integer reserved_mem, Integer guest_overhead,
                      VDSStatus previous_status, String software_version, String version_name, String build_name,
                      Date cpu_over_commit_time_stamp, HypervisorType hypervisor_type, Integer pending_vcpus_count,
                      Integer pending_vmem_sizeField, Boolean net_config_dirty) {
        mVdsVersion = new VdsVersion();
        this.cpu_cores = cpu_cores;
        this.cpu_model = cpu_model;
        this.cpu_speed_mh = BigDecimal.valueOf(cpu_speed_mh);
        this.if_total_speed = if_total_speed;
        this.kvm_enabled = kvm_enabled;
        this.mem_commited = mem_commited;
        this.physical_mem_mb = physical_mem_mb;
        this.status = VDSStatus.forValue(status);
        this.id = vds_id;
        this.vm_active = vm_active;
        this.vm_count = vm_count;
        this.vm_migrating = vm_migrating;
        this.reserved_mem = reserved_mem;
        this.guest_overhead = guest_overhead;
        this.previous_status = previous_status;
        this.setsoftware_version(software_version);
        this.setversion_name(version_name);
        this.setbuild_name(build_name);
        this.setcpu_over_commit_time_stamp(cpu_over_commit_time_stamp);
        this.sethypervisor_type(hypervisor_type);
        this.pending_vcpus_count = pending_vcpus_count;
        this.pending_vmem_size = pending_vmem_sizeField;
        this.net_config_dirty = net_config_dirty;
        this.transparentHugePagesState = VdsTransparentHugePagesState.Never;
    }

    @XmlElement(nillable = true)
    public Integer getcpu_cores() {
        return this.cpu_cores;
    }

    public void setcpu_cores(Integer value) {
        this.cpu_cores = value;
    }

    @XmlElement(nillable = true)
    public Integer getcpu_sockets() {
        return this.cpu_sockets;
    }

    public void setcpu_sockets(Integer value) {
        this.cpu_sockets = value;
    }

    @XmlElement
    public String getcpu_model() {
        return this.cpu_model;
    }

    public void setcpu_model(String value) {
        this.cpu_model = value;
    }

    @XmlElement(nillable = true)
    public Double getcpu_speed_mh() {
        return this.cpu_speed_mh.doubleValue();
    }

    public void setcpu_speed_mh(Double value) {
        this.cpu_speed_mh = BigDecimal.valueOf(value);
    }

    @XmlElement
    public String getif_total_speed() {
        return this.if_total_speed;
    }

    public void setif_total_speed(String value) {
        this.if_total_speed = value;
    }

    @XmlElement(nillable = true)
    public Boolean getkvm_enabled() {
        return this.kvm_enabled;
    }

    public void setkvm_enabled(Boolean value) {
        this.kvm_enabled = value;
    }

    @XmlElement(nillable = true)
    public Integer getmem_commited() {
        return this.mem_commited;
    }

    public void setmem_commited(Integer value) {
        this.mem_commited = value;
    }

    @XmlElement(nillable = true)
    public Integer getphysical_mem_mb() {
        return this.physical_mem_mb;
    }

    public void setphysical_mem_mb(Integer value) {
        this.physical_mem_mb = value;
    }

    @XmlElement
    public VDSStatus getstatus() {
        return status;
    }

    public void setstatus(VDSStatus value) {
        this.status = value;
    }

    @Override
    @XmlElement(name = "Id")
    public Guid getId() {
        return this.id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @XmlElement(nillable = true)
    public Integer getvm_active() {
        return this.vm_active;
    }

    public void setvm_active(Integer value) {
        this.vm_active = value;
    }

    @XmlElement
    public int getvm_count() {
        return this.vm_count;
    }

    public void setvm_count(int value) {
        this.vm_count = value;
    }

    @XmlElement(nillable = true)
    public Integer getvms_cores_count() {
        return this.vms_cores_count;
    }

    public void setvms_cores_count(Integer value) {
        this.vms_cores_count = value;
    }

    @XmlElement(nillable = true)
    public Integer getvm_migrating() {
        return this.vm_migrating;
    }

    public void setvm_migrating(Integer value) {
        this.vm_migrating = value;
    }

    @XmlElement(nillable = true)
    public Integer getreserved_mem() {
        return this.reserved_mem;
    }

    public void setreserved_mem(Integer value) {
        this.reserved_mem = value;
    }

    @XmlElement(nillable = true)
    public Integer getguest_overhead() {
        return this.guest_overhead;
    }

    public void setguest_overhead(Integer value) {
        this.guest_overhead = value;
    }

    @XmlElement
    public VDSStatus getprevious_status() {
        return this.previous_status;
    }

    public void setprevious_status(VDSStatus value) {
        this.previous_status = value;
    }

    @XmlElement
    public String getsoftware_version() {
        if (this.getVersion().getFullVersion() == null) {
            return null;
        }
        return this.getVersion().getFullVersion().toString();
    }

    public void setsoftware_version(String value) {
        this.softwareVersion = value;
        if (!StringHelper.isNullOrEmpty(value)) {
            String[] vers = value.split("[.]", -1);
            this.getVersion().setSoftwareVersion(vers[0]);
            if (vers.length > 1) {
                this.getVersion().setSoftwareVersion(this.getVersion().getSoftwareVersion() + "." + vers[1]);
            }

            if (vers.length > 3) {
                this.getVersion().setSoftwareRevision(StringFormat.format("%s.%s", vers[2], vers[3]));
            }
        }
    }

    @XmlElement
    public String getversion_name() {
        return this.getVersion().getVersionName();
    }

    public void setversion_name(String value) {
        this.versionName = value;
        this.getVersion().setVersionName(value);
    }

    @XmlElement
    public String getbuild_name() {
        return this.getVersion().getBuildName();
    }

    public void setbuild_name(String value) {
        this.buildName = value;
        this.getVersion().setBuildName(value);
    }

    @XmlElement
    public String getcpu_flags() {
        return this.cpu_flags;
    }

    public void setcpu_flags(String value) {
        this.cpu_flags = value;
    }

    @XmlElement(nillable = true)
    public Date getcpu_over_commit_time_stamp() {
        return this.cpu_over_commit_time_stamp;
    }

    public void setcpu_over_commit_time_stamp(Date value) {
        this.cpu_over_commit_time_stamp = value;
    }

    @XmlElement(nillable = true)
    public HypervisorType gethypervisor_type() {
        return this.hypervisor_type;
    }

    public void sethypervisor_type(HypervisorType value) {
        this.hypervisor_type = value;
    }

    @XmlElement(nillable = true)
    public Integer getpending_vcpus_count() {
        return this.pending_vcpus_count;
    }

    public void setpending_vcpus_count(Integer value) {
        this.pending_vcpus_count = value;
    }

    @XmlElement
    public int getpending_vmem_size() {
        return this.pending_vmem_size;
    }

    public void setpending_vmem_size(int value) {
        this.pending_vmem_size = value;
    }

    @XmlElement(nillable = true)
    public Boolean getnet_config_dirty() {
        return this.net_config_dirty;
    }

    public void setnet_config_dirty(Boolean value) {
        this.net_config_dirty = value;
    }

    public String getsupported_cluster_levels() {
        return supported_cluster_levels;
    }

    public void setsupported_cluster_levels(String value) {
        supported_cluster_levels = value;
    }

    public java.util.HashSet<Version> getSupportedClusterVersionsSet() {
        if (_supportedClusterVersionsSet == null && !StringHelper.isNullOrEmpty(getsupported_cluster_levels())) {
            _supportedClusterVersionsSet = new HashSet<Version>();
            for (String ver : getsupported_cluster_levels().split("[,]", -1)) {
                try {
                    _supportedClusterVersionsSet.add(new Version(ver));
                } catch (java.lang.Exception e) {
                    log.errorFormat("Could not parse supported cluster version {0} for vds {1}", ver, getId());
                }
            }
        }
        return _supportedClusterVersionsSet;
    }

    public String getsupported_engines() {
        return supported_engines;
    }

    public void setsupported_engines(String value) {
        supported_engines = value;
    }

    public java.util.HashSet<Version> getSupportedENGINESVersionsSet() {
        if (_supportedENGINESVersionsSet == null && !StringHelper.isNullOrEmpty(getsupported_engines())) {
            _supportedENGINESVersionsSet = new HashSet<Version>();
            for (String ver : getsupported_engines().split("[,]", -1)) {
                try {
                    _supportedENGINESVersionsSet.add(new Version(ver));
                } catch (java.lang.Exception e) {
                    log.errorFormat("Could not parse supported engine version {0} for vds {1}", ver, getId());
                }
            }
        }
        return _supportedENGINESVersionsSet;
    }

    @XmlElement
    public String gethost_os() {
        return this.host_os;
    }

    public void sethost_os(String value) {
        this.host_os = value;
    }

    @XmlElement
    public String getkvm_version() {
        return this.kvm_version;
    }

    public void setkvm_version(String value) {
        this.kvm_version = value;
    }

    @XmlElement
    public String getspice_version() {
        return this.spice_version;
    }

    public void setspice_version(String value) {
        this.spice_version = value;
    }

    @XmlElement
    public String getkernel_version() {
        return this.kernel_version;
    }

    public void setkernel_version(String value) {
        this.kernel_version = value;
    }

    @XmlElement(name = "IScsiInitiatorName")
    public String getIScsiInitiatorName() {
        return this.iScsiInitiatorName;
    }

    public void setIScsiInitiatorName(String value) {
        this.iScsiInitiatorName = value;
    }

    @XmlElement(name = "TransparentHugePagesState")
    public VdsTransparentHugePagesState getTransparentHugePagesState() {
        return this.transparentHugePagesState;
    }

    public void setTransparentHugePagesState(VdsTransparentHugePagesState value) {
        this.transparentHugePagesState = value;
    }

    @XmlElement(name = "AnonymousHugePages")
    public int getAnonymousHugePages() {
        return this.anonymousHugePages;
    }

    public void setAnonymousHugePages(int value) {
        this.anonymousHugePages = value;
    }

    public void setHooksStr(String hooksStr) {
        this.hooksStr = hooksStr;
    }

    public String getHooksStr() {
        return hooksStr;
    }

    private static LogCompat log = LogFactoryCompat.getLog(VdsDynamic.class);

    public NonOperationalReason getNonOperationalReason() {
        return nonOperationalReason;
    }

    public void setNonOperationalReason(NonOperationalReason nonOperationalReason) {
        this.nonOperationalReason = (nonOperationalReason == null ? NonOperationalReason.NONE : nonOperationalReason);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result + ((_supportedClusterVersionsSet == null) ? 0 : _supportedClusterVersionsSet.hashCode());
        result = prime * result + ((_supportedENGINESVersionsSet == null) ? 0 : _supportedENGINESVersionsSet.hashCode());
        result = prime * result + anonymousHugePages;
        result = prime * result + ((buildName == null) ? 0 : buildName.hashCode());
        result = prime * result + ((cpu_cores == null) ? 0 : cpu_cores.hashCode());
        result = prime * result + ((cpu_flags == null) ? 0 : cpu_flags.hashCode());
        result = prime * result + ((cpu_model == null) ? 0 : cpu_model.hashCode());
        result = prime * result + ((cpu_over_commit_time_stamp == null) ? 0 : cpu_over_commit_time_stamp.hashCode());
        result = prime * result + ((cpu_sockets == null) ? 0 : cpu_sockets.hashCode());
        result = prime * result + ((cpu_speed_mh == null) ? 0 : cpu_speed_mh.hashCode());
        result = prime * result + ((guest_overhead == null) ? 0 : guest_overhead.hashCode());
        result = prime * result + ((hooksStr == null) ? 0 : hooksStr.hashCode());
        result = prime * result + ((host_os == null) ? 0 : host_os.hashCode());
        result = prime * result + ((hypervisor_type == null) ? 0 : hypervisor_type.hashCode());
        result = prime * result + ((iScsiInitiatorName == null) ? 0 : iScsiInitiatorName.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((if_total_speed == null) ? 0 : if_total_speed.hashCode());
        result = prime * result + ((kernel_version == null) ? 0 : kernel_version.hashCode());
        result = prime * result + ((kvm_enabled == null) ? 0 : kvm_enabled.hashCode());
        result = prime * result + ((kvm_version == null) ? 0 : kvm_version.hashCode());
        result = prime * result + ((mVdsVersion == null) ? 0 : mVdsVersion.hashCode());
        result = prime * result + ((mem_commited == null) ? 0 : mem_commited.hashCode());
        result = prime * result + ((net_config_dirty == null) ? 0 : net_config_dirty.hashCode());
        result = prime * result + ((nonOperationalReason == null) ? 0 : nonOperationalReason.hashCode());
        result = prime * result + ((pending_vcpus_count == null) ? 0 : pending_vcpus_count.hashCode());
        result = prime * result + ((pending_vmem_size == null) ? 0 : pending_vmem_size.hashCode());
        result = prime * result + ((physical_mem_mb == null) ? 0 : physical_mem_mb.hashCode());
        result = prime * result + ((previous_status == null) ? 0 : previous_status.hashCode());
        result = prime * result + ((reserved_mem == null) ? 0 : reserved_mem.hashCode());
        result = prime * result + ((softwareVersion == null) ? 0 : softwareVersion.hashCode());
        result = prime * result + ((spice_version == null) ? 0 : spice_version.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((supported_cluster_levels == null) ? 0 : supported_cluster_levels.hashCode());
        result = prime * result + ((supported_engines == null) ? 0 : supported_engines.hashCode());
        result = prime * result + ((transparentHugePagesState == null) ? 0 : transparentHugePagesState.hashCode());
        result = prime * result + ((versionName == null) ? 0 : versionName.hashCode());
        result = prime * result + ((vm_active == null) ? 0 : vm_active.hashCode());
        result = prime * result + vm_count;
        result = prime * result + ((vm_migrating == null) ? 0 : vm_migrating.hashCode());
        result = prime * result + ((vms_cores_count == null) ? 0 : vms_cores_count.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VdsDynamic other = (VdsDynamic) obj;
        if (_supportedClusterVersionsSet == null) {
            if (other._supportedClusterVersionsSet != null)
                return false;
        } else if (!_supportedClusterVersionsSet.equals(other._supportedClusterVersionsSet))
            return false;
        if (_supportedENGINESVersionsSet == null) {
            if (other._supportedENGINESVersionsSet != null)
                return false;
        } else if (!_supportedENGINESVersionsSet.equals(other._supportedENGINESVersionsSet))
            return false;
        if (anonymousHugePages != other.anonymousHugePages)
            return false;
        if (buildName == null) {
            if (other.buildName != null)
                return false;
        } else if (!buildName.equals(other.buildName))
            return false;
        if (cpu_cores == null) {
            if (other.cpu_cores != null)
                return false;
        } else if (!cpu_cores.equals(other.cpu_cores))
            return false;
        if (cpu_flags == null) {
            if (other.cpu_flags != null)
                return false;
        } else if (!cpu_flags.equals(other.cpu_flags))
            return false;
        if (cpu_model == null) {
            if (other.cpu_model != null)
                return false;
        } else if (!cpu_model.equals(other.cpu_model))
            return false;
        if (cpu_over_commit_time_stamp == null) {
            if (other.cpu_over_commit_time_stamp != null)
                return false;
        } else if (!cpu_over_commit_time_stamp.equals(other.cpu_over_commit_time_stamp))
            return false;
        if (cpu_sockets == null) {
            if (other.cpu_sockets != null)
                return false;
        } else if (!cpu_sockets.equals(other.cpu_sockets))
            return false;
        if (cpu_speed_mh == null) {
            if (other.cpu_speed_mh != null)
                return false;
        } else if (!cpu_speed_mh.equals(other.cpu_speed_mh))
            return false;
        if (guest_overhead == null) {
            if (other.guest_overhead != null)
                return false;
        } else if (!guest_overhead.equals(other.guest_overhead))
            return false;
        if (hooksStr == null) {
            if (other.hooksStr != null)
                return false;
        } else if (!hooksStr.equals(other.hooksStr))
            return false;
        if (host_os == null) {
            if (other.host_os != null)
                return false;
        } else if (!host_os.equals(other.host_os))
            return false;
        if (hypervisor_type != other.hypervisor_type)
            return false;
        if (iScsiInitiatorName == null) {
            if (other.iScsiInitiatorName != null)
                return false;
        } else if (!iScsiInitiatorName.equals(other.iScsiInitiatorName))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (if_total_speed == null) {
            if (other.if_total_speed != null)
                return false;
        } else if (!if_total_speed.equals(other.if_total_speed))
            return false;
        if (kernel_version == null) {
            if (other.kernel_version != null)
                return false;
        } else if (!kernel_version.equals(other.kernel_version))
            return false;
        if (kvm_enabled == null) {
            if (other.kvm_enabled != null)
                return false;
        } else if (!kvm_enabled.equals(other.kvm_enabled))
            return false;
        if (kvm_version == null) {
            if (other.kvm_version != null)
                return false;
        } else if (!kvm_version.equals(other.kvm_version))
            return false;
        if (mVdsVersion == null) {
            if (other.mVdsVersion != null)
                return false;
        } else if (!mVdsVersion.equals(other.mVdsVersion))
            return false;
        if (mem_commited == null) {
            if (other.mem_commited != null)
                return false;
        } else if (!mem_commited.equals(other.mem_commited))
            return false;
        if (net_config_dirty == null) {
            if (other.net_config_dirty != null)
                return false;
        } else if (!net_config_dirty.equals(other.net_config_dirty))
            return false;
        if (nonOperationalReason != other.nonOperationalReason)
            return false;
        if (pending_vcpus_count == null) {
            if (other.pending_vcpus_count != null)
                return false;
        } else if (!pending_vcpus_count.equals(other.pending_vcpus_count))
            return false;
        if (pending_vmem_size == null) {
            if (other.pending_vmem_size != null)
                return false;
        } else if (!pending_vmem_size.equals(other.pending_vmem_size))
            return false;
        if (physical_mem_mb == null) {
            if (other.physical_mem_mb != null)
                return false;
        } else if (!physical_mem_mb.equals(other.physical_mem_mb))
            return false;
        if (previous_status != other.previous_status)
            return false;
        if (reserved_mem == null) {
            if (other.reserved_mem != null)
                return false;
        } else if (!reserved_mem.equals(other.reserved_mem))
            return false;
        if (getsoftware_version() == null) {
            if (other.getsoftware_version() != null)
                return false;
        } else if (!getsoftware_version().equals(other.getsoftware_version()))
            return false;
        if (spice_version == null) {
            if (other.spice_version != null)
                return false;
        } else if (!spice_version.equals(other.spice_version))
            return false;
        if (status != other.status)
            return false;
        if (supported_cluster_levels == null) {
            if (other.supported_cluster_levels != null)
                return false;
        } else if (!supported_cluster_levels.equals(other.supported_cluster_levels))
            return false;
        if (supported_engines == null) {
            if (other.supported_engines != null)
                return false;
        } else if (!supported_engines.equals(other.supported_engines))
            return false;
        if (transparentHugePagesState != other.transparentHugePagesState)
            return false;
        if (versionName == null) {
            if (other.versionName != null)
                return false;
        } else if (!versionName.equals(other.versionName))
            return false;
        if (vm_active == null) {
            if (other.vm_active != null)
                return false;
        } else if (!vm_active.equals(other.vm_active))
            return false;
        if (vm_count != other.vm_count)
            return false;
        if (vm_migrating == null) {
            if (other.vm_migrating != null)
                return false;
        } else if (!vm_migrating.equals(other.vm_migrating))
            return false;
        if (vms_cores_count == null) {
            if (other.vms_cores_count != null)
                return false;
        } else if (!vms_cores_count.equals(other.vms_cores_count))
            return false;
        return true;
    }
}
