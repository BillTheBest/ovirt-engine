package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetStorageDomainDescriptionVDSCommandParameters")
public class SetStorageDomainDescriptionVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Description")
    private String privateDescription;

    public String getDescription() {
        return privateDescription;
    }

    private void setDescription(String value) {
        privateDescription = value;
    }

    public SetStorageDomainDescriptionVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, String description) {
        super(storagePoolId, storageDomainId);
        setDescription(description);
    }

    public SetStorageDomainDescriptionVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, description = %s", super.toString(), getDescription());
    }
}
