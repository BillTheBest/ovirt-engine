package org.ovirt.engine.api.restapi.types;


import org.junit.Test;

import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.restapi.model.StorageFormat;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;

public class StorageDomainMapperTest extends
        AbstractInvertibleMappingTest<StorageDomain, storage_domain_static, storage_domains> {

    protected StorageDomainMapperTest() {
        super(StorageDomain.class, storage_domain_static.class, storage_domains.class);
    }

    @Override
    protected StorageDomain postPopulate(StorageDomain model) {
        model.setType(MappingTestHelper.shuffle(StorageDomainType.class).value());
        model.getStorage().setType(MappingTestHelper.shuffle(StorageType.class).value());
        model.setStorageFormat(MappingTestHelper.shuffle(StorageFormat.class).value());
        return model;
    }

    @Override
    protected storage_domains getInverse(storage_domain_static to) {
        storage_domains inverse = new storage_domains();
        inverse.setid(to.getId());
        inverse.setstorage_name(to.getstorage_name());
        inverse.setstorage_domain_type(to.getstorage_domain_type());
        inverse.setstorage_type(to.getstorage_type());
        inverse.setStorageFormat(to.getStorageFormat());
        return inverse;
    }

    @Override
    protected void verify(StorageDomain model, StorageDomain transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        // REVIST No descriptions for storage domains
        // assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getType(), transform.getType());
        assertNotNull(transform.getStorage());
        assertEquals(model.getStorage().getType(), transform.getStorage().getType());
        assertEquals(model.getStorageFormat(), transform.getStorageFormat());
    }

    @Test
    public void testMemory() {
        storage_domains entity = new storage_domains();
        entity.setavailable_disk_size(3);
        entity.setused_disk_size(4);
        entity.setcommitted_disk_size(5);
        StorageDomain model = StorageDomainMapper.map(entity, (StorageDomain)null);
        assertEquals(model.getAvailable(), new Long(3221225472L));
        assertEquals(model.getUsed(), new Long(4294967296L));
        assertEquals(model.getCommitted(), new Long(5368709120L));
    }
}
