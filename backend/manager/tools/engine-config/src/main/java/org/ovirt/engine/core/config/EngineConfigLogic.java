package org.ovirt.engine.core.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ovirt.engine.core.config.db.ConfigDAO;
import org.ovirt.engine.core.config.db.ConfigDaoImpl;
import org.ovirt.engine.core.config.entity.ConfigKey;
import org.ovirt.engine.core.config.entity.ConfigKeyFactory;
import org.ovirt.engine.core.config.validation.ConfigActionType;


/**
 * The <code>EngineConfigLogic</code> class is responsible for the logic of the EngineConfig tool.
 */
public class EngineConfigLogic {

    private final static String ALTERNATE_KEY = "alternateKey";
    private final static Logger log = Logger.getLogger(EngineConfigLogic.class);

    private Configuration appConfig;
    private HierarchicalConfiguration keysConfig;
    private Map<String, String> alternateKeysMap;
    private ConfigKeyFactory configKeyFactory;
    private ConfigDAO configDAO;
    private EngineConfigCLIParser parser;

    public EngineConfigLogic(EngineConfigCLIParser parser) throws Exception {
        this.parser = parser;
        init();
    }

    /**
     * Initiates the members of the class.
     *
     * @throws Exception
     */
    private void init() throws Exception {
        log.debug("init: beginning initiation of EngineConfigLogic");
        appConfig = new AppConfig<PropertiesConfiguration>(parser.getAlternateConfigFile()).getFile();
        keysConfig = new KeysConfig<HierarchicalConfiguration>(parser.getAlternatePropertiesFile()).getFile();
        populateAlternateKeyMap(keysConfig);
        ConfigKeyFactory.init(keysConfig, alternateKeysMap);
        configKeyFactory = ConfigKeyFactory.getInstance();
        try {
            this.configDAO = new ConfigDaoImpl(appConfig);
        } catch (SQLException se) {
            log.debug("init: caught connection error. Error details: ", se);
            throw new ConnectException("Connection to the Database failed. Please check that the hostname and port number are correct and that the Database service is up and running.");
        }
    }

    private void populateAlternateKeyMap(HierarchicalConfiguration config) {
        List<SubnodeConfiguration> configurationsAt = config.configurationsAt("/*/" + ALTERNATE_KEY);
        alternateKeysMap = new HashMap<String, String>(configurationsAt.size());
        for (SubnodeConfiguration node : configurationsAt) {
            String rootKey = node.getRootNode()
                    .getParentNode().getName();
            String[] alternateKeys = config.getStringArray("/" + rootKey + "/" + ALTERNATE_KEY);
            for (String token : alternateKeys) {
                alternateKeysMap.put(token, rootKey);
            }
        }
    }

    /**
     * Executes desired action. Assumes the parser is now holding valid arguments.
     *
     * @throws Exception
     */
    public void execute() throws Exception {
        ConfigActionType actionType = parser.getConfigAction();
        log.debug("execute: beginning execution of action " + actionType + ".");

        switch (actionType) {
            case ACTION_ALL:
                printAllValues();
                break;
            case ACTION_LIST:
                printAvailableKeys();
                break;
            case ACTION_GET:
                printKey();
                break;
            case ACTION_SET:
                persistValue();
                break;
            default: // Should have already been discovered before execute
                log.debug("execute: unable to recognize action: " + actionType + ".");
                throw new UnsupportedOperationException("Please tell me what to do: list? get? set? get-all?");
        }
    }

    /**
     * Prints the values of the given key from the DB.
     * 
     * @throws Exception
     */
    private void printAllValuesForKey(String key) throws Exception {
        List<ConfigKey> keysForName = getConfigDAO().getKeysForName(key);
        if (keysForName.size() == 0) {
            log.debug("printKeyValues: failed to fetch key " + key + " value: no such entry with default version.");
            throw new RuntimeException("Error fetching " + key + " value: no such entry with default version.");
        }

        StringBuilder buffer = new StringBuilder();
        for (ConfigKey configKey : keysForName) {
            buffer.append(String.format("%s: %s version: %s\n",
                    key,
                    configKey.getDisplayValue(),
                    configKey.getVersion())
                    );
        }
        buffer.deleteCharAt(buffer.length() - 1);
        log.info(buffer);
    }

    /**
     * Prints all configuration values. Is the actual execution of the 'get-all' action ('-a', '--all')
     */
    private void printAllValues() {
        List<ConfigurationNode> configNodes = keysConfig.getRootNode().getChildren();
        for (ConfigurationNode node : configNodes) {
            ConfigKey key = configKeyFactory.generateByPropertiesKey(node.getName());
            // TODO - move to one statement for all - time permitting;
            try {
                printAllValuesForKey(key.getKey());
            } catch (Exception e) {
                log.error("Skipping " + key.getKey() + " due to an error: " + e.getMessage());
                log.debug("details:", e);
            }
        }
    }

    /**
     * Prints all available configuration keys. Is the actual execution of the 'list' action ('-l', '--list')
     */
    public void printAvailableKeys() {
        List<ConfigurationNode> configNodes = keysConfig.getRootNode().getChildren();
        for (ConfigurationNode node : configNodes) {
            ConfigKey key = configKeyFactory.generateByPropertiesKey(node.getName());
            log.info(MessageFormat.format("{0}: {1} (Value Type: {2})",
                    key.getKey(),
                    key.getDescription(),
                    key.getType()));
        }
    }

