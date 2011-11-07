package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "NonOperationalReason")
public enum NonOperationalReason {

    NONE(0),
    GENERAL(1),
    CPU_TYPE_INCOMPATIBLE_WITH_CLUSTER(2),
    STORAGE_DOMAIN_UNREACHABLE(3),
    NETWORK_UNREACHABLE(4),
    VERSION_INCOMPATIBLE_WITH_CLUSTER(5),
    KVM_NOT_RUNNING(6),
    TIMEOUT_RECOVERING_FROM_CRASH(7);

    private final int value;

    private static final Map<Integer, NonOperationalReason> valueMap = new HashMap<Integer, NonOperationalReason>(
            values().length);

    static {
        for (NonOperationalReason reason : values()) {
            valueMap.put(reason.value, reason);
        }
    }

    private NonOperationalReason(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static NonOperationalReason forValue(int value) {
        return valueMap.get(value);
    }
}
