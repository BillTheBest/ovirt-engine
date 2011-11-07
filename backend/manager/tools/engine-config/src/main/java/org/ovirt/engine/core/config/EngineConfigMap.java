package org.ovirt.engine.core.config;

import org.ovirt.engine.core.config.validation.ConfigActionType;

/**
 * The <code>EngineConfigMap</code> is a structure that holds the relevant values for executing the EngineConfig tool.
 */
public class EngineConfigMap {

    private String version;
    private ConfigActionType configAction;
    private String key;
    private String value;
    private String alternateConfigFile = null;
    private String alternatePropertiesFile = null;

    public EngineConfigMap() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ConfigActionType getConfigAction() {
        return configAction;
    }

    public void setConfigAction(ConfigActionType configAction) {
        this.configAction = configAction;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAlternateConfigFile() {
        return alternateConfigFile;
    }

    public void setAlternateConfigFile(String alternateConfigFile) {
        this.alternateConfigFile = alternateConfigFile;
    }

    public String getAlternatePropertiesFile() {
        return alternatePropertiesFile;
    }

    public void setAlternatePropertiesFile(String alternatePropertiesFile) {
        this.alternatePropertiesFile = alternatePropertiesFile;
    }

    @Override
    public String toString()
    {
        final String SEPARATOR = ", ";

        StringBuffer retValue = new StringBuffer();

        retValue.append("( ")
                .append("version = ").append(this.version).append(SEPARATOR)
                .append("configAction = ").append(this.configAction).append(SEPARATOR)
                .append("key = ").append(this.key).append(SEPARATOR)
                .append("value = ").append(this.value).append(SEPARATOR)
                .append("alternateConfigFile = ").append(this.alternateConfigFile).append(SEPARATOR)
                .append("alternatePropertiesFile = ").append(this.alternatePropertiesFile).append(" )");

        return retValue.toString();
    }
}