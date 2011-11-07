package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.webadmin.widget.tab.HeadlessTabPanel.TabWidgetHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabContainerPresenter;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class MainTabPanelPresenter extends TabContainerPresenter<MainTabPanelPresenter.ViewDef, MainTabPanelPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<MainTabPanelPresenter> {
    }

    public interface ViewDef extends TabView {

        void setTabWidgetHandler(TabWidgetHandler tabWidgetHandler);

    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<RequestTabsHandler>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabContent = new Type<RevealContentHandler<?>>();

    @Inject
    public MainTabPanelPresenter(EventBus eventBus, ViewDef view,
            ProxyDef proxy, HeaderPresenterWidget header) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs);
        view.setTabWidgetHandler(header);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainContentPresenter.TYPE_SetMainTabPanelContent, this);
    }

}
