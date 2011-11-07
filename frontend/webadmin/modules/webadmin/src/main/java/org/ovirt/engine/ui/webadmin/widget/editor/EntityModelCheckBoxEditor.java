package org.ovirt.engine.ui.webadmin.widget.editor;

import org.ovirt.engine.ui.webadmin.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.webadmin.widget.Align;
import org.ovirt.engine.ui.webadmin.widget.table.TakesValueWithChangeHandlersEditor;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.user.client.ui.CheckBox;

/**
 * Composite Editor that uses {@link EntityModelCheckBox}.
 */
public class EntityModelCheckBoxEditor extends AbstractValidatedWidgetWithLabel<Object, EntityModelCheckBox>
        implements IsEditor<WidgetWithLabelEditor<Object, TakesValueWithChangeHandlersEditor<Object>, EntityModelCheckBoxEditor>> {

    private final WidgetWithLabelEditor<Object, TakesValueWithChangeHandlersEditor<Object>, EntityModelCheckBoxEditor> editor;

    private final boolean useCheckBoxWidgetLabel;

    public EntityModelCheckBoxEditor() {
        this(Align.LEFT);
    }

    public EntityModelCheckBoxEditor(Align labelAlign) {
        super(new EntityModelCheckBox());
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
        this.useCheckBoxWidgetLabel = labelAlign == Align.RIGHT;

        // In case we use CheckBox widget label instead of declared LabelElement,
        // align content widget container to the left and hide the LabelElement
        if (useCheckBoxWidgetLabel) {
            getContentWidgetContainer().getElement().getStyle().setFloat(Float.LEFT);
            getLabelElement().getStyle().setDisplay(Display.NONE);
        }
    }

    public CheckBox asCheckBox() {
        return getContentWidget().asCheckBox();
    }

    @Override
    public WidgetWithLabelEditor<Object, TakesValueWithChangeHandlersEditor<Object>, EntityModelCheckBoxEditor> asEditor() {
        return editor;
    }

    @Override
    protected void applyCommonValidationStyles() {
        // Suppress check box styling, as different browsers behave
        // differently when styling check box input elements
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.NONE);
    }

    @Override
    protected String getContentWidgetId() {
        // Actual check box input element is the first child of CheckBox element
        Node input = asCheckBox().getElement().getChild(0);
        return Element.as(input).getId();
    }

    @Override
    public String getLabel() {
        if (useCheckBoxWidgetLabel) {
            return asCheckBox().getText();
        } else {
            return super.getLabel();
        }
    }

    @Override
    public void setLabel(String label) {
        if (useCheckBoxWidgetLabel) {
            asCheckBox().setText(label);
        } else {
            super.setLabel(label);
        }
    }

}
