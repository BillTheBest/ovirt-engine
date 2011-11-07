package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.GetVdsGroupByVdsGroupIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;

import static org.easymock.classextension.EasyMock.expect;

public class BackendClusterNetworksResourceTest extends AbstractBackendNetworksResourceTest {

    static final Guid CLUSTER_ID = GUIDS[1];

    public BackendClusterNetworksResourceTest() {
        super(new BackendClusterNetworksResource(CLUSTER_ID.toString()));
    }

    @Test
    public void testRemoveNotFound() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetAllNetworksByClusterId,
                                     VdsGroupQueryParamenters.class,
                                     new String[] { "VdsGroupId" },
                                     new Object[] { CLUSTER_ID },
                                     new ArrayList<network>());
        control.replay();
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        VDSGroup vdsGroup = setUpVDSGroupExpectations(CLUSTER_ID);

        setUpEntityQueryExpectations(2);

        setUriInfo(setUpActionExpectations(VdcActionType.DetachNetworkToVdsGroup,
                                           AttachNetworkToVdsGroupParameter.class,
                                           new String[] { "VdsGroup" },
                                           new Object[] { vdsGroup },
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
        VDSGroup vdsGroup = setUpVDSGroupExpectations(CLUSTER_ID);

        setUpEntityQueryExpectations(2);

        setUriInfo(setUpActionExpectations(VdcActionType.DetachNetworkToVdsGroup,
                                           AttachNetworkToVdsGroupParameter.class,
                                           new String[] { "VdsGroup" },
                                           new Object[] { vdsGroup },
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
    public void testAddNetwork() throws Exception {
        VDSGroup vdsGroup = setUpVDSGroupExpectations(CLUSTER_ID);

        setUriInfo(setUpBasicUriExpectations());

        setUpCreationExpectations(VdcActionType.AttachNetworkToVdsGroup,
                                  AttachNetworkToVdsGroupParameter.class,
                                  new String[] { "VdsGroup" },
                                  new Object[] { vdsGroup },
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetAllNetworksByClusterId,
                                  VdsGroupQueryParamenters.class,
                                  new String[] { "VdsGroupId" },
                                  new Object[] { CLUSTER_ID },
                                  asList(getEntity(0)));
        Network model = getModel(0);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Network);
        verifyModel((Network) response.getEntity(), 0);
    }

    @Test
    public void testAddNetworkCantDo() throws Exception {
        doTestBadAddNetwork(false, true, CANT_DO);
    }

    @Test
    public void testAddNetworkFailure() throws Exception {
        doTestBadAddNetwork(true, false, FAILURE);
    }

    private void doTestBadAddNetwork(boolean canDo, boolean success, String detail) throws Exception {
        VDSGroup vdsGroup = setUpVDSGroupExpectations(CLUSTER_ID);

        setUriInfo(setUpActionExpectations(VdcActionType.AttachNetworkToVdsGroup,
                                           AttachNetworkToVdsGroupParameter.class,
                                           new String[] { "VdsGroup" },
                                           new Object[] { vdsGroup },
                                           canDo,
                                           success));
        Network model = getModel(0);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters_noId() throws Exception {
        Network model = new Network();
        model.setName("name");
        model.setDescription(DESCRIPTIONS[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Network", "add", "id");
        }
    }

    @Test
    public void testAddIncompleteParameters_noName() throws Exception {
        Network model = new Network();
        model.setId(GUIDS[0].toString());
        model.setDescription(DESCRIPTIONS[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Network", "add", "name");
        }
    }

    protected void setUpEntityQueryExpectations(int times, Object failure) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetAllNetworksByClusterId,
                                         VdsGroupQueryParamenters.class,
                                         new String[] { "VdsGroupId" },
                                         new Object[] { CLUSTER_ID },
                                         getEntityList(),
                                         failure);
        }
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
}
