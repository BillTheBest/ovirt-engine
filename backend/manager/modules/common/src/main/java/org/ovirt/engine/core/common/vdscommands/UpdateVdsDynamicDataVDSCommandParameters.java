package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VdsDynamic;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "UpdateVdsDynamicDataVDSCommandParameters")
public class UpdateVdsDynamicDataVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "VdsDynamic")
    private VdsDynamic privateVdsDynamic;

    public VdsDynamic getVdsDynamic() {
        return privateVdsDynamic;
    }

    private void setVdsDynamic(VdsDynamic value) {
        privateVdsDynamic = value;
    }

    public UpdateVdsDynamicDataVDSCommandParameters(VdsDynamic vdsDynamic) {
        super(vdsDynamic.getId());
        setVdsDynamic(vdsDynamic);
    }

    public UpdateVdsDynamicDataVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, vdsDynamic=%s", super.toString(), getVdsDynamic());
    }
}
