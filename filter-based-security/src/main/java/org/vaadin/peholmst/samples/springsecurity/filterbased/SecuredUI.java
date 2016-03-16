package org.vaadin.peholmst.samples.springsecurity.filterbased;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringUI
@Push(transport = Transport.LONG_POLLING) // Websocket would bypass the filter chain
@Theme(ValoTheme.THEME_NAME) // Looks nicer
public class SecuredUI extends UI {

    @Autowired
    BackendService backendService;

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(new Button("Invoke user method", event -> {
            // This method should be accessible by both 'user' and 'admin'.
            Notification.show(backendService.userMethod());
        }));
        layout.addComponent(new Button("Invoke admin method", event -> {
            // This method should be accessible by 'admin' only.
            Notification.show(backendService.adminMethod());
        }));
        layout.addComponent(new Button("Logout", event -> {
            // Let Spring Security handle the logout by redirecting to the logout URL
            getPage().setLocation("logout");
        }));
        setContent(layout);
        getPage().setTitle("Vaadin and Spring Security Demo - Filter Based Security");
    }
}
