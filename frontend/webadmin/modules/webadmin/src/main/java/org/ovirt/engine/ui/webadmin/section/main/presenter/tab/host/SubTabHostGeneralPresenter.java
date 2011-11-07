package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.HostSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.tab.ModelBoundTabData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabHostGeneralPresenter extends AbstractSubTabPresenter<VDS, HostListModel, HostGeneralModel, SubTabHostGeneralPresenter.ViewDef, SubTabHostGeneralPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.hostGeneralSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostGeneralPresenter> {
        // Nothing.
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
        /**
         * Clear all the alerts currently displayed in the alerts panel
         * of the host.
         */
        void clearAlerts();

        /**
         * Displays a new alert in the alerts panel of the host.
         *
         * @param widget the widget used to display the alert, usually just a
         *   text label, but can also be a text label with a link to an action
         *   embedded
         */
        void addAlert(Widget widget);
    }

    // We need this to get the text of the alert messages:
    private ApplicationMessages messages;

    @TabInfo(container = HostSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().hostGeneralSubTabLabel(), 0,
                ginjector.getSubTabHostGeneralModelProvider());
    }

    @Inject
    public SubTabHostGeneralPresenter(
            final EventBus eventBus,
            final ViewDef view,
            final ProxyDef proxy,
            final PlaceManager placeManager,
            final DetailModelProvider<HostListModel, HostGeneralModel> modelProvider)
    {
        // Call the parent constructor:
        super(eventBus, view, proxy, placeManager, modelProvider);

        // Inject a reference to the messages:
        messages = ClientGinjectorProvider.instance().getApplicationMessages();

        // Initialize the list of alerts:
        final HostGeneralModel model = modelProvider.getModel();
        updateAlerts(view, model);

        // Listen for changes in the properties of the model in order
        // to update the alerts panel:
        model.getPropertyChangedEvent().addListener(
            new IEventListener() {
                public void eventRaised(Event ev, Object sender, EventArgs args) {
                    if (args instanceof PropertyChangedEventArgs) {
                        PropertyChangedEventArgs changedArgs = (PropertyChangedEventArgs) args;
                        if (changedArgs.PropertyName.contains("Alert")) {
                            updateAlerts(view, model);
                        }
                    }
                }
            }
        );
    }

    /**
     * Review the model and if there are alerts add them to the view.
     *
     * @param view the view where alerts should be added
     * @param model the model to review
     */
    private void updateAlerts(final ViewDef view, final HostGeneralModel model) {
        // Clear all the alerts:
        view.clearAlerts();

        // Review the alerts and add those that are active:
        if (model.getHasUpgradeAlert()) {
            addTextAlert(view, messages.hostHasUpgradeAlert());
        }
        if (model.getHasReinstallAlertNonResponsive()) {
            addTextAlert(view, messages.hostHasReinstallAlertNonResponsive());
        }
        if (model.getHasReinstallAlertInstallFailed()) {
            addTextAndLinkAlert(view, messages.hostHasReinstallAlertInstallFailed(), model.getInstallCommand());
        }
        if (model.getHasReinstallAlertMaintenance()) {
            addTextAndLinkAlert(view, messages.hostHasReinstallAlertMaintenance(), model.getInstallCommand());
        }
        if (model.getHasNICsAlert()) {
            addTextAndLinkAlert(view, messages.hostHasNICsAlert(), model.getSaveNICsConfigCommand());
        }
        if (model.getHasManualFenceAlert()) {
            addTextAlert(view, messages.hostHasManualFenceAlert());
        }
        if (model.getHasNoPowerManagementAlert()) {
            addTextAndLinkAlert(view, messages.hostHasNoPowerManagementAlert(), model.getEditHostCommand());
        }
        if (model.getNonOperationalReasonEntity() != null) {
            addTextAlert(view, model.getNonOperationalReasonEntity().toString());
        }
    }

    /**
     * Create a widget containing text and add it to the alerts panel of
     * the host.
     *
     * @param view the view where the alert should be added
     * @param text the text content of the alert
     */
    private void addTextAlert(final ViewDef view, final String text) {
        final Label label = new Label(text);
        view.addAlert(label);
    }

    /**
     * Create a widget containing text and a link that triggers the execution
     * of a command.
     *
     * @param view the view where the alert should be added
     * @param text the text content of the alert
     * @param command the command that should be executed when the link is
     *   clicked
     */
    private void addTextAndLinkAlert(final ViewDef view, final String text, final UICommand command) {
        // Find the open and close positions of the link within the message:
        final int openIndex = text.indexOf("<a>");
        final int closeIndex = text.indexOf("</a>");
        if (openIndex == -1 || closeIndex == -1 || closeIndex < openIndex) {
            return;
        }

        // Extract the text before, inside and after the tags:
        final String beforeText = text.substring(0, openIndex);
        final String betweenText = text.substring(openIndex + 3, closeIndex);
        final String afterText = text.substring(closeIndex + 4);

        // Create a flow panel containing the text and the link:
        final FlowPanel alertPanel = new FlowPanel();

        // Create the label for the text before the tag:
        final Label beforeLabel = new Label(beforeText);
        beforeLabel.getElement().getStyle().setProperty("display", "inline");
        alertPanel.add(beforeLabel);

        // Create the anchor:
        final Anchor betweenAnchor = new Anchor(betweenText);
        betweenAnchor.getElement().getStyle().setProperty("display", "inline");
        alertPanel.add(betweenAnchor);

        // Add a listener to the anchor so that the command is executed when
        // it is clicked:
        betweenAnchor.addClickHandler(
            new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    command.Execute();
                }
            }
        );

        // Create the label for the text after the tag:
        final Label afterLabel = new Label(afterText);
        afterLabel.getElement().getStyle().setProperty("display", "inline");
        alertPanel.add(afterLabel);

        // Add the alert to the view:
        view.addAlert(alertPanel);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, HostSubTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.hostMainTabPlace);
    }

    @ProxyEvent
    public void onHostSelectionChange(HostSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