    /**
     * If a version has been given, prints the specific value for the key and version, otherwise prints all the values
     * for the key. Is the actual execution of the 'get' action ('-g', '--get')
     * @throws Exception
     */
    private void printKey() throws Exception {
        String key = parser.getKey();
        String version = parser.getVersion();
        if (StringUtils.isBlank(version)) {
            if (getConfigKey(key) == null) {
                throw new RuntimeException("Error fetching " + key
                        + " value: no such entry. Please verify key name and property file support.");
            }
            printAllValuesForKey(key);
        } else {
            printKeyWithSpecifiedVersion(key, version);
        }
    }

    /**
     * Fetches the given key with the given version from the DB and prints it.
     * 
     * @param key
     * @param version
     * @throws Exception
     */
    private void printKeyWithSpecifiedVersion(String key, String version) throws Exception {
        ConfigKey configKey = fetchConfigKey(key, version);
        if (configKey == null || configKey.getKey() == null) {
            log.debug("getValue: error fetching " + key + " value: no such entry with version '" + version + "'.");
            throw new RuntimeException("Error fetching " + key + " value: no such entry with version '" + version
                    + "'.");
        } else {
            log.info(configKey.getDisplayValue());
        }
    }

    /**
     * Sets the value of the given key for the given version. Is the actual execution of the 'set' action ('-s',
     * '--set')
     */
    private void persistValue() throws Exception {
        String key = parser.getKey();
        String value = parser.getValue();
        String version = parser.getVersion();
        if (version == null) {
            version = startVersionDialog(key);
        }

        boolean sucessUpdate = persist(key, value, version);
        if (!sucessUpdate) {
            log.debug("setValue: error setting " + key + "'s value. No such entry"
                    + (version == null ? "" : " with version " + version) + ".");
            throw new IllegalArgumentException("Error setting " + key + "'s value. No such entry"
                    + (version == null ? "" : " with version " + version) + ".");
        }
    }

    /**
     * Is called when it is unclear which version is desired. If only one version exists for the given key, assumes that
     * is the desired version, if more than one exist, prompts the user to choose one.
     *
     * @param key
     *            The version needs to be found for this key
     * @return A version for the given key
     * @throws IOException
     * @throws SQLException
     */
    private String startVersionDialog(String key) throws IOException, SQLException {
        log.debug("starting version dialog.");
        String version = null;
        List<ConfigKey> keys = configDAO.getKeysForName(key);
        if (keys.size() == 1) {
            version = keys.get(0).getVersion();
        } else if (keys.size() > 1) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("Please select a version:");
                for (int i = 0; i < keys.size(); i++) {
                    System.out.println(i + 1 + ". " + keys.get(i).getVersion());
                }
                int index = 0;
                try {
                    index = Integer.valueOf(br.readLine());
                } catch (NumberFormatException e) {
                    continue;
                }
                if (index >= 1 && index <= keys.size()) {
                    version = keys.get(index - 1).getVersion();
                    break;
                }
            }
        }
        return version;
    }

    /**
     * Sets the given key with the given version to the given value. Is protected for test purposes.
     *
     * @param key
     * @param value
     * @param version
     * @return
     * @throws IllegalAccessException
     */
    protected boolean persist(String key, String value, String version) throws IllegalAccessException {
        ConfigKey configKey = configKeyFactory.generateByPropertiesKey(key);
        configKey.setVersion(version);
        String message = null;
        boolean res = true;

        try {
            configKey.safeSetValue(value);
            res = (getConfigDAO().updateKey(configKey) == 1);
        } catch (InvalidParameterException ipe) {
            message = (
                    "'" + value + "' is not a valid value for type " + configKey.getType() + ". " +
                    (configKey.getValidValues().isEmpty() ? "" : "Valid values are " + configKey.getValidValues())
            );
        } catch (Exception e) {
            message = (
                    "Error setting " + key +
                    "'s value. No such entry with version '" + version + "'."
            );
            log.debug("Error details: ", e);
        }
        if (message != null) {
            log.debug(message);
            throw new IllegalAccessException(message);
        }

        return res;
    }

    public boolean persist(String key, String value) throws Exception {
        return persist(key, value, "");
    }

    private ConfigKey getConfigKey(String key) {
        ConfigKey ckReturn = null;
        ckReturn = configKeyFactory.generateByPropertiesKey(key);
        if (ckReturn == null || ckReturn.getKey() == null) {
            ckReturn = null;
            log.debug("getConfigKey: Unable to fetch the value of " + key + ".");
        }

        return ckReturn;
    }

    public ConfigKey fetchConfigKey(String key, String version) {
        ConfigKey configKey = getConfigKey(key);
        if (configKey == null || configKey.getKey() == null) {
            log.debug("Unable to fetch the value of " + key + " in version " + version);
            return null;
        }
        configKey.setVersion(version);
        log.debug("Fetching key=" + configKey.getKey() + " ver=" + version);
        try {
            return getConfigDAO().getKey(configKey);
        } catch (SQLException e) {
            return null;
        }
    }

    public ConfigDAO getConfigDAO() {
        return configDAO;
    }
}