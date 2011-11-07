package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import org.ovirt.engine.core.common.action.DisplayNetworkToVdsGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.GetVdsGroupByVdsGroupIdParameters;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import org.ovirt.engine.api.model.Network;

import static org.ovirt.engine.api.restapi.resource.BackendClusterNetworksResourceTest.CLUSTER_ID;
import static org.easymock.EasyMock.expect;

public class BackendClusterNetworkResourceTest
    extends AbstractBackendNetworkResourceTest<BackendClusterNetworkResource> {

    public BackendClusterNetworkResourceTest() {
        super(new BackendClusterNetworkResource(CLUSTER_ID.toString(),
              new BackendClusterNetworksResource(CLUSTER_ID.toString())));
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendClusterNetworkResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetAllNetworksByClusterId,
                                     VdsGroupQueryParamenters.class,
                                     new String[] { "VdsGroupId" },
                                     new Object[] { CLUSTER_ID },
                                     new ArrayList<network>());
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
        setUpEntityQueryExpectations(1, false);
        control.replay();

        verifyModel(resource.get(), 1);
    }

    @Test
    public void testUpdate() throws Exception {
        setUpEntityQueryExpectations(1, false);
        setUpEntityQueryExpectations(1, true);
        setUpVDSGroupExpectations(GUIDS[1]);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateDisplayToVdsGroup,
                                           DisplayNetworkToVdsGroupParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        verifyUpdate(resource.update(getModel(0)), 0);
    }

    protected VDSGroup setUpVDSGroupExpectations(Guid id) {
        VDSGroup group = control.createMock(VDSGroup.class);
        expect(group.getID()).andReturn(id).anyTimes();

        setUpEntityQueryExpectations(VdcQueryType.GetVdsGroupByVdsGroupId,
                                     GetVdsGroupByVdsGroupIdParameters.class,
                                     new String[] { "VdsGroupId" },
                                     new Object[] { id },
                                     group);
        return group;
    }
    private Network getModel(int i) {
        Network network = new Network();
        network.setId(GUIDS[i].toString());
        network.setName(NAMES[i]);
        network.setDisplay(true);
        return network;
    }
    protected void verifyUpdate(Network model, int index) {
        assertTrue(model.isSetDisplay());
        assertEquals(model.isDisplay(), true);
   }


    protected void setUpEntityQueryExpectations(int times, boolean isDisplay) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetAllNetworksByClusterId,
                                         VdsGroupQueryParamenters.class,
                                         new String[] { "VdsGroupId" },
                                         new Object[] { CLUSTER_ID },
                                         getEntityList(isDisplay));
        }
    }
}

