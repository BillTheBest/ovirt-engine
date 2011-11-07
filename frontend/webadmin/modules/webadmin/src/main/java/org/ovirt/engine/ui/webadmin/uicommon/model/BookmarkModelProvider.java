package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.bookmarks;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.bookmark.BookmarkPopupPresenterWidget;

import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class BookmarkModelProvider extends DataBoundTabModelProvider<bookmarks, BookmarkListModel> {

    private final SingleSelectionModel<bookmarks> selectionModel;

    private final Provider<BookmarkPopupPresenterWidget> popupProvider;
    private final Provider<RemoveConfirmationPopupPresenterWidget> removeConfirmPopupProvider;

    private final SystemTreeModelProvider treeModelProvider;
    private final TagModelProvider tagModelProvider;

    @Inject
    public BookmarkModelProvider(ClientGinjector ginjector,
            SystemTreeModelProvider treeModelProvider, TagModelProvider tagModelProvider) {
        super(ginjector);
        this.popupProvider = ginjector.getBookmarkPopupPresenterWidgetProvider();
        this.removeConfirmPopupProvider = ginjector.getRemoveConfirmPopupProvider();
        this.treeModelProvider = treeModelProvider;
        this.tagModelProvider = tagModelProvider;

        // Create selection model
        selectionModel = new SingleSelectionModel<bookmarks>();
        selectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                List<bookmarks> selectedItems = selectionModel.getSelectedObject() != null
                        ? new ArrayList<bookmarks>(Arrays.asList(selectionModel.getSelectedObject()))
                        : new ArrayList<bookmarks>();
                BookmarkModelProvider.this.setSelectedItems(selectedItems);
            }
        });
    }

    @Override
    protected void onCommonModelChange() {
        super.onCommonModelChange();

        // Clear selection when a system tree node is selected
        treeModelProvider.getModel().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (treeModelProvider.getModel().getSelectedItem() != null) {
                    clearSelection();
                }
            }
        });

        // Clear selection when a tag tree node is pinned
        tagModelProvider.getModel().getSelectedItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (tagModelProvider.getModel().getSelectedItems() != null
                        && !tagModelProvider.getModel().getSelectedItems().isEmpty()) {
                    clearSelection();
                }
            }
        });
    }

    void clearSelection() {
        if (selectionModel.getSelectedObject() != null) {
            selectionModel.setSelected(selectionModel.getSelectedObject(), false);
        }
    }

    @Override
    protected void updateDataProvider(List<bookmarks> items) {
        super.updateDataProvider(items);

        // Clear selection when updating data
        clearSelection();
    }

    @Override
    public BookmarkListModel getModel() {
        return getCommonModel().getBookmarkList();
    }

    @Override
    public void setSelectedItems(List<bookmarks> items) {
        getModel().setSelectedItem(items.size() > 0 ? items.get(0) : null);
        getModel().setSelectedItems(items);
    }

    public SingleSelectionModel<bookmarks> getSelectionModel() {
        return selectionModel;
    }

    @Override
    public void addDataDisplay(HasData<bookmarks> display) {
        super.addDataDisplay(display);
        display.setSelectionModel(selectionModel);
    }

    @Override
    protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getNewCommand()
                || lastExecutedCommand == getModel().getEditCommand()) {
            return popupProvider.get();
        } else {
            return super.getModelPopup(lastExecutedCommand);
        }
    }

    @Override
    protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
        if (lastExecutedCommand == getModel().getRemoveCommand()) {
            return removeConfirmPopupProvider.get();
        } else {
            return super.getConfirmModelPopup(lastExecutedCommand);
        }
    }

}
