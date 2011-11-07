package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.view.AbstractPopupView;
import org.ovirt.engine.ui.webadmin.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.webadmin.widget.UiCommandButton;
import org.ovirt.engine.ui.webadmin.widget.dialog.ProgressPopupContent;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for popup views bound to a UiCommon Window model.
 * 
 * @param <T>
 *            Window model type.
 */
public abstract class AbstractModelBoundPopupView<T extends Model> extends AbstractPopupView<SimpleDialogPanel> implements AbstractModelBoundPopupPresenterWidget.ViewDef<T> {

    /**
     * Popup progress indicator widget
     */
    private final ProgressPopupContent progressContent = new ProgressPopupContent();

    /**
     * Actual popup content
     */
    private Widget popupContent;

    /**
     * Popup hash-name
     */
    private String hashName;

    public AbstractModelBoundPopupView(EventBus eventBus, ApplicationResources resources) {
        super(eventBus, resources);
    }

    @Override
    protected void initWidget(SimpleDialogPanel widget) {
        super.initWidget(widget);

        this.popupContent = widget.getContent();
    }

    @Override
    public void setTitle(String title) {
        asWidget().setHeader(new Label(title));
    }

    @Override
    public void setMessage(String message) {
        // No-op, override as necessary
    }

    @Override
    public void setItems(Iterable<?> items) {
        // No-op, override as necessary
    }

    @Override
    public void setHashName(String name) {
        hashName = name;
    }

    @Override
    public HasUiCommandClickHandlers addFooterButton(String label) {
        UiCommandButton button = new UiCommandButton(label);
        asWidget().addFooterButton(button);
        return button;
    }

    @Override
    public void removeButtons() {
        asWidget().removeFooterButtons();
    }

    @Override
    public void startProgress(String progressMessage) {
        progressContent.setProgressMessage(progressMessage);
        asWidget().setContent(progressContent);
    }

    @Override
    public void stopProgress() {
        asWidget().setContent(popupContent);
    }

    protected String getHashName() {
        return hashName;
    }

}
