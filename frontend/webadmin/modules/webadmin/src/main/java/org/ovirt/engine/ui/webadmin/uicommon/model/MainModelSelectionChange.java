package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when an active (selected) main model changes within the {@link CommonModel}.
 */
@GenEvent
public class MainModelSelectionChange {

    SearchableListModel mainModel;

}
