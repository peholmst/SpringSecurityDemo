package org.vaadin.peholmst.samples.springsecurity.hybrid;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;

public class LoginForm extends VerticalLayout {

    public LoginForm(LoginCallback callback) {
        setMargin(true);
        setSpacing(true);

        TextField username = new TextField("Username");
        addComponent(username);

        PasswordField password = new PasswordField("Password");
        addComponent(password);

        Button login = new Button("Login", evt -> {
            String pword = password.getValue();
            password.setValue("");
            if (!callback.login(username.getValue(), pword)) {
                Notification.show("Login failed");
                username.focus();
            }
        });
        login.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        addComponent(login);
    }

    @FunctionalInterface
    public interface LoginCallback {

        boolean login(String username, String password);
    }
}
