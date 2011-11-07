package org.ovirt.engine.core.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.*;

public class EnumUtils {

    private static Map<Class<?>, Map> cacheEnumValuesInCapitalLetters = new HashMap<Class<?>, Map>();

    public static String ConvertToStringWithSpaces(String value) {
        StringBuilder result = new StringBuilder();
        Regex r = new Regex("([A-Z]{1,}[a-z]*)|([0-9]*)");
        MatchCollection coll = r.Matches(value);
        for (int i = 0; i < coll.size(); i++) {
            result.append(coll.get(i).getValue());
            if (i + 1 != coll.size()) {
                result.append(" ");
            }
        }
        return result.toString().trim();
    }

    public static <E extends Enum<E>> E valueOf(Class<E> c, String name, boolean ignorecase) {
        // trim any leading or trailing spaces from the name
        name = name.trim();

        if (!ignorecase) {
            {
                return Enum.<E> valueOf(c, name);
            }
        }

        E[] universe = c.getEnumConstants();
        if (universe == null) {
            throw new IllegalArgumentException(name + " is not an enum type");
        }

        Map<String, E> map = cacheEnumValuesInCapitalLetters.get(c);

        if (map == null) {
            // populate the map with enum values and add it to cache
            map = new HashMap<String, E>(2 * universe.length);

            for (E e : universe) {
                map.put(e.name().toUpperCase(), e);
            }
            cacheEnumValuesInCapitalLetters.put(c, map);
        }

        E result = map.get(name.toUpperCase());
        if (result == null) {
            throw new IllegalArgumentException("No enum const " + c.getName() + "." + name);
        }
        return result;
    }

}
