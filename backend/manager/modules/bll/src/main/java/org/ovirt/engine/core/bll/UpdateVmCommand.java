package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils.VMCustomProperties;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils.ValidationError;

public class UpdateVmCommand<T extends VmManagementParametersBase> extends VmManagementCommandBase<T> {
    private VmStatic mOldVmStatic;

    public UpdateVmCommand(T parameters) {
        super(parameters);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VM : AuditLogType.USER_FAILED_UPDATE_VM;
    }

    @Override
    protected void ExecuteVmCommand() {
        if (getParameters().getVmStaticData() != null) {
            mOldVmStatic = getVm().getStaticData();
            /**
             * patch
             */
            getParameters().getVmStaticData().setcreation_date(mOldVmStatic.getcreation_date());
            if (getParameters().getVmStaticData().getcreation_date().equals(DateTime.getMinValue())
                    || getParameters().getVmStaticData().getcreation_date().equals(DateTime.getMinValue())) {
                getParameters().getVmStaticData().setcreation_date(getNow());
            }
            if (mOldVmStatic != null) {
                UpdateVmNetworks();
                UpdateVmData();
                if (((Boolean) Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.IsValid,
                                new IrsBaseVDSCommandParameters(getVm().getstorage_pool_id())).getReturnValue())
                        .booleanValue()) {

                    // Set the VM to null, to fetch it again from the DB ,instead from the cache.
                    // We want to get the VM current data that was updated to the DB.
                    setVm(null);
                    UpdateVmInSpm(getVm().getstorage_pool_id(),
                            new java.util.ArrayList<VM>(java.util.Arrays.asList(new VM[] { getVm() })));
                }
                setSucceeded(true);
            }
        }
    }

    private void UpdateVmNetworks() {
        VmStatic dbVm = DbFacade.getInstance().getVmStaticDAO().get(getParameters().getVmStaticData().getId());
        // check if the cluster has changed
        if (!dbVm.getvds_group_id().equals(getParameters().getVmStaticData().getvds_group_id())) {
            List<network> networks = DbFacade
                    .getInstance()
                    .getNetworkDAO()
                    .getAllForCluster(
                            getParameters().getVmStaticData().getvds_group_id());
            List<VmNetworkInterface> interfaces = DbFacade.getInstance()
                    .getVmNetworkInterfaceDAO()
                    .getAllForVm(getParameters().getVmStaticData().getId());
            for (final VmNetworkInterface iface : interfaces) {
                // LINQ 29456
                // network net = networks.FirstOrDefault(n => iface.network_name
                // == n.name);
                // // if network not exists in cluster we remove the network to
                // interface connection
                // if (net == null)
                // {
                // iface.network_name = null;
                // DbFacade.Instance.updateVmInterface(iface.InterfaceDynamic);
                // }

                network net = LinqUtils.firstOrNull(networks, new Predicate<network>() {
                    @Override
                    public boolean eval(network n) {
                        return iface.getNetworkName().equals(n.getname());
                    }
                });
                // if network not exists in cluster we remove the network to
                // interface connection
                if (net == null) {
                    iface.setNetworkName(null);
                    DbFacade.getInstance().getVmNetworkInterfaceDAO().update(iface);
                }

            }
        }
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(UpdateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = false;
        List<ValidationError> validationErrors = null;

        String vmName = getParameters().getVm().getvm_name();
        if (vmName == null || vmName.isEmpty()) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_MAY_NOT_BE_EMPTY);
        } else {
            // check that VM name is not too long
            boolean vmNameValidLength = isVmNameValidLength(getParameters().getVm());
            if (!vmNameValidLength) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
            } else if (getVm().getStaticData() != null) {
                VM vm = DbFacade.getInstance().getVmDAO().getById(getVm().getStaticData().getId());
                // Checking if a desktop with same name already exists
                VmStatic vmStaticDataFromParams = getParameters().getVmStaticData();
                boolean exists = (Boolean) Backend
                        .getInstance()
                        .runInternalQuery(VdcQueryType.IsVmWithSameNameExist,
                                new IsVmWithSameNameExistParameters(vmStaticDataFromParams.getvm_name()))
                        .getReturnValue();
                if (exists && (!StringHelper.EqOp(vm.getvm_name(), vmStaticDataFromParams.getvm_name()))) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_ALREADY_EXIST);

                } else if (!(validationErrors = VmPropertiesUtils.validateVMProperties(vmStaticDataFromParams)).isEmpty()) {
                    handleCustomPropertiesError(validationErrors, getReturnValue().getCanDoActionMessages());

                } else if (vmStaticDataFromParams.getauto_startup()
                        && vmStaticDataFromParams.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST) {
                    getReturnValue().getCanDoActionMessages().add(
                            VdcBllMessages.ACTION_TYPE_FAILED_VM_CANNOT_BE_HIGHLY_AVAILABLE_AND_PINNED_TO_HOST
                                    .toString());

                } else if (VmHandler.isMemorySizeLegal(vmStaticDataFromParams.getos(),
                        vmStaticDataFromParams.getmem_size_mb(), getReturnValue().getCanDoActionMessages(),
                        getVdsGroup().getcompatibility_version().toString())) {

                    if (StringHelper.EqOp(vmStaticDataFromParams.getvm_name(), "")) {
                        getReturnValue().getCanDoActionMessages()
                                .add(VdcBllMessages.VM_NAME_CANNOT_BE_EMPTY.toString());
                    } else if (vm != null) {
                        setCustomDefinedProperties(vmStaticDataFromParams);
                        setCustomDefinedProperties(getVm().getStaticData());
                        retValue = VmHandler.mUpdateVmsStatic.IsUpdateValid(getVm().getStaticData(),
                                vmStaticDataFromParams, vm.getstatus());
                        if (!retValue) {
                            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_UPDATE_ILLEGAL_FIELD);
                        } else if (!getVm().getStaticData().getvds_group_id()
                                .equals(vmStaticDataFromParams.getvds_group_id())) {
                            addCanDoActionMessage(VdcBllMessages.VM_CANNOT_UPDATE_CLUSTER);
                            retValue = false;
                        }

                        if (vmStaticDataFromParams.getdedicated_vm_for_vds() != null) {
                            VDS vds = DbFacade.getInstance().getVdsDAO().get(
                                    new Guid(vmStaticDataFromParams.getdedicated_vm_for_vds().toString()));
                            // if vds doesnt exist or not the same cluster
                            if (vds == null || !vds.getvds_group_id().equals(vmStaticDataFromParams.getvds_group_id())) {
                                addCanDoActionMessage(VdcBllMessages.VM_CANNOT_UPDATE_DEFAULT_VDS_NOT_VALID);
                                retValue = false;
                            }
                        }
                        if (vm.getnum_of_monitors() < vmStaticDataFromParams.getnum_of_monitors()) {
                            // LINQ 29456
                            // List<DiskImageBase> allDisks =
                            // DbFacade.Instance.GetImagesByVmGuid(VmId).Select(a
                            // =>
                            // (DiskImageBase)a).ToList();
                            // List<Interface> interfaces =
                            // DbFacade.Instance.getIterfacesByVmId(VmId);
                            // retValue =
                            // CheckPCIAndIDELimit(VmManagementParameters.VmStaticData.num_of_monitors,
                            // interfaces, allDisks);

                            List allDisks = DbFacade.getInstance().getDiskImageDAO().getAllForVm(getVmId());
                            List<VmNetworkInterface> interfaces = DbFacade.getInstance()
                                    .getVmNetworkInterfaceDAO().getAllForVm(getVmId());
                            retValue = CheckPCIAndIDELimit(vmStaticDataFromParams.getnum_of_monitors(), interfaces,
                                    allDisks, getReturnValue().getCanDoActionMessages());
                        }
                        if (!VmTemplateCommand.IsVmPriorityValueLegal(vmStaticDataFromParams.getpriority(),
                                getReturnValue().getCanDoActionMessages())) {
                            retValue = false;
                        }

                        if (retValue && vm.getVmPoolId() != null && vmStaticDataFromParams.getis_stateless()) {
                            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_FROM_POOL_CANNOT_BE_STATELESS);
                            retValue = false;
                        }
                    }
                }
                if (retValue) {
                    retValue = AddVmCommand.CheckCpuSockets(vmStaticDataFromParams.getnum_of_sockets(),
                            vmStaticDataFromParams.getcpu_per_socket(), getVdsGroup().getcompatibility_version()
                                    .toString(), getReturnValue().getCanDoActionMessages());
                }
                // Check id dedicated host is same as VM cluster
                if (retValue) {
                    retValue = isDedicatedVdsOnSameCluster(getParameters().getVmStaticData());
                }
            }
        }
        return retValue;
    }

    private void setCustomDefinedProperties(VmStatic vmStaticDataFromParams) {
        VMCustomProperties properties =
                VmPropertiesUtils.parseProperties(vmStaticDataFromParams.getCustomProperties());
        vmStaticDataFromParams.setPredefinedProperties(properties.getPredefinedProperties());
        vmStaticDataFromParams.setUserDefinedProperties(properties.getUseDefinedProperties());
    }

    private void UpdateVmData() {
        DbFacade.getInstance().getVmStaticDAO().update(getParameters().getVmStaticData());
        // VM vm =
        // ResourceManager.Instance.getVm(VmManagementParameters.VmStaticData.vm_guid);
        // if (vm != null)
        // {
        // vm.StaticData = VmManagementParameters.VmStaticData;
        // }
    }

    @Override
    public Guid getVmId() {
        if (super.getVmId().equals(Guid.Empty)) {
            super.setVmId(getParameters().getVmStaticData().getId());
        }
        return super.getVmId();
    }
}
