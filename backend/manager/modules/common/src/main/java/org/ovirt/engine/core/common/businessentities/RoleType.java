package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RoleType")
public enum RoleType {

    ADMIN(1),
    USER(2);

    private int id;
    private static Map<Integer, RoleType> map = new HashMap<Integer, RoleType>(RoleType.values().length);

    static {
        for (RoleType t : RoleType.values()) {
            map.put(t.id, t);
        }
    }

    private RoleType(Integer val) {
        id = val;
    }

    public int getId() {
        return id;
    }

    public static RoleType getById(Integer id) {
        return map.get(id);
    }

}
