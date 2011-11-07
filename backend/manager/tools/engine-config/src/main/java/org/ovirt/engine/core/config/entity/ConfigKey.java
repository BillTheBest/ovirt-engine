package org.ovirt.engine.core.config.entity;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import org.ovirt.engine.core.config.entity.helper.ValueHelper;

public class ConfigKey {
    private String type;
    private String description;
    private String alternateKey;
    private String keyName;
    private String value;
    private List<String> validValues;
    private static final ArrayList<String> EMPTY_LIST = new ArrayList<String>(0);
    private final static Logger log = Logger.getLogger(ConfigKey.class);
    private String version;
    private ValueHelper valueHelper;

    protected ConfigKey(String type,
            String description,
            String alternateKey,
            String key,
            String value,
            String[] validValues,
            String version,
            ValueHelper helper) {
        super();
        this.type = type;
        this.description = description;
        this.alternateKey = alternateKey;
        this.keyName = key;
        this.value = value;
        setVersion(version);
        this.validValues = validValues != null ? Arrays.asList(validValues) : EMPTY_LIST;
        this.valueHelper = helper;
    }

    public void setVersion(String version) {
        this.version = version == null || version.isEmpty() ? "general" : version;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getAlternateKeys() {
        return alternateKey;
    }

    public String getKey() {
        return keyName;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayValue() throws Exception {
        return valueHelper.getValue(value);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAlternateKey(String alternateKey) {
        this.alternateKey = alternateKey;
    }

    public void setKey(String key) {
        this.keyName = key;
    }

    /**
     * Sets the value of this Config key to the given value. Is meant to be used before updating the DB, therefore is
     * safe, and throws an Exception in case of validation failure.
     *
     * @param value
     *            The value to set
     * @throws InvalidParameterException
     * @throws Exception
     */
    public void safeSetValue(String value) throws InvalidParameterException, Exception {
        if (!valueHelper.validate(this, value)) {
            throw new InvalidParameterException("Cannot set value " + value + "to key " + keyName);
        }
        this.value = valueHelper.setValue(value);
    }

    /**
     * Sets the value of this ConfigKey to the given value without validation. Is meant for internal use only.
     *
     * @param value
     */
    public void unsafeSetValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        String value = "Error fetching value";
        try{
            value = getDisplayValue();
        }
        catch (Exception e) { }
        return (new StringBuilder ("ConfigKey [type=").append(type).append(", description=").append(description)
                .append(", alternateKey=").append(alternateKey).append(", key=").append(keyName).append(", value=").append(value)
                .append(", validValues=").append(validValues).append(", version=").append(version + "]")).toString();
    }

    public List<String> getValidValues() {
        return this.validValues;
    }

    public String getVersion() {
        return version;
    }


    public ValueHelper getValueHelper() {
        return valueHelper;
    }
}
