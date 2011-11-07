package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

@Embeddable
@TypeDef(name = "guid", typeClass = GuidType.class)
public class StoragePoolIsoMapId implements Serializable {
    private static final long serialVersionUID = -3579958698510291360L;

    @Type(type = "guid")
    private Guid storageId;

    @Type(type = "guid")
    private NGuid storagePoolId;

    public StoragePoolIsoMapId() {
    }

    public StoragePoolIsoMapId(Guid storageId, NGuid storagePoolId) {
        this.storageId = storageId;
        this.storagePoolId = storagePoolId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((storageId == null) ? 0 : storageId.hashCode());
        result = prime * result + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
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
        StoragePoolIsoMapId other = (StoragePoolIsoMapId) obj;
        if (storageId == null) {
            if (other.storageId != null)
                return false;
        } else if (!storageId.equals(other.storageId))
            return false;
        if (storagePoolId == null) {
            if (other.storagePoolId != null)
                return false;
        } else if (!storagePoolId.equals(other.storagePoolId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("storagePoolId = ").append(getStoragePoolId());
        sb.append(", storageId = ").append(getStorageId());
        return sb.toString();
    }

    public Guid getStorageId() {
        return storageId;
    }

    public void setStorageId(Guid storageId) {
        this.storageId = storageId;
    }

    public NGuid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(NGuid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }
}
