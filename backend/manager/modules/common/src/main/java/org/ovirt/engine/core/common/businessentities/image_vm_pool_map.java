package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "image_vm_pool_map")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class image_vm_pool_map implements Serializable {
    private static final long serialVersionUID = 3366366801736752333L;

    public image_vm_pool_map() {
    }

    public image_vm_pool_map(Guid image_guid, String internal_drive_mapping, Guid vm_guid) {
        this.imageId = image_guid;
        this.internalDriveMapping = internal_drive_mapping;
        this.vmId = vm_guid;
    }

    @Id
    @Column(name = "image_guid")
    @Type(type = "guid")
    private Guid imageId = new Guid();

    public Guid getimage_guid() {
        return this.imageId;
    }

    public void setimage_guid(Guid value) {
        this.imageId = value;
    }

    @Column(name = "internal_drive_mapping", length = 50)
    private String internalDriveMapping;

    public String getinternal_drive_mapping() {
        return this.internalDriveMapping;
    }

    public void setinternal_drive_mapping(String value) {
        this.internalDriveMapping = value;
    }

    @Column(name = "vm_guid")
    @Type(type = "guid")
    private Guid vmId = new Guid();

    public Guid getvm_guid() {
        return this.vmId;
    }

    public void setvm_guid(Guid value) {
        this.vmId = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((imageId == null) ? 0 : imageId.hashCode());
        result = prime * result + ((internalDriveMapping == null) ? 0 : internalDriveMapping.hashCode());
        result = prime * result + ((vmId == null) ? 0 : vmId.hashCode());
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
        image_vm_pool_map other = (image_vm_pool_map) obj;
        if (imageId == null) {
            if (other.imageId != null)
                return false;
        } else if (!imageId.equals(other.imageId))
            return false;
        if (internalDriveMapping == null) {
            if (other.internalDriveMapping != null)
                return false;
        } else if (!internalDriveMapping.equals(other.internalDriveMapping))
            return false;
        if (vmId == null) {
            if (other.vmId != null)
                return false;
        } else if (!vmId.equals(other.vmId))
            return false;
        return true;
    }
}
