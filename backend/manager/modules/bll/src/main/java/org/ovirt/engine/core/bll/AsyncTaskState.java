package org.ovirt.engine.core.bll;

public enum AsyncTaskState {
    Initializing,
    WaitForPoll,
    Polling,
    Ended,
    AttemptingEndAction,
    ClearFailed,
    Cleared;

    public int getValue() {
        return this.ordinal();
    }

    public static AsyncTaskState forValue(int value) {
        return values()[value];
    }
}
