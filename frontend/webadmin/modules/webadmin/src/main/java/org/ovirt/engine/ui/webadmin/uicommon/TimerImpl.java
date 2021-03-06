package org.ovirt.engine.ui.webadmin.uicommon;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.ui.uicommonweb.ITimer;
import org.ovirt.engine.ui.uicommonweb.ProvideTickEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;

public class TimerImpl implements ITimer {

    private Event tickEvent;

    private int interval;

    private Timer timer = TimerFactory.factoryTimer("UICommon Timer", new Timer() {
        @Override
        public void run() {
            GWT.log("Timer execution");
            tickEvent.raise(this, EventArgs.Empty);
        }
    });

    public TimerImpl() {
        tickEvent = new Event(ProvideTickEvent.Definition);
    }

    @Override
    public int getInterval() {
        return 0;
    }

    @Override
    public void setInterval(int value) {
        GWT.log("Timer interval set to " + value + " by UICommon");
        interval = value;
    }

    @Override
    public void start() {
        GWT.log("Timer started by UICommon");
        timer.scheduleRepeating(interval);
    }

    @Override
    public void stop() {
        GWT.log("Timer stopped by UICommon");
        timer.cancel();
    }

    @Override
    public Event getTickEvent() {
        return tickEvent;
    }

}
