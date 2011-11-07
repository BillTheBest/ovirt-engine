package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.webadmin.widget.HasUiCommandClickHandlers;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class PermissionsPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<AdElementListModel, PermissionsPopupPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<AdElementListModel> {
        HasUiCommandClickHandlers getSearchButton();

        HasKeyPressHandlers getKeyPressSearchInputBox();
        
        HasValue<String> getSearchString();
        
        HasClickHandlers getEveryoneRadio();
        
        HasClickHandlers getSpecificUserOrGroupRadio();

        void changeStateOfElementsWhenAccessIsForEveryone(boolean isEveryone);
        
        void hideRoleSelection(Boolean indic);
        
        void hideEveryoneSelection(Boolean indic);
    }

    @Inject
    public PermissionsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final AdElementListModel model) {
        // Let the parent do its work:
        super.init(model);
        getView().getSearchButton().setCommand(model.getSearchCommand());

        registerHandler(getView().getSearchButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getView().getSearchButton().getCommand().Execute();
            }
        }));

        registerHandler(getView().getKeyPressSearchInputBox().addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
                    model.setSearchString(getView().getSearchString().getValue());
                    getView().getSearchButton().getCommand().Execute();
                }
            }
        }));
        
        registerHandler(getView().getEveryoneRadio().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.setIsEveryoneSelected(true);
                getView().changeStateOfElementsWhenAccessIsForEveryone(true);
                //Disable relevant elements
                
            }
        }));
        
        registerHandler(getView().getSpecificUserOrGroupRadio().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                model.setIsEveryoneSelected(false);
                getView().changeStateOfElementsWhenAccessIsForEveryone(false);
            }
        }));
        
        model.getIsRoleListHiddenModel().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getView().hideRoleSelection(Boolean.parseBoolean(model.getIsRoleListHiddenModel().getEntity().toString()));
            }
        });
        
        model.getIsEveryoneSelectionHidden().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getView().hideEveryoneSelection(Boolean.parseBoolean(model.getIsRoleListHiddenModel().getEntity().toString()));
            }
        });
    }
}
