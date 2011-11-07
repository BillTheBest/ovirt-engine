package org.ovirt.engine.ui.webadmin.widget.tab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.TabData;

public class SimpleTabPanel extends AbstractTabPanel {

    interface WidgetUiBinder extends UiBinder<Widget, SimpleTabPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel tabContainer;

    public SimpleTabPanel() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected void addTabWidget(Widget tabWidget, int index) {
        tabContainer.insert(tabWidget, index);
    }

    @Override
    protected void removeTabWidget(Widget tabWidget) {
        tabContainer.getElement().removeChild(tabWidget.getElement());
    }

    @Override
    protected TabDefinition createNewTab(TabData tabData) {
        return TabFactory.createTab(tabData, this);
    }

}
