package org.ovirt.engine.core.config;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.config.validation.ConfigActionType;

public class EngineConfigCLIParserTest {
    private EngineConfigCLIParser parser;

    @Before
    public void setUp() {
        parser = new EngineConfigCLIParser();
    }

    @Test
    public void testParseAllAction() throws Exception {
        parser.parse(new String[] { "-a" });
        assertEquals(ConfigActionType.ACTION_ALL, parser.getConfigAction());
    }

    @Test
    public void testParseListActionWithExtraArguments() throws Exception {
        parser.parse(new String[] { "-l", "b", "c" });
        assertEquals(ConfigActionType.ACTION_LIST, parser.getConfigAction());
    }

    @Test
    public void testParseNoAction() {
        System.out.println("Testing parse args with no action...");
        try {
            parser.parse(new String[] { "-b", "-t", "filename" });
            // An exception should be thrown
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("\n");
    }

    @Test
    public void testParseActionNotFirst() throws Exception {
        System.out.println("Testing parse args with action not first...");
        try {
            parser.parse(new String[] { "-b", "-a", "filename" });
            // An exception should be thrown
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testGetOptionalConfigDir() throws Exception {
        parser.parse(new String[] { "-a", "-c", "dirname" });
        assertEquals("dirname", parser.getAlternateConfigFile());
    }

    @Test
    public void testGetAlternativePropertiesFile() throws Exception {
        parser.parse(new String[] { "-a", "-p", "filename" });
        assertEquals("filename", parser.getAlternatePropertiesFile());
    }

    @Test
    public void testParseGetActionWithKeyInFirstArgument() throws Exception {
        parser.parse(new String[] { "--get=keyToGet" });
        assertEquals(ConfigActionType.ACTION_GET, parser.getConfigAction());
        assertEquals("keyToGet", parser.getKey());
    }

    @Test
    public void testParseGetActionWithKeyInSecondArgument() throws Exception {
        parser.parse(new String[] { "-g", "keyToGet" });
        assertEquals(ConfigActionType.ACTION_GET, parser.getConfigAction());
        assertEquals("keyToGet", parser.getKey());
    }

    @Test
    public void testParseSetActionWithValidArguments() throws Exception {
        parser.parse(new String[] { "-s", "keyToSet=valueToSet" });
        assertEquals(ConfigActionType.ACTION_SET, parser.getConfigAction());
        assertEquals("keyToSet", parser.getKey());
        assertEquals("valueToSet", parser.getValue());
    }
}
