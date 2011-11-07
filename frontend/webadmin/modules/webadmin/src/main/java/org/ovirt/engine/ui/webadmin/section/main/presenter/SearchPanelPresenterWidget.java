package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.CommonModelChangeEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.CommonModelChangeEvent.CommonModelChangeHandler;
import org.ovirt.engine.ui.webadmin.uicommon.model.CommonModelManager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class SearchPanelPresenterWidget extends PresenterWidget<SearchPanelPresenterWidget.ViewDef> implements CommonModelChangeHandler {

    public interface ViewDef extends View {

        String getSearchString();

        String getSearchPrefixString();

        void setSearchString(String searchString);

        void setSearchStringPrefix(String searchStringPrefix);

        void setHasSearchStringPrefix(boolean hasSearchStringPrefix);

        void setHasSelectedTags(boolean hasSelectedTags);

        HasClickHandlers getBookmarkButton();

        HasClickHandlers getSearchButton();

        HasKeyDownHandlers getSearchInputHandlers();

        void hideSuggestionBox();

    }

    private CommonModel commonModel;

    @Inject
    public SearchPanelPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
        eventBus.addHandler(CommonModelChangeEvent.getType(), this);

        this.commonModel = CommonModelManager.instance();
        addCommonModelListeners();
    }

    @Override
    public void onCommonModelChange(CommonModelChangeEvent event) {
        commonModel = event.getCommonModel();
        addCommonModelListeners();
    }

    void addCommonModelListeners() {
        commonModel.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs pcArgs = (PropertyChangedEventArgs) args;

                // Update search string when 'SearchString' property changes
                if ("SearchString".equals(pcArgs.PropertyName)) {
                    updateViewSearchString();
                }

                // Update search string prefix when 'SearchStringPrefix' property changes
                else if ("SearchStringPrefix".equals(pcArgs.PropertyName)) {
                    updateViewSearchStringPrefix();
                }

                // Update search string prefix visibility when 'HasSearchStringPrefix' property changes
                else if ("HasSearchStringPrefix".equals(pcArgs.PropertyName)) {
                    updateViewHasSearchStringPrefix();
                }

                else if ("HasSelectedTags".equals(pcArgs.PropertyName)) {
                    updateViewHasSelectedTags();
                }
            }
        });
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getBookmarkButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                commonModel.getBookmarkList().getNewCommand().Execute();
            }
        }));

        registerHandler(getView().getSearchButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateModelSearchString();
            }
        }));

        registerHandler(getView().getSearchInputHandlers().addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    updateModelSearchString();
                } else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                    getView().hideSuggestionBox();
                }
            }
        }));
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        updateViewSearchString();
        updateViewSearchStringPrefix();
        updateViewHasSearchStringPrefix();
    }

    void updateModelSearchString() {
        commonModel.setSearchString(getView().getSearchString());
        commonModel.Search();
    }

    void updateViewSearchString() {
        getView().setSearchString(commonModel.getSearchString());
    }

    void updateViewSearchStringPrefix() {
        getView().setSearchStringPrefix(commonModel.getSearchStringPrefix());
    }

    void updateViewHasSearchStringPrefix() {
        getView().setHasSearchStringPrefix(commonModel.getHasSearchStringPrefix());
    }

    void updateViewHasSelectedTags() {
        getView().setHasSelectedTags(commonModel.getHasSelectedTags());
    }

}
