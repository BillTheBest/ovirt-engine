package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "HSMGetIsoListParameters")
public class HSMGetIsoListParameters extends VdsIdVDSCommandParametersBase {
    public HSMGetIsoListParameters(Guid vdsId, Guid storagePoolId) {
        super(vdsId);
        setStoragePoolId(storagePoolId);
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    public HSMGetIsoListParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, storagePoolId=%s", super.toString(), getStoragePoolId());
    }
}
