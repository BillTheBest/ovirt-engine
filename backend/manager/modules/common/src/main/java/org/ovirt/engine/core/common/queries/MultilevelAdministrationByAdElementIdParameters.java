package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MultilevelAdministrationByAdElementIdParameters")
public class MultilevelAdministrationByAdElementIdParameters extends MultilevelAdministrationsQueriesParameters {
    private static final long serialVersionUID = 7614186603701768993L;

    public MultilevelAdministrationByAdElementIdParameters(Guid adElementId) {
        setAdElementId(adElementId);
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "AdElementId")
    private Guid privateAdElementId = new Guid();

    public Guid getAdElementId() {
        return privateAdElementId;
    }

    private void setAdElementId(Guid value) {
        privateAdElementId = value;
    }

    public MultilevelAdministrationByAdElementIdParameters() {
    }
}
