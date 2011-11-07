package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.PermitType;

import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByRoleIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.api.restapi.types.PermitMapper;

public class BackendPermitsResourceTest extends AbstractBackendCollectionResourceTest<Permit, ActionGroup, BackendPermitsResource> {

    public BackendPermitsResourceTest() {
        super(new BackendPermitsResource(GUIDS[1]), null, "");
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Test
    public void testRemoveBadId() throws Exception {
        doTestRemoveNotFound("foo");
    }

    @Test
    public void testRemoveNotFound() throws Exception {
        doTestRemoveNotFound("11111");
    }

    private void doTestRemoveNotFound(String id) throws Exception {
        control.replay();
        try {
            collection.remove(id);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        List<ActionGroup> actionGroups = new ArrayList<ActionGroup>();
        actionGroups.add(ActionGroup.forValue(1));
        setUriInfo(setUpActionExpectations(VdcActionType.DetachActionGroupsFromRole,
                                           ActionGroupsToRoleParameter.class,
                                           new String[] { "RoleId", "ActionGroups" },
                                           new Object[] { GUIDS[1], actionGroups  },
                                           true,
                                           true));
        collection.remove("1");
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
        List<ActionGroup> actionGroups = new ArrayList<ActionGroup>();
        actionGroups.add(ActionGroup.forValue(1));
        setUriInfo(setUpActionExpectations(VdcActionType.DetachActionGroupsFromRole,
                                           ActionGroupsToRoleParameter.class,
                                           new String[] { "RoleId", "ActionGroups" },
                                           new Object[] { GUIDS[1], actionGroups  },
                                           canDo,
                                           success));
        try {
            collection.remove("1");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }


    @Test
    public void testAddPermit() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AttachActionGroupsToRole,
                                  ActionGroupsToRoleParameter.class,
                                  new String[] { "RoleId" },
                                  new Object[] { GUIDS[1] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  null,
                                  null,
                                  null,
                                  null,
                                  getEntity(1));

        Permit model = new Permit();
        model.setId("1");

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Permit);
        verifyModel((Permit)response.getEntity(), 0);
    }

    @Override
    protected List<Permit> getCollection() {
        return collection.list().getPermits();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assert(query.equals(""));

        setUpEntityQueryExpectations(VdcQueryType.GetRoleActionGroupsByRoleId,
                                     MultilevelAdministrationByRoleIdParameters.class,
                                     new String[] { "RoleId" },
                                     new Object[] { GUIDS[1] },
                                     setUpActionGroups(),
                                     failure);

        control.replay();
    }

    static List<ActionGroup> setUpActionGroups() {
        List<ActionGroup> actionGroups = new ArrayList<ActionGroup>();
        for (int i = 1; i <= NAMES.length; i++) {
            actionGroups.add(ActionGroup.forValue(i));
        }
        return actionGroups;
    }

    @Override
    protected ActionGroup getEntity(int index) {
        return ActionGroup.forValue(index);
    }

    static Permit getModel(int index) {
        Permit model = new Permit();
        model.setId(Integer.toString(index));
        model.setName(ActionGroup.forValue(index).toString());
        return model;
    }

    protected void verifyModel(Permit model, int index) {
        assertEquals(Integer.toString(index + 1), model.getId());
        PermitType permitType = PermitMapper.map(ActionGroup.forValue(index + 1), (PermitType)null);
        assertEquals(permitType.value(), model.getName());
    }

}
