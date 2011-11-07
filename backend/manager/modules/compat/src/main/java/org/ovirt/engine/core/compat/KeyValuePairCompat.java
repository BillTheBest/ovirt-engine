package org.ovirt.engine.core.compat;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "KeyValuePairCompat")
public class KeyValuePairCompat<K, V> implements java.util.Map.Entry<K, V>, Serializable {

    private static final long serialVersionUID = 3550666497489591122L;

    private K key;
    private V value;

    public KeyValuePairCompat() {
    }

    public KeyValuePairCompat(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @XmlElement
    @Override
    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    @XmlElement
    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V oldV = value;
        this.value = value;
        return oldV;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KeyValuePairCompat other = (KeyValuePairCompat) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
