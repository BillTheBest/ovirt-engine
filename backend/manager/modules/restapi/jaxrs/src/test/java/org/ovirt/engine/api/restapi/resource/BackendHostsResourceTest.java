package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostStatus;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import static org.easymock.EasyMock.expect;

public class BackendHostsResourceTest
        extends AbstractBackendCollectionResourceTest<Host, VDS, BackendHostsResource> {

    protected static final String[] ADDRESSES = { "10.11.12.13", "13.12.11.10", "10.01.10.01" };
    protected static final VDSStatus[] VDS_STATUS = { VDSStatus.Up, VDSStatus.Down, VDSStatus.Up };
    protected static final HostStatus[] HOST_STATUS = { HostStatus.UP, HostStatus.DOWN,
            HostStatus.UP };
    protected static final String ROOT_PASSWORD = "s3CR3t";

    public BackendHostsResourceTest() {
        super(new BackendHostsResource(), SearchType.VDS, "Hosts : ");
    }

    @Test
    public void testListIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            UriInfo uriInfo = setUpUriExpectations(null);

            setUpQueryExpectations("");
            collection.setUriInfo(uriInfo);
            List<Host> hosts = getCollection();
            assertTrue(hosts.get(0).isSetStatistics());
            verifyCollection(hosts);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVds,
                                           VdsActionParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        collection.remove(GUIDS[0].toString());
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(VdcQueryType.GetVdsByVdsId,
                GetVdsByVdsIdParameters.class,
                new String[] { "VdsId" },
                new Object[] { NON_EXISTANT_GUID },
                null);
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    private void setUpGetEntityExpectations() throws Exception {
        VDS vds = new VDS();
        vds.setvds_id(GUIDS[0]);
        setUpGetEntityExpectations(VdcQueryType.GetVdsByVdsId,
                GetVdsByVdsIdParameters.class,
                new String[] { "VdsId" },
                new Object[] { GUIDS[0] },
                vds);
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        setUpGetEntityExpectations();
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        setUpGetEntityExpectations();
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVds,
                                           VdsActionParameters.class,
                                           new String[] { "VdsId" },
                                           new Object[] { GUIDS[0] },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddHost() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations("Cluster: name=Default",
                                   SearchType.Cluster,
                                   setUpVDSGroup(GUIDS[1]));

        setUpCreationExpectations(VdcActionType.AddVds,
                                  AddVdsActionParameters.class,
                                  new String[] { "RootPassword" },
                                  new Object[] { ROOT_PASSWORD },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetVdsByVdsId,
                                  GetVdsByVdsIdParameters.class,
                                  new String[] { "VdsId" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        Host model = getModel(0);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Host);
        verifyModel((Host) response.getEntity(), 0);
    }

    @Test
    public void testAddHostClusterByName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations("Cluster: name=" + NAMES[1],
                                   SearchType.Cluster,
                                   setUpVDSGroup(GUIDS[1]));

        setUpCreationExpectations(VdcActionType.AddVds,
                                  AddVdsActionParameters.class,
                                  new String[] { "RootPassword" },
                                  new Object[] { ROOT_PASSWORD },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetVdsByVdsId,
                                  GetVdsByVdsIdParameters.class,
                                  new String[] { "VdsId" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        Host model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setName(NAMES[1]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Host);
        verifyModel((Host) response.getEntity(), 0);
    }

    @Test
    public void testAddHostClusterById() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddVds,
                                  AddVdsActionParameters.class,
                                  new String[] { "RootPassword" },
                                  new Object[] { ROOT_PASSWORD },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetVdsByVdsId,
                                  GetVdsByVdsIdParameters.class,
                                  new String[] { "VdsId" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        Host model = getModel(0);
        model.setCluster(new Cluster());
        model.getCluster().setId(GUIDS[1].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Host);
        verifyModel((Host) response.getEntity(), 0);
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Host model = new Host();
        model.setName(NAMES[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Host", "add", "address", "rootPassword");
        }
    }

    @Test
    public void testAddHostCantDo() throws Exception {
        doTestBadAddHost(false, true, CANT_DO);
    }

    @Test
    public void testAddHostFailure() throws Exception {
        doTestBadAddHost(true, false, FAILURE);
    }

    private void doTestBadAddHost(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations("Cluster: name=Default",
                                   SearchType.Cluster,
                                   setUpVDSGroup(GUIDS[1]));

        setUriInfo(setUpActionExpectations(VdcActionType.AddVds,
                                           AddVdsActionParameters.class,
                                           new String[] { "RootPassword" },
                                           new Object[] { ROOT_PASSWORD },
                                           canDo,
                                           success));
        Host model = getModel(0);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    protected VDS getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VDS.class), control.createMock(VdsStatistics.class), index);
    }

    static VDS setUpEntityExpectations(VDS entity, int index) {
        return setUpEntityExpectations(entity, null, index);
    }

    static VDS setUpEntityExpectations(VDS entity, VdsStatistics statistics, int index) {
        expect(entity.getvds_id()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getvds_name()).andReturn(NAMES[index]).anyTimes();
        expect(entity.gethost_name()).andReturn(ADDRESSES[index]).anyTimes();
        expect(entity.getstatus()).andReturn(VDS_STATUS[index]).anyTimes();
        if (statistics != null) {
            setUpStatisticalEntityExpectations(entity, statistics);
        }
        return entity;
    }

    static VDS setUpStatisticalEntityExpectations(VDS entity, VdsStatistics statistics) {
        expect(entity.getphysical_mem_mb()).andReturn(5120).anyTimes();
        expect(entity.getStatisticsData()).andReturn(statistics).anyTimes();
        expect(statistics.getusage_mem_percent()).andReturn(20).anyTimes();
        expect(statistics.getswap_free()).andReturn(25L).anyTimes();
        expect(statistics.getswap_total()).andReturn(30L).anyTimes();
        expect(statistics.getmem_available()).andReturn(35L).anyTimes();
        expect(statistics.getksm_cpu_percent()).andReturn(40).anyTimes();
        expect(statistics.getcpu_user()).andReturn(45.0).anyTimes();
        expect(statistics.getcpu_sys()).andReturn(50.0).anyTimes();
        expect(statistics.getcpu_idle()).andReturn(55.0).anyTimes();
        expect(statistics.getcpu_load()).andReturn(0.60).anyTimes();
        return entity;
    }

    protected List<Host> getCollection() {
        return collection.list().getHosts();
    }

    static Host getModel(int index) {
        Host model = new Host();
        model.setName(NAMES[index]);
        model.setAddress(ADDRESSES[index]);
        model.setRootPassword(ROOT_PASSWORD);
        return model;
    }

    protected void verifyModel(Host model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(Host model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertEquals(ADDRESSES[index], model.getAddress());
        assertEquals(HOST_STATUS[index].value(), model.getStatus().getState());
    }
}
