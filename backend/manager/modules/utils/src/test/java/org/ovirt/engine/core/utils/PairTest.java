package org.ovirt.engine.core.utils;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class PairTest {

    @Test
    public void testHashCode() {
        Map<String, Pair<Boolean, String>> m = new HashMap<String, Pair<Boolean, String>>();

        Pair<Boolean, String> p1 = new Pair<Boolean, String>(true, "abc");
        Pair<Boolean, String> p2 = new Pair<Boolean, String>(true, "abc");
        m.put("test", p1);

        Assert.assertTrue(m.containsValue(p1));
        Assert.assertTrue(m.containsValue(p2));
    }

    @Test
    public void testEquals() {
        Pair<Boolean, String> p1 = new Pair<Boolean, String>(true, "abc");
        Pair<Boolean, String> p2 = new Pair<Boolean, String>(true, "abc");
        Pair<Boolean, String> p3 = new Pair<Boolean, String>(false, "abc");

        Assert.assertTrue(p1.equals(p2));
        Assert.assertFalse(p1.equals(p3));
    }
}
