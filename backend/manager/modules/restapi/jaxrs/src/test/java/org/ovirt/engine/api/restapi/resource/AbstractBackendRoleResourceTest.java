package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByRoleIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;


public abstract class AbstractBackendRoleResourceTest
        extends AbstractBackendSubResourceTest<Role, roles, BackendRoleResource> {

    public AbstractBackendRoleResourceTest(BackendRoleResource roleResource) {
        super(roleResource);
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendRoleResource("foo", null);
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
        setUpGetEntityExpectations(VdcQueryType.GetRoleById,
                                   MultilevelAdministrationByRoleIdParameters.class,
                                   new String[] { "RoleId" },
                                   new Object[] { GUIDS[0] },
                                   notFound ? null : getEntity(0));
    }

    @Override
    protected roles getEntity(int index) {
        roles role = new roles();
        role.setId(GUIDS[index]);
        role.setname(NAMES[index]);
        role.setdescription(DESCRIPTIONS[index]);
        role.setis_readonly(false);
        role.setType(RoleType.ADMIN);
        return role;
    }

    @Override
    protected void verifyModel(Role model, int index) {
        super.verifyModel(model, index);
        assertTrue(model.isMutable());
        assertTrue(model.isAdministrative());
    }

}

