/*
 * Copyright (c) 2010 The original author(s)
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

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.github.peholmst.springsecuritydemo.VersionInfo;
import com.github.peholmst.springsecuritydemo.services.CategoryService;
import com.vaadin.Application;
import com.vaadin.service.ApplicationContext.TransactionListener;
import com.vaadin.ui.Window;
import com.vaadin.ui.Component.Event;

/**
 * Main Vaadin application class for this demo. When first initialized, the
 * application displays an instance of {@link LoginView} as its main window.
 * Once a user has logged in successfully, the main window is replaced with an
 * instance of {@link MainView}, which remains the main window until the
 * application is closed.
 * <p>
 * This application class has been annotated with Spring application context
 * annotations. Its scope has been set to "prototype", meaning that a new
 * instance of the class will be returned every time the bean is accessed in the
 * Spring application context.
 * <p>
 * This class contains a lot of logging entries, the purpose of which is to make
 * it possible to follow what happens under the hood at different stages of the
 * application lifecycle.
 * 
 * @author Petter Holmström
 */
@Component("applicationBean")
@Scope("prototype")
public class SpringSecurityDemoApp extends Application implements I18nProvider,
		TransactionListener {

	private static final long serialVersionUID = -1412284137848857188L;

	/**
	 * Apache Commons logger for logging stuff.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	@Resource
	private MessageSource messages;

	@Resource
	private AuthenticationManager authenticationManager;

	@Resource
	private CategoryService categoryService;

	private LoginView loginView;

	private MainView mainView;

	private static final Locale[] SUPPORTED_LOCALES = { Locale.US,
			new Locale("fi", "FI"), new Locale("sv", "SE") };

	private static final String[] LOCALE_NAMES = { "English", "Suomi",
			"Svenska" };

	@Override
	public Locale getLocale() {
		/*
		 * Fetch the locale resolved by Spring in the application servlet
		 */
		return LocaleContextHolder.getLocale();
	}

	@Override
	public void setLocale(Locale locale) {
		LocaleContextHolder.setLocale(locale);
	}

	@SuppressWarnings("serial")
	@Override
	public void init() {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing application [" + this + "]");
		}
		// Register listener
		getContext().addTransactionListener(this);

		// Create the views
		loginView = new LoginView(this, authenticationManager);
		loginView.setSizeFull();

		setTheme("SpringSecurityDemo"); // We use a custom theme

		final Window loginWindow = new Window(getMessage("app.title",
			getVersion()), loginView);
		setMainWindow(loginWindow);

		loginView.addListener(new com.vaadin.ui.Component.Listener() {
			@Override
			public void componentEvent(Event event) {
				if (event instanceof LoginView.LoginEvent) {
					if (logger.isDebugEnabled()) {
						logger.debug("User logged on ["
								+ ((LoginView.LoginEvent) event)
									.getAuthentication() + "]");
					}
					/*
					 * A user has logged on, which means we can ditch the login
					 * view and open the main view instead. We also have to
					 * update the security context holder.
					 */
					setUser(((LoginView.LoginEvent) event).getAuthentication());
					SecurityContextHolder.getContext().setAuthentication(
						((LoginView.LoginEvent) event).getAuthentication());
					removeWindow(loginWindow);
					loginView = null;
					mainView = new MainView(SpringSecurityDemoApp.this,
						categoryService);
					mainView.setSizeFull();
					setMainWindow(new Window(getMessage("app.title",
						getVersion()), mainView));
				}
			}
		});
	}

	@Override
	@PreDestroy
	// In case the application is destroyed by the container
	public void close() {
		if (logger.isDebugEnabled()) {
			logger.debug("Closing application [" + this + "]");
		}
		// Clear the authentication property to log the user out
		setUser(null);
		// Also clear the security context
		SecurityContextHolder.clearContext();
		getContext().removeTransactionListener(this);
		super.close();
	}

	@Override
	protected void finalize() throws Throwable {
		if (logger.isDebugEnabled()) {
			/*
			 * This is included to make sure that closed applications get
			 * properly garbage collected.
			 */
			logger.debug("Garbage collecting application [" + this + "]");
		}
		super.finalize();
	}

	@Override
	public void transactionEnd(Application application, Object transactionData) {
		if (logger.isDebugEnabled()) {
			logger
				.debug("Transaction ended, removing authentication data from security context");
		}
		/*
		 * The purpose of this
		 */
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	@Override
	public void transactionStart(Application application, Object transactionData) {
		if (logger.isDebugEnabled()) {
			logger
				.debug("Transaction started, setting authentication data of security context to ["
						+ application.getUser() + "]");
		}
		/*
		 * The security context holder uses the thread local pattern to store
		 * its authentication credentials. As requests may be handled by
		 * different threads, we have to update the security context holder in
		 * the beginning of each transaction.
		 */
		SecurityContextHolder.getContext().setAuthentication(
			(Authentication) application.getUser());
	}

	/**
	 * Gets the currently logged in user. If this value is <code>null</code>, no
	 * user has been logged in yet.
	 * 
	 * @return an {@link Authentication} instance.
	 */
	@Override
	public Authentication getUser() {
		return (Authentication) super.getUser();
	}

	@Override
	public String getVersion() {
		return VersionInfo.getApplicationVersion();
	}

	@Override
	public String getMessage(String code, Object... args)
			throws NoSuchMessageException {
		return messages.getMessage(code, args, getLocale());
	}

	@Override
	public Locale[] getSupportedLocales() {
		return SUPPORTED_LOCALES;
	}

	@Override
	public String getLocaleDisplayName(Locale locale) {
		for (int i = 0; i < SUPPORTED_LOCALES.length; i++) {
			if (locale.equals(SUPPORTED_LOCALES[i])) {
				return LOCALE_NAMES[i];
			}
		}
		return "Unsupported Locale";
	}
}
