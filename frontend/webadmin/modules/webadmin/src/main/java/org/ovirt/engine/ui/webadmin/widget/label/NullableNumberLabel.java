package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.webadmin.widget.renderer.NullableNumberRenderer;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.ValueLabel;

public class NullableNumberLabel<T extends Number> extends ValueLabel<T> {

    public NullableNumberLabel() {
        super(new NullableNumberRenderer());
    }

    public NullableNumberLabel(NumberFormat format) {
        super(new NullableNumberRenderer(format));
    }

}
