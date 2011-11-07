package org.ovirt.engine.ui.webadmin.section.main.view.popup.tag;

import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.tag.TagPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class TagPopupView extends AbstractModelBoundPopupView<TagModel> implements TagPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<TagModel, TagPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, TagPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "name.entity")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    EntityModelTextBoxEditor descriptionEditor;

    @Inject
    public TagPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
    }

    void localize(ApplicationConstants constants) {
        nameEditor.setLabel(constants.tagPopupNameLabel());
        descriptionEditor.setLabel(constants.tagPopupDescriptionLabel());
    }

    @Override
    public void edit(TagModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public TagModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focus() {
        nameEditor.setFocus(true);
    }

}
