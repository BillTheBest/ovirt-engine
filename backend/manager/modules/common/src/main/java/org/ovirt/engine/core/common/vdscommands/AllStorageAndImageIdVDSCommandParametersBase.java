package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AllStorageAndImageIdVDSCommandParametersBase")
public class AllStorageAndImageIdVDSCommandParametersBase extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    public AllStorageAndImageIdVDSCommandParametersBase(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            Guid imageId) {
        super(storagePoolId, storageDomainId, imageGroupId);
        _imageId = imageId;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private Guid _imageId = new Guid();

    public Guid getImageId() {
        return _imageId;
    }

    public AllStorageAndImageIdVDSCommandParametersBase() {
    }

    @Override
    public String toString() {
        return String.format("%s, imageId = %s", super.toString(), getImageId());
    }
}
