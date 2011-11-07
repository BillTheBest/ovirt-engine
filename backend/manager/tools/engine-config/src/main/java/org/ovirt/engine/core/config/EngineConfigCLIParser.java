package org.ovirt.engine.core.config;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.config.validation.ConfigActionType;

/**
 * The <code>EngineConfigCLIParser</code> class represents a parser for the EngineConfig tool. It parses the given
 * arguments into meaningful keys and values. The parser assumes the '=' char can only be used in the format k=v, and
 * not as a char that is actually part of a key/value.
 */
public class EngineConfigCLIParser {

    private static final Logger log = Logger.getLogger(EngineConfigCLIParser.class);

    private HashMap<String, String> argsMap = new HashMap<String, String>();
    private EngineConfigMap engineConfigMap = new EngineConfigMap();

    public EngineConfigCLIParser() {
    }

    /**
     * Parses the given arguments, identifies the desired action, and the different keys and values.
     * @param args
     *            The arguments that need to be parsed.
     * @throws IllegalArgumentException
     *             If there are no arguments, if a legal action was not identified, or if second argument has '=' char,
     *             but action is not 'set'.
     */
    public void parse(String[] args) {
        log.debug("parse: beginning to parse arguments.");
        validateNonEmpty(args);
        parseAction(args);
        parseArguments(args);
        log.debug("parse: Finished parsing arguments.");
    }

    private void validateNonEmpty(String[] args) {
        if (args.length == 0) {
            log.debug("parse error: no arguments given.");
            throw (new IllegalArgumentException("Error: at least 1 argument needed for configuration utility to run."));
        }
    }

    /**
     * Parses the argument in the currentIndex of args, into a key and its value. Can also parse version, properties
     * file, config file. If the argument in the currentIndex does not have a value, assumes the value is in the
     * following argument. The argsMap helps to parse the arguments which are eventually set into the engineConfigMap.
     * @param args
     * @param currentIndex
     * @return whether or not the next argument is to be skipped
     */
    private boolean parseKeyValue (String[] args, int currentIndex) {
        boolean fShouldSkip = false;
        int delimiterIndex = args[currentIndex].indexOf("=");
        String key = getStringBeforeEqualChar(args[currentIndex], delimiterIndex); // includes '-'
        String value = getStringAfterEqualChar(args[currentIndex], delimiterIndex);

        if (!key.isEmpty()) {
            if (!value.isEmpty()) {
                argsMap.put(key, value);
            } else {
                if (args.length > currentIndex + 1) { // To make sure there is another argument
                    argsMap.put(key, args[currentIndex + 1]);
                    fShouldSkip = true;
                } else {
                    log.debug("parsing error: missing pair for key " + args[currentIndex] + ". Skipping argument.");
                }
            }
        } else {
            log.debug("parsing error: illegal argument " + args[currentIndex] + ", starts with '='. Skipping argument.");
        }
        return fShouldSkip;
    }

    /**
     * Parses the second argument in case it does not start with a '-'. There are two valid scenarios for this. First,
     * when the argument has been given as the key in the 'get' action, in the format: "-g key". Second, in the 'set'
     * action. For set action, we require key=value, or parsing will fail.
     *
     * @param arg
     */
    private void parseSecondArgWithoutDash(String arg) {
        int delimiterIndex = arg.indexOf("=");
        if (getConfigAction().equals(ConfigActionType.ACTION_SET) && delimiterIndex == -1) {
            throw new IllegalArgumentException("Argument for set action must be in format of key=value.");
        }

        String key = getStringBeforeEqualChar(arg, delimiterIndex);
        String value = getStringAfterEqualChar(arg, delimiterIndex);
        if (!key.isEmpty()) {
            if (!value.isEmpty()) {
                parseSecondArgWithKeyValue(arg, key, value);
            } else if (getConfigAction().equals(ConfigActionType.ACTION_SET) && getKey() == null) {
                engineConfigMap.setKey(key);
                engineConfigMap.setValue(value);
            } else if (getConfigAction().equals(ConfigActionType.ACTION_GET) && getKey() == null) {
                engineConfigMap.setKey(arg); // sets the key in 'get' action with format: "-g key"
            } else {
                log.debug("parsing error: illegal argument " + arg + ". Skipping argument.");
            }
        } else {
            log.debug("parsing error: illegal argument " + arg + ", starts with '='. Skipping argument.");
        }
    }

    /**
     * Parses second argument with a key and a value. Is only valid in the 'set' action.
     * @param arg
     */
    private void parseSecondArgWithKeyValue(String arg, String key, String value) {
        if (getConfigAction().equals(ConfigActionType.ACTION_SET)) {
            engineConfigMap.setKey(key);
            engineConfigMap.setValue(value);
        } else {
            log.debug("parseArguments error: second argument '"
                    + arg + "' has an '=' char but action is not 'set'.");
            throw new IllegalArgumentException("Illegal second argument: " + arg + ".");
        }
    }

