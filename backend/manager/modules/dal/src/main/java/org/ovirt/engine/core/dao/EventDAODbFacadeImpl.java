package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.common.businessentities.event_map;
import org.ovirt.engine.core.common.businessentities.event_notification_methods;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>EventSubscriberDAODbFacadeImpl</code> provides an implementation of {@link EventSubscriberDAO} that uses the
 * refactored {@link DbFacade} code.
 */
public class EventDAODbFacadeImpl extends BaseDAODbFacade implements EventDAO {

    @SuppressWarnings("unchecked")
    @Override
    public List<event_subscriber> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<event_subscriber> mapper = new ParameterizedRowMapper<event_subscriber>() {
            @Override
            public event_subscriber mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                event_subscriber entity = new event_subscriber();
                entity.setevent_up_name(rs.getString("event_up_name"));
                entity.setmethod_id(rs.getInt("method_id"));
                entity.setmethod_address(rs.getString("method_address"));
                entity.setsubscriber_id(Guid.createGuidFromString(rs
                        .getString("subscriber_id")));
                entity.settag_name(rs.getString("tag_name"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromevent_subscriber", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<event_subscriber> getAllForSubscriber(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("subscriber_id", id);

        ParameterizedRowMapper<event_subscriber> mapper = new ParameterizedRowMapper<event_subscriber>() {
            @Override
            public event_subscriber mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                event_subscriber entity = new event_subscriber();
                entity.setevent_up_name(rs.getString("event_up_name"));
                entity.setmethod_id(rs.getInt("method_id"));
                entity.setmethod_address(rs.getString("method_address"));
                entity.setsubscriber_id(Guid.createGuidFromString(rs
                        .getString("subscriber_id")));
                entity.settag_name(rs.getString("tag_name"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("Getevent_subscriberBysubscriber_id", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<event_notification_methods> getAllEventNotificationMethods() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<event_notification_methods> mapper =
                new ParameterizedRowMapper<event_notification_methods>() {
                    @Override
                    public event_notification_methods mapRow(ResultSet rs, int rowNum) throws SQLException {
                        event_notification_methods entity = new event_notification_methods();
                        entity.setmethod_id(rs.getInt("method_id"));
                        entity.setmethod_type(EventNotificationMethods.EMAIL);
                        return entity;
                    }
                };

        return getCallsHandler().executeReadList("GetAllFromevent_notification_methods",mapper,parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<event_notification_methods> getEventNotificationMethodsById(int method_id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("method_id", method_id);

        ParameterizedRowMapper<event_notification_methods> mapper =
                new ParameterizedRowMapper<event_notification_methods>() {
                    @Override
                    public event_notification_methods mapRow(ResultSet rs, int rowNum) throws SQLException {
                        event_notification_methods entity = new event_notification_methods();
                        entity.setmethod_id(rs.getInt("method_id"));
                        entity.setmethod_type(EventNotificationMethods.EMAIL);
                        return entity;
                    }
                };

        return getCallsHandler().executeReadList("GetEventNotificationMethodById", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<event_notification_methods> getEventNotificationMethodsByType(String method_type) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("method_type", method_type);

        ParameterizedRowMapper<event_notification_methods> mapper =
                new ParameterizedRowMapper<event_notification_methods>() {
                    @Override
                    public event_notification_methods mapRow(ResultSet rs, int rowNum) throws SQLException {
                        event_notification_methods entity = new event_notification_methods();
                        entity.setmethod_id(rs.getInt("method_id"));
                        entity.setmethod_type(EventNotificationMethods.EMAIL);
                        return entity;
                    }
                };

        return getCallsHandler().executeReadList("GetEventNotificationMethodByType", mapper, parameterSource);
    }

    @Override
    public void subscribe(event_subscriber subscriber) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("event_up_name", subscriber.getevent_up_name())
                .addValue("method_id", subscriber.getmethod_id())
                .addValue("method_address", subscriber.getmethod_address())
                .addValue("subscriber_id", subscriber.getsubscriber_id())
                .addValue("tag_name", subscriber.gettag_name());

        getCallsHandler().executeModification("Insertevent_subscriber", parameterSource);
    }

    @Override
    public void update(event_subscriber subscriber, int oldMethodId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("event_up_name", subscriber.getevent_up_name())
                .addValue("old_method_id", oldMethodId)
                .addValue("new_method_id", subscriber.getmethod_id())
                .addValue("subscriber_id", subscriber.getsubscriber_id());

        getCallsHandler().executeModification("Updateevent_subscriber", parameterSource);
    }

    @Override
    public void unsubscribe(event_subscriber subscriber) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("event_up_name", subscriber.getevent_up_name())
                .addValue("method_id", subscriber.getmethod_id())
                .addValue("subscriber_id", subscriber.getsubscriber_id())
                .addValue("tag_name", subscriber.gettag_name());

        getCallsHandler().executeModification("Deleteevent_subscriber", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<event_map> getAllEventMaps() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<event_map> mapper = new ParameterizedRowMapper<event_map>() {
            @Override
            public event_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                event_map entity = new event_map();
                entity.setevent_up_name(rs.getString("event_up_name"));
                entity.setevent_down_name(rs.getString("event_down_name"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromevent_map", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<event_map> getEventMapByName(String event_up_name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("event_name", event_up_name);

        ParameterizedRowMapper<event_map> mapper = new ParameterizedRowMapper<event_map>() {
            @Override
            public event_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                event_map entity = new event_map();
                entity.setevent_up_name(rs.getString("event_up_name"));
                entity.setevent_down_name(rs.getString("event_down_name"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetEventMapByName", mapper, parameterSource);
    }
}

