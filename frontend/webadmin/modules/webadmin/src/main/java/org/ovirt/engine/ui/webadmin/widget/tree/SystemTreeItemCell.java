package org.ovirt.engine.ui.webadmin.widget.tree;

import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SystemTreeItemCell extends AbstractCell<SystemTreeItemModel> {

    private final ApplicationResources applicationResources;
    private final ApplicationTemplates templates;

    public SystemTreeItemCell(ApplicationResources applicationResources, ApplicationTemplates templates) {
        this.applicationResources = applicationResources;
        this.templates = templates;
    }

    @Override
    public void render(Context context, SystemTreeItemModel value, SafeHtmlBuilder sb) {
        ImageResource imageResource;

        // get the right image resource
        switch (value.getType()) {
        case Cluster:
            imageResource = applicationResources.clusterImage();
            break;
        case Clusters:
            imageResource = applicationResources.clustersImage();
            break;
        case DataCenter:
            imageResource = applicationResources.dataCenterImage();
            break;
        case Host:
            imageResource = applicationResources.hostImage();
            break;
        case Hosts:
            imageResource = applicationResources.hostsImage();
            break;
        case Storage:
            imageResource = applicationResources.storageImage();
            break;
        case Storages:
            imageResource = applicationResources.storagesImage();
            break;
        case System:
            imageResource = applicationResources.systemImage();
            break;
        case Templates:
            imageResource = applicationResources.templatesImage();
            break;
        case VMs:
            imageResource = applicationResources.vmsImage();
            break;
        default:
            imageResource = applicationResources.questionMarkImage();
        }

        // get the image HTML
        SafeHtml imageHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(imageResource).getHTML());

        // apply to template
        sb.append(templates.treeItem(imageHtml, value.getTitle()));
    }

}
