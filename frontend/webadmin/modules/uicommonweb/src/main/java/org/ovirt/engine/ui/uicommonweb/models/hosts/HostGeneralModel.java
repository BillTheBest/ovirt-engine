package org.ovirt.engine.ui.uicommonweb.models.hosts;
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
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class HostGeneralModel extends EntityModel
{

	public static EventDefinition RequestEditEventDefinition;
	private Event privateRequestEditEvent;
	public Event getRequestEditEvent()
	{
		return privateRequestEditEvent;
	}
	private void setRequestEditEvent(Event value)
	{
		privateRequestEditEvent = value;
	}
	public static EventDefinition RequestGOToEventsTabEventDefinition;
	private Event privateRequestGOToEventsTabEvent;
	public Event getRequestGOToEventsTabEvent()
	{
		return privateRequestGOToEventsTabEvent;
	}
	private void setRequestGOToEventsTabEvent(Event value)
	{
		privateRequestGOToEventsTabEvent = value;
	}



	private UICommand privateSaveNICsConfigCommand;
	public UICommand getSaveNICsConfigCommand()
	{
		return privateSaveNICsConfigCommand;
	}
	private void setSaveNICsConfigCommand(UICommand value)
	{
		privateSaveNICsConfigCommand = value;
	}
	private UICommand privateInstallCommand;
	public UICommand getInstallCommand()
	{
		return privateInstallCommand;
	}
	private void setInstallCommand(UICommand value)
	{
		privateInstallCommand = value;
	}
	private UICommand privateEditHostCommand;
	public UICommand getEditHostCommand()
	{
		return privateEditHostCommand;
	}
	private void setEditHostCommand(UICommand value)
	{
		privateEditHostCommand = value;
	}
	private UICommand privateGoToEventsCommand;
	public UICommand getGoToEventsCommand()
	{
		return privateGoToEventsCommand;
	}
	private void setGoToEventsCommand(UICommand value)
	{
		privateGoToEventsCommand = value;
	}



	public VDS getEntity()
	{
		return (VDS)super.getEntity();
	}
	public void setEntity(VDS value)
	{
		super.setEntity(value);
	}

	private String os;
	public String getOS()
	{
		return os;
	}
	public void setOS(String value)
	{
		if (!StringHelper.stringsEqual(os, value))
		{
			os = value;
			OnPropertyChanged(new PropertyChangedEventArgs("OS"));
		}
	}

	private String kernelVersion;
	public String getKernelVersion()
	{
		return kernelVersion;
	}
	public void setKernelVersion(String value)
	{
		if (!StringHelper.stringsEqual(kernelVersion, value))
		{
			kernelVersion = value;
			OnPropertyChanged(new PropertyChangedEventArgs("KernelVersion"));
		}
	}

	private String kvmVersion;
	public String getKvmVersion()
	{
		return kvmVersion;
	}
	public void setKvmVersion(String value)
	{
		if (!StringHelper.stringsEqual(kvmVersion, value))
		{
			kvmVersion = value;
			OnPropertyChanged(new PropertyChangedEventArgs("KvmVersion"));
		}
	}

	private Version vdsmVersion;
	public Version getVdsmVersion()
	{
		return vdsmVersion;
	}
	public void setVdsmVersion(Version value)
	{
		if (Version.OpInequality(vdsmVersion, value))
		{
			vdsmVersion = value;
			OnPropertyChanged(new PropertyChangedEventArgs("VdsmVersion"));
		}
	}

	private String spiceVersion;
	public String getSpiceVersion()
	{
		return spiceVersion;
	}
	public void setSpiceVersion(String value)
	{
		if (!StringHelper.stringsEqual(spiceVersion, value))
		{
			spiceVersion = value;
			OnPropertyChanged(new PropertyChangedEventArgs("SpiceVersion"));
		}
	}

	private String iScsiInitiatorName;
	public String getIScsiInitiatorName()
	{
		return iScsiInitiatorName;
	}
	public void setIScsiInitiatorName(String value)
	{
		if (!StringHelper.stringsEqual(iScsiInitiatorName, value))
		{
			iScsiInitiatorName = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IScsiInitiatorName"));
		}
	}

	private Integer activeVms;
	public Integer getActiveVms()
	{
		return activeVms;
	}
	public void setActiveVms(Integer value)
	{
		if (activeVms == null && value == null)
		{
			return;
		}
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
		if (activeVms == null || !activeVms.equals(value))
		{
			activeVms = value;
			OnPropertyChanged(new PropertyChangedEventArgs("ActiveVms"));
		}
	}

	private Boolean memoryPageSharing;
	public Boolean getMemoryPageSharing()
	{
		return memoryPageSharing;
	}
	public void setMemoryPageSharing(Boolean value)
	{
		if (memoryPageSharing == null && value == null)
		{
			return;
		}
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
		if (memoryPageSharing == null || !memoryPageSharing.equals(value))
		{
			memoryPageSharing = value;
			OnPropertyChanged(new PropertyChangedEventArgs("MemoryPageSharing"));
		}
	}

	private Object automaticLargePage;
	public Object getAutomaticLargePage()
	{
		return automaticLargePage;
	}
	public void setAutomaticLargePage(Object value)
	{
		if (automaticLargePage != value)
		{
			automaticLargePage = value;
			OnPropertyChanged(new PropertyChangedEventArgs("AutomaticLargePage"));
		}
	}

	private Integer numberOfCPUs;
	public Integer getNumberOfCPUs()
	{
		return numberOfCPUs;
	}
	public void setNumberOfCPUs(Integer value)
	{
		if (numberOfCPUs == null && value == null)
		{
			return;
		}
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
		if (numberOfCPUs == null || !numberOfCPUs.equals(value))
		{
			numberOfCPUs = value;
			OnPropertyChanged(new PropertyChangedEventArgs("NumberOfCPUs"));
		}
	}

	private String cpuName;
	public String getCpuName()
	{
		return cpuName;
	}
	public void setCpuName(String value)
	{
		if (!StringHelper.stringsEqual(cpuName, value))
		{
			cpuName = value;
			OnPropertyChanged(new PropertyChangedEventArgs("CpuName"));
		}
	}

	private String cpuType;
	public String getCpuType()
	{
		return cpuType;
	}
	public void setCpuType(String value)
	{
		if (!StringHelper.stringsEqual(cpuType, value))
		{
			cpuType = value;
			OnPropertyChanged(new PropertyChangedEventArgs("CpuType"));
		}
	}

	private Integer sharedMemory;
	public Integer getSharedMemory()
	{
		return sharedMemory;
	}
	public void setSharedMemory(Integer value)
	{
		if (sharedMemory == null && value == null)
		{
			return;
		}
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
		if (sharedMemory == null || !sharedMemory.equals(value))
		{
			sharedMemory = value;
			OnPropertyChanged(new PropertyChangedEventArgs("SharedMemory"));
		}
	}

	private Integer physicalMemory;
	public Integer getPhysicalMemory()
	{
		return physicalMemory;
	}
	public void setPhysicalMemory(Integer value)
	{
		if (physicalMemory == null && value == null)
		{
			return;
		}
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
		if (physicalMemory == null || !physicalMemory.equals(value))
		{
			physicalMemory = value;
			OnPropertyChanged(new PropertyChangedEventArgs("PhysicalMemory"));
		}
	}

	private Long swapTotal;
	public Long getSwapTotal()
	{
		return swapTotal;
	}
	public void setSwapTotal(Long value)
	{
		if (swapTotal == null && value == null)
		{
			return;
		}
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
		if (swapTotal == null || !swapTotal.equals(value))
		{
			swapTotal = value;
			OnPropertyChanged(new PropertyChangedEventArgs("SwapTotal"));
		}
	}

	private Long swapFree;
	public Long getSwapFree()
	{
		return swapFree;
	}
	public void setSwapFree(Long value)
	{
		if (swapFree == null && value == null)
		{
			return;
		}
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
		if (swapFree == null || !swapFree.equals(value))
		{
			swapFree = value;
			OnPropertyChanged(new PropertyChangedEventArgs("SwapFree"));
		}
	}

	private Integer freeMemory;
	public Integer getFreeMemory()
	{
		return freeMemory;
	}
	public void setFreeMemory(Integer value)
	{
		if (freeMemory == null && value == null)
		{
			return;
		}
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
		if (freeMemory == null || !freeMemory.equals(value))
		{
			freeMemory = value;
			OnPropertyChanged(new PropertyChangedEventArgs("FreeMemory"));
		}
	}

	private Integer usedMemory;
	public Integer getUsedMemory()
	{
		return usedMemory;
	}
	public void setUsedMemory(Integer value)
	{
		if (usedMemory == null && value == null)
		{
			return;
		}
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
		if (usedMemory == null || !usedMemory.equals(value))
		{
			usedMemory = value;
			OnPropertyChanged(new PropertyChangedEventArgs("UsedMemory"));
		}
	}

	private Long usedSwap;
	public Long getUsedSwap()
	{
		return usedSwap;
	}
	public void setUsedSwap(Long value)
	{
		if (usedSwap == null && value == null)
		{
			return;
		}
//C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value logic:
		if (usedSwap == null || !usedSwap.equals(value))
		{
			usedSwap = value;
			OnPropertyChanged(new PropertyChangedEventArgs("UsedSwap"));
		}
	}

	private boolean hasAnyAlert;
	public boolean getHasAnyAlert()
	{
		return hasAnyAlert;
	}
	public void setHasAnyAlert(boolean value)
	{
		if (hasAnyAlert != value)
		{
			hasAnyAlert = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HasAnyAlert"));
		}
	}

	private boolean hasUpgradeAlert;
	public boolean getHasUpgradeAlert()
	{
		return hasUpgradeAlert;
	}
	public void setHasUpgradeAlert(boolean value)
	{
		if (hasUpgradeAlert != value)
		{
			hasUpgradeAlert = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HasUpgradeAlert"));
		}
	}

	private boolean hasManualFenceAlert;
	public boolean getHasManualFenceAlert()
	{
		return hasManualFenceAlert;
	}
	public void setHasManualFenceAlert(boolean value)
	{
		if (hasManualFenceAlert != value)
		{
			hasManualFenceAlert = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HasManualFenceAlert"));
		}
	}

	private boolean hasNoPowerManagementAlert;
	public boolean getHasNoPowerManagementAlert()
	{
		return hasNoPowerManagementAlert;
	}
	public void setHasNoPowerManagementAlert(boolean value)
	{
		if (hasNoPowerManagementAlert != value)
		{
			hasNoPowerManagementAlert = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HasNoPowerManagementAlert"));
		}
	}

	private boolean hasReinstallAlertNonResponsive;
	public boolean getHasReinstallAlertNonResponsive()
	{
		return hasReinstallAlertNonResponsive;
	}
	public void setHasReinstallAlertNonResponsive(boolean value)
	{
		if (hasReinstallAlertNonResponsive != value)
		{
			hasReinstallAlertNonResponsive = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HasReinstallAlertNonResponsive"));
		}
	}

	private boolean hasReinstallAlertInstallFailed;
	public boolean getHasReinstallAlertInstallFailed()
	{
		return hasReinstallAlertInstallFailed;
	}
	public void setHasReinstallAlertInstallFailed(boolean value)
	{
		if (hasReinstallAlertInstallFailed != value)
		{
			hasReinstallAlertInstallFailed = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HasReinstallAlertInstallFailed"));
		}
	}

	private boolean hasReinstallAlertMaintenance;
	public boolean getHasReinstallAlertMaintenance()
	{
		return hasReinstallAlertMaintenance;
	}
	public void setHasReinstallAlertMaintenance(boolean value)
	{
		if (hasReinstallAlertMaintenance != value)
		{
			hasReinstallAlertMaintenance = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HasReinstallAlertMaintenance"));
		}
	}

	private boolean hasNICsAlert;
	public boolean getHasNICsAlert()
	{
		return hasNICsAlert;
	}
	public void setHasNICsAlert(boolean value)
	{
		if (hasNICsAlert != value)
		{
			hasNICsAlert = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HasNICsAlert"));
		}
	}

	private NonOperationalReason nonOperationalReasonEntity;
	public NonOperationalReason getNonOperationalReasonEntity()
	{
		return nonOperationalReasonEntity;
	}
	public void setNonOperationalReasonEntity(NonOperationalReason value)
	{
		if (nonOperationalReasonEntity != value)
		{
			nonOperationalReasonEntity = value;
			OnPropertyChanged(new PropertyChangedEventArgs("NonOperationalReasonEntity"));
		}
	}


	static
	{
		RequestEditEventDefinition = new EventDefinition("RequestEditEvent", HostGeneralModel.class);
		RequestGOToEventsTabEventDefinition = new EventDefinition("RequestGOToEventsTabEvent", HostGeneralModel.class);
	}

	public HostGeneralModel()
	{
		setRequestEditEvent(new Event(RequestEditEventDefinition));
		setRequestGOToEventsTabEvent(new Event(RequestGOToEventsTabEventDefinition));
		setTitle("General");

		setSaveNICsConfigCommand(new UICommand("SaveNICsConfig", this));
		setInstallCommand(new UICommand("Install", this));
		setEditHostCommand(new UICommand("EditHost", this));
		setGoToEventsCommand(new UICommand("GoToEvents", this));
	}

	public void SaveNICsConfig()
	{
		Frontend.RunMultipleAction(VdcActionType.CommitNetworkChanges, new java.util.ArrayList<VdcActionParametersBase>(java.util.Arrays.asList(new VdcActionParametersBase[] { new VdsActionParameters(getEntity().getvds_id()) })),
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {


			}
		}, null);
	}

	public void Install()
	{
		if (getWindow() != null)
		{
			return;
		}

		InstallModel model = new InstallModel();
		setWindow(model);
		model.setTitle("Install Host");
		model.setHashName("install_host");
		model.getOVirtISO().setIsAvailable(false);
		model.getRootPassword().setIsAvailable(false);
		model.getOverrideIpTables().setIsAvailable(false);

		if (getEntity().getvds_type() == VDSType.oVirtNode)
		{
			AsyncDataProvider.GetoVirtISOsList(new AsyncQuery(model,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

				InstallModel installModel = (InstallModel)target;
				java.util.ArrayList<String> isos = (java.util.ArrayList<String>)returnValue;
				installModel.getOVirtISO().setItems(isos);
				installModel.getOVirtISO().setSelectedItem(Linq.FirstOrDefault(isos));
				installModel.getOVirtISO().setIsAvailable(true);
				installModel.getOVirtISO().setIsChangable(true);

			}
		}));
		}
		else
		{
			model.getRootPassword().setIsAvailable(true);
			model.getRootPassword().setIsChangable(true);
			model.getOverrideIpTables().setIsAvailable(true);
			model.getOverrideIpTables().setEntity(true);
		}

		UICommand tempVar = new UICommand("OnInstall", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	public void EditHost()
	{
		//Let's the parent model know about request.
		getRequestEditEvent().raise(this, EventArgs.Empty);
	}

	public void OnInstall()
	{
		InstallModel model = (InstallModel)getWindow();
		boolean isOVirt = getEntity().getvds_type() == VDSType.oVirtNode;

		if (!model.Validate(isOVirt))
		{
			return;
		}

		UpdateVdsActionParameters param = new UpdateVdsActionParameters();
		param.setvds(getEntity());
		param.setVdsId(getEntity().getvds_id());
		param.setRootPassword((String)model.getRootPassword().getEntity());
		param.setIsReinstallOrUpgrade(true);
		param.setInstallVds(true);
		param.setoVirtIsoFile(isOVirt ? (String)model.getOVirtISO().getSelectedItem() : null);
		param.setOverrideFirewall((Boolean)model.getOverrideIpTables().getEntity());

		VdcReturnValueBase returnValue = Frontend.RunAction(VdcActionType.UpdateVds, param);

		if (returnValue != null && returnValue.getSucceeded())
		{
			Cancel();
		}
	}

	private VdsVersion GetHostVersion(Guid hostId)
	{
		VDS host = DataProvider.GetHostById(hostId);
		return host != null ? host.getVersion() : new VdsVersion();
	}

	public void Cancel()
	{
		setWindow(null);
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		if (getEntity() != null)
		{
			UpdateAlerts();
			UpdateMemory();
			UpdateSwapUsed();
			UpdateProperties();
		}
	}

	@Override
	protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.EntityPropertyChanged(sender, e);

		if (e.PropertyName.equals("net_config_dirty") || e.PropertyName.equals("status") || e.PropertyName.equals("spm_status") || e.PropertyName.equals("vm_active"))
		{
			UpdateAlerts();
		}

		if (e.PropertyName.equals("usage_mem_percent") || e.PropertyName.equals("physical_mem_mb"))
		{
			UpdateMemory();
		}

		if (e.PropertyName.equals("swap_total") || e.PropertyName.equals("swap_free"))
		{
			UpdateSwapUsed();
		}
	}

	private void UpdateProperties()
	{
		VDS vds = getEntity();

		setOS(vds.gethost_os());
		setKernelVersion(vds.getkernel_version());
		setKvmVersion(vds.getkvm_version());
		setVdsmVersion(vds.getVersion().getFullVersion());
		setSpiceVersion(vds.getspice_version());
		setIScsiInitiatorName(vds.getIScsiInitiatorName());
		setActiveVms(vds.getvm_active());
		setMemoryPageSharing(vds.getksm_state());
		setAutomaticLargePage(vds.getTransparentHugePagesState());
		setNumberOfCPUs(vds.getcpu_cores());
		setCpuName(vds.getCpuName() != null ? vds.getCpuName().getCpuName() : null);
		setCpuType(vds.getcpu_model());
		setSharedMemory(vds.getmem_shared_percent());
		setPhysicalMemory(vds.getphysical_mem_mb());
		setSwapTotal(vds.getswap_total());
		setSwapFree(vds.getswap_free());
	}

	private void UpdateAlerts()
	{
		setHasAnyAlert(false);
		setHasUpgradeAlert(false);
		setHasManualFenceAlert(false);
		setHasNoPowerManagementAlert(false);
		setHasReinstallAlertNonResponsive(false);
		setHasReinstallAlertInstallFailed(false);
		setHasReinstallAlertMaintenance(false);
		setHasNICsAlert(false);
		getInstallCommand().setIsExecutionAllowed(true);
		getEditHostCommand().setIsExecutionAllowed(VdcActionUtils.CanExecute(new java.util.ArrayList<VDS>(java.util.Arrays.asList(new VDS[] { getEntity() })), VDS.class, VdcActionType.UpdateVds));
		//Check the network alert presense.
		setHasNICsAlert((getEntity().getnet_config_dirty() == null ? false : getEntity().getnet_config_dirty()));


		//Check manual fence alert presense.
		if (getEntity().getstatus() == VDSStatus.NonResponsive && !getEntity().getpm_enabled() && ((getEntity().getvm_active() == null ? 0 : getEntity().getvm_active()) > 0 || getEntity().getspm_status() == VdsSpmStatus.SPM))
		{
			setHasManualFenceAlert(true);
		}
		else if (!getEntity().getpm_enabled())
		{
			setHasNoPowerManagementAlert(true);
		}

		//Check the reinstall alert presense.
		if (getEntity().getstatus() == VDSStatus.NonResponsive)
		{
			setHasReinstallAlertNonResponsive(true);
		}
		else if(getEntity().getstatus() == VDSStatus.InstallFailed)
		{
			setHasReinstallAlertInstallFailed(true);
		}
		else if (getEntity().getstatus() == VDSStatus.Maintenance)
		{
			setHasReinstallAlertMaintenance(true);
		}

		// TODO: Need to come up with a logic to show the Upgrade action-item.
		// Currently, this action-item will be shown for all oVirts assuming there are
		// available oVirt ISOs that are returned by the backend's GetoVirtISOs query.
		else if (getEntity().getvds_type() == VDSType.oVirtNode)
		{
			AsyncDataProvider.GetoVirtISOsList(new AsyncQuery(this,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

				HostGeneralModel hostGeneralModel = (HostGeneralModel)target;
				java.util.ArrayList<String> isos = (java.util.ArrayList<String>)returnValue;
				if (isos.size() > 0)
				{
					VDS vds = hostGeneralModel.getEntity();
					hostGeneralModel.setHasUpgradeAlert(true);
					hostGeneralModel.getInstallCommand().setIsExecutionAllowed(vds.getstatus() != VDSStatus.Up && vds.getstatus() != VDSStatus.Installing && vds.getstatus() != VDSStatus.PreparingForMaintenance && vds.getstatus() != VDSStatus.Reboot && vds.getstatus() != VDSStatus.PendingApproval);
					if (!hostGeneralModel.getInstallCommand().getIsExecutionAllowed())
					{
						hostGeneralModel.getInstallCommand().getExecuteProhibitionReasons().add("Switch to maintenance mode to enable Upgrade.");
					}
				}

			}
		}));
		}

		setNonOperationalReasonEntity((getEntity().getNonOperationalReason() == NonOperationalReason.NONE ? null : (NonOperationalReason)getEntity().getNonOperationalReason()));

		setHasAnyAlert(getHasNICsAlert() || getHasUpgradeAlert() || getHasManualFenceAlert() || getHasNoPowerManagementAlert() || getHasReinstallAlertNonResponsive() || getHasReinstallAlertInstallFailed() || getHasReinstallAlertMaintenance());
	}

	private void GoToEvents()
	{
		this.getRequestGOToEventsTabEvent().raise(this, null);
	}

	private void UpdateMemory()
	{
		setFreeMemory(null);
		setUsedMemory(null);
		if (getEntity().getphysical_mem_mb() != null && getEntity().getusage_mem_percent() != null)
		{
//C# TO JAVA CONVERTER TODO TASK: Arithmetic operations involving nullable type instances are not converted to null-value logic:
			setFreeMemory(getEntity().getphysical_mem_mb() - (getEntity().getphysical_mem_mb() / 100 * getEntity().getusage_mem_percent()));
//C# TO JAVA CONVERTER TODO TASK: Arithmetic operations involving nullable type instances are not converted to null-value logic:
			setUsedMemory(getEntity().getphysical_mem_mb() - getFreeMemory());
		}
	}

	private void UpdateSwapUsed()
	{
		setUsedSwap(null);
		if (getEntity().getswap_total() != null && getEntity().getswap_free() != null)
		{
//C# TO JAVA CONVERTER TODO TASK: Arithmetic operations involving nullable type instances are not converted to null-value logic:
			setUsedSwap(getEntity().getswap_total() - getEntity().getswap_free());
		}
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getSaveNICsConfigCommand())
		{
			SaveNICsConfig();
		}
		else if (command == getInstallCommand())
		{
			Install();
		}
		else if (command == getEditHostCommand())
		{
			EditHost();
		}
		else if (command == getGoToEventsCommand())
		{
			GoToEvents();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnInstall"))
		{
			OnInstall();
		}
		else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
	}
}