package org.ovirt.engine.ui.uicommon.models.storage;
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
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public abstract class SanStorageModel extends SanStorageModelBase
{

	private boolean isGrouppedByTarget;
	/**
	 Gets or sets the value determining whether the
	 items containing target/LUNs or LUN/targets.
	*/
	public boolean getIsGrouppedByTarget()
	{
		return isGrouppedByTarget;
	}
	public void setIsGrouppedByTarget(boolean value)
	{
		if (isGrouppedByTarget != value)
		{
			isGrouppedByTarget = value;
			IsGrouppedByTargetChanged();
			OnPropertyChanged(new PropertyChangedEventArgs("IsGrouppedByTarget"));
		}
	}

	private boolean isAllLunsSelected;
	public boolean getIsAllLunsSelected()
	{
		return isAllLunsSelected;
	}
	public void setIsAllLunsSelected(boolean value)
	{
		if (isAllLunsSelected != value)
		{
			isAllLunsSelected = value;
			IsAllLunsSelectedChanged();
			OnPropertyChanged(new PropertyChangedEventArgs("IsAllLunsSelected"));
		}
	}

	private String getLUNsFailure;
	public String getGetLUNsFailure()
	{
		return getLUNsFailure;
	}
	public void setGetLUNsFailure(String value)
	{
		if (!StringHelper.stringsEqual(getLUNsFailure, value))
		{
			getLUNsFailure = value;
			OnPropertyChanged(new PropertyChangedEventArgs("GetLUNsFailure"));
		}
	}


	private java.util.List<LunModel> includedLUNs;
	private java.util.ArrayList<SanTargetModel> lastDiscoveredTargets;

	protected SanStorageModel()
	{
		includedLUNs = new java.util.ArrayList<LunModel>();
		lastDiscoveredTargets = new java.util.ArrayList<SanTargetModel>();

		InitializeItems(null, null);
	}

	@Override
	protected void PostDiscoverTargets(java.util.ArrayList<SanTargetModel> newItems)
	{
		super.PostDiscoverTargets(newItems);

		InitializeItems(null, newItems);

		//Remember all discovered targets.
		lastDiscoveredTargets.clear();
		lastDiscoveredTargets.addAll(newItems);
	}

	@Override
	protected void Update()
	{
		lastDiscoveredTargets.clear();

		super.Update();
	}

	@Override
	protected void UpdateInternal()
	{
		super.UpdateInternal();

		if (getContainer().getProgress() != null)
		{
			return;
		}

		VDS host = (VDS)getContainer().getHost().getSelectedItem();
		if (host == null)
		{
			ProposeDiscover();
			return;
		}


		ClearItems();
		InitializeItems(null, null);

		getContainer().StartProgress(null);

		Frontend.RunQuery(VdcQueryType.GetDeviceList, new GetDeviceListQueryParameters(host.getvds_id(), getType()), new AsyncQuery(this,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

			SanStorageModel model = (SanStorageModel)target;
			VdcQueryReturnValue response = (VdcQueryReturnValue)returnValue;
			if (response.getSucceeded())
			{
				model.ApplyData((java.util.ArrayList<LUNs>)response.getReturnValue(), false);
			}
			else
			{
				model.setGetLUNsFailure("Could not retrieve LUNs, please check your storage.");
			}
			model.getContainer().StopProgress();

			}
		}, true));
	}

	private void ClearItems()
	{
		if (getItems() == null)
		{
			return;
		}

		if (getIsGrouppedByTarget())
		{
			java.util.List<SanTargetModel> items = (java.util.List<SanTargetModel>)getItems();

			for (SanTargetModel target : Linq.ToList(items))
			{
				boolean found = false;

				//Ensure remove targets that are not in last dicovered targets list.
				if (Linq.FirstOrDefault(lastDiscoveredTargets, new Linq.TargetPredicate(target)) != null)
				{
					found = true;
				}
				else
				{
					//Ensure remove targets that are not contain already included LUNs.
					for (LunModel lun : target.getLuns())
					{
						LunModel foundItem = Linq.FirstOrDefault(includedLUNs, new Linq.LunPredicate(lun));
						if (foundItem == null)
						{
							found = true;
							break;
						}
					}
				}

				if (!found)
				{
					items.remove(target);
				}
			}
		}
		else
		{
			java.util.List<LunModel> items = (java.util.List<LunModel>)getItems();

			//Ensure remove targets that are not contain already included LUNs.
			for (LunModel lun : Linq.ToList(items))
			{
				LunModel foundItem = Linq.FirstOrDefault(includedLUNs, new Linq.LunPredicate(lun));
				if (foundItem == null)
				{
					items.remove(lun);
				}
			}
		}
	}

	/**
	 Creates model items from the provided list of business entities.
	*/
	public void ApplyData(java.util.List<LUNs> source, boolean isIncluded)
	{
		java.util.ArrayList<LunModel> newItems = new java.util.ArrayList<LunModel>();

		for (LUNs a : source)
		{
			if (a.getLunType() == getType() || a.getLunType() == StorageType.UNKNOWN)
			{
				java.util.ArrayList<SanTargetModel> targets = new java.util.ArrayList<SanTargetModel>();
				for (storage_server_connections b : a.getLunConnections())
				{
					SanTargetModel tempVar = new SanTargetModel();
					tempVar.setAddress(b.getconnection());
					tempVar.setPort(b.getport());
					tempVar.setName(b.getiqn());
					tempVar.setIsSelected(true);
					tempVar.setIsLoggedIn(true);
					tempVar.setLuns(new ObservableCollection<LunModel>());
					SanTargetModel model = tempVar;
					model.getLoginCommand().setIsExecutionAllowed(false);

					targets.add(model);
				}

				LunModel tempVar2 = new LunModel();
				tempVar2.setLunId(a.getLUN_id());
				tempVar2.setVendorId(a.getVendorId());
				tempVar2.setProductId(a.getProductId());
				tempVar2.setSerial(a.getSerial());
				tempVar2.setMultipathing(a.getPathCount());
				tempVar2.setTargets(targets);
				tempVar2.setSize(a.getDeviceSize());
				tempVar2.setIsAccessible(a.getAccessible());
				tempVar2.setIsIncluded(isIncluded);
				tempVar2.setIsSelected(isIncluded);
				LunModel lun = tempVar2;
				newItems.add(lun);

				//Remember included LUNs to prevent their removal while updating items.
				if (isIncluded)
				{
					includedLUNs.add(lun);
				}
			}
		}

		InitializeItems(newItems, null);
		ProposeDiscover();
	}

	private void IsGrouppedByTargetChanged()
	{
		InitializeItems(null, null);
	}

	/**
	 Organizes items according to the current groupping flag.
	 When new items provided takes them in account and add to the Items collection.
	*/
	private void InitializeItems(java.util.List<LunModel> newLuns, java.util.List<SanTargetModel> newTargets)
	{
		if (getIsGrouppedByTarget())
		{
			if (getItems() == null)
			{
				setItems(new ObservableCollection<SanTargetModel>());
			}
			else
			{
				//Convert to list of another type as neccessary.
				if (TypeUtil.<LunModel>IsListOf(getItems()))
				{
					setItems(ToTargetModelList((java.util.List<LunModel>)getItems()));
				}
			}

			java.util.List<SanTargetModel> items = (java.util.List<SanTargetModel>)getItems();

			//Add new targets.
			if (newTargets != null)
			{
				for (SanTargetModel newItem : newTargets)
				{
					if (Linq.FirstOrDefault(items, new Linq.TargetPredicate(newItem)) == null)
					{
						items.add(newItem);
					}
				}
			}

			//Merge luns into targets.
			if (newLuns != null)
			{
				MergeLunsToTargets(newLuns, items);
			}

			UpdateLoginAllAvailability();
		}
		else
		{
			if (getItems() == null)
			{
				setItems(new ObservableCollection<LunModel>());
			}
			else
			{
				//Convert to list of another type as neccessary.
				if (TypeUtil.<SanTargetModel>IsListOf(getItems()))
				{
					setItems(ToLunModelList((java.util.List<SanTargetModel>)getItems()));
				}
			}

			java.util.List<LunModel> items = (java.util.List<LunModel>)getItems();

			//Add new LUNs.
			if (newLuns != null)
			{
				for (LunModel newItem : newLuns)
				{
					if (Linq.FirstOrDefault(items, new Linq.LunPredicate(newItem)) == null)
					{
						items.add(newItem);
					}
				}
			}
		}
	}

	private void MergeLunsToTargets(java.util.List<LunModel> newLuns, java.util.List<SanTargetModel> targets)
	{
		for (LunModel lun : newLuns)
		{
			for (SanTargetModel target : lun.getTargets())
			{
				SanTargetModel item = Linq.FirstOrDefault(targets, new Linq.TargetPredicate(target));
				if (item == null)
				{
					item = target;
					targets.add(item);
				}

				if (Linq.FirstOrDefault(item.getLuns(), new Linq.LunPredicate(lun)) == null)
				{
					item.getLuns().add(lun);
				}
			}
		}
	}

	private java.util.List<SanTargetModel> ToTargetModelList(java.util.List<LunModel> source)
	{
		ObservableCollection<SanTargetModel> list = new ObservableCollection<SanTargetModel>();

		for (LunModel lun : source)
		{
			for (SanTargetModel target : lun.getTargets())
			{
				SanTargetModel item = Linq.FirstOrDefault(list, new Linq.TargetPredicate(target));
				if (item == null)
				{
					item = target;
					list.add(item);
				}

				if (Linq.FirstOrDefault(item.getLuns(), new Linq.LunPredicate(lun)) == null)
				{
					item.getLuns().add(lun);
				}
			}
		}

		//Merge with last discovered targets list.
		for (SanTargetModel target : lastDiscoveredTargets)
		{
			if (Linq.FirstOrDefault(list, new Linq.TargetPredicate(target)) == null)
			{
				list.add(target);
			}
		}

		return list;
	}

	private java.util.List<LunModel> ToLunModelList(java.util.List<SanTargetModel> source)
	{
		ObservableCollection<LunModel> list = new ObservableCollection<LunModel>();

		for (SanTargetModel target : source)
		{
			for (LunModel lun : target.getLuns())
			{
				LunModel item = Linq.FirstOrDefault(list, new Linq.LunPredicate(lun));
				if (item == null)
				{
					item = lun;
					list.add(item);
				}

				if (Linq.FirstOrDefault(item.getTargets(), new Linq.TargetPredicate(target)) == null)
				{
					item.getTargets().add(target);
				}
			}
		}

		return list;
	}

	private void ProposeDiscover()
	{
		if (!getProposeDiscoverTargets() && (getItems() == null || Linq.Count(getItems()) == 0))
		{
			setProposeDiscoverTargets(true);
		}
	}

	private void IsAllLunsSelectedChanged()
	{
		if (!getIsGrouppedByTarget())
		{
			java.util.List<LunModel> items = (java.util.List<LunModel>)getItems();
			for (LunModel lun : items)
			{
				if (!lun.getIsIncluded() && lun.getIsAccessible())
				{
					lun.setIsSelected(getIsAllLunsSelected());
				}
			}
		}
	}

	public java.util.ArrayList<LunModel> getAddedLuns()
	{
		java.util.ArrayList<LunModel> luns = new java.util.ArrayList<LunModel>();
		if (getIsGrouppedByTarget())
		{
			java.util.List<SanTargetModel> items = (java.util.List<SanTargetModel>)getItems();
			for (SanTargetModel item : items)
			{
				for (LunModel lun : item.getLuns())
				{
					if (lun.getIsSelected() && !lun.getIsIncluded() && Linq.FirstOrDefault(luns, new Linq.LunPredicate(lun)) == null)
					{
						luns.add(lun);
					}
				}
			}
		}
		else
		{
			java.util.List<LunModel> items = (java.util.List<LunModel>)getItems();
			for (LunModel lun : items)
			{
				if (lun.getIsSelected() && !lun.getIsIncluded() && Linq.FirstOrDefault(luns, new Linq.LunPredicate(lun)) == null)
				{
					luns.add(lun);
				}
			}
		}

		return luns;
	}

	@Override
	public boolean Validate()
	{
		setIsValid(getAddedLuns().size() > 0 || includedLUNs.size() > 0);

		return super.Validate() && getIsValid();
	}
}