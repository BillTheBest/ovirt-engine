package org.ovirt.engine.api.restapi.resource;


import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.resource.AssignedNetworksResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVdsGroupByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import static org.ovirt.engine.api.restapi.resource.BackendClustersResource.SUB_COLLECTIONS;

public class BackendClusterResource extends AbstractBackendSubResource<Cluster, VDSGroup> implements
        ClusterResource {

    public BackendClusterResource(String id) {
        super(id, Cluster.class, VDSGroup.class, SUB_COLLECTIONS);
    }

    @Override
    public Cluster get() {
        return performGet(VdcQueryType.GetVdsGroupById, new GetVdsGroupByIdParameters(guid));
    }

    @Override
    public Cluster update(Cluster incoming) {
        return performUpdate(incoming,
                             new QueryIdResolver(VdcQueryType.GetVdsGroupById, GetVdsGroupByIdParameters.class),
                             VdcActionType.UpdateVdsGroup,
                             new UpdateParametersProvider());
    }

    @Override
    public AssignedNetworksResource getAssignedNetworksSubResource() {
        return inject(new BackendClusterNetworksResource(id));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             Cluster.class,
                                                             VdcObjectType.VdsGroups));
    }

    protected class UpdateParametersProvider implements ParametersProvider<Cluster, VDSGroup> {
        @Override
        public VdcActionParametersBase getParameters(Cluster incoming, VDSGroup entity) {
            return new VdsGroupOperationParameters(map(incoming, entity));
        }
    }
}
