package org.ovirt.engine.api.restapi.resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Templates;
import org.ovirt.engine.api.resource.RemovableStorageDomainContentsResource;
import org.ovirt.engine.api.resource.StorageDomainContentResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateImportExportParameters;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainTemplatesResource
    extends AbstractBackendStorageDomainContentsResource<Templates, Template, VmTemplate>
    implements RemovableStorageDomainContentsResource<Templates, Template> {

    public BackendStorageDomainTemplatesResource(Guid storageDomainId) {
        super(storageDomainId, Template.class, VmTemplate.class);
    }

    @Override
    public Templates list() {
        Templates templates = new Templates();
        templates.getTemplates().addAll(getCollection());
        return templates;
    }

    @Override
    protected Template addParents(Template template) {
        template.setStorageDomain(getStorageDomainModel());
        return template;
    }

    @Override
    protected Collection<VmTemplate> getEntitiesFromDataDomain() {
        return getBackendCollection(VdcQueryType.GetVmTemplatesFromStorageDomain,
                                    new StorageDomainQueryParametersBase(storageDomainId));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<VmTemplate> getEntitiesFromExportDomain() {
        GetAllFromExportDomainQueryParamenters params =
            new GetAllFromExportDomainQueryParamenters(getDataCenterId(storageDomainId), storageDomainId);
        params.setGetAll(true);

        Map<VmTemplate, DiskImageList> ret =
            (Map<VmTemplate, DiskImageList>)getEntity(HashMap.class,
                                                      VdcQueryType.GetTemplatesFromExportDomain,
                                                      params,
                                                      "Templates under storage domain id : " + storageDomainId.toString());
        return ret.keySet();
    }

    @Override
    @SingleEntityResource
    public StorageDomainContentResource<Template> getStorageDomainContentSubResource(String id) {
        return inject(new BackendStorageDomainTemplateResource(this, id));
    }

    @Override
    public void performRemove(String id) {
        performAction(VdcActionType.RemoveVmTemplateFromImportExport,
                      new VmTemplateImportExportParameters(asGuid(id),
                                                           storageDomainId,
                                                           getDataCenterId(storageDomainId)));
    }
}
