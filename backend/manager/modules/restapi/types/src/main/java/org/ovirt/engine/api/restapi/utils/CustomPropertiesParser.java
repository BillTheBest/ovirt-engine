package org.ovirt.engine.api.restapi.utils;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.CustomProperty;

public class CustomPropertiesParser {

     /**
     * Format of @str is <name>=<value>;<name>=<value>;..
     *
     * @param str - The string to parse
     * @param isRegex - defines if CustomProperty is used for regex or value representation
     * @return
    */
    public static List<CustomProperty> parse(String str, boolean isRegex) {
        List<CustomProperty> ret = new ArrayList<CustomProperty>();
        if (str != null) {
            for (String envStr : str.split(";", -1)) {
                String[] parts = envStr.split("=", -1);
                if (parts.length == 2) {
                    CustomProperty env = new CustomProperty();
                    env.setName(parts[0]);
                    if (isRegex) {
                        env.setRegexp(parts[1]);
                    } else {
                        env.setValue(parts[1]);
                    }
                    ret.add(env);
                }
            }
        }
        return ret;
    }

    /**
     * Converts VmHooksEnv to @str as <name>=<value>;<name>=<value>;..
     *
     * @param customProperties
     * @return String representing custom properties
     */
    public static String parse(List<CustomProperty> customProperties) {
        StringBuffer buff = new StringBuffer();
        for (CustomProperty hook : customProperties) {
            if (hook.isSetName() && hook.isSetValue()) {
                buff.append(hook.getName() + "=" + hook.getValue() + ";");
            }
        }
        return buff.toString();
    }
}
