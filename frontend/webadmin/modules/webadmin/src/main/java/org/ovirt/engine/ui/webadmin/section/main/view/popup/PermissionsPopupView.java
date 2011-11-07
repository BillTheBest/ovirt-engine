package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.webadmin.widget.UiCommandButton;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.TextBoxChanger;
import org.ovirt.engine.ui.webadmin.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.webadmin.widget.table.column.EntityModelTextColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.inject.Inject;

public class PermissionsPopupView extends AbstractModelBoundPopupView<AdElementListModel> implements PermissionsPopupPresenterWidget.ViewDef {
    interface Driver extends SimpleBeanEditorDriver<AdElementListModel, PermissionsPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, PermissionsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    UiCommandButton searchButton;

    @UiField(provided = true)
    @Path("domain.selectedItem")
    ListModelListBoxEditor<Object> domainSelection;

    @UiField
    @Ignore
    Label roleToAssignLabel;
    
    @UiField(provided = true)
    @Path("role.selectedItem")
    ListModelListBoxEditor<Object> roleSelection;

    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel> searchItems;
    
    @UiField
    @Ignore
    RadioButton everyoneRadio;
    
    @UiField
    @Ignore
    RadioButton specificUserOrGroupRadio;

    @UiField
    @Path("searchString")
    TextBoxChanger searchStringEditor;

    @Inject
    public PermissionsPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        searchItems = new EntityModelCellTable<ListModel>(true);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initTable();
        specificUserOrGroupRadio.setValue(true);
        everyoneRadio.setValue(false);
        localize(constants);
        Driver.driver.initialize(this);
    }

    private void initListBoxEditors() {
        domainSelection = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return object.toString();
            }
        });

        roleSelection = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((roles) object).getname();
            }
        });
    }

    private void initTable() {
        // Table Entity Columns
        searchItems.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                if (model.getEntity() instanceof DbUser)
                    return ((DbUser)model.getEntity()).getname();
                else {
                    Window.alert("shit: " + model.getEntity().toString());
                    return model.getEntity().toString();
                }
            }
        }, "First Name");

        searchItems.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                if (model.getEntity() instanceof DbUser)
                    return ((DbUser)model.getEntity()).getsurname();
                else {
                    return "";
                }
            }
        }, "Last Name");

        searchItems.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                if (model.getEntity() instanceof DbUser)
                    return ((DbUser)model.getEntity()).getusername();
                else {
                    return "";
                }
            }
        }, "User Name");
    }

    void localize(ApplicationConstants constants) {
        searchButton.setLabel("GO");
    }

    @Override
    public void edit(final AdElementListModel object) {
        searchItems.setRowData(new ArrayList<EntityModel>());
        searchItems.edit(object);
        Driver.driver.edit(object);
    }

    @Override
    public AdElementListModel flush() {
        searchItems.flush();
        return Driver.driver.flush();
    }

    @Override
    public void focus() {
        searchStringEditor.setFocus(true);
    }

    @Override
    public HasUiCommandClickHandlers getSearchButton() {
        return searchButton;
    }

    @Override
    public HasKeyPressHandlers getKeyPressSearchInputBox() {
        return searchStringEditor;
    }

    @Override
    public HasClickHandlers getEveryoneRadio() {
        return everyoneRadio;
    }

    @Override
    public HasClickHandlers getSpecificUserOrGroupRadio() {
        return specificUserOrGroupRadio;
    }

    @Override
    public void changeStateOfElementsWhenAccessIsForEveryone(boolean isEveryone) {
        domainSelection.setEnabled(!isEveryone);
        searchStringEditor.setEnabled(!isEveryone);
        searchButton.getCommand().setIsExecutionAllowed(!isEveryone);
        searchItems.setVisible(!isEveryone);
    }

    @Override
    public HasValue<String> getSearchString() {
        return searchStringEditor;
    }

    @Override
    public void hideRoleSelection(Boolean indic) {
        roleSelection.setVisible(!indic);
        roleToAssignLabel.setVisible(!indic);
    }

    @Override
    public void hideEveryoneSelection(Boolean indic) {
        everyoneRadio.setVisible(!indic);
        specificUserOrGroupRadio.setVisible(!indic);
    }
}
