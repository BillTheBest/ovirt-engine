package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TimeSpan;

@XmlType(name = "vm_pools")
@Entity
@Table(name = "vm_pools")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class vm_pools extends IVdcQueryable implements INotifyPropertyChanged, Serializable {

    private static final long serialVersionUID = -2176168998321713354L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "vm_pool_id")
    @Type(type = "guid")
    private Guid id;

    @NotNull(message = "VALIDATION.VM_POOLS.NAME.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.VM_POOL_NAME_SIZE)
    @Column(name = "vm_pool_name")
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "vm_pool_description", length = 4000)
    private String description;

    @Column(name = "vm_pool_type")
    private VmPoolType type = VmPoolType.Automatic;

    @Size(max = BusinessEntitiesDefinitions.VM_POOL_PARAMS)
    @Column(name = "parameters", length = 200)
    private String parameters = "";

    @Column(name = "vds_group_id")
    @Type(type = "guid")
    private Guid vdsGroupId;

    @Transient
    private int defaultTimeInDays;

    @Transient
    private TimeSpan defaultStartTime = new TimeSpan();

    @Transient
    private TimeSpan defaultEndTime = new TimeSpan();

    @Transient
    private String vdsGroupName;

    @Transient
    private int vmPoolAssignedCount = 1;

    @Transient
    private int vmPoolRunningCount = 1;

    /**
     * TODO: Vitaly change it to better designed implementation
     */
    public vm_pools(String vm_pool_description, Guid vm_pool_id, String vm_pool_name, VmPoolType vmPoolType,
                    Guid vds_group_id, int defaultTimeInDays, TimeSpan defaultStartTime, TimeSpan defaultEndTime) {
        this(vm_pool_description, vm_pool_id, vm_pool_name, vmPoolType, vds_group_id);
        this.defaultTimeInDays = defaultTimeInDays;
        this.defaultStartTime = defaultStartTime;
        this.defaultEndTime = defaultEndTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultEndTime == null) ? 0 : defaultEndTime.hashCode());
        result = prime * result + ((defaultStartTime == null) ? 0 : defaultStartTime.hashCode());
        result = prime * result + defaultTimeInDays;
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((vdsGroupId == null) ? 0 : vdsGroupId.hashCode());
        result = prime * result + ((vdsGroupName == null) ? 0 : vdsGroupName.hashCode());
        result = prime * result + vmPoolAssignedCount;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + vmPoolRunningCount;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        vm_pools other = (vm_pools) obj;
        if (defaultEndTime == null) {
            if (other.defaultEndTime != null)
                return false;
        } else if (!defaultEndTime.equals(other.defaultEndTime))
            return false;
        if (defaultStartTime == null) {
            if (other.defaultStartTime != null)
                return false;
        } else if (!defaultStartTime.equals(other.defaultStartTime))
            return false;
        if (defaultTimeInDays != other.defaultTimeInDays)
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (vdsGroupId == null) {
            if (other.vdsGroupId != null)
                return false;
        } else if (!vdsGroupId.equals(other.vdsGroupId))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
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
        if (type == null) {
            if(other.type != null)
                return false;
        } else if(!type.equals(other.type))
            return false;
        return true;
    }

    private void InitializedTimeLeasedDefaultData(String parameter) {
        String[] values = parameter.split("[,]", -1);
        if (values.length == 3) {
            try {
                defaultTimeInDays = Integer.parseInt(values[0]);
                String[] startTime = values[1].split("[:]", -1);
                if (startTime.length > 1) {

                    defaultStartTime = new TimeSpan(Integer.parseInt(startTime[0]), Integer.parseInt(startTime[1]), 0);
                }
                String[] endTime = values[2].split("[:]", -1);
                if (endTime.length > 1) {
                    defaultEndTime = new TimeSpan(Integer.parseInt(endTime[0]), Integer.parseInt(endTime[1]), 0);
                }
            } catch (java.lang.Exception e) {
            }
        }
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // @XmlElement - don't serizlize parameters since it has logic in it
    public String getparameters() {
        switch (getvm_pool_type()) {
        case TimeLease: {
            return StringFormat.format("%1$s,%2$s:%3$s,%4$s:%5$s", defaultTimeInDays, defaultStartTime.Hours,
                                       defaultStartTime.Minutes, defaultEndTime.Hours, defaultEndTime.Minutes);
        }
        default: {
            return parameters;
        }
        }

    }

    public void setparameters(String value) {
        switch (getvm_pool_type()) {
        case TimeLease: {
            InitializedTimeLeasedDefaultData(value);
            break;
        }
        default: {
            parameters = value;
            break;
        }
        }

    }

    @XmlElement(name = "DefaultTimeInDays")
    public int getDefaultTimeInDays() {
        return defaultTimeInDays;
    }

    public void setDefaultTimeInDays(int value) {
        defaultTimeInDays = value;
    }

    @XmlElement(name = "DefaultStartTime")
    public TimeSpan getDefaultStartTime() {
        return defaultStartTime;
    }

    public void setDefaultStartTime(TimeSpan value) {
        defaultStartTime = value;
    }

    @XmlElement(name = "DefaultEndTime")
    public TimeSpan getDefaultEndTime() {
        return defaultEndTime;
    }

    public void setDefaultEndTime(TimeSpan value) {
        defaultEndTime = value;
    }

    public vm_pools() {
    }

    public vm_pools(String vm_pool_description, Guid vm_pool_id, String vm_pool_name, VmPoolType vmPoolType,
                    Guid vds_group_id) {
        this.description = vm_pool_description;
        this.id = vm_pool_id;
        this.name = vm_pool_name;
        this.type = vmPoolType;
        this.vdsGroupId = vds_group_id;
    }

    @XmlElement
    public String getvm_pool_description() {
        return this.description;
    }

    public void setvm_pool_description(String value) {
        this.description = value;
    }

    @XmlElement
    public Guid getvm_pool_id() {
        return this.id;
    }

    public void setvm_pool_id(Guid value) {
        this.id = value;
    }

    @XmlElement
    public String getvm_pool_name() {
        return this.name;
    }

    public void setvm_pool_name(String value) {
        if (!StringHelper.EqOp(this.name, value)) {
            this.name = value;
        }
    }

    @XmlElement
    public int getvm_assigned_count() {
        return vmPoolAssignedCount;
    }

    public void setvm_assigned_count(int value) {
        vmPoolAssignedCount = value;
    }

    @XmlElement
    public int getvm_running_count() {
        return vmPoolRunningCount;
    }

    public void setvm_running_count(int value) {
        vmPoolRunningCount = value;
    }

    @XmlElement
    public VmPoolType getvm_pool_type() {
        return type;
    }

    public void setvm_pool_type(VmPoolType value) {
        this.type = value;
    }

    @XmlElement
    public Guid getvds_group_id() {
        return this.vdsGroupId;
    }

    public void setvds_group_id(Guid value) {
        this.vdsGroupId = value;
    }

    @XmlElement
    public String getvds_group_name() {
        return this.vdsGroupName;
    }

    public void setvds_group_name(String value) {
        this.vdsGroupName = value;
    }

    @Override
    public Object getQueryableId() {
        return getvm_pool_id();
    }

    private static final java.util.ArrayList<String> _poolProperties = new java.util.ArrayList<String>(
            java.util.Arrays.asList(new String[] { "vm_pool_name", "vm_pool_description", "vm_pool_type",
                    "vm_running_count", "vm_assigned_count", "vds_group_name" }));

    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        return _poolProperties;
    }

}
