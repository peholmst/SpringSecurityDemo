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
import com.vaadin.Application;
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
 * 
 * @author Petter Holmström
 */
@Component("applicationBean")
@Scope("prototype")
public class SpringSecurityDemoApp extends Application {

	private static final long serialVersionUID = -1412284137848857188L;

	/**
	 * Commons Log for logging stuff.
	 */
	protected transient final Log logger = LogFactory.getLog(getClass());

	@Resource
	private transient MessageSource messages;

	@Resource
	private transient AuthenticationManager authenticationManager;

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
		// Create the views
		loginView = new LoginView(this, authenticationManager);
		loginView.setSizeFull();
		mainView = new MainView(this);
		mainView.setSizeFull();

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
					SecurityContextHolder.getContext().setAuthentication(
							((LoginView.LoginEvent) event).getAuthentication());
					removeWindow(loginWindow);
					setMainWindow(new Window(getMessage("app.title",
							getVersion()), mainView));
				}
			}
		});
	}

	@Override
	@PreDestroy // In case the application is destroyed by the container
	public void close() {
		if (logger.isDebugEnabled()) {
			logger.debug("Closing application [" + this + "]");
		}
		// Clear the security context to log the user out
		SecurityContextHolder.clearContext();
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

	/**
	 * Gets the currently logged in user from {@link SecurityContextHolder}. If
	 * this value is <code>null</code>, no user has been logged in yet.
	 * 
	 * @return an {@link Authentication} instance.
	 */
	@Override
	public Object getUser() {
		// Get the user object form Spring Security
		return SecurityContextHolder.getContext().getAuthentication();
	}

	@Override
	public String getVersion() {
		return VersionInfo.getApplicationVersion();
	}

	/**
	 * Tries to resolve the message using the locale returned by
	 * {@link #getLocale()}.
	 * 
	 * @see MessageSource#getMessage(String, Object[], Locale)
	 * 
	 * @param code
	 *            the code to look up.
	 * @param args
	 *            array of arguments that will be filled in for params within
	 *            the message (params look like "{0}", "{1,date}", "{2,time}"),
	 *            or <code>null</code> if there are none.
	 * @return the resolved message (never <code>null</code>).
	 * @throws NoSuchMessageException
	 *             if the message could not be resolved.
	 */
	public String getMessage(String code, Object... args)
			throws NoSuchMessageException {
		return messages.getMessage(code, args, getLocale());
	}

	/**
	 * Gets all the locales that this application supports. More specifically,
	 * the application's UI can be shown in any of the locales returned by this
	 * method.
	 * 
	 * @see #getLocaleDisplayName(Locale)
	 * @return an array of {@link Locale}s.
	 */
	public Locale[] getSupportedLocales() {
		return SUPPORTED_LOCALES;
	}

	/**
	 * Gets the name of <code>locale</code> to display in the user interface.
	 * Each locale is shown in its own language. For example, the "en_US" locale
	 * returns "English", the "fi_FI" locale "Suomi", etc.
	 * <p>
	 * If <code>locale</code> is not in the array returned by
	 * {@link #getSupportedLocales()}, an "Unsuppored Locale" string is
	 * returned.
	 * 
	 * @param locale
	 *            the locale whose display name should be returned.
	 * @return the display name of the locale.
	 */
	public String getLocaleDisplayName(Locale locale) {
		for (int i = 0; i < SUPPORTED_LOCALES.length; i++) {
			if (locale.equals(SUPPORTED_LOCALES[i])) {
				return LOCALE_NAMES[i];
			}
		}
		return "Unsupported Locale";
	}
}
