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

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class LunModel extends Model
{

	private String lunId;
	public String getLunId()
	{
		return lunId;
	}
	public void setLunId(String value)
	{
		if (!StringHelper.stringsEqual(lunId, value))
		{
			lunId = value;
			OnPropertyChanged(new PropertyChangedEventArgs("LunId"));
		}
	}

	private String vendorId;
	public String getVendorId()
	{
		return vendorId;
	}
	public void setVendorId(String value)
	{
		if (!StringHelper.stringsEqual(vendorId, value))
		{
			vendorId = value;
			OnPropertyChanged(new PropertyChangedEventArgs("VendorId"));
		}
	}

	private String productId;
	public String getProductId()
	{
		return productId;
	}
	public void setProductId(String value)
	{
		if (!StringHelper.stringsEqual(productId, value))
		{
			productId = value;
			OnPropertyChanged(new PropertyChangedEventArgs("ProductId"));
		}
	}

	private String serial;
	public String getSerial()
	{
		return serial;
	}
	public void setSerial(String value)
	{
		if (!StringHelper.stringsEqual(serial, value))
		{
			serial = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Serial"));
		}
	}

	private int size;
	public int getSize()
	{
		return size;
	}
	public void setSize(int value)
	{
		if (size != value)
		{
			size = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Size"));
		}
	}

	private int multipathing;
	public int getMultipathing()
	{
		return multipathing;
	}
	public void setMultipathing(int value)
	{
		if (multipathing != value)
		{
			multipathing = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Multipathing"));
		}
	}

	private boolean isAccessible;
	public boolean getIsAccessible()
	{
		return isAccessible;
	}
	public void setIsAccessible(boolean value)
	{
		if (isAccessible != value)
		{
			isAccessible = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsAccessible"));
		}
	}

	private boolean isIncluded;
	public boolean getIsIncluded()
	{
		return isIncluded;
	}
	public void setIsIncluded(boolean value)
	{
		if (isIncluded != value)
		{
			isIncluded = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsIncluded"));
		}
	}

	private java.util.ArrayList<SanTargetModel> targets;
	public java.util.ArrayList<SanTargetModel> getTargets()
	{
		return targets;
	}
	public void setTargets(java.util.ArrayList<SanTargetModel> value)
	{
		if (targets != value)
		{
			targets = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Targets"));
		}
	}

}