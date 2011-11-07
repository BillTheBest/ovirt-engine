package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

//VB & C# TO JAVA CONVERTER NOTE: There is no Java equivalent to C# namespace aliases:
//using Timer=System.Timers.Timer;

//VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VDS")
public class VDS extends IVdcQueryable implements INotifyPropertyChanged, Serializable {
    private static final long serialVersionUID = -7893976203379789926L;
    private VdsStatic mVdsStatic;
    private VdsDynamic mVdsDynamic;
    private VdsStatistics mVdsStatistics;
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "InterfaceList")
    private ArrayList<VdsNetworkInterface> mInterfaceList;
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "NetworkList")
    private java.util.ArrayList<network> mNetworkList;

    public VDS() {
        mVdsStatic = new VdsStatic();
        mVdsDynamic = new VdsDynamic();
        mVdsStatistics = new VdsStatistics();
        mInterfaceList = new java.util.ArrayList<VdsNetworkInterface>();
        mNetworkList = new java.util.ArrayList<network>();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_cpuName == null) ? 0 : _cpuName.hashCode());
        result = prime * result + ((_spm_status == null) ? 0 : _spm_status.hashCode());
        result = prime * result + cpu_over_commit_duration_minutesField;
        result = prime * result + high_utilizationField;
        result = prime * result + low_utilizationField;
        result = prime * result + ((mImagesLastCheck == null) ? 0 : mImagesLastCheck.hashCode());
        result = prime * result + ((mImagesLastDelay == null) ? 0 : mImagesLastDelay.hashCode());
        result = prime * result + ((mInterfaceList == null) ? 0 : mInterfaceList.hashCode());
        result = prime * result + ((mNetworkList == null) ? 0 : mNetworkList.hashCode());
        result = prime * result + ((mVdsDynamic == null) ? 0 : mVdsDynamic.hashCode());
        result = prime * result + ((mVdsStatic == null) ? 0 : mVdsStatic.hashCode());
        result = prime * result + ((mVdsStatistics == null) ? 0 : mVdsStatistics.hashCode());
        result = prime * result + max_vds_memory_over_commitField;
        result = prime * result + ((privateDomains == null) ? 0 : privateDomains.hashCode());
        result = prime * result + ((privatevds_spm_id == null) ? 0 : privatevds_spm_id.hashCode());
        result = prime * result + ((selection_algorithmField == null) ? 0 : selection_algorithmField.hashCode());
        result = prime * result + ((storage_pool_idField == null) ? 0 : storage_pool_idField.hashCode());
        result = prime * result + ((storage_pool_nameField == null) ? 0 : storage_pool_nameField.hashCode());
        result =
                prime
                        * result
                        + ((vds_group_compatibility_versionField == null) ? 0
                                : vds_group_compatibility_versionField.hashCode());
        result = prime * result + ((vds_group_cpu_nameField == null) ? 0 : vds_group_cpu_nameField.hashCode());
        result = prime * result + ((vds_group_descriptionField == null) ? 0 : vds_group_descriptionField.hashCode());
        result = prime * result + ((vds_group_nameField == null) ? 0 : vds_group_nameField.hashCode());
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
        VDS other = (VDS) obj;
        if (_cpuName == null) {
            if (other._cpuName != null)
                return false;
        } else if (!_cpuName.equals(other._cpuName))
            return false;
        if (_spm_status != other._spm_status)
            return false;
        if (cpu_over_commit_duration_minutesField != other.cpu_over_commit_duration_minutesField)
            return false;
        if (high_utilizationField != other.high_utilizationField)
            return false;
        if (low_utilizationField != other.low_utilizationField)
            return false;
        if (mImagesLastCheck == null) {
            if (other.mImagesLastCheck != null)
                return false;
        } else if (!mImagesLastCheck.equals(other.mImagesLastCheck))
            return false;
        if (mImagesLastDelay == null) {
            if (other.mImagesLastDelay != null)
                return false;
        } else if (!mImagesLastDelay.equals(other.mImagesLastDelay))
            return false;
        if (mInterfaceList == null) {
            if (other.mInterfaceList != null)
                return false;
        } else if (!mInterfaceList.equals(other.mInterfaceList))
            return false;
        if (mNetworkList == null) {
            if (other.mNetworkList != null)
                return false;
        } else if (!mNetworkList.equals(other.mNetworkList))
            return false;
        if (mVdsStatic == null) {
            if (other.mVdsStatic != null)
                return false;
        } else if (!mVdsStatic.equals(other.mVdsStatic))
            return false;
        if (max_vds_memory_over_commitField != other.max_vds_memory_over_commitField)
            return false;
        if (privateDomains == null) {
            if (other.privateDomains != null)
                return false;
        } else if (!privateDomains.equals(other.privateDomains))
            return false;
        if (privatevds_spm_id == null) {
            if (other.privatevds_spm_id != null)
                return false;
        } else if (!privatevds_spm_id.equals(other.privatevds_spm_id))
            return false;
        if (selection_algorithmField != other.selection_algorithmField)
            return false;
        if (storage_pool_idField == null) {
            if (other.storage_pool_idField != null)
                return false;
        } else if (!storage_pool_idField.equals(other.storage_pool_idField))
            return false;
        if (storage_pool_nameField == null) {
            if (other.storage_pool_nameField != null)
                return false;
        } else if (!storage_pool_nameField.equals(other.storage_pool_nameField))
            return false;
        if (vds_group_compatibility_versionField == null) {
            if (other.vds_group_compatibility_versionField != null)
                return false;
        } else if (!vds_group_compatibility_versionField.equals(other.vds_group_compatibility_versionField))
            return false;
        if (vds_group_cpu_nameField == null) {
            if (other.vds_group_cpu_nameField != null)
                return false;
        } else if (!vds_group_cpu_nameField.equals(other.vds_group_cpu_nameField))
            return false;
        if (vds_group_descriptionField == null) {
            if (other.vds_group_descriptionField != null)
                return false;
        } else if (!vds_group_descriptionField.equals(other.vds_group_descriptionField))
            return false;
        if (vds_group_nameField == null) {
            if (other.vds_group_nameField != null)
                return false;
        } else if (!vds_group_nameField.equals(other.vds_group_nameField))
            return false;
        return true;
    }

    public VDS(Guid vds_group_id, String vds_group_name, String vds_group_description, Guid vds_id, String vds_name,
            String ip, String host_name, int port, int status, Integer cpu_cores, String cpu_model,
            Double cpu_speed_mh, String if_total_speed, Boolean kvm_enabled, Integer physical_mem_mb,
            Double cpu_idle, Double cpu_load, Double cpu_sys,
            Double cpu_user, Integer mem_commited, Integer vm_active, int vm_count,
            Integer vm_migrating, Integer usage_mem_percent, Integer usage_cpu_percent, Integer usage_network_percent,
            Integer reserved_mem, Integer guest_overhead, VDSStatus previous_status, String software_version,
            String version_name, String build_name, Long mem_available, Long mem_shared, boolean server_SSL_enabled,
            String vds_group_cpu_name, String cpu_name, Boolean net_config_dirty, String pm_type, String pm_user,
            String pm_password, int pm_port, String pm_options, boolean pm_enabled) // Nullable<System.Int32>
                                                                                    // mem_cached,
    {
        mVdsStatic = new VdsStatic();
        mVdsDynamic = new VdsDynamic();
        mVdsStatistics = new VdsStatistics();
        mInterfaceList = new java.util.ArrayList<VdsNetworkInterface>();
        mNetworkList = new java.util.ArrayList<network>();
        this.setvds_group_id(vds_group_id);
        this.vds_group_nameField = vds_group_name;
        this.vds_group_descriptionField = vds_group_description;
        this.setvds_id(vds_id);
        this.setvds_name(vds_name);
        this.setManagmentIp(ip);
        this.sethost_name(host_name);
        this.setport(port);
        this.setstatus(VDSStatus.forValue(status));
        this.setcpu_cores(cpu_cores);
        this.setcpu_model(cpu_model);
        this.setcpu_speed_mh(cpu_speed_mh);
        this.setif_total_speed(if_total_speed);
        this.setkvm_enabled(kvm_enabled);
        this.setphysical_mem_mb(physical_mem_mb);
        this.setcpu_idle(cpu_idle);
        this.setcpu_load(cpu_load);
        this.setcpu_sys(cpu_sys);
        this.setcpu_user(cpu_user);
        this.setmem_commited(mem_commited);
        this.setvm_active(vm_active);
        this.setvm_count(vm_count);
        this.setvm_migrating(vm_migrating);
        this.setusage_mem_percent(usage_mem_percent);
        this.setusage_cpu_percent(usage_cpu_percent);
        this.setusage_network_percent(usage_network_percent);
        this.setreserved_mem(reserved_mem);
        this.setguest_overhead(guest_overhead);
        this.setprevious_status(previous_status);
        this.setmem_available(mem_available);
        this.setmem_shared(mem_shared);
        this.setsoftware_version(software_version);
        this.setversion_name(version_name);
        this.setbuild_name(build_name);
        this.setserver_SSL_enabled(server_SSL_enabled);
        this.vds_group_cpu_nameField = vds_group_cpu_name;
        this.setcpu_flags(getcpu_flags());
        this.setnet_config_dirty(net_config_dirty);
        // Power Management
        this.setpm_enabled(pm_enabled);
        this.setpm_password(pm_password);
        this.setpm_port(pm_port);
        this.setpm_options(pm_options);
        this.setpm_type(pm_type);
        this.setpm_user(pm_user);
    }

    public VDS(VdsStatic vdsStatic, VdsDynamic vdsDynamic, VdsStatistics vdsStatistics) {
        this.mVdsStatic = vdsStatic;
        this.mVdsDynamic = vdsDynamic;
        this.mVdsStatistics = vdsStatistics;
    }

    private Version vds_group_compatibility_versionField;

    @XmlElement
    public Version getvds_group_compatibility_version() {
        return this.vds_group_compatibility_versionField;
    }

    @XmlElement(name = "ContainingHooks")
    public boolean getContainingHooks() {
        // As VDSM reports the hooks in XMLRPCStruct that represents map of maps, we can assume that the string form of
        // the map begins with
        // { and ends with }
        String hooksStr = getHooksStr();
        return hooksStr != null && hooksStr.length() > 2;
    }

    public void setContainingHooks(boolean isContainingHooks) {
        // Empty setter - this is a calculated field
    }

    public void setHooksStr(String hooksStr) {
        getDynamicData().setHooksStr(hooksStr);
    }

    public String getHooksStr() {
        return getDynamicData().getHooksStr();
    }

    public void setvds_group_compatibility_version(Version value) {
        if (Version.OpInequality(vds_group_compatibility_versionField, value)) {
            this.vds_group_compatibility_versionField = value;
            OnPropertyChanged(new PropertyChangedEventArgs("vds_group_compatibility_version"));
        }
    }

    @XmlElement(name = "vds_group_id")
    public Guid getvds_group_id() {
        return this.mVdsStatic.getvds_group_id();
    }

    public void setvds_group_id(Guid value) {
        this.mVdsStatic.setvds_group_id(value);
        OnPropertyChanged(new PropertyChangedEventArgs("vds_group_id"));
    }

    private String vds_group_nameField;

    @XmlElement
    public String getvds_group_name() {
        return this.vds_group_nameField;
    }

    public void setvds_group_name(String value) {
        this.vds_group_nameField = value;
        OnPropertyChanged(new PropertyChangedEventArgs("vds_group_name"));
    }

    private String vds_group_descriptionField;

    @XmlElement
    public String getvds_group_description() {
        return this.vds_group_descriptionField;
    }

    public void setvds_group_description(String value) {
        this.vds_group_descriptionField = value;
    }

    private String vds_group_cpu_nameField;

    @XmlElement
    public String getvds_group_cpu_name() {
        return this.vds_group_cpu_nameField;
    }

    public void setvds_group_cpu_name(String value) {
        this.vds_group_cpu_nameField = value;
    }

    @XmlElement(name = "vds_id")
    public Guid getvds_id() {
        return this.mVdsStatic.getId();
    }

    public void setvds_id(Guid value) {
        this.mVdsStatic.setId(value);
        this.mVdsDynamic.setId(value);
        this.mVdsStatistics.setId(value);
    }

    @XmlElement(name = "vds_name")
    public String getvds_name() {
        return this.mVdsStatic.getvds_name();
    }

    public void setvds_name(String value) {
        if (!StringHelper.EqOp(this.mVdsStatic.getvds_name(), value)) {
            this.mVdsStatic.setvds_name(value);
            OnPropertyChanged(new PropertyChangedEventArgs("vds_name"));
        }
    }

    @XmlElement(name = "ManagmentIp")
    public String getManagmentIp() {
        return this.mVdsStatic.getManagmentIp();
    }

    public void setManagmentIp(String value) {
        this.mVdsStatic.setManagmentIp(value);
    }

    @XmlElement(name = "UniqueId")
    public String getUniqueId() {
        return mVdsStatic.getUniqueID();
    }

    public void setUniqueId(String value) {
        mVdsStatic.setUniqueID(value);
    }

    @XmlElement(name = "host_name")
    public String gethost_name() {
        return this.mVdsStatic.gethost_name();
    }

    public void sethost_name(String value) {
        this.mVdsStatic.sethost_name(value);
        OnPropertyChanged(new PropertyChangedEventArgs("host_name"));
    }

    @XmlElement(name = "port")
    public int getport() {
        return this.mVdsStatic.getport();
    }

    public void setport(int value) {
        this.mVdsStatic.setport(value);
    }

    @XmlElement(name = "server_SSL_enabled")
    public boolean getserver_SSL_enabled() {
        return this.mVdsStatic.getserver_SSL_enabled();
    }

    public void setserver_SSL_enabled(boolean value) {
        this.mVdsStatic.setserver_SSL_enabled(value);
    }

    @XmlElement(name = "vds_type")
    public VDSType getvds_type() {
        return this.mVdsStatic.getvds_type();
    }

    public void setvds_type(VDSType value) {
        this.mVdsStatic.setvds_type(value);
        OnPropertyChanged(new PropertyChangedEventArgs("vds_type"));
    }

    @XmlElement(name = "status")
    public VDSStatus getstatus() {
        return this.mVdsDynamic.getstatus();
    }

    public void setstatus(VDSStatus value) {
        if (this.mVdsDynamic.getstatus() != value) {
            this.mVdsDynamic.setstatus(value);
            // TODO: check how to do deal with locks
            // mLockObj = mLockObj ?? new object();
            // //lock (mLockObj)
            // //{
            // if (((mVdsDynamic.status == VDSStatus.Up) ||
            // (mVdsDynamic.status == VDSStatus.PreparingForMaintenance)) &&
            // (value == VDSStatus.NonResponsive) &&
            // (previous_status != mVdsDynamic.status))
            // {
            // previous_status = mVdsDynamic.status;
            // }
            // this.mVdsDynamic.status = value;
            // if (value == VDSStatus.NonResponsive ||
            // value == VDSStatus.Down ||
            // value == VDSStatus.Maintenance)
            // {
            // this.cpu_sys = 0;
            // this.cpu_user = 0;
            // this.cpu_idle = 0;
            // this.tx_rate = 0;
            // this.rx_rate = 0;
            // this.cpu_load = 0;
            // this.usage_cpu_percent = 0;
            // this.usage_mem_percent = 0;
            // this.usage_network_percent = 0;

            // }
            // //}

            OnPropertyChanged(new PropertyChangedEventArgs("status"));
        }
    }

    @XmlElement(name = "cpu_cores")
    public Integer getcpu_cores() {
        return this.mVdsDynamic.getcpu_cores();
    }

    public void setcpu_cores(Integer value) {
        this.mVdsDynamic.setcpu_cores(value);
    }

    @XmlElement(name = "cpu_sockets")
    public Integer getcpu_sockets() {
        return this.mVdsDynamic.getcpu_sockets();
    }

    public void setcpu_sockets(Integer value) {
        this.mVdsDynamic.setcpu_sockets(value);
    }

    @XmlElement(name = "cpu_model")
    public String getcpu_model() {
        return this.mVdsDynamic.getcpu_model();
    }

    public void setcpu_model(String value) {
        this.mVdsDynamic.setcpu_model(value);
    }

    @XmlElement(name = "cpu_speed_mh")
    public Double getcpu_speed_mh() {
        return this.mVdsDynamic.getcpu_speed_mh();
    }

    public void setcpu_speed_mh(Double value) {
        this.mVdsDynamic.setcpu_speed_mh(value);
    }

    @XmlElement(name = "if_total_speed")
    public String getif_total_speed() {
        return this.mVdsDynamic.getif_total_speed();
    }

    public void setif_total_speed(String value) {
        this.mVdsDynamic.setif_total_speed(value);
    }

    @XmlElement(name = "kvm_enabled")
    public Boolean getkvm_enabled() {
        return this.mVdsDynamic.getkvm_enabled();
    }

    public void setkvm_enabled(Boolean value) {
        this.mVdsDynamic.setkvm_enabled(value);
    }

    @XmlElement(name = "physical_mem_mb", nillable = true)
    public Integer getphysical_mem_mb() {
        return this.mVdsDynamic.getphysical_mem_mb();
    }

    public void setphysical_mem_mb(Integer value) {
        this.mVdsDynamic.setphysical_mem_mb(value);
    }

    @XmlElement(name = "supported_cluster_levels")
    public String getsupported_cluster_levels() {
        return this.mVdsDynamic.getsupported_cluster_levels();
    }

    public void setsupported_cluster_levels(String value) {
        this.mVdsDynamic.setsupported_cluster_levels(value);
    }

    public java.util.HashSet<Version> getSupportedClusterVersionsSet() {
        return this.mVdsDynamic.getSupportedClusterVersionsSet();
    }

    @XmlElement(name = "supported_engines")
    public String getsupported_engines() {
        return this.mVdsDynamic.getsupported_engines();
    }

    public void setsupported_engines(String value) {
        this.mVdsDynamic.setsupported_engines(value);
    }

    public java.util.HashSet<Version> getSupportedENGINESVersionsSet() {
        return this.mVdsDynamic.getSupportedENGINESVersionsSet();
    }

    @XmlElement(name = "cpu_idle")
    public Double getcpu_idle() {
        return this.mVdsStatistics.getcpu_idle();
    }

    public void setcpu_idle(Double value) {
        this.mVdsStatistics.setcpu_idle(value);
    }

    @XmlElement(name = "cpu_load")
    public Double getcpu_load() {
        return this.mVdsStatistics.getcpu_load();
    }

    public void setcpu_load(Double value) {
        this.mVdsStatistics.setcpu_load(value);
    }

    @XmlElement(name = "cpu_sys")
    public Double getcpu_sys() {
        return this.mVdsStatistics.getcpu_sys();
    }

    public void setcpu_sys(Double value) {
        this.mVdsStatistics.setcpu_sys(value);
    }

    @XmlElement(name = "cpu_user")
    public Double getcpu_user() {
        return this.mVdsStatistics.getcpu_user();
    }

    public void setcpu_user(Double value) {
        this.mVdsStatistics.setcpu_user(value);
    }

    @XmlElement(name = "mem_commited")
    public Integer getmem_commited() {
        return this.mVdsDynamic.getmem_commited();
    }

    public void setmem_commited(Integer value) {
        this.mVdsDynamic.setmem_commited(value);
        OnPropertyChanged(new PropertyChangedEventArgs("mem_commited"));
        OnPropertyChanged(new PropertyChangedEventArgs("mem_commited_percent"));
    }

    @XmlElement(name = "vm_active", nillable = true)
    public Integer getvm_active() {
        return this.mVdsDynamic.getvm_active();
    }

    public void setvm_active(Integer value) {
        this.mVdsDynamic.setvm_active(value);
        OnPropertyChanged(new PropertyChangedEventArgs("vm_active"));
    }

    @XmlElement(name = "vm_count")
    public int getvm_count() {
        return this.mVdsDynamic.getvm_count();
    }

    public void setvm_count(int value) {
        this.mVdsDynamic.setvm_count(value);
        OnPropertyChanged(new PropertyChangedEventArgs("vm_count"));
    }

    @XmlElement(name = "vms_cores_count")
    public Integer getvms_cores_count() {
        return this.mVdsDynamic.getvms_cores_count();
    }

    public void setvms_cores_count(Integer value) {
        this.mVdsDynamic.setvms_cores_count(value);
        OnPropertyChanged(new PropertyChangedEventArgs("vms_cores_count"));
    }

    @XmlElement(name = "vm_migrating")
    public Integer getvm_migrating() {
        return this.mVdsDynamic.getvm_migrating();
    }

    public void setvm_migrating(Integer value) {
        this.mVdsDynamic.setvm_migrating(value);
    }

    @XmlElement(name = "usage_mem_percent", nillable = true)
    public Integer getusage_mem_percent() {
        return this.mVdsStatistics.getusage_mem_percent();
    }

    public void setusage_mem_percent(Integer value) {
        this.mVdsStatistics.setusage_mem_percent(value);
        OnPropertyChanged(new PropertyChangedEventArgs("usage_mem_percent"));
    }

    @XmlElement(name = "usage_cpu_percent", nillable = true)
    public Integer getusage_cpu_percent() {
        return this.mVdsStatistics.getusage_cpu_percent();
    }

    public void setusage_cpu_percent(Integer value) {
        this.mVdsStatistics.setusage_cpu_percent(value);
        OnPropertyChanged(new PropertyChangedEventArgs("usage_cpu_percent"));
    }

    @XmlElement(name = "usage_network_percent", nillable = true)
    public Integer getusage_network_percent() {
        return this.mVdsStatistics.getusage_network_percent();
    }

    public void setusage_network_percent(Integer value) {
        this.mVdsStatistics.setusage_network_percent(value);
        OnPropertyChanged(new PropertyChangedEventArgs("usage_network_percent"));
    }

    @XmlElement(name = "guest_overhead", nillable = true)
    public Integer getguest_overhead() {
        return this.mVdsDynamic.getguest_overhead();
    }

    public void setguest_overhead(Integer value) {
        this.mVdsDynamic.setguest_overhead(value);
    }

    @XmlElement(name = "reserved_mem", nillable = true)
    public Integer getreserved_mem() {
        return this.mVdsDynamic.getreserved_mem();
    }

    public void setreserved_mem(Integer value) {
        this.mVdsDynamic.setreserved_mem(value);
    }

    @XmlElement(name = "previous_status")
    public VDSStatus getprevious_status() {
        return this.mVdsDynamic.getprevious_status();
    }

    public void setprevious_status(VDSStatus value) {
        this.mVdsDynamic.setprevious_status(value);
    }

    @XmlElement(name = "mem_available", nillable = true)
    public Long getmem_available() {
        return this.mVdsStatistics.getmem_available();
    }

    public void setmem_available(Long value) {
        this.mVdsStatistics.setmem_available(value);
    }

    @XmlElement(name = "mem_shared", nillable = true)
    public Long getmem_shared() {
        return this.mVdsStatistics.getmem_shared();
    }

    public void setmem_shared(Long value) {
        this.mVdsStatistics.setmem_shared(value);
        OnPropertyChanged(new PropertyChangedEventArgs("mem_shared"));
        OnPropertyChanged(new PropertyChangedEventArgs("mem_shared_percent"));
    }

    @XmlElement(name = "mem_commited_percent", nillable = true)
    public Integer getmem_commited_percent() {
        Integer commited = mVdsDynamic.getmem_commited();
        Integer physical = mVdsDynamic.getphysical_mem_mb();

        if (commited == null || physical == null || physical == 0) {
            return 0;
        }

        return (commited * 100) / physical;
    }

    /**
     * This method is created for SOAP serialization of primitives that are readonly but sent by the client. The setter
     * implementation is empty and the field is not being changed.
     *
     * @param value
     */
    @Deprecated
    public void setmem_commited_percent(Integer value) {

    }

    @XmlElement(name = "mem_shared_percent", nillable = true)
    public Integer getmem_shared_percent() {
        Long shared = mVdsStatistics.getmem_shared();
        Integer physical = mVdsDynamic.getphysical_mem_mb();

        if (shared == null || physical == null || physical == 0) {
            return 0;
        }

        return ((int) (shared * 100) / physical);
    }

    /**
     * This method is created for SOAP serialization of primitives that are readonly but sent by the client. The setter
     * implementation is empty and the field is not being changed.
     *
     * @param value
     */
    @Deprecated
    public void setmem_shared_percent(Integer value) {

    }

    @XmlElement(name = "swap_free", nillable = true)
    public Long getswap_free() {
        return this.mVdsStatistics.getswap_free();
    }

    public void setswap_free(Long value) {
        this.mVdsStatistics.setswap_free(value);
    }

    @XmlElement(name = "swap_total", nillable = true)
    public Long getswap_total() {
        return this.mVdsStatistics.getswap_total();
    }

    public void setswap_total(Long value) {
        this.mVdsStatistics.setswap_total(value);
    }

    @XmlElement(name = "ksm_cpu_percent", nillable = true)
    public Integer getksm_cpu_percent() {
        return this.mVdsStatistics.getksm_cpu_percent();
    }

    public void setksm_cpu_percent(Integer value) {
        this.mVdsStatistics.setksm_cpu_percent(value);
    }

    @XmlElement(name = "ksm_pages", nillable = true)
    public Long getksm_pages() {
        return this.mVdsStatistics.getksm_pages();
    }

    public void setksm_pages(Long value) {
        this.mVdsStatistics.setksm_pages(value);
    }

    @XmlElement(name = "ksm_state")
    public Boolean getksm_state() {
        return this.mVdsStatistics.getksm_state();
    }

    public void setksm_state(Boolean value) {
        this.mVdsStatistics.setksm_state(value);
        OnPropertyChanged(new PropertyChangedEventArgs("ksm_state"));
    }

    @XmlElement(name = "software_version")
    public String getsoftware_version() {
        return this.mVdsDynamic.getsoftware_version();
    }

    public void setsoftware_version(String value) {
        this.mVdsDynamic.setsoftware_version(value);
    }

    @XmlElement(name = "version_name")
    public String getversion_name() {
        return this.mVdsDynamic.getversion_name();
    }

    public void setversion_name(String value) {
        this.mVdsDynamic.setversion_name(value);
    }

    @XmlElement(name = "build_name")
    public String getbuild_name() {
        return this.mVdsDynamic.getbuild_name();
    }

    public void setbuild_name(String value) {
        this.mVdsDynamic.setbuild_name(value);
    }

    @XmlElement(name = "cpu_flags")
    public String getcpu_flags() {
        return mVdsDynamic.getcpu_flags();
    }

    public void setcpu_flags(String value) {
        mVdsDynamic.setcpu_flags(value);
    }

    @XmlElement(name = "cpu_over_commit_time_stamp")
    public java.util.Date getcpu_over_commit_time_stamp() {
        return mVdsDynamic.getcpu_over_commit_time_stamp();
    }

    public void setcpu_over_commit_time_stamp(java.util.Date value) {
        mVdsDynamic.setcpu_over_commit_time_stamp(value);
    }

    public HypervisorType gethypervisor_type() {
        return mVdsDynamic.gethypervisor_type();
    }

    public void sethypervisor_type(HypervisorType value) {
        mVdsDynamic.sethypervisor_type(value);
    }

    @XmlElement(name = "vds_strength")
    public int getvds_strength() {
        return this.mVdsStatic.getvds_strength();
    }

    public void setvds_strength(int value) {
        this.mVdsStatic.setvds_strength(value);
    }

    private int high_utilizationField;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public int gethigh_utilization() {
        return this.high_utilizationField;
    }

    public void sethigh_utilization(int value) {
        this.high_utilizationField = value;
    }

    private int low_utilizationField;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public int getlow_utilization() {
        return this.low_utilizationField;
    }

    public void setlow_utilization(int value) {
        this.low_utilizationField = value;
    }

    private int cpu_over_commit_duration_minutesField;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public int getcpu_over_commit_duration_minutes() {
        return this.cpu_over_commit_duration_minutesField;
    }

    public void setcpu_over_commit_duration_minutes(int value) {
        this.cpu_over_commit_duration_minutesField = value;
    }

    private Guid storage_pool_idField = new Guid();

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public Guid getstorage_pool_id() {
        return this.storage_pool_idField;
    }

    public void setstorage_pool_id(Guid value) {
        this.storage_pool_idField = value;
    }

    private String storage_pool_nameField;

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public String getstorage_pool_name() {
        return this.storage_pool_nameField;
    }

    public void setstorage_pool_name(String value) {
        this.storage_pool_nameField = value;
    }

    private VdsSelectionAlgorithm selection_algorithmField = VdsSelectionAlgorithm.forValue(0);

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    public VdsSelectionAlgorithm getselection_algorithm() {
        return this.selection_algorithmField;
    }

    public void setselection_algorithm(VdsSelectionAlgorithm value) {
        this.selection_algorithmField = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "max_vds_memory_over_commit")
    private int max_vds_memory_over_commitField;

    public int getmax_vds_memory_over_commit() {
        return this.max_vds_memory_over_commitField;
    }

    public void setmax_vds_memory_over_commit(int value) {
        this.max_vds_memory_over_commitField = value;
    }

    @XmlElement
    public Integer getpending_vcpus_count() {
        return mVdsDynamic.getpending_vcpus_count();
    }

    public void setpending_vcpus_count(Integer value) {
        mVdsDynamic.setpending_vcpus_count(value);
    }

    @XmlElement
    public int getpending_vmem_size() {
        return mVdsDynamic.getpending_vmem_size();
    }

    public void setpending_vmem_size(int value) {
        mVdsDynamic.setpending_vmem_size(value);
    }

    @XmlElement(nillable = true)
    public Boolean getnet_config_dirty() {
        return mVdsDynamic.getnet_config_dirty();
    }

    public void setnet_config_dirty(Boolean value) {
        mVdsDynamic.setnet_config_dirty(value);
        OnPropertyChanged(new PropertyChangedEventArgs("net_config_dirty"));
    }

    @XmlElement
    public String getpm_type() {
        return mVdsStatic.getpm_type();
    }

    public void setpm_type(String value) {
        mVdsStatic.setpm_type(value);
        OnPropertyChanged(new PropertyChangedEventArgs("pm_type"));
    }

    @XmlElement
    public String getpm_user() {
        return mVdsStatic.getpm_user();
    }

    public void setpm_user(String value) {
        mVdsStatic.setpm_user(value);
        OnPropertyChanged(new PropertyChangedEventArgs("pm_user"));
    }

    @XmlElement
    public String getpm_password() {
        return mVdsStatic.getpm_password();
    }

    public void setpm_password(String value) {
        mVdsStatic.setpm_password(value);
        OnPropertyChanged(new PropertyChangedEventArgs("pm_password"));
    }

    @XmlElement(nillable = true)
    public Integer getpm_port() {
        return mVdsStatic.getpm_port();
    }

    public void setpm_port(Integer value) {
        mVdsStatic.setpm_port(value);
        OnPropertyChanged(new PropertyChangedEventArgs("pm_port"));
    }

    public String getpm_options() {
        return mVdsStatic.getpm_options();
    }

    public void setpm_options(String value) {
        mVdsStatic.setpm_options(value);
    }

    @XmlElement(name = "PmOptionsMap")
    public ValueObjectMap getPmOptionsMap() {
        return mVdsStatic.getPmOptionsMap();
    }

    public void setPmOptionsMap(ValueObjectMap value) {
        mVdsStatic.setPmOptionsMap(value);
        OnPropertyChanged(new PropertyChangedEventArgs("PmOptionsMap"));
    }

    @XmlElement
    public boolean getpm_enabled() {
        return mVdsStatic.getpm_enabled();
    }

    public void setpm_enabled(boolean value) {
        mVdsStatic.setpm_enabled(value);
        OnPropertyChanged(new PropertyChangedEventArgs("pm_enabled"));
    }

    @XmlElement
    public String gethost_os() {
        return this.mVdsDynamic.gethost_os();
    }

    public void sethost_os(String value) {
        this.mVdsDynamic.sethost_os(value);
    }

    @XmlElement
    public String getkvm_version() {
        return this.mVdsDynamic.getkvm_version();
    }

    public void setkvm_version(String value) {
        this.mVdsDynamic.setkvm_version(value);
    }

    @XmlElement
    public String getspice_version() {
        return this.mVdsDynamic.getspice_version();
    }

    public void setspice_version(String value) {
        this.mVdsDynamic.setspice_version(value);
    }

    @XmlElement
    public String getkernel_version() {
        return this.mVdsDynamic.getkernel_version();
    }

    public void setkernel_version(String value) {
        this.mVdsDynamic.setkernel_version(value);
    }

    @XmlElement(name = "IScsiInitiatorName")
    public void setIScsiInitiatorName(String value) {
        this.mVdsDynamic.setIScsiInitiatorName(value);
    }

    public String getIScsiInitiatorName() {
        return this.mVdsDynamic.getIScsiInitiatorName();
    }

    public void setTransparentHugePagesState(VdsTransparentHugePagesState value) {
        this.mVdsDynamic.setTransparentHugePagesState(value);
    }

    @XmlElement(name = "TransparentHugePagesState")
    public VdsTransparentHugePagesState getTransparentHugePagesState() {
        return this.mVdsDynamic.getTransparentHugePagesState();
    }

    public int getAnonymousHugePages() {
        return this.mVdsDynamic.getAnonymousHugePages();
    }

    public void setAnonymousHugePages(int value) {
        this.mVdsDynamic.setAnonymousHugePages(value);
    }

    public VdsStatic getStaticData() {
        return mVdsStatic;
    }

    public void setStaticData(VdsStatic value) {
        mVdsStatic = value;
    }

    public VdsDynamic getDynamicData() {
        return mVdsDynamic;
    }

    public void setDynamicData(VdsDynamic value) {
        mVdsDynamic = value;
    }

    public VdsStatistics getStatisticsData() {
        return mVdsStatistics;
    }

    public void setStatisticsData(VdsStatistics value) {
        mVdsStatistics = value;
    }

    public VdsFencingOptions getVdsFencingOptions() {
        return (StringHelper.isNullOrEmpty(getpm_type())) ? null : new VdsFencingOptions(getpm_type(), getpm_options());
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Events are not available in Java:
    // public event PropertyChangedEventHandler PropertyChanged;

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
        /* if (PropertyChanged != null) */
        {
            /* PropertyChanged(this, e); */
        }
    }

    public java.util.ArrayList<network> getNetworks() {
        return this.mNetworkList;
    }

    public java.util.ArrayList<VdsNetworkInterface> getInterfaces() {
        return this.mInterfaceList;
    }

    private java.util.ArrayList<VDSDomainsData> privateDomains;

    public java.util.ArrayList<VDSDomainsData> getDomains() {
        return privateDomains;
    }

    public void setDomains(java.util.ArrayList<VDSDomainsData> value) {
        privateDomains = value;
    }

    private Double mImagesLastCheck;
    private Double mImagesLastDelay;

    public Double getImagesLastCheck() {
        return mImagesLastCheck;
    }

    public void setImagesLastCheck(Double value) {
        mImagesLastCheck = value;
    }

    @XmlElement
    public Double getImagesLastDelay() {
        return mImagesLastDelay;
    }

    public void setImagesLastDelay(Double value) {
        mImagesLastDelay = value;
    }

    @XmlElement(name = "Version")
    public void setVersion(VdsVersion value) {
        mVdsDynamic.setVersion(value);
        OnPropertyChanged(new PropertyChangedEventArgs("Version"));
    }

    public VdsVersion getVersion() {
        return mVdsDynamic.getVersion();
    }

    private ServerCpu _cpuName;

    @XmlElement(name = "CpuName")
    public ServerCpu getCpuName() {
        return _cpuName;
    }

    public void setCpuName(ServerCpu value) {
        _cpuName = value;
        OnPropertyChanged(new PropertyChangedEventArgs("CpuName"));
    }

    public enum VDSRefreshType {
        LIST(0),
        STATS(1);

        private int intValue;
        private static java.util.HashMap<Integer, VDSRefreshType> mappings;

        private synchronized static java.util.HashMap<Integer, VDSRefreshType> getMappings() {
            if (mappings == null) {
                mappings = new java.util.HashMap<Integer, VDSRefreshType>();
            }
            return mappings;
        }

        private VDSRefreshType(int value) {
            intValue = value;
            VDSRefreshType.getMappings().put(value, this);
        }

        public int getValue() {
            return intValue;
        }

        public static VDSRefreshType forValue(int value) {
            return getMappings().get(value);
        }
    }

    @XmlElement(name = "vds_spm_id")
    private Integer privatevds_spm_id;

    public Integer getvds_spm_id() {
        return privatevds_spm_id;
    }

    public void setvds_spm_id(Integer value) {
        privatevds_spm_id = value;
    }

    public long getOtpValidity() {
        return mVdsStatic.getOtpValidity();
    }

    public void setOtpValidity(long value) {
        mVdsStatic.setOtpValidity(value);
    }

    @Override
    public Object getQueryableId() {
        return getvds_id();
    }

    private static final java.util.ArrayList<String> _vdsProperties = new java.util.ArrayList<String>(
            java.util.Arrays.asList(new String[] { "vds_name", "status", "usage_cpu_percent", "usage_mem_percent",
                    "usage_network_percent", "mem_commited", "vm_count", "vm_active", "host_name", "vds_group_name",
                    "vds_type", "CpuName", "vds_group_id", "net_config_dirty", "spm_status", "pm_enabled", "pm_user",
                    "pm_password", "pm_type", "pm_port", "pm_options", "vms_cores_count", "ksm_state", "mem_shared",
                    "Version", "vds_group_compatibility_version", "UniqueId", "TransparentHugePagesState", "swap_total",
                    "PmOptionsMap", "swap_free", "mem_shared_percent", "ManagmentIp", "supported_cluster_levels", "ContainingHooks", "NonOperationalReason",
                    "host_os", "kernel_version", "spice_version", "kvm_version", "storage_pool_id", "physical_mem_mb"}));

    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        return _vdsProperties;
    }

    private VdsSpmStatus _spm_status = VdsSpmStatus.forValue(0);

    @XmlElement
    public VdsSpmStatus getspm_status() {
        return _spm_status;
    }

    public void setspm_status(VdsSpmStatus value) {
        _spm_status = value;
        OnPropertyChanged(new PropertyChangedEventArgs("spm_status"));
    }

    @XmlElement(name = "NonOperationalReason")
    public NonOperationalReason getNonOperationalReason() {
        return this.mVdsDynamic.getNonOperationalReason();
    }

    public void setNonOperationalReason(NonOperationalReason nonOperationalReason) {
        this.mVdsDynamic.setNonOperationalReason(nonOperationalReason);
    }
}
