package org.ovirt.engine.ui.webadmin.widget;

import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Convenience base class for widgets implementing the {@link HasValidation} interface.
 */
public abstract class AbstractValidatedWidget extends Composite implements HasValidation {

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);
        applyCommonValidationStyles();
        markAsValid();
    }

    @Override
    public void markAsValid() {
        applyCommonValidationStyles();
        getValidatedWidgetStyle().setBorderColor("gray");
        getValidatedWidget().setTitle(null);
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        applyCommonValidationStyles();
        getValidatedWidgetStyle().setBorderColor("orange");
        getValidatedWidget().setTitle(getValidationTitle(validationHints));
    }

    protected void applyCommonValidationStyles() {
        getValidatedWidgetStyle().setBorderWidth(1, Unit.PX);
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.SOLID);
    }

    String getValidationTitle(List<String> validationHints) {
        return validationHints != null && validationHints.size() > 0 ? validationHints.get(0) : null;
    }

    protected Style getValidatedWidgetStyle() {
        return getValidatedWidget().getElement().getStyle();
    }

    protected abstract Widget getValidatedWidget();

}
