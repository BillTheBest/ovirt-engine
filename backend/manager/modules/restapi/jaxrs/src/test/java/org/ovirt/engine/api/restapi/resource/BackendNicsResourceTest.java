package org.ovirt.engine.api.restapi.resource;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.NicResource;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendNicsResourceTest
        extends AbstractBackendNicsResourceTest<BackendNicsResource> {

    public BackendNicsResourceTest() {
        super(new BackendVmNicsResource(PARENT_ID),
              VdcQueryType.GetVmInterfacesByVmId,
              new GetVmByVmIdParameters(PARENT_ID),
              "Id");
    }

    @Test
    public void testListIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            setUriInfo(setUpUriExpectations(null));
            setGetVmQueryExpectations(1);
            setGetNetworksQueryExpectations(1);
            setUpQueryExpectations("");

            List<NIC> nics = getCollection();
            assertTrue(nics.get(0).isSetStatistics());
            verifyCollection(nics);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpEntityQueryExpectations(1);
        setGetVmQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmInterface,
                                           RemoveVmInterfaceParameters.class,
                                           new String[] { "VmId", "InterfaceId" },
                                           new Object[] { PARENT_ID, GUIDS[0] },
                                           true,
                                           true));
        collection.remove(GUIDS[0].toString());
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpEntityQueryExpectations(VdcQueryType.GetVmInterfacesByVmId,
                GetVmByVmIdParameters.class,
                new String[] { "Id" },
                new Object[] { PARENT_ID },
                new LinkedList<VmNetworkInterface>());
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

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(1);
        setGetVmQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVmInterface,
                                           RemoveVmInterfaceParameters.class,
                                           new String[] { "VmId", "InterfaceId" },
                                           new Object[] { PARENT_ID, GUIDS[0] },
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
    public void testAddNic() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setGetVmQueryExpectations(2);
        setGetNetworksQueryExpectations(2);
        setUpCreationExpectations(VdcActionType.AddVmInterface,
                                  AddVmInterfaceParameters.class,
                                  new String[] { "VmId" },
                                  new Object[] { PARENT_ID },
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetVmInterfacesByVmId,
                                  GetVmByVmIdParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { PARENT_ID },
                                  asList(getEntity(0)));
        NIC model = getModel(0);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof NIC);
        verifyModel((NIC) response.getEntity(), 0);
    }

    @Test
    public void testAddNicCantDo() throws Exception {
        doTestBadAddNic(false, true, CANT_DO);
    }

    @Test
    public void testAddNicFailure() throws Exception {
        doTestBadAddNic(true, false, FAILURE);
    }

    private void doTestBadAddNic(boolean canDo, boolean success, String detail) throws Exception {
        setGetVmQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.AddVmInterface,
                                           AddVmInterfaceParameters.class,
                                           new String[] { "VmId" },
                                           new Object[] { PARENT_ID },
                                           canDo,
                                           success));
        NIC model = getModel(0);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        NIC model = new NIC();
        model.setName(NAMES[0]);
        model.setNetwork(new Network());

        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "NIC", "add", "network.name|id");
        }
    }

    @Test
    public void testSubResourceLocator() throws Exception {
        control.replay();
        assertTrue(collection.getDeviceSubResource(GUIDS[0].toString()) instanceof NicResource);
    }

    @Test
    public void testSubResourceLocatorBadGuid() throws Exception {
        control.replay();
        try {
            collection.getDeviceSubResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setGetVmQueryExpectations(1);
        setGetNetworksQueryExpectations(1);
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
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
}
