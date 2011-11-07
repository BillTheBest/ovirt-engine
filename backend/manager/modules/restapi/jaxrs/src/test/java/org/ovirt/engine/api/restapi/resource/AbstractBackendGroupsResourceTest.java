package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.ArrayList;
import javax.ws.rs.WebApplicationException;

import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.core.common.businessentities.ad_groups;

import static org.easymock.classextension.EasyMock.expect;

public abstract class AbstractBackendGroupsResourceTest
        extends AbstractBackendCollectionResourceTest<Group, ad_groups, AbstractBackendGroupsResource> {

    public AbstractBackendGroupsResourceTest(AbstractBackendGroupsResource collection) {
        super(collection, null, "");
    }

    @Test
    @Ignore
    public void testQuery() throws Exception {
        // skip test inherited from base class
    }

    @Test
    public void testRemoveBadGuid() throws Exception {
        control.replay();
        try {
            collection.remove("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    protected void setUpQueryExpectations(String query) throws Exception {
        setUpEntityQueryExpectations(1);
        control.replay();
    }

    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(1, failure);
        control.replay();
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        setUpEntityQueryExpectations(times, null);
    }

    protected abstract void setUpEntityQueryExpectations(int times, Object failure) throws Exception;

    protected List<ad_groups> getEntityList() {
        List<ad_groups> entities = new ArrayList<ad_groups>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    protected ad_groups getEntity(int index) {
        return setUpEntityExpectations(control.createMock(ad_groups.class), index);
    }

    static ad_groups setUpEntityExpectations(ad_groups entity, int index) {
        expect(entity.getid()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getname()).andReturn(NAMES[index]).anyTimes();
        return entity;
    }

    protected List<Group> getCollection() {
        return collection.list().getGroups();
    }

    static Group getModel(int index) {
        Group model = new Group();
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    protected void verifyModel(Group model, int index) {
        super.verifyModel(model, index);
    }
}
