package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.queries.GetDbUserByUserIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import static org.ovirt.engine.api.restapi.resource.BackendUsersResourceTest.GROUPS;
import static org.ovirt.engine.api.restapi.resource.BackendUsersResourceTest.PARSED_GROUPS;

public class BackendUserResourceTest
        extends AbstractBackendSubResourceTest<User, DbUser, BackendUserResource> {

    public BackendUserResourceTest() {
        super(new BackendUserResource(GUIDS[0].toString(), new BackendUsersResource()));
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendUserResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(true);
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
        setUpGetEntityExpectations();
        control.replay();

        verifyModel(resource.get(), 0);
    }

    protected void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(false);
    }

    protected void setUpGetEntityExpectations(boolean notFound) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetDbUserByUserId,
                                   GetDbUserByUserIdParameters.class,
                                   new String[] { "UserId" },
                                   new Object[] { GUIDS[0] },
                                   notFound ? null : getEntity(0));
    }

    @Override
    protected DbUser getEntity(int index) {
        DbUser entity = new DbUser();
        entity.setuser_id(GUIDS[index]);
        entity.setname(NAMES[index]);
        entity.setgroups(GROUPS);
        entity.setdomain(DOMAIN);

        return entity;
    }

    protected void verifyModel(User model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertNotNull(model.getDomain());
        assertEquals(DOMAIN, model.getDomain().getName());
        assertTrue(model.isSetGroups());
        assertEquals(PARSED_GROUPS.length, model.getGroups().getGroups().size());
        for (int i = 0 ; i < PARSED_GROUPS.length ; i++) {
            Group group = model.getGroups().getGroups().get(i);
            assertEquals(PARSED_GROUPS[i], group.getName());
        }
        verifyLinks(model);
    }
}
