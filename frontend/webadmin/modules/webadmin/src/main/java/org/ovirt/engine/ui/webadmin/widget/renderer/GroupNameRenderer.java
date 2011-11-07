package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.DataProvider;

import com.google.gwt.text.shared.AbstractRenderer;

public class GroupNameRenderer extends AbstractRenderer<Object[]> {

    @Override
    public String render(Object[] arg) {

        Object entity = arg[0];
        Guid objectGuid = (Guid) arg[1];

        if (DataProvider.GetEntityGuid(entity).equals(objectGuid)) {
            return "";
        }
        String ownerName = (String) arg[2];
        return ownerName;
    }
}
