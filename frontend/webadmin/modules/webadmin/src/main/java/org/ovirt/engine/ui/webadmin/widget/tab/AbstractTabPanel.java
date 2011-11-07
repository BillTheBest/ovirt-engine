package org.ovirt.engine.ui.webadmin.widget.tab;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabPanel;

/**
 * Base class used to implement tab panel widgets.
 * <p>
 * Subclasses are free to style the UI, given that they declare:
 * <ul>
 * <li>{@link #tabContentContainer} widget for displaying tab contents
 * </ul>
 */
public abstract class AbstractTabPanel extends Composite implements TabPanel {

    @UiField
    SimplePanel tabContentContainer;

    // List of tabs managed by this tab panel, sorted by tab priority
    private final List<TabDefinition> tabList = new ArrayList<TabDefinition>();

    private Tab activeTab;

    @Override
    public Tab addTab(TabData tabData, String historyToken) {
        TabDefinition newTab = createNewTab(tabData);

        int beforeIndex;
        for (beforeIndex = 0; beforeIndex < tabList.size(); ++beforeIndex) {
            if (newTab.getPriority() < tabList.get(beforeIndex).getPriority())
                break;
        }

        addTabWidget(newTab.asWidget(), beforeIndex);
        tabList.add(beforeIndex, newTab);

        newTab.setTargetHistoryToken(historyToken);
        newTab.setText(tabData.getLabel());
        updateTab(newTab);

        return newTab;
    }

    @Override
    public void removeTab(Tab tab) {
        removeTabWidget(tab.asWidget());
        tabList.remove(tab);
    }

    @Override
    public void removeTabs() {
        for (Tab tab : tabList)
            removeTabWidget(tab.asWidget());

        tabList.clear();
    }

    @Override
    public void setActiveTab(Tab tab) {
        if (activeTab != null)
            activeTab.deactivate();

        if (tab != null)
            tab.activate();

        activeTab = tab;
    }

    /**
     * Sets a content widget to be displayed for the active tab.
     */
    public void setTabContent(Widget content) {
        tabContentContainer.clear();

        if (content != null)
            tabContentContainer.setWidget(content);
    }

    /**
     * Ensures that the specified tab is visible or hidden as it should.
     */
    public void updateTab(TabDefinition tab) {
        tab.asWidget().setVisible(tab.isAccessible());
    }

    /**
     * Adds a tab widget to this tab panel at the given position.
     */
    protected abstract void addTabWidget(Widget tabWidget, int index);

    /**
     * Removes a tab widget from this tab panel.
     */
    protected abstract void removeTabWidget(Widget tabWidget);

    /**
     * Returns a new tab widget based on the given data.
     */
    protected abstract TabDefinition createNewTab(TabData tabData);

}
