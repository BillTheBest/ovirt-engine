package org.ovirt.engine.ui.webadmin.widget.editor;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

/**
 * Composite Editor that uses {@link EntityModelTextBox}.
 */
public class EntityModelTextBoxEditor extends AbstractValueBoxWithLabelEditor<Object, EntityModelTextBox> {

    public EntityModelTextBoxEditor() {
        super(new EntityModelTextBox());
    }

    public EntityModelTextBoxEditor(Renderer<Object> renderer, Parser<Object> parser) {
        super(new EntityModelTextBox(renderer, parser));
    }
}
