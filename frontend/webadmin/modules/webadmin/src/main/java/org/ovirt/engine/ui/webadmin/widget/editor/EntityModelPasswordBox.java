package org.ovirt.engine.ui.webadmin.widget.editor;

import com.google.gwt.dom.client.Document;
import com.google.gwt.editor.ui.client.adapters.ValueBoxEditor;
import com.google.gwt.user.client.ui.ValueBoxBase;

public class EntityModelPasswordBox extends ValueBoxBase<Object> implements EditorWidget<Object, ValueBoxEditor<Object>> {

    private ObservableValueBoxEditor editor;

    public EntityModelPasswordBox() {
        super(Document.get().createPasswordInputElement(), new EntityModelRenderer(), new EntityModelParser());
    }

    @Override
    public ValueBoxEditor<Object> asEditor() {
        if (editor == null) {
            editor = ObservableValueBoxEditor.of(this);
        }
        return editor;
    }

}
