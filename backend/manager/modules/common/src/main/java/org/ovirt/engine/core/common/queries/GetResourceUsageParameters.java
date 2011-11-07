package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetResourceUsageParameters")
public class GetResourceUsageParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1804179976690860124L;

    public GetResourceUsageParameters(String resourceName) {
        _resourceName = resourceName;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "ResourceName")
    private String _resourceName;

    public String getResourceName() {
        return _resourceName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetResourceUsageParameters() {
    }
}
