package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DbUser", namespace = "http://service.engine.ovirt.org")
@Entity
@Table(name = "users")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class DbUser extends DbUserBase implements Serializable {
    private static final long serialVersionUID = 7052102138405696755L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "user_id")
    @Type(type = "guid")
    private Guid id = new Guid();

    @Size(max = BusinessEntitiesDefinitions.USER_NAME_SIZE)
    @Column(name = "name")
    private String name = "";

    @Size(max = BusinessEntitiesDefinitions.USER_SURENAME_SIZE)
    @Column(name = "surname")
    private String surname = "";

    @Size(min = 1, max = BusinessEntitiesDefinitions.USER_DOMAIN_SIZE)
    @Column(name = "domain")
    private String domain;

    @Size(min = 1, max = BusinessEntitiesDefinitions.USER_USER_NAME_SIZE)
    @Column(name = "username")
    private String username = "";

    @Size(min = 1, max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    @Column(name = "groups", length = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String groups;

    @Size(max = BusinessEntitiesDefinitions.USER_DEPARTMENT_SIZE)
    @Column(name = "department")
    private String department = "";

    @Size(max = BusinessEntitiesDefinitions.USER_ROLE_SIZE)
    @Column(name = "role")
    private String role = "";

    @Size(max = BusinessEntitiesDefinitions.USER_ICON_PATH_SIZE)
    @Column(name = "user_icon_path")
    private String userIconPath = "";

    @Size(max = BusinessEntitiesDefinitions.USER_DESKTOP_DEVICE_SIZE)
    @Column(name = "desktop_device")
    private String desktopDevice = "";

    @Size(max = BusinessEntitiesDefinitions.USER_EMAIL_SIZE)
    @Column(name = "email")
    private String email;

    @Size(max = BusinessEntitiesDefinitions.USER_NOTE_SIZE)
    @Column(name = "note")
    private String note = "";

    @Column(name = "status")
    private int status;

    @Column(name = "session_count")
    private int sessionCount;

    @Transient
    private boolean isLoggedIn;

    /**
     * GUI flag only. Do not use for internal logic. The sole purpose of calculating this field is for the GUI user to
     * understand who is admin in a snap on the user-grid
     */
    @XmlElement(name = "LastAdminCheckStatus")
    @Transient
    private boolean lastAdminCheckStatus;

    /**
     * comma delimited list of group guids
     */
    @Size(max = BusinessEntitiesDefinitions.USER_GROUP_IDS_SIZE)
    @Column(name = "group_ids")
    private String groupIds;

    public DbUser() {
    }

    public DbUser(String department, String desktop_device, String domain, String email, String groups, String name,
            String note, String role, int status, String surname, String user_icon_path, Guid user_id, String username,
            int sessionCount, String groupIds) {
        this.department = department;
        this.desktopDevice = desktop_device;
        this.domain = domain;
        this.email = email;
        this.groups = groups;
        this.name = name;
        this.note = note;
        this.role = role;
        this.status = status;
        this.surname = surname;
        this.userIconPath = user_icon_path;
        this.id = user_id;
        this.username = username;
        this.sessionCount = sessionCount;
        this.setGroupIds(groupIds);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((department == null) ? 0 : department.hashCode());
        result = prime
                * result
                + ((desktopDevice == null) ? 0 : desktopDevice
                        .hashCode());
        result = prime * result
                + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result
                + ((email == null) ? 0 : email.hashCode());
        result = prime * result
                + ((groups == null) ? 0 : groups.hashCode());
        result = prime * result + (isLoggedIn ? 1231 : 1237);
        result = prime * result + (lastAdminCheckStatus ? 1231 : 1237);
        result = prime * result
                + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((note == null) ? 0 : note.hashCode());
        result = prime * result
                + ((role == null) ? 0 : role.hashCode());
        result = prime * result + sessionCount;
        result = prime * result + status;
        result = prime * result
                + ((surname == null) ? 0 : surname.hashCode());
        result = prime
                * result
                + ((userIconPath == null) ? 0 : userIconPath
                        .hashCode());
        result = prime * result
                + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((username == null) ? 0 : username.hashCode());
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
        DbUser other = (DbUser) obj;
        if (department == null) {
            if (other.department != null)
                return false;
        } else if (!department.equals(other.department))
            return false;
        if (desktopDevice == null) {
            if (other.desktopDevice != null)
                return false;
        } else if (!desktopDevice.equals(other.desktopDevice))
            return false;
        if (domain == null) {
            if (other.domain != null)
                return false;
        } else if (!domain.equals(other.domain))
            return false;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (groups == null) {
            if (other.groups != null)
                return false;
        } else if (!groups.equals(other.groups))
            return false;
        if (isLoggedIn != other.isLoggedIn)
            return false;
        if (lastAdminCheckStatus != other.lastAdminCheckStatus)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (note == null) {
            if (other.note != null)
                return false;
        } else if (!note.equals(other.note))
            return false;
        if (role == null) {
            if (other.role != null)
                return false;
        } else if (!role.equals(other.role))
            return false;
        if (sessionCount != other.sessionCount)
            return false;
        if (status != other.status)
            return false;
        if (surname == null) {
            if (other.surname != null)
                return false;
        } else if (!surname.equals(other.surname))
            return false;
        if (userIconPath == null) {
            if (other.userIconPath != null)
                return false;
        } else if (!userIconPath.equals(other.userIconPath))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    @XmlElement
    public String getdepartment() {
        return this.department;
    }

    public void setdepartment(String value) {
        this.department = value;
    }

    @XmlElement
    public String getdesktop_device() {
        return this.desktopDevice;
    }

    public void setdesktop_device(String value) {
        this.desktopDevice = value;
    }

    @XmlElement
    public String getdomain() {
        return this.domain;
    }

    public void setdomain(String value) {
        this.domain = value;
    }

    @XmlElement
    public String getemail() {
        return this.email;
    }

    public void setemail(String value) {
        this.email = value;
    }

    @XmlElement
    public String getgroups() {
        return this.groups;
    }

    public void setgroups(String value) {
        this.groups = value;
        OnPropertyChanged(new PropertyChangedEventArgs("groups"));
    }

    @XmlElement
    public String getname() {
        return this.name;
    }

    public void setname(String value) {
        if (!StringHelper.EqOp(this.name, value)) {
            this.name = value;
            OnPropertyChanged(new PropertyChangedEventArgs("name"));
        }
    }

    @XmlElement
    public String getnote() {
        return this.note;
    }

    public void setnote(String value) {
        this.note = value;
    }

    @XmlElement
    public String getrole() {
        return this.role;
    }

    public void setrole(String value) {
        this.role = value;
    }

    @XmlElement
    public int getstatus() {
        return this.status;
    }

    public void setstatus(int value) {
        this.status = value;
    }

    @XmlElement
    public String getsurname() {
        return this.surname;
    }

    public void setsurname(String value) {
        this.surname = value;
        OnPropertyChanged(new PropertyChangedEventArgs("surname"));
    }

    @XmlElement
    public String getuser_icon_path() {
        return this.userIconPath;
    }

    public void setuser_icon_path(String value) {
        this.userIconPath = value;
    }

    @XmlElement(name = "user_id")
    public Guid getuser_id() {
        return this.id;
    }

    public void setuser_id(Guid value) {
        this.id = value;
    }

    @XmlElement(name = "username")
    public String getusername() {
        return this.username;
    }

    public void setusername(String value) {
        this.username = value;
        OnPropertyChanged(new PropertyChangedEventArgs("username"));
    }

    @XmlElement
    public int getsession_count() {
        return sessionCount;
    }

    public void setsession_count(int value) {
        sessionCount = value;
        OnPropertyChanged(new PropertyChangedEventArgs("session_count"));
        setIsLogedin((sessionCount > 0));
    }

    @XmlElement(name = "IsLogedin")
    public boolean getIsLogedin() {
        return isLoggedIn;
    }

    public void setIsLogedin(boolean value) {
        isLoggedIn = value;
        OnPropertyChanged(new PropertyChangedEventArgs("IsLogedin"));
    }

    public DbUser(AdUser adUser) {
        setuser_id(adUser.getUserId());
        setusername(adUser.getUserName());
        setname(adUser.getName());
        setsurname(adUser.getSurName());
        setdepartment(adUser.getDepartment());
        setdomain(adUser.getDomainControler());
        setemail(adUser.getEmail());
        setgroups(adUser.getGroup());
        setstatus(AdRefStatus.Active.getValue());
        setGroupIds(adUser.getGroupIds());
    }

    public AdRefStatus getAdStatus() {
        if (getstatus() == 0) {
            return AdRefStatus.Inactive;
        } else {
            return AdRefStatus.Active;
        }
    }

    public boolean getIsGroup() {
        return StringHelper.isNullOrEmpty(getusername());
    }

    public void setIsGroup(boolean value) {
        // do nothing for nothing
    }

    @Override
    public Object getQueryableId() {
        return getuser_id();
    }

    private static final java.util.ArrayList<String> _vmProperties = new java.util.ArrayList<String>(java.util.Arrays
            .asList(new String[] { "name", "surname", "username", "groups", "session_count", "IsLogedin",
                    "LastAdminCheckStatus" }));

    @Override
    public java.util.ArrayList<String> getChangeablePropertiesList() {
        return _vmProperties;
    }

    public void setLastAdminCheckStatus(boolean val) {
        this.lastAdminCheckStatus = val;
    }

    public boolean getLastAdminCheckStatus() {
        return lastAdminCheckStatus;
    }

    /**
     * Returns the user's given and family name and username in a standard format.
     *
     * @return the coalesced name
     */
    public String getCoalescedName() {
        return name + " " + surname + " (" + username + ")";
    }

    /**
     * Returns the set of group names as an array.
     *
     * @return the group names
     */
    public String[] getGroupsAsArray() {
        return groups.split(",");
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }

    public String getGroupIds() {
        return groupIds;
    }
}
