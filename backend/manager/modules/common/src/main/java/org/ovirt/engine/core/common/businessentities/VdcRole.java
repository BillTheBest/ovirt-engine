package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlType;

//using VdcDAL.AdBroker;

@XmlType(name = "VdcRole")
public enum VdcRole {
    None(-1),
    User(0),
    Admin(1),
    PowerUser(2);

    private int intValue;
    private static java.util.HashMap<Integer, VdcRole> mappings = new HashMap<Integer, VdcRole>();

    static {
        for (VdcRole vdcRole : values()) {
            mappings.put(vdcRole.getValue(), vdcRole);
        }
    }

    private VdcRole(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static VdcRole forValue(int value) {
        return mappings.get(value);
    }
}
