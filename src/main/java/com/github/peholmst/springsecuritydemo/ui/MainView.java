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

import com.github.peholmst.springsecuritydemo.domain.Category;
import com.github.peholmst.springsecuritydemo.services.CategoryService;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * TODO Document me!
 * @author petter
 * 
 */
public class MainView extends AbstractView implements CategoryBrowser.CategorySelectionListener {

	private static final long serialVersionUID = -8421758733452231380L;

	private final CategoryBrowser categoryBrowser;
	
	public MainView(SpringSecurityDemoApp application, CategoryService categoryService) {
		super(application);
		categoryBrowser = new CategoryBrowser(categoryService, application);
		categoryBrowser.addListener(this);
		init();
	}
	
	protected void init() {
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();

		/*
		 * The header is shown on top of the window and shows information about
		 * the application and the current user.
		 */
		final Component header = createHeader();
		mainLayout.addComponent(header);

		/*
		 * The split panel will contain the component that actually make the
		 * application usable.
		 */

		final SplitPanel splitPanel = new SplitPanel(
				SplitPanel.ORIENTATION_HORIZONTAL);
		splitPanel.setSizeFull();
		mainLayout.addComponent(splitPanel);
		mainLayout.setExpandRatio(splitPanel, 1.0f);

		splitPanel.addComponent(categoryBrowser.getComponent());

		final Component ticketBrowser = createTicketBrowser();
		splitPanel.addComponent(ticketBrowser);

		splitPanel.setSplitPosition(25, Sizeable.UNITS_PERCENTAGE);

		setCompositionRoot(mainLayout);
	}
	
	@Override
	public void selectedCategoryChanged(Category newCategory) {
		// TODO Update the ticket browser!
		getWindow().showNotification("Selected category: " + newCategory);		
	}

	// TODO Remove this - For demonstrational purposes only
/*	private CategoryService createCategoryServiceStub() {
		CategoryService service = new CategoryServiceStub();
		for (int i = 1; i <= 5; ++i) {
			Category r = new Category();
			r.setName("Root" + i);
			service.saveCategory(r);
			for (int j = 1; j <= 5; ++j) {
				Category c = new Category();
				c.setName("Child" + i + "_" + j);
				c.setParent(r);
				service.saveCategory(c);
			}
		}
		return service;
	}*/

	/**
	 * TODO Document me!
	 * 
	 * @return
	 */
	protected Component createTicketBrowser() {
		final HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setSpacing(true);
		toolbar.setWidth("100%");

		final Button refreshButton = new Button(getApplication().getMessage(
				"tickets.refresh.caption"));
		refreshButton.setIcon(new ThemeResource("icons/16/refresh.png"));
		refreshButton.setStyleName("small");
		refreshButton.setDescription(getApplication().getMessage(
				"tickets.refresh.descr"));
		toolbar.addComponent(refreshButton);
		toolbar.setComponentAlignment(refreshButton, Alignment.MIDDLE_LEFT);

		final Button addButton = new Button(getApplication().getMessage(
				"tickets.add.caption"));
		addButton.setIcon(new ThemeResource("icons/16/add.png"));
		addButton.setStyleName("small");
		addButton.setDescription(getApplication().getMessage(
				"tickets.add.descr"));
		toolbar.addComponent(addButton);
		toolbar.setComponentAlignment(addButton, Alignment.MIDDLE_RIGHT);

		final SplitPanel splitPanel = new SplitPanel();
		splitPanel.setSizeFull();

		final Table ticketsTable = new Table();
		ticketsTable.setSizeFull();
		splitPanel.addComponent(ticketsTable);

		splitPanel.addComponent(new Label(
				"The form for editing tickets will show up here"));

		final VerticalLayout browser = new VerticalLayout();
		browser.setSizeFull();
		browser.addComponent(toolbar);
		browser.addComponent(splitPanel);
		browser.setExpandRatio(splitPanel, 1.0f);

		return browser;
	}

	/**
	 * TODO Document me!
	 * 
	 * @return
	 */
	@SuppressWarnings("serial")
	protected Component createHeader() {
		final HorizontalLayout header = new HorizontalLayout();
		header.setMargin(true);
		header.setWidth("100%");

		/*
		 * Header label will contain the name and version of the application.
		 */
		final Label headerLabel = new Label(getApplication().getMessage(
				"app.title", getApplication().getVersion()));
		headerLabel.setStyleName("appHeaderText");
		header.addComponent(headerLabel);
		header.setStyleName("appHeader");
		header.setExpandRatio(headerLabel, 1.0f);
		header.setComponentAlignment(headerLabel, Alignment.MIDDLE_LEFT);

		/*
		 * User links contains information about the current user and a button
		 * for logging out.
		 */
		final HorizontalLayout userLinks = new HorizontalLayout();
		userLinks.setStyleName("appHeaderUserLinks");
		userLinks.setSpacing(true);

		/*
		 * The user label contains the name of the current user.
		 */
		final Label userLabel = new Label(
				getApplication().getMessage("main.currentlyLoggedIn",
						getApplication().getUser().getName()),
				Label.CONTENT_XHTML);
		userLinks.addComponent(userLabel);
		userLinks.setComponentAlignment(userLabel, Alignment.MIDDLE_LEFT);
		userLabel.setSizeUndefined();

		/*
		 * The logout button closes the application, effectively logging the
		 * user out.
		 */
		final Button logoutButton = new Button(getApplication().getMessage(
				"main.logout.caption"), new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Add confirmation
				getApplication().close();
			}
		});
		logoutButton.setDescription(getApplication().getMessage(
				"main.logout.descr"));
		logoutButton.setStyleName("small");
		userLinks.addComponent(logoutButton);
		userLinks.setComponentAlignment(logoutButton, Alignment.MIDDLE_RIGHT);

		header.addComponent(userLinks);
		header.setComponentAlignment(userLinks, Alignment.MIDDLE_RIGHT);
		return header;
	}
}
