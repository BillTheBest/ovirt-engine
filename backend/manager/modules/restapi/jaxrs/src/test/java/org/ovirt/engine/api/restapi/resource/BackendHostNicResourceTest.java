package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendHostNicsResourceTest.NETWORK_GUID;
import static org.ovirt.engine.api.restapi.resource.BackendHostNicsResourceTest.NETWORK_NAME;
import static org.ovirt.engine.api.restapi.resource.BackendHostNicsResourceTest.PARENT_GUID;
import static org.ovirt.engine.api.restapi.resource.BackendHostNicsResourceTest.getEntitySpecific;
import static org.ovirt.engine.api.restapi.resource.BackendHostNicsResourceTest.getNetwork;
import static org.ovirt.engine.api.restapi.resource.BackendHostNicsResourceTest.setUpInterfaces;
import static org.easymock.EasyMock.expect;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsParameters;
import org.ovirt.engine.core.common.action.UpdateNetworkToVdsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.GetAllChildVlanInterfacesQueryParameters;
import org.ovirt.engine.core.common.queries.GetAllNetworkQueryParamenters;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNicResourceTest
        extends AbstractBackendSubResourceTest<HostNIC, VdsNetworkInterface, BackendHostNicResource> {

    private static final int NIC_IDX = 1;
    private static final Guid NIC_ID = GUIDS[NIC_IDX];

    private static final String[] IPS = new String[]{"10.35.1.1", "10.35.1.2", "10.35.1.3", "10.35.1.4"};
    private static final String[] GATEWAYS = new String[]{"10.35.1.254", "10.35.1.126", "10.35.1.254", "10.35.1.126"};
    private static final String[] MASKS = new String[]{"255.255.255.0", "255.255.255.128", "255.255.0.0", "255.0.0.0"};

    private BackendHostNicsResourceTest hostNicsResource;

    public BackendHostNicResourceTest() {
        super(new BackendHostNicResource(NIC_ID.toString(),
                                         new BackendHostNicsResource(PARENT_GUID.toString())));
        hostNicsResource = new BackendHostNicsResourceTest();
        hostNicsResource.setUp();
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Override
    protected VdsNetworkInterface getEntity(int index) {
        return getEntitySpecific(index);
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendHostNicResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVdsInterfacesByVdsId,
                                     GetVdsByVdsIdParameters.class,
                                     new String[] { "VdsId" },
                                     new Object[] { PARENT_GUID },
                                     new ArrayList<VdsNetworkInterface>());
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setGetVdsQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUpEntityQueryExpectations();
        control.replay();

        hostNicsResource.verifyModelSpecific(resource.get(), NIC_IDX);
    }

    @Test
    public void testGetIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            setUriInfo(setUpBasicUriExpectations());
            setGetVdsQueryExpectations(1);
            setGetNetworksQueryExpectations(1);
            setUpEntityQueryExpectations();
            control.replay();

            HostNIC nic = resource.get();
            assertTrue(nic.isSetStatistics());
            hostNicsResource.verifyModelSpecific(nic, NIC_IDX);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testAttach() throws Exception {
        testAction(VdcActionType.AttachNetworkToVdsInterface,
                   NETWORK_GUID.toString(),
                   null);
    }

    @Test
    public void testAttachByName() throws Exception {
        testAction(VdcActionType.AttachNetworkToVdsInterface,
                   null,
                   NETWORK_NAME);
    }

    @Test
    public void testAttachNotFound() throws Exception {
        testActionNotFound(VdcActionType.AttachNetworkToVdsInterface);
    }

    @Test
    public void testIncompleteAttach() throws Exception {
        Action action = new Action();
        action.setNetwork(new Network());
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            resource.attach(action);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "attach", "network.id|name");
        }
    }

    @Test
    public void testDetach() throws Exception {
        testAction(VdcActionType.DetachNetworkFromVdsInterface,
                   NETWORK_GUID.toString(),
                   null);
    }

    @Test
    public void testDetachByName() throws Exception {
        testAction(VdcActionType.DetachNetworkFromVdsInterface,
                   null,
                   NETWORK_NAME);
    }

    @Test
    public void testDetachNotFound() throws Exception {
        testActionNotFound(VdcActionType.DetachNetworkFromVdsInterface);
    }

    @Test
    public void testIncompleteDetach() throws Exception {
        Action action = new Action();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            resource.detach(action);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "detach", "network.id|name");
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setGetVdsQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUpGetEntityExpectations(2);
        setupGetHostExpectations(2);
        setupGetNetworkExpectations(2);
        setupUpdateExpectations();
        verifyUpdate(resource.update(getHostNicModel(1)), 1);
    }

    @Test
    public void testUpdateNoNetworkName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setGetVdsQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        VdsNetworkInterface model1 = getModel(1);
        model1.setNetworkName(null);
        model1.setVdsId(GUIDS[0]);
        VdsNetworkInterface model2 = getModel(2);
        model2.setNetworkName(null);
        model1.setVdsId(GUIDS[0]);
        setUpGetEntityExpectations(VdcQueryType.GetVdsInterfacesByVdsId,
                GetVdsByVdsIdParameters.class,
                new String[] { "VdsId" },
                new Object[] { PARENT_GUID },
                asList(model1));
        setUpGetEntityExpectations(VdcQueryType.GetVdsInterfacesByVdsId,
                GetVdsByVdsIdParameters.class,
                new String[] { "VdsId" },
                new Object[] { PARENT_GUID },
                asList(model2));
        HostNIC hostNicModel = getHostNicModel(1);
        hostNicModel.setNetwork(null);
        setUpVlanQueryExpectations(model1);
        setupGetHostExpectations(1);
        setupGetNetworkExpectations(1);
        setupUpdateExpectations();
        HostNIC result = resource.update(hostNicModel);
        assertNotNull(result);
        assertNotNull(result.getIp());

        assertEquals(result.getIp().getAddress(), IPS[2]);
        assertEquals(result.getIp().getNetmask(), MASKS[2]);
        assertEquals(result.getIp().getGateway(), GATEWAYS[2]);
    }

    private void setUpVlanQueryExpectations(VdsNetworkInterface hostNicModel) {
        List<VdsNetworkInterface> vlans = new LinkedList<VdsNetworkInterface>();
        VdsNetworkInterface vlan = new VdsNetworkInterface();
        vlan.setNetworkName("some network name");
        vlans.add(vlan);
        setUpEntityQueryExpectations(VdcQueryType.GetAllChildVlanInterfaces,
                                     GetAllChildVlanInterfacesQueryParameters.class,
                                     new String[]{"VdsId", "Interface"},
                                     new Object[]{PARENT_GUID, hostNicModel},
                                     vlans);
    }

    private void setupGetHostExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVdsByVdsId,
                    GetVdsByVdsIdParameters.class,
                    new String[] { "VdsId" },
                    new Object[] { PARENT_GUID },
                    getHostModel());
        }
    }

    private void setupGetNetworkExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetAllNetworksByClusterId,
                    VdsGroupQueryParamenters.class,
                    new String[] { "VdsGroupId" },
                    new Object[] { GUIDS[0] },
                    getNetworksModel());
        }
    }

    private List<network> getNetworksModel() {
        List<network> networks = new ArrayList<network>();
        for(int i=0; i < 3; i++){
            network net = new network();
            net.setId(GUIDS[i]);
            net.setname(NAMES[i]);
            networks.add(net);
        }
        return networks;
    }

    private VDS getHostModel() {
        VDS vds = new VDS();
        vds.setvds_id(PARENT_GUID);
        vds.setvds_group_id(GUIDS[0]);
        return vds;
    }

    private HostNIC getHostNicModel(int i) {
        HostNIC nic = new HostNIC();
        nic.setId(GUIDS[1].toString());
        nic.setNetwork(new Network());
        nic.getNetwork().setId(GUIDS[i].toString());
        nic.getNetwork().setName(NAMES[i]);
        nic.getNetwork().setCluster(new Cluster());
        nic.getNetwork().getCluster().setId(GUIDS[0].toString());
        return nic;
    }

    private void setupUpdateExpectations() throws Exception {
        setUpActionExpectations(VdcActionType.UpdateNetworkToVdsInterface,
                UpdateNetworkToVdsParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                null,
                true);

    }

    private void verifyUpdate(HostNIC nic, Integer i) {
        assertNotNull(nic);
        assertNotNull(nic.getNetwork());
        assertNotNull(nic.getIp());
        assertNull(nic.getNetwork().getName());
        assertEquals(nic.getNetwork().getId(), GUIDS[i].toString());
        assertEquals(nic.getIp().getAddress(), IPS[i]);
        assertEquals(nic.getIp().getNetmask(), MASKS[i]);
        assertEquals(nic.getIp().getGateway(), GATEWAYS[i]);
    }

    private void setUpGetEntityExpectations(Integer times) throws Exception {
        int i=0;
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVdsInterfacesByVdsId,
                    GetVdsByVdsIdParameters.class,
                    new String[] { "VdsId" },
                    new Object[] { PARENT_GUID },
                    asList(getModel(i)));
            i += 1;
        }
    }

    private VdsNetworkInterface getModel(int i) {
        VdsNetworkInterface nic = new VdsNetworkInterface();
        nic.setId(GUIDS[1]);
        nic.setNetworkName(NAMES[i]);
        nic.setAddress(IPS[i]);
        nic.setSubnet(MASKS[i]);
        nic.setGateway(GATEWAYS[i]);
        return nic;
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    private void doTestBadUpdate(boolean canDo, boolean success, String detail) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        setupGetHostExpectations(2);
        setupGetNetworkExpectations(2);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateNetworkToVdsInterface,
                                           UpdateNetworkToVdsParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           canDo,
                                           success));

        try {
            resource.update(getHostNicModel(1));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }


    protected void testAction(VdcActionType actionType, String networkId, String networkName) throws Exception {
        Action action = new Action();
        action.setNetwork(new Network());
        if (networkId != null) {
            action.getNetwork().setId(networkId);
        } else {
            action.getNetwork().setName(networkName);
        }

        setUpEntityQueryExpectations(VdcQueryType.GetAllNetworks,
                                     GetAllNetworkQueryParamenters.class,
                                     new String[] { "StoragePoolId" },
                                     new Object[] { Guid.Empty },
                                     asList(getNetwork()));

        setUpEntityQueryExpectations();

        setUriInfo(setUpActionExpectations(actionType,
                                           AttachNetworkToVdsParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { PARENT_GUID },
                                           true,
                                           true,
                                           null,
                                           null,
                                           true));

        if (actionType == VdcActionType.AttachNetworkToVdsInterface) {
            verifyActionResponse(resource.attach(action));
        } else {
            verifyActionResponse(resource.detach(action));
        }
    }

    protected void testActionNotFound(VdcActionType actionType) throws Exception {
        Action action = new Action();
        action.setNetwork(new Network());
        action.getNetwork().setId(NETWORK_GUID.toString());
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVdsInterfacesByVdsId,
                                     GetVdsByVdsIdParameters.class,
                                     new String[] { "VdsId" },
                                     new Object[] { PARENT_GUID },
                                     new ArrayList<VdsNetworkInterface>());
        control.replay();
        try {
            if (actionType == VdcActionType.AttachNetworkToVdsInterface) {
                resource.attach(action);
            } else {
                resource.detach(action);
            }
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testStatisticalQuery() throws Exception {
        VdsNetworkInterface entity = setUpStatisticalExpectations();

        @SuppressWarnings("unchecked")
        BackendStatisticsResource<HostNIC, VdsNetworkInterface> statisticsResource =
            (BackendStatisticsResource<HostNIC, VdsNetworkInterface>)resource.getStatisticsResource();
        assertNotNull(statisticsResource);

        verifyQuery(statisticsResource.getQuery(), entity);
    }

    protected VdsNetworkInterface setUpStatisticalExpectations() throws Exception {
        VdsNetworkStatistics stats = control.createMock(VdsNetworkStatistics.class);
        VdsNetworkInterface entity = control.createMock(VdsNetworkInterface.class);
        expect(entity.getStatistics()).andReturn(stats);
        expect(entity.getId()).andReturn(NIC_ID).anyTimes();
        expect(stats.getReceiveRate()).andReturn(10D);
        expect(stats.getTransmitRate()).andReturn(20D);
        expect(stats.getReceiveDropRate()).andReturn(30D);
        expect(stats.getTransmitDropRate()).andReturn(40D);
        List<VdsNetworkInterface> ifaces = new ArrayList<VdsNetworkInterface>();
        ifaces.add(entity);
        setUpEntityQueryExpectations(VdcQueryType.GetVdsInterfacesByVdsId,
                                     GetVdsByVdsIdParameters.class,
                                     new String[] { "VdsId" },
                                     new Object[] { PARENT_GUID },
                                     ifaces);
        control.replay();
        return entity;
    }

    protected void verifyQuery(AbstractStatisticalQuery<HostNIC, VdsNetworkInterface> query, VdsNetworkInterface entity) throws Exception {
        assertEquals(HostNIC.class, query.getParentType());
        assertSame(entity, query.resolve(NIC_ID));
        List<Statistic> statistics = query.getStatistics(entity);
        verifyStatistics(statistics,
                         new String[] {"data.current.rx", "data.current.tx", "errors.total.rx", "errors.total.tx"},
                         new BigDecimal[] {asDec(10), asDec(20), asDec(30), asDec(40)});
        Statistic adopted = query.adopt(new Statistic());
        assertTrue(adopted.isSetHostNic());
        assertEquals(NIC_ID.toString(), adopted.getHostNic().getId());
        assertTrue(adopted.getHostNic().isSetHost());
        assertEquals(GUIDS[0].toString(), adopted.getHostNic().getHost().getId());
    }

    protected void setUpEntityQueryExpectations() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetVdsInterfacesByVdsId,
                                     GetVdsByVdsIdParameters.class,
                                     new String[] { "VdsId" },
                                     new Object[] { PARENT_GUID },
                                     setUpInterfaces());
    }

    protected void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r,
                             "hosts/" + PARENT_GUID.toString() + "/nics/" + NIC_ID.toString(),
                             false);
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
