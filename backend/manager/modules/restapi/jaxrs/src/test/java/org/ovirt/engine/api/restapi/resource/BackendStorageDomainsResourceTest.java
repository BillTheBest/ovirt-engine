package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainType;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.VolumeGroup;
import org.ovirt.engine.core.common.action.AddSANStorageDomainParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetDeviceListQueryParameters;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageDomainsResourceTest
        extends AbstractBackendCollectionResourceTest<StorageDomain, storage_domains, BackendStorageDomainsResource> {

    protected static final StorageDomainType[] TYPES = { StorageDomainType.DATA,
            StorageDomainType.ISO, StorageDomainType.EXPORT };
    protected static final StorageType[] STORAGE_TYPES = { StorageType.NFS, StorageType.NFS,
            StorageType.LOCALFS };

    protected static final int LOCAL_IDX = 2;

    protected static final String[] ADDRESSES = { "10.11.12.13", "13.12.11.10", "10.01.10.01" };
    protected static final String[] PATHS = { "/1", "/2", "/3" };
    protected static final String LUN = "1IET_00010001";
    protected static final String TARGET = "iqn.2009-08.org.fubar.engine:markmc.test1";
    protected static final Integer PORT = 3260;

    protected static final org.ovirt.engine.core.common.businessentities.StorageDomainType TYPES_MAPPED[] = {
            org.ovirt.engine.core.common.businessentities.StorageDomainType.Data,
            org.ovirt.engine.core.common.businessentities.StorageDomainType.ISO,
            org.ovirt.engine.core.common.businessentities.StorageDomainType.ImportExport };
    protected static final org.ovirt.engine.core.common.businessentities.StorageType STORAGE_TYPES_MAPPED[] = {
            org.ovirt.engine.core.common.businessentities.StorageType.NFS,
            org.ovirt.engine.core.common.businessentities.StorageType.NFS,
            org.ovirt.engine.core.common.businessentities.StorageType.LOCALFS };

    public BackendStorageDomainsResourceTest() {
        super(new BackendStorageDomainsResource(), SearchType.StorageDomain, "Storage : ");
    }

    @Test
    public void testRemoveBadGuid() throws Exception {
        control.replay();
        try {
            StorageDomain storageDomain = new StorageDomain();
            storageDomain.setHost(new Host());
            storageDomain.getHost().setId(GUIDS[1].toString());
            collection.remove("foo", storageDomain);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRemoveStorageDomainNull() throws Exception {
        control.replay();
        try {
            collection.remove(GUIDS[0].toString(), null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertEquals(400, wae.getResponse().getStatus());
        }
    }

    public void testRemoveWithHostId() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveStorageDomain,
                                           RemoveStorageDomainParameters.class,
                                           new String[] { "StorageDomainId", "VdsId", "DoFormat" },
                                           new Object[] { GUIDS[0], GUIDS[1], Boolean.FALSE },
                                           true,
                                           true));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setHost(new Host());
        storageDomain.getHost().setId(GUIDS[1].toString());
        collection.remove(GUIDS[0].toString(), storageDomain);
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStorageDomainById,
                StorageDomainQueryParametersBase.class,
                new String[] { "StorageDomainId" },
                new Object[] { GUIDS[0] },
                new storage_domains());
    }

    @Test
    public void testRemoveWithFormat() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveStorageDomain,
                                           RemoveStorageDomainParameters.class,
                                           new String[] { "StorageDomainId", "VdsId", "DoFormat" },
                                           new Object[] { GUIDS[0], GUIDS[1], Boolean.TRUE },
                                           true,
                                           true));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setHost(new Host());
        storageDomain.getHost().setId(GUIDS[1].toString());
        storageDomain.setFormat(true);
        collection.remove(GUIDS[0].toString(), storageDomain);
    }

    @Test
    public void testRemoveWithHostName() throws Exception {
        setUpGetEntityExpectations();
        VDS host = BackendHostsResourceTest.setUpEntityExpectations(control.createMock(VDS.class), 1);
        setUpGetEntityExpectations("Hosts: name=" + NAMES[1],
                                   SearchType.VDS,
                                   host);

        setUriInfo(setUpActionExpectations(VdcActionType.RemoveStorageDomain,
                                           RemoveStorageDomainParameters.class,
                                           new String[] { "StorageDomainId", "VdsId", "DoFormat" },
                                           new Object[] { GUIDS[0], GUIDS[1], Boolean.FALSE },
                                           true,
                                           true));

        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setHost(new Host());
        storageDomain.getHost().setName(NAMES[1]);
        collection.remove(GUIDS[0].toString(), storageDomain);
    }

    @Test
    public void testIncompleteRemove() throws Exception {
        control.replay();
        try {
            collection.remove(GUIDS[0].toString(), new StorageDomain());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "StorageDomain", "remove", "host.id|name");
        }
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
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveStorageDomain,
                                           RemoveStorageDomainParameters.class,
                                           new String[] { "StorageDomainId", "VdsId", "DoFormat" },
                                           new Object[] { GUIDS[0], GUIDS[1], Boolean.FALSE },
                                           canDo,
                                           success));

        try {
            StorageDomain storageDomain = new StorageDomain();
            storageDomain.setHost(new Host());
            storageDomain.getHost().setId(GUIDS[1].toString());
            collection.remove(GUIDS[0].toString(), storageDomain);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddStorageDomain() throws Exception {
        Host host = new Host();
        host.setId(GUIDS[0].toString());
        doTestAddStorageDomain(0, host, false);
    }

    @Test
    public void testAddStorageDomainWithHostName() throws Exception {
        Host host = new Host();
        host.setName(NAMES[0]);

        setUpGetEntityExpectations("Hosts: name=" + NAMES[0], SearchType.VDS, setUpVDS(0));

        doTestAddStorageDomain(0, host, false);
    }

    @Test
    public void testAddExistingStorageDomain() throws Exception {
        Host host = new Host();
        host.setId(GUIDS[0].toString());
        doTestAddStorageDomain(1, host, true);
    }

    public void doTestAddStorageDomain(int idx, Host host, boolean existing) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AddStorageServerConnection,
                                           StorageServerConnectionParametersBase.class,
                                           new String[] { "StorageServerConnection.connection", "StorageServerConnection.storage_type", "VdsId" },
                                           new Object[] { ADDRESSES[idx] + ":" + PATHS[idx], STORAGE_TYPES_MAPPED[idx], GUIDS[0] },
                                           true,
                                           true,
                                           GUIDS[idx].toString(),
                                           false));

        setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                                   StorageServerConnectionQueryParametersBase.class,
                                   new String[] { "ServerConnectionId" },
                                   new Object[] { GUIDS[idx].toString() },
                                   setUpStorageServerConnection(idx));

        setUpGetEntityExpectations(VdcQueryType.GetExistingStorageDomainList,
                                   GetExistingStorageDomainListParameters.class,
                                   new String[] { "VdsId", "StorageType", "StorageDomainType", "Path" },
                                   new Object[] { GUIDS[0], STORAGE_TYPES_MAPPED[idx], TYPES_MAPPED[idx], ADDRESSES[idx] + ":" + PATHS[idx] },
                                   getExistingStorageDomains(existing));


        setUpCreationExpectations(!existing ? VdcActionType.AddNFSStorageDomain : VdcActionType.AddExistingNFSStorageDomain,
                                  StorageDomainManagementParameter.class,
                                  new String[] {},
                                  new Object[] {},
                                  true,
                                  true,
                                  GUIDS[idx],
                                  VdcQueryType.GetStorageDomainById,
                                  StorageDomainQueryParametersBase.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[idx] },
                                  getEntity(idx));

        StorageDomain model = getModel(idx);
        model.setHost(host);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyModel((StorageDomain) response.getEntity(), idx);
    }

    @Test
    public void testAddLocalStorageDomain() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AddStorageServerConnection,
                                           StorageServerConnectionParametersBase.class,
                                           new String[] { "StorageServerConnection.connection", "StorageServerConnection.storage_type", "VdsId" },
                                           new Object[] { PATHS[LOCAL_IDX], STORAGE_TYPES_MAPPED[LOCAL_IDX], GUIDS[0] },
                                           true,
                                           true,
                                           GUIDS[LOCAL_IDX].toString(),
                                           false));

        setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                                   StorageServerConnectionQueryParametersBase.class,
                                   new String[] { "ServerConnectionId" },
                                   new Object[] { GUIDS[LOCAL_IDX].toString() },
                                   setUpLocalStorageServerConnection(LOCAL_IDX));

        setUpCreationExpectations(VdcActionType.AddLocalStorageDomain,
                                  StorageDomainManagementParameter.class,
                                  new String[] { "VdsId" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  GUIDS[LOCAL_IDX],
                                  VdcQueryType.GetStorageDomainById,
                                  StorageDomainQueryParametersBase.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[LOCAL_IDX] },
                                  getEntity(LOCAL_IDX));

        StorageDomain model = getModel(LOCAL_IDX);
        model.getStorage().setAddress(null);
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyModel((StorageDomain) response.getEntity(), LOCAL_IDX);
    }

    @Test
    public void testAddIscsiStorageDomain() throws Exception {
        StorageDomain model = getIscsi();

        Host host = new Host();
        host.setId(GUIDS[0].toString());
        model.setHost(host);

        setUriInfo(setUpActionExpectations(VdcActionType.ConnectStorageToVds,
                                           StorageServerConnectionParametersBase.class,
                                           new String[] { "StorageServerConnection.connection", "VdsId" },
                                           new Object[] { ADDRESSES[0], GUIDS[0] },
                                           true,
                                           true,
                                           GUIDS[0].toString(),
                                           false));

        setUpGetEntityExpectations(VdcQueryType.GetDeviceList,
                GetDeviceListQueryParameters.class,
                new String[] { "VdsId", "StorageType" },
                new Object[] { GUIDS[0], org.ovirt.engine.core.common.businessentities.StorageType.ISCSI },
                "this return value isn't used");

        setUpGetEntityExpectations(VdcQueryType.GetLunsByVgId,
                                   GetLunsByVgIdParameters.class,
                                   new String[] { "VgId" },
                                   new Object[] { GUIDS[GUIDS.length-1].toString() },
                                   setUpLuns());

        setUpCreationExpectations(VdcActionType.AddSANStorageDomain,
                                  AddSANStorageDomainParameters.class,
                                  new String[] { "VdsId" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetStorageDomainById,
                                  StorageDomainQueryParametersBase.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[0] },
                                  getIscsiEntity());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyIscsi((StorageDomain) response.getEntity());
    }

    @Test
    public void testAddIscsiStorageDomainAssumingConnection() throws Exception {
        StorageDomain model = getIscsi();

        Host host = new Host();
        host.setId(GUIDS[0].toString());
        model.setHost(host);
        for (LogicalUnit lun : model.getStorage().getVolumeGroup().getLogicalUnits()) {
            lun.setAddress(null);
            lun.setTarget(null);
        }
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(VdcQueryType.GetDeviceList,
                GetDeviceListQueryParameters.class,
                new String[] { "VdsId", "StorageType" },
                new Object[] { GUIDS[0], org.ovirt.engine.core.common.businessentities.StorageType.ISCSI },
                "this return value isn't used");

        List<LUNs> luns = setUpLuns();
        setUpGetEntityExpectations(VdcQueryType.GetLunsByVgId,
                                   GetLunsByVgIdParameters.class,
                                   new String[] { "VgId" },
                                   new Object[] { GUIDS[GUIDS.length-1].toString() },
                                   luns);

        setUpCreationExpectations(VdcActionType.AddSANStorageDomain,
                                  AddSANStorageDomainParameters.class,
                                  new String[] { "VdsId" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetStorageDomainById,
                                  StorageDomainQueryParametersBase.class,
                                  new String[] { "StorageDomainId" },
                                  new Object[] { GUIDS[0] },
                                  getIscsiEntity());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof StorageDomain);
        verifyIscsi((StorageDomain) response.getEntity());
    }

    @Test
    public void testAddStorageDomainNoHost() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        StorageDomain model = getModel(0);
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "StorageDomain", "add", "host.id|name");
        }
    }

    @Test
    public void testAddStorageDomainCantDo() throws Exception {
        doTestBadAddStorageDomain(false, true, CANT_DO);
    }

    @Test
    public void testAddStorageDomainFailure() throws Exception {
        doTestBadAddStorageDomain(true, false, FAILURE);
    }

    private void doTestBadAddStorageDomain(boolean canDo, boolean success, String detail)
            throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AddStorageServerConnection,
                                           StorageServerConnectionParametersBase.class,
                                           new String[] { "StorageServerConnection.connection", "StorageServerConnection.storage_type", "VdsId" },
                                           new Object[] { ADDRESSES[0] + ":" + PATHS[0], STORAGE_TYPES_MAPPED[0], GUIDS[0] },
                                           true,
                                           true,
                                           GUIDS[0].toString(),
                                           false));

        setUpGetEntityExpectations(VdcQueryType.GetExistingStorageDomainList,
                                   GetExistingStorageDomainListParameters.class,
                                   new String[] { "VdsId", "StorageType", "StorageDomainType", "Path" },
                                   new Object[] { GUIDS[0], STORAGE_TYPES_MAPPED[0], TYPES_MAPPED[0], ADDRESSES[0] + ":" + PATHS[0] },
                                   new ArrayList<storage_domain_static>());

        setUpActionExpectations(VdcActionType.AddNFSStorageDomain,
                                StorageDomainManagementParameter.class,
                                new String[] {},
                                new Object[] {},
                                canDo,
                                success);

        StorageDomain model = getModel(0);
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddStorageDomainCantDoCnxAdd() throws Exception {
        doTestBadCnxAdd(false, true, CANT_DO);
    }

    @Test
    public void testAddStorageDomainCnxAddFailure() throws Exception {
        doTestBadCnxAdd(true, false, FAILURE);
    }

    private void doTestBadCnxAdd(boolean canDo, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AddStorageServerConnection,
                                           StorageServerConnectionParametersBase.class,
                                           new String[] { "StorageServerConnection.connection", "StorageServerConnection.storage_type", "VdsId" },
                                           new Object[] { ADDRESSES[0] + ":" + PATHS[0], STORAGE_TYPES_MAPPED[0], GUIDS[0] },
                                           canDo,
                                           success,
                                           GUIDS[0].toString(),
                                           true));

        StorageDomain model = getModel(0);
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());

        try {
            collection.add(model);
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteDomainParameters() throws Exception {
        StorageDomain model = getModel(0);
        model.setName(NAMES[0]);
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());
        model.setStorage(new Storage());
        model.getStorage().setAddress(ADDRESSES[0]);
        model.getStorage().setPath(PATHS[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "StorageDomain", "add", "storage.type");
        }
    }

    @Test
    public void testAddIncompleteNfsStorageParameters() throws Exception {
        StorageDomain model = getModel(0);
        model.setName(NAMES[0]);
        model.setHost(new Host());
        model.getHost().setId(GUIDS[0].toString());
        model.setStorage(new Storage());
        model.getStorage().setType(StorageType.NFS.value());
        model.getStorage().setPath(PATHS[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Storage", "add", "address");
        }
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        if (failure == null) {
            for (int i = 0; i < NAMES.length; i++) {
                setUpGetEntityExpectations(VdcQueryType.GetStorageServerConnectionById,
                        StorageServerConnectionQueryParametersBase.class,
                        new String[] { "ServerConnectionId" },
                        new Object[] { GUIDS[i].toString() }, setUpStorageServerConnection(i));
            }
        }
        super.setUpQueryExpectations(query, failure);
    }

    static storage_server_connections setUpLocalStorageServerConnection(int index) {
        return setUpStorageServerConnection(index, index, true);
    }

    static storage_server_connections setUpStorageServerConnection(int index) {
        return setUpStorageServerConnection(index, index, false);
    }

    static storage_server_connections setUpStorageServerConnection(int idIndex, int index, boolean local) {
        storage_server_connections cnx = new storage_server_connections();
        if (idIndex != -1) {
            cnx.setid(GUIDS[idIndex].toString());
        }
        if (local) {
            cnx.setconnection(PATHS[index]);
        } else {
            cnx.setconnection(ADDRESSES[index] + ":" + PATHS[index]);
        }
        return cnx;
    }

    protected VDS setUpVDS(int index) {
        VDS vds = new VDS();
        vds.setvds_id(GUIDS[index]);
        vds.setvds_name(NAMES[index]);
        return vds;
    }

    @Override
    protected storage_domains getEntity(int index) {
        return setUpEntityExpectations(control.createMock(storage_domains.class), index);
    }

    static storage_domains setUpEntityExpectations(storage_domains entity, int index) {
        expect(entity.getid()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getstorage_name()).andReturn(NAMES[index]).anyTimes();
        // REVIST No descriptions for storage domains
        // expect(entity.getdescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getstorage_domain_type()).andReturn(TYPES_MAPPED[index]).anyTimes();
        expect(entity.getstorage_type()).andReturn(STORAGE_TYPES_MAPPED[index]).anyTimes();
        expect(entity.getstorage()).andReturn(GUIDS[index].toString()).anyTimes();
        return entity;
    }

    protected List<LUNs> setUpLuns() {
        storage_server_connections cnx = new storage_server_connections();
        cnx.setconnection(ADDRESSES[0]);
        cnx.setiqn(TARGET);
        cnx.setport(Integer.toString(PORT));

        LUNs lun = new LUNs();
        lun.setLUN_id(LUN);
        lun.setLunConnections(new ArrayList<storage_server_connections>());
        lun.getLunConnections().add(cnx);

        List<LUNs> luns = new ArrayList<LUNs>();
        luns.add(lun);
        return luns;
    }

    protected storage_domains getIscsiEntity() {
        storage_domains entity = control.createMock(storage_domains.class);
        expect(entity.getid()).andReturn(GUIDS[0]).anyTimes();
        expect(entity.getstorage_name()).andReturn(NAMES[0]).anyTimes();
        expect(entity.getstorage_domain_type()).andReturn(TYPES_MAPPED[0]).anyTimes();
        expect(entity.getstorage_type()).andReturn(org.ovirt.engine.core.common.businessentities.StorageType.ISCSI).anyTimes();
        expect(entity.getstorage()).andReturn(GUIDS[GUIDS.length-1].toString()).anyTimes();
        return entity;
    }

    static StorageDomain getModel(int index) {
        StorageDomain model = new StorageDomain();
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        model.setType(TYPES[index].value());
        model.setStorage(new Storage());
        model.getStorage().setType(STORAGE_TYPES[index].value());
        model.getStorage().setAddress(ADDRESSES[index]);
        model.getStorage().setPath(PATHS[index]);
        return model;
    }

    protected List<storage_domains> getExistingStorageDomains(boolean existing) {
        List<storage_domains> ret = new ArrayList<storage_domains>();
        if (existing) {
            ret.add(new storage_domains());
        }
        return ret;
    }

    @Override
    protected List<StorageDomain> getCollection() {
        return collection.list().getStorageDomains();
    }

    @Override
    protected void verifyModel(StorageDomain model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(StorageDomain model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        // REVIST No descriptions for storage domains
        // assertEquals(DESCRIPTIONS[index], model.getDescription());
        assertEquals(TYPES[index].value(), model.getType());
        assertNotNull(model.getStorage());
        assertEquals(STORAGE_TYPES[index].value(), model.getStorage().getType());
        if (index != LOCAL_IDX) {
            assertEquals(ADDRESSES[index], model.getStorage().getAddress());
        }
        assertEquals(PATHS[index], model.getStorage().getPath());
        assertEquals("permissions", model.getLinks().get(0).getRel());
        if (StorageDomainType.fromValue(model.getType()) == StorageDomainType.ISO) {
            assertEquals(2, model.getLinks().size());
            assertEquals("files", model.getLinks().get(1).getRel());

        } else if(model.getType().equals(TYPES[2].value())){
            assertEquals(3, model.getLinks().size());
            assertEquals("templates", model.getLinks().get(1).getRel());
            assertEquals("vms", model.getLinks().get(2).getRel());
        }
        assertNotNull(model.getLinks().get(0).getHref());
    }

    protected StorageDomain getIscsi() {
        StorageDomain model = getModel(0);
        model.getStorage().setType(StorageType.ISCSI.value());
        model.getStorage().setAddress(null);
        model.getStorage().setPath(null);
        model.getStorage().setVolumeGroup(new VolumeGroup());
        model.getStorage().getVolumeGroup().getLogicalUnits().add(new LogicalUnit());
        model.getStorage().getVolumeGroup().getLogicalUnits().get(0).setId(LUN);
        model.getStorage().getVolumeGroup().getLogicalUnits().get(0).setTarget(TARGET);
        model.getStorage().getVolumeGroup().getLogicalUnits().get(0).setAddress(ADDRESSES[0]);
        model.getStorage().getVolumeGroup().getLogicalUnits().get(0).setPort(PORT);
        return model;
    }

    protected void verifyIscsi(StorageDomain model) {
        assertEquals(GUIDS[0].toString(), model.getId());
        assertEquals(NAMES[0], model.getName());
        assertEquals(TYPES[0].value(), model.getType());
        assertNotNull(model.getStorage());
        assertEquals(StorageType.ISCSI.value(), model.getStorage().getType());
        assertNotNull(model.getStorage().getVolumeGroup());
        assertEquals(GUIDS[GUIDS.length-1].toString(), model.getStorage().getVolumeGroup().getId());
        assertTrue(model.getStorage().getVolumeGroup().isSetLogicalUnits());
        assertNotNull(model.getStorage().getVolumeGroup().getLogicalUnits().get(0));
        assertEquals(LUN, model.getStorage().getVolumeGroup().getLogicalUnits().get(0).getId());
        assertEquals(TARGET, model.getStorage().getVolumeGroup().getLogicalUnits().get(0).getTarget());
        assertEquals(ADDRESSES[0], model.getStorage().getVolumeGroup().getLogicalUnits().get(0).getAddress());
        assertEquals(PORT, model.getStorage().getVolumeGroup().getLogicalUnits().get(0).getPort());
        assertEquals(1, model.getLinks().size());
        assertEquals("permissions", model.getLinks().get(0).getRel());
        assertNotNull(model.getLinks().get(0).getHref());
        verifyLinks(model);
    }
}
