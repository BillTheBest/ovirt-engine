/*
* Copyright © 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Templates;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VMs;

@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
public interface StorageDomainResource extends UpdatableResource<StorageDomain> {

    @Path("permissions")
    public AssignedPermissionsResource getPermissionsResource();

    @Path("vms")
    public StorageDomainContentsResource<VMs, VM> getStorageDomainVmsResource();

    @Path("templates")
    public StorageDomainContentsResource<Templates, Template> getStorageDomainTemplatesResource();

    @Path("files")
    public FilesResource getFilesResource();
}
