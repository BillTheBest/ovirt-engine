package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.StorageSelectionChangeEvent;
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

public class SubTabStoragePermissionPresenter extends AbstractSubTabPresenter<storage_domains, StorageListModel, PermissionListModel, SubTabStoragePermissionPresenter.ViewDef, SubTabStoragePermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.storagePermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabStoragePermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<storage_domains> {
    }

    @TabInfo(container = StorageSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().storagePermissionSubTabLabel(), 7,
                ginjector.getSubTabStoragePermissionModelProvider());
    }

    @Inject
    public SubTabStoragePermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<permissions, StorageListModel, PermissionListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, StorageSubTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.storageMainTabPlace);
    }

    @ProxyEvent
    public void onStorageSelectionChange(StorageSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
