package org.ovirt.engine.ui.uicommon.models.users;
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

import org.ovirt.engine.ui.uicommon.models.common.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class UserEventNotifierListModel extends SearchableListModel
{

	private UICommand privateManageEventsCommand;
	public UICommand getManageEventsCommand()
	{
		return privateManageEventsCommand;
	}
	private void setManageEventsCommand(UICommand value)
	{
		privateManageEventsCommand = value;
	}



	public DbUser getEntity()
	{
		return (DbUser)super.getEntity();
	}
	public void setEntity(DbUser value)
	{
		super.setEntity(value);
	}

	private Model window;
	public Model getWindow()
	{
		return window;
	}
	public void setWindow(Model value)
	{
		if (window != value)
		{
			window = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Window"));
		}
	}



	public UserEventNotifierListModel()
	{
		setTitle("Event Notifier");

		setManageEventsCommand(new UICommand("ManageEvents", this));
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();
		getSearchCommand().Execute();
	}

	@Override
	public void Search()
	{
		if (getEntity() != null)
		{
			super.Search();
		}
	}

	@Override
	protected void SyncSearch()
	{
		super.SyncSearch();

		super.SyncSearch(VdcQueryType.GetEventSubscribersBySubscriberIdGrouped, new GetEventSubscribersBySubscriberIdParameters(getEntity().getuser_id()));
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetEventSubscribersBySubscriberIdGrouped, new GetEventSubscribersBySubscriberIdParameters(getEntity().getuser_id())));
		setItems(getAsyncResult().getData());
	}

	public void ManageEvents()
	{
		EventNotificationModel model = new EventNotificationModel();
		model.setTitle("Add Event Notification");
		model.setHashName("add_event_notification");

		java.util.ArrayList<EventNotificationEntity> eventTypes = DataProvider.GetEventNotificationTypeList();
		java.util.Map<EventNotificationEntity, java.util.HashSet<AuditLogType>> availableEvents = DataProvider.GetAvailableNotificationEvents();

		//var tags = DataProvider.GetVisibleTagsList();
		//tags.Insert(0, new tags("", null, null, -1, "none"));

		Translator eventNotificationEntityTranslator = EnumTranslator.Create(EventNotificationEntity.class);
		Translator auditLogTypeTranslator = EnumTranslator.Create(AuditLogType.class);

		java.util.ArrayList<SelectionTreeNodeModel> list = new java.util.ArrayList<SelectionTreeNodeModel>();

		java.util.ArrayList<event_subscriber> items = getItems() == null ? new java.util.ArrayList<event_subscriber>() : Linq.<event_subscriber>Cast(getItems());
		for (EventNotificationEntity eventType : eventTypes)
		{
			SelectionTreeNodeModel stnm = new SelectionTreeNodeModel();
			stnm.setTitle(eventType.toString());
			stnm.setDescription(eventNotificationEntityTranslator.containsKey(eventType) ? eventNotificationEntityTranslator.get(eventType) : eventType.toString());
			list.add(stnm);

			for (AuditLogType logtype : availableEvents.get(eventType))
			{
				SelectionTreeNodeModel eventGrp = new SelectionTreeNodeModel();

				eventGrp.setTitle(logtype.toString());
				eventGrp.setDescription(auditLogTypeTranslator.containsKey(logtype) ? auditLogTypeTranslator.get(logtype) : logtype.toString());
				eventGrp.setParent(list.get(list.size() - 1));
				eventGrp.setIsSelectedNotificationPrevent(true);
				eventGrp.setIsSelectedNullable(false);
				for (event_subscriber es : items)
				{
					if (es.getevent_up_name().equals(logtype.toString()))
					{
						eventGrp.setIsSelectedNullable(true);
						break;
					}
				}
				//					eventGrp.IsSelected = items.Count(a => a.event_up_name.Equals(logtype.ToString())) > 0 ? true : false;

				list.get(list.size() - 1).getChildren().add(eventGrp);
				eventGrp.setIsSelectedNotificationPrevent(false);
			}
			if (list.get(list.size() - 1).getChildren().size() > 0)
			{
				list.get(list.size() - 1).getChildren().get(0).UpdateParentSelection();
			}
		}

		model.setEventGroupModels(list);
		//AddModel.Tags = tags;
		//AddModel.Tag = tags.FirstOrDefault();
		if (!StringHelper.isNullOrEmpty(getEntity().getemail()))
		{
			model.getEmail().setEntity(getEntity().getemail());
		}
		else if (items.size() > 0)
		{
			model.getEmail().setEntity(items.get(0).getmethod_address());
		}

		model.setOldEmail((String)model.getEmail().getEntity());

		setWindow(model);

		UICommand tempVar = new UICommand("OnSave", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnSave()
	{
		EventNotificationModel model = (EventNotificationModel)getWindow();

		if (!model.Validate())
		{
			return;
		}

		java.util.ArrayList<VdcActionParametersBase> toAddList = new java.util.ArrayList<VdcActionParametersBase>();
		java.util.ArrayList<VdcActionParametersBase> toRemoveList = new java.util.ArrayList<VdcActionParametersBase>();

		// var selected = model.EventGroupModels.SelectMany(a => a.Children).Where(a => a.IsSelected == true);
		java.util.ArrayList<SelectionTreeNodeModel> selected = new java.util.ArrayList<SelectionTreeNodeModel>();
		for (SelectionTreeNodeModel node : model.getEventGroupModels())
		{
			for (SelectionTreeNodeModel child : node.getChildren())
			{
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
				if (child.getIsSelectedNullable() != null && child.getIsSelectedNullable().equals(true))
				{
					selected.add(child);
				}
			}
		}

		java.util.ArrayList<event_subscriber> existing = getItems() != null ? Linq.<event_subscriber>Cast(getItems()) : new java.util.ArrayList<event_subscriber>();
		java.util.ArrayList<SelectionTreeNodeModel> added = new java.util.ArrayList<SelectionTreeNodeModel>();
		java.util.ArrayList<event_subscriber> removed = new java.util.ArrayList<event_subscriber>();

		// check what has been added:
		for (SelectionTreeNodeModel selectedEvent : selected)
		{
			boolean selectedInExisting = false;
			for (event_subscriber existingEvent : existing)
			{
				if (selectedEvent.getTitle().equals(existingEvent.getevent_up_name()))
				{
					selectedInExisting = true;
					break;
				}
			}

			if (!selectedInExisting)
			{
				added.add(selectedEvent);
			}
		}

		// check what has been deleted:
		for (event_subscriber existingEvent : existing)
		{
			boolean existingInSelected = false;
			for (SelectionTreeNodeModel selectedEvent : selected)
			{
				if (selectedEvent.getTitle().equals(existingEvent.getevent_up_name()))
				{
					existingInSelected = true;
					break;
				}
			}

			if (!existingInSelected)
			{
				removed.add(existingEvent);
			}
		}
		if (!StringHelper.isNullOrEmpty(model.getOldEmail()) && !model.getOldEmail().equals((String)model.getEmail().getEntity()))
		{
			for (event_subscriber a : existing)
			{
				toRemoveList.add(new EventSubscriptionParametesBase(new event_subscriber(a.getevent_up_name(), EventNotificationMethods.EMAIL.getValue(), a.getmethod_address(), a.getsubscriber_id(), ""), ""));
			}
			for (SelectionTreeNodeModel a : selected)
			{
				toAddList.add(new EventSubscriptionParametesBase(new event_subscriber(a.getTitle(), EventNotificationMethods.EMAIL.getValue(), (String)model.getEmail().getEntity(), getEntity().getuser_id(), ""), ""));
			}
		}
		else
		{
			//selected.Each(a => toAddList.Add(new EventSubscriptionParametesBase(new event_subscriber(a.Title, (int)EventNotificationMethods.EMAIL, model.Email.ValueAs<string>(), user.user_id, String.Empty), string.Empty)));
			for (SelectionTreeNodeModel a : added)
			{
				toAddList.add(new EventSubscriptionParametesBase(new event_subscriber(a.getTitle(), EventNotificationMethods.EMAIL.getValue(), (String)model.getEmail().getEntity(), getEntity().getuser_id(), ""), ""));
			}

			//existing.Each(a => toRemoveList.Add(new EventSubscriptionParametesBase(new event_subscriber(a.event_up_name, (int)EventNotificationMethods.EMAIL, a.method_address, a.subscriber_id, String.Empty), string.Empty)));
			for (event_subscriber a : removed)
			{
				toRemoveList.add(new EventSubscriptionParametesBase(new event_subscriber(a.getevent_up_name(), EventNotificationMethods.EMAIL.getValue(), a.getmethod_address(), a.getsubscriber_id(), ""), ""));
			}
		}

		if (toRemoveList.size() > 0)
		{
			for (VdcActionParametersBase param : toRemoveList)
			{
				Frontend.RunAction(VdcActionType.RemoveEventSubscription, param);
			}
		}

		if (toAddList.size() > 0)
		{
			Frontend.RunMultipleAction(VdcActionType.AddEventSubscription, toAddList);
		}
		Cancel();
	}

	public void Cancel()
	{
		setWindow(null);
	}

	@Override
	protected void ItemsChanged()
	{
		super.ItemsChanged();
		UpdateActionAvailability();
	}

	private void UpdateActionAvailability()
	{
		if (getEntity() == null || getEntity().getIsGroup() == true)
		{
			getManageEventsCommand().setIsExecutionAllowed(false);
		}
		else
		{
			getManageEventsCommand().setIsExecutionAllowed(true);
		}
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getManageEventsCommand())
		{
			ManageEvents();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnSave"))
		{
			OnSave();
		}
		if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
	}
}