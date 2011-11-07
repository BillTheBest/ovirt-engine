package org.ovirt.engine.core.utils.ovf;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmStatic;

public class VMStaticOvfLogHandler extends OvfLogEventHandler<VmStatic> {

    private static HashMap<String, TypeConverter> typeConvertersMap = new HashMap<String, TypeConverter>();

    static {
        typeConvertersMap.put("migrationSupport", new MigrationSupportConverter());
    }

    public VMStaticOvfLogHandler(VmStatic entity) {
        super(entity);
    }

    @Override
    protected Map<String, TypeConverter> getTypeConvertersMap() {
        return typeConvertersMap;
    }

}
