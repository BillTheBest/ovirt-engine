package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetTagsByUserGroupIdParameters")
public class GetTagsByUserGroupIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 7397977099793878525L;

    public GetTagsByUserGroupIdParameters(String groupId) {
        _groupId = groupId;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "GroupId")
    private String _groupId;

    public String getGroupId() {
        return _groupId;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetTagsByUserGroupIdParameters() {
    }
}
