package org.ovirt.engine.ui.uicommonweb.models.datacenters;
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
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommonweb.dataprovider.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class DataCenterModel extends Model
{

	private storage_pool privateEntity;
	public storage_pool getEntity()
	{
		return privateEntity;
	}
	public void setEntity(storage_pool value)
	{
		privateEntity = value;
	}
	private NGuid privateDataCenterId;
	public NGuid getDataCenterId()
	{
		return privateDataCenterId;
	}
	public void setDataCenterId(NGuid value)
	{
		privateDataCenterId = value;
	}
	private boolean privateIsNew;
	public boolean getIsNew()
	{
		return privateIsNew;
	}
	public void setIsNew(boolean value)
	{
		privateIsNew = value;
	}
	private String privateOriginalName;
	public String getOriginalName()
	{
		return privateOriginalName;
	}
	public void setOriginalName(String value)
	{
		privateOriginalName = value;
	}

	private EntityModel privateName;
	public EntityModel getName()
	{
		return privateName;
	}
	public void setName(EntityModel value)
	{
		privateName = value;
	}
	private EntityModel privateDescription;
	public EntityModel getDescription()
	{
		return privateDescription;
	}
	public void setDescription(EntityModel value)
	{
		privateDescription = value;
	}
	private ListModel privateStorageTypeList;
	public ListModel getStorageTypeList()
	{
		return privateStorageTypeList;
	}
	public void setStorageTypeList(ListModel value)
	{
		privateStorageTypeList = value;
	}
	private ListModel privateVersion;
	public ListModel getVersion()
	{
		return privateVersion;
	}
	public void setVersion(ListModel value)
	{
		privateVersion = value;
	}

	private int privateMaxNameLength;
	public int getMaxNameLength()
	{
		return privateMaxNameLength;
	}
	public void setMaxNameLength(int value)
	{
		privateMaxNameLength = value;
	}


	public DataCenterModel()
	{
		setName(new EntityModel());
		setDescription(new EntityModel());
		setVersion(new ListModel());

		setStorageTypeList(new ListModel());
		getStorageTypeList().getSelectedItemChangedEvent().addListener(this);
		getStorageTypeList().setItems(DataProvider.GetStoragePoolTypeList());
		setMaxNameLength(1);
		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object result)
			{
				DataCenterModel dataCenterModel = (DataCenterModel)model;
				dataCenterModel.setMaxNameLength((Integer)result);
			}};
		AsyncDataProvider.GetDataCenterMaxNameLength(_asyncQuery);

	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getStorageTypeList())
		{
			StorageType_SelectedItemChanged();
		}
	}

	private void StorageType_SelectedItemChanged()
	{
		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object result)
		{
			DataCenterModel dataCenterModel = (DataCenterModel)model;
			java.util.ArrayList<Version> versions = (java.util.ArrayList<Version>)result;

			//Rebuild version items.
			java.util.ArrayList<Version> list = new java.util.ArrayList<Version>();
			StorageType type = (StorageType)dataCenterModel.getStorageTypeList().getSelectedItem();

			for (Version item : versions)
			{
				if (DataProvider.IsVersionMatchStorageType(item, type))
				{
					list.add(item);
				}
			}

			if (type == StorageType.LOCALFS)
			{
				java.util.ArrayList<Version> tempList = new java.util.ArrayList<Version>();
				for (Version version : list)
				{
					Version version3_0 = new Version(3, 0);
					if (version.compareTo(version3_0) >= 0)
					{
						tempList.add(version);
					}
				}
				list = tempList;
			}

			Version selectedVersion = null;
			if (dataCenterModel.getVersion().getSelectedItem() != null)
			{
				selectedVersion = (Version)dataCenterModel.getVersion().getSelectedItem();
				boolean hasSelectedVersion = false;
				for (Version version : list)
				{
					if (selectedVersion.equals(version))
					{
						selectedVersion = version;
						hasSelectedVersion = true;
						break;
					}
				}
				if (!hasSelectedVersion)
				{
					selectedVersion = null;
				}
			}

			dataCenterModel.getVersion().setItems(list);

			if (selectedVersion == null)
			{
				dataCenterModel.getVersion().setSelectedItem(Linq.SelectHighestVersion(list));
				if (getEntity() != null)
				{
					InitVersion();
				}
			}
			else
			{
				dataCenterModel.getVersion().setSelectedItem(selectedVersion);
			}

		}};
		AsyncDataProvider.GetDataCenterVersions(_asyncQuery, getDataCenterId());
	}

	private boolean isVersionInit = false;
	private void InitVersion()
	{
		if (!isVersionInit)
		{
			isVersionInit = true;
			for (Object a : getVersion().getItems())
			{
				Version item = (Version)a;
				if (Version.OpEquality(item, getEntity().getcompatibility_version()))
				{
					getVersion().setSelectedItem(item);
					break;
				}
			}
		}
	}

	public boolean Validate()
	{
		String nameRegex = StringFormat.format("^[A-Za-z0-9_-]{1,%1$s}$", getMaxNameLength());
		String nameMessage = StringFormat.format("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters, max length: %1$s", getMaxNameLength());

		RegexValidation tempVar = new RegexValidation();
		tempVar.setExpression(nameRegex);
		tempVar.setMessage(nameMessage);
		getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar });
		getStorageTypeList().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		getVersion().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		//TODO: add this code to async validate.
		//string name = (string)Name.Entity;
		//if (String.Compare(name, OriginalName, true) != 0 && !DataProvider.IsDataCenterNameUnique(name))
		//{
		//    Name.IsValid = false;
		//    Name.InvalidityReasons.Add("Name must be unique.");
		//}

		return getName().getIsValid() && getDescription().getIsValid() && getStorageTypeList().getIsValid() && getVersion().getIsValid();
	}

}