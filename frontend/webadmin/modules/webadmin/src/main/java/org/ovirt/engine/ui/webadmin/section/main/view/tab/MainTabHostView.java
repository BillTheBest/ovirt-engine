package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.PercentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.ProgressBarColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class MainTabHostView extends AbstractMainTabWithDetailsTableView<VDS, HostListModel> implements MainTabHostPresenter.ViewDef {

    @Inject
    public MainTabHostView(MainModelProvider<VDS, HostListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new HostStatusColumn(), "", "30px");

        TextColumn<VDS> nameColumn = new TextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getvds_name();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumn<VDS> hostColumn = new TextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.gethost_name();
            }
        };
        getTable().addColumn(hostColumn, "Host/IP");

        TextColumn<VDS> clusterColumn = new TextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getvds_group_name();
            }
        };
        getTable().addColumn(clusterColumn, "Cluster");

        TextColumn<VDS> statusColumn = new EnumColumn<VDS, VDSStatus>() {
            @Override
            public VDSStatus getRawValue(VDS object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        ProgressBarColumn<VDS> loadColumn = new ProgressBarColumn<VDS>() {
            @Override
            protected String getProgressText(VDS object) {
                int numOfActiveVMs = object.getvm_active() != null ? object.getvm_active() : 0;
                return numOfActiveVMs + " VMs";
            }

            @Override
            protected Integer getProgressValue(VDS object) {
                return object.getvm_active();
            }
        };
        getTable().addColumn(loadColumn, "Load", "100px");

        PercentColumn<VDS> memColumn = new PercentColumn<VDS>() {
            @Override
            public Integer getProgressValue(VDS object) {
                return object.getusage_mem_percent();
            }
        };
        getTable().addColumn(memColumn, "Memory", "60px");

        PercentColumn<VDS> cpuColumn = new PercentColumn<VDS>() {
            @Override
            public Integer getProgressValue(VDS object) {
                return object.getusage_cpu_percent();
            }
        };
        getTable().addColumn(cpuColumn, "CPU", "60px");

        PercentColumn<VDS> netColumn = new PercentColumn<VDS>() {
            @Override
            public Integer getProgressValue(VDS object) {
                return object.getusage_network_percent();
            }
        };
        getTable().addColumn(netColumn, "Network", "60px");

        TextColumn<VDS> spmColumn = new EnumColumn<VDS, VdsSpmStatus>() {
            @Override
            public VdsSpmStatus getRawValue(VDS object) {
                return object.getspm_status();
            }
        };
        getTable().addColumn(spmColumn, "SpmStatus");

        getTable().addActionButton(new UiCommandButtonDefinition<VDS>(getMainModel().getNewCommand()));
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>(getMainModel().getEditCommand()));
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>(getMainModel().getRemoveCommand()));
        // TODO: separator
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>(getMainModel().getActivateCommand()));
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>(getMainModel().getMaintenanceCommand()));
        // TODO: separator
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>(getMainModel().getApproveCommand()));
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>(getMainModel().getConfigureLocalStorageCommand()));
        getTable().addActionButton(new UiCommandButtonDefinition<VDS>(getMainModel().getAssignTagsCommand(),
                "Assign Tags"));
    }

}
