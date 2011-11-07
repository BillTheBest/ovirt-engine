package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.common.util.StringHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

@SuppressWarnings("serial")
public class ChangeVMClusterCommand<T extends ChangeVMClusterParameters> extends VmCommand<T> {

    private VDSGroup targetCluster;
    private boolean dedicatedHostWasCleared;

    public ChangeVMClusterCommand(T params) {
        super(params);
        setVmId(params.getVmId());
    }

    @Override
    protected boolean canDoAction() {
        // Set parameters for messeging.
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM__CLUSTER);

        VM vm = getVm();
        if (vm == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
            return false;
        } else {
            if (ObjectIdentityChecker.CanUpdateField(vm, "vds_group_id", vm.getstatus())) {
                targetCluster = DbFacade.getInstance().getVdsGroupDAO().get(getParameters().getClusterId());
                if (targetCluster == null) {
                    addCanDoActionMessage(VdcBllMessages.VM_CLUSTER_IS_NOT_VALID);
                    return false;
                }

                // Check that the target cluster is in the same data center.
                if (!targetCluster.getstorage_pool_id().equals(vm.getstorage_pool_id())) {
                    addCanDoActionMessage(VdcBllMessages.VM_CANNOT_MOVE_TO_CLUSTER_IN_OTHER_STORAGE_POOL);
                    return false;
                }

                List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDAO()
                .getAllForVm(getParameters().getVmId());

                // Get if the cluster chosen got limit of nics.
                boolean limitNumOfNics = Config.<Boolean> GetValue(ConfigValues.LimitNumberOfNetworkInterfaces,
                                                                   targetCluster.getcompatibility_version()
                                                                                .getValue()
                                                                                .toString());

                // If so , check if nic count has exceeded and print appropriate
                // message.
                if (limitNumOfNics) {
                    // Check that the number of interfaces does not exceed
                    // limit.
                    // Necessary only for version 2.2.
                    boolean numOfNicsLegal = validateNumberOfNics(interfaces, null);
                    if (!numOfNicsLegal) {
                        addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_EXITED_MAX_INTERFACES);
                        return false;
                    }
                }

                // Check the destination cluster have all the networks that the VM use
                List<network> networks = DbFacade.getInstance().getNetworkDAO().getAllForCluster(getParameters().getClusterId());
                StringBuilder missingNets = new StringBuilder();
                for (VmNetworkInterface iface: interfaces) {
                    if (!StringHelper.isEmpty(iface.getNetworkName())) {
                        boolean exists = false;
                        for (network net: networks) {
                            if (net.getname().equals(iface.getNetworkName())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            if (missingNets.length() > 0) {
                                missingNets.append(", ");
                            }
                            missingNets.append(iface.getNetworkName());
                        }
                    }
                }
                if (missingNets.length() > 0) {
                    addCanDoActionMessage(VdcBllMessages.MOVE_VM_CLUSTER_MISSING_NETWORK);
                    addCanDoActionMessage(String.format("$networks %1$s", missingNets.toString()));
                    return false;
                }

                // Check if VM static parameters are compatible for new cluster.
                boolean isCpuSocketsValid = AddVmCommand.CheckCpuSockets(
                                                                         vm.getStaticData().getnum_of_sockets(),
                                                                         vm.getStaticData().getcpu_per_socket(),
                                                                         targetCluster.getcompatibility_version()
                                                                                      .getValue(),
                                                                         getReturnValue().getCanDoActionMessages());
                if (!isCpuSocketsValid) {
                    return false;
                }

            } else {
                addCanDoActionMessage(VdcBllMessages.VM_STATUS_NOT_VALID_FOR_UPDATE);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        // check that the cluster are not the same
        VM vm = getVm();
        if (vm.getvds_group_id().equals(getParameters().getClusterId())) {
            setSucceeded(true);
            return;
        }

        // update vm interfaces
        List<network> networks = DbFacade.getInstance().getNetworkDAO()
                .getAllForCluster(getParameters().getClusterId());
        List<VmNetworkInterface> interfaces = DbFacade.getInstance().getVmNetworkInterfaceDAO()
                .getAllForVm(getParameters().getVmId());

        for (final VmNetworkInterface iface : interfaces) {
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

        if (vm.getdedicated_vm_for_vds() != null) {
            vm.setdedicated_vm_for_vds(null);
            dedicatedHostWasCleared = true;
        }

        vm.setvds_group_id(getParameters().getClusterId());
        DbFacade.getInstance().getVmStaticDAO().update(vm.getStaticData());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ?
                dedicatedHostWasCleared ?
                        AuditLogType.USER_UPDATE_VM_CLUSTER_DEFAULT_HOST_CLEARED
                        : AuditLogType.USER_UPDATE_VM
                : AuditLogType.USER_FAILED_UPDATE_VM;
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        Map<Guid, VdcObjectType> map = new HashMap<Guid, VdcObjectType>(2);
        map.put(getParameters().getVmId(), VdcObjectType.VM);
        map.put(getParameters().getClusterId(), VdcObjectType.VdsGroups);
        return Collections.unmodifiableMap(map);
    }

}
