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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;

import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * This is a utility class for handling common exceptions, such as access
 * denied, optimistic locking failure and data integrity violation.
 * 
 * @author Petter Holmström
 */
public class ExceptionUtils {

	/**
	 * Apache Commons logger for logging stuff.
	 */
	protected static final Log logger = LogFactory.getLog(ExceptionUtils.class);

	/**
	 * Shows a message to the user that access has been deined.
	 * 
	 * @param window
	 *            the window in which the notification should be shown.
	 * @param exception
	 *            the exception to handle.
	 * @param message
	 *            the message to show to the user.
	 */
	public static void handleException(Window window,
			AccessDeniedException exception, String message) {
		if (logger.isDebugEnabled()) {
			logger.debug("Handling exception", exception);
		}
		window.showNotification(((SpringSecurityDemoApp) window
				.getApplication()).getMessage("common.accessDenied.title"),
				message, Notification.TYPE_WARNING_MESSAGE);
	}

	/**
	 * Shows a message to the user that an optimistic failure has occurred.
	 * 
	 * @param window
	 *            the window in which the notification should be shown.
	 * @param exception
	 *            the exception to handle.
	 * @param message
	 *            the message to show to the user.
	 */
	public static void handleException(Window window,
			OptimisticLockingFailureException exception, String message) {
		if (logger.isDebugEnabled()) {
			logger.debug("Handling exception", exception);
		}
		window.showNotification(((SpringSecurityDemoApp) window
				.getApplication()).getMessage("common.optLockFail.title"),
				message, Notification.TYPE_WARNING_MESSAGE);
	}

	/**
	 * Shows a message to the user that an unexpected error has occurred.
	 * 
	 * @param window
	 *            the window in which the notification should be shown.
	 * @param exception
	 *            the exception to handle.
	 */
	public static void handleException(Window window, Exception exception) {
		if (logger.isDebugEnabled()) {
			logger.debug("Handling exception", exception);
		}
		SpringSecurityDemoApp app = (SpringSecurityDemoApp) window
				.getApplication();
		window.showNotification(app
				.getMessage("common.unexpectedException.title"), app
				.getMessage("common.unexpectedException.descr", exception
						.getMessage()), Notification.TYPE_ERROR_MESSAGE);
		// FIXME It seems that {0} shows up instead of the exception message. why?
	}

	/**
	 * Shows a message to the user that a data integrity violation has occurred.
	 * 
	 * @param window
	 *            the window in which the notification should be shown.
	 * @param exception
	 *            the exception to handle.
	 * @param message
	 *            the message to show to the user.
	 */
	public static void handleException(Window window,
			DataIntegrityViolationException exception, String message) {
		if (logger.isDebugEnabled()) {
			logger.debug("Handling exception", exception);
		}
		window.showNotification(((SpringSecurityDemoApp) window
				.getApplication())
				.getMessage("common.dataIntegrityViolation.title"), message,
				Notification.TYPE_WARNING_MESSAGE);
	}
}
