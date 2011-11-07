package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddBondParameters;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.vdscommands.NetworkVdsmVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class AddBondCommand<T extends AddBondParameters> extends VdsBondCommand<T> {
    public AddBondCommand(T parameters) {
        super(parameters);
        if (parameters.getNics() != null) {
            for (String nic : parameters.getNics()) {
                AppendCustomValue("Interfaces", nic, ", ");
            }
        }
    }

    @Override
    protected void executeCommand() {
        String address = getParameters().getAddress();
        String subnet = StringHelper.isNullOrEmpty(getParameters().getSubnet()) ? getParameters().getNetwork()
                .getsubnet() : getParameters().getSubnet();
        String gateway = StringHelper.isNullOrEmpty(getParameters().getGateway()) ? getParameters().getNetwork()
                .getgateway() : getParameters().getGateway();

        NetworkVdsmVDSCommandParameters parameters = new NetworkVdsmVDSCommandParameters(getParameters().getVdsId(),
                getParameters().getNetwork().getname(), getParameters().getNetwork().getvlan_id(), getParameters()
                        .getBondName(), getParameters().getNics(), address, subnet, gateway, getParameters()
                        .getNetwork().getstp(), getParameters().getBondingOptions(), getParameters().getBootProtocol());
        VDSReturnValue retVal = Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.AddNetwork, parameters);

        if (retVal.getSucceeded()) {
            // update vds network data
            retVal = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.CollectVdsNetworkData,
                            new VdsIdAndVdsVDSCommandParametersBase(getParameters().getVdsId()));

            if (retVal.getSucceeded()) {
                // set network status (this can change the network status to
                // operational)
                VdsStatic vdsStatic = DbFacade.getInstance().getVdsStaticDAO().get(getParameters().getVdsId());
                AttachNetworkToVdsGroupCommand.SetNetworkStatus(vdsStatic.getvds_group_id(), getParameters()
                        .getNetwork());
                setSucceeded(true);
            }
        }
    }

    @Override
    protected boolean canDoAction() {
        // check minimum 2 nics in bond
        if (getParameters().getNics().length < 2) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_BOND_PARAMETERS_INVALID);
            return false;
        }

        if (getParameters().getNetwork() == null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NETWORK_NOT_EXISTS);
            return false;
        }

        List<VdsNetworkInterface> interfaces = DbFacade.getInstance().getInterfaceDAO().getAllInterfacesForVds(
                getParameters().getVdsId());

        // check that bond exists
        // Interface bond = null; //LINQ 31899 interfaces.FirstOrDefault(i =>
        // i.name == BondParameters.BondName);
        VdsNetworkInterface bond = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface anInterface) {
                return anInterface.getName().equals(getParameters().getBondName());
            }
        });

        if (bond == null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_BOND_NAME_EXISTS);
            return false;
        }

        // check that each nic is valid
        for (final String nic : getParameters().getNics()) {
            // Interface iface = null; //LINQ 31899 interfaces.FirstOrDefault(i
            // => i.name == nic);
            VdsNetworkInterface iface = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
                @Override
                public boolean eval(VdsNetworkInterface i) {
                    return i.getName().equals(nic);
                }
            });

            if (iface == null) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_BOND_NAME_EXISTS);
                return false;
            } else if (!StringHelper.isNullOrEmpty(iface.getBondName())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_NAME_ALREAY_IN_USE);
                return false;
            } else if (!StringHelper.isNullOrEmpty(iface.getNetworkName())) {
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_NAME_ALREAY_IN_USE);
                return false;
            } else if (NetworkUtils.interfaceHasVlan(iface, interfaces)) {
                // check that one of the nics is not connected to vlan
                addCanDoActionMessage(VdcBllMessages.NETWORK_INTERFACE_IN_USE_BY_VLAN);
                return false;
            }

        }

        // check that the network not in use
        // Interface I = null; //LINQ 31899 interfaces.FirstOrDefault(n =>
        // n.network_name == AddBondParameters.Network.name);
        VdsNetworkInterface I = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
            @Override
            public boolean eval(VdsNetworkInterface i) {
                if (i.getNetworkName() != null) {
                    return i.getNetworkName().equals(getParameters().getNetwork().getname());
                }
                return false;
            }
        });

        if (I != null) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NETWORK_ALREAY_ATTACH_TO_INTERFACE);
            return false;
        }

        // check that the network exists in current cluster
        List<network> networks = DbFacade.getInstance().getNetworkDAO()
                .getAllForCluster(getVds().getvds_group_id());
        // if (true) //LINQ 31899 null == networks.FirstOrDefault(n => n.name ==
        // AddBondParameters.Network.name))
        if (null == LinqUtils.firstOrNull(networks, new Predicate<network>() {
            @Override
            public boolean eval(network network) {
                return network.getname().equals(getParameters().getNetwork().getname());
            }
        })) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_NETWORK_NET_EXISTS_IN_CLUSTER);
            return false;
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ADD_BOND : AuditLogType.NETWORK_ADD_BOND_FAILED;
    }
}
