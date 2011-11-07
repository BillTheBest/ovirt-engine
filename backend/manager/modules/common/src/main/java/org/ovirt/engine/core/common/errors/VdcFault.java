package org.ovirt.engine.core.common.errors;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdcFault")
public class VdcFault extends IVdcQueryable {
    private static final long serialVersionUID = -8004317251171749327L;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "SessionID")
    private String privateSessionID;

    public String getSessionID() {
        return privateSessionID;
    }

    public void setSessionID(String value) {
        privateSessionID = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Details")
    private java.util.ArrayList<String> privateDetails;

    public java.util.ArrayList<String> getDetails() {
        return privateDetails;
    }

    public void setDetails(java.util.ArrayList<String> value) {
        privateDetails = value;
    }

    private VdcBllErrors _Error;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Error")
    public VdcBllErrors getError() {
        return _Error;
    }

    public void setError(VdcBllErrors value) {
        _Error = value;
    }

    @XmlElement(name = "ErrorCode")
    public int getErrorCode() {
        return _Error.getValue();
    }

    public void setError(int value) {
        _Error = VdcBllErrors.forValue(value);
    }

    public VdcFault(RuntimeException ex, VdcBllErrors error) {
        this(ex);
        setMessage(error.toString());
        setError(error);
    }

    public VdcFault(RuntimeException ex) {
        _Error = VdcBllErrors.unexpected;
        setMessage(ex.getMessage());

        // Pass over the inner exceptions and accumulate them within an array.
        setDetails(getInnerException(ex));
    }

    public VdcFault() {
        _Error = VdcBllErrors.unexpected;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Message")
    private String privateMessage;

    public String getMessage() {
        return privateMessage;
    }

    public void setMessage(String value) {
        privateMessage = value;
    }

    private static java.util.ArrayList<String> getInnerException(Throwable ex) {
        java.util.ArrayList<String> result = new java.util.ArrayList<String>();
        while (ex.getCause() != null) {
            result.add(ex.getCause().getMessage());
            ex = ex.getCause();
        }
        return result;
    }

}
