package org.vaadin.peholmst.samples.springsecurity.hybrid;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

@SpringUI
// No @Push annotation, we are going to enable it programmatically when the user logs on
@Theme(ValoTheme.THEME_NAME) // Looks nicer
public class SecuredUI extends UI {

    @Autowired
    AuthenticationManager authenticationManager;

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
        getPage().setTitle("Vaadin and Spring Security Demo - Hybrid Security");
        if (SecurityUtils.isLoggedIn()) {
            showMain();
        } else {
            showLogin();
        }
    }

    private void showLogin() {
        setContent(new LoginForm(this::login));
    }

    private void showMain() {
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
        buttons.addComponent(new Button("Logout", event -> logout()));
        timeAndUser = new Label();
        timeAndUser.setSizeUndefined();
        buttons.addComponent(timeAndUser);
        buttons.setComponentAlignment(timeAndUser, Alignment.MIDDLE_LEFT);

        Panel viewContainer = new Panel();
        viewContainer.setSizeFull();
        layout.addComponent(viewContainer);
        layout.setExpandRatio(viewContainer, 1.0f);

        setContent(layout);
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
        timer.cancel();
        super.detach();
    }

    private void updateTimeAndUser() {
        // Demonstrate that server push works and that you can even access the security context from within the
        // access(...) method.
        access(() -> timeAndUser.setValue(String.format("The server-side time is %s and the current user is %s",
            LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            SecurityContextHolder.getContext().getAuthentication().getName())));
    }

    private boolean login(String username, String password) {
        try {
            Authentication token = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));
            // Reinitialize the session to protect against session fixation attacks. This does not work
            // with websocket communication.
            VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
            SecurityContextHolder.getContext().setAuthentication(token);
            // Now when the session is reinitialized, we can enable websocket communication. Or we could have just
            // used WEBSOCKET_XHR and skipped this step completely.
            getPushConfiguration().setTransport(Transport.WEBSOCKET);
            getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
            // Show the main UI
            showMain();
            return true;
        } catch (AuthenticationException ex) {
            return false;
        }
    }

    private void logout() {
        getPage().reload();
        getSession().close();
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
