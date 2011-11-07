package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "IrsBaseVDSCommandParameters")
public class IrsBaseVDSCommandParameters extends VDSParametersBase {
    public IrsBaseVDSCommandParameters(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "StoragePoolId")
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    public void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "IgnoreFailoverLimit")
    private boolean privateIgnoreFailoverLimit;

    public boolean getIgnoreFailoverLimit() {
        return privateIgnoreFailoverLimit;
    }

    public void setIgnoreFailoverLimit(boolean value) {
        privateIgnoreFailoverLimit = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private String privateCompatibilityVersion;

    public String getCompatibilityVersion() {
        return privateCompatibilityVersion;
    }

    public void setCompatibilityVersion(String value) {
        privateCompatibilityVersion = value;
    }

    public IrsBaseVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("storagePoolId = %s, ignoreFailoverLimit = %s, compatabilityVersion = %s",
                getStoragePoolId(), getIgnoreFailoverLimit(), getCompatibilityVersion());
    }
}
