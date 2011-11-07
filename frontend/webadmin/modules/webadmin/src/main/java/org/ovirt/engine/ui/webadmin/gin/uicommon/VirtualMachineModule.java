package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmAssignTagsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmChangeCDPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmMakeTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmRunOncePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmSnapshotCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vms.VmNewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainTabModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailTabModelProvider;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class VirtualMachineModule extends AbstractGinModule {

    // Main List Model

    @Provides
    @Singleton
    public MainModelProvider<VM, VmListModel> getVmListProvider(ClientGinjector ginjector,
            final Provider<VmAssignTagsPopupPresenterWidget> assignTagsPopupProvider,
            final Provider<VmMakeTemplatePopupPresenterWidget> makeTemplatePopupProvider,
            final Provider<VmRunOncePopupPresenterWidget> runOncePopupProvider,
            final Provider<VmChangeCDPopupPresenterWidget> changeCDPopupProvider,
            final Provider<VmNewPopupPresenterWidget> newVmPopupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new MainTabModelProvider<VM, VmListModel>(ginjector, VmListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getAssignTagsCommand()) {
                    return assignTagsPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getNewTemplateCommand()) {
                    return makeTemplatePopupProvider.get();
                } else if (lastExecutedCommand == getModel().getRunOnceCommand()) {
                    return runOncePopupProvider.get();
                } else if (lastExecutedCommand == getModel().getChangeCdCommand()) {
                    return changeCDPopupProvider.get();
                } else if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else if ((lastExecutedCommand == getModel().getNewDesktopCommand())
                        || (lastExecutedCommand == getModel().getNewServerCommand())
                        || (lastExecutedCommand == getModel().getEditCommand())) {
                    return newVmPopupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand() ||
                        lastExecutedCommand == getModel().getStopCommand() ||
                        lastExecutedCommand == getModel().getShutdownCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    // Form Detail Models

    @Provides
    @Singleton
    public DetailModelProvider<VmListModel, VmGeneralModel> getVmGeneralProvider(ClientGinjector ginjector) {
        return new DetailTabModelProvider<VmListModel, VmGeneralModel>(ginjector,
                VmListModel.class,
                VmGeneralModel.class);
    }

    // Searchable Detail Models
    @Provides
    @Singleton
    public SearchableDetailModelProvider<permissions, VmListModel, PermissionListModel> getPermissionListProvider(ClientGinjector ginjector,
            final Provider<PermissionsPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<permissions, VmListModel, PermissionListModel>(ginjector,
                VmListModel.class,
                PermissionListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                PermissionListModel model = getModel();

                if (lastExecutedCommand == model.getAddCommand()) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<DiskImage, VmListModel, VmDiskListModel> getVmDiskListProvider(ClientGinjector ginjector,
            final Provider<VmDiskPopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<DiskImage, VmListModel, VmDiskListModel>(ginjector,
                VmListModel.class,
                VmDiskListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                VmDiskListModel model = getModel();

                if (lastExecutedCommand == model.getNewCommand() || lastExecutedCommand == model.getEditCommand()) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<VmNetworkInterface, VmListModel, VmInterfaceListModel> getVmInterfaceListProvider(ClientGinjector ginjector,
            final Provider<VmInterfacePopupPresenterWidget> popupProvider,
            final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider) {
        return new SearchableDetailTabModelProvider<VmNetworkInterface, VmListModel, VmInterfaceListModel>(ginjector,
                VmListModel.class,
                VmInterfaceListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                VmInterfaceListModel model = getModel();

                if (lastExecutedCommand == model.getNewCommand() || lastExecutedCommand == model.getEditCommand()) {
                    return popupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }

            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getRemoveCommand()) {
                    return removeConfirmPopupProvider.get();
                } else {
                    return super.getConfirmModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<AuditLog, VmListModel, VmEventListModel> getVmEventListProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<AuditLog, VmListModel, VmEventListModel>(ginjector,
                VmListModel.class,
                VmEventListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<String, VmListModel, VmAppListModel> getVmAppsProvider(ClientGinjector ginjector) {
        return new SearchableDetailTabModelProvider<String, VmListModel, VmAppListModel>(ginjector,
                VmListModel.class,
                VmAppListModel.class);
    }

    @Provides
    @Singleton
    public SearchableDetailModelProvider<SnapshotModel, VmListModel, VmSnapshotListModel> getVmSnapshotListProvider(ClientGinjector ginjector,
            final Provider<VmSnapshotCreatePopupPresenterWidget> createPopupProvider) {
        return new SearchableDetailTabModelProvider<SnapshotModel, VmListModel, VmSnapshotListModel>(ginjector,
                VmListModel.class, VmSnapshotListModel.class) {
            @Override
            protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
                if (lastExecutedCommand == getModel().getNewCommand()) {
                    return createPopupProvider.get();
                } else {
                    return super.getModelPopup(lastExecutedCommand);
                }
            }
        };
    }

    @Override
    protected void configure() {
    }

}
