package org.ovirt.engine.core.compat;


public final class StringHelper {
    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'IsNullOrEmpty'.
    // ------------------------------------------------------------------------------------
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'Join' (2 parameter
    // version).
    // ------------------------------------------------------------------------------------
    public static String join(String separator, Object[] values) {
        if (values == null)
            return null;
        else
            return join(separator, values, 0, values.length);
    }

    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'Join' (4 parameter
    // version).
    // ------------------------------------------------------------------------------------
    public static String join(String separator, Object[] objectArray, int startindex, int count) {
        String result = "";

        if (objectArray == null)
            return null;

        for (int index = startindex; index < objectArray.length && index - startindex < count; index++) {
            if (separator != null && index > startindex)
                result += separator;

            if (objectArray[index] != null)
                result += objectArray[index].toString();
        }

        return result;
    }

    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'TrimEnd'.
    // ------------------------------------------------------------------------------------
    public static String trimEnd(String string, Character... charsToTrim) {
        if (string == null || charsToTrim == null)
            return string;

        int lengthToKeep = string.length();
        for (int index = string.length() - 1; index >= 0; index--) {
            boolean removeChar = false;
            if (charsToTrim.length == 0) {
                if (Character.isSpace(string.charAt(index))) {
                    lengthToKeep = index;
                    removeChar = true;
                }
            } else {
                for (int trimCharIndex = 0; trimCharIndex < charsToTrim.length; trimCharIndex++) {
                    if (string.charAt(index) == charsToTrim[trimCharIndex]) {
                        lengthToKeep = index;
                        removeChar = true;
                        break;
                    }
                }
            }
            if (!removeChar)
                break;
        }
        return string.substring(0, lengthToKeep);
    }

    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'TrimStart'.
    // ------------------------------------------------------------------------------------
    public static String trimStart(String string, Character... charsToTrim) {
        if (string == null || charsToTrim == null)
            return string;

        int startingIndex = 0;
        for (int index = 0; index < string.length(); index++) {
            boolean removeChar = false;
            if (charsToTrim.length == 0) {
                if (Character.isSpace(string.charAt(index))) {
                    startingIndex = index + 1;
                    removeChar = true;
                }
            } else {
                for (int trimCharIndex = 0; trimCharIndex < charsToTrim.length; trimCharIndex++) {
                    if (string.charAt(index) == charsToTrim[trimCharIndex]) {
                        startingIndex = index + 1;
                        removeChar = true;
                        break;
                    }
                }
            }
            if (!removeChar)
                break;
        }
        return string.substring(startingIndex);
    }

    // ------------------------------------------------------------------------------------
    // This method replaces the .NET static string method 'Trim' when arguments
    // are used.
    // ------------------------------------------------------------------------------------
    public static String trim(String string, Character... charsToTrim) {
        return trimEnd(trimStart(string, charsToTrim), charsToTrim);
    }

    // ------------------------------------------------------------------------------------
    // This method is used for string equality comparisons when the option
    // 'Use helper 'stringsEqual' method to handle null strings' is selected
    // (The Java String 'equals' method can't be called on a null instance).
    // ------------------------------------------------------------------------------------
    public static boolean EqOp(String s1, String s2) {
        if (s1 == null && s2 == null)
            return true;
        else
            return s1 != null && s1.equals(s2);
    }

    public static String trim(String s, char[] cs) {
        Character[] chars = new Character[cs.length];
        for (int i = 0; i < cs.length; i++)
            chars[i] = cs[i];

        return trim(s, chars);
    }

    public static String padLeft(String value, int length, char c) {
        StringBuilder builder = new StringBuilder(value);
        while (builder.length() < length) {
            builder.insert(0, c);
        }

        return builder.toString();
    }

    /**
     * Aggregate String using the given char separator
     *
     * @param messgaes
     *            - the string to aggregate
     * @param seperator
     *            - the separator to use between 2 strings
     * @return the string separated by the give separator
     */
    public static String aggregate(java.util.List<String> messages, char separator) {
        if (messages == null) {
            throw new IllegalArgumentException("The messages parameter can not be null");
        }
        StringBuffer fullString = new StringBuffer();
        if (messages.size() > 0) {
            for (String msg : messages) {
                fullString.append(msg).append(separator);
            }
            fullString.deleteCharAt(fullString.length() - 1);
        }
        return fullString.toString();
    }

    // ------------------------------------------------------------------------------------
    // This method is used for string equality comparisons when the option
    // 'Use helper 'stringsEqual' method to handle null strings' is selected
    // (The Java String 'equals' method can't be called on a null instance).
    // ------------------------------------------------------------------------------------
    public static boolean stringsEqual(String s1, String s2) {
        if (s1 == null && s2 == null)
            return true;
        else
            return s1 != null && s1.equals(s2);
    }

    public static boolean stringsEqualIgnoreCase(String s1, String s2) {
        if (s1 == null && s2 == null)
            return true;
        else
            return s1 != null && s1.equalsIgnoreCase(s2);
    }

    /**
     * Returns a literal pattern <code>String</code> for the specified <code>String</code>.
     *
     * <p>
     * This method produces a <code>String</code> that can be used to create a <code>Pattern</code> that would match the
     * string <code>s</code> as if it were a literal pattern.
     * </p>
     * Metacharacters or escape sequences in the input sequence will be given no special meaning.
     *
     * Copied from Pattern.java code for GWT compatibility.
     *
     * @param s
     *            The string to be literalized
     * @return A literal string replacement
     * @since 1.5
     */
    public static String quote(String s) {
        int slashEIndex = s.indexOf("\\E");
        if (slashEIndex == -1)
            return "\\Q" + s + "\\E";
        StringBuilder sb = new StringBuilder(s.length() * 2);
        sb.append("\\Q");
        slashEIndex = 0;
        int current = 0;
        while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
            sb.append(s.substring(current, slashEIndex));
            current = slashEIndex + 2;
            sb.append("\\E\\\\E\\Q");
        }
        sb.append(s.substring(current, s.length()));
        sb.append("\\E");
        return sb.toString();
    }

}
