package org.ovirt.engine.ui.uicommonweb.models.storage;
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

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class SanTargetModel extends Model
{

	public static EventDefinition LoggedInEventDefinition;
	private Event privateLoggedInEvent;
	public Event getLoggedInEvent()
	{
		return privateLoggedInEvent;
	}
	private void setLoggedInEvent(Event value)
	{
		privateLoggedInEvent = value;
	}



	private UICommand privateLoginCommand;
	public UICommand getLoginCommand()
	{
		return privateLoginCommand;
	}
	public void setLoginCommand(UICommand value)
	{
		privateLoginCommand = value;
	}



	private String address;
	public String getAddress()
	{
		return address;
	}
	public void setAddress(String value)
	{
		if (!StringHelper.stringsEqual(address, value))
		{
			address = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Address"));
		}
	}

	private String port;
	public String getPort()
	{
		return port;
	}
	public void setPort(String value)
	{
		if (!StringHelper.stringsEqual(port, value))
		{
			port = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Port"));
		}
	}

	private String name;
	public String getName()
	{
		return name;
	}
	public void setName(String value)
	{
		if (!StringHelper.stringsEqual(name, value))
		{
			name = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Name"));
		}
	}

	private boolean isLoggedIn;
	public boolean getIsLoggedIn()
	{
		return isLoggedIn;
	}
	public void setIsLoggedIn(boolean value)
	{
		if (isLoggedIn != value)
		{
			isLoggedIn = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsLoggedIn"));
		}
	}

	private java.util.List<LunModel> luns;
	public java.util.List<LunModel> getLuns()
	{
		return luns;
	}
	public void setLuns(java.util.List<LunModel> value)
	{
		if (luns != value)
		{
			luns = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Luns"));
		}
	}


	static
	{
		LoggedInEventDefinition = new EventDefinition("LoggedIn", SanTargetModel.class);
	}

	public SanTargetModel()
	{
		setLoggedInEvent(new Event(LoggedInEventDefinition));

		setLoginCommand(new UICommand("Login", this));
	}

	private void Login()
	{
		getLoggedInEvent().raise(this, EventArgs.Empty);
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getLoginCommand())
		{
			Login();
		}
	}
}