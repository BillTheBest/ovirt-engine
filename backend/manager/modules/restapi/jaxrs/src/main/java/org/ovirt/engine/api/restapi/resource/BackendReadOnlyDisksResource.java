package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.resource.ReadOnlyDevicesResource;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendReadOnlyDisksResource
        extends AbstractBackendReadOnlyDevicesResource<Disk, Disks, DiskImage>
        implements ReadOnlyDevicesResource<Disk, Disks> {

    public BackendReadOnlyDisksResource(Guid parentId, VdcQueryType queryType, VdcQueryParametersBase queryParams) {
        super(Disk.class, Disks.class, DiskImage.class, parentId, queryType, queryParams);
    }

    @Override
    public Disks list() {
        GetStorageDomainsByVmTemplateIdQueryParameters queryParams = new GetStorageDomainsByVmTemplateIdQueryParameters(parentId);
        List<storage_domains> storageDomains = getBackendCollection(storage_domains.class, VdcQueryType.GetStorageDomainsByVmTemplateId, queryParams);
        Disks disks = mapCollection(getBackendCollection(queryType, queryParams));
        for (Disk disk : disks.getDisks()) {
            if (disk.isSetStorageDomains()) {
                disk.getStorageDomains().getStorageDomains().clear();
            } else {
                disk.setStorageDomains(new StorageDomains());
            }
            for (storage_domains sd : storageDomains) {
                StorageDomain storageDomain = new StorageDomain();
                storageDomain.setId(sd.getid().toString());
                disk.getStorageDomains().getStorageDomains().add(storageDomain);
            }
        }
        return disks;
    }

    protected boolean matchEntity(DiskImage entity, Guid id) {
        return id != null && id.equals(entity.getId());
    }
}
