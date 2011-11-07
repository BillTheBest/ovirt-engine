package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("NetworkName") })
public abstract class VdsNetworkCommand<T extends AttachNetworkToVdsParameters> extends VdsCommand<T> {
    public VdsNetworkCommand(T parameters) {
        super(parameters);
    }

    public String getNetworkName() {
        return getParameters().getNetwork().getname();
    }
}
