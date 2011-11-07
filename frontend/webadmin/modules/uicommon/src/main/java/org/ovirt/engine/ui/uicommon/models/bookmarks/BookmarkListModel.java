package org.ovirt.engine.ui.uicommon.models.bookmarks;
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

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class BookmarkListModel extends SearchableListModel
{

	public static EventDefinition NavigatedEventDefinition;
	private Event privateNavigatedEvent;
	public Event getNavigatedEvent()
	{
		return privateNavigatedEvent;
	}
	private void setNavigatedEvent(Event value)
	{
		privateNavigatedEvent = value;
	}



	private UICommand privateNewCommand;
	public UICommand getNewCommand()
	{
		return privateNewCommand;
	}
	private void setNewCommand(UICommand value)
	{
		privateNewCommand = value;
	}
	private UICommand privateEditCommand;
	public UICommand getEditCommand()
	{
		return privateEditCommand;
	}
	private void setEditCommand(UICommand value)
	{
		privateEditCommand = value;
	}
	private UICommand privateRemoveCommand;
	public UICommand getRemoveCommand()
	{
		return privateRemoveCommand;
	}
	private void setRemoveCommand(UICommand value)
	{
		privateRemoveCommand = value;
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


	static
	{
		NavigatedEventDefinition = new EventDefinition("Navigated", BookmarkListModel.class);
	}

	public BookmarkListModel()
	{
		setNavigatedEvent(new Event(NavigatedEventDefinition));

		setNewCommand(new UICommand("New", this));
		setEditCommand(new UICommand("Edit", this));
		setRemoveCommand(new UICommand("Remove", this));

		getSearchCommand().Execute();

		UpdateActionAvailability();
	}

	@Override
	protected void SyncSearch()
	{
		super.SyncSearch();

		VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetAllBookmarks, new VdcQueryParametersBase());

		if (returnValue != null && returnValue.getSucceeded())
		{
			setItems((java.util.ArrayList<org.ovirt.engine.core.common.businessentities.bookmarks>)returnValue.getReturnValue());
		}
		else
		{
			setItems(new java.util.ArrayList<org.ovirt.engine.core.common.businessentities.bookmarks>());
		}
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();
		SyncSearch();
	}

	public void remove()
	{
		if (getWindow() != null)
		{
			return;
		}

		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Remove Bookmark(s)");
		model.setHashName("remove_bookmark");
		model.setMessage("Bookmark(s):");

		java.util.ArrayList<String> list = new java.util.ArrayList<String>();
		for (Object item : getSelectedItems())
		{
			org.ovirt.engine.core.common.businessentities.bookmarks i = (org.ovirt.engine.core.common.businessentities.bookmarks)item;
			list.add(i.getbookmark_name());
		}
		model.setItems(list);

		UICommand tempVar = new UICommand("OnRemove", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnRemove()
	{
		// 			Frontend.RunMultipleActions(VdcActionType.RemoveBookmark,
		// 				SelectedItems.Cast<bookmarks>()
		// 				.Select(a => (VdcActionParametersBase)new BookmarksParametersBase(a.bookmark_id))
		// 				.ToList()
		// 			);
		//List<VdcActionParametersBase> prms = new List<VdcActionParametersBase>();
		//foreach (object item in SelectedItems)
		//{
		//    org.ovirt.engine.core.common.businessentities.bookmarks i = (org.ovirt.engine.core.common.businessentities.bookmarks)item;
		//    prms.Add(new BookmarksParametersBase(i.bookmark_id));
		//}
		//Frontend.RunMultipleActions(VdcActionType.RemoveBookmark, prms);

		VdcReturnValueBase returnValue = Frontend.RunAction(VdcActionType.RemoveBookmark, new BookmarksParametersBase(((org.ovirt.engine.core.common.businessentities.bookmarks)getSelectedItem()).getbookmark_id()));
		if (returnValue != null && returnValue.getSucceeded())
		{
			getSearchCommand().Execute();
		}

		Cancel();
	}

	public void Edit()
	{
		org.ovirt.engine.core.common.businessentities.bookmarks bookmark = (org.ovirt.engine.core.common.businessentities.bookmarks)getSelectedItem();

		if (getWindow() != null)
		{
			return;
		}

		BookmarkModel model = new BookmarkModel();
		setWindow(model);
		model.setTitle("Edit Bookmark");
		model.setHashName("edit_bookmark");
		model.setIsNew(false);
		model.getName().setEntity(bookmark.getbookmark_name());
		model.getSearchString().setEntity(bookmark.getbookmark_value());

		UICommand tempVar = new UICommand("OnSave", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void New()
	{
		if (getWindow() != null)
		{
			return;
		}

		BookmarkModel model = new BookmarkModel();
		setWindow(model);
		model.setTitle("New Bookmark");
		model.setHashName("new_bookmark");
		model.setIsNew(true);
		model.getSearchString().setEntity(getSearchString());

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
		BookmarkModel model = (BookmarkModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		if (!model.Validate())
		{
			return;
		}

		org.ovirt.engine.core.common.businessentities.bookmarks tempVar = new org.ovirt.engine.core.common.businessentities.bookmarks();
		tempVar.setbookmark_id(model.getIsNew() ? (Guid)Guid.Empty : ((org.ovirt.engine.core.common.businessentities.bookmarks)getSelectedItem()).getbookmark_id());
		tempVar.setbookmark_name((String)model.getName().getEntity());
		tempVar.setbookmark_value((String)model.getSearchString().getEntity());
		org.ovirt.engine.core.common.businessentities.bookmarks bookmark = tempVar;


		model.StartProgress(null);

		Frontend.RunAction(model.getIsNew() ? VdcActionType.AddBookmark : VdcActionType.UpdateBookmark, new BookmarksOperationParameters(bookmark),
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

			BookmarkListModel localModel = (BookmarkListModel)result.getState();
			localModel.PostOnSave(result.getReturnValue());

			}
		}, this);
	}

	public void PostOnSave(VdcReturnValueBase returnValue)
	{
		BookmarkModel model = (BookmarkModel)getWindow();

		model.StopProgress();

		if (returnValue != null && returnValue.getSucceeded())
		{
			Cancel();
			getSearchCommand().Execute();
		}
	}

	public void Cancel()
	{
		setWindow(null);
	}

	@Override
	protected void OnSelectedItemChanged()
	{
		super.OnSelectedItemChanged();
		UpdateActionAvailability();

		if (getSelectedItem() != null)
		{
			getNavigatedEvent().raise(this, new BookmarkEventArgs((org.ovirt.engine.core.common.businessentities.bookmarks)getSelectedItem()));
		}
	}

	@Override
	protected void SelectedItemsChanged()
	{
		super.SelectedItemsChanged();
		UpdateActionAvailability();
	}

	private void UpdateActionAvailability()
	{
		getEditCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1);
		getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getNewCommand())
		{
			New();
		}
		else if (command == getEditCommand())
		{
			Edit();
		}
		else if (command == getRemoveCommand())
		{
			remove();
		}

		else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
		{
			OnRemove();
		}

		else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
		{
			OnSave();
		}
		else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
	}
}