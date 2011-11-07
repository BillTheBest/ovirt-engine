package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.resource.ActionResource;

public class BackendActionResource
        extends AbstractBackendAsyncStatusResource<Action>
        implements ActionResource {

    private String action;

    public BackendActionResource(String action, String ids) {
        super(Action.class, ids);
        this.action = action;
    }

    public Response get() {
        return Response.ok(query()).build();
    }

    @Override
    protected Action populate(Action model, List entity) {
        model.setId(asString(ids));
        if (model.isSetFault()) {
            setReason(model.getFault());
        }
        return model;
    }

    @Override
    public Action getAction() {
        // REVISIT
        return null;
    }
}
