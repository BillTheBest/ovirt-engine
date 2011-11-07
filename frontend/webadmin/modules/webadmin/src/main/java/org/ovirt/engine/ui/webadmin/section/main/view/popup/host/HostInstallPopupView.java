package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.uicommonweb.models.hosts.InstallModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInstallPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.renderer.NullSafeRenderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

/**
 * This is the dialog used to re-install a host.
 * 
 * Take into account that it can be used both for a normal host an also for an
 * bare metal hypervisor. In the first case it will ask for the root password and
 * in the second it will as for the location of the ISO image of the hypervisor.
 */
public class HostInstallPopupView extends AbstractModelBoundPopupView<InstallModel> implements HostInstallPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<InstallModel, HostInstallPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostInstallPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "rootPassword.entity")
    EntityModelPasswordBoxEditor passwordEditor;

    @UiField(provided = true)
    @Path(value = "oVirtISO.selectedItem")
    ListModelListBoxEditor<Object> isoEditor;

    @Inject
    public HostInstallPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
    }

    void initListBoxEditors() {
        isoEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return object.toString();
            }
        });
    }

    void localize(ApplicationConstants constants) {
        passwordEditor.setLabel(constants.hostInstallPasswordLabel());
        isoEditor.setLabel(constants.hostInstallIsoLabel());
    }

    @Override
    public void edit(final InstallModel model) {
        Driver.driver.edit(model);
    }

    @Override
    public InstallModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focus() {
        // We are trusting the model to decide which of the two alternatives of
        // the dialog (for a normal host or for a bare metal hypervisor):
        if (passwordEditor.isAccessible()) {
            passwordEditor.setFocus(true);
        }
        if (isoEditor.isAccessible()) {
            isoEditor.setFocus(true);
        }
    }

}
