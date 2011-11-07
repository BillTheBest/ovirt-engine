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

package org.ovirt.engine.api.common.util;

import java.text.MessageFormat;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Fault;

import static org.ovirt.engine.api.common.util.ReflectionHelper.capitalize;
import static org.ovirt.engine.api.common.util.ReflectionHelper.different;
import static org.ovirt.engine.api.common.util.ReflectionHelper.isSet;

/**
 * Used to assert that fields set on a model type do not conflict with
 * mutability constraints
 */
public class MutabilityAssertor {

    // REVISIT: i18n
    private static final String BROKEN_CONSTRAINT_REASON = "Broken immutability constraint";
    private static final String BROKEN_CONSTRAINT_DETAIL = "Attempt to set immutable field: {0}";

    // REVISIT: is "409 Conflicted" actually the appropriate status here?
    // The idea is to convey a conflict with the fundamental immutable state
    // of the resource, but it also carries connotations of a dirty update
    private static final Response.Status BROKEN_CONSTRAINT_STATUS = Response.Status.CONFLICT;

    /**
     * Validate update from an immutability point of view.
     *
     * @param <T>       representation type
     * @param strict    array of strictly immutable field names
     * @param incoming  the incoming representation
     * @param existing  the existing representation
     * @throws WebApplicationException wrapping an appropriate response
     * iff an immutability constraint has been broken
     */
    public static <T extends BaseResource> void validateUpdate(String[] strict, T incoming, T existing) {
        Response error = imposeConstraints(strict, incoming, existing);
        if (error != null) {
            throw new WebApplicationException(error);
        }
    }

    /**
     * Impose immutability constraints.
     *
     * @param <T>       representation type
     * @param strict    array of strictly immutable field names
     * @param incoming  incoming representation
     * @param existing  existing representation
     * @return          error Response if appropriate
     */
    public static <T extends BaseResource> Response imposeConstraints(String[] strict, T incoming, T existing) {
        return imposeConstraints(strict, incoming, existing, BROKEN_CONSTRAINT_REASON, BROKEN_CONSTRAINT_DETAIL);
    }

    /**
     * Impose immutability constraints.
     *
     * @param <T>       representation type
     * @param strict    array of strictly immutable field names
     * @param incoming  incoming representation
     * @param existing  existing representation
     * @param reason    the fault reason
     * @param detail    the fault detail
     * @return          error Response if appropriate
     */
    public static <T extends BaseResource> Response imposeConstraints(String[] strict, T incoming, T existing, String reason, String detail) {
        for (String s: strict) {
            String field = capitalize(s);
            if (isSet(incoming, field) && different(incoming, existing, field)) {
                Fault fault = new Fault();
                fault.setReason(reason);
                fault.setDetail(MessageFormat.format(detail, s));
                return Response.status(BROKEN_CONSTRAINT_STATUS)
                               .entity(fault)
                               .build();
            }
        }
        return null;
    }
}
