package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetVmTicketVDSCommandParameters")
public class SetVmTicketVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private String _ticket;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private int _validTime;

    public SetVmTicketVDSCommandParameters(Guid vdsId, Guid vmId, String ticket, int validTime) {
        super(vdsId, vmId);
        _ticket = ticket;
        _validTime = validTime;
    }

    public String getTicket() {
        return _ticket;
    }

    public int getValidTime() {
        return _validTime;
    }

    public SetVmTicketVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, ticket=%s, validTime=%s", super.toString(), getTicket(), getValidTime());
    }
}
