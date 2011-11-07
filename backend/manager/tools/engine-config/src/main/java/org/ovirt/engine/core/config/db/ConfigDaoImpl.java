package org.ovirt.engine.core.config.db;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.ovirt.engine.core.config.entity.ConfigKey;
import org.ovirt.engine.core.config.entity.ConfigKeyFactory;
import org.ovirt.engine.core.tools.common.db.ConnectionFactory;
import org.ovirt.engine.core.tools.common.db.JbossConnectionFactory;

public class ConfigDaoImpl implements ConfigDAO {

    private String updateSql;
    private String selectSql;
    private String selectKeysForNameSql;
    private final Configuration appConfig;
    private String configTable;
    private String nameColumn;
    private String valueColumn;
    private String versionColumn;
    private Connection connection;

    public ConfigDaoImpl(Configuration appConfig) throws ClassNotFoundException, SQLException, ConfigurationException,
            ConnectException {
        this.appConfig = appConfig;
        valueColumn = appConfig.getString("configColumnValue");
        configTable = appConfig.getString("configTable");
        nameColumn = appConfig.getString("configColumnName");
        versionColumn = appConfig.getString("configColumnVersion");
        selectSql =
                MessageFormat.format("select {0} from {1} where {2}=? and {3} =?",
                                 valueColumn,
                                 configTable,
                                 nameColumn,
                                 versionColumn);
        updateSql =
                MessageFormat.format("update {0} set {1}=? where {2}=? and {3}=?",
                                 configTable,
                                 valueColumn,
                                 nameColumn,
                                 versionColumn);
        selectKeysForNameSql =
                MessageFormat.format("select * from {0} where {1}=? ",
                        configTable,
                        nameColumn);
        connection = getDbConnection();
    }

    @Override
    public int updateKey(ConfigKey configKey) throws SQLException {
        PreparedStatement prepareStatement = null;
        int executeUpdate;
        try {
            prepareStatement = connection.prepareStatement(updateSql);
            prepareStatement.setString(1, configKey.getValue());
            prepareStatement.setString(2, configKey.getKey());
            prepareStatement.setString(3, configKey.getVersion());
            executeUpdate = prepareStatement.executeUpdate();
        } finally {
            if (prepareStatement != null) {
                prepareStatement.close();
            }
        }
        return executeUpdate;
    }

    @Override
    public ConfigKey getKey(ConfigKey key) throws SQLException {
        PreparedStatement prepareStatement = null;
        ConfigKey ckReturn = null;
        try {
            prepareStatement = connection.prepareStatement(selectSql);
            prepareStatement.setString(1, key.getKey());
            prepareStatement.setString(2, key.getVersion());
            ResultSet resultSet = prepareStatement.executeQuery();
            resultSet.next();
            try {
                String value = resultSet.getString(valueColumn);
                ckReturn = ConfigKeyFactory.getInstance().copyOf(key, value, key.getVersion());
            } catch (Exception e) {
                throw new SQLException("Failed to fetch value of " + key.getKey() + " with version " + key.getVersion() + " from DB. " + e.getMessage());
            }
        } finally {
            if (prepareStatement != null) {
                prepareStatement.close();
            }
        }
        return ckReturn;
    }

    private Connection getDbConnection() throws ClassNotFoundException, SQLException, ConfigurationException, ConnectException {
        ConnectionFactory factory =
                new JbossConnectionFactory(appConfig.getString("jbossDataSourceFile"),
                        appConfig.getString("jbossLoginConfigFile"));
        return factory.getConnection();
    }

    @Override
    public List<ConfigKey> getKeysForName(String name) throws SQLException {
        PreparedStatement prepareStatement = null;
        List<ConfigKey> keys = new ArrayList<ConfigKey>();
        try {
            prepareStatement = connection.prepareStatement(selectKeysForNameSql);
            prepareStatement.setString(1, name);
            ResultSet resultSet = prepareStatement.executeQuery();
            while (resultSet.next()) {
                try {
                    keys.add(ConfigKeyFactory.getInstance().fromResultSet(resultSet));
                } catch (Exception e) {
                    throw new SQLException("Failed to fetch value of " + name + " from DB. " +
                            e.getMessage());
                }
            }
        } finally {
            if (prepareStatement != null) {
                prepareStatement.close();
            }
        }

        return keys;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        connection.close();
    }
}