    /**
     * Parses all arguments except for the first argument which is assumed to be the action. The argsMap member helps to
     * parse the arguments which are eventually set into the engineConfigMap.
     * @param args
     *            The arguments that needs to be parsed.
     */
    private void parseArguments(String[] args) {
        boolean fShouldSkip = true; // So the first arg which is the action will be skipped
        for (int currentIndex = 0; currentIndex < args.length; currentIndex++) {
            if (fShouldSkip) {
                fShouldSkip = false;
                continue;
            }
            if (args[currentIndex].startsWith("-")) {
                fShouldSkip = parseKeyValue(args, currentIndex);
            } else if (currentIndex == 1) {
                parseSecondArgWithoutDash(args[currentIndex]);
            } else {
                log.debug("parseArguments error: Skipping argument " + args[currentIndex] + ".");
            }
        }
        fillEngineConfigMap();
    }

    /**
     * Parses the action from the given arguments.
     * @param args
     * @throws IllegalArgumentException
     *             If the first argument is not a legal action
     */
    private void parseAction(String[] args) {
        validateArgStartsWithDash(args[0]);
        int delimiterIndex = args[0].indexOf("=");
        String action = getStringBeforeEqualChar(args[0], delimiterIndex);
        String key = getStringAfterEqualChar(args[0], delimiterIndex);

        if (!action.isEmpty()) {
            if (!key.isEmpty()) {
                handleActionWithKey(action, key);
            } else {
                handleActionWithoutKey(action);
            }
        } else {
            log.debug("parseAction error: Illegal first argument: '" + args[0] + "' - not a legal action.");
            throw new IllegalArgumentException("Action verb must come first, and '" + args[0]
                    + "' is not an action.\nPlease tell me what to do: list? get? set? get-all?");
        }
    }

    /**
     * Returns the first part of the given arg, until the delimiterIndex, excluding. Did not use split() because of
     * problematic handling of empty parts.
     */
    private String getStringAfterEqualChar(String arg, int delimiterIndex) {
        String value;
        if (delimiterIndex > 0) {
            value = arg.substring(delimiterIndex + 1);
        } else {
            value = "";
        }
        return value;
    }

    /**
     * Returns the second part of the given arg, starting from the delimiterIndex, excluding. Did not use split()
     * because of problematic handling of empty parts.
     */
    private String getStringBeforeEqualChar(String arg, int delimiterIndex) {
        String key;
        if (delimiterIndex > 0) {
            key = arg.substring(0, delimiterIndex);
        } else {
            key = arg;
        }
        return key;
    }

    /**
     * Handles an action without a key.
     * @param action
     */
    private void handleActionWithoutKey(String action) {
        engineConfigMap.setConfigAction(ConfigActionType.getActionType(action));
        if (getConfigAction() == null) {
            log.debug("parseAction error: Illegal first argument: '" + action + "' - not a legal action.");
            throw new IllegalArgumentException("Action verb must come first, and '" + action
                    + "' is not an action.\nPlease tell me what to do: list? get? set? get-all?");
        }
    }

    /**
     * Handles an action with a key. The only valid action with a key in the first argument is the 'get' action, in the
     * format: "--get=key".
     * @param action
     * @param key
     */
    private void handleActionWithKey(String action, String key) {
        engineConfigMap.setConfigAction(ConfigActionType.getActionType(action));
        if (action.equals("--get")) { // Since this is the only valid case for having a key in the first arg
            engineConfigMap.setKey(key);
        } else {
            log.debug("parseAction error: first argument is illegal.");
            throw new IllegalArgumentException("Action verb must come first, and '" + action + '=' + key
                    + "' is not an action.\nPlease tell me what to do: list? get? set? get-all?");
        }
    }

    /**
     * Makes sure the first argument starts with a '-', since all actions do.
     * @param arg
     */
    private void validateArgStartsWithDash(String arg) {
        if (!arg.startsWith("-")) {
            log.debug("parseAction error: first argument '" + arg + "' did not start with '-' or '--'.");
            throw (new IllegalArgumentException("First argument must be an action, and start with '-' or '--'"));
        }
    }

    private String parseAlternateConfigFile() {
        for (String configKeyName : AlternateFileType.OPTION_CONFIG.getOptionalStrings()) {
            if (argsMap.containsKey(configKeyName)) {
                return argsMap.get(configKeyName);
            }
        }
        return null;
    }

    private String parseAlternatePropertiesFile() {
        for (String propertyKeyName : AlternateFileType.OPTION_PROPERTIES.getOptionalStrings()) {
            if (argsMap.containsKey(propertyKeyName)) {
                return argsMap.get(propertyKeyName);
            }
        }
        return null;
    }

    private void fillEngineConfigMap() {
        engineConfigMap.setVersion(argsMap.get("--cver"));
        engineConfigMap.setAlternateConfigFile(parseAlternateConfigFile());
        engineConfigMap.setAlternatePropertiesFile(parseAlternatePropertiesFile());
    }

    public EngineConfigMap getEngineConfigMap() {
        return engineConfigMap;
    }

    public String getVersion() {
        return engineConfigMap.getVersion();
    }

    public ConfigActionType getConfigAction() {
        return engineConfigMap.getConfigAction();
    }

    public String getKey() {
        return engineConfigMap.getKey();
    }

    public String getValue() {
        return engineConfigMap.getValue();
    }

    public String getAlternateConfigFile() {
        return engineConfigMap.getAlternateConfigFile();
    }

    public String getAlternatePropertiesFile() {
        return engineConfigMap.getAlternatePropertiesFile();
    }

    public String engineConfigMapToString() {
        return engineConfigMap.toString();
    }
}
