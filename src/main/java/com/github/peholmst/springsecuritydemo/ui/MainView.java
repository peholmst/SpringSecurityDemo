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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * 
 * @author petter
 *
 */
public class MainView extends AbstractView {

	private static final long serialVersionUID = -8421758733452231380L;

	public MainView(SpringSecurityDemoApp application) {
		super(application);
	}
	
	@SuppressWarnings("serial")
	@Override
	protected void init() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();

		final HorizontalLayout header = new HorizontalLayout();
		header.setMargin(true);
		header.setWidth("100%");

		final Label headerLabel = new Label(getApplication().getMessage("app.title", getApplication().getVersion()));
		headerLabel.setStyleName("appHeaderText");
		header.addComponent(headerLabel);
		header.setStyleName("appHeader");
		
		final Button logoutButton = new Button(getApplication().getMessage("main.logout"), new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Add confirmation
				getApplication().close();
			}
		});
		header.addComponent(logoutButton);
		header.setComponentAlignment(logoutButton, Alignment.MIDDLE_RIGHT);

		mainLayout.addComponent(header);
		
		setCompositionRoot(mainLayout);			
	}
	
}
