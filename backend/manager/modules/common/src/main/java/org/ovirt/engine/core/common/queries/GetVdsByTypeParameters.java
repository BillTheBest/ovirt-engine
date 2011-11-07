package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVdsByTypeParameters")
public class GetVdsByTypeParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 9106511793583677642L;

    public GetVdsByTypeParameters(VDSType vdsType) {
        _vdsType = vdsType;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private VDSType _vdsType = VDSType.forValue(0);

    public VDSType getVdsType() {
        return _vdsType;
    }

    public GetVdsByTypeParameters() {
    }
}
