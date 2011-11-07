package org.ovirt.engine.api.restapi.utils;

import org.ovirt.engine.api.model.Version;

public class VersionUtils {
    public static boolean greaterOrEqual(Version a, Version b) {
        return a.getMajor() != b.getMajor() ? a.getMajor() >= b.getMajor() : a.getMinor() >= b.getMinor();
    }
    public static boolean greaterOrEqual(org.ovirt.engine.core.compat.Version a, org.ovirt.engine.core.compat.Version b) {
        return a.getMajor() != b.getMajor() ? a.getMajor() >= b.getMajor() : a.getMinor() >= b.getMinor();
    }

    public static boolean greaterOrEqual(Version a, org.ovirt.engine.core.compat.Version b) {
        return a.getMajor() != b.getMajor() ? a.getMajor() >= b.getMajor() : a.getMinor() >= b.getMinor();
    }
}
