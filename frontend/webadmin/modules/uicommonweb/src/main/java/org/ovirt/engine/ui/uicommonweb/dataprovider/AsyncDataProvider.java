package org.ovirt.engine.ui.uicommonweb.dataprovider;
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

import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.ui.uicommonweb.*;

@SuppressWarnings("unused")
public final class AsyncDataProvider
{
	public static void GetDomainListViaPublic(AsyncQuery aQuery, boolean filterInternalDomain)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
											{
												return source != null ? new java.util.ArrayList<String>((java.util.ArrayList<String>)source) : new java.util.ArrayList<String>();
											} };
		GetDomainListParameters tempVar = new GetDomainListParameters();
		tempVar.setFilterInternalDomain(filterInternalDomain);
		Frontend.RunPublicQuery(VdcQueryType.GetDomainList, tempVar, aQuery);
	}

	public static void IsBackendAvailable(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null;
		} };
		GetDomainListParameters tempVar = new GetDomainListParameters();
		tempVar.setFilterInternalDomain(true);
		Frontend.RunPublicQuery(VdcQueryType.GetDomainList, tempVar, aQuery);
	}

	public static void IsCustomPropertiesAvailable(AsyncQuery aQuery, String version)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Boolean)source).booleanValue() : true;
		} };
		GetConfigurationValueParameters tempVar = new GetConfigurationValueParameters(ConfigurationValues.SupportCustomProperties);
		tempVar.setVersion(version);
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, tempVar, aQuery);
	}

	public static void GetIsoDomainByDataCenterId(AsyncQuery aQuery, Guid dataCenterId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				java.util.ArrayList<storage_domains> storageDomains = (java.util.ArrayList<storage_domains>)source;
				for (storage_domains domain : storageDomains)
				{
					if (domain.getstorage_domain_type() == StorageDomainType.ISO)
					{
						return domain;
					}
				}
			}

			return null;
		} };

		StoragePoolQueryParametersBase getIsoParams = new StoragePoolQueryParametersBase(dataCenterId);
		Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, getIsoParams, aQuery);
	}

	public static void GetExportDomainByDataCenterId(AsyncQuery aQuery, Guid dataCenterId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			java.util.ArrayList<storage_domains> storageDomains = (java.util.ArrayList<storage_domains>)source;
			for (storage_domains domain : storageDomains)
			{
				if (domain.getstorage_domain_type() == StorageDomainType.ImportExport)
				{
					return domain;
				}
			}

			return null;
		} };

		StoragePoolQueryParametersBase getExportParams = new StoragePoolQueryParametersBase(dataCenterId);
		Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, getExportParams, aQuery);
	}

	public static void GetIrsImageList(AsyncQuery aQuery, Guid isoDomainId, boolean forceRefresh)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				java.util.ArrayList<RepoFileMetaData> repoList = (java.util.ArrayList<RepoFileMetaData>)source;
				java.util.ArrayList<String> fileNameList = new java.util.ArrayList<String>();
				for (RepoFileMetaData RepoFileMetaData : repoList)
				{
					fileNameList.add(RepoFileMetaData.getRepoFileName());
				}

				Collections.sort(fileNameList, new Linq.CaseInsensitiveComparer());
				return fileNameList;
			}
			return new java.util.ArrayList<String>();
		} };

		GetAllIsoImagesListParameters parameters = new GetAllIsoImagesListParameters();
		parameters.setStorageDomainId(isoDomainId);
		parameters.setForceRefresh(forceRefresh);
		Frontend.RunQuery(VdcQueryType.GetAllIsoImagesList, parameters, aQuery);
	}

	public static void GetFloppyImageList(AsyncQuery aQuery, Guid isoDomainId, boolean forceRefresh)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				java.util.ArrayList<RepoFileMetaData> repoList = (java.util.ArrayList<RepoFileMetaData>)source;
				java.util.ArrayList<String> fileNameList = new java.util.ArrayList<String>();
				for (RepoFileMetaData RepoFileMetaData : repoList)
				{
					fileNameList.add(RepoFileMetaData.getRepoFileName());
				}

				Collections.sort(fileNameList, new Linq.CaseInsensitiveComparer());
				return fileNameList;
			}
			return new java.util.ArrayList<String>();
		} };

		GetAllIsoImagesListParameters parameters = new GetAllIsoImagesListParameters();
		parameters.setStorageDomainId(isoDomainId);
		parameters.setForceRefresh(forceRefresh);
		Frontend.RunQuery(VdcQueryType.GetAllFloppyImagesList, parameters, aQuery);
	}

	public static void GetClusterById(AsyncQuery aQuery, Guid id)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source;
		} };
		Frontend.RunQuery(VdcQueryType.GetVdsGroupById, new GetVdsGroupByIdParameters(id), aQuery);
	}

	public static void GetPoolById(AsyncQuery aQuery, Guid poolId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
											{
												return source;
											} };
		Frontend.RunQuery(VdcQueryType.GetVmPoolById, new GetVmPoolByIdParameters(poolId), aQuery);
	}

	public static void GetVmById(AsyncQuery aQuery, Guid vmId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source;
		} };
		Frontend.RunQuery(VdcQueryType.GetVmByVmId, new GetVmByVmIdParameters(vmId), aQuery);
	}

	public static void GetAnyVm(AsyncQuery aQuery, String poolName)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			java.util.ArrayList<VM> vms = Linq.<VM>Cast((java.util.ArrayList<IVdcQueryable>)source);
			return vms.size() > 0 ? vms.get(0) : null;
		} };
		Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Vms: pool=" + poolName, SearchType.VM), aQuery);
	}

	public static void GetTimeZoneList(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source == null)
			{
				return new java.util.HashMap<String, String>();
			}
			return source;
		} };
		Frontend.RunQuery(VdcQueryType.GetTimeZones, new VdcQueryParametersBase(), aQuery);
	}

	public static void GetDataCenterList(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source == null)
			{
				return new java.util.ArrayList<storage_pool>();
			}
			return source;
		} };
		Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("DataCenter: sortby name", SearchType.StoragePool), aQuery);
	}

	public static void GetMinimalVmMemSize(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 1;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.VMMinMemorySizeInMB), aQuery);
	}

	public static void GetMaximalVmMemSize64OS(AsyncQuery aQuery, String version)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 262144;
		} };
		GetConfigurationValueParameters tempVar = new GetConfigurationValueParameters(ConfigurationValues.VM64BitMaxMemorySizeInMB);
		tempVar.setVersion(version);
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, tempVar, aQuery);
	}

	public static void GetMaximalVmMemSize32OS(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 20480;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.VM32BitMaxMemorySizeInMB), aQuery);
	}

	public static void GetMaxNumOfVmSockets(AsyncQuery aQuery, String version)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 1;
		} };
		GetConfigurationValueParameters tempVar = new GetConfigurationValueParameters(ConfigurationValues.MaxNumOfVmSockets);
		tempVar.setVersion(version);
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, tempVar, aQuery);
	}

	public static void GetMaxNumOfVmCpus(AsyncQuery aQuery, String version)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 1;
		} };
		GetConfigurationValueParameters tempVar = new GetConfigurationValueParameters(ConfigurationValues.MaxNumOfVmCpus);
		tempVar.setVersion(version);
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, tempVar, aQuery);
	}

	public static void GetMaxNumOfCPUsPerSocket(AsyncQuery aQuery, String version)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 1;
		} };
		GetConfigurationValueParameters tempVar = new GetConfigurationValueParameters(ConfigurationValues.MaxNumOfCpuPerSocket);
		tempVar.setVersion(version);
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, tempVar, aQuery);
	}

	public static void GetClusterList(AsyncQuery aQuery, Guid dataCenterId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				java.util.ArrayList<VDSGroup> list = (java.util.ArrayList<VDSGroup>)source;
				Collections.sort(list, new Linq.VdsGroupByNameComparer());
				return list;
			}
			return new java.util.ArrayList<VDSGroup>();
		} };
		Frontend.RunQuery(VdcQueryType.GetVdsGroupsByStoragePoolId, new StoragePoolQueryParametersBase(dataCenterId), aQuery);
	}

	public static void GetClusterList(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				java.util.ArrayList<VDSGroup> list = (java.util.ArrayList<VDSGroup>)source;
				Collections.sort(list, new Linq.VdsGroupByNameComparer());
				return list;
			}
			return new java.util.ArrayList<VDSGroup>();
		} };
		Frontend.RunQuery(VdcQueryType.GetAllVdsGroups, new VdcQueryParametersBase(), aQuery);
	}

	public static void GetTemplateDiskList(AsyncQuery aQuery, Guid templateId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source == null)
			{
				return new java.util.ArrayList<DiskImage>();
			}
			return source;
		} };
		Frontend.RunQuery(VdcQueryType.GetVmTemplatesDisks, new GetVmTemplatesDisksParameters(templateId), aQuery);
	}

	public static void GetRoundedPriority(AsyncQuery aQuery, int priority)
	{
		aQuery.setData(new Object[] {priority});
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			int max = ((Integer)source).intValue();
			int medium = max / 2;

			int[] levels = new int[] { 1, medium, max };

			for (int i = 0; i < levels.length; i++)
			{
				int lengthToLess = levels[i] - (Integer)_asyncQuery.Data[0];
				int lengthToMore = levels[i + 1] - (Integer)_asyncQuery.Data[0];

				if (lengthToMore < 0)
				{
					continue;
				}

				return Math.abs(lengthToLess) < lengthToMore ? levels[i] : levels[i + 1];
			}


			return 0;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.VmPriorityMaxValue), aQuery);
	}

	public static void GetTemplateListByDataCenter(AsyncQuery aQuery, Guid dataCenterId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			java.util.ArrayList<VmTemplate> list = new java.util.ArrayList<VmTemplate>();
			if (source != null)
			{
				VmTemplate blankTemplate = new VmTemplate();
				for (VmTemplate template : (java.util.ArrayList<VmTemplate>)source)
				{
					if (template.getId().equals(Guid.Empty))
					{
						blankTemplate = template;
					}
					else if (template.getstatus() == VmTemplateStatus.OK)
					{
						list.add(template);
					}
				}

				Collections.sort(list, new Linq.VmTemplateByNameComparer());
				list.add(0, blankTemplate);
			}

			return list;
		} };
		Frontend.RunQuery(VdcQueryType.GetVmTemplatesByStoragePoolId, new GetVmTemplatesByStoragePoolIdParameters(dataCenterId), aQuery);
	}

	public static void GetTemplateListByStorage(AsyncQuery aQuery, Guid storageId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			java.util.ArrayList<VmTemplate> list = new java.util.ArrayList<VmTemplate>();
			if (source != null)
			{
				for (VmTemplate template : (java.util.ArrayList<VmTemplate>)source)
				{
					if (template.getstatus() == VmTemplateStatus.OK)
					{
						list.add(template);
					}
				}

				Collections.sort(list, new Linq.VmTemplateByNameComparer());
			}

			return list;
		} };
		Frontend.RunQuery(VdcQueryType.GetVmTemplatesFromStorageDomain, new StorageDomainQueryParametersBase(storageId), aQuery);
	}

	public static void GetNumOfMonitorList(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			java.util.ArrayList<Integer> nums = new java.util.ArrayList<Integer>();
			if (source != null)
			{
				Iterable numEnumerable = (Iterable)source;
				java.util.Iterator numIterator = numEnumerable.iterator();
				while (numIterator.hasNext())
				{
					nums.add(Integer.parseInt(numIterator.next().toString()));
				}
			}
			return nums;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.ValidNumOfMonitors), aQuery);
	}

	public static void GetStorageDomainListByTemplate(AsyncQuery aQuery, Guid templateId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if(source== null)
			{
				return new java.util.ArrayList<storage_domains>();
			}
			return source;
		} };
		Frontend.RunQuery(VdcQueryType.GetStorageDomainsByVmTemplateId, new GetStorageDomainsByVmTemplateIdQueryParameters(templateId), aQuery);
	}

	public static void GetStorageDomainList(AsyncQuery aQuery, Guid dataCenterId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source == null)
			{
				return new java.util.ArrayList<storage_domains>();
			}
			return source;
		} };
		Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, new StoragePoolQueryParametersBase(dataCenterId), aQuery);
	}

	public static void GetMaxVmPriority(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source == null)
			{
				return 100;
			}
			return ((Integer)source).intValue();
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.VmPriorityMaxValue), aQuery);
	}

	public static void GetDefaultTimeZone(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				return ((java.util.Map.Entry<String, String>) source).getKey();
			}
			return "";
		} };
		Frontend.RunQuery(VdcQueryType.GetDefualtTimeZone, new VdcQueryParametersBase(), aQuery);
	}

	public static void GetHostListByCluster(AsyncQuery aQuery, String clusterName)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				java.util.ArrayList<VDS> list = Linq.<VDS>Cast((java.util.ArrayList<IVdcQueryable>)source);
				return list;
			}

			return new java.util.ArrayList<VDS>();
		} };
		Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = " + clusterName + " sortby name", SearchType.VDS), aQuery);
	}

	public static void GetHostListByDataCenter(AsyncQuery aQuery, String dataCenterName)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				java.util.ArrayList<VDS> list = Linq.<VDS>Cast((java.util.ArrayList<IVdcQueryable>)source);
				return list;
			}

			return new java.util.ArrayList<VDS>();
		} };
		Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: datacenter = " + dataCenterName + " sortby name", SearchType.VDS), aQuery);
	}

	public static void GetVmDiskList(AsyncQuery aQuery, Guid vmId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				return source;
			}
			return new java.util.ArrayList<DiskImage>();
		} };
		Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId, new GetAllDisksByVmIdParameters(vmId), aQuery);
	}

	public final static class GetSnapshotListQueryResult
	{
		private Guid privatePreviewingImage = new Guid();
		public Guid getPreviewingImage()
		{
			return privatePreviewingImage;
		}
		private void setPreviewingImage(Guid value)
		{
			privatePreviewingImage = value;
		}
		private java.util.ArrayList<DiskImage> privateSnapshots;
		public java.util.ArrayList<DiskImage> getSnapshots()
		{
			return privateSnapshots;
		}
		private void setSnapshots(java.util.ArrayList<DiskImage> value)
		{
			privateSnapshots = value;
		}
		private DiskImage privateDisk;
		public DiskImage getDisk()
		{
			return privateDisk;
		}
		private void setDisk(DiskImage value)
		{
			privateDisk = value;
		}
		private Guid privateVmId = new Guid();
		public Guid getVmId()
		{
			return privateVmId;
		}
		public void setVmId(Guid value)
		{
			privateVmId = value;
		}

		public GetSnapshotListQueryResult(Guid previewingImage, java.util.ArrayList<DiskImage> snapshots, DiskImage disk)
		{
			setPreviewingImage(previewingImage);
			setSnapshots(snapshots);
			setDisk(disk);
		}
	}
	public static void GetSnapshotList(AsyncQuery aQuery, Guid vmId, DiskImage disk)
	{
		aQuery.setData(new Object[] {disk});
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			GetAllVmSnapshotsByDriveQueryReturnValue returnValue = (GetAllVmSnapshotsByDriveQueryReturnValue)_asyncQuery.OriginalReturnValue;
			return new GetSnapshotListQueryResult(returnValue.getTryingImage(), (java.util.ArrayList<DiskImage>)source, (DiskImage)_asyncQuery.Data[0]);
		} };
		Frontend.RunQuery(VdcQueryType.GetAllVmSnapshotsByDrive, new GetAllVmSnapshotsByDriveParameters(vmId, disk.getinternal_drive_mapping()), aQuery);
	}

	public static void GetMaxVmMemSize(AsyncQuery aQuery, boolean is64)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				return source;
			}
			return 262144;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(is64 ? ConfigurationValues.VM64BitMaxMemorySizeInMB : ConfigurationValues.VM32BitMaxMemorySizeInMB), aQuery);
	}

	public static void GetDomainList(AsyncQuery aQuery, boolean filterInternalDomain)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? new java.util.ArrayList<String>((java.util.ArrayList<String>)source) : new java.util.ArrayList<String>();
		} };
		GetDomainListParameters tempVar = new GetDomainListParameters();
		tempVar.setFilterInternalDomain(filterInternalDomain);
		Frontend.RunQuery(VdcQueryType.GetDomainList, tempVar, aQuery);
	}

	public static void GetRoleList(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (java.util.ArrayList<roles>)source : new java.util.ArrayList<roles>();
		} };
		Frontend.RunQuery(VdcQueryType.GetAllRoles, new MultilevelAdministrationsQueriesParameters(), aQuery);
	}

	public static void GetStorageDomainById(AsyncQuery aQuery, Guid storageDomainId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (storage_domains)source : null;
		} };
		Frontend.RunQuery(VdcQueryType.GetStorageDomainById, new StorageDomainQueryParametersBase(storageDomainId), aQuery);
	}

	public static void GetDiskPresetList(AsyncQuery aQuery, VmType vmType, StorageType storageType)
	{
		aQuery.setData(new Object[] {vmType, storageType});
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source == null)
			{
				return null;
			}

			java.util.ArrayList<DiskImageBase> list = new java.util.ArrayList<DiskImageBase>();
			DiskImageBase presetData = null;
			DiskImageBase presetSystem = null;
			for (DiskImageBase disk : (java.util.ArrayList<DiskImageBase>)source)
			{
				if (disk.getdisk_type() == DiskType.System || disk.getdisk_type() == DiskType.Data)
				{
					list.add(disk);
				}
				if (disk.getdisk_type() == DiskType.System && presetSystem == null)
				{
					presetSystem = disk;
				}
				else if (disk.getdisk_type() == DiskType.Data && presetData == null)
				{
					presetData = disk;
				}
			}
			java.util.ArrayList<DiskImageBase> presetList = list;

			if (presetData != null)
			{
				presetData.setvolume_type(VolumeType.Preallocated);
				presetData.setvolume_format(DataProvider.GetDiskVolumeFormat(presetData.getvolume_type(), (StorageType)_asyncQuery.Data[1]));
			}
			if (presetSystem != null)
			{
				presetSystem.setvolume_type((VmType)_asyncQuery.Data[0] == VmType.Server ? VolumeType.Preallocated : VolumeType.Sparse);
				presetSystem.setvolume_format(DataProvider.GetDiskVolumeFormat(presetSystem.getvolume_type(), (StorageType)_asyncQuery.Data[1]));
			}

			return presetList;
		} };
		Frontend.RunQuery(VdcQueryType.GetDiskConfigurationList, new VdcQueryParametersBase(), aQuery);
	}

	public static void GetClusterNetworkList(AsyncQuery aQuery, Guid clusterId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source == null)
			{
				return new java.util.ArrayList<network>();
			}
			return (java.util.ArrayList<network>)source;
		} };
		Frontend.RunQuery(VdcQueryType.GetAllNetworksByClusterId, new VdsGroupQueryParamenters(clusterId), aQuery);
	}

	public static void GetDataCenterById(AsyncQuery aQuery, Guid dataCenterId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
											{
												return source;
											} };
		Frontend.RunQuery(VdcQueryType.GetStoragePoolById, new StoragePoolQueryParametersBase(dataCenterId), aQuery);
	}

	public static void GetTemplateById(AsyncQuery aQuery, Guid templateId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source;
		} };
		Frontend.RunQuery(VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(templateId), aQuery);
	}

	public static void GetHostList(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				java.util.ArrayList<VDS> list = Linq.<VDS>Cast((Iterable)source);
				return list;
			}

			return new java.util.ArrayList<VDS>();
		} };
		SearchParameters searchParameters = new SearchParameters("Host:", SearchType.VDS);
		searchParameters.setMaxCount(9999);
		Frontend.RunQuery(VdcQueryType.Search, searchParameters, aQuery);
	}

	public static void GetRpmVersionViaPublic(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (String)source : "";
		} };
		Frontend.RunPublicQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.ProductRPMVersion), aQuery);
	}

	public static void GetSearchResultsLimit(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 100;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.SearchResultsLimit), aQuery);
	}

	public static void GetSANWipeAfterDelete(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Boolean)source).booleanValue() : false;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.SANWipeAfterDelete), aQuery);
	}

	public static void GetCustomPropertiesList(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (String)source : null;
		} };
		Frontend.RunQuery(VdcQueryType.GetVmCustomProperties, new VdcQueryParametersBase(), aQuery);
	}

	public static void GetPermissionsByAdElementId(AsyncQuery aQuery, Guid userId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (java.util.ArrayList<permissions>)source : new java.util.ArrayList<permissions>();
		} };
		Frontend.RunQuery(VdcQueryType.GetPermissionsByAdElementId, new MultilevelAdministrationByAdElementIdParameters(userId), aQuery);
	}

	public static void GetRoleActionGroupsByRoleId(AsyncQuery aQuery, Guid roleId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
			{
				return source != null ? (java.util.ArrayList<ActionGroup>)source : new java.util.ArrayList<ActionGroup>();
			} };
		Frontend.RunQuery(VdcQueryType.GetRoleActionGroupsByRoleId, new MultilevelAdministrationByRoleIdParameters(roleId), aQuery);
	}

	public static void IsTemplateNameUnique(AsyncQuery aQuery, String name)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? !((Boolean)source).booleanValue() : false;
		} };
		Frontend.RunQuery(VdcQueryType.IsVmTemlateWithSameNameExist, new IsVmTemlateWithSameNameExistParameters(name), aQuery);
	}

	public static void IsVmNameUnique(AsyncQuery aQuery, String name)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? !((Boolean)source).booleanValue() : false;
		} };
		Frontend.RunQuery(VdcQueryType.IsVmWithSameNameExist, new IsVmWithSameNameExistParameters(name), aQuery);
	}

	public static void GetDataCentersWithPermittedActionOnClusters(AsyncQuery aQuery, ActionGroup actionGroup)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source == null)
			{
				return new java.util.ArrayList<storage_pool>();
			}
			return source;
		} };

		GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters = new GetEntitiesWithPermittedActionParameters();
		getEntitiesWithPermittedActionParameters.setActionGroup(actionGroup);
		Frontend.RunQuery(VdcQueryType.GetDataCentersWithPermittedActionOnClusters, getEntitiesWithPermittedActionParameters, aQuery);
	 }

	public static void GetClustersWithPermittedAction(AsyncQuery aQuery, ActionGroup actionGroup)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				java.util.ArrayList<VDSGroup> list = (java.util.ArrayList<VDSGroup>)source;
				Collections.sort(list, new Linq.VdsGroupByNameComparer());
				return list;
			}
			return new java.util.ArrayList<VDSGroup>();
		} };

		GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters = new GetEntitiesWithPermittedActionParameters();
		getEntitiesWithPermittedActionParameters.setActionGroup(actionGroup);
		Frontend.RunQuery(VdcQueryType.GetClustersWithPermittedAction, getEntitiesWithPermittedActionParameters, aQuery);
	 }

	public static void GetVmTemplatesWithPermittedAction(AsyncQuery aQuery, ActionGroup actionGroup)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			java.util.ArrayList<VmTemplate> list = new java.util.ArrayList<VmTemplate>();
			if (source != null)
			{
				VmTemplate blankTemplate = new VmTemplate();
				for (VmTemplate template : (java.util.ArrayList<VmTemplate>)source)
				{
					if (template.getId().equals(Guid.Empty))
					{
						blankTemplate = template;
					}
					else if (template.getstatus() == VmTemplateStatus.OK)
					{
						list.add(template);
					}
				}

				Collections.sort(list, new Linq.VmTemplateByNameComparer());
				list.add(0, blankTemplate);
			}

			return list;
		} };

		GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters = new GetEntitiesWithPermittedActionParameters();
		getEntitiesWithPermittedActionParameters.setActionGroup(actionGroup);
		Frontend.RunQuery(VdcQueryType.GetVmTemplatesWithPermittedAction, getEntitiesWithPermittedActionParameters, aQuery);
	}

	public static void IsUSBEnabledByDefault(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Boolean)source).booleanValue() : false;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.EnableUSBAsDefault), aQuery);
	}

	public static void GetStorageConnectionById(AsyncQuery aQuery, String id)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (storage_server_connections)source : null;
		} };
		Frontend.RunQuery(VdcQueryType.GetStorageServerConnectionById, new StorageServerConnectionQueryParametersBase(id), aQuery);
	}

	public static void GetDataCentersByStorageDomain(AsyncQuery aQuery, Guid storageDomainId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (java.util.ArrayList<storage_pool>)source : null;
		} };
		Frontend.RunQuery(VdcQueryType.GetStoragePoolsByStorageDomainId, new StorageDomainQueryParametersBase(storageDomainId), aQuery);
	}

	public static void GetDataCenterVersions(AsyncQuery aQuery, NGuid dataCenterId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if(source == null)
			{
				return new java.util.ArrayList<Version>();
			}
			else
			{
				java.util.ArrayList<Version> list = (java.util.ArrayList<Version>)source;
				Collections.sort(list);
				return list;
			}
		} };
		GetAvailableClusterVersionsByStoragePoolParameters tempVar = new GetAvailableClusterVersionsByStoragePoolParameters();
		tempVar.setStoragePoolId(dataCenterId);
		Frontend.RunQuery(VdcQueryType.GetAvailableClusterVersionsByStoragePool, tempVar, aQuery);
	}

	public static void GetDataCenterMaxNameLength(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 1;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.StoragePoolNameSizeLimit), aQuery);
	}

	public static void GetClusterServerMemoryOverCommit(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 0;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.MaxVdsMemOverCommitForServers), aQuery);
	}

	public static void GetClusterDesktopMemoryOverCommit(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 0;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.MaxVdsMemOverCommit), aQuery);
	}

	public static void GetCPUList(AsyncQuery aQuery, Version version)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (java.util.ArrayList<ServerCpu>)source : new java.util.ArrayList<ServerCpu>();
		} };
			Frontend.RunQuery(VdcQueryType.GetAllServerCpuList, new GetAllServerCpuListParameters(version), aQuery);
	}

	public static void GetPmTypeList(AsyncQuery aQuery, Version version)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			java.util.ArrayList<String> list = new java.util.ArrayList<String>();
			if (source != null)
			{
				String[] array = ((String)source).split("[,]", -1);
				for (String item : array)
				{
					list.add(item);
				}
			}
			return list;
		} };
		GetConfigurationValueParameters tempVar = new GetConfigurationValueParameters(ConfigurationValues.VdsFenceType);
		tempVar.setVersion(version != null ? version.toString() : null);
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, tempVar, aQuery);
	}

	public static void GetPmOptions(AsyncQuery aQuery, String pmType)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			String pmtype = (String)_asyncQuery.Data[0];
			java.util.HashMap<String, java.util.ArrayList<String>> cachedPmMap = new java.util.HashMap<String, java.util.ArrayList<String>>();
			java.util.HashMap<String, java.util.HashMap<String, Object>> dict = (java.util.HashMap<String, java.util.HashMap<String, Object>>)source;
			for (java.util.Map.Entry<String, java.util.HashMap<String, Object>> pair : dict.entrySet())
			{
				java.util.ArrayList<String> list = new java.util.ArrayList<String>();
				for (java.util.Map.Entry<String, Object> p : pair.getValue().entrySet())
				{
					list.add(p.getKey());
				}

				cachedPmMap.put(pair.getKey(), list);
			}
			return cachedPmMap.get(pmtype);
		} };
		aQuery.setData(new Object[] { pmType });
		Frontend.RunQuery(VdcQueryType.GetAgentFenceOptions2, new VdcQueryParametersBase(), aQuery);
	}

	public static void GetNetworkList(AsyncQuery aQuery, Guid dataCenterId)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (java.util.ArrayList<network>)source : new java.util.ArrayList<network>();
		} };

		Frontend.RunQuery(VdcQueryType.GetAllNetworks, new GetAllNetworkQueryParamenters(dataCenterId), aQuery);
	}

	public static void GetISOStorageDomainList(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				java.util.ArrayList<storage_domains> allStorageDomains = (java.util.ArrayList<storage_domains>)source;
				java.util.ArrayList<storage_domains> isoStorageDomains = new java.util.ArrayList<storage_domains>();
				for (storage_domains storageDomain : allStorageDomains)
				{
					if (storageDomain.getstorage_domain_type() == StorageDomainType.ISO)
					{
						isoStorageDomains.add(storageDomain);
					}
				}
				return isoStorageDomains;
			}
			return new java.util.ArrayList<storage_domains>();
		} };

		SearchParameters searchParams = new SearchParameters("Storage:", SearchType.StorageDomain);
		searchParams.setMaxCount(9999);

		Frontend.RunQuery(VdcQueryType.Search, searchParams, aQuery);
	}

	public static void GetStorageDomainList(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (java.util.ArrayList<storage_domains>)source : new java.util.ArrayList<storage_domains>();
		} };

		SearchParameters searchParams = new SearchParameters("Storage:", SearchType.StorageDomain);
		searchParams.setMaxCount(9999);

		Frontend.RunQuery(VdcQueryType.Search, searchParams, aQuery);
	}

	public static void GetLocalStorageHost(AsyncQuery aQuery, String dataCenterName)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				for (IVdcQueryable item : (java.util.ArrayList<IVdcQueryable>)source)
				{
					return (VDS)item;
				}
			}
			return null;
		} };
		SearchParameters sp = new SearchParameters(StringFormat.format("hosts: datacenter=%1$s", dataCenterName), SearchType.VDS);
		Frontend.RunQuery(VdcQueryType.Search, sp, aQuery);
	}

	public static void GetStorageDomainsByConnection(AsyncQuery aQuery, NGuid storagePoolId, String connectionPath)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (java.util.ArrayList<storage_domains>)source : null;
		} };


		GetStorageDomainsByConnectionParameters param = new GetStorageDomainsByConnectionParameters();
		param.setConnection(connectionPath);
		if (storagePoolId != null)
		{
			param.setStoragePoolId(storagePoolId.getValue());
		}

		Frontend.RunQuery(VdcQueryType.GetStorageDomainsByConnection, param, aQuery);
	}

	public static void GetExistingStorageDomainList(AsyncQuery aQuery, Guid hostId, StorageDomainType domainType, String path)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (java.util.ArrayList<storage_domains>)source : null;
		} };

		Frontend.RunQuery(VdcQueryType.GetExistingStorageDomainList, new GetExistingStorageDomainListParameters(hostId, StorageType.NFS, domainType, path), aQuery);
	}

	public static void GetLocalFSPath(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? (String)source : "";
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.RhevhLocalFSPath), aQuery);
	}

	public static void GetStorageDomainMaxNameLength(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 1;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.StorageDomainNameSizeLimit), aQuery);
	}

	public static void IsStorageDomainNameUnique(AsyncQuery aQuery, String name)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				java.util.ArrayList<storage_domains> storageDomains = (java.util.ArrayList<storage_domains>)source;
				return storageDomains.isEmpty();
			}

			return source;
		} };
		Frontend.RunQuery(VdcQueryType.Search, new SearchParameters(StringFormat.format("Storage: name=%1$s", name), SearchType.StorageDomain), aQuery);
	}

	public static void GetHighUtilizationForEvenDistribution(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 0;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForEvenlyDistribute), aQuery);
	}

	public static void GetLowUtilizationForPowerSave(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 0;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.LowUtilizationForPowerSave), aQuery);
	}

	public static void GetHighUtilizationForPowerSave(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? ((Integer)source).intValue() : 0;
		} };
		Frontend.RunQuery(VdcQueryType.GetConfigurationValue, new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForPowerSave), aQuery);
	}

	public static void GetRootTag(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				tags tag = (tags)source;

				tags root = new tags(tag.getdescription(), tag.getparent_id(), tag.getIsReadonly(), tag.gettag_id(), tag.gettag_name());
				if (tag.getChildren() != null)
				{
					DataProvider.fillTagsRecursive(root, tag.getChildren());
				}

				return root;
			}

			return new tags();
		} };
		Frontend.RunQuery(VdcQueryType.GetRootTag, new VdcQueryParametersBase(), aQuery);
	 }

	private static void SetAttachedTagsConverter(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			if (source != null)
			{
				java.util.ArrayList<tags> ret = new java.util.ArrayList<tags>();
				for (tags tags : (java.util.ArrayList<tags>)source)
				{
					if (tags.gettype() == TagsType.GeneralTag)
					{
						ret.add(tags);
					}
				}
				return ret;
			}

			return new tags();
		} };
	}

	public static void GetAttachedTagsToVm(AsyncQuery aQuery, Guid id)
	{
		SetAttachedTagsConverter(aQuery);

		Frontend.RunQuery(VdcQueryType.GetTagsByVmId, new GetTagsByVmIdParameters(id.toString()), aQuery);
	}

	public static void GetAttachedTagsToUser(AsyncQuery aQuery, Guid id)
	{
		SetAttachedTagsConverter(aQuery);

		Frontend.RunQuery(VdcQueryType.GetTagsByUserId, new GetTagsByUserIdParameters(id.toString()), aQuery);
	}

	public static void GetAttachedTagsToUserGroup(AsyncQuery aQuery, Guid id)
	{
		SetAttachedTagsConverter(aQuery);

		Frontend.RunQuery(VdcQueryType.GetTagsByUserGroupId, new GetTagsByUserGroupIdParameters(id.toString()), aQuery);
	}

	public static void GetAttachedTagsToHost(AsyncQuery aQuery, Guid id)
	{
		SetAttachedTagsConverter(aQuery);

		Frontend.RunQuery(VdcQueryType.GetTagsByVdsId, new GetTagsByVdsIdParameters(id.toString()), aQuery);
	}

	public static void GetoVirtISOsList(AsyncQuery aQuery)
	{
		aQuery.converterCallback = new IAsyncConverter() { public Object Convert(Object source, AsyncQuery _asyncQuery)
		{
			return source != null ? new java.util.ArrayList<String>((java.util.ArrayList<String>)source) : new java.util.ArrayList<String>();
		} };
		Frontend.RunQuery(VdcQueryType.GetoVirtISOs, new VdcQueryParametersBase(), aQuery);
	}
}