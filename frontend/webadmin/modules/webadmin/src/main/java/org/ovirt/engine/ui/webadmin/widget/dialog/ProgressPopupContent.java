package org.ovirt.engine.ui.webadmin.widget.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class ProgressPopupContent extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, ProgressPopupContent> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Image progressImage;

    @UiField
    InlineLabel progressLabel;

    public ProgressPopupContent() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        progressImage.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
    }

    public void setProgressMessage(String text) {
        progressLabel.setText(text);
        progressLabel.setVisible(text != null);
    }

}
