package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.resource.DeviceResource;

public class BackendCdRomResource extends BackendDeviceResource<CdRom, CdRoms, VM> implements DeviceResource<CdRom>{

    public static final String CURRENT_CONSTRAINT_PARAMETER = "current";

    public BackendCdRomResource(Class<CdRom> modelType,
                                 Class<VM> entityType,
                                 final Guid guid,
                                 final AbstractBackendReadOnlyDevicesResource<CdRom, CdRoms, VM> collection,
                                 VdcActionType updateType,
                                 ParametersProvider<CdRom, VM> updateParametersProvider,
                                 String[] requiredUpdateFields,
                                 String... subCollections) {
        super(modelType, entityType, guid, collection, updateType, updateParametersProvider, requiredUpdateFields, subCollections);
    }

    @Override
    public CdRom update(CdRom resource) {
        if (QueryHelper.hasConstraint(getUriInfo().getQueryParameters(), CURRENT_CONSTRAINT_PARAMETER)) {
            validateParameters(resource, requiredUpdateFields);
            performAction(VdcActionType.ChangeDisk,
                          new ChangeDiskCommandParameters(getEntity(entityResolver, true).getvm_guid(),
                                                          resource.getFile().getId()));
            return resource;
        } else {
            return super.update(resource);
        }
    }
}
