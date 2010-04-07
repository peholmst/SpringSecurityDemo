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

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

/**
 * This interface defines some methods that are useful for internationalization
 * (i18n). It provides localized strings and information about the locales of
 * the application.
 * 
 * @author Petter Holmström
 */
public interface I18nProvider {

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
			throws NoSuchMessageException;

	/**
	 * Gets all the locales that this application supports. More specifically,
	 * the application's UI can be shown in any of the locales returned by this
	 * method.
	 * 
	 * @see #getLocaleDisplayName(Locale)
	 * @return an array of {@link Locale}s.
	 */
	public Locale[] getSupportedLocales();

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
	public String getLocaleDisplayName(Locale locale);

	/**
	 * Gets the current locale.
	 * 
	 * @return the current locale.
	 */
	public Locale getLocale();

}
