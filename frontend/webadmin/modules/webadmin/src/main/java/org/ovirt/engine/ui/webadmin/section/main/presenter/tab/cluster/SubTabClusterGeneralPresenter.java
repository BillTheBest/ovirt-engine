package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterPolicyModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.ClusterSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.DetailModelProvider;
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

public class SubTabClusterGeneralPresenter extends AbstractSubTabPresenter<VDSGroup, ClusterListModel, ClusterPolicyModel, SubTabClusterGeneralPresenter.ViewDef, SubTabClusterGeneralPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.clusterGeneralSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabClusterGeneralPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDSGroup> {
    }

    @TabInfo(container = ClusterSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().clusterGeneralSubTabLabel(), 0,
                ginjector.getSubTabClusterGeneralModelProvider());
    }

    @Inject
    public SubTabClusterGeneralPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, DetailModelProvider<ClusterListModel, ClusterPolicyModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.clusterMainTabPlace);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, ClusterSubTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @ProxyEvent
    public void onClusterSelectionChange(ClusterSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
