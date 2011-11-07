package org.ovirt.engine.ui.uicommonweb.models.clusters;
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
import org.ovirt.engine.ui.uicommonweb.validation.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommonweb.dataprovider.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class ClusterModel extends Model
{

	private int privateServerOverCommit;
	public int getServerOverCommit()
	{
		return privateServerOverCommit;
	}
	public void setServerOverCommit(int value)
	{
		privateServerOverCommit = value;
	}
	private int privateDesktopOverCommit;
	public int getDesktopOverCommit()
	{
		return privateDesktopOverCommit;
	}
	public void setDesktopOverCommit(int value)
	{
		privateDesktopOverCommit = value;
	}
	private int privateDefaultOverCommit;
	public int getDefaultOverCommit()
	{
		return privateDefaultOverCommit;
	}
	public void setDefaultOverCommit(int value)
	{
		privateDefaultOverCommit = value;
	}
	private VDSGroup privateEntity;
	public VDSGroup getEntity()
	{
		return privateEntity;
	}
	public void setEntity(VDSGroup value)
	{
		privateEntity = value;
	}
	private boolean privateIsEdit;
	public boolean getIsEdit()
	{
		return privateIsEdit;
	}
	public void setIsEdit(boolean value)
	{
		privateIsEdit = value;
	}
	private boolean isCPUinitialized = false;

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
	private NGuid privateClusterId;
	public NGuid getClusterId()
	{
		return privateClusterId;
	}
	public void setClusterId(NGuid value)
	{
		privateClusterId = value;
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
	private ListModel privateDataCenter;
	public ListModel getDataCenter()
	{
		return privateDataCenter;
	}
	public void setDataCenter(ListModel value)
	{
		privateDataCenter = value;
	}
	private ListModel privateCPU;
	public ListModel getCPU()
	{
		return privateCPU;
	}
	public void setCPU(ListModel value)
	{
		privateCPU = value;
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

	private EntityModel privateOptimizationNone;
	public EntityModel getOptimizationNone()
	{
		return privateOptimizationNone;
	}
	public void setOptimizationNone(EntityModel value)
	{
		privateOptimizationNone = value;
	}
	private EntityModel privateOptimizationForServer;
	public EntityModel getOptimizationForServer()
	{
		return privateOptimizationForServer;
	}
	public void setOptimizationForServer(EntityModel value)
	{
		privateOptimizationForServer = value;
	}
	private EntityModel privateOptimizationForDesktop;
	public EntityModel getOptimizationForDesktop()
	{
		return privateOptimizationForDesktop;
	}
	public void setOptimizationForDesktop(EntityModel value)
	{
		privateOptimizationForDesktop = value;
	}
	private EntityModel privateOptimizationCustom;
	public EntityModel getOptimizationCustom()
	{
		return privateOptimizationCustom;
	}
	public void setOptimizationCustom(EntityModel value)
	{
		privateOptimizationCustom = value;
	}

	private EntityModel privateOptimizationNone_IsSelected;
	public EntityModel getOptimizationNone_IsSelected()
	{
		return privateOptimizationNone_IsSelected;
	}
	public void setOptimizationNone_IsSelected(EntityModel value)
	{
		privateOptimizationNone_IsSelected = value;
	}
	private EntityModel privateOptimizationForServer_IsSelected;
	public EntityModel getOptimizationForServer_IsSelected()
	{
		return privateOptimizationForServer_IsSelected;
	}
	public void setOptimizationForServer_IsSelected(EntityModel value)
	{
		privateOptimizationForServer_IsSelected = value;
	}
	private EntityModel privateOptimizationForDesktop_IsSelected;
	public EntityModel getOptimizationForDesktop_IsSelected()
	{
		return privateOptimizationForDesktop_IsSelected;
	}
	public void setOptimizationForDesktop_IsSelected(EntityModel value)
	{
		privateOptimizationForDesktop_IsSelected = value;
	}
	private EntityModel privateOptimizationCustom_IsSelected;
	public EntityModel getOptimizationCustom_IsSelected()
	{
		return privateOptimizationCustom_IsSelected;
	}
	public void setOptimizationCustom_IsSelected(EntityModel value)
	{
		privateOptimizationCustom_IsSelected = value;
	}

	private EntityModel privateMigrateOnErrorOption_NO;
	public EntityModel getMigrateOnErrorOption_NO()
	{
		return privateMigrateOnErrorOption_NO;
	}
	public void setMigrateOnErrorOption_NO(EntityModel value)
	{
		privateMigrateOnErrorOption_NO = value;
	}
	private EntityModel privateMigrateOnErrorOption_YES;
	public EntityModel getMigrateOnErrorOption_YES()
	{
		return privateMigrateOnErrorOption_YES;
	}
	public void setMigrateOnErrorOption_YES(EntityModel value)
	{
		privateMigrateOnErrorOption_YES = value;
	}
	private EntityModel privateMigrateOnErrorOption_HA_ONLY;
	public EntityModel getMigrateOnErrorOption_HA_ONLY()
	{
		return privateMigrateOnErrorOption_HA_ONLY;
	}
	public void setMigrateOnErrorOption_HA_ONLY(EntityModel value)
	{
		privateMigrateOnErrorOption_HA_ONLY = value;
	}

	private boolean isGeneralTabValid;
	public boolean getIsGeneralTabValid()
	{
		return isGeneralTabValid;
	}
	public void setIsGeneralTabValid(boolean value)
	{
		if (isGeneralTabValid != value)
		{
			isGeneralTabValid = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid"));
		}
	}

	private MigrateOnErrorOptions migrateOnErrorOption = MigrateOnErrorOptions.values()[0];
	public MigrateOnErrorOptions getMigrateOnErrorOption()
	{
		if ((Boolean)getMigrateOnErrorOption_NO().getEntity() == true)
		{
			return MigrateOnErrorOptions.NO;
		}
		else if ((Boolean)getMigrateOnErrorOption_YES().getEntity() == true)
		{
			return MigrateOnErrorOptions.YES;
		}
		else if ((Boolean)getMigrateOnErrorOption_HA_ONLY().getEntity() == true)
		{
			return MigrateOnErrorOptions.HA_ONLY;
		}
		return MigrateOnErrorOptions.YES;
	}
	public void setMigrateOnErrorOption(MigrateOnErrorOptions value)
	{
		if (migrateOnErrorOption != value)
		{
			migrateOnErrorOption = value;

				//webadmin use.
			switch (migrateOnErrorOption)
			{
				case NO:
					getMigrateOnErrorOption_NO().setEntity(true);
					getMigrateOnErrorOption_YES().setEntity(false);
					getMigrateOnErrorOption_HA_ONLY().setEntity(false);
					break;
				case YES:
					getMigrateOnErrorOption_NO().setEntity(false);
					getMigrateOnErrorOption_YES().setEntity(true);
					getMigrateOnErrorOption_HA_ONLY().setEntity(false);
					break;
				case HA_ONLY:
					getMigrateOnErrorOption_NO().setEntity(false);
					getMigrateOnErrorOption_YES().setEntity(false);
					getMigrateOnErrorOption_HA_ONLY().setEntity(true);
					break;
				default:
					break;
			}
			OnPropertyChanged(new PropertyChangedEventArgs("MigrateOnErrorOption"));
		}
	}

	private boolean privateisResiliencePolicyTabAvailable;
	public boolean getisResiliencePolicyTabAvailable()
	{
		return privateisResiliencePolicyTabAvailable;
	}
	public void setisResiliencePolicyTabAvailable(boolean value)
	{
		privateisResiliencePolicyTabAvailable = value;
	}
	public boolean getIsResiliencePolicyTabAvailable()
	{
		return getisResiliencePolicyTabAvailable();
	}

	public void setIsResiliencePolicyTabAvailable(boolean value)
	{
		if (getisResiliencePolicyTabAvailable() != value)
		{
			setisResiliencePolicyTabAvailable(value);
			OnPropertyChanged(new PropertyChangedEventArgs("IsResiliencePolicyTabAvailable"));
		}
	}

	public int getMemoryOverCommit()
	{
		if ((Boolean)getOptimizationNone_IsSelected().getEntity())
		{
			return (Integer)getOptimizationNone().getEntity();
		}

		if ((Boolean)getOptimizationForServer_IsSelected().getEntity())
		{
			return (Integer)getOptimizationForServer().getEntity();
		}

		if ((Boolean)getOptimizationForDesktop_IsSelected().getEntity())
		{
			return (Integer)getOptimizationForDesktop().getEntity();
		}

		if ((Boolean)getOptimizationCustom_IsSelected().getEntity())
		{
			return (Integer)getOptimizationCustom().getEntity();
		}

		return DataProvider.GetClusterDefaultMemoryOverCommit();
	}
	public void setMemoryOverCommit(int value)
	{
		getOptimizationNone_IsSelected().setEntity(value == (Integer)getOptimizationNone().getEntity());
		getOptimizationForServer_IsSelected().setEntity(value == (Integer)getOptimizationForServer().getEntity());
		getOptimizationForDesktop_IsSelected().setEntity(value == (Integer)getOptimizationForDesktop().getEntity());

		if (!(Boolean)getOptimizationNone_IsSelected().getEntity() && !(Boolean)getOptimizationForServer_IsSelected().getEntity() && !(Boolean)getOptimizationForDesktop_IsSelected().getEntity())
		{
			getOptimizationCustom().setIsAvailable(true);
			getOptimizationCustom().setEntity(value);
			getOptimizationCustom_IsSelected().setIsAvailable(true);
			getOptimizationCustom_IsSelected().setEntity(true);
		}
	}


	public ClusterModel()
	{
	}

	public void Init(boolean isEdit)
	{
		setIsEdit(isEdit);
		setName(new EntityModel());
		setDescription(new EntityModel());

		setOptimizationNone(new EntityModel());
		setOptimizationForServer(new EntityModel());
		setOptimizationForDesktop(new EntityModel());
		setOptimizationCustom(new EntityModel());

		EntityModel tempVar = new EntityModel();
		tempVar.setEntity(false);
		setOptimizationNone_IsSelected(tempVar);
		getOptimizationNone_IsSelected().getEntityChangedEvent().addListener(this);
		EntityModel tempVar2 = new EntityModel();
		tempVar2.setEntity(false);
		setOptimizationForServer_IsSelected(tempVar2);
		getOptimizationForServer_IsSelected().getEntityChangedEvent().addListener(this);
		EntityModel tempVar3 = new EntityModel();
		tempVar3.setEntity(false);
		setOptimizationForDesktop_IsSelected(tempVar3);
		getOptimizationForDesktop_IsSelected().getEntityChangedEvent().addListener(this);
		EntityModel tempVar4 = new EntityModel();
		tempVar4.setEntity(false);
		tempVar4.setIsAvailable(false);
		setOptimizationCustom_IsSelected(tempVar4);
		getOptimizationCustom_IsSelected().getEntityChangedEvent().addListener(this);

		EntityModel tempVar5 = new EntityModel();
		tempVar5.setEntity(false);
		setMigrateOnErrorOption_YES(tempVar5);
		getMigrateOnErrorOption_YES().getEntityChangedEvent().addListener(this);
		EntityModel tempVar6 = new EntityModel();
		tempVar6.setEntity(false);
		setMigrateOnErrorOption_NO(tempVar6);
		getMigrateOnErrorOption_NO().getEntityChangedEvent().addListener(this);
		EntityModel tempVar7 = new EntityModel();
		tempVar7.setEntity(false);
		setMigrateOnErrorOption_HA_ONLY(tempVar7);
		getMigrateOnErrorOption_HA_ONLY().getEntityChangedEvent().addListener(this);

		//Optimization methods:
		//default value =100;
		setDefaultOverCommit(DataProvider.GetClusterDefaultMemoryOverCommit());

		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object result)
		{
			ClusterModel clusterModel = (ClusterModel)model;
			clusterModel.setServerOverCommit((Integer)result);
			AsyncQuery _asyncQuery1 = new AsyncQuery();
			_asyncQuery1.setModel(clusterModel);
			_asyncQuery1.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model1, Object result1)
			{
				ClusterModel clusterModel1 = (ClusterModel)model1;
				clusterModel1.setDesktopOverCommit((Integer)result1);

				//temp is used for conversion purposes
				EntityModel temp;

				temp = clusterModel1.getOptimizationNone();
				temp.setEntity(clusterModel1.getDefaultOverCommit());
				//res1, res2 is used for conversion purposes.
				boolean res1 = clusterModel1.getDesktopOverCommit() != clusterModel1.getDefaultOverCommit();
				boolean res2 = clusterModel1.getServerOverCommit() != clusterModel1.getDefaultOverCommit();
				temp = clusterModel1.getOptimizationNone_IsSelected();
				setIsSelected(res1 && res2);
				temp.setEntity(getIsSelected());

				temp = clusterModel1.getOptimizationForServer();
				temp.setEntity(clusterModel1.getServerOverCommit());
				temp = clusterModel1.getOptimizationForServer_IsSelected();
				temp.setEntity(clusterModel1.getServerOverCommit() == clusterModel1.getDefaultOverCommit());

				temp = clusterModel1.getOptimizationForDesktop();
				temp.setEntity(clusterModel1.getDesktopOverCommit());
				temp = temp = clusterModel1.getOptimizationForDesktop_IsSelected();
				temp.setEntity(clusterModel1.getDesktopOverCommit() == clusterModel1.getDefaultOverCommit());

				temp = clusterModel1.getOptimizationCustom();
				temp.setIsAvailable(false);
				temp.setIsChangable(false);

				if (clusterModel1.getIsEdit())
				{
					clusterModel1.postInit();
				}

			}};
			AsyncDataProvider.GetClusterServerMemoryOverCommit(_asyncQuery1);
		}};
		AsyncDataProvider.GetClusterDesktopMemoryOverCommit(_asyncQuery);

		setDataCenter(new ListModel());
		getDataCenter().getSelectedItemChangedEvent().addListener(this);
		setCPU(new ListModel());
		setVersion(new ListModel());
		getVersion().getSelectedItemChangedEvent().addListener(this);
		setMigrateOnErrorOption(MigrateOnErrorOptions.YES);

		setIsGeneralTabValid(true);
		setIsResiliencePolicyTabAvailable(true);
	}

	private void postInit()
	{
		getDescription().setEntity(getEntity().getdescription());
		setMemoryOverCommit(getEntity().getmax_vds_memory_over_commit());

		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object result)
			{
				ClusterModel clusterModel = (ClusterModel)model;
				java.util.ArrayList<storage_pool> dataCenters = (java.util.ArrayList<storage_pool>)result;

				clusterModel.getDataCenter().setItems(dataCenters);

				clusterModel.getDataCenter().setSelectedItem(null);
				for (storage_pool a : dataCenters)
				{
					if (clusterModel.getEntity().getstorage_pool_id() != null && a.getId().equals(clusterModel.getEntity().getstorage_pool_id()))
					{
						clusterModel.getDataCenter().setSelectedItem(a);
						break;
					}
				}
				clusterModel.getDataCenter().setIsChangable(clusterModel.getDataCenter().getSelectedItem() == null);

				clusterModel.setMigrateOnErrorOption(clusterModel.getEntity().getMigrateOnError());
			}};
		AsyncDataProvider.GetDataCenterList(_asyncQuery);


	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(ListModel.SelectedItemChangedEventDefinition))
		{
			if (sender == getDataCenter())
			{
				StoragePool_SelectedItemChanged(args);
			}
			else if (sender == getVersion())
			{
				Version_SelectedItemChanged(args);
			}
		}
		else if (ev.equals(EntityModel.EntityChangedEventDefinition))
		{
			EntityModel senderEntityModel = (EntityModel)sender;
			if ((Boolean)senderEntityModel.getEntity())
			{
				if (senderEntityModel == getOptimizationNone_IsSelected())
				{
					getOptimizationForServer_IsSelected().setEntity(false);
					getOptimizationForDesktop_IsSelected().setEntity(false);
					getOptimizationCustom_IsSelected().setEntity(false);
				}
				else if (senderEntityModel == getOptimizationForServer_IsSelected())
				{
					getOptimizationNone_IsSelected().setEntity(false);
					getOptimizationForDesktop_IsSelected().setEntity(false);
					getOptimizationCustom_IsSelected().setEntity(false);
				}
				else if (senderEntityModel == getOptimizationForDesktop_IsSelected())
				{
					getOptimizationNone_IsSelected().setEntity(false);
					getOptimizationForServer_IsSelected().setEntity(false);
					getOptimizationCustom_IsSelected().setEntity(false);
				}
				else if (senderEntityModel == getOptimizationCustom_IsSelected())
				{
					getOptimizationNone_IsSelected().setEntity(false);
					getOptimizationForServer_IsSelected().setEntity(false);
					getOptimizationForDesktop_IsSelected().setEntity(false);
				}
				else if (senderEntityModel == getMigrateOnErrorOption_YES())
				{
					getMigrateOnErrorOption_NO().setEntity(false);
					getMigrateOnErrorOption_HA_ONLY().setEntity(false);
				}
				else if (senderEntityModel == getMigrateOnErrorOption_NO())
				{
					getMigrateOnErrorOption_YES().setEntity(false);
					getMigrateOnErrorOption_HA_ONLY().setEntity(false);
				}
				else if (senderEntityModel == getMigrateOnErrorOption_HA_ONLY())
				{
					getMigrateOnErrorOption_YES().setEntity(false);
					getMigrateOnErrorOption_NO().setEntity(false);
				}
			}
		}
	}

	private void Version_SelectedItemChanged(EventArgs e)
	{
		Version version;
		if (getVersion().getSelectedItem() != null)
		{
			version = (Version)getVersion().getSelectedItem();
		}
		else
		{
			version = ((storage_pool)getDataCenter().getSelectedItem()).getcompatibility_version();
		}
		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object result)
			{
				ClusterModel clusterModel = (ClusterModel)model;
				java.util.ArrayList<ServerCpu> cpus = (java.util.ArrayList<ServerCpu>)result;

				ServerCpu oldSelectedCpu = (ServerCpu)clusterModel.getCPU().getSelectedItem();
				clusterModel.getCPU().setItems(cpus);

				if (oldSelectedCpu != null)
				{
					clusterModel.getCPU().setSelectedItem(Linq.FirstOrDefault(cpus, new Linq.ServerCpuPredicate(oldSelectedCpu.getCpuName())));
				}

				if (clusterModel.getCPU().getSelectedItem() == null)
				{
					clusterModel.getCPU().setSelectedItem(Linq.FirstOrDefault(cpus));
					InitCPU();
				}
			}};
		AsyncDataProvider.GetCPUList(_asyncQuery, version);

	}

	private void InitCPU()
	{
		if (!isCPUinitialized && getIsEdit())
		{
			isCPUinitialized = true;
			getCPU().setSelectedItem(null);
			for (ServerCpu a : (java.util.ArrayList<ServerCpu>)getCPU().getItems())
			{
				if (StringHelper.stringsEqual(a.getCpuName(), getEntity().getcpu_name()))
				{
					getCPU().setSelectedItem(a);
					break;
				}
			}
		}
	}

	private void StoragePool_SelectedItemChanged(EventArgs e)
	{
		// possible versions for new cluster (when editing cluster, this event won't occur)
		// are actually the possible versions for the data-center that the cluster is going
		// to be attached to.
		storage_pool selectedDataCenter = (storage_pool)getDataCenter().getSelectedItem();
		if (selectedDataCenter == null)
		{
			return;
		}
		if (selectedDataCenter.getstorage_pool_type() == StorageType.LOCALFS)
		{
			setIsResiliencePolicyTabAvailable(false);
		}
		else
		{
			setIsResiliencePolicyTabAvailable(true);
		}

		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object result)
			{
				ClusterModel clusterModel = (ClusterModel)model;
				java.util.ArrayList<Version> versions = (java.util.ArrayList<Version>)result;
				clusterModel.getVersion().setItems(versions);
				if (!versions.contains((Version)clusterModel.getVersion().getSelectedItem()))
				{
					if (versions.contains(((storage_pool)clusterModel.getDataCenter().getSelectedItem()).getcompatibility_version()))
					{
						clusterModel.getVersion().setSelectedItem((Version)((storage_pool)clusterModel.getDataCenter().getSelectedItem()).getcompatibility_version());
					}
					else
					{
						clusterModel.getVersion().setSelectedItem(Linq.SelectHighestVersion(versions));
					}
				}
			}};
		AsyncDataProvider.GetDataCenterVersions(_asyncQuery, selectedDataCenter == null ? null : (NGuid)(selectedDataCenter.getId()));
	}

	public boolean Validate()
	{
		return Validate(true);
	}

	public boolean Validate(boolean validateStoragePool)
	{
		RegexValidation tempVar = new RegexValidation();
		tempVar.setExpression("^[A-Za-z0-9_-]+$");
		tempVar.setMessage("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters.");
		getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new NoSpacesValidation(), tempVar });
		if (validateStoragePool)
		{
			getDataCenter().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
		}
		getCPU().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
		getVersion().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

		//TODO: async validation for webadmin
		//string name = (string)Name.Entity;

		////Check name unicitate.
		//if (String.Compare(name, OriginalName, true) != 0 && !DataProvider.IsClusterNameUnique(name))
		//{
		//    Name.IsValid = false;
		//    Name.InvalidityReasons.Add("Name must be unique.");
		//}

		setIsGeneralTabValid(getName().getIsValid() && getDataCenter().getIsValid() && getCPU().getIsValid() && getVersion().getIsValid());

		return getName().getIsValid() && getDataCenter().getIsValid() && getCPU().getIsValid() && getVersion().getIsValid();
	}

}