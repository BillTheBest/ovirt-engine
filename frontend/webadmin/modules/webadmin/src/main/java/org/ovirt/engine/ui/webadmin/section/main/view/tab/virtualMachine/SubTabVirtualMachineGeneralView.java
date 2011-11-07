package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.webadmin.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.form.FormBuilder;
import org.ovirt.engine.ui.webadmin.widget.form.FormItem;
import org.ovirt.engine.ui.webadmin.widget.form.GeneralFormPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class SubTabVirtualMachineGeneralView extends AbstractSubTabFormView<VM, VmListModel, VmGeneralModel> implements SubTabVirtualMachineGeneralPresenter.ViewDef, Editor<VmGeneralModel> {

    interface ViewUiBinder extends UiBinder<Widget, SubTabVirtualMachineGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<VmGeneralModel, SubTabVirtualMachineGeneralView> {
        Driver driver = GWT.create(Driver.class);
    }

    Label name = new Label();
    Label description = new Label();
    Label template = new Label();
    Label definedMemory = new Label();
    Label minAllocatedMemory = new Label();
    Label oS = new Label();
    Label cpuInfo = new Label();
    Label defaultDisplayType = new Label();
    Label storageDomain = new Label();
    Label origin = new Label();
    Label priority = new Label();
    Label usbPolicy = new Label();
    Label defaultHost = new Label();
    Label customProperties = new Label();
    Label domain = new Label();
    Label timeZone = new Label();

    @Ignore
    Label monitorCount = new Label();
    @Ignore
    Label isHighlyAvailable = new Label();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    @Inject
    public SubTabVirtualMachineGeneralView(DetailModelProvider<VmListModel, VmGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 3, 7);
        formBuilder.addFormItem(new FormItem("Name", name, 0, 0));
        formBuilder.addFormItem(new FormItem("Description", description, 1, 0));
        formBuilder.addFormItem(new FormItem("Template", template, 2, 0));
        formBuilder.addFormItem(new FormItem("Operating System", oS, 3, 0));
        formBuilder.addFormItem(new FormItem("Default Display Type", defaultDisplayType, 4, 0));
        formBuilder.addFormItem(new FormItem("Priority", priority, 5, 0) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasPriority();
            }
        });

        formBuilder.addFormItem(new FormItem("Defined Memory", definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem("Physical Memory Guaranteed", minAllocatedMemory, 1, 1));
        formBuilder.addFormItem(new FormItem("Number of CPU Cores", cpuInfo, 2, 1));
        formBuilder.addFormItem(new FormItem("Highly Available", isHighlyAvailable, 3, 1) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasHighlyAvailable();
            }
        });
        formBuilder.addFormItem(new FormItem("Number of Monitors", monitorCount, 3, 1) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasMonitorCount();
            }
        });
        formBuilder.addFormItem(new FormItem("USB Policy", usbPolicy, 4, 1) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasUsbPolicy();
            }
        });
        formBuilder.addFormItem(new FormItem("Resides on Storage Domain", storageDomain, 5, 1, "HasStorageDomain") {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasStorageDomain();
            }
        });

        formBuilder.addFormItem(new FormItem("Origin", origin, 0, 2));
        formBuilder.addFormItem(new FormItem("Run On", defaultHost, 1, 2));
        formBuilder.addFormItem(new FormItem("Custom Properties", customProperties, 2, 2));
        formBuilder.addFormItem(new FormItem("Domain", domain, 3, 2) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasDomain();
            }
        });
        formBuilder.addFormItem(new FormItem("Time Zone", timeZone, 4, 2) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasTimeZone();
            }
        });
    }

    @Override
    public void setMainTabSelectedItem(VM selectedItem) {
        Driver.driver.edit(getDetailModel());

        // TODO required because of GWT#5864
        monitorCount.setText(Integer.toString(getDetailModel().getMonitorCount()));
        isHighlyAvailable.setText(Boolean.toString(getDetailModel().getIsHighlyAvailable()));

        formBuilder.showForm(getDetailModel(), Driver.driver);
    }

}
