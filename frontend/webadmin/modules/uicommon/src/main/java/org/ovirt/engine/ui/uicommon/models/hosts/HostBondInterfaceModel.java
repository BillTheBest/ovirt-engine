package org.ovirt.engine.ui.uicommon.models.hosts;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommon.validation.*;

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class HostBondInterfaceModel extends Model
{

	private ListModel privateBond;
	public ListModel getBond()
	{
		return privateBond;
	}
	private void setBond(ListModel value)
	{
		privateBond = value;
	}
	private EntityModel privateAddress;
	public EntityModel getAddress()
	{
		return privateAddress;
	}
	private void setAddress(EntityModel value)
	{
		privateAddress = value;
	}
	private EntityModel privateSubnet;
	public EntityModel getSubnet()
	{
		return privateSubnet;
	}
	private void setSubnet(EntityModel value)
	{
		privateSubnet = value;
	}
	private EntityModel privateGateway;
	public EntityModel getGateway()
	{
		return privateGateway;
	}
	private void setGateway(EntityModel value)
	{
		privateGateway = value;
	}
	private ListModel privateNetwork;
	public ListModel getNetwork()
	{
		return privateNetwork;
	}
	private void setNetwork(ListModel value)
	{
		privateNetwork = value;
	}
	private ListModel privateBondingOptions;
	public ListModel getBondingOptions()
	{
		return privateBondingOptions;
	}
	private void setBondingOptions(ListModel value)
	{
		privateBondingOptions = value;
	}
	private EntityModel privateCheckConnectivity;
	public EntityModel getCheckConnectivity()
	{
		return privateCheckConnectivity;
	}
	private void setCheckConnectivity(EntityModel value)
	{
		privateCheckConnectivity = value;
	}

	private NetworkBootProtocol bootProtocol = NetworkBootProtocol.values()[0];
	public NetworkBootProtocol getBootProtocol()
	{
		return bootProtocol;
	}

	public void setBootProtocol(NetworkBootProtocol value)
	{
		if (bootProtocol != value)
		{
			bootProtocol = value;
			BootProtocolChanged();
			OnPropertyChanged(new PropertyChangedEventArgs("BootProtocol"));
		}
	}

	private boolean noneBootProtocolAvailable = true;
	public boolean getNoneBootProtocolAvailable()
	{
		return noneBootProtocolAvailable;
	}

	public void setNoneBootProtocolAvailable(boolean value)
	{
		if (noneBootProtocolAvailable != value)
		{
			noneBootProtocolAvailable = value;
			OnPropertyChanged(new PropertyChangedEventArgs("NoneBootProtocolAvailable"));
		}
	}

	public boolean getIsStaticAddress()
	{
		return getBootProtocol() == NetworkBootProtocol.StaticIp;
	}

	private boolean commitChanges;
	public boolean getCommitChanges()
	{
		return commitChanges;
	}
	public void setCommitChanges(boolean value)
	{
		if (commitChanges != value)
		{
			commitChanges = value;
			OnPropertyChanged(new PropertyChangedEventArgs("CommitChanges"));
		}
	}


	public HostBondInterfaceModel()
	{
		setAddress(new EntityModel());
		setSubnet(new EntityModel());
		setGateway(new EntityModel());
		setBond(new ListModel());
		setNetwork(new ListModel());
		setBondingOptions(new ListModel());
		java.util.Map.Entry<String, EntityModel> defaultItem = null;
		RefObject<java.util.Map.Entry<String, EntityModel>> tempRef_defaultItem = new RefObject<java.util.Map.Entry<String, EntityModel>>(defaultItem);
		java.util.ArrayList<java.util.Map.Entry<String, EntityModel>> list = DataProvider.GetBondingOptionList(tempRef_defaultItem);
		defaultItem = tempRef_defaultItem.argvalue;
		getBondingOptions().setItems(list);
		getBondingOptions().setSelectedItem(defaultItem);
		setCheckConnectivity(new EntityModel());
		getCheckConnectivity().setEntity(false);

		getNetwork().getSelectedItemChangedEvent().addListener(this);

		// call the Network_ValueChanged method to set all
		// properties according to default value of Network:
		Network_SelectedItemChanged(null);
	}

	private void Network_SelectedItemChanged(EventArgs e)
	{
		UpdateCanSpecify();

		// ** TODO: When BootProtocol will be added to 'network', and when
		// ** BootProtocol, Address, Subnet, and Gateway will be added to
		// ** the Network Add/Edit dialog, the next lines will be uncommented.
		// ** DO NOT DELETE NEXT COMMENTED LINES!
		//var network = (network)Network;
		//BootProtocol = network == null ? null : network.bootProtocol;
		//Address.Value = network == null ? null : network.addr;
		//Subnet.Value = network == null ? null : network.subnet;
		//Gateway.Value = network == null ? null : network.gateway;
	}

	private void BootProtocolChanged()
	{
		UpdateCanSpecify();

		getAddress().setIsValid(true);
		getSubnet().setIsValid(true);
		getGateway().setIsValid(true);
	}

	private void UpdateCanSpecify()
	{
		network network = (network)getNetwork().getSelectedItem();
		boolean isChangeble = getIsStaticAddress() && network != null && !network.getId().equals(Guid.Empty);
		getAddress().setIsChangable(isChangeble);
		getSubnet().setIsChangable(isChangeble);
		getGateway().setIsChangable(isChangeble);
	}

	public boolean Validate()
	{
		getNetwork().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
		getBond().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		getAddress().setIsValid(true);
		getSubnet().setIsValid(true);
		getGateway().setIsValid(true);

		if (getIsStaticAddress())
		{
			getAddress().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
			getSubnet().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
			getGateway().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new IpAddressValidation() });
		}

		return getBond().getIsValid() && getNetwork().getIsValid() && getAddress().getIsValid() && getSubnet().getIsValid() && getGateway().getIsValid();
	}
}