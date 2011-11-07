package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import org.junit.Test;

import org.ovirt.engine.core.common.businessentities.StorageDomainOwnerType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.compat.Guid;

public class ObjectStreamSerializerTest {

    @Test
    public void testSerialize() {
        try {
            ObjectStreamSerializer serializer = new ObjectStreamSerializer();
            storage_pool_iso_map isoMap = new storage_pool_iso_map();
            isoMap.setstorage_id(Guid.NewGuid());
            isoMap.setstorage_pool_id(Guid.NewGuid());
            isoMap.setstatus(StorageDomainStatus.Active);
            isoMap.setowner(StorageDomainOwnerType.Unknown);
            byte[] bytes = (byte[]) serializer.serialize(isoMap);
            storage_pool_iso_map readEntity =
                    (storage_pool_iso_map) new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
            assertEquals(isoMap, readEntity);
        } catch (Exception ex) {
            assertTrue(ex.getMessage(), false);
        }

    }
}
