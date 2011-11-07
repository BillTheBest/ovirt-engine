package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.Extensions;

import com.google.gwt.text.shared.AbstractRenderer;

public class VersionRenderer extends AbstractRenderer<Version> {

    @Override
    public String render(Version version) {
        String formattedVersion = Extensions.GetFriendlyVersion(version).toString();
        return new EmptyValueRenderer<String>().render(formattedVersion);
    }

}
