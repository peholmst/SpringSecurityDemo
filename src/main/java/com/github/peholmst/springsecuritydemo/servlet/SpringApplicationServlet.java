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

package com.github.peholmst.springsecuritydemo.servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;

/**
 * This Vaadin application servlet looks up the {@link Application} instance
 * from the {@link WebApplicationContext} returned by
 * {@link WebApplicationContextUtils#getWebApplicationContext(javax.servlet.ServletContext)}
 * . The name of the application bean has to be defined as a servlet parameter
 * named <code>applicationBean</code>. In addition, the application bean has to
 * have either prototype or session scope, otherwise all users will share the
 * same application instance (not good).
 * <p>
 * This servlet also resolves the locale using a Spring locale resolver and
 * updates the requests accordingly.
 * <p>
 * This class is based on the <a
 * href="http://dev.vaadin.com/browser/incubator/SpringApplication"
 * >SpringApplication example</a> by Petri Hakala, with some minor modifications
 * here and there by yours truly.
 * 
 * @author Petter Holmström
 */
public class SpringApplicationServlet extends AbstractApplicationServlet {

	private static final long serialVersionUID = 9119643883315827366L;

	/**
	 * Protected Apache Commons Log for logging stuff.
	 */
	protected transient final Log logger = LogFactory.getLog(getClass());

	private transient WebApplicationContext applicationContext;

	private Class<? extends Application> applicationClass;

	private String applicationBean;

	private transient LocaleResolver localeResolver;

	@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		/*
		 * Look up the name of the Vaadin application bean. The bean should be
		 * either a prototype or have session scope.
		 */
		applicationBean = servletConfig.getInitParameter("applicationBean");
		if (applicationBean == null) {
			if (logger.isErrorEnabled()) {
				logger
						.error("ApplicationBean not specified in servlet parameters");
			}
			throw new ServletException(
					"ApplicationBean not specified in servlet parameters");
		}

		if (logger.isInfoEnabled()) {
			logger.info("Using applicationBean '" + applicationBean + "'");
		}

		/*
		 * Fetch the Spring web application context
		 */
		applicationContext = WebApplicationContextUtils
				.getWebApplicationContext(servletConfig.getServletContext());
		/*
		 * Get the application class from the application context
		 */
		applicationClass = (Class<? extends Application>) applicationContext
				.getType(applicationBean);
		if (logger.isDebugEnabled()) {
			logger.debug("Vaadin application class is [" + applicationClass
					+ "]");
		}
		/*
		 * Initialize the locale resolver
		 */
		initLocaleResolver(applicationContext);
	}

	private void initLocaleResolver(ApplicationContext context) {
		try {
			/*
			 * Try to look up the locale resolver from the application context
			 */
			localeResolver = context.getBean(
					DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME,
					LocaleResolver.class);
			if (logger.isInfoEnabled()) {
				logger.info("Using LocaleResolver [" + localeResolver + "]");
			}
		} catch (NoSuchBeanDefinitionException e) {
			/*
			 * No locale resolver was defined in the application context, so we
			 * create a default one.
			 */
			localeResolver = new SessionLocaleResolver();
			if (logger.isWarnEnabled()) {
				logger.warn("Unable to locate LocaleResolver with name '"
						+ DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME
						+ "', using default [" + localeResolver + "]");
			}
		}
	}

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/*
		 * Resolve the locale from the request
		 */
		final Locale locale = localeResolver.resolveLocale(request);
		if (logger.isDebugEnabled()) {
			logger.debug("Resolved locale [" + locale + "]");
		}

		/*
		 * Store the locale in the LocaleContextHolder, making it available to
		 * Spring.
		 */
		LocaleContextHolder.setLocale(locale);
		ServletRequestAttributes requestAttributes = new ServletRequestAttributes(
				request);
		RequestContextHolder.setRequestAttributes(requestAttributes);
		try {
			/*
			 * We need to override the request to return the locale resolved by
			 * Spring.
			 */
			super.service(new HttpServletRequestWrapper(request) {
				@Override
				public Locale getLocale() {
					return locale;
				}
			}, response);
		} finally {
			if (!locale.equals(LocaleContextHolder.getLocale())) {
				/*
				 * The locale in LocaleContextHolder was changed during the
				 * request, so we have to update the resolver.
				 */
				if (logger.isDebugEnabled()) {
					logger.debug("Locale changed, updating locale resolver");
				}
				localeResolver.setLocale(request, response, LocaleContextHolder
						.getLocale());
			}
			LocaleContextHolder.resetLocaleContext();
			RequestContextHolder.resetRequestAttributes();
		}
	}

	@Override
	protected Class<? extends Application> getApplicationClass()
			throws ClassNotFoundException {
		return applicationClass;
	}

	@Override
	protected Application getNewApplication(HttpServletRequest request)
			throws ServletException {
		/*
		 * AS the application bean should be defined either as a prototype or a
		 * sessions scoped bean, this call should always return a new
		 * application instance.
		 */
		return (Application) applicationContext.getBean(applicationBean);
	}

}
