package org.ovirt.engine.core.common.businessentities;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "storage_pool_iso_map")
@Entity
@Table(name = "storage_pool_iso_map", uniqueConstraints = { @UniqueConstraint(columnNames = { "storage_id",
        "storage_pool_id" }) })
@TypeDef(name = "guid", typeClass = GuidType.class)
@NamedQueries({
        @NamedQuery(name = "all_storage_pool_iso_map_by_storage_pool_id",
                query = "select m from storage_pool_iso_map m where m.id.storagePoolId = :storagePoolId"),
        @NamedQuery(name = "all_storage_pool_iso_map_by_storage_id",
                query = "select m from storage_pool_iso_map m where m.id.storageId  = :storageId"),
        @NamedQuery(
                name = "all_storage_pool_iso_map_by_storage_id_and_storage_pool_id",
                query = "select m from storage_pool_iso_map m where m.id = :id") })
public class storage_pool_iso_map implements BusinessEntity<StoragePoolIsoMapId> {

    private static final long serialVersionUID = -2829958589095415567L;

    public storage_pool_iso_map() {
    }

    public storage_pool_iso_map(Guid storage_id, Guid storage_pool_id, StorageDomainStatus status) {
        setstorage_id(storage_id);
        setstorage_pool_id(storage_pool_id);
        this.setstatus(status);
    }

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "storageId", column = @Column(name = "storage_id")),
            @AttributeOverride(name = "storagePoolId", column = @Column(name = "storage_pool_id")) })
    private StoragePoolIsoMapId id = new StoragePoolIsoMapId();

    @Override
    public StoragePoolIsoMapId getId() {
        return this.id;
    }

    @Override
    public void setId(StoragePoolIsoMapId id) {
        this.id = id;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "storage_id")
    public Guid getstorage_id() {
        return id.getStorageId();
    }

    public void setstorage_id(Guid value) {
        id.setStorageId(value);
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "storage_pool_id", nillable = true)
    public NGuid getstorage_pool_id() {
        return this.id.getStoragePoolId();
    }

    public void setstorage_pool_id(NGuid value) {
        this.id.setStoragePoolId(value);
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @Column(name = "status", nullable = true)
    private Integer persistentStorageDomainStatus = null;

    @XmlElement(nillable = true, name = "status")
    public StorageDomainStatus getstatus() {
        if (persistentStorageDomainStatus == null) {
            return null;
        }
        return StorageDomainStatus.forValue(persistentStorageDomainStatus);
    }

    public void setstatus(StorageDomainStatus value) {
        if (value == null) {
            persistentStorageDomainStatus = null;
        } else {
            persistentStorageDomainStatus = value.getValue();
        }
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @Column(name = "owner", nullable = true)
    private Integer persistentOwner = StorageDomainOwnerType.Unknown.getValue();

    @XmlElement(nillable = true, name = "ownerField")
    public StorageDomainOwnerType getowner() {
        if (persistentOwner == null) {
            return null;
        }
        return StorageDomainOwnerType.forValue(persistentOwner);
    }

    public void setowner(StorageDomainOwnerType value) {
        if (value == null) {
            persistentOwner = null;
        } else {
            persistentOwner = value.getValue();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((persistentOwner == null) ? 0 : persistentOwner.hashCode());
        result =
                prime * result
                        + ((persistentStorageDomainStatus == null) ? 0 : persistentStorageDomainStatus.hashCode());
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
        storage_pool_iso_map other = (storage_pool_iso_map) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (persistentOwner == null) {
            if (other.persistentOwner != null)
                return false;
        } else if (!persistentOwner.equals(other.persistentOwner))
            return false;
        if (persistentStorageDomainStatus == null) {
            if (other.persistentStorageDomainStatus != null)
                return false;
        } else if (!persistentStorageDomainStatus.equals(other.persistentStorageDomainStatus))
            return false;
        return true;
    }
}
