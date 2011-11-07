package org.ovirt.engine.core.dal.dbbroker.generic;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.OptionBehaviour;
import org.ovirt.engine.core.common.config.OptionBehaviourAttribute;
import org.ovirt.engine.core.common.config.TypeConverterAttribute;

public class DBConfigUtilsTest {
    private DBConfigUtils config;

    @Before
    public void setup() {
        config = new DBConfigUtils(false);
    }

    @Test
    public void testDefaultValues() {
        ConfigValues[] values = ConfigValues.values();

        for (ConfigValues curConfig : values) {
            if (curConfig == ConfigValues.Invalid)
                continue;

            Field configField = null;
            try {
                configField = ConfigValues.class.getField(curConfig.name());
            } catch (Exception e) {
                Assert.fail("Failed to look up" + curConfig.name());
                e.printStackTrace();
            }

            OptionBehaviourAttribute behaviourAttr = configField.getAnnotation(OptionBehaviourAttribute.class);
            if (behaviourAttr != null
                    && (behaviourAttr.behaviour() == OptionBehaviour.Password ||
                            behaviourAttr.behaviour() == OptionBehaviour.DomainsPasswordMap)) {
                continue; // no cert available for password decrypt
            }

            TypeConverterAttribute typeAttr = configField.getAnnotation(TypeConverterAttribute.class);
            Class<?> c = typeAttr.value();

            Object obj = config.GetValue(curConfig, Config.DefaultConfigurationVersion);

            Assert.assertTrue("null return for " + curConfig.name(), obj != null);
            Assert.assertTrue(
                    curConfig.name() + " is a " + obj.getClass().getName() + " but should be a " + c.getName(),
                    c.isInstance(obj));
        }
    }
}
