package org.ovirt.engine.ui.webadmin.widget.table.column;

/**
 * Column for displaying percent-based progress bar.
 * 
 * @param <T>
 *            Table row data type.
 */
public abstract class PercentColumn<T> extends ProgressBarColumn<T> {

    @Override
    protected String getProgressText(T object) {
        Integer progressValue = getProgressValue(object);
        return progressValue != null ? progressValue + "%" : "0%";
    }

}
