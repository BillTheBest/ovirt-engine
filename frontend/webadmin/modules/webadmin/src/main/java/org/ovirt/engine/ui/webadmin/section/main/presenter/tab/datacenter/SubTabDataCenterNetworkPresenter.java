package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DataCenterSelectionChangeEvent;
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

public class SubTabDataCenterNetworkPresenter extends AbstractSubTabPresenter<storage_pool, DataCenterListModel, DataCenterNetworkListModel, SubTabDataCenterNetworkPresenter.ViewDef, SubTabDataCenterNetworkPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.dataCenterNetworkSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDataCenterNetworkPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<storage_pool> {
    }

    @TabInfo(container = DataCenterSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().dataCenterNetworkSubTabLabel(), 1,
                ginjector.getSubTabDataCenterNetworkModelProvider());
    }

    @Inject
    public SubTabDataCenterNetworkPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<network, DataCenterListModel, DataCenterNetworkListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, DataCenterSubTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.dataCenterMainTabPlace);
    }

    @ProxyEvent
    public void onDataCenterSelectionChange(DataCenterSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
