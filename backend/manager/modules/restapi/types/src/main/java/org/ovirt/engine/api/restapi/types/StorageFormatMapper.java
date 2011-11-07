package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.restapi.model.StorageFormat;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;

public class StorageFormatMapper {

    @Mapping(from = StorageFormatType.class, to = StorageFormat.class)
    public static StorageFormat map(StorageFormatType model, StorageFormat template) {
        if (model==null) {
            return null;
        }
        switch (model) {
        case V1:
            return StorageFormat.V1;
        case V2:
            return StorageFormat.V2;
        default:
            assert(false) : "unknown storage-format value: " + model.toString();
            return null;
        }
    }

    @Mapping(from = StorageFormat.class, to = StorageFormatType.class)
    public static StorageFormatType map(StorageFormat model, StorageFormatType template) {
        if (model==null) {
            return null;
        }
        switch (model) {
        case V1:
            return StorageFormatType.V1;
        case V2:
            return StorageFormatType.V2;
        default:
            assert(false) : "unknown storage-format value: " + model.toString();
            return null;
        }
    }
}
