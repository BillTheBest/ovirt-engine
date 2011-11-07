package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "FenceVdsActionParameters")
public class FenceVdsActionParameters extends VdsActionParameters {
    private static final long serialVersionUID = 6174371941176548263L;

    public FenceVdsActionParameters(Guid vdsId, FenceActionType action) {
        super(vdsId);
        _action = action;
    }

    @XmlElement
    private FenceActionType _action = FenceActionType.forValue(0);

    public FenceActionType getAction() {
        return _action;
    }

    public FenceVdsActionParameters() {
    }
}
