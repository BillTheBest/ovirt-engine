package org.ovirt.engine.ui.webadmin.widget;

import org.ovirt.engine.ui.webadmin.widget.editor.EditorWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for validated widgets that have a label associated with them.
 * 
 * @param <W>
 *            Content widget type.
 */
public abstract class AbstractValidatedWidgetWithLabel<T, W extends EditorWidget<T, ?>> extends AbstractValidatedWidget implements HasLabel, HasEnabled, HasAccess, HasAllKeyHandlers, Focusable {

    interface WidgetUiBinder extends UiBinder<Widget, AbstractValidatedWidgetWithLabel<?, ?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {

        String labelEnabled();

        String labelDisabled();

    }

    private final W contentWidget;

    @UiField
    HTMLPanel wrapperPanel;

    @UiField
    LabelElement labelElement;

    @UiField
    SimplePanel contentWidgetContainer;

    @UiField
    Style style;

    public AbstractValidatedWidgetWithLabel(W contentWidget) {
        this.contentWidget = contentWidget;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected void initWidget(Widget wrapperWidget) {
        super.initWidget(wrapperWidget);
        contentWidgetContainer.setWidget(contentWidget);

        // Adjust content widget width
        contentWidget.asWidget().setWidth("100%");

        // Connect label with content widget for better accessibility
        labelElement.setHtmlFor(getContentWidgetId());
    }

    protected String getContentWidgetId() {
        Element contentWidgetElement = contentWidget.asWidget().getElement();

        if (contentWidgetElement.getId() == null || contentWidgetElement.getId().isEmpty()) {
            contentWidgetElement.setId(DOM.createUniqueId());
        }

        return contentWidgetElement.getId();
    }

    protected W getContentWidget() {
        return contentWidget;
    }

    protected SimplePanel getContentWidgetContainer() {
        return contentWidgetContainer;
    }

    protected LabelElement getLabelElement() {
        return labelElement;
    }

    @Override
    protected Widget getValidatedWidget() {
        return getContentWidget().asWidget();
    }

    @Override
    public String getLabel() {
        return labelElement.getInnerText();
    }

    @Override
    public void setLabel(String label) {
        labelElement.setInnerText(label);
    }

    @Override
    public boolean isAccessible() {
        return wrapperPanel.isVisible();
    }

    @Override
    public void setAccessible(boolean accessible) {
        wrapperPanel.setVisible(accessible);
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return contentWidget.addKeyDownHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return contentWidget.addKeyPressHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return contentWidget.addKeyUpHandler(handler);
    }

    @Override
    public int getTabIndex() {
        return contentWidget.getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        contentWidget.setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        contentWidget.setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        contentWidget.setTabIndex(index);
    }

    @Override
    public boolean isEnabled() {
        return contentWidget.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        contentWidget.setEnabled(enabled);

        if (enabled) {
            labelElement.replaceClassName(style.labelDisabled(), style.labelEnabled());
        } else {
            labelElement.replaceClassName(style.labelEnabled(), style.labelDisabled());
        }
    }

    public void addContentWidgetStyleName(String styleName) {
        contentWidgetContainer.addStyleName(styleName);
    }

    public void setLabelStyleName(String styleName) {
        labelElement.setClassName(styleName);
    }

    public void addLabelStyleName(String styleName) {
        labelElement.addClassName(styleName);
    }
}
