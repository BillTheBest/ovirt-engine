package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

@XmlType(namespace = "http://service.engine.ovirt.org")
@XmlAccessorType(XmlAccessType.NONE)
public class ValueObjectMap extends ValueObject implements Serializable {
    private static final long serialVersionUID = -8970215546874151379L;

    private ValueObjectPair[] valuePairs = new ValueObjectPair[0];

    public ValueObjectMap() {
    }

    public ValueObjectMap(Map map, boolean mapOfMaps) {
        valuePairs = new ValueObjectPair[map.keySet().size()];
        int i = 0;
        // if the value is also a map construct a ValueObjectMap from the value
        // as well.
        if (mapOfMaps) {
            for (Object key : map.keySet()) {
                Map innerMap = (Map) map.get(key);
                boolean innerMapIsMapOfMaps = false;
                // If map of maps, it is possible the inner map is also a map of maps
                // So the inner ValueObjectMap should be constructed accordingly
                // To determine if the inner map is also a map, the set of entries of the map is retrieved.
                // If there is at least a single entry, it will be checked if this entry has a value which is a map
                // itself
                Set entries = innerMap.entrySet();
                Iterator entriesIterator = entries.iterator();
                if (entriesIterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) entriesIterator.next();
                    if (entry.getValue() instanceof Map) {
                        innerMapIsMapOfMaps = true;
                    }
                }

                valuePairs[i++] = new ValueObjectPair(key, new ValueObjectMap(innerMap, innerMapIsMapOfMaps));
            }
        } else {
            for (Object key : map.keySet()) {
                valuePairs[i++] = new ValueObjectPair(key, map.get(key));
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(valuePairs);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ValueObjectMap other = (ValueObjectMap) obj;
        if (!Arrays.equals(valuePairs, other.valuePairs))
            return false;
        return true;
    }

    @XmlElement
    public ValueObjectPair[] getValuePairs() {
        return valuePairs;
    }

    public void setValuePairs(ValueObjectPair[] valuePairs) {
        if (valuePairs != null) {
            this.valuePairs = valuePairs;
        } else {
            this.valuePairs = new ValueObjectPair[0];
        }
    }

    @Override
    public Map asMap() {
        HashMap hashMap = new HashMap();
        for (ValueObjectPair valuePair : valuePairs) {
            hashMap.put(valuePair.getKey(), valuePair.getValue());
        }
        return hashMap;
    }
}
