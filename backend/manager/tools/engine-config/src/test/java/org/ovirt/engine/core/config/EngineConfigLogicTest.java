package org.ovirt.engine.core.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.config.entity.ConfigKey;
import org.ovirt.engine.core.config.validation.ConfigActionType;

public class EngineConfigLogicTest {

    public static final Logger log = Logger.getLogger(RhevConfigTest.class);
    private EngineConfigCLIParser parser;
    private EngineConfigLogic engineConfigLogic;

    @Before
    public void setUpEngineConfigLogicTest() throws Exception {
        parser = mock(EngineConfigCLIParser.class);
        engineConfigLogic = new EngineConfigLogic(parser);
    }

    @Test
    public void testGetValue() throws Exception {
        String key = "MaxNumberOfHostsInStoragePool";
        log.info("getValue: Testing fetch of " + key);
        ConfigKey configKey = engineConfigLogic.fetchConfigKey(key, null);
        log.info("getValue: got: " + configKey);
        Assert.assertNotNull(configKey.getValue());
    }

    @Test
    public void testListAction() throws Exception {
        setUpTestListAction();
        log.info("Get all config keys (-l or --list)");
        engineConfigLogic.execute();
    }

    @Test
    public void testSetIntValue() throws Exception {
        String key = "VdsRefreshRate";
        String newValue = "15";
        String oldValue = getOldValue(key);

        log.info(key + " old value: " + oldValue);
        log.info("setIntValue: Testing set of " + key);

        engineConfigLogic.persist(key, newValue, "");
        String updatedValue = engineConfigLogic.fetchConfigKey(key, null).getValue();

        log.info(key + " new value: " + updatedValue);
        Assert.assertEquals(Integer.parseInt(updatedValue), Integer.parseInt(newValue));

        // Restoring original value
        engineConfigLogic.persist(key, oldValue, "");
    }

    @Test
    public void testSetStringValue() throws Exception {
        String key = "DomainName";
        String newValue = "EXAMPLE.COM,DOMAIN.COM";
        String oldValue = getOldValue(key);

        log.info(key + " old value: " + oldValue);
        log.info("setStringValue: Testing set of " + key);

        engineConfigLogic.persist(key, newValue, "");
        String updatedValue = engineConfigLogic.fetchConfigKey(key, null).getValue();

        log.info(key + " new value: " + updatedValue);
        Assert.assertEquals(updatedValue, newValue);

        // Restoring original value
        engineConfigLogic.persist(key, oldValue, "");
    }

    @Test
    public void testGetNonExitingKey() throws Exception {
        String key = "NonExistignKeyDB";
        ConfigKey configKey = engineConfigLogic.fetchConfigKey(key, null);
        Assert.assertTrue(configKey == null || configKey.getKey() == null);
    }

    @Test(expected = IllegalAccessException.class)
    public void testSetInvalidIntValue() throws Exception {
        String key = "VdsRefreshRate";
        // An exception should be thrown
        engineConfigLogic.persist(key, "Not A Number", "");
    }

    @Test
    public void testSetEncryptedField() throws Exception {
        // The tool does not support getting passwords therefore it is enough
        // for the test not to throw an exception in order to succeed
        engineConfigLogic.persist("AdUserPassword", "123456");
    }

    private String getOldValue(String key) {
        ConfigKey configKey = engineConfigLogic.fetchConfigKey(key, null);
        return configKey.getValue();
    }

    private void setUpTestListAction() throws Exception {
        when(parser.getConfigAction()).thenReturn(ConfigActionType.ACTION_LIST);
        engineConfigLogic = new EngineConfigLogic(parser);
        EngineConfig.getInstance().setEngineConfigLogic(engineConfigLogic);
    }
}