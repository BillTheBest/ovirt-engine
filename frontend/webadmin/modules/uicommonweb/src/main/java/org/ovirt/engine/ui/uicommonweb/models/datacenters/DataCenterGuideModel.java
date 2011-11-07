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

import org.ovirt.engine.ui.uicommonweb.dataprovider.*;
import org.ovirt.engine.ui.uicommonweb.models.clusters.*;
import org.ovirt.engine.ui.uicommonweb.models.hosts.*;
import org.ovirt.engine.ui.uicommonweb.models.storage.*;
import org.ovirt.engine.ui.uicommonweb.validation.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.interfaces.*;

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class DataCenterGuideModel extends GuideModel implements ITaskTarget
{

	public final String DataCenterConfigureClustersAction = "Configure Cluster";
	public final String DataCenterAddAnotherClusterAction = "Add another Cluster";
	public final String DataCenterConfigureHostsAction = "Configure Host";
	public final String DataCenterSelectHostsAction = "Select Hosts";
	public final String DataCenterConfigureStorageAction = "Configure Storage";
	public final String DataCenterAddMoreStorageAction = "Add more Storage";
	public final String DataCenterAttachStorageAction = "Attach Storage";
	public final String DataCenterAttachMoreStorageAction = "Attach more Storage";
	public final String DataCenterConfigureISOLibraryAction = "Configure ISO Library";
	public final String DataCenterAttachISOLibraryAction = "Attach ISO Library";

	public final String NoUpHostReason = "There should be at least one active Host in the Data Center";
	public final String NoDataDomainAttachedReason = "Cannot create an ISO domain in a non-active Data Center";



	public storage_pool getEntity()
	{
		return (storage_pool)super.getEntity();
	}
	public void setEntity(storage_pool value)
	{
		super.setEntity(value);
	}



	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();
		UpdateOptions();
	}

	private void UpdateOptions()
	{
		getCompulsoryActions().clear();
		getOptionalActions().clear();

		if (getEntity() != null)
		{
			if (getEntity().getstorage_pool_type() != StorageType.LOCALFS)
			{
				//Add cluster action.
				java.util.ArrayList<VDSGroup> clusters = DataProvider.GetClusterList(getEntity().getId());

				UICommand addClusterAction = new UICommand("AddCluster", this);
				if (clusters.isEmpty())
				{
					addClusterAction.setTitle(DataCenterConfigureClustersAction);
					getCompulsoryActions().add(addClusterAction);
				}
				else
				{
					addClusterAction.setTitle(DataCenterAddAnotherClusterAction);
					getOptionalActions().add(addClusterAction);
				}

				//Add host action.
				//Version minimalClusterVersion = clusters.Min(a => a.compatibility_version);
				Version minimalClusterVersion = Linq.GetMinVersionByClusters(clusters);

				if (minimalClusterVersion == null)
				{
					minimalClusterVersion = new Version();
				}

				java.util.ArrayList<VDS> hosts = new java.util.ArrayList<VDS>();
				java.util.ArrayList<VDS> availableHosts = new java.util.ArrayList<VDS>();
				java.util.ArrayList<VDS> upHosts = new java.util.ArrayList<VDS>();
				for (VDS vds : DataProvider.GetHostList())
				{
					//var hosts = DataProvider.GetHostList()
					//    .Where(a => clusters.Any(b => b.ID == a.vds_group_id)
					//           && (a.Version.FullVersion == null || a.Version.FullVersion.GetFriendlyVersion() >= minimalClusterVersion))
					//    .ToList();
					if (Linq.IsClusterItemExistInList(clusters, vds.getvds_group_id()) && (vds.getVersion() == null || vds.getVersion().getFullVersion() == null || Extensions.GetFriendlyVersion(vds.getVersion().getFullVersion()).compareTo(minimalClusterVersion) >= 0))
					{
						hosts.add(vds);
					}

					//var availableHosts = DataProvider.GetHostList()
					//    .Where(a => clusters.All(b => b.ID != a.vds_group_id)
					//        && (a.status == VDSStatus.Maintenance || a.status == VDSStatus.PendingApproval)
					//        && (a.Version.FullVersion == null || a.Version.FullVersion.GetFriendlyVersion() >= minimalClusterVersion))
					//    .ToList();
					if ((!Linq.IsHostBelongsToAnyOfClusters(clusters, vds)) && (vds.getstatus() == VDSStatus.Maintenance || vds.getstatus() == VDSStatus.PendingApproval) && (vds.getVersion().getFullVersion() == null || Extensions.GetFriendlyVersion(vds.getVersion().getFullVersion()).compareTo(minimalClusterVersion) >= 0))
					{
						availableHosts.add(vds);
					}

					//var upHosts = DataProvider.GetHostList().Where(a => a.status == VDSStatus.Up).ToList();
					if (vds.getstatus() == VDSStatus.Up && Linq.IsClusterItemExistInList(clusters, vds.getvds_group_id()))
					{
						upHosts.add(vds);
					}
				}

				UICommand tempVar = new UICommand("AddHost", this);
				tempVar.setIsExecutionAllowed(clusters.size() > 0);
				UICommand addHostAction = tempVar;

				addHostAction.setTitle(DataCenterConfigureHostsAction);
				getCompulsoryActions().add(addHostAction);


				//Select host action.
				UICommand selectHostAction = new UICommand("SelectHost", this);

				if (availableHosts.size() > 0 && clusters.size() > 0)
				{
					if (hosts.isEmpty())
					{
						selectHostAction.setTitle(DataCenterSelectHostsAction);
						getCompulsoryActions().add(selectHostAction);
					}
					else
					{
						selectHostAction.setTitle(DataCenterSelectHostsAction);
						getOptionalActions().add(selectHostAction);
					}
				}

				java.util.ArrayList<storage_domains> allStorage = DataProvider.GetStorageDomainList();
				java.util.ArrayList<storage_domains> unattachedStorage = new java.util.ArrayList<storage_domains>();
				boolean addToList;
				Version version3_0 = new Version(3, 0);
				for (storage_domains item : allStorage)
				{
					addToList = false;
					if (item.getstorage_domain_type() == StorageDomainType.Data && item.getstorage_type() == getEntity().getstorage_pool_type() && item.getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached)
					{
						if (getEntity().getStoragePoolFormatType() == null)
						{
							//compat logic: in case its not v1 and the version is less than 3.0 break.
							if (item.getStorageStaticData().getStorageFormat() != StorageFormatType.V1 && getEntity().getcompatibility_version().compareTo(version3_0) < 0)
							{
								continue;
							}
							addToList = true;
						}
						else if ((StorageFormatType)getEntity().getStoragePoolFormatType() == item.getStorageStaticData().getStorageFormat())
						{
							addToList = true;
						}
					}

					if (addToList)
					{
						unattachedStorage.add(item);
					}
				}

				java.util.ArrayList<storage_domains> attachedStorage = DataProvider.GetStorageDomainList(getEntity().getId());

				java.util.ArrayList<storage_domains> attachedDataStorages = new java.util.ArrayList<storage_domains>();
				for (storage_domains a : attachedStorage)
				{
					if (a.getstorage_domain_type() == StorageDomainType.Data || a.getstorage_domain_type() == StorageDomainType.Master)
					{
						attachedDataStorages.add(a);
					}
				}

				UICommand tempVar2 = new UICommand("AddDataStorage", this);
				tempVar2.setIsExecutionAllowed(upHosts.size() > 0);
				UICommand addDataStorageAction = tempVar2;
				addDataStorageAction.getExecuteProhibitionReasons().add(NoUpHostReason);

				if (unattachedStorage.isEmpty() && attachedDataStorages.isEmpty())
				{
					addDataStorageAction.setTitle(DataCenterConfigureStorageAction);
					getCompulsoryActions().add(addDataStorageAction);
				}
				else
				{
					addDataStorageAction.setTitle(DataCenterAddMoreStorageAction);
					getOptionalActions().add(addDataStorageAction);
				}

				//Attach data storage action.
				UICommand tempVar3 = new UICommand("AttachDataStorage", this);
				tempVar3.setIsExecutionAllowed(unattachedStorage.size() > 0 && upHosts.size() > 0);
				UICommand attachDataStorageAction = tempVar3;
				if (upHosts.isEmpty())
				{
					attachDataStorageAction.getExecuteProhibitionReasons().add(NoUpHostReason);
				}
				if (attachedDataStorages.isEmpty())
				{
					attachDataStorageAction.setTitle(DataCenterAttachStorageAction);
					getCompulsoryActions().add(attachDataStorageAction);
				}
				else
				{
					attachDataStorageAction.setTitle(DataCenterAttachMoreStorageAction);
					getOptionalActions().add(attachDataStorageAction);
				}

				java.util.ArrayList<storage_domains> isoStorages = DataProvider.GetISOStorageDomainList();

				UICommand tempVar4 = new UICommand("AddIsoStorage", this);
				tempVar4.setIsExecutionAllowed(getEntity().getstatus() == StoragePoolStatus.Up);
				UICommand addIsoStorageAction = tempVar4;
				addIsoStorageAction.getExecuteProhibitionReasons().add(NoDataDomainAttachedReason);

				if (isoStorages.isEmpty())
				{
					addIsoStorageAction.setTitle(DataCenterConfigureISOLibraryAction);
					getOptionalActions().add(addIsoStorageAction);
				}

				//Attach ISO storage action.
				// Allow to attach ISO domain only when there are Data storages attached
				// and there ISO storages to attach and ther are no ISO storages actually
				// attached.
				//var attachedIsoStorages = attachedStorage
				//    .Where(a => a.storage_domain_type == StorageDomainType.ISO)
				//    .ToList();
				java.util.ArrayList<storage_domains> attachedIsoStorages = new java.util.ArrayList<storage_domains>();
				for (storage_domains sd : attachedStorage)
				{
					if (sd.getstorage_domain_type() == StorageDomainType.ISO)
					{
						attachedIsoStorages.add(sd);
					}
				}

				//bool attachIsoAllowed =
				//    (attachedDataStorages.Count > 0
				//     && attachedDataStorages.Any(a => a.storage_domain_type == StorageDomainType.Master && a.status.HasValue && a.status.Value == StorageDomainStatus.Active)
				//     && isoStorages.Count() > 0
				//     && attachedIsoStorages.Count == 0);
				boolean attachIsoAllowed = (attachedDataStorages.size() > 0 && Linq.IsAnyStorageDomainIsMatserAndActive(attachedDataStorages) && isoStorages.size() > 0 && attachedIsoStorages.isEmpty() && upHosts.size() > 0);

				// The action is available if there are no storages attached to the
				// Data Center. It will not always be allowed.
				boolean attachIsoAvailable = attachedIsoStorages.isEmpty();

				UICommand tempVar5 = new UICommand("AttachIsoStorage", this);
				tempVar5.setIsExecutionAllowed(attachIsoAllowed);
				tempVar5.setIsAvailable(attachIsoAvailable);
				UICommand attachIsoStorageAction = tempVar5;
				if (upHosts.isEmpty())
				{
					attachIsoStorageAction.getExecuteProhibitionReasons().add(NoUpHostReason);
				}

				if (attachIsoAvailable)
				{
					attachIsoStorageAction.setTitle(DataCenterAttachISOLibraryAction);
					getOptionalActions().add(attachIsoStorageAction);
				}
			}
			else
			{
				java.util.ArrayList<VDSGroup> clusters = DataProvider.GetClusterList(getEntity().getId());

				UICommand addClusterAction = new UICommand("AddCluster", this);
				if (clusters.isEmpty())
				{
					addClusterAction.setTitle(DataCenterConfigureClustersAction);
					getCompulsoryActions().add(addClusterAction);
				}
				else
				{
					UICommand tempVar6 = new UICommand("AddHost", this);
					tempVar6.setTitle(DataCenterConfigureHostsAction);
					UICommand addHostAction = tempVar6;
					UICommand tempVar7 = new UICommand("SelectHost", this);
					tempVar7.setTitle(DataCenterSelectHostsAction);
					UICommand selectHost = tempVar7;
					VdcQueryReturnValue retVal = Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Hosts: datacenter!= " + getEntity().getname() + " status=maintenance or status=pendingapproval ", SearchType.VDS));
					boolean hasMaintenance3_0Host = false;
					if (retVal != null && retVal.getSucceeded())
					{
						java.util.ArrayList<VDS> list = (java.util.ArrayList<VDS>)retVal.getReturnValue();
						Version version3_0 = new Version(3, 0);
						for (VDS vds : list)
						{
							String[] hostVersions = vds.getsupported_cluster_levels().split("[,]", -1);
							for (String hostVersion : hostVersions)
							{
								if (version3_0.compareTo(new Version(hostVersion)) <= 0)
								{
									hasMaintenance3_0Host = true;
									break;
								}
							}
							if (hasMaintenance3_0Host)
							{
								break;
							}
						}
					}

					VDS host = DataProvider.GetLocalStorageHost(getEntity().getname());
					if (host != null)
					{
						addHostAction.setIsExecutionAllowed(false);
						selectHost.setIsExecutionAllowed(false);
						String hasHostReason = "Local Data Center already contains a Host";
						addHostAction.getExecuteProhibitionReasons().add(hasHostReason);
						selectHost.getExecuteProhibitionReasons().add(hasHostReason);
						if (host.getstatus() == VDSStatus.Up)
						{
							UICommand tempVar8 = new UICommand("AddLocalStorage", this);
							tempVar8.setTitle("Add Local Storage");
							UICommand addLocalStorageAction = tempVar8;
							getOptionalActions().add(addLocalStorageAction);
						}
					}
					else if(getEntity().getstatus() != StoragePoolStatus.Uninitialized)
					{
						addHostAction.setIsExecutionAllowed(false);
						selectHost.setIsExecutionAllowed(false);
						String dataCenterInitializeReason = "Data Center was already initialized";
						addHostAction.getExecuteProhibitionReasons().add(dataCenterInitializeReason);
						selectHost.getExecuteProhibitionReasons().add(dataCenterInitializeReason);
					}

					if (hasMaintenance3_0Host)
					{
						getOptionalActions().add(selectHost);
					}
					getCompulsoryActions().add(addHostAction);
				}
			}
		}
	}

	private void AddLocalStorage()
	{
		StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
		setWindow(model);
		model.setTitle("New Local Domain");
		model.setHashName("new_local_domain");
		LocalStorageModel localStorageModel = new LocalStorageModel();
		localStorageModel.setRole(StorageDomainType.Data);

		java.util.ArrayList<IStorageModel> list = new java.util.ArrayList<IStorageModel>();
		list.add(localStorageModel);
		model.setItems(list);
		model.setSelectedItem(list.get(0));

		VDS localHost = DataProvider.GetLocalStorageHost(getEntity().getname());
		model.getHost().setItems(new java.util.ArrayList<VDS>(java.util.Arrays.asList(new VDS[] { localHost })));
		model.getHost().setSelectedItem(localHost);
		model.getDataCenter().setItems(new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { getEntity() })));
		model.getDataCenter().setSelectedItem(getEntity());
		UICommand tempVar = new UICommand("OnAddStorage", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void AddIsoStorage()
	{
		AddStorageInternal("New ISO Library", StorageDomainType.ISO);
	}

	public void AddDataStorage()
	{
		AddStorageInternal("New Storage", StorageDomainType.Data);
	}

	private void AddStorageInternal(String title, StorageDomainType type)
	{
		StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
		setWindow(model);
		model.setTitle(title);
		model.setHashName("new_domain");
		model.getDataCenter().setSelectedItem(getEntity());
		model.getDataCenter().setIsChangable(false);

		java.util.ArrayList<IStorageModel> items = new java.util.ArrayList<IStorageModel>();

		if (type == StorageDomainType.Data)
		{
			NfsStorageModel tempVar = new NfsStorageModel();
			tempVar.setRole(StorageDomainType.Data);
			items.add(tempVar);
			IscsiStorageModel tempVar2 = new IscsiStorageModel();
			tempVar2.setRole(StorageDomainType.Data);
			tempVar2.setIsGrouppedByTarget(true);
			items.add(tempVar2);
			FcpStorageModel tempVar3 = new FcpStorageModel();
			tempVar3.setRole(StorageDomainType.Data);
			items.add(tempVar3);
			LocalStorageModel tempVar4 = new LocalStorageModel();
			tempVar4.setRole(StorageDomainType.Data);
			items.add(tempVar4);
		}
		else if (type == StorageDomainType.ISO)
		{
			NfsStorageModel tempVar5 = new NfsStorageModel();
			tempVar5.setRole(StorageDomainType.ISO);
			items.add(tempVar5);
		}

		model.setItems(items);

		model.Initialize();


		UICommand tempVar6 = new UICommand("OnAddStorage", this);
		tempVar6.setTitle("OK");
		tempVar6.setIsDefault(true);
		model.getCommands().add(tempVar6);
		UICommand tempVar7 = new UICommand("Cancel", this);
		tempVar7.setTitle("Cancel");
		tempVar7.setIsCancel(true);
		model.getCommands().add(tempVar7);
	}

	public void OnAddStorage()
	{
		StorageModel model = (StorageModel)getWindow();
		String storageName = (String)model.getName().getEntity();

		AsyncDataProvider.IsStorageDomainNameUnique(new AsyncQuery(this,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

			DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel)target;
			StorageModel storageModel = (StorageModel)dataCenterGuideModel.getWindow();
			String name = (String)storageModel.getName().getEntity();
			String tempVar = storageModel.getOriginalName();
			String originalName = (tempVar != null) ? tempVar : "";
			boolean isNameUnique = (Boolean)returnValue;
			if (!isNameUnique && name.compareToIgnoreCase(originalName) != 0)
			{
				storageModel.getName().getInvalidityReasons().add("Name must be unique.");
				storageModel.getName().setIsValid(false);
			}
			AsyncDataProvider.GetStorageDomainMaxNameLength(new AsyncQuery(dataCenterGuideModel,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target1, Object returnValue1) {

				DataCenterGuideModel dataCenterGuideModel1 = (DataCenterGuideModel)target1;
				StorageModel storageModel1 = (StorageModel)dataCenterGuideModel1.getWindow();
				int nameMaxLength = (Integer)returnValue1;
				RegexValidation tempVar2 = new RegexValidation();
				tempVar2.setExpression("^[A-Za-z0-9_-]{1," + nameMaxLength + "}$");
				tempVar2.setMessage("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters, max length: " + nameMaxLength);
				storageModel1.getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar2 });
				dataCenterGuideModel1.PostOnAddStorage();

			}
		}));

			}
		}), storageName);
	}

	public void PostOnAddStorage()
	{
		StorageModel model = (StorageModel)getWindow();

		if (!model.Validate())
		{
			return;
		}

		//Save changes.
		if (model.getSelectedItem() instanceof NfsStorageModel)
		{
			SaveNfsStorage();
		}
		else if (model.getSelectedItem() instanceof LocalStorageModel)
		{
			SaveLocalStorage();
		}
		else
		{
			SaveSanStorage();
		}
	}

	private void SaveLocalStorage()
	{
		if (getWindow().getProgress() != null)
		{
			return;
		}

		getWindow().StartProgress(null);

		Task.Create(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "SaveLocal" }))).Run();
	}

	private void SaveLocalStorage(TaskContext context)
	{
		StorageModel model = (StorageModel)getWindow();
		LocalStorageModel localModel = (LocalStorageModel)model.getSelectedItem();
		VDS host = (VDS)model.getHost().getSelectedItem();


		String path = (String)localModel.getPath().getEntity();
		String storageName = null;
		RefObject<String> tempRef_storageName = new RefObject<String>(storageName);
		boolean tempVar = DataProvider.IsDomainAlreadyExist(host.getstorage_pool_id(), path, tempRef_storageName);
			storageName = tempRef_storageName.argvalue;
		if (tempVar)
		{
			context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "Finish", false, localModel, "Create Operation failed. Domain " + storageName + " already exists in the system." })));

			return;
		}


		//Create storage connection.
		storage_server_connections tempVar2 = new storage_server_connections();
		tempVar2.setconnection(path);
		tempVar2.setstorage_type(localModel.getType());
		storage_server_connections connection = tempVar2;

		storage_domain_static tempVar3 = new storage_domain_static();
		tempVar3.setstorage_name((String)model.getName().getEntity());
		tempVar3.setstorage_type(localModel.getType());
		tempVar3.setstorage_domain_type(localModel.getRole());
		storage_domain_static storageDomain = tempVar3;

		boolean removeConnection = false;

		VdcReturnValueBase returnValue = Frontend.RunAction(VdcActionType.AddStorageServerConnection, new StorageServerConnectionParametersBase(connection, host.getvds_id()));

		if (returnValue != null && returnValue.getSucceeded())
		{
			storageDomain.setstorage((String)returnValue.getActionReturnValue());

			//Add storage domain.
			StorageDomainManagementParameter tempVar4 = new StorageDomainManagementParameter(storageDomain);
			tempVar4.setVdsId(host.getvds_id());
			returnValue = Frontend.RunAction(VdcActionType.AddLocalStorageDomain, tempVar4);

			if (returnValue == null || !returnValue.getSucceeded())
			{
				removeConnection = true;
			}
		}

		//Clean up connection in case of storage creation failure.
		if (removeConnection)
		{
			Frontend.RunAction(VdcActionType.RemoveStorageServerConnection, new StorageServerConnectionParametersBase(connection, host.getvds_id()));
		}


		context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "Finish", returnValue != null && returnValue.getSucceeded(), localModel, null })));
	}

	private void SaveNfsStorage()
	{
		if (getWindow().getProgress() != null)
		{
			return;
		}

		getWindow().StartProgress(null);

		Task.Create(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "SaveNfs" }))).Run();
	}

	private void SaveNfsStorage(TaskContext context)
	{
		StorageModel model = (StorageModel)getWindow();
		NfsStorageModel nfsModel = (NfsStorageModel)model.getSelectedItem();
		VDS host = (VDS)model.getHost().getSelectedItem();


		String path = (String)nfsModel.getPath().getEntity();
		String storageName = null;
		RefObject<String> tempRef_storageName = new RefObject<String>(storageName);
		boolean tempVar = DataProvider.IsDomainAlreadyExist(path, tempRef_storageName);
			storageName = tempRef_storageName.argvalue;
		if (tempVar)
		{
			context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "Finish", false, nfsModel, "Create Operation failed. Domain " + storageName + " already exists in the system." })));

			return;
		}


		//Create storage connection.
		storage_server_connections tempVar2 = new storage_server_connections();
		tempVar2.setconnection(path);
		tempVar2.setstorage_type(nfsModel.getType());
		storage_server_connections connection = tempVar2;

		storage_domain_static tempVar3 = new storage_domain_static();
		tempVar3.setstorage_type(nfsModel.getType());
		tempVar3.setstorage_domain_type(nfsModel.getRole());
		tempVar3.setstorage_name((String)model.getName().getEntity());
		tempVar3.setStorageFormat((StorageFormatType)model.getFormat().getSelectedItem());
		storage_domain_static storageDomain = tempVar3;

		boolean attach = false;

		VdcReturnValueBase returnValue = Frontend.RunAction(VdcActionType.AddStorageServerConnection, new StorageServerConnectionParametersBase(connection, host.getvds_id()));

		if (returnValue != null && returnValue.getSucceeded())
		{
			storageDomain.setstorage((String)returnValue.getActionReturnValue());

			//Add storage domain.
			StorageDomainManagementParameter tempVar4 = new StorageDomainManagementParameter(storageDomain);
			tempVar4.setVdsId(host.getvds_id());
			returnValue = Frontend.RunAction(VdcActionType.AddNFSStorageDomain, tempVar4);

			attach = returnValue != null && returnValue.getSucceeded();
		}

		//Clean up connection.
		Frontend.RunAction(VdcActionType.RemoveStorageServerConnection, new StorageServerConnectionParametersBase(connection, host.getvds_id()));

		if (attach)
		{
			//Attach storage to data center as neccessary.
			storage_pool dataCenter = (storage_pool)model.getDataCenter().getSelectedItem();
			if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
			{
				NGuid storageId = (NGuid)returnValue.getActionReturnValue();
				AttachStorageToDataCenter((Guid)storageId, dataCenter.getId());
			}
		}


		context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "Finish", returnValue != null && returnValue.getSucceeded(), nfsModel, null })));
	}

	private void SaveSanStorage()
	{
		if (getWindow().getProgress() != null)
		{
			return;
		}

		getWindow().StartProgress(null);

		Task.Create(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "SaveSan" }))).Run();
	}

	private void SaveSanStorage(TaskContext context)
	{
		StorageModel model = (StorageModel)getWindow();
		SanStorageModel sanModel = (SanStorageModel)model.getSelectedItem();
		VDS host = (VDS)model.getHost().getSelectedItem();

		java.util.ArrayList<String> lunIds = new java.util.ArrayList<String>();
		for (LunModel lun : sanModel.getAddedLuns())
		{
			lunIds.add(lun.getLunId());
		}

		storage_domain_static tempVar = new storage_domain_static();
		tempVar.setstorage_name((String)model.getName().getEntity());
		tempVar.setstorage_type(sanModel.getType());
		tempVar.setstorage_domain_type(sanModel.getRole());
		tempVar.setStorageFormat((StorageFormatType)sanModel.getContainer().getFormat().getSelectedItem());
		storage_domain_static storageDomain = tempVar;

		AddSANStorageDomainParameters tempVar2 = new AddSANStorageDomainParameters(storageDomain);
		tempVar2.setVdsId(host.getvds_id());
		tempVar2.setLunIds(lunIds);
		VdcReturnValueBase returnValue = Frontend.RunAction(VdcActionType.AddSANStorageDomain, tempVar2);

		if (returnValue != null && returnValue.getSucceeded())
		{
			//Attach storage to data center as neccessary.
			storage_pool dataCenter = (storage_pool)model.getDataCenter().getSelectedItem();
			if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
			{
				NGuid storageId = (NGuid)returnValue.getActionReturnValue();
				AttachStorageToDataCenter((Guid)storageId, dataCenter.getId());
			}
		}


		context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "Finish", returnValue != null && returnValue.getSucceeded(), sanModel, null })));
	}

	private void AttachStorageInternal(java.util.List<storage_domains> storages, String title)
	{
		ListModel model = new ListModel();
		model.setTitle(title);
		setWindow(model);
		//var items = storages.Select(a => new EntityModel() { Entity = a }).ToList();
		java.util.ArrayList<EntityModel> items = new java.util.ArrayList<EntityModel>();
		for (storage_domains sd : storages)
		{
			EntityModel tempVar = new EntityModel();
			tempVar.setEntity(sd);
			items.add(tempVar);
		}

		model.setItems(items);


		UICommand tempVar2 = new UICommand("OnAttachStorage", this);
		tempVar2.setTitle("OK");
		tempVar2.setIsDefault(true);
		model.getCommands().add(tempVar2);
		UICommand tempVar3 = new UICommand("Cancel", this);
		tempVar3.setTitle("Cancel");
		tempVar3.setIsCancel(true);
		model.getCommands().add(tempVar3);
	}

	private void AttachStorageToDataCenter(Guid storageId, Guid dataCenterId)
	{
		Frontend.RunActionAsyncroniousely(VdcActionType.AttachStorageDomainToPool, new StorageDomainPoolParametersBase(storageId, dataCenterId));
	}

	public void OnAttachStorage()
	{
		ListModel model = (ListModel)getWindow();

		//var items = model.Items
		//    .Cast<EntityModel>()
		//    .Where(Selector.GetIsSelected)
		//    .Select(a => (storage_domains)a.Entity)
		//    .ToList();
		java.util.ArrayList<storage_domains> items = new java.util.ArrayList<storage_domains>();
		for (EntityModel a : Linq.<EntityModel>Cast(model.getItems()))
		{
			if (a.getIsSelected())
			{
				items.add((storage_domains)a.getEntity());
			}
		}

		if (items.size() > 0)
		{
			//items.Select(a => (VdcActionParametersBase)new StorageDomainPoolParametersBase(a.id, Entity.id))
			//    .Each(a => Frontend.RunAction(VdcActionType.AttachStorageDomainToPool, a));
			for (storage_domains sd : items)
			{
				Frontend.RunAction(VdcActionType.AttachStorageDomainToPool, new StorageDomainPoolParametersBase(sd.getid(), getEntity().getId()));
			}
		}

		Cancel();
		PostAction();
	}

	public void AttachIsoStorage()
	{
		java.util.ArrayList<storage_domains> attachedStorage = DataProvider.GetStorageDomainList(getEntity().getId());

		//AttachStorageInternal(DataProvider.GetISOStorageDomainList(Entity.id)
		//    .Where(a => attachedStorage.All(b => b.id != a.id))
		//    .ToList(),
		//    "Attach ISO Library");
		java.util.ArrayList<storage_domains> sdl = new java.util.ArrayList<storage_domains>();
		for (storage_domains a : DataProvider.GetISOStorageDomainList())
		{
			boolean isContains = false;
			for (storage_domains b : attachedStorage)
			{
				if (b.getid().equals(a.getid()))
				{
					isContains = true;
					break;
				}
			}
			if (!isContains)
			{
				sdl.add(a);
			}
		}
		AttachStorageInternal(sdl, "Attach ISO Library");
	}

	public void AttachDataStorage()
	{
		java.util.ArrayList<storage_domains> unattachedStorage = new java.util.ArrayList<storage_domains>();
		boolean addToList;
		Version version3_0 = new Version(3, 0);
		for (storage_domains item : DataProvider.GetStorageDomainList())
		{
			addToList = false;
			if (item.getstorage_domain_type() == StorageDomainType.Data && item.getstorage_type() == getEntity().getstorage_pool_type() && item.getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached)
			{
				if (getEntity().getStoragePoolFormatType() == null)
				{
					//compat logic: in case its not v1 and the version is less than 3.0 continue.
					if (item.getStorageStaticData().getStorageFormat() != StorageFormatType.V1 && getEntity().getcompatibility_version().compareTo(version3_0) < 0)
					{
						continue;
					}
					addToList = true;
				}
				else if ((StorageFormatType)getEntity().getStoragePoolFormatType() == item.getStorageStaticData().getStorageFormat())
				{
					addToList = true;
				}
			}

			if (addToList)
			{
				unattachedStorage.add(item);
			}
		}
		AttachStorageInternal(unattachedStorage, "Attach Storage");
	}

	public void AddCluster()
	{
		ClusterModel model = new ClusterModel();
		model.Init(false);
		setWindow(model);
		model.setTitle("New Cluster");
		model.setHashName("new_cluster");
		model.setIsNew(true);

		java.util.ArrayList<storage_pool> dataCenters = new java.util.ArrayList<storage_pool>();
		dataCenters.add(getEntity());
		model.getDataCenter().setItems(dataCenters);
		model.getDataCenter().setSelectedItem(getEntity());
		model.getDataCenter().setIsChangable(false);


		UICommand tempVar = new UICommand("OnAddCluster", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnAddCluster()
	{
		ClusterModel model = (ClusterModel)getWindow();
		VDSGroup cluster = new VDSGroup();

		if (model.getProgress() != null)
		{
			return;
		}

		if (!model.Validate())
		{
			return;
		}

		//Save changes.
		Version version = (Version)model.getVersion().getSelectedItem();

		cluster.setname((String)model.getName().getEntity());
		cluster.setdescription((String)model.getDescription().getEntity());
		cluster.setstorage_pool_id(((storage_pool)model.getDataCenter().getSelectedItem()).getId());
		cluster.setcpu_name(((ServerCpu)model.getCPU().getSelectedItem()).getCpuName());
		cluster.setmax_vds_memory_over_commit(model.getMemoryOverCommit());
		cluster.setTransparentHugepages(version.compareTo(new Version("3.0")) >= 0);
		cluster.setcompatibility_version(version);


		model.StartProgress(null);

		Frontend.RunAction(VdcActionType.AddVdsGroup, new VdsGroupOperationParameters(cluster),
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

			DataCenterGuideModel localModel = (DataCenterGuideModel)result.getState();
			localModel.PostOnAddCluster(result.getReturnValue());

			}
		}, this);
	}

	public void PostOnAddCluster(VdcReturnValueBase returnValue)
	{
		ClusterModel model = (ClusterModel)getWindow();

		model.StopProgress();

		if (returnValue != null && returnValue.getSucceeded())
		{
			Cancel();
			PostAction();
		}
	}

	public void SelectHost()
	{
		java.util.ArrayList<VDSGroup> clusters = DataProvider.GetClusterList(getEntity().getId());

		MoveHost model = new MoveHost();
		model.setTitle("Select Host");
		model.setHashName("select_host");
		setWindow(model);
		model.getCluster().setItems(clusters);
		model.getCluster().setSelectedItem(Linq.FirstOrDefault(clusters));


		UICommand tempVar = new UICommand("OnSelectHost", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnSelectHost()
	{
		MoveHost model = (MoveHost)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		if (!model.Validate())
		{
			return;
		}

		model.setSelectedHosts(new java.util.ArrayList<VDS>());
		for (EntityModel a : Linq.<EntityModel>Cast(model.getItems()))
		{
			if (a.getIsSelected())
			{
				model.getSelectedHosts().add((VDS)a.getEntity());
			}
		}

		VDSGroup cluster = (VDSGroup)model.getCluster().getSelectedItem();
		java.util.ArrayList<VdcActionParametersBase> paramerterList = new java.util.ArrayList<VdcActionParametersBase>();
		for (VDS host : model.getSelectedHosts())
		{
			//Try to change host's cluster as neccessary.
			if (host.getvds_group_id() != null && !host.getvds_group_id().equals(cluster.getID()))
			{
				paramerterList.add(new ChangeVDSClusterParameters(cluster.getID(), host.getvds_id()));

			}
		}

		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.ChangeVDSCluster, paramerterList,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			DataCenterGuideModel dataCenterGuideModel = (DataCenterGuideModel)result.getState();
			java.util.ArrayList<VDS> hosts = ((MoveHost)dataCenterGuideModel.getWindow()).getSelectedHosts();
			java.util.ArrayList<VdcReturnValueBase> retVals = (java.util.ArrayList<VdcReturnValueBase>)result.getReturnValue();
			if (retVals != null && hosts.size() == retVals.size())
			{
				int i = 0;
				for (VDS selectedHost : hosts)
				{
					if (selectedHost.getstatus() == VDSStatus.PendingApproval && retVals.get(i) != null && retVals.get(i).getSucceeded())
					{
						Frontend.RunAction(VdcActionType.ApproveVds, new ApproveVdsParameters(selectedHost.getvds_id()));
					}
				}
				i++;
			}
			dataCenterGuideModel.getWindow().StopProgress();
			dataCenterGuideModel.Cancel();
			dataCenterGuideModel.PostAction();

			}
		}, this);
	}

	public void AddHost()
	{
		HostModel model = new HostModel();
		setWindow(model);
		model.setTitle("New Host");
		model.setHashName("new_host_guide_me");
		model.getPort().setEntity(54321);
		model.getOverrideIpTables().setEntity(true);

		model.getDataCenter().setItems(new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { getEntity() })));
		model.getDataCenter().setSelectedItem(getEntity());
		model.getDataCenter().setIsChangable(false);


		UICommand tempVar = new UICommand("OnConfirmPMHost", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void OnConfirmPMHost()
	{
		HostModel model = (HostModel)getWindow();

		if (!model.Validate())
		{
			return;
		}

		if (!((Boolean)model.getIsPm().getEntity()))
		{
			ConfirmationModel confirmModel = new ConfirmationModel();
			setConfirmWindow(confirmModel);
			confirmModel.setTitle("Power Management Configuration");
			confirmModel.setHashName("power_management_configuration");
			confirmModel.setMessage("You haven't configured Power Management for this Host. Are you sure you want to continue?");

			UICommand tempVar = new UICommand("OnAddHost", this);
			tempVar.setTitle("OK");
			tempVar.setIsDefault(true);
			confirmModel.getCommands().add(tempVar);
			UICommand tempVar2 = new UICommand("CancelConfirmWithFocus", this);
			tempVar2.setTitle("Cancel");
			tempVar2.setIsCancel(true);
			confirmModel.getCommands().add(tempVar2);
		}
		else
		{
			OnAddHost();
		}
	}

	public void OnAddHost()
	{
		CancelConfirm();

		HostModel model = (HostModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		//Save changes.
		VDS host = new VDS();
		host.setvds_name((String)model.getName().getEntity());
		host.sethost_name((String)model.getHost().getEntity());
		host.setManagmentIp((String)model.getManagementIp().getEntity());
		host.setport(Integer.parseInt(model.getPort().getEntity().toString()));
		host.setvds_group_id(((VDSGroup)model.getCluster().getSelectedItem()).getID());
		host.setpm_enabled((Boolean)model.getIsPm().getEntity());
		host.setpm_user((Boolean)model.getIsPm().getEntity() ? (String)model.getPmUserName().getEntity() : null);
		host.setpm_password((Boolean)model.getIsPm().getEntity() ? (String)model.getPmPassword().getEntity() : null);
		host.setpm_type((Boolean)model.getIsPm().getEntity() ? (String)model.getPmType().getSelectedItem() : null);
		host.setPmOptionsMap((Boolean)model.getIsPm().getEntity() ? new ValueObjectMap(model.getPmOptionsMap(), false) : null);

		AddVdsActionParameters addVdsParams = new AddVdsActionParameters();
		addVdsParams.setVdsId(host.getvds_id());
		addVdsParams.setvds(host);
		addVdsParams.setRootPassword((String)model.getRootPassword().getEntity());


		model.StartProgress(null);

		Frontend.RunAction(VdcActionType.AddVds, addVdsParams,
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

			DataCenterGuideModel localModel = (DataCenterGuideModel)result.getState();
			localModel.PostOnAddHost(result.getReturnValue());

			}
		}, this);
	}

	public void PostOnAddHost(VdcReturnValueBase returnValue)
	{
		HostModel model = (HostModel)getWindow();

		model.StopProgress();

		if (returnValue != null && returnValue.getSucceeded())
		{
			Cancel();
			PostAction();
		}
	}

	private void PostAction()
	{
		UpdateOptions();
	}

	public void Cancel()
	{
		setWindow(null);
	}

	public void CancelConfirm()
	{
		setConfirmWindow(null);
	}

	public void CancelConfirmWithFocus()
	{
		setConfirmWindow(null);

		HostModel hostModel = (HostModel)getWindow();
		hostModel.setIsPowerManagementSelected(true);
		hostModel.getIsPm().setEntity(true);
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (StringHelper.stringsEqual(command.getName(), "AddCluster"))
		{
			AddCluster();
		}

		if (StringHelper.stringsEqual(command.getName(), "AddHost"))
		{
			AddHost();
		}

		if (StringHelper.stringsEqual(command.getName(), "SelectHost"))
		{
			SelectHost();
		}
		if (StringHelper.stringsEqual(command.getName(), "AddDataStorage"))
		{
			AddDataStorage();
		}
		if (StringHelper.stringsEqual(command.getName(), "AttachDataStorage"))
		{
			AttachDataStorage();
		}
		if (StringHelper.stringsEqual(command.getName(), "AddIsoStorage"))
		{
			AddIsoStorage();
		}
		if (StringHelper.stringsEqual(command.getName(), "AttachIsoStorage"))
		{
			AttachIsoStorage();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnAddCluster"))
		{
			OnAddCluster();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnSelectHost"))
		{
			OnSelectHost();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnAddHost"))
		{
			OnAddHost();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnAddStorage"))
		{
			OnAddStorage();
		}

		if (StringHelper.stringsEqual(command.getName(), "OnAttachStorage"))
		{
			OnAttachStorage();
		}

		if (StringHelper.stringsEqual(command.getName(), "AddLocalStorage"))
		{
			AddLocalStorage();
		}

		if (StringHelper.stringsEqual(command.getName(), "OnConfirmPMHost"))
		{
			OnConfirmPMHost();
		}

		if (StringHelper.stringsEqual(command.getName(), "CancelConfirm"))
		{
			CancelConfirm();
		}
		if (StringHelper.stringsEqual(command.getName(), "CancelConfirmWithFocus"))
		{
			CancelConfirmWithFocus();
		}

		if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
	}

	public void run(TaskContext context)
	{
		java.util.ArrayList<Object> data = (java.util.ArrayList<Object>)context.getState();
		String key = (String)data.get(0);

//C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
//		switch (key)
//ORIGINAL LINE: case "SaveNfs":
		if (StringHelper.stringsEqual(key, "SaveNfs"))
		{
				SaveNfsStorage(context);

		}
//ORIGINAL LINE: case "SaveLocal":
		else if (StringHelper.stringsEqual(key, "SaveLocal"))
		{
				SaveLocalStorage(context);

		}
//ORIGINAL LINE: case "SaveSan":
		else if (StringHelper.stringsEqual(key, "SaveSan"))
		{
				SaveSanStorage(context);

		}
//ORIGINAL LINE: case "Finish":
		else if (StringHelper.stringsEqual(key, "Finish"))
		{
				getWindow().StopProgress();

				if ((Boolean)data.get(1))
				{
					Cancel();
					PostAction();
				}
				else
				{
					((Model)data.get(2)).setMessage((String)data.get(3));
				}
		}
	}
}