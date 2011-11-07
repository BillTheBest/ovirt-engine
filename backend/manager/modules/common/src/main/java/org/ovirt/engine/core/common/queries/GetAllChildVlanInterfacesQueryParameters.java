package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.NetworkInterface;
import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAllChildVlanInterfacesQueryParameters")
public class GetAllChildVlanInterfacesQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2875732930025180055L;

    public GetAllChildVlanInterfacesQueryParameters(Guid vdsId, NetworkInterface<?> iface) {
        _vdsId = vdsId;
        _interface = iface;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "VdsId")
    private Guid _vdsId;

    public Guid getVdsId() {
        return _vdsId;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Interface")
    private NetworkInterface<?> _interface;

    public NetworkInterface<?> getInterface() {
        return _interface;
    }

    public GetAllChildVlanInterfacesQueryParameters() {
    }
}
