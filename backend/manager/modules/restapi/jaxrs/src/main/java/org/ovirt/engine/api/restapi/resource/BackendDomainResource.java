package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.common.util.ReflectionHelper.assignChildModel;
import static org.ovirt.engine.api.restapi.resource.BackendDomainsResource.SUB_COLLECTIONS;

import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.resource.DomainGroupsResource;
import org.ovirt.engine.api.resource.DomainUsersResource;
import org.ovirt.engine.api.resource.DomainResource;
import org.ovirt.engine.api.restapi.model.Directory;

public class BackendDomainResource extends AbstractBackendSubResource<Domain, Directory>
implements DomainResource {
    private String id;
    private BackendDomainsResource parent;

    public BackendDomainResource(String id, BackendDomainsResource parent) {
        super(id, Domain.class, Directory.class, SUB_COLLECTIONS);
        this.id = id;
        this.parent = parent;
    }

    @Override
    public Domain get() {
        Domain domain = parent.lookupDirectoryById(id,true);
        return injectSearchLinks(addLinks(domain),SUB_COLLECTIONS);
    }

    public Domain getDirectory() {
        return parent.lookupDirectoryById(id,false);
    }

    @Override
    public DomainGroupsResource getDomainGroupsResource() {
        return inject(new BackendDomainGroupsResource(id, this));
    }

    @Override
    public DomainUsersResource getDomainUsersResource() {
        return inject(new BackendDomainUsersResource(id, this));
    }

    @Override
    protected Domain addParents(Domain domain) {
        if(parent!=null){
            assignChildModel(domain, Domain.class).setId(id);
        }
        return domain;
    }
}
