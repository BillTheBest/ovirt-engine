package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

/**
 * Provider of main model instances.
 * 
 * @param <T>
 *            Main model item type.
 * @param <M>
 *            Main model type.
 */
public interface MainModelProvider<T, M extends SearchableListModel> extends SearchableTableModelProvider<T, M> {

    /**
     * Notifies the model that the corresponding main tab has been selected.
     */
    void onMainTabSelected();

}
