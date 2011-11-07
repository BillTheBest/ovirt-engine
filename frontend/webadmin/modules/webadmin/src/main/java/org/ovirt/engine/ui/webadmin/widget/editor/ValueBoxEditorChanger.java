package org.ovirt.engine.ui.webadmin.widget.editor;

import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * A {@link ValueBoxEditor} that listens to Value changes
 * 
 * @param <T>
 */
public class ValueBoxEditorChanger<T> extends ValueBoxEditor<T> implements HasValueChangeHandlers<T> {

    /**
     * Create a {@link ValueBoxEditorChanger} for the specified ValueBox
     * 
     * @param valueBox
     * @return
     */
    public static <T> ValueBoxEditorChanger<T> of(ValueBoxBase<T> valueBox) {
        return new ValueBoxEditorChanger<T>(valueBox);
    }

    private final ValueBoxBase<T> peer;

    protected ValueBoxEditorChanger(final ValueBoxBase<T> peer) {
        super(peer);
        this.peer = peer;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        peer.fireEvent(event);

    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
        return peer.addValueChangeHandler(handler);
    }

}
