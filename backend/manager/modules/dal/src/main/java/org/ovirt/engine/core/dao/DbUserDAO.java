package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.user_sessions;

/**
 * <code>DbUserDAO</code> defines a type for performing CRUD operations on instances of {@link DbUser}.
 *
 *
 */
public interface DbUserDAO extends DAO {

    /**
     * Retrieves the suser with the specified id.
     *
     * @param id
     *            the id
     * @return the user, or <code>null</code> if the id was invalid
     */
    DbUser get(Guid id);

    /**
     * Retrieves a user by username.
     *
     * @param username
     *            the username
     * @return the user
     */
    DbUser getByUsername(String username);

    /**
     * Retrieves all users associated with the specified virtual machine.
     *
     * @param id
     *            the VM id
     * @return the list of users
     */
    List<DbUser> getAllForVm(Guid id);

    /**
     * Retrieves all users associated with the specified virtual machine id.
     *
     * @param vmid
     *            the virtual machine id
     * @return the list of users
     */
    List<DbUser> getAllTimeLeasedUsersForVm(int vmid);

    /**
     * Retrieves all users who meet some arbitrary SQL query.
     *
     * @param query
     *            the query
     * @return the list of users
     */
    List<DbUser> getAllWithQuery(String query);

    /**
     * Retrieves all defined used.
     *
     * @return the collection of all users
     */
    List<DbUser> getAll();

    /**
     * Retrieves all sessions.
     *
     * @return the list of sessions
     */
    List<user_sessions> getAllUserSessions();

    /**
     * Saves the user.
     *
     * @param user
     *            the user
     */
    void save(DbUser user);

    /**
     * Saves the specified user session. Admin users, though, are not saved.
     *
     * @param session
     *            the session
     */
    void saveSession(user_sessions session);

    /**
     * Updates the specified user in the database.
     *
     * @param user
     *            the user
     */
    void update(DbUser user);

    /**
     * Removes the user with the specified id.
     *
     * @param user
     *            the user id
     */
    void remove(Guid user);

    /**
     * Removes the specified session for the specified user.
     *
     * @param session
     *            the session
     * @param user
     *            the user
     */
    void removeUserSession(String session, Guid user);

    /**
     * Removes the specified user sessions.
     *
     * @param sessionmap
     *            the session map
     */
    void removeUserSessions(Map<String, Guid> sessionmap);

    /**
     * Removes all user sessions.
     */
    void removeAllSessions();
}
