package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * A {@link TreeNodeModel} for {@link SelectionTreeNodeModel} instances
 */
public class SimpleSelectionTreeNodeModel implements TreeNodeModel<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> {

    /**
     * Build from a list of {@link SelectionTreeNodeModel} instances
     */
    public static List<SimpleSelectionTreeNodeModel> fromList(List<SelectionTreeNodeModel> list) {
        List<SimpleSelectionTreeNodeModel> result = new ArrayList<SimpleSelectionTreeNodeModel>();
        for (SelectionTreeNodeModel selectionTreeNodeModel : list) {
            result.add(new SimpleSelectionTreeNodeModel(selectionTreeNodeModel));
        }
        return result;
    }

    /**
     * Build from a single {@link SelectionTreeNodeModel} instance
     */
    public static SimpleSelectionTreeNodeModel fromModel(SelectionTreeNodeModel model) {
        return new SimpleSelectionTreeNodeModel(model);
    }

    private final ArrayList<SimpleSelectionTreeNodeModel> children;

    private final EventBus eventBus;

    private final SelectionTreeNodeModel model;

    protected SimpleSelectionTreeNodeModel(SelectionTreeNodeModel model) {
        this.eventBus = ClientGinjectorProvider.instance().getEventBus();
        this.model = model;

        // Build children List
        this.children = new ArrayList<SimpleSelectionTreeNodeModel>();
        for (SelectionTreeNodeModel childModel : model.getChildren()) {
            children.add(new SimpleSelectionTreeNodeModel(childModel));
        }

        // Add selection listener
        model.getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ("IsSelectedNullable".equals(((PropertyChangedEventArgs) args).PropertyName)) {
                    SelectionEvent.fire(SimpleSelectionTreeNodeModel.this, SimpleSelectionTreeNodeModel.this);
                }
            }
        });

    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<SimpleSelectionTreeNodeModel> handler) {
        return eventBus.addHandler(SelectionEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    @Override
    public ArrayList<SimpleSelectionTreeNodeModel> getChildren() {
        return children;
    }

    @Override
    public String getName() {
        return model.getDescription();
    }

    @Override
    public boolean getSelected() {
        return model.getIsSelectedNullable() != null ? model.getIsSelectedNullable() : false;
    }

    @Override
    public boolean isEditable() {
        return model.getIsChangable();
    }

    @Override
    public void setSelected(boolean value) {
        model.setIsSelectedNullable(value);
    }

}
