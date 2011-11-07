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

package org.ovirt.engine.api.resteasy.json;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;

public class CustomObjectMapper extends ObjectMapper
{
    public static CustomObjectMapper get() {
        CustomObjectMapper mapper = new CustomObjectMapper();
        mapper.setSerializer(new CustomBeanFactory()).includeDefaults(false).indent(true);
        return mapper;
    }

    protected CustomObjectMapper setSerializer(CustomSerializerFactory ser) {
        setSerializerFactory(ser);
        getSerializationConfig().setSerializationView(String.class);
        return this;
    }

    protected CustomObjectMapper includeDefaults(boolean include) {
        getSerializationConfig().setSerializationInclusion(
            include ? JsonSerialize.Inclusion.ALWAYS : JsonSerialize.Inclusion.NON_DEFAULT);
        return this;
    }

    protected CustomObjectMapper indent(boolean indent) {
        configure(SerializationConfig.Feature.INDENT_OUTPUT, indent);
        return this;
    }
}
