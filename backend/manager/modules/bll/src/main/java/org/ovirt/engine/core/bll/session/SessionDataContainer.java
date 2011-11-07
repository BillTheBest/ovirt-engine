package org.ovirt.engine.core.bll.session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;

public class SessionDataContainer {

    private ConcurrentMap<String, Map<String, Object>> oldContext =
            new ConcurrentHashMap<String, Map<String, Object>>();
    private ConcurrentMap<String, Map<String, Object>> newContext =
            new ConcurrentHashMap<String, Map<String, Object>>();

    private static SessionDataContainer dataProviderInstance = new SessionDataContainer();

    private SessionDataContainer() {
    }

    public static SessionDataContainer getInstance() {
        return dataProviderInstance;
    }

    /**
     * Get data of session attached to current thread by key
     *
     * @param key
     *            - the internal key
     * @return
     */
    public final Object GetData(String key) {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (sessionId == null) {
            return null;
        }
        return GetData(sessionId, key);
    }

    /**
     * This method will set user by session which is attached to thread
     * @param key
     * @param value
     * @return At case when session is attached to thread will be return a true value
     */
    public final boolean SetData(String key, Object value) {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (StringHelper.isNullOrEmpty(sessionId)) {
            return false;
        }
        SetData(sessionId, key, value);
        return true;
    }

    /**
     * Get data by session and internal key
     *
     * @param sessionId
     *            - id of session
     * @param key
     *            - the internal key
     * @return
     */
    public final Object GetData(String sessionId, String key) {
        return GetData(sessionId, key, true);
    }

    /**
     * Get data by session and internal key
     *
     * @param sessionId
     *            - id of session
     * @param key
     *            - the internal key
     * @param refresh
     *            - if perform refresh of session
     * @return
     */
    public final Object GetData(String sessionId, String key, boolean refresh) {
        Map<String, Object> currentContext = null;
        if ((currentContext = newContext.get(sessionId)) != null) {
            return currentContext.get(key);
        }
        if (refresh) {
            if((currentContext = oldContext.remove(sessionId)) != null) {
                newContext.put(sessionId, currentContext);
            }
        } else {
            currentContext = oldContext.get(sessionId);
        }
        if (currentContext != null) {
            return currentContext.get(key);
        }
        return null;
    }

    public final void SetData(String sessionId, String key, Object value) {
        // Try to get value from new generation
        Map<String, Object> context = newContext.get(sessionId);
        if (context == null) {
            // Try to get value from old generation
            context = oldContext.get(sessionId);
            if (context == null) {
                context = new ConcurrentHashMap<String, Object>();
            }
            // Put a value to new generation , for case that other thread put
            // value before current thread , his value will be used
            Map<String, Object> oldSessionContext = newContext.putIfAbsent(sessionId, context);
            if (oldSessionContext != null) {
                context = oldSessionContext;
            }
        }
        context.put(key, value);
    }

    /**
     * This method will move all newGeneration to old generation and old
     * generation will be cleaned automaticly
     *
     * @return
     */
    private Map<String, Map<String, Object>> deleteOldGeneration() {
        Map<String, Map<String, Object>> temp = oldContext;
        oldContext = newContext;
        newContext = new ConcurrentHashMap<String, Map<String, Object>>();
        return temp;
    }

    /**
     * Remove the cached data of session The sessionId is retrieved from
     * ThreadLocal
     */
    public final void removeSession() {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (sessionId != null) {
            removeSession(sessionId);
        }
    }

    public boolean containsKey(String key) {
        String sessionId = ThreadLocalParamsContainer.getHttpSessionId();
        if (sessionId != null) {
            return oldContext.containsKey(key) || newContext.containsKey(key);
        }
        return false;
    }

    /**
     * Remove the cached data of current session
     *
     * @param sessionId
     *            - id of current session
     */
    public final void removeSession(String sessionId) {
        oldContext.remove(sessionId);
        newContext.remove(sessionId);
    }

    /**
     * Will run the process of cleaning expired sessions. At case when session
     * connected to user, it will be also deleted from DB
     */
    @OnTimerMethodAnnotation("cleanExpiredUsersSessions")
    public final void cleanExpiredUsersSessions() {
        Map<String, Map<String, Object>> map = getInstance().deleteOldGeneration();
        if (map != null && !map.isEmpty()) {
            Map<String, Guid> userSessionMap = new HashMap<String, Guid>();
            for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
                VdcUser user = (VdcUser) entry.getValue().get("VdcUser");
                if (user != null) {
                    userSessionMap.put(entry.getKey(), user.getUserId());
                }
            }
            if (!userSessionMap.isEmpty()) {
                DbFacade.getInstance().getDbUserDAO().removeUserSessions(userSessionMap);
            }
        }
    }
}
