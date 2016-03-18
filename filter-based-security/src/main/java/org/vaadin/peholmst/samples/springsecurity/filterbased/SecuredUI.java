package org.vaadin.peholmst.samples.springsecurity.filterbased;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

@SpringUI
@Push(transport = Transport.WEBSOCKET_XHR) // Websocket would bypass the filter chain, Websocket+XHR works
@Theme(ValoTheme.THEME_NAME) // Looks nicer
public class SecuredUI extends UI {

    @Autowired
    BackendService backendService;

    @Autowired
    SpringViewProvider viewProvider;

    @Autowired
    ErrorView errorView;

    private Label timeAndUser;

    private Timer timer;

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeFull();

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        layout.addComponent(buttons);

        buttons.addComponent(new Button("Invoke user method", event -> {
            // This method should be accessible by both 'user' and 'admin'.
            Notification.show(backendService.userMethod());
        }));
        buttons.addComponent(new Button("Navigate to user view", event -> {
            getNavigator().navigateTo("");
        }));
        buttons.addComponent(new Button("Invoke admin method", event -> {
            // This method should be accessible by 'admin' only.
            Notification.show(backendService.adminMethod());
        }));
        buttons.addComponent(new Button("Navigate to admin view", event -> {
            getNavigator().navigateTo("admin");
        }));
        buttons.addComponent(new Button("Logout", event -> {
            // Let Spring Security handle the logout by redirecting to the logout URL
            getPage().setLocation("logout");
        }));
        timeAndUser = new Label();
        timeAndUser.setSizeUndefined();
        buttons.addComponent(timeAndUser);
        buttons.setComponentAlignment(timeAndUser, Alignment.MIDDLE_LEFT);

        Panel viewContainer = new Panel();
        viewContainer.setSizeFull();
        layout.addComponent(viewContainer);
        layout.setExpandRatio(viewContainer, 1.0f);

        setContent(layout);
        getPage().setTitle("Vaadin and Spring Security Demo - Filter Based Security");
        setErrorHandler(this::handleError);

        Navigator navigator = new Navigator(this, viewContainer);
        navigator.addProvider(viewProvider);
        navigator.setErrorView(errorView);
        viewProvider.setAccessDeniedViewClass(AccessDeniedView.class);

        // Fire up a timer to demonstrate server push. Do NOT use timers in real-world applications, use a thread pool.
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTimeAndUser();
            }
        }, 1000L, 1000L);
    }

    @Override
    public void detach() {
        super.detach();
        timer.cancel();
    }

    private void updateTimeAndUser() {
        // Demonstrate that server push works, but the security context is not available inside the Timer thread
        // since it is thread-local and populated by a servlet filter.
        access(
            () -> timeAndUser.setValue(String.format("The server-side time is %s and the authentication token in this thread is %s",
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), SecurityContextHolder.getContext().getAuthentication())));
    }

    private void handleError(com.vaadin.server.ErrorEvent event) {
        Throwable t = DefaultErrorHandler.findRelevantThrowable(event.getThrowable());
        if (t instanceof AccessDeniedException) {
            Notification.show("You do not have permission to perform this operation",
                Notification.Type.WARNING_MESSAGE);
        } else {
            DefaultErrorHandler.doDefault(event);
        }
    }
}
