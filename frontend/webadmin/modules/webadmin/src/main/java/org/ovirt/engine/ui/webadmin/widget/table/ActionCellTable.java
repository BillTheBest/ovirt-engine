package org.ovirt.engine.ui.webadmin.widget.table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.ProvidesKey;

public class ActionCellTable<T> extends CellTable<T> {

    private static final int DEFAULT_PAGESIZE = 1000;
    private static Resources DEFAULT_RESOURCES = GWT.create(CellTable.Resources.class);

    public ActionCellTable(ProvidesKey<T> keyProvider, Resources resources) {
        super(DEFAULT_PAGESIZE, resources != null ? resources : DEFAULT_RESOURCES, keyProvider);
    }

    public ActionCellTable(Resources resources) {
        super(DEFAULT_PAGESIZE, resources != null ? resources : DEFAULT_RESOURCES);
    }
}