package org.ovirt.engine.ui.uicommon.models.hosts;
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

import org.ovirt.engine.ui.uicommon.models.clusters.*;
import org.ovirt.engine.ui.uicommon.models.common.*;
import org.ovirt.engine.ui.uicommon.models.configure.*;
import org.ovirt.engine.ui.uicommon.models.datacenters.*;
import org.ovirt.engine.ui.uicommon.models.tags.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class DataBag
{
	private Guid privateDataCenterId;
	public Guid getDataCenterId()
	{
		return privateDataCenterId;
	}
	public void setDataCenterId(Guid value)
	{
		privateDataCenterId = value;
	}
	private Guid privateClusterId;
	public Guid getClusterId()
	{
		return privateClusterId;
	}
	public void setClusterId(Guid value)
	{
		privateClusterId = value;
	}
	private Guid privateOldClusterId;
	public Guid getOldClusterId()
	{
		return privateOldClusterId;
	}
	public void setOldClusterId(Guid value)
	{
		privateOldClusterId = value;
	}
}