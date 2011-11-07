package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;
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
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.ui.uicommonweb.models.configure.*;

@SuppressWarnings("unused")
public class RoleNode
{
	private static Translator actionGroupTranslator = EnumTranslator.Create(ActionGroup.class);
	public RoleNode(String name, RoleNode[] leafs)
	{
		this.setName(name);
		this.setLeafRoles(new java.util.ArrayList<RoleNode>());
		for (RoleNode roleNode : leafs)
		{
			this.getLeafRoles().add(roleNode);
		}
	}

	public RoleNode(String name, String tooltip, RoleNode[] leafs)
	{
		this(name, leafs);
		this.setTooltip(tooltip);
	}

	public RoleNode(String name, RoleNode leaf)
	{
		this.setName(name);
		this.setLeafRoles(new java.util.ArrayList<RoleNode>());
		this.getLeafRoles().add(leaf);
	}
	public RoleNode(ActionGroup actionGroup, String tooltip)
	{
		this.setName(actionGroup.toString());
		this.setTooltip(tooltip);
		this.setDesc(RoleNode.actionGroupTranslator.get(actionGroup));
	}
	public RoleNode(String name, String desc)
	{
		this.setName(name);
		this.setDesc(desc);
	}
	private String privateName;
	public String getName()
	{
		return privateName;
	}
	private void setName(String value)
	{
		privateName = value;
	}
	private String privateTooltip;
	public String getTooltip()
	{
		return privateTooltip;
	}
	private void setTooltip(String value)
	{
		privateTooltip = value;
	}
	private String privateDesc;
	public String getDesc()
	{
		return privateDesc;
	}
	private void setDesc(String value)
	{
		privateDesc = value;
	}
	private java.util.ArrayList<RoleNode> privateLeafRoles;
	public java.util.ArrayList<RoleNode> getLeafRoles()
	{
		return privateLeafRoles;
	}
	private void setLeafRoles(java.util.ArrayList<RoleNode> value)
	{
		privateLeafRoles = value;
	}
}