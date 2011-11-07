package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Templates;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VMs;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.StorageDomainContentsResource;
import org.ovirt.engine.api.resource.FilesResource;
import org.ovirt.engine.api.resource.StorageDomainResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExtendSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.api.restapi.util.StorageDomainHelper;

import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainsResource.SUB_COLLECTIONS;

public class BackendStorageDomainResource extends
        AbstractBackendSubResource<StorageDomain, storage_domains> implements StorageDomainResource {

    private BackendStorageDomainsResource parent;

    public BackendStorageDomainResource(String id, BackendStorageDomainsResource parent) {
        super(id, StorageDomain.class, storage_domains.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    BackendStorageDomainsResource getParent() {
        return parent;
    }

    @Override
    public StorageDomain get() {
        StorageDomain storageDomain = performGet(VdcQueryType.GetStorageDomainById, new StorageDomainQueryParametersBase(guid));
        storageDomain.setStatus(null); //status is only relevant in the context of a data-center, so we reset it to 'null'.
        return addLinks(storageDomain, getLinksToExclude(storageDomain));
    }

    @Override
    public StorageDomain update(StorageDomain incoming) {
        QueryIdResolver storageDomainResolver = new QueryIdResolver(VdcQueryType.GetStorageDomainById, StorageDomainQueryParametersBase.class);
        storage_domains entity = getEntity(storageDomainResolver, true);
        StorageDomain model = map(entity, new StorageDomain());
        StorageType storageType = entity.getstorage_type();
        if (storageType != null) {
            switch (storageType) {
            case ISCSI:
            case FCP:
                extendStorageDomain(incoming, model, storageType);
                break;
            default:
                break;
            }
        }

        return addLinks(performUpdate(incoming,
                                      entity,
                                      model,
                                      storageDomainResolver,
                                      VdcActionType.UpdateStorageDomain,
                                      new UpdateParametersProvider()),
                        new String[]{"templates", "vms"});
    }

    @Override
    public FilesResource getFilesResource() {
        return inject(new BackendFilesResource(id));
    }

    public static synchronized boolean isIsoDomain(StorageDomain storageDomain) {
        StorageDomainType type = StorageDomainType.fromValue(storageDomain.getType());
        return type != null && type == StorageDomainType.ISO ? true : false;
    }

    public static synchronized boolean isIsoDomain(storage_domains storageDomain) {
        org.ovirt.engine.core.common.businessentities.StorageDomainType type =  storageDomain.getstorage_domain_type() ;
        return type != null && type == org.ovirt.engine.core.common.businessentities.StorageDomainType.ISO ? true : false;
    }

    public static synchronized boolean isExportDomain(StorageDomain storageDomain) {
        StorageDomainType type = StorageDomainType.fromValue(storageDomain.getType());
        return type != null && type == StorageDomainType.EXPORT ? true : false;
    }

    public static synchronized String[] getLinksToExclude(StorageDomain storageDomain) {
        return isIsoDomain(storageDomain) ? new String[]{"templates", "vms"}
                                            :
                                            isExportDomain(storageDomain) ? new String[]{"files"}
                                                                            :
                                                                            new String[]{"templates", "vms", "files"};
    }

    /**
     * if user added new LUNs - extend the storage domain.
     * @param incoming
     */
    private void extendStorageDomain(StorageDomain incoming, StorageDomain storageDomain, StorageType storageType) {
        List<LogicalUnit> existingLuns = storageDomain.getStorage().getVolumeGroup().getLogicalUnits();
        List<LogicalUnit> incomingLuns = getIncomingLuns(incoming.getStorage());
        List<LogicalUnit> newLuns = findNewLuns(existingLuns, incomingLuns);
        if (!newLuns.isEmpty()) {
            //If there are new LUNs, this means the user wants to extend the storage domain.
            //Supplying a host is necessary for this operation, but not for regular update
            //of storage-domain. So only now is the time for this validation.
            validateParameters(incoming, "host.id|name");
            addLunsToStorageDomain(incoming, storageType, newLuns);
            //Remove the new LUNs from the incoming LUns before update, since they have already been dealt with.
            incomingLuns.removeAll(newLuns);
        }
    }

    private void addLunsToStorageDomain(StorageDomain incoming, StorageType storageType, List<LogicalUnit> newLuns) {
        for (LogicalUnit lun : newLuns) {
            if (lun.isSetAddress() && lun.isSetTarget()) {
                storage_server_connections connection = StorageDomainHelper.getConnection(storageType, lun.getAddress(), lun.getTarget(), lun.getUsername(), lun.getPassword(), lun.getPort());
                performAction(VdcActionType.ConnectStorageToVds,
                        new StorageServerConnectionParametersBase(connection, getHostId(incoming)));
            }
        }

        refreshVDSM(incoming);

        ExtendSANStorageDomainParameters params = createParameters(guid, newLuns);

        performAction(VdcActionType.ExtendSANStorageDomain, params);
    }

    //This is a work-around for a VDSM bug. The call to GetDeviceList causes a refresh in the VDSM, without which the creation will fail.
    private void refreshVDSM(StorageDomain incoming) {
        getEntity(Object.class, VdcQueryType.GetDeviceList, new GetDeviceListQueryParameters(getHostId(incoming), StorageType.ISCSI), "");
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             StorageDomain.class,
                                                             VdcObjectType.Storage));
    }

    @Override
    protected StorageDomain map(storage_domains entity, StorageDomain template) {
        return parent.map(entity, template);
    }

    private List<LogicalUnit> getIncomingLuns(Storage storage) {
        //user may pass the LUNs under Storage, or Storage-->VolumeGroup; both are supported.
        return !storage.getLogicalUnits().isEmpty() ? storage.getLogicalUnits() : storage.getVolumeGroup().getLogicalUnits();
    }

    private Guid getHostId(StorageDomain storageDomain) {
        // presence of host ID or name already validated
        return storageDomain.getHost().isSetId()
               ? new Guid(storageDomain.getHost().getId())
               : storageDomain.getHost().isSetName()
                 ? getEntity(VDS.class,
                             SearchType.VDS,
                             "Hosts: name=" + storageDomain.getHost().getName()).getvds_id()
                 : null;
    }

    private ExtendSANStorageDomainParameters createParameters(Guid storageDomainId, List<LogicalUnit> newLuns) {
        ExtendSANStorageDomainParameters params = new ExtendSANStorageDomainParameters();
        params.setStorageDomainId(storageDomainId);
        ArrayList<String> lunIds = new ArrayList<String>();
        for (LogicalUnit newLun : newLuns) {
            lunIds.add(newLun.getId());
        }
        params.setLunIds(lunIds);
        return params;
    }

    private List<LogicalUnit> findNewLuns(List<LogicalUnit> existingLuns, List<LogicalUnit> incomingLuns) {
        List<LogicalUnit> newLuns = new LinkedList<LogicalUnit>();
        for (LogicalUnit incomingLun : incomingLuns) {
            boolean found = false;
            for (LogicalUnit existingLun : existingLuns) {
                if (lunsEqual(incomingLun, existingLun)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newLuns.add(incomingLun);
            }
        }
        return newLuns;
    }

    private boolean lunsEqual(LogicalUnit firstLun, LogicalUnit secondLun) {
        return firstLun.getId().equals(secondLun.getId());
    }

    protected class UpdateParametersProvider implements
            ParametersProvider<StorageDomain, storage_domains> {
        @Override
        public VdcActionParametersBase getParameters(StorageDomain incoming, storage_domains entity) {
            storage_domain_static updated = getMapper(modelType, storage_domain_static.class).map(
                    incoming, entity.getStorageStaticData());
            return new StorageDomainManagementParameter(updated);
        }
    }

    @Override
    public StorageDomainContentsResource<Templates, Template> getStorageDomainTemplatesResource() {
        return inject(new BackendStorageDomainTemplatesResource(guid));
    }

    @Override
    public StorageDomainContentsResource<VMs, VM> getStorageDomainVmsResource() {
        return inject(new BackendStorageDomainVmsResource(guid));
    }
}
