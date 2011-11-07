package org.ovirt.engine.ui.webadmin.section.main.presenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class MainContentPresenter extends Presenter<MainContentPresenter.ViewDef, MainContentPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<MainContentPresenter> {
    }

    public interface ViewDef extends View {

        void update(boolean subTabPanelVisible);

    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetMainTabPanelContent = new Type<RevealContentHandler<?>>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetSubTabPanelContent = new Type<RevealContentHandler<?>>();

    @Inject
    public MainContentPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy) {
        super(eventBus, view, proxy);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainSectionPresenter.TYPE_SetMainContent, this);
    }

    @ProxyEvent
    public void onUpdateMainContentLayout(UpdateMainContentLayoutEvent event) {
        getView().update(event.isSubTabPanelVisible());
    }

}
