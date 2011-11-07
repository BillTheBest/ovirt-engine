package org.ovirt.engine.ui.webadmin.section.main.presenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootLayoutContentEvent;

public class MainSectionPresenter extends Presenter<MainSectionPresenter.ViewDef, MainSectionPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<MainSectionPresenter> {
    }

    public interface ViewDef extends View, HasUiHandlers<MainTabBarOffsetUiHandlers> {
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetHeader = new Type<RevealContentHandler<?>>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();

    private final HeaderPresenterWidget header;

    @Inject
    public MainSectionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy, HeaderPresenterWidget header) {
        super(eventBus, view, proxy);
        this.header = header;
        getView().setUiHandlers(header);
    }

    @Override
    protected void revealInParent() {
        RevealRootLayoutContentEvent.fire(this, this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        setInSlot(TYPE_SetHeader, header);
    }

}
