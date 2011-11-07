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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import org.ovirt.engine.api.model.BaseDevice;
import org.ovirt.engine.api.model.BaseDevices;


@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
public interface ReadOnlyDevicesResource<D extends BaseDevice, C extends BaseDevices> {

    @GET
    @Formatted
    public C list();

    /**
     * Sub-resource locator method, returns individual DeviceResource on which the
     * remainder of the URI is dispatched.
     *
     * @param id  the Device ID
     * @return    matching subresource if found
     */
    @Path("{id}")
    public ReadOnlyDeviceResource<D> getDeviceSubResource(@PathParam("id") String id);
}
