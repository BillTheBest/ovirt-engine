package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendDataCenterResource.getStoragePools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendStorageDomainContentsResource<C extends BaseResources,
                                                                   R extends BaseResource,
                                                                   Q extends IVdcQueryable>
    extends AbstractBackendCollectionResource<R, Q> {

    protected Guid storageDomainId;

    public AbstractBackendStorageDomainContentsResource(Guid storageDomainId,
                                                        Class<R> modelType,
                                                        Class<Q> entityType) {
        super(modelType, entityType);
        this.storageDomainId = storageDomainId;
    }

    protected Guid getDataCenterId(Action action) {
        return getStoragePoolId(action);
    }

    public Guid getStoragePoolId(Action action) {
        if(action.getStorageDomain().isSetId()){
            return getDataCenterId(Guid.createGuidFromString(action.getStorageDomain().getId()));
        } else {
            return getDataCenterId(lookupStorageDomainIdByName(action.getStorageDomain().getName()));
        }
    }

    protected Guid lookupStorageDomainIdByName(String name) {
        return getEntity(storage_domains.class, SearchType.StorageDomain, "Storage: name=" + name).getid();
    }

    public Guid getDataCenterId(Guid storageDomainId) {
        List<storage_pool> storagepools = getStoragePools(storageDomainId, this);
        return storagepools.size() > 0 ?
                storagepools.get(0).getId()
                :
                null;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public storage_domains getStorageDomain() {
        return getEntity(storage_domains.class,
                         VdcQueryType.GetStorageDomainById,
                         new StorageDomainQueryParametersBase(storageDomainId),
                         storageDomainId.toString());
    }

    public StorageDomainType getStorageDomainType() {
        return getStorageDomain().getstorage_domain_type();
    }

    public StorageDomain getStorageDomainModel() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId.toString());
        return storageDomain;
    }

    public List<R> getCollection() {
        return getCollection(getStorageDomainType());
    }

    public List<R> getCollection(StorageDomainType storageDomainType) {
        Collection<Q> entities = new ArrayList<Q>();

        switch (storageDomainType) {
        case Data:
        case Master:
            break;
        case ImportExport:
            entities = getEntitiesFromExportDomain();
            break;
        case ISO:
        case Unknown:
        }

        List<R> collection = new ArrayList<R>();
        for (Q entity : entities) {
            collection.add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    protected abstract Collection<Q> getEntitiesFromDataDomain();
    protected abstract Collection<Q> getEntitiesFromExportDomain();
}
