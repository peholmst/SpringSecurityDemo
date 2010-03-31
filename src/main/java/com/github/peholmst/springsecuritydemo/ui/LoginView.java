/*
 * Copyright 2010 Petter Holmström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.peholmst.springsecuritydemo.ui;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;

/**
 * 
 * @author Petter Holmström
 */
public class LoginView extends AbstractView {
	
	private static final long serialVersionUID = 2192899815277460850L;
	
	private final AuthenticationManager authenticationManager;
	
	/**
	 * 
	 * @author petter
	 *
	 */
	public static class LoginEvent extends Event {

		private static final long serialVersionUID = -8875211687130316896L;
		
		private final Authentication authentication;
		
		/**
		 * 
		 * @param source
		 * @param authentication
		 */
		public LoginEvent(Component source, Authentication authentication) {
			super(source);
			this.authentication = authentication;
		}
		
		/**
		 * 
		 * @return
		 */
		public Authentication getAuthentication() {
			return authentication;
		}	
	}
	
	/**
	 * 
	 * @param application
	 * @param authenticationManager
	 */
	public LoginView(SpringSecurityDemoApp application, AuthenticationManager authenticationManager) {
		super(application);
		assert authenticationManager != null : "authenticationManager must not be null";
		// Note, that the container will already have been initialized at this stage.
		this.authenticationManager = authenticationManager;
	}
	
	@SuppressWarnings("serial")
	@Override
	protected void init() {
		final Panel loginPanel = new Panel();
		loginPanel.setCaption(getApplication().getMessage("login.title"));		
		((VerticalLayout) loginPanel.getContent()).setSpacing(true);
		
		final TextField username = new TextField(getApplication().getMessage("login.username"));
		username.setWidth("100%");
		loginPanel.addComponent(username);
		
		final TextField password = new TextField(getApplication().getMessage("login.password"));
		password.setSecret(true);
		password.setWidth("100%");
		loginPanel.addComponent(password);
				
		final Button loginButton = new Button(getApplication().getMessage("login.button"));
		loginPanel.addComponent(loginButton);
		((VerticalLayout) loginPanel.getContent()).setComponentAlignment(loginButton, Alignment.MIDDLE_RIGHT);
		loginButton.addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				final Authentication auth = new UsernamePasswordAuthenticationToken(username.getValue(), password.getValue());
				try {
					Authentication returned = getAuthenticationManager().authenticate(auth);
					fireEvent(new LoginEvent(LoginView.this, returned));
				} catch (BadCredentialsException e) {
					getWindow().showNotification(getApplication().getMessage("login.badCredentials.title"),
							getApplication().getMessage("login.badCredentials.descr"),
							Notification.TYPE_WARNING_MESSAGE);
				} catch (DisabledException e) {
					getWindow().showNotification(getApplication().getMessage("login.disabled.title"),
							getApplication().getMessage("login.disabled.descr"),
							Notification.TYPE_WARNING_MESSAGE);
				} catch (LockedException e) {
					getWindow().showNotification(getApplication().getMessage("login.locked.title"),
							getApplication().getMessage("login.locked.descr"),
							Notification.TYPE_WARNING_MESSAGE);					
				} catch (AuthenticationException e) {
					getWindow().showNotification(getApplication().getMessage("login.exception.title"),
							getApplication().getMessage("login.exception.descr", e.getMessage()),
							Notification.TYPE_ERROR_MESSAGE);
				}
			}
		});
		
		HorizontalLayout languages = new HorizontalLayout();
		languages.setSpacing(true);
		final Button.ClickListener languageListener = new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				Locale locale = (Locale) event.getButton().getData();
				getApplication().setLocale(locale);
				getApplication().close();
			}
		};
		for (Locale locale : getApplication().getSupportedLocales()) {
			if (!getLocale().equals(locale)) {
				final Button languageButton = new Button(getApplication().getLocaleDisplayName(locale));
				languageButton.setStyleName(Button.STYLE_LINK);
				languageButton.setData(locale);
				languageButton.addListener(languageListener);
				languages.addComponent(languageButton);
			}
		}		
		loginPanel.addComponent(languages);
		
		loginPanel.setWidth("300px");
		
		final HorizontalLayout viewLayout = new HorizontalLayout();
		viewLayout.addComponent(loginPanel);
		viewLayout.setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER);
		viewLayout.setSizeFull();
		viewLayout.setMargin(true);
		
		setCompositionRoot(viewLayout);
		setSizeFull();		
	}
	
	/**
	 * 
	 * @return
	 */
	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}
}
