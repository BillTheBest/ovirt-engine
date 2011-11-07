package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NicStatus;
import org.ovirt.engine.api.model.Slaves;
import org.ovirt.engine.api.resource.HostNicResource;
import org.ovirt.engine.core.common.action.AddBondParameters;
import org.ovirt.engine.core.common.action.RemoveBondParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.GetAllNetworkQueryParamenters;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNicsResourceTest
    extends AbstractBackendCollectionResourceTest<HostNIC, VdsNetworkInterface, BackendHostNicsResource> {

    public static final Guid PARENT_GUID = GUIDS[0];
    public static final Guid NETWORK_GUID = new Guid("33333333-3333-3333-3333-333333333333");
    public static final String NETWORK_NAME = "skynet";
    private static final Guid MASTER_GUID = new Guid("99999999-9999-9999-9999-999999999999");
    private static final String MASTER_NAME = "master";
    private static final Guid SLAVE_GUID = new Guid("66666666-6666-6666-6666-666666666666");
    private static final String SLAVE_NAME = "slave";
    private static final int SINGLE_NIC_IDX = GUIDS.length - 2;
    private static final Integer NIC_SPEED = 100;
    private static final InterfaceStatus NIC_STATUS = InterfaceStatus.Up;
    private static final NetworkBootProtocol BOOT_PROTOCOL = NetworkBootProtocol.StaticIp;

    public BackendHostNicsResourceTest() {
        super(new BackendHostNicsResource(PARENT_GUID.toString()), null, null);
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Test
    public void testGet() throws Exception {
        HostNicResource subresource = collection.getHostNicSubResource(GUIDS[SINGLE_NIC_IDX].toString());

        setGetVdsQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUriInfo(setUpBasicUriExpectations());
        setUpQueryExpectations("");

        verifyModel(subresource.get(), SINGLE_NIC_IDX);
    }

    @Test
    public void testListIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            setUriInfo(setUpUriExpectations(null));
            setGetVdsQueryExpectations(1);
            setGetNetworksQueryExpectations(1);
            setUpQueryExpectations("");

            List<HostNIC> nics = getCollection();
            assertTrue(nics.get(0).isSetStatistics());
            verifyCollection(nics);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testAddBond() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetAllNetworks,
                                     GetAllNetworkQueryParamenters.class,
                                     new String[] { "StoragePoolId" },
                                     new Object[] { Guid.Empty },
                                     asList(getNetwork()));

        setUpEntityQueryExpectations(1);

        setUpCreationExpectations(VdcActionType.AddBond,
                                  AddBondParameters.class,
                                  new String[] { "VdsId", "BondName" },
                                  new Object[] { PARENT_GUID, MASTER_NAME },
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetVdsInterfacesByVdsId,
                                  GetVdsByVdsIdParameters.class,
                                  new String[] { "VdsId" },
                                  new Object[] { PARENT_GUID },
                                  setUpInterfaces());

        HostNIC nic = getBondEntity();

        Response response = collection.add(nic);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof HostNIC);
        verifyMaster((HostNIC) response.getEntity());
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
        setUpEntityQueryExpectations(VdcQueryType.GetAllNetworks,
                                     GetAllNetworkQueryParamenters.class,
                                     new String[] { "StoragePoolId" },
                                     new Object[] { Guid.Empty },
                                     asList(getNetwork()));

        setUriInfo(setUpActionExpectations(VdcActionType.AddBond,
                                           AddBondParameters.class,
                                           new String[] { "VdsId", "BondName" },
                                           new Object[] { PARENT_GUID, MASTER_NAME },
                                           canDo,
                                           success));

        HostNIC model = getBondEntity();

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }


    @Test
    public void testAddIncompleteParameters() throws Exception {
        HostNIC nic = new HostNIC();
        nic.setName(MASTER_NAME);
        nic.setNetwork(new Network());
        nic.getNetwork().setDescription(DESCRIPTIONS[0]);
        nic.setBonding(new Bonding());
        nic.getBonding().setSlaves(new Slaves());
        nic.getBonding().getSlaves().getSlaves().add(new HostNIC());
        nic.getBonding().getSlaves().getSlaves().get(0).setId(SLAVE_GUID.toString());
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(nic);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "HostNIC", "add", "network.id|name");
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVdsInterfacesByVdsId,
                                     GetVdsByVdsIdParameters.class,
                                     new String[] { "VdsId" },
                                     new Object[] { PARENT_GUID },
                                     new ArrayList<VdsNetworkInterface>());
        control.replay();
        try {
            collection.remove(MASTER_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        setGetVdsQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUpEntityQueryExpectations(2);

        setUriInfo(setUpActionExpectations(VdcActionType.RemoveBond,
                                           RemoveBondParameters.class,
                                           new String[] { "VdsId", "BondName" },
                                           new Object[] { PARENT_GUID, MASTER_NAME },
                                           true,
                                           true));
        collection.remove(MASTER_GUID.toString());
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpEntityQueryExpectations(VdcQueryType.GetVdsInterfacesByVdsId,
                GetVdsByVdsIdParameters.class,
                new String[] { "VdsId" },
                new Object[] { PARENT_GUID },
                new LinkedList<VdsNetworkInterface>());
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
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

    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setGetVdsQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setGetVdsQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUpEntityQueryExpectations(2);

        setUriInfo(setUpActionExpectations(VdcActionType.RemoveBond,
                                           RemoveBondParameters.class,
                                           new String[] { "VdsId", "BondName" },
                                           new Object[] { PARENT_GUID, MASTER_NAME },
                                           canDo,
                                           success));
        try {
            collection.remove(MASTER_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        setUpEntityQueryExpectations(times, null);
    }

    protected void setUpEntityQueryExpectations(int times, Object failure) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetVdsInterfacesByVdsId,
                                         GetVdsByVdsIdParameters.class,
                                         new String[] { "VdsId" },
                                         new Object[] { PARENT_GUID },
                                         setUpInterfaces());
        }
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assert(query.equals(""));

        setUpEntityQueryExpectations(VdcQueryType.GetVdsInterfacesByVdsId,
                                     GetVdsByVdsIdParameters.class,
                                     new String[] { "VdsId" },
                                     new Object[] { PARENT_GUID },
                                     setUpInterfaces(),
                                     failure);

        control.replay();
    }

    public static List<VdsNetworkInterface> setUpInterfaces() {
        List<VdsNetworkInterface> ifaces = new ArrayList<VdsNetworkInterface>();
        for (int i = 0; i < NAMES.length; i++) {
            ifaces.add(getEntitySpecific(i));
        }
        ifaces.add(getMaster());
        ifaces.add(getSlave());
        return ifaces;
    }

    @Override
    protected VdsNetworkInterface getEntity(int index) {
        return getEntitySpecific(index);
    }

    public static VdsNetworkInterface getEntitySpecific(int index) {
        VdsNetworkInterface entity = new VdsNetworkInterface();
        entity.setId(GUIDS[index]);
        entity.setName(NAMES[index]);
        entity.setNetworkName(NETWORK_NAME);
        entity.setSpeed(NIC_SPEED);
        entity = setUpStatistics(entity, GUIDS[index]);
        entity.getStatistics().setStatus(NIC_STATUS);
        entity.setBootProtocol(BOOT_PROTOCOL);
        return entity;
    }

    public static VdsNetworkInterface getMaster() {
        VdsNetworkInterface entity = new VdsNetworkInterface();
        entity.setId(MASTER_GUID);
        entity.setName(MASTER_NAME);
        entity.setNetworkName(NETWORK_NAME);
        entity.setBonded(true);
        entity.setBootProtocol(BOOT_PROTOCOL);
        return setUpStatistics(entity, MASTER_GUID);
    }

    public static VdsNetworkInterface getSlave() {
        VdsNetworkInterface entity = new VdsNetworkInterface();
        entity.setId(SLAVE_GUID);
        entity.setName(SLAVE_NAME);
        entity.setNetworkName(NETWORK_NAME);
        entity.setBondName(MASTER_NAME);
        entity.setBootProtocol(BOOT_PROTOCOL);
        return setUpStatistics(entity, SLAVE_GUID);
    }

    public static VdsNetworkInterface setUpStatistics(VdsNetworkInterface entity, Guid id) {
        VdsNetworkStatistics statistics = new VdsNetworkStatistics();

        statistics.setId(null);
        statistics.setReceiveDropRate(1D);
        statistics.setReceiveRate(2D);
        statistics.setTransmitDropRate(3D);
        statistics.setTransmitRate(4D);
        statistics.setVdsId(id);
        statistics.setStatus(null);
        entity.setStatistics(statistics);
        return entity;
    }

    public static network getNetwork() {
        network entity = new network();
        entity.setId(NETWORK_GUID);
        entity.setname(NETWORK_NAME);
        return entity;
    }

    protected HostNIC getBondEntity() {
        HostNIC nic = new HostNIC();
        nic.setName(MASTER_NAME);
        nic.setNetwork(new Network());
        nic.getNetwork().setName(NETWORK_NAME);
        nic.setBonding(new Bonding());
        nic.getBonding().setSlaves(new Slaves());
        nic.getBonding().getSlaves().getSlaves().add(new HostNIC());
        nic.getBonding().getSlaves().getSlaves().get(0).setName(SLAVE_NAME);
        return nic;
    }

    @Override
    protected void verifyModel(HostNIC model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    public void verifyModelSpecific(HostNIC model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertNotNull(model.getNetwork());
        assertEquals(NETWORK_NAME, model.getNetwork().getName());
        assertEquals(calcSpeed(NIC_SPEED), model.getSpeed());
        assertNotNull(model.getStatus());
        assertEquals(map(NIC_STATUS,null).value(), model.getStatus().getState());
        assertEquals(map(BOOT_PROTOCOL,null).value(), model.getBootProtocol());
    }

    private Long calcSpeed(Integer nicSpeed) {
        return nicSpeed == 0 ?
                             null
                             :
                             nicSpeed * 1000L * 1000 ;
    }

    protected NicStatus map(InterfaceStatus interfaceStatus, NicStatus params) {
        return getMapper(InterfaceStatus.class, NicStatus.class).map(interfaceStatus, params);
    }

    protected BootProtocol map(NetworkBootProtocol networkBootProtocol, BootProtocol params) {
        return getMapper(NetworkBootProtocol.class, BootProtocol.class).map(networkBootProtocol, params);
    }

    protected void verifyMaster(HostNIC model) {
        assertEquals(MASTER_GUID.toString(), model.getId());
        assertEquals(MASTER_NAME, model.getName());
        assertNotNull(model.getNetwork());
        assertEquals(NETWORK_NAME, model.getNetwork().getName());
        assertNotNull(model.getBonding());
        assertNotNull(model.getBonding().getSlaves());
        assertEquals(1, model.getBonding().getSlaves().getSlaves().size());
        assertEquals(SLAVE_GUID.toString(), model.getBonding().getSlaves().getSlaves().get(0).getId());
        assertNotNull(model.getBonding().getSlaves().getSlaves().get(0).getHref());
    }

    protected void verifySlave(HostNIC model) {
        assertEquals(SLAVE_GUID.toString(), model.getId());
        assertEquals(SLAVE_NAME, model.getName());
        assertNotNull(model.getNetwork());
        assertEquals(NETWORK_NAME, model.getNetwork().getName());
        assertEquals(2, model.getLinks().size());
        assertTrue("master".equals(model.getLinks().get(0).getRel()) ||
                   "master".equals(model.getLinks().get(1).getRel()));
        assertNotNull(model.getLinks().get(0).getHref());
    }

    @Override
    protected void verifyCollection(List<HostNIC> collection) throws Exception {
        assertNotNull(collection);
        assertEquals(NAMES.length + 2, collection.size());
        for (int i = 0; i < NAMES.length; i++) {
            verifyModel(collection.get(i), i);
        }
        verifyMaster(collection.get(NAMES.length));
        verifySlave(collection.get(NAMES.length + 1));
    }

    @Override
    protected List<HostNIC> getCollection() {
        return collection.list().getHostNics();
    }

    protected void setGetVdsQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            VDS vds = new VDS();
            vds.setvds_group_id(GUIDS[0]);
            setUpEntityQueryExpectations(VdcQueryType.GetVdsByVdsId,
                    GetVdsByVdsIdParameters.class,
                    new String[] { "VdsId" },
                    new Object[] { PARENT_GUID },
                    vds);
        }
    }

    protected void setGetNetworksQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            ArrayList<network> networks = new ArrayList<network>();
            network network = new network();
            network.setId(GUIDS[0]);
            network.setname("orcus");
            networks.add(network);
            setUpEntityQueryExpectations(VdcQueryType.GetAllNetworksByClusterId,
                    VdsGroupQueryParamenters.class,
                    new String[] { "VdsGroupId" },
                    new Object[] { GUIDS[0] },
                    networks);
        }
    }
}
