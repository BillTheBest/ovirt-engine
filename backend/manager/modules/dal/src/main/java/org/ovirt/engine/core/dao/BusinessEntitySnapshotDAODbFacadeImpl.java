/**
 *
 */
package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot.SnapshotType;
import org.ovirt.engine.core.compat.Guid;

/**
 * JDBC-Template DAO for business entity snapshots
 *
 */
public class BusinessEntitySnapshotDAODbFacadeImpl extends BaseDAODbFacade implements BusinessEntitySnapshotDAO {

    private static class BusinessEntitySnapshotMapper implements ParameterizedRowMapper<BusinessEntitySnapshot> {

        @Override
        public BusinessEntitySnapshot mapRow(ResultSet rs, int rowNum) throws SQLException {
            BusinessEntitySnapshot result = new BusinessEntitySnapshot();
            result.setCommandId(Guid.createGuidFromString(rs.getString("command_id")));
            result.setCommandType(rs.getString("command_type"));
            result.setEntityId(rs.getString("entity_id"));
            result.setEntityType(rs.getString("entity_type"));
            result.setEntitySnapshot(rs.getString("entity_snapshot"));
            result.setSnapshotClass(rs.getString("snapshot_class"));
            result.setSnapshotType(SnapshotType.values()[rs.getInt("snapshot_type")]);
            result.setInsertionOrder(rs.getInt("insertion_order"));
            return result;
        }
    }

    private static class BusinessEntitySnapshotIdMapper implements ParameterizedRowMapper<KeyValue> {

        @Override
        public DefaultKeyValue mapRow(ResultSet rs, int rowNum) throws SQLException {
            DefaultKeyValue result = new DefaultKeyValue();
            result.setKey(Guid.createGuidFromString(rs.getString("command_id")));
            result.setValue(rs.getString("command_type"));
            return result;
        }
    }

    /**
     *
     */
    public BusinessEntitySnapshotDAODbFacadeImpl() {
    }

    @Override
    public List<BusinessEntitySnapshot> getAllForCommandId(Guid commandID) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("command_id", commandID);

        return getCallsHandler().executeReadList("get_entity_snapshot_by_command_id",
                new BusinessEntitySnapshotMapper(),
                parameterSource);
    }

    @Override
    public void removeAllForCommandId(Guid commandID) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("command_id", commandID);
        getCallsHandler().executeModification("delete_entity_snapshot_by_command_id", parameterSource);
    }

    @Override
    public void save(BusinessEntitySnapshot entitySnapshot) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource()
                        .addValue("command_id", entitySnapshot.getCommandId())
                        .addValue("command_type", entitySnapshot.getCommandType())
                        .addValue("entity_id", entitySnapshot.getEntityId())
                        .addValue("entity_type", entitySnapshot.getEntityType())
                        .addValue("entity_snapshot", entitySnapshot.getEntitySnapshot())
                        .addValue("snapshot_class", entitySnapshot.getSnapshotClass())
                        .addValue("snapshot_type", entitySnapshot.getSnapshotType())
                        .addValue("insertion_order", entitySnapshot.getInsertionOrder());
        getCallsHandler().executeModification("insert_entity_snapshot", parameterSource);
    }

    @Override
    public List<KeyValue> getAllCommands() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("get_all_commands",
                new BusinessEntitySnapshotIdMapper(),
                parameterSource);
    }
}
