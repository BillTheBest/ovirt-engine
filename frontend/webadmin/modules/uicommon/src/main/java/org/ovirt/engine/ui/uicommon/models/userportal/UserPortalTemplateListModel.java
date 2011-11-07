package org.ovirt.engine.ui.uicommon.models.userportal;
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

import org.ovirt.engine.ui.uicommon.models.templates.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class UserPortalTemplateListModel extends TemplateListModel implements IFrontendMultipleQueryAsyncCallback
{
	@Override
	protected void SyncSearch()
	{
		MultilevelAdministrationByAdElementIdParameters parameters = new MultilevelAdministrationByAdElementIdParameters(Frontend.getLoggedInUser().getUserId());

		// Get user permissions and send them to PostGetUserPermissions:
		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object ReturnValue)
		{
			UserPortalTemplateListModel userPortalTemplateListModel = (UserPortalTemplateListModel)model;
			java.util.ArrayList<permissions> userPermissions = ReturnValue != null ? (java.util.ArrayList<permissions>)((VdcQueryReturnValue)ReturnValue).getReturnValue() : new java.util.ArrayList<permissions>();

			userPortalTemplateListModel.PostGetUserPermissions(userPermissions);
		}};

		Frontend.RunQuery(VdcQueryType.GetPermissionsByAdElementId, parameters, _asyncQuery);
	}

	public void PostGetUserPermissions(java.util.ArrayList<permissions> userPermissions)
	{
		java.util.ArrayList<VdcQueryType> listQueryType = new java.util.ArrayList<VdcQueryType>();
		java.util.ArrayList<VdcQueryParametersBase> listQueryParameters = new java.util.ArrayList<VdcQueryParametersBase>();

		for (permissions userPermission : userPermissions)
		{
			if (userPermission.getObjectType() == VdcObjectType.System)
			{
				// User has a permission on System -> Get all templates in the system:
				listQueryType.add(VdcQueryType.Search);
				SearchParameters searchParams = new SearchParameters("Template:", SearchType.VmTemplate);
				searchParams.setMaxCount(9999);
				listQueryParameters.add(searchParams);
				break;
			}
			else
			{
				// if user has a permission on a Template, add a query-request for that template:
				if (userPermission.getObjectType() == VdcObjectType.VmTemplate)
				{
					listQueryType.add(VdcQueryType.GetVmTemplate);
					listQueryParameters.add(new GetVmTemplateParameters(userPermission.getObjectId()));
				}
				// if user has a permission on a DataCenter, add a query-request for all the templates in that DataCenter:
				else if (userPermission.getObjectType() == VdcObjectType.StoragePool)
				{
					listQueryType.add(VdcQueryType.Search);
					SearchParameters searchParams = new SearchParameters("Template: datacenter = " + userPermission.getObjectName(), SearchType.VmTemplate);
					searchParams.setMaxCount(9999);
					listQueryParameters.add(searchParams);
				}
			}
		}

		GetUserTemplates(listQueryType, listQueryParameters);
	}

	private void GetUserTemplates(java.util.ArrayList<VdcQueryType> listQueryType, java.util.ArrayList<VdcQueryParametersBase> listQueryParameters)
	{
		if (listQueryType.isEmpty())
		{
			setItems(new java.util.ArrayList<VmTemplate>());
		}
		else
		{
			Frontend.RunMultipleQueries(listQueryType, listQueryParameters, this);
		}
	}


	public void Executed(FrontendMultipleQueryAsyncResult result)
	{
		java.util.ArrayList<VmTemplate> items = new java.util.ArrayList<VmTemplate>();

		if (result != null)
		{
			java.util.List<VdcQueryType> listQueryType = result.getQueryTypes();
			java.util.List<VdcQueryReturnValue> listReturnValue = result.getReturnValues();
			for (int i = 0; i < listQueryType.size(); i++)
			{
				switch (listQueryType.get(i))
				{
					case GetVmTemplate:
						if (listReturnValue.get(i) != null && listReturnValue.get(i).getSucceeded() && listReturnValue.get(i).getReturnValue() != null)
						{
							VmTemplate template = (VmTemplate) listReturnValue.get(i).getReturnValue();
							items.add(template);
						}
						break;

					case Search:
						if (listReturnValue.get(i) != null && listReturnValue.get(i).getSucceeded() && listReturnValue.get(i).getReturnValue() != null)
						{
							java.util.ArrayList<VmTemplate> templateList = (java.util.ArrayList<VmTemplate>) listReturnValue.get(i).getReturnValue();
							items.addAll(templateList);
						}
						break;
				}
			}
		}

		// Sort templates list
		java.util.ArrayList<VmTemplate> list = new java.util.ArrayList<VmTemplate>();
		VmTemplate blankTemplate = new VmTemplate();
		for (VmTemplate template : items)
		{
			if (template.getId().equals(Guid.Empty))
			{
				blankTemplate = template;
				continue;
			}
			list.add(template);
		}
		Collections.sort(list, new Linq.VmTemplateByNameComparer());
		if (items.contains(blankTemplate))
		{
			list.add(0, blankTemplate);
		}

		setItems(list);
	}

	@Override
	protected void UpdateActionAvailability()
	{
		VmTemplate item = (VmTemplate)getSelectedItem();
		if (item != null)
		{
			java.util.ArrayList items = new java.util.ArrayList();
			items.add(item);
			getEditCommand().setIsExecutionAllowed(item.getstatus() != VmTemplateStatus.Locked && !item.getId().equals(Guid.Empty));
			getRemoveCommand().setIsExecutionAllowed(VdcActionUtils.CanExecute(items, VmTemplate.class, VdcActionType.RemoveVmTemplate));
		}
		else
		{
			getEditCommand().setIsExecutionAllowed(false);
			getRemoveCommand().setIsExecutionAllowed(false);
		}
	}

}