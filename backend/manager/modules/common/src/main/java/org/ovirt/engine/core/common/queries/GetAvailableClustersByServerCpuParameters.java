package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAvailableClustersByServerCpuParameters")
public class GetAvailableClustersByServerCpuParameters extends GetAllServerCpuListParameters {
    private static final long serialVersionUID = -6088424248179452456L;

    public GetAvailableClustersByServerCpuParameters(String cpuName, Version version) {
        super(version);
        _cpuName = cpuName;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "CpuName")
    private String _cpuName;

    public String getCpuName() {
        return _cpuName;
    }

    public GetAvailableClustersByServerCpuParameters() {
    }
}
