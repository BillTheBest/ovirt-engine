package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.ui.uicommonweb.DataProvider;

import com.google.gwt.text.shared.AbstractRenderer;

public class ObjectNameRenderer extends AbstractRenderer<Object[]> {

    @Override
    public String render(Object[] arg) {

        VdcObjectType vdcObjectType = (VdcObjectType) arg[0];
        String objectType = "(" + new EnumRenderer<VdcObjectType>().render(vdcObjectType) + ")";
        String objectName = (String) arg[1];
        if (arg.length == 4 && DataProvider.GetEntityGuid(arg[2]).equals(arg[3])) {
            return "";
        }
        if (vdcObjectType.equals(VdcObjectType.System)) {
            return objectType;
        }
        return objectName + " " + objectType;
    }
}
