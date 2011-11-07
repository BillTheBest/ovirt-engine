package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.TemplateSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.view.AbstractTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

public class TemplateSubTabPanelView extends AbstractTabPanelView implements TemplateSubTabPanelPresenter.ViewDef {

    private final SimpleTabPanel tabPanel = new SimpleTabPanel();

    public TemplateSubTabPanelView() {
        initWidget(getTabPanel());
    }

    @Override
    protected Object getContentSlot() {
        return TemplateSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

}
