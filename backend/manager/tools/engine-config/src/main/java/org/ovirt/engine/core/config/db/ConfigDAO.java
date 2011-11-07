package org.ovirt.engine.core.config.db;

import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.config.entity.ConfigKey;

public interface ConfigDAO {

    ConfigKey getKey(ConfigKey key) throws SQLException;

    int updateKey(ConfigKey configKey) throws SQLException;

    List<ConfigKey> getKeysForName(String name) throws SQLException;
}
