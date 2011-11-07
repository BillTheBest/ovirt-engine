package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.constraints.Email;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "event_subscriber")
@Entity
@Table(name = "event_subscriber")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class event_subscriber extends IVdcQueryable implements INotifyPropertyChanged, Serializable {
    private static final long serialVersionUID = 5899827011779820180L;

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "subscriberId", column = @Column(name = "subscriber_id")),
            @AttributeOverride(name = "eventUpName", column = @Column(name = "event_up_name")),
            @AttributeOverride(name = "methodId", column = @Column(name = "method_id")),
            @AttributeOverride(name = "tagName", column = @Column(name = "tag_name")) })
    private event_subscriber_id id = new event_subscriber_id();

    public event_subscriber() {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((id.eventUpName == null) ? 0 : id.eventUpName
                        .hashCode());
        result = prime
                * result
                + ((methodAddress == null) ? 0 : methodAddress
                        .hashCode());
        result = prime * result + id.methodId;
        result = prime
                * result
                + ((id.subscriberId == null) ? 0 : id.subscriberId
                        .hashCode());
        result = prime * result
                + ((id.tagName == null) ? 0 : id.tagName.hashCode());
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
        event_subscriber other = (event_subscriber) obj;
        if (id.eventUpName == null) {
            if (other.id.eventUpName != null)
                return false;
        } else if (!id.eventUpName.equals(other.id.eventUpName))
            return false;
        if (methodAddress == null) {
            if (other.methodAddress != null)
                return false;
        } else if (!methodAddress.equals(other.methodAddress))
            return false;
        if (id.methodId != other.id.methodId)
            return false;
        if (id.subscriberId == null) {
            if (other.id.subscriberId != null)
                return false;
        } else if (!id.subscriberId.equals(other.id.subscriberId))
            return false;
        if (id.tagName == null) {
            if (other.id.tagName != null)
                return false;
        } else if (!id.tagName.equals(other.id.tagName))
            return false;
        return true;
    }

    public event_subscriber(String event_up_name, int method_id, Guid subscriber_id, String tagName) {
        this.id.eventUpName = event_up_name;
        this.id.methodId = method_id;
        this.methodAddress = "";
        this.id.subscriberId = subscriber_id;
        this.id.tagName = tagName;
    }

    public event_subscriber(String event_up_name, int method_id, String method_address, Guid subscriber_id,
            String tagName) {
        this.id.eventUpName = event_up_name;
        this.id.methodId = method_id;
        this.methodAddress = method_address;
        this.id.subscriberId = subscriber_id;
        this.id.tagName = tagName;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    public String getevent_up_name() {
        return this.id.eventUpName;
    }

    public void setevent_up_name(String value) {
        this.id.eventUpName = value;
        OnPropertyChanged(new PropertyChangedEventArgs("event_up_name"));
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    public int getmethod_id() {
        return this.id.methodId;
    }

    public void setmethod_id(int value) {
        this.id.methodId = value;
        OnPropertyChanged(new PropertyChangedEventArgs("method_id"));
    }

    @Column(name = "method_address", length = 255)
    @Email(message = "VALIDATION.EVENTS.EMAIL_FORMAT")
    private String methodAddress;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    public String getmethod_address() {
        return this.methodAddress;
    }

    public void setmethod_address(String value) {
        this.methodAddress = value;
        OnPropertyChanged(new PropertyChangedEventArgs("method_address"));
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    public Guid getsubscriber_id() {
        return this.id.subscriberId;
    }

    public void setsubscriber_id(Guid value) {
        this.id.subscriberId = value;
        OnPropertyChanged(new PropertyChangedEventArgs("subscriber_id"));
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    public String gettag_name() {
        return this.id.tagName;
    }

    public void settag_name(String value) {
        this.id.tagName = value;
        OnPropertyChanged(new PropertyChangedEventArgs("tag_name"));
    }

    // if there will be subscribers edit we should add unique field to this
    // table
    @Override
    public Object getQueryableId() {
        return StringFormat.format("%1$s%2$s%3$s%4$s", id.eventUpName, id.methodId, id.subscriberId,
                id.tagName == null ? "" : id.tagName);
    }

    private static final java.util.ArrayList<String> _event_subscriberProperties = new java.util.ArrayList<String>(
            java.util.Arrays.asList(new String[] { "event_up_name", "method_id", "method_address", "subscriber_id",
                    "tag_name" }));

    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        return _event_subscriberProperties;
    }

    // C# TO JAVA CONVERTER TODO TASK: Events are not available in Java:
    // public event PropertyChangedEventHandler PropertyChanged;

    protected void OnPropertyChanged(PropertyChangedEventArgs e) {
        /* if (PropertyChanged != null) */
        {
            /* PropertyChanged(this, e); */
        }
    }

}
