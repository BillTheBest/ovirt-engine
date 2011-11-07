package org.ovirt.engine.ui.webadmin.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer that sums up Double values.
 */
public class SumUpRenderer extends AbstractRenderer<Double[]> {

    @Override
    public String render(Double[] values) {
        double sum = 0;

        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                return "[N/A]";
            }
            sum += values[i];
        }

        int intVal = (int) sum;
        return Integer.toString(intVal);
    }

}
