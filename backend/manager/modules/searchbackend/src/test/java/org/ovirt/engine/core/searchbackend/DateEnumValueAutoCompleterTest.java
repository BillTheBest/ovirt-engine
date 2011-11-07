package org.ovirt.engine.core.searchbackend;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class DateEnumValueAutoCompleterTest extends TestCase {

    public void testValues() {
        DateEnumValueAutoCompleter comp = new DateEnumValueAutoCompleter(Jedi.class);
        List<String> comps = Arrays.asList(comp.getCompletion(" "));
        System.out.println(comps);
        assertTrue("Monday", comps.contains("Monday") || comps.contains("Tuesday"));
        assertTrue("mace", comps.contains("mace"));
    }

    public void testConvertFieldEnumValueToActualValue() {
        DateEnumValueAutoCompleter comp = new DateEnumValueAutoCompleter(Jedi.class);
        // Dates should return dates
        String test = "01/20/1972";
        String expected = test;
        assertEquals(test, expected, comp.convertFieldEnumValueToActualValue(test));

        // Days of the week should not change the value
        test = "Monday";
        expected = test;
        assertEquals(test, expected, comp.convertFieldEnumValueToActualValue(test));

        // Check Caps
        test = "mOnday";
        expected = "Monday";
        assertEquals(test, expected, comp.convertFieldEnumValueToActualValue(test));

        // Check the other enum
        test = "MACE";
        expected = "4";
        assertEquals(test, expected, comp.convertFieldEnumValueToActualValue(test));
    }
}
