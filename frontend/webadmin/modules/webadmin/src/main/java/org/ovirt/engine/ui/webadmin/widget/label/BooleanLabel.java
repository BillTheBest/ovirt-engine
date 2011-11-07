package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.webadmin.widget.renderer.BooleanRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class BooleanLabel extends ValueLabel<Boolean> {

    public BooleanLabel() {
        super(new BooleanRenderer());
    }

    public BooleanLabel(String trueText, String falseText) {
        super(new BooleanRenderer(trueText, falseText));
    }
}
