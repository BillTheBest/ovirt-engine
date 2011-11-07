package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

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
import org.ovirt.engine.core.compat.NGuid;

@XmlType(name = "vm_pool_map")
@XmlAccessorType(XmlAccessType.NONE)
@Entity
@Table(name = "vm_pool_map")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class vm_pool_map implements Serializable {
    private static final long serialVersionUID = 5876397644156138863L;

    public vm_pool_map() {
    }

    public vm_pool_map(Guid vm_guid, NGuid vm_pool_id) {
        this.vmId = vm_guid;
        this.vmPoolId = vm_pool_id;
    }

    @Id
    @Column(name = "vm_guid")
    @Type(type = "guid")
    private Guid vmId = new Guid();

    @XmlElement
    public Guid getvm_guid() {
        return this.vmId;
    }

    public void setvm_guid(Guid value) {
        this.vmId = value;
    }

    @Column(name = "vm_pool_id")
    @Type(type = "guid")
    private NGuid vmPoolId;

    @XmlElement
    public NGuid getvm_pool_id() {
        return this.vmPoolId;
    }

    public void setvm_pool_id(Guid value) {
        this.vmPoolId = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vmId == null) ? 0 : vmId.hashCode());
        result = prime * result + ((vmPoolId == null) ? 0 : vmPoolId.hashCode());
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
        vm_pool_map other = (vm_pool_map) obj;
        if (vmId == null) {
            if (other.vmId != null)
                return false;
        } else if (!vmId.equals(other.vmId))
            return false;
        if (vmPoolId == null) {
            if (other.vmPoolId != null)
                return false;
        } else if (!vmPoolId.equals(other.vmPoolId))
            return false;
        return true;
    }
}
