package org.ovirt.engine.core.utils.collections;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class CopyOnAccessMapTest {

    private static final int VALUE2_LENGTH = 50;
    private static final int VALUE2_WIDTH = 25;
    private static final int VALUE1_LENGTH = 10;
    private static final int VALUE1_WIDTH = 5;

    public static class MyKey implements Serializable {

        private static final long serialVersionUID = 3675790292994230887L;
        private String name;
        private int age;

        public MyKey() {
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + age;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
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
            MyKey other = (MyKey) obj;
            if (age != other.age)
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }

        public MyKey(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    public static class MyValue implements Serializable {

        private static final long serialVersionUID = 2509582906973648615L;
        private int width;
        private int length;

        public MyValue() {
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + length;
            result = prime * result + width;
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
            MyValue other = (MyValue) obj;
            if (length != other.length)
                return false;
            if (width != other.width)
                return false;
            return true;
        }

        public MyValue(int width, int length) {
            super();
            this.width = width;
            this.length = length;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }

    private CopyOnAccessMap<MyKey, MyValue> testedMap = null;
    private MyKey key1;
    private MyValue value1;
    private MyKey key2;
    private MyValue value2;
    private Map<MyKey, MyValue> innerMap = null;

    @Before
    public void setup() {
        innerMap = new HashMap<MyKey, MyValue>();
        testedMap = new CopyOnAccessMap<MyKey, MyValue>(innerMap);
        key1 = new MyKey("RHEL", 0);
        value1 = new MyValue(VALUE1_WIDTH, VALUE1_LENGTH);
        key2 = new MyKey("Fedora", 0);
        value2 = new MyValue(VALUE2_WIDTH, VALUE2_LENGTH);
    }

    /**
     * Changes the data of value1 after put is used to insert it to the map. If this was a regular HashMap, the data of
     * the value held by the map would have also changed.
     */
    @Test
    public void testPut() {
        testedMap.put(key1, value1);
        value1.setWidth(10);
        assertSingleMapEntryUnchanged();
    }

    private void assertSingleMapEntryUnchanged() {
        MyValue newValue = innerMap.get(key1);
        assertEquals(VALUE1_WIDTH, newValue.getWidth());
        assertEquals(VALUE1_LENGTH, newValue.getLength());
    }

    /**
     * Getting data from map and changing data on the retrieved object. If this was a hash map - the change would effect
     * the referenced object held by the map
     */
    @Test
    public void testGet() {
        innerMap.put(key1, value1);
        MyValue newValue = testedMap.get(key1);
        newValue.setWidth(10); // Changing the data
        assertSingleMapEntryUnchanged();
    }

    /**
     * Changes the data of value1 and value2, after putAll is used to put them in the map If this was a regular HashMap,
     * the data of the value held by the map would change
     */

    @Test
    public void testPutAll() {
        Map<MyKey, MyValue> map = new HashMap<MyKey, MyValue>();
        map.put(key1, value1);
        map.put(key2, value2);
        testedMap.putAll(map);
        value1.setLength(10);
        value2.setLength(30);
        assertEntireMapContentsUnchanged();
    }

    /**
     * Tests activating the "values" method, changing some of the values, and activating the method again. If this was a
     * regular hashmap, the 2nd activating of the values method would return values with altered data the map were
     * effected
     */
    @Test
    public void testValues() {
        fillMapValues();
        Collection<MyValue> values = testedMap.values();
        for (MyValue val : values) {
            val.setLength(val.getLength() + 5);
        }
        assertEntireMapContentsUnchanged();
    }

    @Test
    public void testKeySet() {
        fillMapValues();
        Set<MyKey> keySet = testedMap.keySet();
        // If this was a regular map - updating the keys data would have effected them
        for (MyKey key : keySet) {
            key.setAge(key.getAge() + 5);
        }
        assertEntireMapContentsUnchanged();
    }

    /**
     * Tests activating "entrySet" method, changing some values and keys and activating this method again. If this was a
     * regular map, the keys and values obtained from the 2nd entrySet would contain the altered data.
     */
    @Test
    public void testEntrySet() {
        fillMapValues();
        Set<Entry<MyKey, MyValue>> entrySet = testedMap.entrySet();
        for (Map.Entry<MyKey, MyValue> entry : entrySet) {
            MyKey key = entry.getKey();
            key.setAge(key.getAge() + 5);
            MyValue value = entry.getValue();
            value.setLength(value.getLength() + 5);
        }
        assertEntireMapContentsUnchanged();
    }

    public void assertEntireMapContentsUnchanged() {
        MyValue newVal1 = innerMap.get(key1);
        MyValue newVal2 = innerMap.get(key2);
        assertEquals(VALUE1_WIDTH, newVal1.getWidth());
        assertEquals(VALUE1_LENGTH, newVal1.getLength());
        assertEquals(VALUE2_WIDTH, newVal2.getWidth());
        assertEquals(VALUE2_LENGTH, newVal2.getLength());

    }


    private void fillMapValues() {
        innerMap.put(key1, value1);
        innerMap.put(key2, value2);
    }

}
