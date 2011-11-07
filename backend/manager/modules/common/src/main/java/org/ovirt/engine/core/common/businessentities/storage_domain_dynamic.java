package org.ovirt.engine.core.common.businessentities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "storage_domain_dynamic")
@Entity
@Table(name = "storage_domain_dynamic")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class storage_domain_dynamic implements BusinessEntity<Guid> {
    private static final long serialVersionUID = -5305319985243261293L;

    public storage_domain_dynamic() {
    }

    public storage_domain_dynamic(Integer available_disk_size, Guid id, Integer used_disk_size) {
        this.availableDiskSize = available_disk_size;
        this.id = id;
        this.usedDiskSize = used_disk_size;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "available_disk_size", nillable = true)
    @Column(name = "available_disk_size")
    private Integer availableDiskSize = 0;

    public Integer getavailable_disk_size() {
        return this.availableDiskSize;
    }

    public void setavailable_disk_size(Integer value) {
        this.availableDiskSize = value;
    }

    @XmlElement(name = "Id")
    @Id
    @Type(type = "guid")
    @Column(name = "id")
    private Guid id = new Guid();

    @Override
    public Guid getId() {
        return this.id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:

    @XmlElement(name = "used_disk_size", nillable = true)
    @Column(name = "used_disk_size")
    private Integer usedDiskSize = 0;

    public Integer getused_disk_size() {
        return this.usedDiskSize;
    }

    public void setused_disk_size(Integer value) {
        this.usedDiskSize = value;
    }

    public int getfreeDiskPercent() {
        Integer usedDiskSizeObj = getused_disk_size();
        Integer availableDiskSizeObj = getavailable_disk_size();
        int usedDiskSize = usedDiskSizeObj == null ? 0 : usedDiskSizeObj;
        int availableDiskSize = availableDiskSizeObj == null ? 0 : availableDiskSizeObj;

        int sum = usedDiskSize + availableDiskSize;
        int val = (sum == 0) ? 0 : (100 - (usedDiskSize * 100) / sum);
        return val;
    }

    public int getfreeDiskInGB() {
            int availableDiskSize = getavailable_disk_size() == null ? 0 : getavailable_disk_size();
            return availableDiskSize;
    }

    public static storage_domain_dynamic copyOf(storage_domain_dynamic domain) {
        // TODO: not using the relevant constructor, since it copies a GUID
        // obejct, which
        // does not seem right for clone shallow copy behavior in original c#
        // code.
        // if this is true, need to change all constructor to use primitives,
        // or use New when dealing with C# primitives
        storage_domain_dynamic sdd = new storage_domain_dynamic();
        sdd.availableDiskSize = domain.availableDiskSize;
        sdd.id = new Guid(domain.id.getUuid());
        sdd.usedDiskSize = domain.usedDiskSize;
        return sdd;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((availableDiskSize == null) ? 0 : availableDiskSize.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((usedDiskSize == null) ? 0 : usedDiskSize.hashCode());
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
        storage_domain_dynamic other = (storage_domain_dynamic) obj;
        if (availableDiskSize == null) {
            if (other.availableDiskSize != null)
                return false;
        } else if (!availableDiskSize.equals(other.availableDiskSize))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (usedDiskSize == null) {
            if (other.usedDiskSize != null)
                return false;
        } else if (!usedDiskSize.equals(other.usedDiskSize))
            return false;
        return true;
    }

}
