package org.ovirt.engine.ui.uicommon.models;
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

import org.ovirt.engine.ui.uicommon.*;

@SuppressWarnings("unused")
public class ListWithDetailsModel extends SearchableListModel
{

	private java.util.List<EntityModel> detailModels;
	public java.util.List<EntityModel> getDetailModels()
	{
		return detailModels;
	}
	public void setDetailModels(java.util.List<EntityModel> value)
	{
		if (detailModels != value)
		{
			detailModels = value;
			OnPropertyChanged(new PropertyChangedEventArgs("DetailModels"));
		}
	}

	private EntityModel activeDetailModel;
	public EntityModel getActiveDetailModel()
	{
		return activeDetailModel;
	}
	public void setActiveDetailModel(EntityModel value)
	{
		if (activeDetailModel != value)
		{
			ActiveDetailModelChanging(value, getActiveDetailModel());
			activeDetailModel = value;
			ActiveDetailModelChanged();
			OnPropertyChanged(new PropertyChangedEventArgs("ActiveDetailModel"));
		}
	}

	private boolean detailModelsInitialized;


	protected void InitDetailModels()
	{
	}

	protected void UpdateDetailsAvailability()
	{
	}

	private void ActiveDetailModelChanging(EntityModel newValue, EntityModel oldValue)
	{
		//Make sure we had set an entity property of details model.
		if (oldValue != null)
		{
			oldValue.setEntity(null);

			if (oldValue instanceof SearchableListModel)
			{
				((SearchableListModel)oldValue).EnsureAsyncSearchStopped();
			}
		}

		if (newValue != null)
		{
			newValue.setEntity(ProvideDetailModelEntity(getSelectedItem()));
		}
	}

	protected Object ProvideDetailModelEntity(Object selectedItem)
	{
		return selectedItem;
	}

	@Override
	protected void OnSelectedItemChanged()
	{
		super.OnSelectedItemChanged();

		if (getSelectedItem() != null)
		{
			//Initialize detail models on demand, just after some item was selected.
			if (!detailModelsInitialized)
			{
				InitDetailModels();
				detailModelsInitialized = true;
			}
			//Try to choose default (first) detail model.
			UpdateDetailsAvailability();
			if (getDetailModels() != null)
			{
				if ((getActiveDetailModel() != null && !getActiveDetailModel().getIsAvailable()) || getActiveDetailModel() == null)
				{
					//ActiveDetailModel = DetailModels.FirstOrDefault(AvailabilityDecorator.GetIsAvailable);
					EntityModel model = null;
					for (EntityModel item : getDetailModels())
					{
						if (item.getIsAvailable())
						{
							model = item;
							break;
						}
					}
					setActiveDetailModel(model);
				}
			}

			//if (DetailModels != null && ActiveDetailModel == null)
			//{
			//    ActiveDetailModel = DetailModels.FirstOrDefault();
			//}
		}
		else
		{
			//If selected item become null, make sure we stop all activity on an active detail model.
			if (getActiveDetailModel() != null && getActiveDetailModel() instanceof SearchableListModel)
			{
				((SearchableListModel)getActiveDetailModel()).EnsureAsyncSearchStopped();
			}
		}

		//Syncronise selected item with the entity of an active details model.
		if (getActiveDetailModel() != null)
		{
			getActiveDetailModel().setEntity(ProvideDetailModelEntity(getSelectedItem()));
		}
	}

	protected void ActiveDetailModelChanged()
	{
	}

	@Override
	public void EnsureAsyncSearchStopped()
	{
		super.EnsureAsyncSearchStopped();

		if (getDetailModels() != null)
		{
			//Stop search on all list models.
			for (EntityModel model : getDetailModels())
			{
				if (model instanceof SearchableListModel)
				{
					SearchableListModel listModel = (SearchableListModel)model;
					listModel.EnsureAsyncSearchStopped();
				}
			}
		}
	}
}