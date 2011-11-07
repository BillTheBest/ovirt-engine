package org.ovirt.engine.core.config.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.config.EngineConfig;
import org.ovirt.engine.core.config.entity.helper.StringValueHelper;
import org.ovirt.engine.core.config.entity.helper.ValueHelper;

public class ConfigKeyFactory {

    private HierarchicalConfiguration keysConfig;
    private Map<String, String> alternateKeysMap;
    private static ConfigKeyFactory instance;

    static {
        instance = new ConfigKeyFactory();
    }

    private ConfigKeyFactory() {
    }

    public static void init(HierarchicalConfiguration keysConfig, Map<String, String> alternateKeysMap) {
        instance.keysConfig = keysConfig;
        instance.alternateKeysMap = alternateKeysMap;
    }

    public static ConfigKeyFactory getInstance() {
        return instance;
    }

    public ConfigKey generateByPropertiesKey(String key) {
        SubnodeConfiguration configurationAt = null;
        try {
            if (Character.isLetter(key.charAt(0))) {
                configurationAt = keysConfig.configurationAt(key);
            }
        } catch (IllegalArgumentException e) {
            // Can't find a key. maybe its an alternate key.
        }
        if (configurationAt == null || configurationAt.isEmpty()) {
            key = alternateKeysMap.get(key);
            configurationAt = keysConfig.configurationAt(key);
        }

        String type = configurationAt.getString("type");
        if (StringUtils.isBlank(type)) {
            type = "String";
        }
        String[] validValues = configurationAt.getStringArray("validValues");
        String description = configurationAt.getString("description");
        String alternateKey = keysConfig.getString("/" + key + "/" + "alternateKey");
        ConfigKey configKey = new ConfigKey(type, description, alternateKey, key, "", validValues, "", getHelperByType(type));
        return configKey;
    }

    public ConfigKey generateBlankConfigKey(String keyName,String keyType) {
        return new ConfigKey(keyType, "", "", keyName, "", null, "", getHelperByType(keyType));
    }

    private ValueHelper getHelperByType(String type) {
        ValueHelper valueHelper;
        try {
            if (type == null) {
                type = "String";
            }
            Class<?> cls = Class.forName("org.ovirt.engine.core.config.entity.helper." + type + "ValueHelper");
            valueHelper = (ValueHelper) cls.newInstance();
        } catch (Exception e) {
            // failed finding a helper for this type. Setting default string type
            Logger.getLogger(EngineConfig.class).debug("Unable to find " + type + " type. Using default string type.");
            valueHelper = new StringValueHelper();
        }
        return valueHelper;
    }

    public ConfigKey copyOf(ConfigKey key, String value, String version) {
        return new ConfigKey(
                             key.getType(),
                             key.getDescription(),
                             key.getAlternateKeys(),
                             key.getKey(),
                             value,
                             key.getValidValues().toArray(new String[0]),
                             version,
                             key.getValueHelper());
    }

    /**
     * Create a ConfigKey from a ResultSet object. <b>Note</b>: Some fields are not represented by the DB, such as
     * decription and type.<br>
     *
     * @TODO Consider refactoring the entity to be composed out of a real value-object which will represent the db
     *      entity and a view-object which will represent the user interaction (view) layer.
     * @TODO move "option_name" and other column indexes to Enum values.
     *
     *
     * @param resultSet
     * @return
     * @throws SQLException
     */
    public ConfigKey fromResultSet(ResultSet resultSet) throws SQLException {
        ConfigKey configKey = generateByPropertiesKey(resultSet.getString("option_name"));
        configKey.unsafeSetValue(resultSet.getString("option_value"));
        configKey.setVersion(resultSet.getString("version"));
        return configKey;
    }
}
