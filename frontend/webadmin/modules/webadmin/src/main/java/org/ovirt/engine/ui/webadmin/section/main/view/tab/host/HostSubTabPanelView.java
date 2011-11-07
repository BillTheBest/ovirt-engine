package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.view.AbstractTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

public class HostSubTabPanelView extends AbstractTabPanelView implements HostSubTabPanelPresenter.ViewDef {

    private final SimpleTabPanel tabPanel = new SimpleTabPanel();

    public HostSubTabPanelView() {
        initWidget(getTabPanel());
    }

    @Override
    protected Object getContentSlot() {
        return HostSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

}
