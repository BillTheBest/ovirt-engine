package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Ticket;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.DevicesResource;
import org.ovirt.engine.api.resource.SnapshotsResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.HibernateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.MigrateVmToServerParameters;
import org.ovirt.engine.core.common.action.MoveVmParameters;
import org.ovirt.engine.core.common.action.RemoveVmFromPoolParameters;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.ShutdownVmParameters;
import org.ovirt.engine.core.common.action.StopVmParameters;
import org.ovirt.engine.core.common.action.StopVmTypeEnum;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.core.utils.Ticketing.GenerateOTP;

import static org.ovirt.engine.api.restapi.resource.BackendVmsResource.SUB_COLLECTIONS;

public class BackendVmResource extends
        AbstractBackendActionableResource<VM, org.ovirt.engine.core.common.businessentities.VM> implements
        VmResource {

    private static final long DEFAULT_TICKET_EXPIRY = 120 * 60; // 2 hours

    private BackendVmsResource parent;

    public BackendVmResource(String id, BackendVmsResource parent) {
        super(id, VM.class, org.ovirt.engine.core.common.businessentities.VM.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    @Override
    public VM get() {
        return performGet(VdcQueryType.GetVmByVmId, new GetVmByVmIdParameters(guid));
    }

    @Override
    public VM update(VM incoming) {
        if (incoming.isSetCluster() && (incoming.getCluster().isSetId() || incoming.getCluster().isSetName())) {
            Guid clusterId = lookupClusterId(incoming);
            if(!clusterId.toString().equals(get().getCluster().getId())){
                performAction(VdcActionType.ChangeVMCluster,
                              new ChangeVMClusterParameters(clusterId, guid));
            }
        }

        return performUpdate(incoming,
                             new QueryIdResolver(VdcQueryType.GetVmByVmId, GetVmByVmIdParameters.class),
                             VdcActionType.UpdateVm,
                             new UpdateParametersProvider());
    }

    protected Guid lookupClusterId(VM vm) {
        return vm.getCluster().isSetId() ? asGuid(vm.getCluster().getId())
                                           :
                                           getEntity(VDSGroup.class,
                                                     SearchType.Cluster,
                                                     "Cluster: name=" + vm.getCluster().getName()).getID();
    }

    @Override
    public DevicesResource<CdRom, CdRoms> getCdRomsResource() {
        return inject(new BackendCdRomsResource(guid,
                                                VdcQueryType.GetVmByVmId,
                                                new GetVmByVmIdParameters(guid)));
    }

    @Override
    public DevicesResource<Disk, Disks> getDisksResource() {
        return inject(new BackendDisksResource(guid,
                                               VdcQueryType.GetAllDisksByVmId,
                                               new GetAllDisksByVmIdParameters(guid)));
    }

    @Override
    public DevicesResource<NIC, Nics> getNicsResource() {
        return inject(new BackendVmNicsResource(guid));
    }

    @Override
    public SnapshotsResource getSnapshotsResource() {
        return inject(new BackendSnapshotsResource(guid));
    }

    @Override
    public AssignedTagsResource getTagsResource() {
        return inject(new BackendVmTagsResource(id));
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             VM.class,
                                                             VdcObjectType.VM));
    }

    @Override
    public CreationResource getCreationSubresource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public ActionResource getActionSubresource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        EntityIdResolver resolver = new QueryIdResolver(VdcQueryType.GetVmByVmId, GetVmByVmIdParameters.class);
        VmStatisticalQuery query = new VmStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<VM, org.ovirt.engine.core.common.businessentities.VM>(entityType, guid, query));
    }

    @Override
    public Response migrate(Action action) {
        boolean forceMigration = action.isSetForce() ? action.isForce() : false;
        if (!action.isSetHost()) {
            return doAction(VdcActionType.MigrateVm,
                    new MigrateVmParameters(forceMigration, guid),
                    action);
        } else {
            return doAction(VdcActionType.MigrateVmToServer,
                        new MigrateVmToServerParameters(forceMigration, guid, getHostId(action)),
                        action);
        }
    }

    @Override
    public Response shutdown(Action action) {
        // REVISIT add waitBeforeShutdown Action paramater
        // to api schema before next sub-milestone
        return doAction(VdcActionType.ShutdownVm,
                        new ShutdownVmParameters(guid, true),
                        action);
    }

    @Override
    public Response start(Action action) {
        RunVmOnceParams params = new RunVmOnceParams(guid);

        if (action.isSetPause() && action.isPause()) {
            params.setRunAndPause(true);
        }

        if (action.isSetVm()) {
            VM vm = action.getVm();

            if (vm.isSetPlacementPolicy() && vm.getPlacementPolicy().isSetHost()) {
                validateParameters(vm.getPlacementPolicy(), "host.id|name");
                params.setDestinationVdsId(getHostId(vm.getPlacementPolicy().getHost()));
            }

            params = map(vm, params);
        }

        return doAction(VdcActionType.RunVmOnce, setReinitializeSysPrep(params), action);
    }

    private VdcActionParametersBase setReinitializeSysPrep(RunVmOnceParams params) {
        //REVISE when BE supports default val. for RunVmOnceParams.privateReinitialize
        org.ovirt.engine.core.common.businessentities.VM vm = getEntity(org.ovirt.engine.core.common.businessentities.VM.class,
                                                                        VdcQueryType.GetVmByVmId,
                                                                        new GetVmByVmIdParameters(guid),
                                                                        "VM");
        if(vm.getvm_os().isWindows() && vm.getIsFirstRun()) {
            params.setReinitialize(true);
        }
        return params;
    }

    @Override
    public Response stop(Action action) {
        return doAction(VdcActionType.StopVm,
                        new StopVmParameters(guid, StopVmTypeEnum.NORMAL),
                        action);
    }

    @Override
    public Response suspend(Action action) {
        return doAction(VdcActionType.HibernateVm,
                        new HibernateVmParameters(guid),
                        action);
    }

    @Override
    public Response detach(Action action) {
        return doAction(VdcActionType.RemoveVmFromPool,
                        new RemoveVmFromPoolParameters(guid),
                        action);
    }

    @Override
    public Response export(Action action) {
        validateParameters(action, "storageDomain.id|name");

        MoveVmParameters params = new MoveVmParameters(guid, getStorageDomainId(action));

        if (action.isSetExclusive() && action.isExclusive()) {
            params.setForceOverride(true);
        }

        if (action.isSetDiscardSnapshots() && action.isDiscardSnapshots()) {
            params.setCopyCollapse(true);
        }

        return doAction(VdcActionType.ExportVm, params, action);
    }

    @Override
    public Response move(Action action) {
        validateParameters(action, "storageDomain.id|name");

        return doAction(VdcActionType.MoveVm,
                        new MoveVmParameters(guid, getStorageDomainId(action)),
                        action);
    }

    protected Guid getStorageDomainId(Action action) {
        if (action.getStorageDomain().isSetId()) {
            return asGuid(action.getStorageDomain().getId());
        } else {
            return lookupStorageDomainIdByName(action.getStorageDomain().getName());
        }
    }

    protected Guid lookupStorageDomainIdByName(String name) {
        return getEntity(storage_domains.class, SearchType.StorageDomain, "Storage: name=" + name).getid();
    }

    @Override
    public Response ticket(Action action) {
        return doAction(VdcActionType.SetVmTicket,
                        new SetVmTicketParameters(guid,
                                                  getTicketValue(action),
                                                  getTicketExpiry(action)),
                        action);
    }

    protected String getTicketValue(Action action) {
        if (!ensureTicket(action).isSetValue()) {
            action.getTicket().setValue(GenerateOTP());
        }
        return action.getTicket().getValue();
    }


    protected int getTicketExpiry(Action action) {
        if (!ensureTicket(action).isSetExpiry()) {
            action.getTicket().setExpiry(DEFAULT_TICKET_EXPIRY);
        }
        return action.getTicket().getExpiry().intValue();
    }

    protected Ticket ensureTicket(Action action) {
        if (!action.isSetTicket()) {
            action.setTicket(new Ticket());
        }
        return action.getTicket();
    }

    protected RunVmOnceParams map(VM vm, RunVmOnceParams params) {
        return getMapper(VM.class, RunVmOnceParams.class).map(vm, params);
    }

    @Override
    protected VM populate(VM model, org.ovirt.engine.core.common.businessentities.VM entity) {
        return parent.addStatistics(model, entity, uriInfo, httpHeaders);
    }

    protected class UpdateParametersProvider implements
            ParametersProvider<VM, org.ovirt.engine.core.common.businessentities.VM> {
        @Override
        public VdcActionParametersBase getParameters(VM incoming,
                org.ovirt.engine.core.common.businessentities.VM entity) {
            VmStatic updated = getMapper(modelType, VmStatic.class).map(incoming,
                    entity.getStaticData());
            return new VmManagementParametersBase(updated);
        }
    }
}
