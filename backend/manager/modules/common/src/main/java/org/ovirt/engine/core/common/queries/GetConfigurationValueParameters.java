package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetConfigurationValueParameters", namespace = "http://service.engine.ovirt.org")
public class GetConfigurationValueParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5889171970595969719L;

    public GetConfigurationValueParameters(ConfigurationValues cVal) {
        _configValue = cVal;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "ConfigValue", required = true)
    private ConfigurationValues _configValue = ConfigurationValues.forValue(0);

    public ConfigurationValues getConfigValue() {
        return _configValue;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Version")
    private String privateVersion;

    public String getVersion() {
        return privateVersion;
    }

    public void setVersion(String value) {
        privateVersion = value;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetConfigurationValueParameters() {
    }
}
