package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "HibernateVmParameters")
public class HibernateVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = 4526154915680207381L;
    @XmlElement(name = "AutomaticSuspend")
    private boolean privateAutomaticSuspend;

    public boolean getAutomaticSuspend() {
        return privateAutomaticSuspend;
    }

    public void setAutomaticSuspend(boolean value) {
        privateAutomaticSuspend = value;
    }

    public HibernateVmParameters(Guid vmId) {
        super(vmId);
    }

    public HibernateVmParameters() {
    }
}
