package org.ovirt.engine.ui.uicommonweb.models.vms;
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
import org.ovirt.engine.ui.uicommonweb.validation.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class NewVmModelBehavior extends IVmModelBehavior
{
	@Override
	public void Initialize(SystemTreeItemModel systemTreeSelectedItem)
	{
		super.Initialize(systemTreeSelectedItem);
		AsyncDataProvider.GetDataCenterList(new AsyncQuery(getModel(),
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

			UnitVmModel model = (UnitVmModel) target;
			java.util.ArrayList<storage_pool> list = new java.util.ArrayList<storage_pool>();
			for (storage_pool a : (java.util.ArrayList<storage_pool>) returnValue)
			{
				if (a.getstatus() == StoragePoolStatus.Up)
				{
					list.add(a);
				}
			}
			model.SetDataCenter(model, list);

			}
		}, getModel().getHash()));
	}

	@Override
	public void DataCenter_SelectedItemChanged()
	{
		storage_pool dataCenter = (storage_pool)getModel().getDataCenter().getSelectedItem();

		getModel().setIsHostAvailable(dataCenter.getstorage_pool_type() != StorageType.LOCALFS);

		AsyncDataProvider.GetClusterList(new AsyncQuery(new Object[] { this, getModel() },
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

			Object[] array = (Object[])target;
			NewVmModelBehavior behavior = (NewVmModelBehavior)array[0];
			UnitVmModel model = (UnitVmModel)array[1];
			java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>)returnValue;
			model.SetClusters(model, clusters, null);
			behavior.InitTemplate();
			behavior.InitCdImage();

			}
		}, getModel().getHash()), dataCenter.getId());
	}

	@Override
	public void Template_SelectedItemChanged()
	{
		VmTemplate template = (VmTemplate)getModel().getTemplate().getSelectedItem();

		if (template != null)
		{
			//Copy VM parameters from template.
			getModel().getOSType().setSelectedItem(template.getos());
			getModel().getNumOfSockets().setEntity(template.getnum_of_sockets());
			getModel().getTotalCPUCores().setEntity(template.getnum_of_cpus());
			getModel().getNumOfMonitors().setSelectedItem(template.getnum_of_monitors());
			getModel().getDomain().setSelectedItem(template.getdomain());
			getModel().getMemSize().setEntity(template.getmem_size_mb());
			getModel().getUsbPolicy().setSelectedItem(template.getusb_policy());
			getModel().setBootSequence(template.getdefault_boot_sequence());
			getModel().getIsHighlyAvailable().setEntity(template.getauto_startup());


			getModel().getCdImage().setIsChangable(!StringHelper.isNullOrEmpty(template.getiso_path()));
			if (getModel().getCdImage().getIsChangable())
			{
				getModel().getCdImage().setSelectedItem(template.getiso_path());
			}


			if (!StringHelper.isNullOrEmpty(template.gettime_zone()))
			{
				//Patch! Create key-value pair with a right key.
				getModel().getTimeZone().setSelectedItem(new KeyValuePairCompat<String, String>(template.gettime_zone(), ""));

				UpdateTimeZone();
			}
			else
			{
				UpdateDefaultTimeZone();
			}

			// Update domain list
			UpdateDomain();

			java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>)getModel().getCluster().getItems();
			VDSGroup selectCluster = (VDSGroup)Linq.FirstOrDefault(clusters, new Linq.ClusterPredicate(template.getvds_group_id()));

			getModel().getCluster().setSelectedItem((selectCluster != null) ? selectCluster : Linq.FirstOrDefault(clusters));


			// Update display protocol selected item
			EntityModel displayProtocol = null;
			boolean isFirst = true;
			for (Object item : getModel().getDisplayProtocol().getItems())
			{
				EntityModel a = (EntityModel)item;
				if (isFirst)
				{
					displayProtocol = a;
					isFirst = false;
				}
				DisplayType dt = (DisplayType)a.getEntity();
				if (dt == template.getdefault_display_type())
				{
					displayProtocol = a;
					break;
				}
			}
			getModel().getDisplayProtocol().setSelectedItem(displayProtocol);


			//By default, take kernel params from template.
			getModel().getKernel_path().setEntity(template.getkernel_url());
			getModel().getKernel_parameters().setEntity(template.getkernel_params());
			getModel().getInitrd_path().setEntity(template.getinitrd_url());


			if (!template.getId().equals(Guid.Empty))
			{
				getModel().getStorageDomain().setIsChangable(true);
				getModel().getProvisioning().setIsChangable(true);

				getModel().setIsBlankTemplate(false);
				InitDisks();
			}
			else
			{
				getModel().getStorageDomain().setIsChangable(false);
				getModel().getProvisioning().setIsChangable(false);

				getModel().setIsBlankTemplate(true);
				getModel().setIsDisksAvailable(false);
				getModel().setDisks(null);
			}


			InitPriority(template.getpriority());
			InitStorageDomains();
			UpdateMinAllocatedMemory();
		}
	}

	@Override
	public void Cluster_SelectedItemChanged()
	{
		UpdateDefaultHost();
		UpdateIsCustomPropertiesAvailable();
		UpdateMinAllocatedMemory();
		UpdateNumOfSockets();
	}

	@Override
	public void DefaultHost_SelectedItemChanged()
	{
		UpdateCdImage();
	}

	@Override
	public void Provisioning_SelectedItemChanged()
	{
		UpdateIsDisksAvailable();
		InitStorageDomains();
	}

	@Override
	public void UpdateMinAllocatedMemory()
	{
		VDSGroup cluster = (VDSGroup)getModel().getCluster().getSelectedItem();
		if (cluster == null)
		{
			return;
		}

		double overCommitFactor = 100.0 / cluster.getmax_vds_memory_over_commit();
		getModel().getMinAllocatedMemory().setEntity((int)((Integer)getModel().getMemSize().getEntity() * overCommitFactor));
	}

	private void InitTemplate()
	{
		storage_pool dataCenter = (storage_pool)getModel().getDataCenter().getSelectedItem();

		//Filter according to system tree selection.
		if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
		{
			storage_domains storage = (storage_domains)getSystemTreeSelectedItem().getEntity();

			AsyncDataProvider.GetTemplateListByDataCenter(new AsyncQuery(new Object[] { this, storage },
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target1, Object returnValue1) {

				Object[] array1 = (Object[])target1;
				NewVmModelBehavior behavior1 = (NewVmModelBehavior)array1[0];
				storage_domains storage1 = (storage_domains)array1[1];
				AsyncDataProvider.GetTemplateListByStorage(new AsyncQuery(new Object[] { behavior1, returnValue1 },
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target2, Object returnValue2) {

					Object[] array2 = (Object[])target2;
					NewVmModelBehavior behavior2 = (NewVmModelBehavior)array2[0];
					java.util.ArrayList<VmTemplate> templatesByDataCenter = (java.util.ArrayList<VmTemplate>)array2[1];
					java.util.ArrayList<VmTemplate> templatesByStorage = (java.util.ArrayList<VmTemplate>)returnValue2;
					VmTemplate blankTemplate = Linq.FirstOrDefault(templatesByDataCenter, new Linq.TemplatePredicate(Guid.Empty));
					if (blankTemplate != null)
					{
						templatesByStorage.add(0, blankTemplate);
					}
					behavior2.PostInitTemplate((java.util.ArrayList<VmTemplate>)returnValue2);

			}
		}), storage1.getid());

			}
		}, getModel().getHash()), dataCenter.getId());
		}
		else
		{
			AsyncDataProvider.GetTemplateListByDataCenter(new AsyncQuery(this,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

				NewVmModelBehavior behavior = (NewVmModelBehavior)target;
				behavior.PostInitTemplate((java.util.ArrayList<VmTemplate>)returnValue);

			}
		}, getModel().getHash()), dataCenter.getId());
		}
	}

	private void PostInitTemplate(java.util.ArrayList<VmTemplate> templates)
	{
		//If there was some template selected before, try select it again.
		VmTemplate oldTemplate = (VmTemplate)getModel().getTemplate().getSelectedItem();

		getModel().getTemplate().setItems(templates);

		getModel().getTemplate().setSelectedItem(Linq.FirstOrDefault(templates, oldTemplate != null ? new Linq.TemplatePredicate(oldTemplate.getId()) : new Linq.TemplatePredicate(Guid.Empty)));

		UpdateIsDisksAvailable();
	}

	public void InitCdImage()
	{
		UpdateCdImage();
	}

	private void InitDisks()
	{
		VmTemplate template = (VmTemplate)getModel().getTemplate().getSelectedItem();

		AsyncDataProvider.GetTemplateDiskList(new AsyncQuery(getModel(),
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

			UnitVmModel model = (UnitVmModel)target;
			java.util.ArrayList<DiskImage> disks = (java.util.ArrayList<DiskImage>)returnValue;
			Collections.sort(disks, new Linq.DiskByInternalDriveMappingComparer());
			java.util.ArrayList<DiskModel> list = new java.util.ArrayList<DiskModel>();
			for (DiskImage a : disks)
			{
				DiskModel diskModel = new DiskModel();
				diskModel.setIsNew(true);
				diskModel.setName(a.getinternal_drive_mapping());
				EntityModel tempVar = new EntityModel();
				tempVar.setEntity(a.getSizeInGigabytes());
				diskModel.setSize(tempVar);
				ListModel tempVar2 = new ListModel();
				tempVar2.setItems((a.getvolume_type() == VolumeType.Preallocated ? new java.util.ArrayList<VolumeType>(java.util.Arrays.asList(new VolumeType[] {VolumeType.Preallocated})) : DataProvider.GetVolumeTypeList()));
				tempVar2.setSelectedItem(a.getvolume_type());
				diskModel.setVolumeType(tempVar2);
				list.add(diskModel);
			}
			model.setDisks(list);
			UpdateIsDisksAvailable();

			}
		}, getModel().getHash()), template.getId());
	}

	public void UpdateIsDisksAvailable()
	{
		boolean provisioning = (Boolean)((EntityModel)getModel().getProvisioning().getSelectedItem()).getEntity();

		getModel().setIsDisksAvailable(provisioning && getModel().getDisks() != null);
	}

	private void InitStorageDomains()
	{
		VmTemplate template = (VmTemplate)getModel().getTemplate().getSelectedItem();

		if (template != null && !template.getId().equals(Guid.Empty))
		{
			boolean provisioning = (Boolean)((EntityModel)getModel().getProvisioning().getSelectedItem()).getEntity();

			if (!provisioning)
			{
				AsyncDataProvider.GetStorageDomainListByTemplate(new AsyncQuery(this,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

					NewVmModelBehavior behavior = (NewVmModelBehavior)target;
					java.util.ArrayList<storage_domains> storageDomains = (java.util.ArrayList<storage_domains>)returnValue;
					behavior.PostInitStorageDomains(storageDomains);

			}
		}, getModel().getHash()), template.getId());
			}
			else
			{
				AsyncDataProvider.GetStorageDomainListByTemplate(new AsyncQuery(this,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

					java.util.ArrayList<storage_domains> storageDomains = (java.util.ArrayList<storage_domains>)returnValue;
					boolean isStorageDomainActive = false;
					for (storage_domains storageDomain : storageDomains)
					{
						isStorageDomainActive = storageDomain.getstatus() == StorageDomainStatus.Active;
						if (!isStorageDomainActive)
						{
							break;
						}
					}
					if (isStorageDomainActive)
					{
						NewVmModelBehavior behavior = (NewVmModelBehavior)target;
						storage_pool dataCenter = (storage_pool)behavior.getModel().getDataCenter().getSelectedItem();
						behavior.InitStorageDomains(dataCenter);
					}

			}
		}, getModel().getHash()), template.getId());
			}
		}
		else
		{
			getModel().getStorageDomain().setItems(new java.util.ArrayList<storage_domains>());
			getModel().getStorageDomain().setSelectedItem(null);
			getModel().getStorageDomain().setIsChangable(false);
		}
	}

	public void PostInitStorageDomains(java.util.ArrayList<storage_domains> storageDomains)
	{
		// filter only the Active storage domains (Active regarding the relevant storage pool).
		java.util.ArrayList<storage_domains> list = new java.util.ArrayList<storage_domains>();
		for (storage_domains a : storageDomains)
		{
			if (a.getstatus() != null && a.getstatus() == StorageDomainStatus.Active)
			{
				list.add(a);
			}
		}

		//Filter according to system tree selection.
		if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
		{
			storage_domains selectStorage = (storage_domains)getSystemTreeSelectedItem().getEntity();
			storage_domains sd = Linq.FirstOrDefault(list, new Linq.StoragePredicate(selectStorage.getid()));

			getModel().getStorageDomain().setItems(new java.util.ArrayList<storage_domains>(java.util.Arrays.asList(new storage_domains[] { sd })));
			getModel().getStorageDomain().setSelectedItem(sd);
			getModel().getStorageDomain().setIsChangable(false);
		}
		else
		{
			getModel().getStorageDomain().setItems(list);
			getModel().getStorageDomain().setSelectedItem(Linq.FirstOrDefault(list));
			getModel().getStorageDomain().setIsChangable(true);
		}
	}

	public void InitStorageDomains(storage_pool dataCenter)
	{
		AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

			NewVmModelBehavior behavior = (NewVmModelBehavior)target;
			java.util.ArrayList<storage_domains> storageDomains = new java.util.ArrayList<storage_domains>();
			for (storage_domains a : (java.util.ArrayList<storage_domains>)returnValue)
			{
				if (a.getstorage_domain_type() == StorageDomainType.Data || a.getstorage_domain_type() == StorageDomainType.Master)
				{
					storageDomains.add(a);
				}
			}
			behavior.PostInitStorageDomains(storageDomains);

			}
		}, getModel().getHash()), dataCenter.getId());
	}
}