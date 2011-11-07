package org.ovirt.engine.ui.webadmin.editor;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.IEventListener;

/**
 * A Map of {@link IEventListener}s - used by the Editor Driver to call the relevant Listener when a change has
 * occurred.
 */
public class UiCommonListenerMap {
    private final Map<String, IEventListener> listenerMap;

    public UiCommonListenerMap() {
        listenerMap = new HashMap<String, IEventListener>();
    }

    /**
     * Add a Listener to the map
     * 
     * @param name
     *            The property name (i.e. "DefinedMemory")
     * @param type
     *            The event type (i.e. "PropertyChanged")
     * @param listener
     *            The Listener
     */
    public void addListener(String name, String type, IEventListener listener) {
        listenerMap.put(getKey(name, type), listener);
    }

    /**
     * Invoke a registered Listener
     * 
     * @param name
     *            The property name (i.e. "DefinedMemory")
     * @param type
     *            The event type (i.e. "PropertyChanged")
     */
    public void callListener(String name, String type) {
        String key = getKey(name, type);
        if (listenerMap.containsKey(key)) {
            listenerMap.get(key).eventRaised(null, null, null);
        }
    }

    private String getKey(String name, String type) {
        return name + "_" + type;
    }

}
