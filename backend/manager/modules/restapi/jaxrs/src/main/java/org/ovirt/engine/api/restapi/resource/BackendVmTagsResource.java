package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetTagsByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendVmTagsResource
    extends AbstractBackendAssignedTagsResource
    implements AssignedTagsResource {

    public BackendVmTagsResource(String parentId) {
        super(VM.class, parentId, VdcActionType.AttachVmsToTag, VdcActionType.DetachVmFromTag);
    }

    public List<tags> getCollection() {
        return getBackendCollection(VdcQueryType.GetTagsByVmId, new GetTagsByVmIdParameters(parentId));
    }

    protected TagsActionParametersBase getAttachParams(String id) {
        return new AttachEntityToTagParameters(asGuid(id), asList(asGuid(parentId)));
    }
}
