package org.ovirt.engine.ui.uicommonweb.models.hosts;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommonweb.validation.*;
import org.ovirt.engine.ui.uicompat.*;

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

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

	private EntityModel privateNetworkBootProtocol_None;
	public EntityModel getNetworkBootProtocol_None()
	{
		return privateNetworkBootProtocol_None;
	}
	public void setNetworkBootProtocol_None(EntityModel value)
	{
		privateNetworkBootProtocol_None = value;
	}
	private EntityModel privateNetworkBootProtocol_Dhcp;
	public EntityModel getNetworkBootProtocol_Dhcp()
	{
		return privateNetworkBootProtocol_Dhcp;
	}
	public void setNetworkBootProtocol_Dhcp(EntityModel value)
	{
		privateNetworkBootProtocol_Dhcp = value;
	}
	private EntityModel privateNetworkBootProtocol_StaticIp;
	public EntityModel getNetworkBootProtocol_StaticIp()
	{
		return privateNetworkBootProtocol_StaticIp;
	}
	public void setNetworkBootProtocol_StaticIp(EntityModel value)
	{
		privateNetworkBootProtocol_StaticIp = value;
	}
	private EntityModel privateCommitChanges;
	public EntityModel getCommitChanges()
	{
		return privateCommitChanges;
	}
	public void setCommitChanges(EntityModel value)
	{
		privateCommitChanges = value;
	}

	//private NetworkBootProtocol bootProtocol;
	//public NetworkBootProtocol BootProtocol
	//{
	//    get
	//    {
	//        return bootProtocol;
	//    }

	//    set
	//    {
	//        if (bootProtocol != value)
	//        {
	//            bootProtocol = value;
	//            BootProtocolChanged();
	//            OnPropertyChanged(new PropertyChangedEventArgs("BootProtocol"));
	//        }
	//    }
	//}

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
		return (Boolean)getNetworkBootProtocol_StaticIp().getEntity() == true;
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
		EntityModel tempVar = new EntityModel();
		tempVar.setEntity(false);
		setCommitChanges(tempVar);

		EntityModel tempVar2 = new EntityModel();
		tempVar2.setEntity(true);
		setNetworkBootProtocol_None(tempVar2);
		getNetworkBootProtocol_None().getEntityChangedEvent().addListener(this);
		EntityModel tempVar3 = new EntityModel();
		tempVar3.setEntity(false);
		setNetworkBootProtocol_Dhcp(tempVar3);
		getNetworkBootProtocol_Dhcp().getEntityChangedEvent().addListener(this);
		EntityModel tempVar4 = new EntityModel();
		tempVar4.setEntity(false);
		setNetworkBootProtocol_StaticIp(tempVar4);
		getNetworkBootProtocol_StaticIp().getEntityChangedEvent().addListener(this);

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

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);
		if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender instanceof EntityModel)
		{
			EntityModel senderEntityModel = (EntityModel)sender;
			if ((Boolean)senderEntityModel.getEntity())
			{
				if (senderEntityModel.equals(getNetworkBootProtocol_None()))
				{
					getNetworkBootProtocol_Dhcp().setEntity(false);
					getNetworkBootProtocol_StaticIp().setEntity(false);
					BootProtocolChanged();
				}
				else if (senderEntityModel.equals(getNetworkBootProtocol_Dhcp()))
				{
					getNetworkBootProtocol_None().setEntity(false);
					getNetworkBootProtocol_StaticIp().setEntity(false);
					BootProtocolChanged();
				}
				else if (senderEntityModel.equals(getNetworkBootProtocol_StaticIp()))
				{
					getNetworkBootProtocol_None().setEntity(false);
					getNetworkBootProtocol_Dhcp().setEntity(false);
					BootProtocolChanged();
				}
			}
		}
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