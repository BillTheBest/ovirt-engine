package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.queries.ValueObjectMap;
import org.ovirt.engine.core.common.validation.annotation.HostnameOrIp;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.PowerManagementCheck;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsStatic")
@Entity
@Table(name = "vds_static")
@TypeDef(name = "guid", typeClass = GuidType.class)
@NamedQueries(
              value = {
                      @NamedQuery(name = "all_vds_static_for_vds_group_without_migration",
                                  query = "select s from VdsStatic s, VdsDynamic d, VmDynamic v where " +
                                  "(s.vdsGroupId = :vds_group_id) and " +
                                  "(d.id = s.id) and " +
                                  "(d.status = 3) and " +
                                  "(v.status in (5, 6, 11, 12)) and " +
                      "(s.id != v.run_on_vds)")
              })
              public class VdsStatic implements INotifyPropertyChanged, BusinessEntity<Guid> {

    private static final long serialVersionUID = -1425566208615075937L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "Id")
    @Type(type = "guid")
    private Guid id;

    @Size(min = 1, max = BusinessEntitiesDefinitions.HOST_NAME_SIZE)
    @Column(name = "vds_name")
    private String name = ""; // GREGM prevents NPE

    @HostnameOrIp(message = "VALIDATION.VDS.POWER_MGMT.ADDRESS.HOSTNAME_OR_IP", groups = PowerManagementCheck.class)
    @Size(max = BusinessEntitiesDefinitions.HOST_IP_SIZE)
    @Column(name = "ip")
    private String ip;

    @Size(max = BusinessEntitiesDefinitions.HOST_UNIQUE_ID_SIZE)
    @Column(name = "vds_unique_id")
    private String uniqueId;

    @HostnameOrIp(message = "VALIDATION.VDS.HOSTNAME.HOSTNAME_OR_IP",
                  groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = BusinessEntitiesDefinitions.HOST_HOSTNAME_SIZE)
    @Column(name = "host_name", length = BusinessEntitiesDefinitions.HOST_HOSTNAME_SIZE)
    private String hostname;

    @Column(name = "port")
    private int port;

    @Column(name = "vds_group_id")
    @Type(type = "guid")
    private Guid vdsGroupId;

    @Column(name = "server_ssl_enabled")
    private Boolean serverSslEnabled;

    @Column(name = "vds_type")
    private VDSType vdsType = VDSType.VDS;

    @Column(name = "vds_strength")
    private Integer vdsStrength;

    @XmlElement(name = "pm_type")
    @Size(max = BusinessEntitiesDefinitions.HOST_PM_TYPE_SIZE)
    @Column(name = "pm_type")
    private String pmType;

    @XmlElement(name = "pm_user")
    @Size(max = BusinessEntitiesDefinitions.HOST_PM_USER_SIZE)
    @Column(name = "pm_user")
    private String pmUser;

    @XmlElement(name = "pm_password")
    @Size(max = BusinessEntitiesDefinitions.HOST_PM_PASSWD_SIZE)
    @Column(name = "pm_password")
    private String pmPassword;

    @XmlElement(name = "pm_port", nillable = true)
    @Column(name = "pm_port")
    private Integer pmPort;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @XmlElement(name = "pm_options", nillable = true)
    @Column(name = "pm_options", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String pmOptions;

    @XmlElement(name = "pm_enabled")
    @Column(name = "pm_enabled")
    private boolean pmEnabled;

    @XmlElement(name = "PmOptionsMap")
    @Transient
    private ValueObjectMap pmOptionsMap;

    @Column(name = "otp_validity")
    private long otpValidity;

    public VdsStatic() {
        serverSslEnabled = false;
        vdsStrength = 100;
        this.setpm_options("");
    }

    public VdsStatic(String host_name, String ip, String uniqueId, int port, Guid vds_group_id, Guid vds_id,
                     String vds_name, boolean server_SSL_enabled, VDSType vds_type) {
        serverSslEnabled = false;
        vdsStrength = 100;
        this.hostname = host_name;
        this.ip = ip;
        this.uniqueId = uniqueId;
        this.port = port;
        this.vdsGroupId = vds_group_id;
        this.id = vds_id;
        this.name = vds_name;
        this.serverSslEnabled = server_SSL_enabled;
        this.setvds_type(vds_type);
        this.setpm_options("");
    }

    @XmlElement
    public boolean getserver_SSL_enabled() {
        return serverSslEnabled;
    }

    public void setserver_SSL_enabled(boolean value) {
        serverSslEnabled = value;
    }

    @XmlElement
    public String gethost_name() {
        return this.hostname;
    }

    public void sethost_name(String value) {
        this.hostname = value;
    }

    @XmlElement(name = "ManagmentIp")
    public String getManagmentIp() {
        return this.ip;
    }

    public void setManagmentIp(String value) {
        this.ip = value;
    }

    @XmlElement(name = "UniqueID")
    public String getUniqueID() {
        return uniqueId;
    }

    public void setUniqueID(String value) {
        uniqueId = value;
    }

    @XmlElement
    public int getport() {
        return this.port;
    }

    public void setport(int value) {
        this.port = value;
    }

    @XmlElement
    public Guid getvds_group_id() {
        return this.vdsGroupId;
    }

    public void setvds_group_id(Guid value) {
        this.vdsGroupId = value;
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

    @XmlElement
    public String getvds_name() {
        return this.name;
    }

    public void setvds_name(String value) {
        this.name = value;
    }

    @XmlElement
    public VDSType getvds_type() {
        return this.vdsType;
    }

    public void setvds_type(VDSType value) {
        this.vdsType = value;
    }

    @XmlElement
    public int getvds_strength() {
        return this.vdsStrength;
    }

    public void setvds_strength(int value) {
        // strength should be between 1 and 100
        this.vdsStrength = value < 1 ? 1 : value > 100 ? 100 : value;
    }

    public String getpm_type() {
        return pmType;
    }

    public void setpm_type(String value) {
        pmType = value;
    }

    public String getpm_user() {
        return pmUser;
    }

    public void setpm_user(String value) {
        pmUser = value;
    }

    public String getpm_password() {
        return pmPassword;
    }

    public void setpm_password(String value) {
        pmPassword = value;
    }

    public Integer getpm_port() {
        return pmPort;
    }

    public void setpm_port(Integer value) {
        pmPort = value;
    }

    public String getpm_options() {
        return pmOptions;
    }

    public void setpm_options(String value) {
        pmOptions = value;
        // set pmOptionsMap value content to match the given string.
        pmOptionsMap = PmOptionsStringToMap(value);
    }

    public ValueObjectMap getPmOptionsMap() {
        return pmOptionsMap;
    }

    public void setPmOptionsMap(ValueObjectMap value) {
        pmOptionsMap = value;
        pmOptions = PmOptionsMapToString(value);
    }

    public boolean getpm_enabled() {
        return pmEnabled;
    }

    public void setpm_enabled(boolean value) {
        pmEnabled = value;
    }

    public long getOtpValidity() {
        return otpValidity;
    }

    public void setOtpValidity(long otpValidity) {
        this.otpValidity = otpValidity;
    }

    /**
     * Converts a PM Options map to string
     *
     * @param map
     * @return
     */
    public static String PmOptionsMapToString(ValueObjectMap optionsMap) {
        String result = "";
        String seperator = "";
        Map map = optionsMap.asMap();
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pairs = it.next();
            String value = pairs.getValue();
            result +=
                    seperator + pairs.getKey()
                            + ((value != null && value.length() > 0) ? "=" + value : "");
            seperator = ",";
        }
        return result;
    }

    /**
     * Converts a PM Options string to a map
     *
     * @param pmOptions
     * @return
     */
    public static ValueObjectMap PmOptionsStringToMap(String pmOptions) {
        if(pmOptions.equals("")) {
            return new ValueObjectMap();
        }
        HashMap<String, String> map = new HashMap<String, String>();
        String[] tokens = pmOptions.split(",");
        for (String token : tokens) {
            String[] pair = token.split("=");
            if (pair.length == 2) { // key=value setting
                pair[1] = (pair[1] == null ? "" : pair[1]);
                // ignore illegal settings
                if (pair[0].trim().length() > 0 && pair[1].trim().length() > 0)
                    map.put(pair[0], pair[1]);
            } else { // only key setting
                map.put(pair[0], "");
            }
        }
        return new ValueObjectMap(map, false);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (int) (otpValidity ^ (otpValidity >>> 32));
        result = prime * result + (pmEnabled ? 1231 : 1237);
        result = prime * result + ((pmOptions == null) ? 0 : pmOptions.hashCode());
        result = prime * result + ((pmOptionsMap == null) ? 0 : pmOptionsMap.hashCode());
        result = prime * result + ((pmPassword == null) ? 0 : pmPassword.hashCode());
        result = prime * result + ((pmPort == null) ? 0 : pmPort.hashCode());
        result = prime * result + ((pmType == null) ? 0 : pmType.hashCode());
        result = prime * result + ((pmUser == null) ? 0 : pmUser.hashCode());
        result = prime * result + port;
        result = prime * result + ((serverSslEnabled == null) ? 0 : serverSslEnabled.hashCode());
        result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
        result = prime * result + ((vdsGroupId == null) ? 0 : vdsGroupId.hashCode());
        result = prime * result + ((vdsStrength == null) ? 0 : vdsStrength.hashCode());
        result = prime * result + ((vdsType == null) ? 0 : vdsType.hashCode());
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
        VdsStatic other = (VdsStatic) obj;
        if (hostname == null) {
            if (other.hostname != null)
                return false;
        } else if (!hostname.equals(other.hostname))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (ip == null) {
            if (other.ip != null)
                return false;
        } else if (!ip.equals(other.ip))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (otpValidity != other.otpValidity)
            return false;
        if (pmEnabled != other.pmEnabled)
            return false;
        if (pmOptions == null) {
            if (other.pmOptions != null)
                return false;
        } else if (!pmOptions.equals(other.pmOptions))
            return false;
        if (pmOptionsMap == null) {
            if (other.pmOptionsMap != null)
                return false;
        } else if (!pmOptionsMap.equals(other.pmOptionsMap))
            return false;
        if (pmPassword == null) {
            if (other.pmPassword != null)
                return false;
        } else if (!pmPassword.equals(other.pmPassword))
            return false;
        if (pmPort == null) {
            if (other.pmPort != null)
                return false;
        } else if (!pmPort.equals(other.pmPort))
            return false;
        if (pmType == null) {
            if (other.pmType != null)
                return false;
        } else if (!pmType.equals(other.pmType))
            return false;
        if (pmUser == null) {
            if (other.pmUser != null)
                return false;
        } else if (!pmUser.equals(other.pmUser))
            return false;
        if (port != other.port)
            return false;
        if (serverSslEnabled == null) {
            if (other.serverSslEnabled != null)
                return false;
        } else if (!serverSslEnabled.equals(other.serverSslEnabled))
            return false;
        if (uniqueId == null) {
            if (other.uniqueId != null)
                return false;
        } else if (!uniqueId.equals(other.uniqueId))
            return false;
        if (vdsGroupId == null) {
            if (other.vdsGroupId != null)
                return false;
        } else if (!vdsGroupId.equals(other.vdsGroupId))
            return false;
        if (vdsStrength == null) {
            if (other.vdsStrength != null)
                return false;
        } else if (!vdsStrength.equals(other.vdsStrength))
            return false;
        if (vdsType != other.vdsType)
            return false;
        return true;
    }

}
