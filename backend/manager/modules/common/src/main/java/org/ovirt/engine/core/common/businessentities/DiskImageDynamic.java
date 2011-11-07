package org.ovirt.engine.core.common.businessentities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DiskImageDynamic")
@Entity
@Table(name = "disk_image_dynamic")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class DiskImageDynamic implements INotifyPropertyChanged,BusinessEntity<Guid> {
    private static final long serialVersionUID = 6357763045419255853L;
    private Guid id;

    @Column(name = "read_rate")
    private Integer readRate;

    @Column(name = "write_rate")
    private Integer writeRate;

    @Column(name = "actual_size", nullable = false)
    private long actualSize;

    // Latency fields are measured in second.
    @Column(name = "readLatency")
    private Double readLatency;

    @Column(name = "writeLatency")
    private Double writeLatency;

    @Column(name = "flushLatency")
    private Double flushLatency;

    public DiskImageDynamic() {
    }

    @XmlElement
    public Integer getread_rate() {
        return readRate;
    }

    public void setread_rate(Integer rate) {
        readRate = rate;
    }

    @XmlElement
    public Integer getwrite_rate() {
        return writeRate;
    }

    public void setwrite_rate(Integer rate) {
        writeRate = rate;
    }

    @XmlElement(name="ReadLatency")
    public Double getReadLatency() {
        return readLatency;
    }

    public void setReadLatency(Double readLatency) {
        this.readLatency = readLatency;
    }

    @XmlElement(name="WriteLatency")
    public Double getWriteLatency() {
        return writeLatency;
    }

    public void setWriteLatency(Double writeLatency) {
        this.writeLatency = writeLatency;
    }

    @XmlElement(name="FlushLatency")
    public Double getFlushLatency() {
        return flushLatency;
    }

    public void setFlushLatency(Double flushLatency) {
        this.flushLatency = flushLatency;
    }

    @XmlElement
    public long getactual_size() {
        return this.actualSize;
    }

    public void setactual_size(long size) {
        this.actualSize = size;
        OnPropertyChanged(new PropertyChangedEventArgs("actual_size"));
    }

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (actualSize ^ (actualSize >>> 32));
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((readRate == null) ? 0 : readRate.hashCode());
        result = prime * result + ((writeRate == null) ? 0 : writeRate.hashCode());
        result = prime * result + ((writeLatency == null) ? 0 : writeLatency.hashCode());
        result = prime * result + ((readLatency == null) ? 0 : readLatency.hashCode());
        result = prime * result + ((flushLatency == null) ? 0 : flushLatency.hashCode());
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
        DiskImageDynamic other = (DiskImageDynamic) obj;
        if (actualSize != other.actualSize)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (readRate == null) {
            if (other.readRate != null)
                return false;
        } else if (!readRate.equals(other.readRate))
            return false;
        if (writeRate == null) {
            if (other.writeRate != null)
                return false;
        } else if (!writeRate.equals(other.writeRate))
            return false;
        if (readLatency == null) {
            if (other.readLatency != null)
                return false;
        } else if (!readLatency.equals(other.readLatency))
            return false;
        if (writeLatency == null) {
            if (other.writeLatency != null)
                return false;
        } else if (!writeLatency.equals(other.writeLatency))
            return false;
        if (flushLatency == null) {
            if (other.flushLatency != null)
                return false;
        } else if (!flushLatency.equals(other.flushLatency))
            return false;
        return true;
    }

    @Override
    @XmlElement(name = "Id")
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "image_id")
    @Type(type = "guid")
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }
}
