package org.ovirt.engine.ui.webadmin.widget.renderer;

public class StringRenderer<T> extends NullSafeRenderer<T> {

    @Override
    protected String renderNullSafe(T object) {
        return object.toString();
    }

}
