package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.ovirt.engine.core.common.businessentities.bookmarks;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;

/**
 * <code>BookmarkDAODbFacadeImpl</code> provides a concrete implementation of <code>BookmarkDAO</code> that uses
 * pre-existing code from <code>DbFacade</code>
 *
 *
 */
public class BookmarkDAODbFacadeImpl extends BaseDAODbFacade implements BookmarkDAO {
    private class BookmarkRowMapper implements
            ParameterizedRowMapper<bookmarks> {
        @Override
        public bookmarks mapRow(ResultSet rs, int rowNum) throws SQLException {
            bookmarks entity = new bookmarks();
            entity.setbookmark_id(new Guid(rs
                    .getString("bookmark_id")));
            entity.setbookmark_name(rs.getString("bookmark_name"));
            entity.setbookmark_value(rs.getString("bookmark_value"));
            return entity;
        }
    }

    private class BookmarkSqlParameterSource extends
            CustomMapSqlParameterSource {
        public BookmarkSqlParameterSource(bookmarks bookmark) {
            super(dialect);
            addValue("bookmark_id", bookmark.getbookmark_id());
            addValue("bookmark_name", bookmark.getbookmark_name());
            addValue("bookmark_value", bookmark.getbookmark_value());
        }

        public BookmarkSqlParameterSource() {
            super(dialect);
        }

        public BookmarkSqlParameterSource(Guid id) {
            super(dialect);
            addValue("bookmark_id", id);
        }

        public BookmarkSqlParameterSource(String name) {
            super(dialect);
            addValue("bookmark_name", name);
        }
    }

    @Override
    public bookmarks get(Guid id) {
        MapSqlParameterSource parameterSource = new BookmarkSqlParameterSource(
                id);
        return getCallsHandler().executeRead("GetBookmarkBybookmark_id", new BookmarkRowMapper(), parameterSource);
    }

    @Override
    public bookmarks getByName(String name) {
        MapSqlParameterSource parameterSource = new BookmarkSqlParameterSource(
                name);
        return getCallsHandler().executeRead("GetBookmarkBybookmark_name", new BookmarkRowMapper(), parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<bookmarks> getAll() {
        MapSqlParameterSource parameterSource = new BookmarkSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromBookmarks", new BookmarkRowMapper(), parameterSource);
    }

    @Override
    public void save(bookmarks bookmark) {
        MapSqlParameterSource parameterSource = new BookmarkSqlParameterSource(
                bookmark);

        getCallsHandler().executeModification("InsertBookmark", parameterSource);
    }

    @Override
    public void update(bookmarks bookmark) {
        MapSqlParameterSource parameterSource = new BookmarkSqlParameterSource(
                bookmark);

        getCallsHandler().executeModification("UpdateBookmark", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = new BookmarkSqlParameterSource(
                id);

        getCallsHandler().executeModification("DeleteBookmark", parameterSource);
    }
}
