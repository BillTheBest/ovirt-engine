package org.ovirt.engine.ui.webadmin.section.login.view;

import org.ovirt.engine.ui.webadmin.section.login.presenter.LoginSectionPresenter;
import org.ovirt.engine.ui.webadmin.view.AbstractView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class LoginSectionView extends AbstractView implements LoginSectionPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, LoginSectionView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    public LoginSectionView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

}
