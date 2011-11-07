package org.ovirt.engine.core.common.businessentities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

@XmlType(name = "VmDynamic")
@Entity
@Table(name = "vm_dynamic")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class VmDynamic implements BusinessEntity<Guid> {
    private static final long serialVersionUID = 521748509912037953L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "vm_guid")
    @Type(type = "guid")
    private Guid id = new Guid();

    @Column(name = "status")
    private VMStatus status = VMStatus.Down;

    @Column(name = "vm_ip")
    private String vm_ip;

    @Column(name = "vm_host")
    private String vm_host;

    @Column(name = "vm_pid")
    private Integer vm_pid;

    @Column(name = "vm_last_up_time")
    private java.util.Date vm_last_up_time;

    @Column(name = "vm_last_boot_time")
    private java.util.Date vm_last_boot_time;

    @Column(name = "guest_cur_user_name")
    private String guest_cur_user_name;

    @Column(name = "guest_cur_user_id")
    @Type(type = "guid")
    private NGuid guest_cur_user_id;

    @Column(name = "guest_last_login_time")
    private java.util.Date guest_last_login_time;

    @Column(name = "guest_last_logout_time")
    private java.util.Date guest_last_logout_time;

    @Column(name = "guest_os")
    private String guest_os;

    @Column(name = "migrating_to_vds")
    @Type(type = "guid")
    private NGuid migrating_to_vds;

    @Column(name = "run_on_vds")
    @Type(type = "guid")
    private NGuid run_on_vds;

    @Column(name = "app_list")
    private String appList;

    @Column(name = "display")
    private Integer display;

    @Column(name = "acpi_enable")
    private Boolean acpi_enable;

    @Column(name = "session")
    private SessionState session = SessionState.Unknown;

    @Column(name = "display_ip")
    private String display_ip;

    @Column(name = "display_type")
    private DisplayType display_type = DisplayType.vnc;

    @Column(name = "kvm_enable")
    private Boolean kvm_enable;

    @Column(name = "display_secure_port")
    private Integer display_secure_port;

    @Column(name = "utc_diff")
    private Integer utc_diff;

    @Column(name = "last_vds_run_on")
    @Type(type = "guid")
    private NGuid last_vds_run_on;

    @Column(name = "client_ip")
    private String client_ip;

    @Column(name = "guest_requested_memory")
    private Integer guest_requested_memory;

    @Column(name = "hibernation_vol_handle")
    private String hibernation_vol_handle;

    @Column(name = "boot_sequence")
    private BootSequence boot_sequence = BootSequence.C;

    @Column(name = "exit_status")
    private VmExitStatus mExitStatus = VmExitStatus.Normal;

    @Column(name = "pause_status")
    private VmPauseStatus pauseStatus = VmPauseStatus.NONE;

    @Column(name = "exit_message")
    private String mExitMessage;

    @Transient
    private java.util.ArrayList<DiskImageDynamic> mDisks;

    @Transient
    private boolean mWin2kHackEnable = false;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((acpi_enable == null) ? 0 : acpi_enable.hashCode());
        result = prime * result
                + ((appList == null) ? 0 : appList.hashCode());
        result = prime
                * result
                + boot_sequence.hashCode() * prime;
        result = prime * result
                + ((client_ip == null) ? 0 : client_ip.hashCode());
        result = prime * result
                + ((display == null) ? 0 : display.hashCode());
        result = prime * result
                + ((display_ip == null) ? 0 : display_ip.hashCode());
        result = prime
                * result
                + ((display_secure_port == null) ? 0
                        : display_secure_port.hashCode());
        result = prime
                * result
                + display_type.hashCode() * prime;
        result = prime
                * result
                + ((guest_cur_user_id == null) ? 0
                        : guest_cur_user_id.hashCode());
        result = prime
                * result
                + ((guest_cur_user_name == null) ? 0
                        : guest_cur_user_name.hashCode());
        result = prime
                * result
                + ((guest_last_login_time == null) ? 0
                        : guest_last_login_time.hashCode());
        result = prime
                * result
                + ((guest_last_logout_time == null) ? 0
                        : guest_last_logout_time.hashCode());
        result = prime * result
                + ((guest_os == null) ? 0 : guest_os.hashCode());
        result = prime
                * result
                + ((guest_requested_memory == null) ? 0
                        : guest_requested_memory.hashCode());
        result = prime
                * result
                + ((hibernation_vol_handle == null) ? 0
                        : hibernation_vol_handle.hashCode());
        result = prime * result
                + ((kvm_enable == null) ? 0 : kvm_enable.hashCode());
        result = prime
                * result
                + ((last_vds_run_on == null) ? 0 : last_vds_run_on
                        .hashCode());
        result = prime * result + ((mDisks == null) ? 0 : mDisks.hashCode());
        result = prime * result
                + ((mExitMessage == null) ? 0 : mExitMessage.hashCode());
        result = prime * result
                + mExitStatus.hashCode() * prime;
        result = prime * result + (mWin2kHackEnable ? 1231 : 1237);
        result = prime
                * result
                + ((migrating_to_vds == null) ? 0 : migrating_to_vds
                        .hashCode());
        result = prime * result
                + ((pauseStatus == null) ? 0 : pauseStatus.hashCode());
        result = prime * result
                + ((run_on_vds == null) ? 0 : run_on_vds.hashCode());
        result = prime * result
                + session.hashCode() * prime;
        result = prime * result
                + status.hashCode() * prime;
        result = prime * result
                + ((utc_diff == null) ? 0 : utc_diff.hashCode());
        result = prime * result
                + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((vm_host == null) ? 0 : vm_host.hashCode());
        result = prime * result
                + ((vm_ip == null) ? 0 : vm_ip.hashCode());
        result = prime
                * result
                + ((vm_last_boot_time == null) ? 0
                        : vm_last_boot_time.hashCode());
        result = prime
                * result
                + ((vm_last_up_time == null) ? 0 : vm_last_up_time
                        .hashCode());
        result = prime * result
                + ((vm_pid == null) ? 0 : vm_pid.hashCode());
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
        VmDynamic other = (VmDynamic) obj;
        if (acpi_enable == null) {
            if (other.acpi_enable != null)
                return false;
        } else if (!acpi_enable.equals(other.acpi_enable))
            return false;
        if (appList == null) {
            if (other.appList != null)
                return false;
        } else if (!appList.equals(other.appList))
            return false;
        if (boot_sequence != other.boot_sequence)
            return false;
        if (client_ip == null) {
            if (other.client_ip != null)
                return false;
        } else if (!client_ip.equals(other.client_ip))
            return false;
        if (display == null) {
            if (other.display != null)
                return false;
        } else if (!display.equals(other.display))
            return false;
        if (display_ip == null) {
            if (other.display_ip != null)
                return false;
        } else if (!display_ip.equals(other.display_ip))
            return false;
        if (display_secure_port == null) {
            if (other.display_secure_port != null)
                return false;
        } else if (!display_secure_port
                .equals(other.display_secure_port))
            return false;
        if (display_type != other.display_type)
            return false;
        if (guest_cur_user_id == null) {
            if (other.guest_cur_user_id != null)
                return false;
        } else if (!guest_cur_user_id.equals(other.guest_cur_user_id))
            return false;
        if (guest_cur_user_name == null) {
            if (other.guest_cur_user_name != null)
                return false;
        } else if (!guest_cur_user_name
                .equals(other.guest_cur_user_name))
            return false;
        if (guest_last_login_time == null) {
            if (other.guest_last_login_time != null)
                return false;
        } else if (!guest_last_login_time
                .equals(other.guest_last_login_time))
            return false;
        if (guest_last_logout_time == null) {
            if (other.guest_last_logout_time != null)
                return false;
        } else if (!guest_last_logout_time
                .equals(other.guest_last_logout_time))
            return false;
        if (guest_os == null) {
            if (other.guest_os != null)
                return false;
        } else if (!guest_os.equals(other.guest_os))
            return false;
        if (guest_requested_memory == null) {
            if (other.guest_requested_memory != null)
                return false;
        } else if (!guest_requested_memory
                .equals(other.guest_requested_memory))
            return false;
        if (hibernation_vol_handle == null) {
            if (other.hibernation_vol_handle != null)
                return false;
        } else if (!hibernation_vol_handle
                .equals(other.hibernation_vol_handle))
            return false;
        if (kvm_enable == null) {
            if (other.kvm_enable != null)
                return false;
        } else if (!kvm_enable.equals(other.kvm_enable))
            return false;
        if (last_vds_run_on == null) {
            if (other.last_vds_run_on != null)
                return false;
        } else if (!last_vds_run_on.equals(other.last_vds_run_on))
            return false;
        if (mDisks == null) {
            if (other.mDisks != null)
                return false;
        } else if (!mDisks.equals(other.mDisks))
            return false;
        if (mExitMessage == null) {
            if (other.mExitMessage != null)
                return false;
        } else if (!mExitMessage.equals(other.mExitMessage))
            return false;
        if (mExitStatus != other.mExitStatus)
            return false;
        if (mWin2kHackEnable != other.mWin2kHackEnable)
            return false;
        if (migrating_to_vds == null) {
            if (other.migrating_to_vds != null)
                return false;
        } else if (!migrating_to_vds.equals(other.migrating_to_vds))
            return false;
        if (pauseStatus != other.pauseStatus)
            return false;
        if (run_on_vds == null) {
            if (other.run_on_vds != null)
                return false;
        } else if (!run_on_vds.equals(other.run_on_vds))
            return false;
        if (session != other.session)
            return false;
        if (status != other.status)
            return false;
        if (utc_diff == null) {
            if (other.utc_diff != null)
                return false;
        } else if (!utc_diff.equals(other.utc_diff))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (vm_host == null) {
            if (other.vm_host != null)
                return false;
        } else if (!vm_host.equals(other.vm_host))
            return false;
        if (vm_ip == null) {
            if (other.vm_ip != null)
                return false;
        } else if (!vm_ip.equals(other.vm_ip))
            return false;
        if (vm_last_boot_time == null) {
            if (other.vm_last_boot_time != null)
                return false;
        } else if (!vm_last_boot_time.equals(other.vm_last_boot_time))
            return false;
        if (vm_last_up_time == null) {
            if (other.vm_last_up_time != null)
                return false;
        } else if (!vm_last_up_time.equals(other.vm_last_up_time))
            return false;
        if (vm_pid == null) {
            if (other.vm_pid != null)
                return false;
        } else if (!vm_pid.equals(other.vm_pid))
            return false;
        return true;
    }

    @XmlElement(name = "ExitMessage")
    public String getExitMessage() {
        return mExitMessage;
    }

    public void setExitMessage(String value) {
        mExitMessage = value;
    }

    @XmlElement(name = "ExitStatus")
    public VmExitStatus getExitStatus() {
        return this.mExitStatus;
    }

    public void setExitStatus(VmExitStatus value) {
        mExitStatus = value;
    }

    public java.util.ArrayList<DiskImageDynamic> getDisks() {
        return mDisks;
    }

    public void setDisks(java.util.ArrayList<DiskImageDynamic> value) {
        mDisks = value;
    }

    @XmlElement(name = "Win2kHackEnable")
    public boolean getWin2kHackEnable() {
        return mWin2kHackEnable;
    }

    public void setWin2kHackEnable(boolean value) {
        mWin2kHackEnable = value;
    }

    public VmDynamic() {
        mExitStatus = VmExitStatus.Normal;
        mWin2kHackEnable = false;
        acpi_enable = true;
        kvm_enable = true;
        session = SessionState.Unknown;
        boot_sequence = BootSequence.C;
    }

    public VmDynamic(String app_list, NGuid guest_cur_user_id, String guest_cur_user_name,
            java.util.Date guest_last_login_time, java.util.Date guest_last_logout_time, String guest_os,
            NGuid migrating_to_vds, NGuid run_on_vds, int status, Guid vm_guid, String vm_host, String vm_ip,
            java.util.Date vm_last_boot_time, java.util.Date vm_last_up_time, Integer vm_pid, Integer display,
            Boolean acpi_enable, String display_ip, Integer display_type, Boolean kvm_enable, Integer session,
            Integer boot_sequence, Integer display_secure_port, Integer utc_diff, Guid last_vds_run_on,
            String client_ip, Integer guest_requested_memory) {
        mExitStatus = VmExitStatus.Normal;
        mWin2kHackEnable = false;

        this.appList = app_list;
        this.guest_cur_user_id = guest_cur_user_id;
        this.guest_cur_user_name = guest_cur_user_name;
        this.guest_last_login_time = guest_last_login_time;
        this.guest_last_logout_time = guest_last_logout_time;
        this.guest_os = guest_os;
        this.migrating_to_vds = migrating_to_vds;
        this.run_on_vds = run_on_vds;
        this.status = VMStatus.forValue(status);
        this.id = vm_guid;
        this.vm_host = vm_host;
        this.vm_ip = vm_ip;
        this.vm_last_boot_time = vm_last_boot_time;
        this.vm_last_up_time = vm_last_up_time;
        this.vm_pid = vm_pid;
        this.display = display;
        this.acpi_enable = acpi_enable;
        this.display_ip = display_ip;
        this.display_type = DisplayType.forValue(display_type);
        this.kvm_enable = kvm_enable;
        this.session = SessionState.forValue(session);
        this.boot_sequence = BootSequence.forValue(boot_sequence);
        this.display_secure_port = display_secure_port;
        this.setutc_diff(utc_diff);
        this.setlast_vds_run_on(last_vds_run_on);
        this.setclient_ip(client_ip);
        this.setguest_requested_memory(guest_requested_memory);
    }

    @XmlElement
    public String getapp_list() {
        return this.appList;
    }

    public void setapp_list(String value) {
        this.appList = value;
    }

    @XmlElement
    public NGuid getguest_cur_user_id() {
        return this.guest_cur_user_id;
    }

    public void setguest_cur_user_id(NGuid value) {
        this.guest_cur_user_id = value;
    }

    @XmlElement
    public String getguest_cur_user_name() {
        return this.guest_cur_user_name;
    }

    public void setguest_cur_user_name(String value) {
        this.guest_cur_user_name = value;
    }

    @XmlElement
    public String getguest_os() {
        return this.guest_os;
    }

    public void setguest_os(String value) {
        this.guest_os = value;
    }

    @XmlElement(nillable = true)
    public java.util.Date getguest_last_login_time() {
        return this.guest_last_login_time;
    }

    public void setguest_last_login_time(java.util.Date value) {
        this.guest_last_login_time = value;
    }

    @XmlElement(nillable = true)
    public java.util.Date getguest_last_logout_time() {
        return this.guest_last_logout_time;
    }

    public void setguest_last_logout_time(java.util.Date value) {
        this.guest_last_logout_time = value;
    }

    @XmlElement(nillable = true)
    public NGuid getmigrating_to_vds() {
        return this.migrating_to_vds;
    }

    public void setmigrating_to_vds(NGuid value) {
        this.migrating_to_vds = value;
    }

    @XmlElement(nillable = true)
    public NGuid getrun_on_vds() {
        return this.run_on_vds;
    }

    public void setrun_on_vds(NGuid value) {
        this.run_on_vds = value;
    }

    @XmlElement
    public VMStatus getstatus() {
        return this.status;
    }

    public void setstatus(VMStatus value) {
        this.status = value;
    }

    @XmlElement
    public Guid getId() {
        return this.id;
    }

    public void setId(Guid value) {
        this.id = value;
    }

    @XmlElement
    public String getvm_host() {
        return this.vm_host;
    }

    public void setvm_host(String value) {
        this.vm_host = value;
    }

    @XmlElement
    public String getvm_ip() {
        return this.vm_ip;
    }

    public void setvm_ip(String value) {
        this.vm_ip = value;
    }

    @XmlElement(nillable = true)
    public java.util.Date getvm_last_boot_time() {
        return this.vm_last_boot_time;
    }

    public void setvm_last_boot_time(java.util.Date value) {
        this.vm_last_boot_time = value;
    }

    @XmlElement(nillable = true)
    public java.util.Date getvm_last_up_time() {
        return this.vm_last_up_time;
    }

    public void setvm_last_up_time(java.util.Date value) {
        this.vm_last_up_time = value;
    }

    @XmlElement(nillable = true)
    public Integer getvm_pid() {
        return this.vm_pid;
    }

    public void setvm_pid(Integer value) {
        this.vm_pid = value;
    }

    @XmlElement(nillable = true)
    public Integer getdisplay() {
        return this.display;
    }

    public void setdisplay(Integer value) {
        this.display = value;
    }

    @XmlElement(nillable = true)
    public Boolean getacpi_enable() {
        return this.acpi_enable;
    }

    public void setacpi_enable(Boolean value) {
        this.acpi_enable = value;
    }

    @XmlElement
    public String getdisplay_ip() {
        return this.display_ip;
    }

    public void setdisplay_ip(String value) {
        this.display_ip = value;
    }

    @XmlElement
    public DisplayType getdisplay_type() {
        return display_type;
    }

    public void setdisplay_type(DisplayType value) {
        this.display_type = value;
    }

    @XmlElement(nillable = true)
    public Boolean getkvm_enable() {
        return this.kvm_enable;
    }

    public void setkvm_enable(Boolean value) {
        this.kvm_enable = value;
    }

    @XmlElement
    public SessionState getsession() {
        return this.session;
    }

    public void setsession(SessionState value) {
        this.session = value;
    }

    @XmlElement
    public BootSequence getboot_sequence() {
        return this.boot_sequence;
    }

    public void setboot_sequence(BootSequence value) {
        this.boot_sequence = value;
    }

    @XmlElement(nillable = true)
    public Integer getdisplay_secure_port() {
        return this.display_secure_port;
    }

    public void setdisplay_secure_port(Integer value) {
        this.display_secure_port = value;
    }

    @XmlElement(nillable = true)
    public Integer getutc_diff() {
        return this.utc_diff;
    }

    public void setutc_diff(Integer value) {
        this.utc_diff = value;
    }

    @XmlElement(nillable = true)
    public NGuid getlast_vds_run_on() {
        return this.last_vds_run_on;
    }

    public void setlast_vds_run_on(NGuid value) {
        this.last_vds_run_on = value;
    }

    @XmlElement
    public String getclient_ip() {
        return this.client_ip;
    }

    public void setclient_ip(String value) {
        this.client_ip = value;
    }

    @XmlElement(nillable = true)
    public Integer getguest_requested_memory() {
        return this.guest_requested_memory;
    }

    public void setguest_requested_memory(Integer value) {
        this.guest_requested_memory = value;
    }

    @XmlElement
    public String gethibernation_vol_handle() {
        return this.hibernation_vol_handle;
    }

    public void sethibernation_vol_handle(String value) {
        this.hibernation_vol_handle = value;
    }

    public void setPauseStatus(VmPauseStatus pauseStatus) {
        this.pauseStatus = pauseStatus;

    }

    @XmlElement(name = "PauseStatus")
    public VmPauseStatus getPauseStatus() {
        return this.pauseStatus;
    }
}
