package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.NGuid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "network")
@Entity
@Table(name = "network")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class network extends IVdcQueryable implements INotifyPropertyChanged, Serializable, BusinessEntity<Guid> {
    private static final long serialVersionUID = 7357288865938773402L;

    @XmlElement(name = "Id")
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "Id")
    @Type(type = "guid")
    private Guid id;

    @Pattern(regexp = "^[_a-zA-Z0-9]{1,15}$", message = "NETWORK_ILEGAL_NETWORK_NAME", groups = { CreateEntity.class,
            UpdateEntity.class })
    @XmlElement(name = "name")
    @Size(min = 1, max = BusinessEntitiesDefinitions.NETWORK_NAME_SIZE)
    @Column(name = "name")
    private String name;

    @XmlElement(name = "description")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "description")
    private String description;

    @XmlElement(name = "type", nillable = true)
    @Column(name = "type")
    private Integer type;

    @XmlElement(name = "addr")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_NETWORK_ADDR_SIZE)
    @Column(name = "addr")
    private String addr;

    @XmlElement(name = "subnet")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_SUBNET_SIZE)
    @Column(name = "subnet")
    private String subnet;

    @XmlElement(name = "gateway")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_GATEWAY_SIZE)
    @Column(name = "gateway")
    private String gateway;

    @XmlElement(name = "vlan_id", nillable = true)
    @Column(name = "vlan_id")
    private Integer vlan_id;

    @XmlElement(name = "stp")
    @Column(name = "stp")
    private boolean stp = false;

    @XmlElement(name = "storage_pool_id")
    @Column(name = "storage_pool_id")
    @Type(type = "guid")
    private NGuid storage_pool_id;

    @ManyToOne
    @JoinTable(name = "network_cluster", joinColumns = @JoinColumn(name = "network_id"),
            inverseJoinColumns = @JoinColumn(name = "cluster_id"))
    private network_cluster cluster = new network_cluster();

    public network() {
        id = Guid.NewGuid();
    }
    //Because the webadmin uses the same BE as backend, the constructor of these BEs
    //should not contain any logic that refer only to backend side.
    public network(String dummyVariable){}

    public network(String addr, String description, Guid id, String name, String subnet, String gateway, Integer type,
            Integer vlan_id, boolean stp) {
        this.addr = addr;
        this.description = description;
        this.id = id;
        this.name = name;
        this.subnet = subnet;
        this.gateway = gateway;
        this.type = type;
        this.vlan_id = vlan_id;
        this.stp = stp;
    }

    public network_cluster getCluster() {
        return cluster;
    }

    public String getaddr() {
        return this.addr;
    }

    public void setaddr(String value) {
        this.addr = value;
    }

    public String getdescription() {
        return this.description;
    }

    public void setdescription(String value) {
        this.description = value;
    }

    public Guid getId() {
        return this.id;
    }

    public void setId(Guid value) {
        this.id = value;
    }

    public String getname() {
        return this.name;
    }

    public void setname(String value) {
        this.name = value;
    }

    public String getsubnet() {
        return this.subnet;
    }

    public void setsubnet(String value) {
        this.subnet = value;
    }

    public String getgateway() {
        return this.gateway;
    }

    public void setgateway(String value) {
        this.gateway = value;
    }

    public Integer gettype() {
        return this.type;
    }

    public void settype(Integer value) {
        this.type = value;
    }

    public Integer getvlan_id() {
        return this.vlan_id;
    }

    public void setvlan_id(Integer value) {
        this.vlan_id = value;
    }

    @XmlElement(name = "Status")
    public NetworkStatus getStatus() {
        return NetworkStatus.forValue(cluster.getstatus());
    }

    public void setStatus(NetworkStatus value) {
        cluster.setstatus(value.getValue());
    }

    public boolean getstp() {
        return this.stp;
    }

    public void setstp(boolean value) {
        this.stp = value;
    }

    public NGuid getstorage_pool_id() {
        return this.storage_pool_id;
    }

    public void setstorage_pool_id(NGuid value) {
        this.storage_pool_id = value;
    }

    @XmlElement(name="is_display",nillable=true)
    public Boolean getis_display() {
        return cluster.getis_display();
    }

    public void setis_display(Boolean value) {
        if (value == null) {
            value = false;
        }
        cluster.setis_display(value);
    }

    public void setCluster(network_cluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    private static final java.util.ArrayList<String> _networkProperties = new java.util.ArrayList<String>(
            java.util.Arrays.asList(new String[] { "addr", "description", "name", "subnet", "type", "vlan_id",
                    "Status", "stp", "storage_pool_id", "gateway", "is_display" }));

    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        return _networkProperties;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((addr == null) ? 0 : addr.hashCode());
        //FIXME: remove cluster from hashCode calculation - breaks the tests when working in JDBC template mode
        /*
        result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
        */
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((gateway == null) ? 0 : gateway.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((storage_pool_id == null) ? 0 : storage_pool_id.hashCode());
        result = prime * result + (stp ? 1231 : 1237);
        result = prime * result + ((subnet == null) ? 0 : subnet.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((vlan_id == null) ? 0 : vlan_id.hashCode());
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
        network other = (network) obj;
        if (addr == null) {
            if (other.addr != null)
                return false;
        } else if (!addr.equals(other.addr))
            return false;
        //FIXME: currently removing cluster from equals, tests are failing
        /*
        if (cluster == null) {
            if (other.cluster != null)
                return false;
        } else if (!cluster.equals(other.cluster))
            return false;
            */
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (gateway == null) {
            if (other.gateway != null)
                return false;
        } else if (!gateway.equals(other.gateway))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (storage_pool_id == null) {
            if (other.storage_pool_id != null)
                return false;
        } else if (!storage_pool_id.equals(other.storage_pool_id))
            return false;
        if (stp != other.stp)
            return false;
        if (subnet == null) {
            if (other.subnet != null)
                return false;
        } else if (!subnet.equals(other.subnet))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (vlan_id == null) {
            if (other.vlan_id != null)
                return false;
        } else if (!vlan_id.equals(other.vlan_id))
            return false;
        return true;
    }
}
