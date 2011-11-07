package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.ui.uicommonweb.models.users.UserEventNotifierListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.UserSelectionChangeEvent;
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

public class SubTabUserEventNotifierPresenter extends AbstractSubTabPresenter<DbUser, UserListModel, UserEventNotifierListModel, SubTabUserEventNotifierPresenter.ViewDef, SubTabUserEventNotifierPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.userEventNotifierSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabUserEventNotifierPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<DbUser> {
    }

    @TabInfo(container = UserSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().userEventNotifierSubTabLabel(), 3,
                ginjector.getSubTabUserEventNotifierModelProvider());
    }

    @Inject
    public SubTabUserEventNotifierPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<event_subscriber, UserListModel, UserEventNotifierListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, UserSubTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.userMainTabPlace);
    }

    @ProxyEvent
    public void onUserSelectionChange(UserSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
