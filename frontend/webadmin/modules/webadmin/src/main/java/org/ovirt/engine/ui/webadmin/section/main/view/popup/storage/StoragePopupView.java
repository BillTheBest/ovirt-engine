package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.storage.IStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.renderer.EnumRenderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class StoragePopupView extends AbstractModelBoundPopupView<StorageModel>
        implements StoragePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<StorageModel, StoragePopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, StoragePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    @Path(value = "name.entity")
    EntityModelTextBoxEditor nameEditor;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    ListModelListBoxEditor<Object> datacenterListEditor;

    @UiField(provided = true)
    @Path(value = "availableStorageItems.selectedItem")
    ListModelListBoxEditor<Object> storageTypeListEditor;

    @UiField(provided = true)
    @Path(value = "format.selectedItem")
    ListModelListBoxEditor<Object> formatListEditor;

    @UiField(provided = true)
    @Path(value = "host.selectedItem")
    ListModelListBoxEditor<Object> hostListEditor;

    @Ignore
    @UiField
    FlowPanel specificStorageTypePanel;

    @Inject
    public StoragePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        addStyles();
        Driver.driver.initialize(this);
    }

    @SuppressWarnings("unchecked")
    void initListBoxEditors() {
        datacenterListEditor = new ListModelListBoxEditor<Object>(new AbstractRenderer<Object>() {
            @Override
            public String render(Object object) {
                String formattedString = "";

                if (object != null) {
                    storage_pool storage = (storage_pool) object;

                    // Get formatted storage type and format using Enum renders
                    String storageType = storage.getstorage_pool_type() == StorageType.UNKNOWN ? "" :
                            (new EnumRenderer<StorageType>()).render(storage.getstorage_pool_type());
                    String storageFormatType = storage.getStoragePoolFormatType() == null ? "" :
                            (new EnumRenderer<StorageFormatType>()).render(storage.getStoragePoolFormatType());

                    // Add storage type and format if available
                    if (storageType.length() > 0) {
                        formattedString = " (" + storageType;
                        if (storageFormatType.length() > 0) {
                            formattedString += ", " + storageFormatType;
                        }
                        formattedString += ")";
                    }

                    formattedString = storage.getname() + formattedString;
                }

                return formattedString;
            }
        });

        formatListEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());

        hostListEditor = new ListModelListBoxEditor<Object>(new AbstractRenderer<Object>() {
            @Override
            public String render(Object object) {
                return object == null ? "" : ((VDS) object).getvds_name();
            }
        });

        storageTypeListEditor = new ListModelListBoxEditor<Object>(new AbstractRenderer<Object>() {
            @Override
            public String render(Object object) {
                String formattedString = "";

                if (object != null) {
                    EnumRenderer<StorageType> storageEnumRenderer = new EnumRenderer<StorageType>();
                    EnumRenderer<StorageDomainType> storageDomainEnumRenderer = new EnumRenderer<StorageDomainType>();

                    String storageDomainType = storageDomainEnumRenderer.render(((IStorageModel) object).getRole());
                    String storageType = storageEnumRenderer.render(((IStorageModel) object).getType());

                    formattedString = storageDomainType + " / " + storageType;
                }
                return formattedString;
            }
        });
    }

    void addStyles() {
        storageTypeListEditor.setLabelStyleName(style.label());
        storageTypeListEditor.addContentWidgetStyleName(style.storageContentWidget());
        formatListEditor.setLabelStyleName(style.label());
        formatListEditor.addContentWidgetStyleName(style.formatContentWidget());
    }

    void localize(ApplicationConstants constants) {
        nameEditor.setLabel(constants.storagePopupNameLabel());
        datacenterListEditor.setLabel(constants.storagePopupDataCenterLabel());
        storageTypeListEditor.setLabel(constants.storagePopupStorageTypeLabel());
        formatListEditor.setLabel(constants.storagePopupFormatTypeLabel());
        hostListEditor.setLabel(constants.storagePopupHostLabel());
    }

    @Override
    public void edit(StorageModel object) {
        Driver.driver.edit(object);

        final StorageModel storageModel = object;
        storageModel.getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                // Reveal the appropriate storage view according to the selected storage type
                revealStorageView(storageModel);
            }
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void revealStorageView(StorageModel object) {
        AbstractStorageView storageView = null;

        IStorageModel model = object.getSelectedItem();
        if (model.getType() == StorageType.NFS) {
            storageView = new NfsStorageView();
        } else if (model.getType() == StorageType.LOCALFS) {
            storageView = new LocalStorageView();
        } else if (model.getType() == StorageType.FCP) {
            storageView = new FcpStorageView();
        } else if (model.getType() == StorageType.ISCSI) {
            storageView = new IscsiStorageView();
        }

        // Add the command list to the concrete storage model
        ((Model) model).setCommands(object.getCommands());

        // Clear the current storage view
        specificStorageTypePanel.clear();

        // Add the new storage view and call focus on it if needed
        if (storageView != null) {
            storageView.edit(model);
            specificStorageTypePanel.add(storageView);

            if (!nameEditor.isVisible()) {
                storageView.focus();
            }
        }
    }

    @Override
    public StorageModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focus() {
        nameEditor.setFocus(true);
    }

    interface WidgetStyle extends CssResource {
        String formatContentWidget();

        String storageContentWidget();

        String label();

        String storageTypeLabel();

        String storageDomainTypeLabel();
    }
}
