package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.text.StrSubstitutor;
import org.ovirt.engine.core.common.interfaces.ErrorTranslator;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;

public final class ErrorTranslatorImpl implements ErrorTranslator {

    private static final long ONE_HOUR = 60 * 60 * 1000L;

    private List<String> messageSources;
    private Locale standardLocale;
    private Map<String, String> standardMessages;
    private ReapedMap<Locale, Map<String, String>> messagesByLocale;

    // Will assume these are property files, not ResxFiles.
    public ErrorTranslatorImpl(String... errorFileNames) {
        messageSources = asList(errorFileNames);
        standardLocale = Locale.getDefault();
        standardMessages = retrieveByLocale(standardLocale);
        messagesByLocale = new ReapedMap<Locale, Map<String, String>>(ONE_HOUR, true);
    }

    private synchronized Map<String, String> getMessages(Locale locale) {
        Map<String, String> messages = null;
        if (standardLocale.equals(locale)) {
            messages = standardMessages;
        } else {
            if ((messages = messagesByLocale.get(locale)) == null) {
                messages = retrieveByLocale(locale);
                messagesByLocale.put(locale, messages);
                messagesByLocale.reapable(locale);
            }
        }
        return messages;
    }

    private Map<String, String> retrieveByLocale(Locale locale) {
        Map<String, String> messages = new HashMap<String, String>();
        for (String messageSource : messageSources) {
            retrieveByLocale(locale, messageSource, messages);
        }
        return messages;
    }

    private Map<String, String> retrieveByLocale(Locale locale, String messageSource, Map<String, String> messages) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(messageSource, locale);
            for (String key : bundle.keySet()) {
                if (!messages.containsKey(key)) {
                    messages.put(key, bundle.getString(key));
                } else {
                    log.warnFormat("Code {0} appears more then once in string table.", key);
                }
            }
        } catch (RuntimeException e) {
            log.errorFormat("File: {0} could not be loaded: {1}", messageSource, e.toString());
        }
        return messages;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#TranslateErrorText(java.util.List, boolean)
     */
    public final List<String> TranslateErrorText(List<String> errorMsg, boolean changeIfNotFound) {
        return translate(errorMsg, changeIfNotFound, Locale.getDefault());
    }

    private final List<String> translate(List<String> errorMsg, boolean changeIfNotFound, Locale locale) {
        List<String> translatedMessages = doTranslation(errorMsg, changeIfNotFound, locale);
        return ResolveMessages(translatedMessages);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#TranslateErrorText(java.util.List)
     */
    public List<String> TranslateErrorText(List<String> errorMsg) {
        return translate(errorMsg, true, Locale.getDefault());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#TranslateErrorText(java.util.List)
     */
    public List<String> TranslateErrorText(List<String> errorMsg, Locale locale) {
        return translate(errorMsg, true, locale);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#TranslateMessages(java.util.List, boolean)
     */
    public final List<String> TranslateMessages(List<String> errorMsg, boolean changeIfNotFound) {
        return doTranslation(errorMsg, changeIfNotFound, Locale.getDefault());
    }

    public final List<String> doTranslation(List<String> errorMsg, boolean changeIfNotFound, Locale locale) {
        java.util.ArrayList<String> translatedMessages = new java.util.ArrayList<String>();
        if (errorMsg != null && errorMsg.size() > 0) {
            for (String curError : errorMsg) {
                translatedMessages.add(translate(curError, changeIfNotFound, locale));
            }
        }
        return translatedMessages;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#IsDynamicVariable(java.lang.String )
     */
    public final boolean IsDynamicVariable(String strMessage) {
        return strMessage.startsWith("$");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#TranslateErrorTextSingle(java. lang.String, boolean)
     */
    public final String TranslateErrorTextSingle(String errorMsg, boolean changeIfNotFound) {
        return translate(errorMsg, changeIfNotFound, Locale.getDefault());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#TranslateErrorTextSingle(java. lang.String, boolean)
     */
    public String TranslateErrorTextSingle(String errorMsg, Locale locale) {
        return translate(errorMsg, true, locale);
    }

    private final String translate(String errorMsg, boolean changeIfNotFound, Locale locale) {
        String ret = "";
        Map<String, String> messages = getMessages(locale);
        if (messages != null && messages.containsKey(errorMsg)) {
            ret = messages.get(errorMsg);
        } else {
            if (!(errorMsg == null || errorMsg.isEmpty())) {
                if ((IsDynamicVariable(errorMsg)) || (!changeIfNotFound)) {
                    ret = errorMsg;
                } else
                // just a message that doesn't have a value in the resource:
                {
                    String[] splitted = errorMsg.toLowerCase().split("[_]", -1);
                    ret = StringHelper.join(" ", splitted);
                }
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#TranslateErrorTextSingle(java. lang.String)
     */
    public final String TranslateErrorTextSingle(String errorMsg) {
        return TranslateErrorTextSingle(errorMsg, true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ovirt.engine.core.utils.ErrorTranslator#ResolveMessages(java.util.List)
     */
    public final List<String> ResolveMessages(List<String> translatedMessages) {
        java.util.ArrayList<String> translatedErrors = new java.util.ArrayList<String>();
        java.util.HashMap<String, String> variables = new java.util.HashMap<String, String>();
        for (String currentMessage : translatedMessages) {
            if (currentMessage.startsWith("$")) {
                AddVariable(currentMessage, variables);
            } else {
                translatedErrors.add(currentMessage);
            }
        }
        /**
         * Place to global variable adding
         */
        java.util.ArrayList<String> returnValue = new java.util.ArrayList<String>();
        for (String error : translatedErrors) {
            returnValue.add(resolveMessage(error, variables));
        }
        return returnValue;
    }

    private void AddVariable(String variable, java.util.HashMap<String, String> variables) {
        int firstSpace = variable.indexOf(' ');
        if (firstSpace != -1 && firstSpace < variable.length()) {
            String key = variable.substring(1, firstSpace);
            String value = variable.substring(firstSpace + 1);
            if (!variables.containsKey(key)) {
                variables.put(key, value);
            }
        }
    }

    private String resolveMessage(String message, java.util.HashMap<String, String> variables) {
        StrSubstitutor sub = new StrSubstitutor(variables);
        return sub.replace(message);
    }

    private static List<String> asList(String[] names) {
        List<String> ret = new ArrayList<String>();
        for (int i = 0; i < names.length; i++) {
            ret.add(trim(names[i]));
        }
        return ret;
    }

    private static String trim(String name) {
        return name != null && name.endsWith(".properties")
                ? name.substring(0, name.lastIndexOf(".properties"))
                : name;
    }

    private static LogCompat log = LogFactoryCompat.getLog(ErrorTranslatorImpl.class);
}
