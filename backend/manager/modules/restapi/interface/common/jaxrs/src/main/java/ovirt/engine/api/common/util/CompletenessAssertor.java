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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Fault;

import static org.ovirt.engine.api.common.util.ReflectionHelper.capitalize;
import static org.ovirt.engine.api.common.util.ReflectionHelper.get;
import static org.ovirt.engine.api.common.util.ReflectionHelper.isSet;

/**
 * Used to validate that the required fields are set on a user-provided
 * model instance
 */
public class CompletenessAssertor {

    // REVISIT: i18n
    private static final String INCOMPLETE_PARAMS_REASON = "Incomplete parameters";
    private static final String INCOMPLETE_PARAMS_DETAIL = "{0} {1} required for {2}";
    private static final String ALTERNATIVE = "\\|";
    private static final String DELIMITER = "\\.";

    private static final Response.Status INCOMPLETE_PARAMS_STATUS = Response.Status.BAD_REQUEST;

    /**
     * Validate presence of required parameters.
     * Note the model type is java.lang.Object as opposed to a generic
     * <T extends BaseResource> in order to accommodate parameters types
     * such as Action.
     *
     * @param model     the incoming representation
     * @param required  the required field names
     * @throws WebApplicationException wrapping an appropriate response
     * iff a required parameter is missing
     */
    public static void validateParameters(Object model, String... required) {
        validateParameters(INCOMPLETE_PARAMS_REASON, INCOMPLETE_PARAMS_DETAIL, model, 2, required);
    }

    /**
     * Validate presence of required parameters.
     * Note the model type is java.lang.Object as opposed to a generic
     * <T extends BaseResource> in order to accommodate parameters types
     * such as Action.
     *
     * @param reason    the fault reason
     * @param detail    the fault detail
     * @param model     the incoming representation
     * @param required  the required field names
     * @throws WebApplicationException wrapping an appropriate response
     * iff a required parameter is missing
     */
    public static void validateParameters(String reason, String detail, Object model, String... required) {
        validateParameters(reason, detail, model, 2, required);
    }

    /**
     * Validate presence of required parameters.
     * Note the model type is java.lang.Object as opposed to a generic
     * <T extends BaseResource> in order to accommodate parameters types
     * such as Action.
     *
     * @param model        the incoming representation
     * @param required     the required field names
     * @param frameOffset  the stack frame offset of the public resource method
     * @throws WebApplicationException wrapping an appropriate response
     * iff a required parameter is missing
     */
    public static void validateParameters(Object model, int frameOffset, String... required) {
        Response error = assertRequired(INCOMPLETE_PARAMS_REASON, INCOMPLETE_PARAMS_DETAIL, model, frameOffset, required);
        if (error != null) {
            throw new WebApplicationException(error);
        }
    }

    /**
     * Validate presence of required parameters.
     * Note the model type is java.lang.Object as opposed to a generic
     * <T extends BaseResource> in order to accommodate parameters types
     * such as Action.
     *
     * @param reason       the fault reason
     * @param detail       the fault detail
     * @param model        the incoming representation
     * @param required     the required field names
     * @param frameOffset  the stack frame offset of the public resource method
     * @throws WebApplicationException wrapping an appropriate response
     * iff a required parameter is missing
     */
    public static void validateParameters(String reason, String detail, Object model, int frameOffset, String... required) {
        Response error = assertRequired(reason, detail, model, frameOffset, required);
        if (error != null) {
            throw new WebApplicationException(error);
        }
    }

    /**
     * Validate presence of required parameters.
     *
     * @param reason         the fault reason
     * @param detail         the fault detail
     * @param model          the incoming representation
     * @param frameOffset    the stack frame offset of the public resource method
     * @param missingMembers the stack frame offset of the public resource method
     * @param required       the required field names
     * @return               error Response if appropriate
     */
    private static Response assertRequired(String reason, String detail, Object model, int frameOffset, String... required) {
        List<String> missing = doAssertRequired(reason, detail, model, frameOffset, required);
        Response response = null;
        if (!missing.isEmpty()) {
            StackTraceElement[] trace = new Throwable().getStackTrace();
            Fault fault = new Fault();
            fault.setReason(reason);
            fault.setDetail(MessageFormat.format(detail,
                                                 model.getClass().getSimpleName(),
                                                 missing,
                                                 trace[frameOffset + 1].getMethodName()));
            response = Response.status(INCOMPLETE_PARAMS_STATUS)
                               .entity(fault)
                               .build();
        }

        return response;
    }
    /**
     * Validate presence of required parameters.
     *
     * @param reason         the fault reason
     * @param detail         the fault detail
     * @param model          the incoming representation
     * @param frameOffset    the stack frame offset of the public resource method
     * @param required       the required field names
     * @return               error Response if appropriate
     */
    private static List<String> doAssertRequired(String reason, String detail, Object model, int frameOffset, String... required) {
        List<String> missing = new ArrayList<String>();

        for (String r : required) {
            if (topLevel(r)) {
                if (!assertFields(model, subField(r))) {
                    missing.add(r);
                }
            } else if (isList(model, superField(r))) {
                for (Object item : asList(model, superField(r))) {
                    if (!assertFields(item, subField(r))) {
                        missing.add(r);
                    }
                }
            } else if(!isLeaf(r)){
                Object superType = get(model, superField(r));
                if(superType != null)
                    missing.addAll(joinSuperType(doAssertRequired(reason, detail, superType ,frameOffset, subField(r)), superType));
                else
                    missing.add(r);
            }
            else {
                boolean found = false;
                for (String superField : superField(r).split(ALTERNATIVE)) {
                    found = found || (isSet(model, capitalize(superField)) && assertFields(model, superField, subField(r)));
                }
                if (!found) {
                    missing.add(r);
                }
            }
        }

        return missing;
    }

    /**
     * add SuperType to missing arguments
     *
     * @param missing     the missing arguments
     * @param superType   the super type to join
     * @return            collection of missing parameters
     */
    private static Collection<? extends String> joinSuperType(List<String> missing, Object superType) {
        String superTypeName = superType.getClass().getSimpleName().toLowerCase();
        for(int i = 0; i < missing.size(); i++){
            missing.set(i, superTypeName + "." + missing.get(i));
        }
        return missing;
    }

    private static boolean assertFields(Object model, String fields) {
        return assertFields(model, null, fields);
    }

    private static boolean assertFields(Object model, String superField, String subFields) {
        String[] splitFields = subFields.split(ALTERNATIVE);
        boolean found = false;
        for (String subField : splitFields) {
            found = found || isSet(superField != null ? get(model, superField) : model, capitalize(subField));
        }
        return found;
    }

    private static boolean topLevel(String required) {
        return required.indexOf(".") == -1;
    }

    /**
     * Checks if this type is leaf in arguments
     *
     * @param required the type to check
     * @return         boolean
     */
    private static boolean isLeaf(String required) {
        String[] res = required.split(DELIMITER);
        return res == null || res.length <= 2;
    }

    private static String superField(String required) {
        return capitalize(required.substring(0, required.indexOf(".")));
    }

    private static String subField(String required) {
        return required.substring(required.indexOf(".") + 1);
    }

    @SuppressWarnings("unchecked")
    private static boolean isList(Object model, String superField) {
        return model !=null
               && isSet(model, superField)
               && isSet(get(model, superField), superField)
               && get(get(model, superField), superField) instanceof List;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object model, String superField) {
        return (List<Object>)get(get(model, superField), superField);
    }
}
