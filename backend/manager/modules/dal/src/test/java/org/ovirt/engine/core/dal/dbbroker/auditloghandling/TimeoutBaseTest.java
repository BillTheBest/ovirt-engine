package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class TimeoutBaseTest {

    @Test
    public void timeoutDefault() {
        final TestTimeoutBase t = new TestTimeoutBase();
        assertFalse(t.getUseTimout());
    }

    @Test
    public void timeoutTrue() {
        final TestTimeoutBase t = new TestTimeoutBase();
        t.setUseTimout(true);
        assertTrue(t.getUseTimout());
    }

    @Test
    public void timeoutFalse() {
        final TestTimeoutBase t = new TestTimeoutBase();
        t.setUseTimout(false);
        assertFalse(t.getUseTimout());
    }

    @Test
    public void defaultEndtime() {
        final long epochOffset = 0;
        final TestTimeoutBase t = new TestTimeoutBase();
        assertEquals(epochOffset, t.getEndTime().getTime());
    }

    @Test
    public void endTime() {
        final TestTimeoutBase t = new TestTimeoutBase();
        final Date d = Calendar.getInstance().getTime();
        t.setEndTime(d);
        assertEquals(d, t.getEndTime());
    }

    @Test
    public void timeoutObjectNull() {
        final TestTimeoutBase t = new TestTimeoutBase();
        final String s = null;
        t.setTimeoutObjectId(s);
        assertEquals(t.getTimeoutObjectId(), s);
    }

    @Test
    public void timeoutObject() {
        final TestTimeoutBase t = new TestTimeoutBase();
        final String s = "testtimeout";
        t.setTimeoutObjectId(s);
        assertEquals(t.getTimeoutObjectId(), s);
    }

    @Test
    public void legalWithoutTimeoutSet() {
        final TestTimeoutBase t = new TestTimeoutBase();
        final boolean result = t.getLegal();
        assertTrue(result);
    }

    @Test
    public void legalFirstTime() {
        final TestTimeoutBase t = new TestTimeoutBase();
        t.setUseTimout(true);
        final boolean result = t.getLegal();
        assertTrue(result);
    }

    @Test
    public void legalNullObjectId() {
        final TestTimeoutBase t = new TestTimeoutBase();
        t.setUseTimout(true);
        t.setTimeoutObjectId(null);
        final boolean result = t.getLegal();
        assertTrue(result);
    }

    @Test
    public void legalTimedOut() {
        final TestTimeoutBase t = new TestTimeoutBase();
        t.setUseTimout(true);
        long c = System.currentTimeMillis();
        c -= 1;
        final Date d = new Date(c);
        t.setEndTime(d);
        final String s = "timeout";
        t.setTimeoutObjectId(s);
        // get it into the hashtable
        t.getLegal();
        final boolean result = t.getLegal();
        assertTrue(result);
    }

    @Test
    public void legalNotTimedOut() {
        final TestTimeoutBase t = new TestTimeoutBase();
        t.setUseTimout(true);
        final String s = "timeout";
        t.setTimeoutObjectId(s);
        long c = System.currentTimeMillis();
        c += 5000;
        final Date d = new Date(c);
        t.setEndTime(d);
        // get it into the hashtable
        t.getLegal();
        final boolean result = t.getLegal();
        assertFalse(result);
    }

    public class TestTimeoutBase extends TimeoutBase {

        @Override
        protected String getKey() {
            return "testkey";
        }

    }

}
