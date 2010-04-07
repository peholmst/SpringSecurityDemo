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

import com.vaadin.ui.CustomComponent;

/**
 * This class acts as a base class for views. In this context, a view is a
 * custom component that is associated with exactly one instance of
 * {@link SpringSecurityDemoApp}.
 * 
 * @author Petter Holmström
 */
public abstract class AbstractView extends CustomComponent {

	private static final long serialVersionUID = -1275291398930837578L;
	private final SpringSecurityDemoApp application;
	/**
	 * Apache Commons logger for logging stuff.
	 */
	protected transient final Log logger = LogFactory.getLog(getClass());

	/**
	 * Creates a new <code>AbstractView</code>. Subclasses should remember to
	 * set the composition root of the view.
	 * 
	 * @see #setCompositionRoot(com.vaadin.ui.Component)
	 * @param application
	 *            the application that owns the view (never <code>null</code>).
	 */
	public AbstractView(SpringSecurityDemoApp application) {
		super();
		assert application != null : "application must not be null";
		this.application = application;
	}

	/**
	 * Returns the {@link SpringSecurityDemoApp} that this view belongs to.
	 * 
	 * @return the application instance, never <code>null</code>.
	 */
	@Override
	public SpringSecurityDemoApp getApplication() {
		return application;
	}

	@Override
	protected void finalize() throws Throwable {
		if (logger.isDebugEnabled()) {
			/*
			 * I included this because I wanted to see when views are garbage
			 * collected.
			 */
			logger.debug("Garbage collecting view [" + this + "] owned by ["
					+ application + "]");
		}
		super.finalize();
	}
}
