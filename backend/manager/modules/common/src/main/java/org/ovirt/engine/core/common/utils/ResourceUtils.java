package org.ovirt.engine.core.common.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

/**
 * Provides utilities for handling resource files.
 */
public class ResourceUtils {
    private static LogCompat log = LogFactoryCompat.getLog(ResourceUtils.class);

    /**
     * Loads a collection of {@link Properties} from a resource file.
     *
     * @param cls
     *            The class (used by the ClassLoader)
     * @param name
     *            The resource file name
     * @return The Properties class
     * @throws IOException
     */
    public static Properties loadProperties(Class<?> cls, String name) throws IOException {
        final String ERR_MSG = "Failed to locate resource file: " + name;
        InputStream is = cls.getClassLoader().getResourceAsStream(name);
        if (is == null) {
            log.error(ERR_MSG);
            throw new FileNotFoundException(ERR_MSG);
        }

        try {
            Properties props = new Properties();
            props.load(is);
            return props;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                String msg = (e.getMessage() != null) ? e.getMessage() : "";
                log.error("Failed to close input stream: " + msg);
            }
        }
    }
}
