package org.ovirt.engine.ui.uicommon.models.vms;
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
import org.ovirt.engine.ui.uicommon.validation.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class RunOnceModel extends Model
{

	private EntityModel privateAttachFloppy;
	public EntityModel getAttachFloppy()
	{
		return privateAttachFloppy;
	}
	private void setAttachFloppy(EntityModel value)
	{
		privateAttachFloppy = value;
	}
	private ListModel privateFloppyImage;
	public ListModel getFloppyImage()
	{
		return privateFloppyImage;
	}
	private void setFloppyImage(ListModel value)
	{
		privateFloppyImage = value;
	}
	private EntityModel privateAttachIso;
	public EntityModel getAttachIso()
	{
		return privateAttachIso;
	}
	private void setAttachIso(EntityModel value)
	{
		privateAttachIso = value;
	}
	private ListModel privateIsoImage;
	public ListModel getIsoImage()
	{
		return privateIsoImage;
	}
	private void setIsoImage(ListModel value)
	{
		privateIsoImage = value;
	}
	private ListModel privateDisplayProtocol;
	public ListModel getDisplayProtocol()
	{
		return privateDisplayProtocol;
	}
	private void setDisplayProtocol(ListModel value)
	{
		privateDisplayProtocol = value;
	}

	private EntityModel privateInitrd_path;
	public EntityModel getInitrd_path()
	{
		return privateInitrd_path;
	}
	private void setInitrd_path(EntityModel value)
	{
		privateInitrd_path = value;
	}
	private EntityModel privateKernel_path;
	public EntityModel getKernel_path()
	{
		return privateKernel_path;
	}
	private void setKernel_path(EntityModel value)
	{
		privateKernel_path = value;
	}
	private EntityModel privateKernel_parameters;
	public EntityModel getKernel_parameters()
	{
		return privateKernel_parameters;
	}
	private void setKernel_parameters(EntityModel value)
	{
		privateKernel_parameters = value;
	}

	private ListModel privateSysPrepDomainName;
	public ListModel getSysPrepDomainName()
	{
		return privateSysPrepDomainName;
	}
	private void setSysPrepDomainName(ListModel value)
	{
		privateSysPrepDomainName = value;
	}
	private EntityModel privateSysPrepUserName;
	public EntityModel getSysPrepUserName()
	{
		return privateSysPrepUserName;
	}
	private void setSysPrepUserName(EntityModel value)
	{
		privateSysPrepUserName = value;
	}
	private EntityModel privateSysPrepPassword;
	public EntityModel getSysPrepPassword()
	{
		return privateSysPrepPassword;
	}
	private void setSysPrepPassword(EntityModel value)
	{
		privateSysPrepPassword = value;
	}
	private EntityModel privateUseAlternateCredentials;
	public EntityModel getUseAlternateCredentials()
	{
		return privateUseAlternateCredentials;
	}
	private void setUseAlternateCredentials(EntityModel value)
	{
		privateUseAlternateCredentials = value;
	}
	private EntityModel privateIsSysprepEnabled;
	public EntityModel getIsSysprepEnabled()
	{
		return privateIsSysprepEnabled;
	}
	private void setIsSysprepEnabled(EntityModel value)
	{
		privateIsSysprepEnabled = value;
	}
	private EntityModel privateIsVmFirstRun;
	public EntityModel getIsVmFirstRun()
	{
		return privateIsVmFirstRun;
	}
	private void setIsVmFirstRun(EntityModel value)
	{
		privateIsVmFirstRun = value;
	}

	private EntityModel privateCustomProperties;
	public EntityModel getCustomProperties()
	{
		return privateCustomProperties;
	}
	private void setCustomProperties(EntityModel value)
	{
		privateCustomProperties = value;
	}

	private EntityModel privateRunAndPause;
	public EntityModel getRunAndPause()
	{
		return privateRunAndPause;
	}
	public void setRunAndPause(EntityModel value)
	{
		privateRunAndPause = value;
	}
	private EntityModel privateRunAsStateless;
	public EntityModel getRunAsStateless()
	{
		return privateRunAsStateless;
	}
	public void setRunAsStateless(EntityModel value)
	{
		privateRunAsStateless = value;
	}

	private boolean privateIsLinux_Unassign_UnknownOS;
	public boolean getIsLinux_Unassign_UnknownOS()
	{
		return privateIsLinux_Unassign_UnknownOS;
	}
	public void setIsLinux_Unassign_UnknownOS(boolean value)
	{
		privateIsLinux_Unassign_UnknownOS = value;
	}
	private boolean privateIsWindowsOS;
	public boolean getIsWindowsOS()
	{
		return privateIsWindowsOS;
	}
	public void setIsWindowsOS(boolean value)
	{
		privateIsWindowsOS = value;
	}

	private boolean hwAcceleration;
	public boolean getHwAcceleration()
	{
		return hwAcceleration;
	}
	public void setHwAcceleration(boolean value)
	{
		if (hwAcceleration != value)
		{
			hwAcceleration = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HwAcceleration"));
		}
	}

	private BootSequenceModel bootSequence;
	public BootSequenceModel getBootSequence()
	{
		return bootSequence;
	}
	public void setBootSequence(BootSequenceModel value)
	{
		if (bootSequence != value)
		{
			bootSequence = value;
			OnPropertyChanged(new PropertyChangedEventArgs("BootSequence"));
		}
	}

		// The "sysprep" option was moved from a standalone check box to a
		// pseudo floppy disk image. In order not to change the back-end
		// interface, the Reinitialize variable was changed to a read-only
		// property and its value is based on the selected floppy image.
	public boolean getReinitialize()
	{
		return ((Boolean)getAttachFloppy().getEntity() && getFloppyImage().getSelectedItem() != null && getFloppyImage().getSelectedItem().equals("[sysprep]"));
	}

	public String getFloppyImagePath()
	{
		if ((Boolean)getAttachFloppy().getEntity())
		{
			return getReinitialize() ? "" : (String)getFloppyImage().getSelectedItem();
		}
		else
		{
			return "";
		}
	}

	private java.util.ArrayList<String> privateCustomPropertiesKeysList;
	public java.util.ArrayList<String> getCustomPropertiesKeysList()
	{
		return privateCustomPropertiesKeysList;
	}
	public void setCustomPropertiesKeysList(java.util.ArrayList<String> value)
	{
		privateCustomPropertiesKeysList = value;
	}



	public RunOnceModel()
	{
		setAttachFloppy(new EntityModel());
		getAttachFloppy().getEntityChangedEvent().addListener(this);
		setFloppyImage(new ListModel());
		getFloppyImage().getSelectedItemChangedEvent().addListener(this);
		setAttachIso(new EntityModel());
		getAttachIso().getEntityChangedEvent().addListener(this);
		setIsoImage(new ListModel());
		setDisplayProtocol(new ListModel());
		setBootSequence(new BootSequenceModel());

		setKernel_parameters(new EntityModel());
		setKernel_path(new EntityModel());
		setInitrd_path(new EntityModel());

		setSysPrepDomainName(new ListModel());
		EntityModel tempVar = new EntityModel();
		tempVar.setIsChangable(false);
		setSysPrepUserName(tempVar);
		EntityModel tempVar2 = new EntityModel();
		tempVar2.setIsChangable(false);
		setSysPrepPassword(tempVar2);

		setIsSysprepEnabled(new EntityModel());
		EntityModel tempVar3 = new EntityModel();
		tempVar3.setEntity(false);
		setIsVmFirstRun(tempVar3);
		getIsVmFirstRun().getEntityChangedEvent().addListener(this);
		EntityModel tempVar4 = new EntityModel();
		tempVar4.setEntity(false);
		setUseAlternateCredentials(tempVar4);
		getUseAlternateCredentials().getEntityChangedEvent().addListener(this);

		setCustomProperties(new EntityModel());

		EntityModel tempVar5 = new EntityModel();
		tempVar5.setEntity(false);
		setRunAndPause(tempVar5);
		EntityModel tempVar6 = new EntityModel();
		tempVar6.setEntity(false);
		setRunAsStateless(tempVar6);
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(ListModel.SelectedItemChangedEventDefinition))
		{
			if (sender == getFloppyImage())
			{
				FloppyImage_SelectedItemChanged();
			}
		}
		else if (ev.equals(EntityModel.EntityChangedEventDefinition))
		{
			if (sender == getAttachFloppy())
			{
				AttachFloppy_EntityChanged();
			}
			else if (sender == getAttachIso())
			{
				AttachIso_EntityChanged();
			}
			else if (sender == getIsVmFirstRun())
			{
				IsVmFirstRun_EntityChanged();
			}
			else if (sender == getUseAlternateCredentials())
			{
				UseAlternateCredentials_EntityChanged();
			}
		}
	}

	private void AttachIso_EntityChanged()
	{
		getIsoImage().setIsChangable((Boolean)getAttachIso().getEntity());
		getBootSequence().getCdromOption().setIsChangable((Boolean)getAttachIso().getEntity());
	}

	private void AttachFloppy_EntityChanged()
	{
		getFloppyImage().setIsChangable((Boolean)getAttachFloppy().getEntity());
		UpdateIsSysprepEnabled();
	}

	private void UseAlternateCredentials_EntityChanged()
	{
		boolean useAlternateCredentials = (Boolean) getUseAlternateCredentials().getEntity();

		getSysPrepUserName().setIsChangable((Boolean)getUseAlternateCredentials().getEntity());
		getSysPrepPassword().setIsChangable((Boolean)getUseAlternateCredentials().getEntity());

		getSysPrepUserName().setEntity(useAlternateCredentials ? "" : null);
		getSysPrepPassword().setEntity(useAlternateCredentials ? "" : null);
	}

	private void IsVmFirstRun_EntityChanged()
	{
		UpdateIsSysprepEnabled();
	}

	private void FloppyImage_SelectedItemChanged()
	{
		UpdateIsSysprepEnabled();
	}

	// Sysprep section is displayed only when VM's OS-type is 'Windows'
	// and [Reinitialize-sysprep == true || IsVmFirstRun == true (IsVmFirstRun == !VM.is_initialized) and no attached floppy]
	private void UpdateIsSysprepEnabled()
	{
		boolean isFloppyAttached = (Boolean) getAttachFloppy().getEntity();
		boolean isVmFirstRun = (Boolean)getIsVmFirstRun().getEntity();

		getIsSysprepEnabled().setEntity(getIsWindowsOS() && (getReinitialize() || (isVmFirstRun && !isFloppyAttached)));
	}

	public boolean Validate()
	{
		getIsoImage().setIsValid(true);
		if ((Boolean)getAttachIso().getEntity())
		{
			getIsoImage().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
		}

		getFloppyImage().setIsValid(true);
		if ((Boolean)getAttachFloppy().getEntity())
		{
			getFloppyImage().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
		}

		getKernel_path().setIsValid(true);
		getKernel_parameters().setIsValid(true);
		getInitrd_path().setIsValid(true);
		if (getKernel_path().getEntity() == null)
		{
			getKernel_path().setEntity("");
		}
		if (getKernel_parameters().getEntity() == null)
		{
			getKernel_parameters().setEntity("");
		}
		if (getInitrd_path().getEntity() == null)
		{
			getInitrd_path().setEntity("");
		}

		getCustomProperties().ValidateEntity(new IValidation[] { new CustomPropertyValidation(this.getCustomPropertiesKeysList()) });



		if (getIsLinux_Unassign_UnknownOS() && ((((String)getKernel_parameters().getEntity()).length() > 0 || ((String)getInitrd_path().getEntity()).length() > 0) && ((String)getKernel_path().getEntity()).length() == 0))
		{
			int count = 0;
			String msg = "When ";
			if (((String)getKernel_parameters().getEntity()).length() > 0)
			{
				getKernel_parameters().setIsValid(false);
				msg += "a kernel parameter argument ";
				count++;
			}
			if (((String)getInitrd_path().getEntity()).length() > 0)
			{
				getInitrd_path().setIsValid(false);
				if (count == 1)
				{
					msg += "or ";
				}
				msg += "an initrd path ";
			}
			msg += "is used, kernel path must be non-empty";

			getKernel_path().setIsValid(false);
			getInitrd_path().getInvalidityReasons().add(msg);
			getKernel_parameters().getInvalidityReasons().add(msg);
			getKernel_path().getInvalidityReasons().add(msg);
		}

		return getIsoImage().getIsValid() && getFloppyImage().getIsValid() && getKernel_path().getIsValid() && getCustomProperties().getIsValid();
	}
}