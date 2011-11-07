package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmTemplateImportExportParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.BackendTemplatesResourceTest.verifyModelSpecific;

public class BackendStorageDomainTemplatesResourceTest
    extends AbstractBackendCollectionResourceTest<Template, VmTemplate, BackendStorageDomainTemplatesResource> {

    private static final Guid DATA_CENTER_ID = GUIDS[0];
    private static final Guid STORAGE_DOMAIN_ID = GUIDS[GUIDS.length-1];

    public BackendStorageDomainTemplatesResourceTest() {
        super(new BackendStorageDomainTemplatesResource(STORAGE_DOMAIN_ID), null, null);
    }

    @Test
    @Ignore
    public void testQuery() throws Exception {
    }

    @Test
    @Override
    @Ignore
    public void testList() throws Exception {
    }

    @Test
    @Override
    @Ignore
    public void testListFailure() throws Exception {

    }

    @Test
    @Override
    @Ignore
    public void testListCrash() throws Exception {

    }

    @Test
    @Override
    @Ignore
    public void testListCrashClientLocale() throws Exception {

    }

    @Test
    public void testListExport() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 1);
        setUpQueryExpectations("", null, StorageDomainType.ImportExport, true);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpQueryExpectations(query, failure, StorageDomainType.Data, true);
    }

    protected void setUpQueryExpectations(String query, Object failure, StorageDomainType domainType, boolean replay) throws Exception {
        assert(query.equals(""));

        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainById,
                                     StorageDomainQueryParametersBase.class,
                                     new String[] { "StorageDomainId" },
                                     new Object[] { STORAGE_DOMAIN_ID },
                                     setUpStorageDomain(domainType));

        switch (domainType) {
        case Data:
            setUpEntityQueryExpectations(VdcQueryType.GetVmTemplatesFromStorageDomain,
                                         StorageDomainQueryParametersBase.class,
                                         new String[] { "StorageDomainId" },
                                         new Object[] { STORAGE_DOMAIN_ID },
                                         setUpTemplates(),
                                         failure);
            break;
        case ImportExport:
            setUpEntityQueryExpectations(VdcQueryType.GetTemplatesFromExportDomain,
                                         GetAllFromExportDomainQueryParamenters.class,
                                         new String[] { "StoragePoolId", "StorageDomainId", "GetAll" },
                                         new Object[] { DATA_CENTER_ID, STORAGE_DOMAIN_ID, Boolean.TRUE },
                                         setUpExportTemplates(),
                                         failure);
            break;
        default:
            break;
        }

        if (replay) {
            control.replay();
        }
    }

    protected VmTemplate getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmTemplate.class), index);
    }

    protected List<VmTemplate> setUpTemplates() {
        List<VmTemplate> ret = new ArrayList<VmTemplate>();
        for (int i = 0; i < NAMES.length; i++) {
            ret.add(getEntity(i));
        }
        return ret;
    }

    protected HashMap<VmTemplate, DiskImageList> setUpExportTemplates() {
        HashMap<VmTemplate, DiskImageList> ret = new LinkedHashMap<VmTemplate, DiskImageList>();
        for (int i = 0; i < NAMES.length; i++) {
            ret.put(getEntity(i), new DiskImageList());
        }
        return ret;
    }

    public static storage_domains setUpStorageDomain(StorageDomainType domainType) {
        storage_domains entity = new storage_domains();
        entity.setid(STORAGE_DOMAIN_ID);
        entity.setstorage_domain_type(domainType);
        return entity;
    }

    public static List<storage_pool> setUpStoragePool() {
        final storage_pool entity = new storage_pool();
        entity.setId(DATA_CENTER_ID);
        return new ArrayList<storage_pool>(){
            private static final long serialVersionUID = 6544998068993726769L;
        {
            add(entity);}
        };
    }

    protected List<Template> getCollection() {
        return collection.list().getTemplates();
    }

    protected void verifyModel(Template model, int index) {
        super.verifyModel(model, index);
        verifyModelSpecific(model, index);
    }

    private void setUpGetDataCenterByStorageDomainExpectations(Guid id, int times) {
        while (times-->0) {
            setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolsByStorageDomainId,
                    StorageDomainQueryParametersBase.class,
                    new String[] { "StorageDomainId" },
                    new Object[] { id },
                    setUpStoragePool());
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpQueryExpectations("", null, StorageDomainType.ImportExport, false);
        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 2);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmTemplateFromImportExport,
                                           VmTemplateImportExportParameters.class,
                                           new String[] { "VmTemplateId", "StorageDomainId", "StoragePoolId" },
                                           new Object[] { GUIDS[0], GUIDS[3], GUIDS[0] },
                                           true,
                                           true));
        collection.remove(GUIDS[0].toString());
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpQueryExpectations("", null, StorageDomainType.ImportExport, false);
        setUpGetDataCenterByStorageDomainExpectations(GUIDS[3], 2);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmTemplateFromImportExport,
                                           VmTemplateImportExportParameters.class,
                                           new String[] { "VmTemplateId", "StorageDomainId", "StoragePoolId" },
                                           new Object[] { GUIDS[0], GUIDS[3], GUIDS[0] },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }
}
