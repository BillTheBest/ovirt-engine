package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.HeaderPresenterWidget;
import org.ovirt.engine.ui.webadmin.view.AbstractSingleSlotView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HeaderView extends AbstractSingleSlotView implements HeaderPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, HeaderView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private static final int mainTabBarInitialOffset = 250;

    @UiField
    InlineLabel userNameLabel;

    @UiField(provided = true)
    final Anchor logoutLink;

    @UiField(provided = true)
    final Anchor configureLink;
    
    @UiField(provided = true)
    final Anchor aboutLink;

    @UiField(provided = true)
    final Anchor guideLink;

    @UiField
    SimplePanel searchPanelContainer;

    @UiField
    HTMLPanel mainTabBarPanel;

    @UiField
    FlowPanel mainTabContainer;

    @Inject
    public HeaderView(ApplicationConstants constants) {
        this.configureLink = new Anchor(constants.configureLinkLabel());
        this.logoutLink = new Anchor(constants.logoutLinkLabel());
        this.aboutLink = new Anchor(constants.aboutLinkLabel());
        this.guideLink = new Anchor(constants.guideLinkLabel());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        mainTabBarPanel.getElement().getStyle().setZIndex(1);

        // Ensure proper main tab bar position
        setMainTabBarOffset(mainTabBarInitialOffset);
    }

    @Override
    protected Object getContentSlot() {
        return HeaderPresenterWidget.TYPE_SetSearchPanel;
    }

    @Override
    protected void setContent(Widget content) {
        setPanelContent(searchPanelContainer, content);
    }

    @Override
    public void addTabWidget(Widget tabWidget, int index) {
        mainTabContainer.insert(tabWidget, index);
    }

    @Override
    public void removeTabWidget(Widget tabWidget) {
        mainTabContainer.getElement().removeChild(tabWidget.getElement());
    }

    @Override
    public void setMainTabBarOffset(int left) {
        mainTabBarPanel.getElement().getStyle().setLeft(left, Unit.PX);
        mainTabBarPanel.getElement().getStyle().setWidth(Window.getClientWidth() - left, Unit.PX);
    }

    @Override
    public void setUserNameLabel(String userName) {
        userNameLabel.setText(userName);
    }

    @Override
    public HasClickHandlers getLogoutLink() {
        return logoutLink;
    }

    @Override
    public HasClickHandlers getAboutLink() {
        return aboutLink;
    }

    @Override
    public HasClickHandlers getGuideLink() {
        return guideLink;
    }

    @Override
    public HasClickHandlers getConfigureLink() {
        return configureLink;
    }
}
