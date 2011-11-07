package org.ovirt.engine.ui.webadmin.section.main.view.tab.pool;

import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.PoolSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.view.AbstractTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

public class PoolSubTabPanelView extends AbstractTabPanelView implements PoolSubTabPanelPresenter.ViewDef {

    private final SimpleTabPanel tabPanel = new SimpleTabPanel();

    public PoolSubTabPanelView() {
        initWidget(getTabPanel());
    }

    @Override
    protected Object getContentSlot() {
        return PoolSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

}
