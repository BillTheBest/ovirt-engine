package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.core.common.action.MoveTagParameters;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.queries.GetTagByTagIdParameters;
import org.ovirt.engine.core.common.queries.GetTagByTagNameParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.PARENT_GUID;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.PARENT_IDX;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.setUpTags;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.verifyParent;

public class BackendTagResourceTest
    extends AbstractBackendSubResourceTest<Tag, tags, BackendTagResource> {

    public BackendTagResourceTest() {
        super(new BackendTagResource(GUIDS[0].toString(), new BackendTagsResource()));
    }

    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendTagResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(0, true);
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
        setUpGetEntityExpectations(0);
        setUriInfo(setUpBasicUriExpectations());

        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(0, true);
        control.replay();
        try {
            resource.update(getModel(0, false));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(0);
        setUpGetEntityExpectations(0);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateTag,
                                           TagsOperationParameters.class,
                                           new String[] { "Tag.tag_name", "Tag.parent_id" },
                                           new Object[] { NAMES[0], PARENT_GUID },
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0, false)), 0);
    }

    @Test
    public void testMove() throws Exception {
        doTestMove(getModel(0), 0);
    }

    @Test
    public void testMoveNamedParent() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetTagByTagName,
                                     GetTagByTagNameParameters.class,
                                     new String[] { "TagName" },
                                     new Object[] { NAMES[PARENT_IDX] },
                                     getEntity(PARENT_IDX));

        Tag model = getModel(0);
        model.getParent().getTag().setId(null);
        model.getParent().getTag().setName(NAMES[PARENT_IDX]);

        doTestMove(model, 0);
    }

    protected void doTestMove(Tag model, int index) throws Exception {
        setUpActionExpectations(VdcActionType.MoveTag,
                                MoveTagParameters.class,
                                new String[] { "TagId", "NewParentId" },
                                new Object[] { GUIDS[index], PARENT_GUID },
                                true,
                                true,
                                null,
                                null,
                                false);

        setUpGetEntityExpectations(index);
        setUpGetEntityExpectations(index);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateTag,
                                           TagsOperationParameters.class,
                                           new String[] { "Tag.tag_name", "Tag.parent_id" },
                                           new Object[] { NAMES[index], PARENT_GUID },
                                           true,
                                           true));

        verifyModel(resource.update(model), index);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(0);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateTag,
                                           TagsOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           canDo,
                                           success));

        try {
            resource.update(getModel(0, false));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUpGetEntityExpectations(0);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();

        Tag model = getModel(1, false);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    protected void setUpGetEntityExpectations(int index) throws Exception {
        setUpGetEntityExpectations(index, false);
    }

    protected void setUpGetEntityExpectations(int index, boolean notFound) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetTagByTagId,
                                   GetTagByTagIdParameters.class,
                                   new String[] { "TagId" },
                                   new Object[] { GUIDS[index] },
                                   notFound ? null : getEntity(index));
    }

    @Override
    protected tags getEntity(int index) {
        return setUpTags().get(index);
    }

    @Override
    protected void verifyModel(Tag model, int index) {
        super.verifyModel(model, index);
        verifyParent(model, PARENT_GUID.toString());
    }
}
