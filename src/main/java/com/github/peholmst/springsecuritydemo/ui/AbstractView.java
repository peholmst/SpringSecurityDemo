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

import com.vaadin.ui.CustomComponent;

/**
 * TODO document me!
 * 
 * @author Petter Holmström
 */
public abstract class AbstractView extends CustomComponent {

	private static final long serialVersionUID = -1275291398930837578L;
	private final SpringSecurityDemoApp application;

	/**
	 * Creates a new <code>AbstractView</code>.
	 * 
	 * @param application
	 *            the application that owns the view (never <code>null</code>).
	 */
	public AbstractView(SpringSecurityDemoApp application) {
		super();
		assert application != null : "application must not be null";
		this.application = application;
		init();
	}
	
	/**
	 * Called by the constructor to initialize the view.
	 */
	protected abstract void init();

	@Override
	public SpringSecurityDemoApp getApplication() {
		return application;
	}
}
