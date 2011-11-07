package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "LicenseReturnValue")
public class LicenseReturnValue extends VdcQueryReturnValue {
    private static final long serialVersionUID = 7270804103951142563L;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Messages")
    private java.util.ArrayList<String> _messages;

    public java.util.ArrayList<String> getMessages() {
        return _messages;
    }

    public void setMessages(java.util.ArrayList<String> value) {
        _messages = value;
    }

    public LicenseReturnValue() {
    }
}
