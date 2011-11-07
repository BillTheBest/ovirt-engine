package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHooksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.HostSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.tab.ModelBoundTabData;

import com.google.gwt.event.shared.EventBus;
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

public class SubTabHostHookPresenter extends AbstractSubTabPresenter<VDS, HostListModel, HostHooksListModel, SubTabHostHookPresenter.ViewDef, SubTabHostHookPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.hostHookSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostHookPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
    }

    @TabInfo(container = HostSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().hostHookSubTabLabel(), 3,
                ginjector.getSubTabHostHookModelProvider());
    }

    @Inject
    public SubTabHostHookPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<Map<String, String>, HostListModel, HostHooksListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
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
