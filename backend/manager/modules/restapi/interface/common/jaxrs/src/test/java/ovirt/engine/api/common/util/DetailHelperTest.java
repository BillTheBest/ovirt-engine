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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.junit.Assert;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;


public class DetailHelperTest extends Assert {

    private static final String ACCEPTABLE = "application/xml";

    @Test
    public void testIncludeSingle() throws Exception {
        doTestIncludes(";detail=statistics",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, false});
    }

    @Test
    public void testIncludeSome() throws Exception {
        doTestIncludes(";detail=devices ;detail=statistics",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, false});
    }

    @Test
    public void testIncludeSomeCollapsed() throws Exception {
        doTestIncludes(";detail=devices+statistics",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, false});
    }

    @Test
    public void testIncludeMore() throws Exception {
        doTestIncludes(";detail=devices; detail=statistics; detail=tags; detail=permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeMoreCollapsed() throws Exception {
        doTestIncludes(";detail=devices; detail=statistics+tags+permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeAll() throws Exception {
        doTestIncludes(";detail=statistics; detail=permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeAllCollapsed() throws Exception {
        doTestIncludes(";detail=statistics+permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeWithSpacePrefix() throws Exception {
        doTestIncludes("; detail=statistics ; detail=permissions",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {true, true});
    }

    @Test
    public void testIncludeNone() throws Exception {
        doTestIncludes("",
                       new String[] {"statistics", "permissions"},
                       new boolean[] {false, false});
    }

    private void doTestIncludes(String details, String[] rels, boolean[] expected) throws Exception {

        HttpHeaders httpheaders = createMock(HttpHeaders.class);
        List<String> requestHeaders = new ArrayList<String>();
        expect(httpheaders.getRequestHeader("Accept")).andReturn(requestHeaders).anyTimes();
        requestHeaders.add(ACCEPTABLE + details);

        replay(httpheaders);

        for (int i = 0 ; i < rels.length ; i++) {
            assertEquals(expected[i], DetailHelper.include(httpheaders, rels[i]));
        }

        verify(httpheaders);
    }

}
