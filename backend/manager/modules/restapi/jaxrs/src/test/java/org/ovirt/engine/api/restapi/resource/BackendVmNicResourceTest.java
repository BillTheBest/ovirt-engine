package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.AbstractBackendNicsResourceTest.PARENT_ID;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendNicsResourceTest.setUpEntityExpectations;
import static org.ovirt.engine.api.restapi.resource.AbstractBackendNicsResourceTest.verifyModelSpecific;
import static org.easymock.EasyMock.expect;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import org.ovirt.engine.api.model.MAC;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.resource.NicResource;

import org.ovirt.engine.api.restapi.resource.BaseBackendResource.WebFaultException;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicResourceTest
        extends AbstractBackendSubResourceTest<NIC, VmNetworkInterface, BackendDeviceResource<NIC, Nics, VmNetworkInterface>> {

    protected static final Guid NIC_ID = GUIDS[1];

    protected static BackendVmNicsResource collection;

    public BackendVmNicResourceTest() {
        super((BackendVmNicResource)getCollection().getDeviceSubResource(NIC_ID.toString()));
    }

    protected static BackendVmNicsResource getCollection() {
        return new BackendVmNicsResource(PARENT_ID);
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getCollection());
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVmInterfacesByVmId,
                                     GetVmByVmIdParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_ID },
                                     new ArrayList<VmNetworkInterface>());
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
        setUpEntityQueryExpectations(1);
        setGetVmQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        control.replay();

        NIC nic = resource.get();
        verifyModelSpecific(nic, 1);
        verifyLinks(nic);
    }

    @Test
    public void testGetIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            setUriInfo(setUpBasicUriExpectations());
            setUpEntityQueryExpectations(1);
            setGetVmQueryExpectations(1);
            setGetNetworksQueryExpectations(1);
            control.replay();

            NIC nic = resource.get();
            assertTrue(nic.isSetStatistics());
            verifyModelSpecific(nic, 1);
            verifyLinks(nic);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVmInterfacesByVmId,
                                     GetVmByVmIdParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_ID },
                                     new ArrayList<VmNetworkInterface>());
        control.replay();
        try {
            resource.update(getUpdate(false));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2);
        setGetVmQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmInterface,
                                           AddVmInterfaceParameters.class,
                                           new String[] { "VmId", "Interface.Id" },
                                           new Object[] { PARENT_ID, GUIDS[1] },
                                           true,
                                           true));

        NIC nic = resource.update(getUpdate(false));
        assertNotNull(nic);
    }

    @Test(expected=WebFaultException.class)
    public void testUpdateWithNonExistingNetwork() throws Exception {
        setGetVmQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        control.replay();
        NIC nic = resource.update(getUpdate(true));
        assertNotNull(nic);
    }

    @Test
    public void testUpdateWithExistingNetwork() throws Exception {
        setUpGetEntityExpectations(2);
        setGetVmQueryExpectations(2);
        setGetNetworksQueryExpectations(2);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVmInterface,
                                           AddVmInterfaceParameters.class,
                                           new String[] { "VmId", "Interface.Id" },
                                           new Object[] { PARENT_ID, GUIDS[1] },
                                           true,
                                           true));

        NIC nic = resource.update(getUpdate(true));
        assertNotNull(nic);
    }

    @Test
    public void testStatisticalQuery() throws Exception {
        VmNetworkInterface entity = setUpStatisticalExpectations();

        @SuppressWarnings("unchecked")
        BackendStatisticsResource<NIC, VmNetworkInterface> statisticsResource =
            (BackendStatisticsResource<NIC, VmNetworkInterface>)((NicResource) resource).getStatisticsResource();
        assertNotNull(statisticsResource);

        verifyQuery(statisticsResource.getQuery(), entity);
    }

    protected void setGetVmQueryExpectations(int times) throws Exception {
            while (times-- > 0) {
                VM vm = new VM();
                vm.setvds_group_id(GUIDS[0]);
                setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                                             GetVmByVmIdParameters.class,
                                             new String[] { "Id" },
                                             new Object[] { PARENT_ID },
                                             vm);
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

    protected VmNetworkInterface setUpStatisticalExpectations() throws Exception {
        VmNetworkStatistics stats = control.createMock(VmNetworkStatistics.class);
        VmNetworkInterface entity = control.createMock(VmNetworkInterface.class);
        expect(entity.getStatistics()).andReturn(stats);
        expect(entity.getId()).andReturn(NIC_ID).anyTimes();
        expect(stats.getReceiveRate()).andReturn(10D);
        expect(stats.getTransmitRate()).andReturn(20D);
        expect(stats.getReceiveDropRate()).andReturn(30D);
        expect(stats.getTransmitDropRate()).andReturn(40D);
        List<VmNetworkInterface> ifaces = new ArrayList<VmNetworkInterface>();
        ifaces.add(entity);
        setUpEntityQueryExpectations(VdcQueryType.GetVmInterfacesByVmId,
                                     GetVmByVmIdParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { PARENT_ID },
                                     ifaces);
        control.replay();
        return entity;
    }

    protected void verifyQuery(AbstractStatisticalQuery<NIC, VmNetworkInterface> query, VmNetworkInterface entity) throws Exception {
        assertEquals(NIC.class, query.getParentType());
        assertSame(entity, query.resolve(NIC_ID));
        List<Statistic> statistics = query.getStatistics(entity);
        verifyStatistics(statistics,
                         new String[] {"data.current.rx", "data.current.tx", "errors.total.rx", "errors.total.tx"},
                         new BigDecimal[] {asDec(10), asDec(20), asDec(30), asDec(40)});
        Statistic adopted = query.adopt(new Statistic());
        assertTrue(adopted.isSetNic());
        assertEquals(NIC_ID.toString(), adopted.getNic().getId());
        assertTrue(adopted.getNic().isSetVm());
        assertEquals(PARENT_ID.toString(), adopted.getNic().getVm().getId());
    }

    protected NIC getUpdate(boolean withNetwork) {
        NIC update = new NIC();
        update.setMac(new MAC());
        update.getMac().setAddress("00:1a:4a:16:85:18");
        if (withNetwork) {
            Network network = new Network();
            network.setId(GUIDS[0].toString());
            update.setNetwork(network);
        }
        return update;
    }

    @Override
    protected VmNetworkInterface getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VmNetworkInterface.class),
                                       control.createMock(VmNetworkStatistics.class),
                                       index);
    }

    protected List<VmNetworkInterface> getEntityList() {
        List<VmNetworkInterface> entities = new ArrayList<VmNetworkInterface>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;

    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetVmInterfacesByVmId,
                                         GetVmByVmIdParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { PARENT_ID },
                                         getEntityList());
        }
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, getEntity(1));
    }

    protected void setUpGetEntityExpectations(int times, VmNetworkInterface entity) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVmInterfacesByVmId,
                                       GetVmByVmIdParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { PARENT_ID },
                                       entity);
        }
    }

}
