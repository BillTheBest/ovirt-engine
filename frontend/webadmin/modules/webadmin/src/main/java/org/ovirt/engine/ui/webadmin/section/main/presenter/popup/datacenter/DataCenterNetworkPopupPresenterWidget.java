package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter;

import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class DataCenterNetworkPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<DataCenterNetworkModel, DataCenterNetworkPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<DataCenterNetworkModel> {
    }

    @Inject
    public DataCenterNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
