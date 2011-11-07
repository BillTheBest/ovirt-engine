package org.ovirt.engine.ui.uicommon.models.templates;
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

import org.ovirt.engine.ui.uicommon.models.vms.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommon.dataprovider.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class TemplateInterfaceListModel extends SearchableListModel
{

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



	//TODO: Check if we really need the following property.
	private VmTemplate getEntityStronglyTyped()
	{
		Object tempVar = getEntity();
		return (VmTemplate)((tempVar instanceof VmTemplate) ? tempVar : null);
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


	public TemplateInterfaceListModel()
	{
		setTitle("Network Interfaces");

		setNewCommand(new UICommand("New", this));
		setEditCommand(new UICommand("Edit", this));
		setRemoveCommand(new UICommand("Remove", this));

		UpdateActionAvailability();
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		getSearchCommand().Execute();
		UpdateActionAvailability();
	}

	@Override
	public void Search()
	{
		if (getEntityStronglyTyped() != null)
		{
			super.Search();
		}
	}

	@Override
	protected void SyncSearch()
	{
		super.SyncSearch(VdcQueryType.GetTemplateInterfacesByTemplateId, new GetVmTemplateParameters(getEntityStronglyTyped().getId()));
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetTemplateInterfacesByTemplateId, new GetVmTemplateParameters(getEntityStronglyTyped().getId())));
		setItems(getAsyncResult().getData());
	}

	private void New()
	{
		if (getWindow() != null)
		{
			return;
		}

		VmInterfaceModel model = new VmInterfaceModel();
		setWindow(model);
		model.setTitle("New Network Interface");
		model.setIsNew(true);

		AsyncDataProvider.GetClusterNetworkList(new AsyncQuery(this,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

			TemplateInterfaceListModel vmInterfaceListModel = (TemplateInterfaceListModel)target;
			java.util.ArrayList<network> network_list = returnValue != null ? (java.util.ArrayList<network>)returnValue : new java.util.ArrayList<network>();
			vmInterfaceListModel.PostGetClusterNetworkList_New(network_list);

			}
		}),getEntityStronglyTyped().getvds_group_id());
	}

	public void PostGetClusterNetworkList_New(java.util.ArrayList<network> network_list)
	{
		java.util.ArrayList<network> networks = new java.util.ArrayList<network>();
		for (network a : network_list)
		{
			if (a.getStatus() == NetworkStatus.Operational)
			{
				networks.add(a);
			}
		}

		java.util.ArrayList<VmNetworkInterface> nics = Linq.<VmNetworkInterface>Cast(getItems());
		int nicCount = nics.size();
		String newNicName = DataProvider.GetNewNicName(nics);

		VmInterfaceModel model = (VmInterfaceModel)getWindow();
		model.getNetwork().setItems(networks);
		model.getNetwork().setSelectedItem(networks.size() > 0 ? networks.get(0) : null);
		model.getNicType().setItems(DataProvider.GetNicTypeList(getEntityStronglyTyped().getos(), false));
		model.getNicType().setSelectedItem(DataProvider.GetDefaultNicType(getEntityStronglyTyped().getos()));
		model.getName().setEntity(newNicName);
		model.getMAC().setIsAvailable(false);

		UICommand tempVar = new UICommand("OnSave", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private void Edit()
	{
		if (getWindow() != null)
		{
			return;
		}

		VmInterfaceModel model = new VmInterfaceModel();
		setWindow(model);
		model.setTitle("Edit Network Interface");

		AsyncDataProvider.GetClusterNetworkList(new AsyncQuery(this,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

			TemplateInterfaceListModel vmInterfaceListModel = (TemplateInterfaceListModel)target;
			java.util.ArrayList<network> network_list = returnValue != null ? (java.util.ArrayList<network>)returnValue : new java.util.ArrayList<network>();
			vmInterfaceListModel.PostGetClusterNetworkList_Edit(network_list);

			}
		}),getEntityStronglyTyped().getvds_group_id());
	}

	public void PostGetClusterNetworkList_Edit(java.util.ArrayList<network> network_list)
	{
		VmNetworkInterface nic = (VmNetworkInterface)getSelectedItem();
		int nicCount = Linq.<VmNetworkInterface>Cast(getItems()).size();
		java.util.ArrayList<network> networks = new java.util.ArrayList<network>();
		for (network a : network_list)
		{
			if (a.getStatus() == NetworkStatus.Operational)
			{
				networks.add(a);
			}
		}

		VmInterfaceModel model = (VmInterfaceModel)getWindow();
		model.getNetwork().setItems(networks);
		network network = null;
		for (network a : networks)
		{
			if (StringHelper.stringsEqual(a.getname(), nic.getNetworkName()))
			{
				network = a;
				break;
			}
		}
		model.getNetwork().setSelectedItem(network);

		Integer selectedNicType = nic.getType();
		java.util.ArrayList<VmInterfaceType> nicTypes = DataProvider.GetNicTypeList(getEntityStronglyTyped().getos(), VmInterfaceType.forValue(selectedNicType) == VmInterfaceType.rtl8139_pv);
		model.getNicType().setItems(nicTypes);

		if (selectedNicType == null || !nicTypes.contains(VmInterfaceType.forValue(selectedNicType)))
		{
			selectedNicType = DataProvider.GetDefaultNicType(getEntityStronglyTyped().getos()).getValue();
		}

		model.getNicType().setSelectedItem(VmInterfaceType.forValue(selectedNicType));

		model.getName().setEntity(nic.getName());
		model.getMAC().setIsAvailable(false);


		UICommand tempVar = new UICommand("OnSave", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private void OnSave()
	{
		VmInterfaceModel model = (VmInterfaceModel)getWindow();
		VmNetworkInterface nic = model.getIsNew() ? new VmNetworkInterface() : (VmNetworkInterface)Cloner.clone((VmNetworkInterface)getSelectedItem());

		if (!model.Validate())
		{
			return;
		}

		//Save changes.
		nic.setName((String)model.getName().getEntity());
		nic.setNetworkName(((network)model.getNetwork().getSelectedItem()).getname());
		if (model.getNicType().getSelectedItem() == null)
		{
			nic.setType(null);
		}
		else
		{
			nic.setType(((VmInterfaceType)model.getNicType().getSelectedItem()).getValue());
		}
		nic.setMacAddress(model.getMAC().getIsChangable() ? (model.getMAC().getEntity() == null ? null : ((String)(model.getMAC().getEntity())).toLowerCase()) : model.getIsNew() ? "" : nic.getMacAddress());


		if (model.getIsNew())
		{
			Frontend.RunMultipleAction(VdcActionType.AddVmTemplateInterface, new java.util.ArrayList<VdcActionParametersBase>(java.util.Arrays.asList(new VdcActionParametersBase[] { new AddVmTemplateInterfaceParameters(getEntityStronglyTyped().getId(), nic) })),
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

				Cancel();

			}
		}, null);
		}
		else
		{
			Frontend.RunMultipleAction(VdcActionType.UpdateVmTemplateInterface, new java.util.ArrayList<VdcActionParametersBase>(java.util.Arrays.asList(new VdcActionParametersBase[] { new AddVmTemplateInterfaceParameters(getEntityStronglyTyped().getId(), nic) })),
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

				Cancel();

			}
		}, null);
		}
	}

	private void remove()
	{
		if (getWindow() != null)
		{
			return;
		}

		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Remove Network Interface(s)");
		model.setMessage("Network Interface(s)");

		java.util.ArrayList<String> items = new java.util.ArrayList<String>();
		for (Object item : getSelectedItems())
		{
			VmNetworkInterface a = (VmNetworkInterface)item;
			items.add(a.getName());
		}
		model.setItems(items);

		UICommand tempVar = new UICommand("OnRemove", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private void OnRemove()
	{
		ConfirmationModel model = (ConfirmationModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VmNetworkInterface a = (VmNetworkInterface)item;
			list.add(new RemoveVmTemplateInterfaceParameters(getEntityStronglyTyped().getId(), a.getId()));
		}

		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.RemoveVmTemplateInterface, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ConfirmationModel localModel = (ConfirmationModel)result.getState();
			localModel.StopProgress();
			Cancel();

			}
		}, model);
	}

	private void Cancel()
	{
		setWindow(null);
	}

	@Override
	protected void SelectedItemsChanged()
	{
		super.SelectedItemsChanged();
		UpdateActionAvailability();
	}

	@Override
	protected void OnSelectedItemChanged()
	{
		super.OnSelectedItemChanged();
		UpdateActionAvailability();
	}

	private void UpdateActionAvailability()
	{
		getEditCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() == 1 && getSelectedItem() != null);
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
		else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
		{
			OnSave();
		}
		else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
		{
			OnRemove();
		}
	}
}