package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MultilevelAdministrationByPermissionIdParameters")
public class MultilevelAdministrationByPermissionIdParameters extends MultilevelAdministrationsQueriesParameters {
    private static final long serialVersionUID = -2853514093532756677L;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "PermissionId")
    private Guid privatePermissionId = new Guid();

    public Guid getPermissionId() {
        return privatePermissionId;
    }

    private void setPermissionId(Guid value) {
        privatePermissionId = value;
    }

    public MultilevelAdministrationByPermissionIdParameters(Guid permissionId) {
        setPermissionId(permissionId);
    }

    public MultilevelAdministrationByPermissionIdParameters() {
    }
}
