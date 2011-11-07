package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.RolesOperationsParameters;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public abstract class RolesOperationCommandBase<T extends RolesOperationsParameters> extends RolesCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    public RolesOperationCommandBase(Guid commandId) {
        super(commandId);
    }

    public RolesOperationCommandBase(T parameters) {
        super(parameters);
    }

    @Override
    protected roles getRole() {
        return getParameters().getRole();
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class, UpdateEntity.class);
        return super.getValidationGroups();
    }
}
