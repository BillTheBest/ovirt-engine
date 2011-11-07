package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByAdElementIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendUserAssignedPermissionsResourceTest
        extends AbstractBackendAssignedPermissionsResourceTest {

    public BackendUserAssignedPermissionsResourceTest() {
        super(GUIDS[1],
              VdcQueryType.GetPermissionsByAdElementId,
              new MultilevelAdministrationByAdElementIdParameters(GUIDS[1]),
              User.class,
              "VdcUser.UserId",
              "AdElementId");
    }

    @Test
    public void testAddIncompletePermission() throws Exception {
        Permission model = new Permission();
        model.setUser(new User());
        model.getUser().setId(GUIDS[1].toString());
        model.setRole(new Role());
        model.getRole().setId(GUIDS[3].toString());

        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae,
                                       "Permission",
                                       "add",
                                       "dataCenter|cluster|host|storageDomain|vm|vmpool|template.id");
        }
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpGetEntityExpectations(VdcQueryType.Search,
                                   SearchParameters.class,
                                   new String[] {"SearchPattern", "SearchTypeValue"},
                                   new Object[] {"users:", SearchType.DBUser},
                                   getUsers());
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    protected Permission getModel() {
        Permission model = new Permission();
        model.setDataCenter(new DataCenter());
        model.getDataCenter().setId(GUIDS[2].toString());
        model.setRole(new Role());
        model.getRole().setId(GUIDS[3].toString());
        return model;
    }

    @Override
    protected List<permissions> setUpPermissions() {
        List<permissions> perms = super.setUpPermissions();
        perms.add(getEntity(perms.size() - 1));
        perms.get(perms.size() - 1).setObjectType(VdcObjectType.System);
        return perms;
    }

    @Override
    protected void verifyModel(Permission model, int index) {
        super.verifyModel(model, index);
        assertTrue(model.isSetUser());
        assertEquals(GUIDS[1].toString(), model.getUser().getId());
    }
}

