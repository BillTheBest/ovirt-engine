package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "IsVmWithSameNameExistParameters", namespace = "http://service.engine.ovirt.org")
public class IsVmWithSameNameExistParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -329586454172262561L;

    public IsVmWithSameNameExistParameters(String vmName) {
        _vmName = vmName;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "VmName", required = true)
    private String _vmName;

    public String getVmName() {
        return _vmName;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public IsVmWithSameNameExistParameters() {
    }
}
