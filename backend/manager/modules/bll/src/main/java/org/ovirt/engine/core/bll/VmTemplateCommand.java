package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmTemplateParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.vdscommands.RemoveVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.UpdateVMVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.ovf.OvfManager;

public abstract class VmTemplateCommand<T extends VmTemplateParametersBase> extends CommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected VmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    public VmTemplateCommand(T parameters) {
        super(parameters);
        setVmTemplateId(parameters.getVmTemplateId());
    }

    public VmTemplateCommand() {
    }

    protected String mVmTemplateDescription = "";

    @Override
    protected void executeCommand() {
        throw new NotImplementedException();
    }

    public static boolean isVmTemlateWithSameNameExist(String name) {
        SearchParameters p = new SearchParameters("template : name=" + name, SearchType.VmTemplate);
        p.setMaxCount(Integer.MAX_VALUE);
        List<IVdcQueryable> list = (List<IVdcQueryable>) Backend.getInstance().runInternalQuery(VdcQueryType.Search, p)
                .getReturnValue();

        return list.size() > 0;
    }

    public static boolean isVmTemplateImagesReady(Guid vmTemplateId,
                                                  Guid storageDomainId,
                                                  java.util.ArrayList<String> reasons,
                                                  boolean checkImagesExists,
                                                  boolean checkLocked,
                                                  boolean checkIllegal,
                                                  boolean checkStorageDomain) {
        boolean returnValue = true;
        VmTemplate vmTemplate = DbFacade.getInstance().getVmTemplateDAO().get(vmTemplateId);
        if (checkStorageDomain) {
            storage_domains storageDomain = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(
                    storageDomainId, vmTemplate.getstorage_pool_id());
            if (storageDomain == null) {
                reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST.toString());
                returnValue = false;
            } else if (storageDomain.getstatus() == null || storageDomain.getstatus() != StorageDomainStatus.Active) {
                reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL.toString());
                returnValue = false;
            }
        }
        if (returnValue && checkImagesExists) {
            java.util.ArrayList<DiskImage> vmtImages = new java.util.ArrayList<DiskImage>();
            for (DiskImageTemplate image : DbFacade.getInstance()
                    .getDiskImageTemplateDAO()
                    .getAllByVmTemplate(vmTemplateId)) {
                if (!VmTemplateHandler.BlankVmTemplateId.equals(image.getId())) {
                    vmtImages.add(DbFacade.getInstance().getDiskImageDAO().getSnapshotById(image.getId()));
                }
            }
            if (vmtImages.size() > 0
                    && !ImagesHandler.isImagesExists(vmtImages, vmtImages.get(0).getstorage_pool_id().getValue(),
                            storageDomainId)) {
                reasons.add(VdcBllMessages.TEMPLATE_IMAGE_NOT_EXIST.toString());
                returnValue = false;
            }
        }
        if (returnValue) {
            if (checkLocked && (vmTemplate.getstatus() == VmTemplateStatus.Locked)) {
                returnValue = false;
                reasons.add(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED.toString());
            } else if (checkIllegal && (vmTemplate.getstatus() == VmTemplateStatus.Illegal)) {
                returnValue = false;
                reasons.add(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_ILLEGAL.toString());
            }
        }
        return returnValue;
    }

    /**
     * Determines whether [is domain legal] [the specified domain name].
     *
     * @param domainName
     *            Name of the domain.
     * @param reasons
     *            The reasons.
     * @return <c>true</c> if [is domain legal] [the specified domain name];
     *         otherwise, <c>false</c>.
     */
    public static boolean IsDomainLegal(String domainName, java.util.ArrayList<String> reasons) {
        boolean result = true;
        char[] illegalChars = new char[] { '&' };
        if (!StringHelper.isNullOrEmpty(domainName)) {
            for (char c : illegalChars) {
                if (domainName.contains((Character.toString(c)))) {
                    result = false;
                    reasons.add(VdcBllMessages.ACTION_TYPE_FAILED_ILLEGAL_DOMAIN_NAME.toString());
                    reasons.add(String.format("$Domain %1$s", domainName));
                    reasons.add(String.format("$Char %1$s", c));
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines whether [is high availability value legal] [the specified
     * value].
     *
     * @param value
     *            The value.
     * @param reasons
     *            The reasons.
     * @return <c>true</c> if [is vm priority value legal] [the specified
     *         value]; otherwise, <c>false</c>.
     */
    public static boolean IsVmPriorityValueLegal(int value, java.util.ArrayList<String> reasons) {
        boolean res = false;
        if (value >= 0 && value <= Config.<Integer> GetValue(ConfigValues.VmPriorityMaxValue)) {
            res = true;
        } else {
            reasons.add(VdcBllMessages.VM_OR_TEMPLATE_ILLEGAL_PRIORITY_VALUE.toString());
            reasons.add(String.format("$MaxValue %1$s", Config.<Integer> GetValue(ConfigValues.VmPriorityMaxValue)));
        }
        return res;
    }

    @Override
    protected String getDescription() {
        return getVmTemplateName();
    }

    /**
     * This method create OVF for each template in list and call updateVm in SPM
     *
     * @param storagePoolId
     * @param templatesList
     * @return Returns true if updateVm succeeded.
     */
    public static boolean UpdateTemplateInSpm(Guid storagePoolId, java.util.ArrayList<VmTemplate> templatesList) {
        return UpdateTemplateInSpm(storagePoolId, templatesList, Guid.Empty, null);
    }

    public static boolean UpdateTemplateInSpm(Guid storagePoolId, java.util.ArrayList<VmTemplate> templatesList,
                                              Guid storageDomainId, List<DiskImage> images) {
        java.util.HashMap<Guid, KeyValuePairCompat<String, List<Guid>>> templatesAndMetaDictionary =
                new java.util.HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>(
                        templatesList.size());
        OvfManager ovfManager = new OvfManager();
        for (VmTemplate template : templatesList) {
            List<DiskImage> allTemplateImages = images;
            if (allTemplateImages == null) {
                allTemplateImages = (List) Backend
                        .getInstance()
                        .runInternalQuery(VdcQueryType.GetVmTemplatesDisks,
                                new GetVmTemplatesDisksParameters(template.getId())).getReturnValue();
            }

            // TODO remove this when the API changes
            template.getInterfaces().clear();
            for (VmNetworkInterface iface : DbFacade.getInstance().getVmNetworkInterfaceDAO()
                    .getAllForTemplate(template.getId())) {
                template.getInterfaces().add(iface);
            }

            String templateMeta = null;
            RefObject<String> tempRefObject = new RefObject<String>(templateMeta);
            ovfManager.ExportTemplate(tempRefObject, template, allTemplateImages);
            templateMeta = tempRefObject.argvalue;
            // LINQ 29456
            // templatesAndMetaDictionary.Add(template.vmt_guid, new
            // KeyValuePair<string, List<Guid>>
            // (templateMeta, allTemplateImages.Select(a =>
            // a.image_group_id.Value).ToList()));
            templatesAndMetaDictionary.put(template.getId(), new KeyValuePairCompat<String, List<Guid>>(
                    templateMeta, LinqUtils.foreach(allTemplateImages, new Function<DiskImage, Guid>() {
                        @Override
                        public Guid eval(DiskImage diskImage) {
                            return diskImage.getimage_group_id().getValue();
                        }
                    })));
        }
        UpdateVMVDSCommandParameters tempVar = new UpdateVMVDSCommandParameters(storagePoolId,
                templatesAndMetaDictionary);
        tempVar.setStorageDomainId(storageDomainId);
        return Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.UpdateVM, tempVar)
                .getSucceeded();
    }

    protected static boolean RemoveTemplateInSpm(Guid storagePoolId, Guid templateId) {
        return Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.RemoveVM, new RemoveVMVDSCommandParameters(storagePoolId, templateId))
                .getSucceeded();
    }

    protected static boolean RemoveTemplateInSpm(Guid storagePoolId, Guid templateId, Guid storageDomainId) {
        RemoveVMVDSCommandParameters tempVar = new RemoveVMVDSCommandParameters(storagePoolId, templateId);
        tempVar.setStorageDomainId(storageDomainId);
        return Backend.getInstance().getResourceManager().RunVdsCommand(VDSCommandType.RemoveVM, tempVar)
                .getSucceeded();
    }

    protected void RemoveNetwork() {
        List<VmNetworkInterface> list = DbFacade.getInstance().getVmNetworkInterfaceDAO()
                .getAllForTemplate(getVmTemplateId());
        for (VmNetworkInterface iface : list) {
            DbFacade.getInstance().getVmNetworkInterfaceDAO().remove(iface.getId());
            // \\DbFacade.Instance.RemoveInterfaceStatistics(iface.id);
        }
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(getVmTemplateId(), VdcObjectType.VmTemplate);
    }
}
