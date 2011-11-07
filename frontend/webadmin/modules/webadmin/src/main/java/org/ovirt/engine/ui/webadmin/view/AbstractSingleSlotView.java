package org.ovirt.engine.ui.webadmin.view;

import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for views having a single content slot for displaying child contents.
 */
public abstract class AbstractSingleSlotView extends AbstractView {

    @Override
    public void setInSlot(Object slot, Widget content) {
        if (slot == getContentSlot())
            setContent(content);
        else
            super.setInSlot(slot, content);
    }

    /**
     * Returns the slot object associated with the view content area.
     */
    protected abstract Object getContentSlot();

    /**
     * Sets the child widget into the view content area.
     */
    protected abstract void setContent(Widget content);

}
