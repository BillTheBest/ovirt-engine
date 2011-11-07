package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;
import org.ovirt.engine.core.common.queries.StoragePoolQueryParametersBase;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.expect;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqQueryParams;

public class BackendAttachedStorageDomainsResourceTest
    extends AbstractBackendCollectionResourceTest<StorageDomain,
                                                  storage_domains,
                                                  BackendAttachedStorageDomainsResource> {

    public BackendAttachedStorageDomainsResourceTest() {
        super(new BackendAttachedStorageDomainsResource(GUIDS[NAMES.length-1].toString()), null, null);
    }

    @Test
    @Ignore
    public void testQuery() throws Exception {
    }

    @Test
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());

        setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[0].toString() },
                setUpStorageServerConnection());

        setUpCreationExpectations(VdcActionType.AttachStorageDomainToPool,
                                  DetachStorageDomainFromPoolParameters.class,
                                  new String[] { "StorageDomainId", "StoragePoolId" },
                                  new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetStorageDomainByIdAndStoragePoolId,
                                  StorageDomainAndPoolQueryParameters.class,
                                  new String[] { "StorageDomainId", "StoragePoolId" },
                                  new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                  getEntity(0));

        StorageDomain model = new StorageDomain();
        model.setId(GUIDS[0].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyModel((StorageDomain) response.getEntity(), 0);
    }

    static storage_server_connections setUpStorageServerConnection() {
        storage_server_connections cnx = new storage_server_connections();
            cnx.setid(GUIDS[0].toString());
            cnx.setconnection("10.11.12.13" + ":" + "/1");
        return cnx;
    }

    @Test
    public void testAddByName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());

        setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[0].toString() },
                setUpStorageServerConnection());
        setUpGetEntityExpectations("Storage: name=" + NAMES[0],
                                   SearchType.StorageDomain,
                                   getEntity(0));

        setUpCreationExpectations(VdcActionType.AttachStorageDomainToPool,
                                  DetachStorageDomainFromPoolParameters.class,
                                  new String[] { "StorageDomainId", "StoragePoolId" },
                                  new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetStorageDomainByIdAndStoragePoolId,
                                  StorageDomainAndPoolQueryParameters.class,
                                  new String[] { "StorageDomainId", "StoragePoolId" },
                                  new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                  getEntity(0));

        StorageDomain model = new StorageDomain();
        model.setName(NAMES[0]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyModel((StorageDomain) response.getEntity(), 0);
    }

    @Test
    public void testAddCantDo() throws Exception {
        doTestBadAdd(false, true, CANT_DO);
    }

    @Test
    public void testAddFailure() throws Exception {
        doTestBadAdd(true, false, FAILURE);
    }

    private void doTestBadAdd(boolean canDo, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AttachStorageDomainToPool,
                                           DetachStorageDomainFromPoolParameters.class,
                                           new String[] { "StorageDomainId", "StoragePoolId" },
                                           new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                           canDo,
                                           success));

        StorageDomain model = new StorageDomain();
        model.setId(GUIDS[0].toString());

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        StorageDomain model = new StorageDomain();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "StorageDomain", "add", "id|name");
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[0].toString() },
                setUpStorageServerConnection());
        setUpGetEntityExpectations(GUIDS[0]);
        setUriInfo(setUpActionExpectations(VdcActionType.DetachStorageDomainFromPool,
                                           DetachStorageDomainFromPoolParameters.class,
                                           new String[] { "StorageDomainId", "StoragePoolId" },
                                           new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                           true,
                                           true));
        collection.remove(GUIDS[0].toString());
    }

    private void setUpGetEntityExpectations(Guid guid) throws Exception {
        setUpGetEntityExpectations(guid, false);
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(NON_EXISTANT_GUID, true);
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(wae.getResponse().getStatus(), 404);
        }
    }

    private void setUpGetEntityExpectations(Guid entityId, boolean returnNull) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStorageDomainByIdAndStoragePoolId,
                StorageDomainAndPoolQueryParameters.class,
                new String[] { "StorageDomainId", "StoragePoolId" },
                new Object[] { entityId, GUIDS[NAMES.length-1] },
                returnNull ? null : getEntity(0));
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        String[] paramNames = new String[] { "ServerConnectionId" };
        Object[] paramValues = new Object[] { GUIDS[0].toString() };
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        expect(backend.RunQuery(eq(VdcQueryType.GetStorageServerConnectionById), eqQueryParams(StorageServerConnectionQueryParametersBase.class, addSession(paramNames), addSession(paramValues))))
        .andReturn(queryResult).anyTimes();
        expect(queryResult.getSucceeded()).andReturn(true).anyTimes();
        expect(queryResult.getReturnValue()).andReturn(setUpStorageServerConnection()).anyTimes();
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                StorageServerConnectionQueryParametersBase.class,
                new String[] { "ServerConnectionId" },
                new Object[] { GUIDS[0].toString() },
                setUpStorageServerConnection());
        setUpGetEntityExpectations(GUIDS[0]);
        setUriInfo(setUpActionExpectations(VdcActionType.DetachStorageDomainFromPool,
                                           DetachStorageDomainFromPoolParameters.class,
                                           new String[] { "StorageDomainId", "StoragePoolId" },
                                           new Object[] { GUIDS[0], GUIDS[NAMES.length-1] },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assert (query.equals(""));

        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainsByStoragePoolId,
                                     StoragePoolQueryParametersBase.class,
                                     new String[] { "StoragePoolId" },
                                     new Object[] { GUIDS[NAMES.length-1] },
                                     setUpStorageDomains(),
                                     failure);

        control.replay();
    }

    protected List<storage_domains> setUpStorageDomains() {
        List<storage_domains> entities = new ArrayList<storage_domains>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    protected storage_domains getEntity(int index) {
        storage_domains entity = control.createMock(storage_domains.class);
        return setUpEntityExpectations(entity, index);
    }

    static storage_domains setUpEntityExpectations(storage_domains entity, int index) {
        expect(entity.getid()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getstatus()).andReturn(StorageDomainStatus.Active).anyTimes();
        expect(entity.getstorage_domain_type()).andReturn(StorageDomainType.Master).anyTimes();
        expect(entity.getstorage_type()).andReturn(StorageType.NFS).anyTimes();
        expect(entity.getstorage()).andReturn(GUIDS[0].toString()).anyTimes();
        return entity;
    }

    protected void verifyModel(StorageDomain model, int index) {
        verifyStorageDomain(model, index);
        verifyLinks(model);
    }

    static void verifyStorageDomain(StorageDomain model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertNotNull(model.getDataCenter());
        assertEquals(GUIDS[NAMES.length-1].toString(), model.getDataCenter().getId());
        assertEquals(org.ovirt.engine.api.model.StorageDomainStatus.ACTIVE.value(), model.getStatus().getState());
        assertEquals(true, model.isMaster());
    }

    protected List<StorageDomain> getCollection() {
        return collection.list().getStorageDomains();
    }
}
