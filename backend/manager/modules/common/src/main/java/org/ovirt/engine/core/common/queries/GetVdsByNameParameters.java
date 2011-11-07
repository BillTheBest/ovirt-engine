package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVdsByNameParameters")
public class GetVdsByNameParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5403234842490143970L;

    public GetVdsByNameParameters(String name) {
        _name = name;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Name")
    private String _name;

    public String getName() {
        return _name;
    }

    public GetVdsByNameParameters() {
    }
}
