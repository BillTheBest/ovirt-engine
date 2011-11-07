package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.widget.tree.SystemTreeItemCell;

import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;

public class SystemTreeModelProvider extends DataBoundTabModelProvider<SystemTreeItemModel, SystemTreeModel> implements SearchableTreeModelProvider<SystemTreeItemModel, SystemTreeModel> {

    private final DefaultSelectionEventManager<SystemTreeItemModel> selectionManager =
            DefaultSelectionEventManager.createDefaultManager();
    private final SingleSelectionModel<SystemTreeItemModel> selectionModel;

    private final ApplicationResources resources;
    private final ApplicationTemplates templates;

    @Inject
    public SystemTreeModelProvider(ClientGinjector ginjector) {
        super(ginjector);
        this.resources = ginjector.getApplicationResources();
        this.templates = ginjector.getApplicationTemplates();

        // Create selection model
        selectionModel = new SingleSelectionModel<SystemTreeItemModel>();
        selectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                SystemTreeModelProvider.this.setSelectedItems(Arrays.asList(selectionModel.getSelectedObject()));
            }
        });
    }

    @Override
    protected void onCommonModelChange() {
        super.onCommonModelChange();

        // Add model reset handler
        getModel().getResetRequestedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                ArrayList<SystemTreeItemModel> items = getModel().getItems();
                if (items != null && !items.isEmpty()) {
                    // Select first (root) tree item
                    selectionModel.setSelected(items.get(0), true);
                }
            }
        });
    }

    @Override
    protected void updateDataProvider(List<SystemTreeItemModel> items) {
        // Update data provider only for non-empty data
        if (!items.isEmpty()) {
            super.updateDataProvider(items);
            selectionModel.setSelected(items.get(0), true);
        }
    }

    @Override
    public SystemTreeModel getModel() {
        return getCommonModel().getSystemTree();
    }

    @Override
    public void setSelectedItems(List<SystemTreeItemModel> items) {
        getModel().setSelectedItem(items.size() > 0 ? items.get(0) : null);
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T parent) {
        SystemTreeItemCell cell = new SystemTreeItemCell(resources, templates);

        if (parent != null) {
            // Not a root node
            SystemTreeItemModel parentModel = (SystemTreeItemModel) parent;
            List<SystemTreeItemModel> children = parentModel.getChildren();
            return new DefaultNodeInfo<SystemTreeItemModel>(new ListDataProvider<SystemTreeItemModel>(children),
                    cell, selectionModel, selectionManager, null);
        } else {
            // This is the root node
            return new DefaultNodeInfo<SystemTreeItemModel>(getDataProvider(),
                    cell, selectionModel, selectionManager, null);
        }
    }

    @Override
    public boolean isLeaf(Object value) {
        if (value != null) {
            SystemTreeItemModel itemModel = (SystemTreeItemModel) value;
            List<SystemTreeItemModel> children = itemModel.getChildren();

            if (children != null) {
                return children.isEmpty();
            }
        }

        return false;
    }

}
