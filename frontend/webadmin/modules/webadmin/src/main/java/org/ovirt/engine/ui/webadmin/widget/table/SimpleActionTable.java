package org.ovirt.engine.ui.webadmin.widget.table;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Widget;

public class SimpleActionTable<T> extends AbstractActionTable<T> {

    interface WidgetUiBinder extends UiBinder<Widget, SimpleActionTable<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Style style;

    public SimpleActionTable(ActionTableDataProvider<T> dataProvider) {
        this(dataProvider, null, null);
    }

    public SimpleActionTable(ActionTableDataProvider<T> dataProvider, Resources resources) {
        this(dataProvider, resources, null);
    }

    public SimpleActionTable(ActionTableDataProvider<T> dataProvider, Resources resources, Resources headerResources) {
        super(dataProvider, resources, headerResources);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        localize(ClientGinjectorProvider.instance().getApplicationConstants());
        addStyles();

        refreshPageButton.setVisible(false);
        prevPageButton.setVisible(false);
        nextPageButton.setVisible(false);
    }

    void localize(ApplicationConstants constants) {
        prevPageButton.setText(constants.actionTablePrevPageButtonLabel());
        nextPageButton.setText(constants.actionTableNextPageButtonLabel());
    }

    void addStyles() {
        tableContainer.setStyleName(showDefaultHeader ? style.contentWithDefaultHeader() : style.content());
    }

    public void showRefreshButton() {
        refreshPageButton.setVisible(true);
    }

    public void showPagingButtons() {
        prevPageButton.setVisible(true);
        nextPageButton.setVisible(true);
    }

    @Override
    protected ActionButton createNewActionButton(ActionButtonDefinition<T> buttonDef) {
        return new SimpleActionButton();
    }

    interface Style extends CssResource {
        String content();

        String contentWithDefaultHeader();
    }

}
