package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
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

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "LUNs")
@Entity
@Table(name = "luns")
@NamedQueries({ @NamedQuery(
        name = "all_luns_for_storage_server_connection",
        query = "select lun from LUNs lun, LUN_storage_server_connection_map lmap where lmap.id.storageServerConnection = :storage_server_connection and lmap.id.lunId = lun.id")})
public class LUNs implements Serializable {
    private static final long serialVersionUID = 3026455643639610091L;

    public LUNs() {
    }

    public LUNs(String lUN_id, String phisical_volume_id, String volume_group_id) {
        this.id = lUN_id;
        this.physicalVolumeId = phisical_volume_id;
        this.volumeGroupId = volume_group_id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_lunConnections == null) ? 0 : _lunConnections.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((lunMapping == null) ? 0 : lunMapping.hashCode());
        result = prime * result + ((physicalVolumeId == null) ? 0 : physicalVolumeId.hashCode());
        result = prime * result + deviceSize;
        result = prime * result + ((lunType == null) ? 0 : lunType.hashCode());
        result = prime * result + ((pathsDictionary == null) ? 0 : pathsDictionary.hashCode());
        result = prime * result + ((vendorName == null) ? 0 : vendorName.hashCode());
        result = prime * result + ((productId == null) ? 0 : productId.hashCode());
        result = prime * result + ((serial == null) ? 0 : serial.hashCode());
        result = prime * result + ((vendorId == null) ? 0 : vendorId.hashCode());
        result = prime * result + ((volumeGroupId == null) ? 0 : volumeGroupId.hashCode());
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
        LUNs other = (LUNs) obj;
        if (_lunConnections == null) {
            if (other._lunConnections != null)
                return false;
        } else if (!_lunConnections.equals(other._lunConnections))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (lunMapping == null) {
            if (other.lunMapping != null)
                return false;
        } else if (!lunMapping.equals(other.lunMapping))
            return false;
        if (physicalVolumeId == null) {
            if (other.physicalVolumeId != null)
                return false;
        } else if (!physicalVolumeId.equals(other.physicalVolumeId))
            return false;
        if (deviceSize != other.deviceSize)
            return false;
        if (lunType != other.lunType)
            return false;
        if (pathsDictionary == null) {
            if (other.pathsDictionary != null)
                return false;
        } else if (!pathsDictionary.equals(other.pathsDictionary))
            return false;
        if (vendorName == null) {
            if (other.vendorName != null)
                return false;
        } else if (!vendorName.equals(other.vendorName))
            return false;
        if (productId == null) {
            if (other.productId != null)
                return false;
        } else if (!productId.equals(other.productId))
            return false;
        if (serial == null) {
            if (other.serial != null)
                return false;
        } else if (!serial.equals(other.serial))
            return false;
        if (vendorId == null) {
            if (other.vendorId != null)
                return false;
        } else if (!vendorId.equals(other.vendorId))
            return false;
        if (volumeGroupId == null) {
            if (other.volumeGroupId != null)
                return false;
        } else if (!volumeGroupId.equals(other.volumeGroupId))
            return false;
        return true;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "LUN_id")
    @Id
    @Size(min = 1, max = BusinessEntitiesDefinitions.LUN_ID)
    @Column(name = "lun_id", length = BusinessEntitiesDefinitions.LUN_ID)
    private String id;

    public String getLUN_id() {
        return this.id;
    }

    public void setLUN_id(String value) {
        this.id = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "phisical_volume_id")
    // TODO rename the column
    @Size(max = BusinessEntitiesDefinitions.LUN_PHISICAL_VOLUME_ID)
    @Column(name = "phisical_volume_id", length = BusinessEntitiesDefinitions.LUN_PHISICAL_VOLUME_ID)
    private String physicalVolumeId;

    public String getphisical_volume_id() {
        return this.physicalVolumeId;
    }

    public void setphisical_volume_id(String value) {
        this.physicalVolumeId = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "volume_group_id")
    @Size(max = BusinessEntitiesDefinitions.LUN_VOLUME_GROUP_ID)
    @Column(name = "volume_group_id", length = BusinessEntitiesDefinitions.LUN_VOLUME_GROUP_ID)
    private String volumeGroupId;

    public String getvolume_group_id() {
        return this.volumeGroupId;
    }

    public void setvolume_group_id(String value) {
        this.volumeGroupId = value;
    }

    @XmlElement(name = "Serial")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "serial", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String serial;

    public String getSerial() {
        return this.serial;
    }

    public void setSerial(String value) {
        this.serial = value;
    }

    @XmlElement(name = "LunMapping")
    @Column(name = "lun_mapping")
    private Integer lunMapping;

    public Integer getLunMapping() {
        return this.lunMapping;
    }

    public void setLunMapping(Integer value) {
        this.lunMapping = value;
    }

    @XmlElement(name = "VendorId")
    @Size(max = BusinessEntitiesDefinitions.LUN_VENDOR_ID)
    @Column(name = "vendor_id", length = BusinessEntitiesDefinitions.LUN_VENDOR_ID)
    private String vendorId;

    public String getVendorId() {
        return this.vendorId;
    }

    public void setVendorId(String value) {
        this.vendorId = value;
    }

    @XmlElement(name = "ProductId")
    @Size(max = BusinessEntitiesDefinitions.LUN_PRODUCT_ID)
    @Column(name = "product_id", length = BusinessEntitiesDefinitions.LUN_PRODUCT_ID)
    private String productId;

    public String getProductId() {
        return this.productId;
    }

    public void setProductId(String value) {
        this.productId = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "LunConnections")
    @Transient
    private java.util.ArrayList<storage_server_connections> _lunConnections;

    public java.util.ArrayList<storage_server_connections> getLunConnections() {
        return _lunConnections;
    }

    public void setLunConnections(java.util.ArrayList<storage_server_connections> value) {
        _lunConnections = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "DeviceSize")
    @Column(name = "device_size")
    private int deviceSize;

    public int getDeviceSize() {
        return deviceSize;
    }

    public void setDeviceSize(int value) {
        deviceSize = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "VendorName")
    @Transient
    private String vendorName;

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String value) {
        vendorName = value;
    }

    /**
     * Empty setter for CXF compliance, this field is automatically computed.
     */
    @Deprecated
    public void setPathCount(int pathCount) {
    }

    /**
     * @return Count of how many paths this LUN has.
     */
    @XmlElement(name = "PathCount")
    public int getPathCount() {
        return (getPathsDictionary() == null ? 0 : getPathsDictionary().size());
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // @XmlElement(name="PathsDictionary")
    @Transient
    private java.util.HashMap<String, Boolean> pathsDictionary;

    public java.util.HashMap<String, Boolean> getPathsDictionary() {
        return pathsDictionary;
    }

    public void setPathsDictionary(java.util.HashMap<String, Boolean> value) {
        pathsDictionary = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "LunType")
    @Transient
    private StorageType lunType = StorageType.forValue(0);

    public StorageType getLunType() {
        return lunType;
    }

    public void setLunType(StorageType value) {
        lunType = value;
    }

    /**
     * @return Whether the LUN is accessible from at least one of the paths.
     */
    @XmlElement(name = "Accessible")
    public boolean getAccessible() {
        return getPathsDictionary() != null && getPathsDictionary().values().contains(true);
    }

    /**
     * Empty setter for CXF compliance, this field is automatically computed.
     */
    @Deprecated
    public void setAccessible(boolean accessible) {
    }
}
