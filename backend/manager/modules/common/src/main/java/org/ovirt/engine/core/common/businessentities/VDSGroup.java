package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.INotifyPropertyChanged;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VDSGroup", namespace = "http://service.engine.ovirt.org")
@Entity
@Table(name = "vds_groups")
@TypeDef(name = "guid", typeClass = GuidType.class)
@NamedQueries(
              value = {
                      @NamedQuery(
                                  name = "vdsgroup_with_running_vms",
                                  query = "from VDSGroup g where g.id = :vds_group_id and :vds_group_id in (select s.vds_group_id from VmStatic s, VmDynamic d where d.status not in (0, 13, 14) and d.id = s.id)")
              })
public class VDSGroup extends IVdcQueryable implements INotifyPropertyChanged, Serializable {


    private static final long serialVersionUID = 5659359762655478095L;

    public static final Guid DEFAULT_VDS_GROUP_ID = new Guid("99408929-82CF-4DC7-A532-9D998063FA95");

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "vds_group_id")
    @Type(type = "guid")
    private Guid id;

    @NotNull(message = "VALIDATION.VDS_GROUP.NAME.NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.CLUSTER_NAME_SIZE, message = "VALIDATION.VDS_GROUP.NAME.MAX",
            groups = {
            CreateEntity.class, UpdateEntity.class })
    @ValidName(message = "VALIDATION.VDS_GROUP.NAME.INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    @Column(name = "name")
    private String name = ""; // GREGM Prevents NPE

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "description")
    private String description;

    @Size(max = BusinessEntitiesDefinitions.CLUSTER_CPU_NAME_SIZE)
    @Column(name = "cpu_name")
    private String cpu_name;

    @XmlElement(name = "selection_algorithm")
    @Column(name = "selection_algorithm")
    private VdsSelectionAlgorithm selection_algorithm = VdsSelectionAlgorithm.None;

    @XmlElement(name = "high_utilization")
    @Column(name = "high_utilization")
    private int high_utilization = 0;

    @XmlElement(name = "low_utilization")
    @Column(name = "low_utilization")
    private int low_utilization = 0;

    @XmlElement(name = "cpu_over_commit_duration_minutes")
    @Column(name = "cpu_over_commit_duration_minutes")
    private int cpu_over_commit_duration_minutes = 0;

    @XmlElement(name = "hypervisor_type")
    @Column(name = "hypervisor_type")
    private HypervisorType hypervisor_type = HypervisorType.KVM;

    @XmlElement(name = "storage_pool_id")
    @Column(name = "storage_pool_id")
    @Type(type = "guid")
    private NGuid storagePool;


    @XmlElement(name = "max_vds_memory_over_commit")
    @Column(name = "max_vds_memory_over_commit")
    private int max_vds_memory_over_commit = 0;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_VERSION_SIZE)
    @Column(name = "compatibility_version")
    private String compatibility_version;

    @Transient
    private Version compatVersion;

    @XmlElement(name = "TransparentHugepages")
    @Column(name = "transparent_hugepages")
    private boolean transparentHugepages;

    @NotNull(message = "VALIDATION.VDS_GROUP.MigrateOnError.NOT_NULL")
    @XmlElement(name = "MigrateOnError")
    @Column(name = "migrate_on_error")
    private MigrateOnErrorOptions migrateOnError;

    public VDSGroup() {
        selection_algorithm = VdsSelectionAlgorithm.None;
        high_utilization = -1;
        low_utilization = -1;
        cpu_over_commit_duration_minutes = -1;
        hypervisor_type = HypervisorType.KVM;
        migrateOnError = MigrateOnErrorOptions.YES;
    }

    public VDSGroup(String name, String description, String cpu_name) {
        this();
        this.name = name;
        this.description = description;
        this.cpu_name = cpu_name;
    }

    @XmlElement
    public Guid getID() {
        return id;
    }

    public void setID(Guid value) {
        id = value;
    }

    public void setvds_group_id(Guid value) {
        setID(value);
    }

    @XmlElement
    public String getname() {
        return name;
    }

    public void setname(String value) {
        name = value;
    }

    @XmlElement
    public String getdescription() {
        return description;
    }

    public void setdescription(String value) {
        description = value;
    }

    @XmlElement
    public String getcpu_name() {
        return this.cpu_name;
    }

    public void setcpu_name(String value) {
        this.cpu_name = value;
    }

    public VdsSelectionAlgorithm getselection_algorithm() {
        return selection_algorithm;
    }

    public void setselection_algorithm(VdsSelectionAlgorithm value) {
        selection_algorithm = value;
    }

    public int gethigh_utilization() {
        return this.high_utilization;
    }

    public void sethigh_utilization(int value) {
        this.high_utilization = value;
    }

    public int getlow_utilization() {
        return this.low_utilization;
    }

    public void setlow_utilization(int value) {
        this.low_utilization = value;
    }

    public int getcpu_over_commit_duration_minutes() {
        return this.cpu_over_commit_duration_minutes;
    }

    public void setcpu_over_commit_duration_minutes(int value) {
        this.cpu_over_commit_duration_minutes = value;
    }

    public HypervisorType gethypervisor_type() {
        return this.hypervisor_type;
    }

    public void sethypervisor_type(HypervisorType value) {
        this.hypervisor_type = value;
    }

    public NGuid getstorage_pool_id() {
        return storagePool;
    }

    public void setstorage_pool_id(NGuid storagePool) {
        this.storagePool = storagePool;
    }


    public int getmax_vds_memory_over_commit() {
        return this.max_vds_memory_over_commit;
    }

    public void setmax_vds_memory_over_commit(int value) {
        this.max_vds_memory_over_commit = value;
    }

    @XmlElement(name = "compatibility_version")
    public Version getcompatibility_version() {
        return compatVersion;
    }

    public boolean getTransparentHugepages() {
        return this.transparentHugepages;
    }

    public void setTransparentHugepages(boolean value) {
        this.transparentHugepages = value;
    }

    public void setcompatibility_version(Version value) {
        compatibility_version = value.getValue();
        compatVersion = value;
    }

    @Override
    public Object getQueryableId() {
        return getID();
    }

    private static final java.util.ArrayList<String> _vmProperties = new java.util.ArrayList<String>(
            java.util.Arrays.asList(new String[] { "name", "description", "cpu_name",
                    "cpu_over_commit_duration_minutes", "low_utilization", "high_utilization", "selection_algorithm",
                    "max_vds_memory_over_commit", "storage_pool_id", "compatibility_version",
                    "TransparentHugepages", "MigrateOnError" }));

    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        return _vmProperties;
    }

    public void setMigrateOnError(MigrateOnErrorOptions migrateOnError) {
        this.migrateOnError = migrateOnError;
    }

    public MigrateOnErrorOptions getMigrateOnError() {
        return migrateOnError;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((compatVersion == null) ? 0 : compatVersion.hashCode());
        result = prime * result + ((compatibility_version == null) ? 0 : compatibility_version.hashCode());
        result = prime * result + ((cpu_name == null) ? 0 : cpu_name.hashCode());
        result =
            prime
            * result
            + cpu_over_commit_duration_minutes;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + high_utilization;
        result = prime * result + ((hypervisor_type == null) ? 0 : hypervisor_type.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + low_utilization;
        result = prime * result + max_vds_memory_over_commit;
        result = prime * result + ((migrateOnError == null) ? 0 : migrateOnError.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((selection_algorithm == null) ? 0 : selection_algorithm.hashCode());
        result = prime * result + ((storagePool == null) ? 0 : storagePool.hashCode());
        result = prime * result + (transparentHugepages ? 1231 : 1237);
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
        VDSGroup other = (VDSGroup) obj;
        if (compatVersion == null) {
            if (other.compatVersion != null)
                return false;
        } else if (!compatVersion.equals(other.compatVersion))
            return false;
        if (compatibility_version == null) {
            if (other.compatibility_version != null)
                return false;
        } else if (!compatibility_version.equals(other.compatibility_version))
            return false;
        if (cpu_name == null) {
            if (other.cpu_name != null)
                return false;
        } else if (!cpu_name.equals(other.cpu_name))
            return false;
        if (cpu_over_commit_duration_minutes != other.cpu_over_commit_duration_minutes)
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
        if (high_utilization != other.high_utilization)
            return false;
        if (hypervisor_type == null) {
            if (other.hypervisor_type != null)
                return false;
        } else if (!hypervisor_type.equals(other.hypervisor_type))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (low_utilization != other.low_utilization)
            return false;
        if (max_vds_memory_over_commit != other.max_vds_memory_over_commit)
            return false;
        if (migrateOnError != other.migrateOnError)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (selection_algorithm == null) {
            if (other.selection_algorithm != null)
                return false;
        } else if (!selection_algorithm.equals(other.selection_algorithm))
            return false;
        if (storagePool == null) {
            if (other.storagePool != null)
                return false;
        } else if (!storagePool.equals(other.storagePool))
            return false;
        if (transparentHugepages != other.transparentHugepages)
            return false;
        return true;
    }
}
