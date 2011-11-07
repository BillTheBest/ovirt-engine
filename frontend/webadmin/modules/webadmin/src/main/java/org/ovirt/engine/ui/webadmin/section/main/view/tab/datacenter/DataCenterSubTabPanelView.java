package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.view.AbstractTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

public class DataCenterSubTabPanelView extends AbstractTabPanelView implements DataCenterSubTabPanelPresenter.ViewDef {

    private final SimpleTabPanel tabPanel = new SimpleTabPanel();

    public DataCenterSubTabPanelView() {
        initWidget(getTabPanel());
    }

    @Override
    protected Object getContentSlot() {
        return DataCenterSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

}
