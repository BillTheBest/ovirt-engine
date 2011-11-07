package org.ovirt.engine.ui.uicommonweb.models.resources;
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

import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class ResourcesModel extends SearchableListModel
{

	private EntityModel privateDefinedVMs;
	public EntityModel getDefinedVMs()
	{
		return privateDefinedVMs;
	}
	private void setDefinedVMs(EntityModel value)
	{
		privateDefinedVMs = value;
	}
	private EntityModel privateRunningVMs;
	public EntityModel getRunningVMs()
	{
		return privateRunningVMs;
	}
	private void setRunningVMs(EntityModel value)
	{
		privateRunningVMs = value;
	}
	private EntityModel privateRunningVMsPercentage;
	public EntityModel getRunningVMsPercentage()
	{
		return privateRunningVMsPercentage;
	}
	private void setRunningVMsPercentage(EntityModel value)
	{
		privateRunningVMsPercentage = value;
	}
	private EntityModel privateDefinedCPUs;
	public EntityModel getDefinedCPUs()
	{
		return privateDefinedCPUs;
	}
	private void setDefinedCPUs(EntityModel value)
	{
		privateDefinedCPUs = value;
	}
	private EntityModel privateUsedCPUs;
	public EntityModel getUsedCPUs()
	{
		return privateUsedCPUs;
	}
	private void setUsedCPUs(EntityModel value)
	{
		privateUsedCPUs = value;
	}
	private EntityModel privateUsedCPUsPercentage;
	public EntityModel getUsedCPUsPercentage()
	{
		return privateUsedCPUsPercentage;
	}
	private void setUsedCPUsPercentage(EntityModel value)
	{
		privateUsedCPUsPercentage = value;
	}
	private EntityModel privateDefinedMemory;
	public EntityModel getDefinedMemory()
	{
		return privateDefinedMemory;
	}
	private void setDefinedMemory(EntityModel value)
	{
		privateDefinedMemory = value;
	}
	private EntityModel privateUsedMemory;
	public EntityModel getUsedMemory()
	{
		return privateUsedMemory;
	}
	private void setUsedMemory(EntityModel value)
	{
		privateUsedMemory = value;
	}
	private EntityModel privateUsedMemoryPercentage;
	public EntityModel getUsedMemoryPercentage()
	{
		return privateUsedMemoryPercentage;
	}
	private void setUsedMemoryPercentage(EntityModel value)
	{
		privateUsedMemoryPercentage = value;
	}
	private EntityModel privateTotalDisksSize;
	public EntityModel getTotalDisksSize()
	{
		return privateTotalDisksSize;
	}
	private void setTotalDisksSize(EntityModel value)
	{
		privateTotalDisksSize = value;
	}
	private EntityModel privateNumOfSnapshots;
	public EntityModel getNumOfSnapshots()
	{
		return privateNumOfSnapshots;
	}
	private void setNumOfSnapshots(EntityModel value)
	{
		privateNumOfSnapshots = value;
	}
	private EntityModel privateTotalSnapshotsSize;
	public EntityModel getTotalSnapshotsSize()
	{
		return privateTotalSnapshotsSize;
	}
	private void setTotalSnapshotsSize(EntityModel value)
	{
		privateTotalSnapshotsSize = value;
	}


	public ResourcesModel()
	{
		setDefinedVMs(new EntityModel());
		setRunningVMs(new EntityModel());
		setRunningVMsPercentage(new EntityModel());
		setDefinedCPUs(new EntityModel());
		setUsedCPUs(new EntityModel());
		setUsedCPUsPercentage(new EntityModel());
		setDefinedMemory(new EntityModel());
		setUsedMemory(new EntityModel());
		setUsedMemoryPercentage(new EntityModel());
		setTotalDisksSize(new EntityModel());
		setNumOfSnapshots(new EntityModel());
		setTotalSnapshotsSize(new EntityModel());
	}

	@Override
	protected void SyncSearch()
	{
		super.SyncSearch();

		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object ReturnValue)
		{
			ResourcesModel resourcesModel = (ResourcesModel)model;
			java.util.ArrayList<VM> list = (java.util.ArrayList<VM>)((VdcQueryReturnValue)ReturnValue).getReturnValue();
			//TODO: Insert dummy data regarding disks and snapshots.
			for (VM vm : list)
			{
				//vm.DiskList =
				//    new[]
				//    {
				//        new DiskImage
				//        {
				//            internal_drive_mapping = "1",
				//            SizeInGigabytes = 100,
				//            ActualSize = 50,
				//Snapshots =
				//    new[]
				//    {
				//        new DiskImage(),
				//        new DiskImage()
				//    }
				//},
				//new DiskImage
				//{
				//    internal_drive_mapping = "2",
				//    SizeInGigabytes = 200,
				//    ActualSize = 80,
				//Snapshots =
				//    new[]
				//    {
				//        new DiskImage(),
				//        new DiskImage(),
				//        new DiskImage()
				//    }
				//    }
				//};
			}


			//Update calculated properties.
			int runningVMs = 0;
			int definedCPUs = 0;
			int usedCPUs = 0;
			int definedMemory = 0;
			int usedMemory = 0;
			long totalDisksSize = 0;
			long totalSnapshotsSize = 0;
			int numOfSnapshots = 0;

			for (VM vm : list)
			{
				definedCPUs += vm.getnum_of_cpus();
				definedMemory += vm.getvm_mem_size_mb();

				if (vm.isStatusUp())
				{
					runningVMs++;
					usedCPUs += vm.getnum_of_cpus();
					usedMemory += vm.getvm_mem_size_mb();
				}

				if (vm.getDiskList() != null)
				{
					for (DiskImage disk : vm.getDiskList())
					{
						totalDisksSize += disk.getSizeInGigabytes();
						totalSnapshotsSize += (long)disk.getActualDiskWithSnapshotsSize();
						numOfSnapshots += disk.getSnapshots().size();
					}
				}
			}

			getDefinedVMs().setEntity(list.size());
			getRunningVMs().setEntity(runningVMs);
			getRunningVMsPercentage().setEntity(runningVMs * 100 / list.size());
			getDefinedCPUs().setEntity(definedCPUs);
			getUsedCPUs().setEntity(usedCPUs);
			getUsedCPUsPercentage().setEntity(usedCPUs * 100 / definedCPUs);
			getDefinedMemory().setEntity(SizeParser(definedMemory));
			getUsedMemory().setEntity(SizeParser(usedMemory));
			getUsedMemoryPercentage().setEntity(usedMemory * 100 / definedMemory);
			getTotalDisksSize().setEntity(totalDisksSize >= 1 ? totalDisksSize + "GB" : "<1GB");
			getTotalSnapshotsSize().setEntity(totalSnapshotsSize >= 1 ? totalSnapshotsSize + "GB" : "<1GB");
			getNumOfSnapshots().setEntity(numOfSnapshots);

			resourcesModel.setItems(list);
		}};


		//Items property will contain list of VMs.
		GetUserVmsByUserIdAndGroupsParameters getUserVmsByUserIdAndGroupsParameters = new GetUserVmsByUserIdAndGroupsParameters(Frontend.getLoggedInUser().getUserId());
		getUserVmsByUserIdAndGroupsParameters.setIncludeDiskData(true);
		getUserVmsByUserIdAndGroupsParameters.setRefresh(getIsQueryFirstTime());
		Frontend.RunQuery(VdcQueryType.GetUserVmsByUserIdAndGroups, getUserVmsByUserIdAndGroupsParameters, _asyncQuery);
	}

	// Temporarily converter
	// TODO: Use converters infrastructure in UICommon
	public String SizeParser(long sizeInMb)
	{
		return ((sizeInMb >= 1024 && sizeInMb % 1024 == 0) ? (sizeInMb / 1024 + "GB") : (sizeInMb + "MB"));
	}

}