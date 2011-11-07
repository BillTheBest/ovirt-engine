package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.NetworkStatusColumn;

import com.google.gwt.user.cellview.client.TextColumn;

public class SubTabClusterNetworkView extends AbstractSubTabTableView<VDSGroup, network, ClusterListModel, ClusterNetworkListModel>
        implements SubTabClusterNetworkPresenter.ViewDef {

    @Inject
    public SubTabClusterNetworkView(SearchableDetailModelProvider<network, ClusterListModel, ClusterNetworkListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new NetworkStatusColumn(), "", "20px");

        TextColumn<network> nameColumn = new TextColumn<network>() {
            @Override
            public String getValue(network object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumn<network> statusColumn = new EnumColumn<network, NetworkStatus>() {
            @Override
            public NetworkStatus getRawValue(network object) {
                return object.getStatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        TextColumn<network> roleColumn = new TextColumn<network>() {
            @Override
            public String getValue(network object) {
                // according to ClusterNetworkListView.xaml:45
                return object.getis_display() ? "Display" : "";
            }
        };
        getTable().addColumn(roleColumn, "Role");

        TextColumn<network> descColumn = new TextColumn<network>() {
            @Override
            public String getValue(network object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(descColumn, "Description");

        getTable().addActionButton(new UiCommandButtonDefinition<network>(getDetailModel().getNewNetworkCommand(), "Add Network"));
        getTable().addActionButton(new UiCommandButtonDefinition<network>(getDetailModel().getSetAsDisplayCommand(), "Set as Display"));
    }

}
