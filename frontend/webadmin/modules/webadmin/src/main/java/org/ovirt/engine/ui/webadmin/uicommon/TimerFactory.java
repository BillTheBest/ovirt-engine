package org.ovirt.engine.ui.webadmin.uicommon;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;

/**
 * Not really a factory since it doesn't actually generate a timer but rather registers it in a list, this is done out
 * of convenience instead of having to instantiate a timer and then invoke addTimerToList on a static class.
 */
public abstract class TimerFactory {

    private static Map<String, Timer> timerList = new HashMap<String, Timer>();

    public static Timer factoryTimer(String timerName, Timer timer) {
        GWT.log("Adding timer '" + timerName + "' to the timers list");
        timerList.put(timerName, timer);
        return timer;
    }

    public static void cancelAllTimers() {
        for (String name : timerList.keySet()) {
            GWT.log("Cancelling the timer '" + name + "'");
            timerList.get(name).cancel();
        }
    }

    public static void cancelTimer(String timerName) {
        for (String name : timerList.keySet()) {
            if (name.equals(timerName)) {
                GWT.log("Cancelling the timer '" + name + "'");
                timerList.get(name).cancel();
            }
        }
    }

}
