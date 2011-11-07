package org.ovirt.engine.ui.webadmin.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

public abstract class NullSafeRenderer<T> extends AbstractRenderer<T> {

    @Override
    public String render(T object) {
        return object == null ? "" : renderNullSafe(object);
    }

    protected abstract String renderNullSafe(T object);

}
