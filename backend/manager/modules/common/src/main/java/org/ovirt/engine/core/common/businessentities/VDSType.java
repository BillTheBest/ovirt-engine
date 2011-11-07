package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "VDSType")
public enum VDSType {
    VDS(0),
    PowerClient(1),
    oVirtNode(2);

    private int intValue;
    private static java.util.HashMap<Integer, VDSType> mappings = new HashMap<Integer, VDSType>();

    static {
        for (VDSType type : values()) {
            mappings.put(type.getValue(), type);
        }
    }

    private VDSType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VDSType forValue(int value) {
        return mappings.get(value);
    }
}
