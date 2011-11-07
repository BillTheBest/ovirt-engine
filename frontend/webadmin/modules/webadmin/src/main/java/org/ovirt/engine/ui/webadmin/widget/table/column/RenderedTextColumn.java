package org.ovirt.engine.ui.webadmin.widget.table.column;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.cellview.client.TextColumn;

/**
 * Base class for text columns that use {@link Renderer} to render the given column type into its text-based
 * representation.
 * 
 * @param <T>
 *            Table row data type.
 * @param <C>
 *            Column value type.
 */
public abstract class RenderedTextColumn<T, C> extends TextColumn<T> {

    protected final Renderer<C> renderer;

    public RenderedTextColumn(Renderer<C> renderer) {
        super();
        this.renderer = renderer;
    }

    @Override
    public String getValue(T object) {
        return renderer.render(getRawValue(object));
    };

    /**
     * Returns the raw value to be rendered.
     */
    protected abstract C getRawValue(T object);

}
