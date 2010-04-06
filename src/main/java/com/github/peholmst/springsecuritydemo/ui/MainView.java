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

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;

import com.github.peholmst.springsecuritydemo.domain.Category;
import com.github.peholmst.springsecuritydemo.services.CategoryService;
import com.github.peholmst.springsecuritydemo.services.stubs.CategoryServiceStub;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;

/**
 * 
 * @author petter
 * 
 */
public class MainView extends AbstractView {

	private static final long serialVersionUID = -8421758733452231380L;

	private final CategoryService categoryService;

	public MainView(SpringSecurityDemoApp application) {
		super(application);
		categoryService = createCategoryServiceStub();
		init();
	}

	@Override
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

		final Component categoryBrowser = createCategoryBrowser();
		splitPanel.addComponent(categoryBrowser);

		final Component ticketBrowser = createTicketBrowser();
		splitPanel.addComponent(ticketBrowser);

		splitPanel.setSplitPosition(25, Sizeable.UNITS_PERCENTAGE);

		setCompositionRoot(mainLayout);
	}

	/**
	 * 
	 * @return
	 */
	public CategoryService getCategoryService() {
		return categoryService;
	}

	// TODO Remove this - For demonstrational purposes only
	private CategoryService createCategoryServiceStub() {
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
	}

	/**
	 * TODO Document me!
	 * 
	 * @return
	 */
	@SuppressWarnings("serial")
	protected Component createCategoryBrowser() {
		final CategoryContainer container = new CategoryContainer(
				getCategoryService());
		container.refresh();

		/*
		 * The tree will show all the categories returned from the category
		 * service.
		 */
		final Tree categoryTree = new Tree();
		categoryTree.setSizeFull();
		categoryTree.setContainerDataSource(container);
		categoryTree.setItemCaptionPropertyId("name");
		categoryTree.setImmediate(true);

		/*
		 * The toolbar will be placed at the bottom of the browser and contains
		 * buttons for refreshing the category tree, and adding, editing and
		 * removing categories.
		 */
		final HorizontalLayout toolbar = new HorizontalLayout();
		final Button refreshButton = new Button();
		refreshButton.setIcon(new ThemeResource("icons/16/refresh.png"));
		refreshButton.setStyleName("small");
		refreshButton.setDescription(getApplication().getMessage(
				"categories.refresh.descr"));
		refreshButton.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					if (logger.isDebugEnabled()) {
						logger
								.debug("Attempting to refresh the category browser");
					}
					container.refresh();
				} catch (Exception e) {
					if (logger.isErrorEnabled()) {
						logger
								.error(
										"Unexpected error while attempting to refresh the category browser",
										e);
					}
					getWindow().showNotification(
							getApplication().getMessage(
									"main.unexpectedException.title"),
							getApplication().getMessage(
									"main.unexpectedException.descr",
									e.getMessage()),
							Notification.TYPE_ERROR_MESSAGE);
				}

			}
		});
		toolbar.addComponent(refreshButton);

		final Button addButton = new Button();
		addButton.setIcon(new ThemeResource("icons/16/add.png"));
		addButton.setStyleName("small");
		addButton.setDescription(getApplication().getMessage(
				"categories.add.descr"));
		toolbar.addComponent(addButton);

		final Button editButton = new Button();
		editButton.setIcon(new ThemeResource("icons/16/pencil.png"));
		editButton.setStyleName("small");
		editButton.setDescription(getApplication().getMessage(
				"categories.edit.descr"));
		editButton.setEnabled(false);
		toolbar.addComponent(editButton);

		final Button deleteButton = new Button();
		deleteButton.setIcon(new ThemeResource("icons/16/delete.png"));
		deleteButton.setStyleName("small");
		deleteButton.setDescription(getApplication().getMessage(
				"categories.delete.descr"));
		deleteButton.setEnabled(false);
		deleteButton.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				final String selectedCategoryUUID = (String) categoryTree
						.getValue();
				if (selectedCategoryUUID != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Attempting to delete category '"
								+ selectedCategoryUUID + "'");
					}
					try {
						getCategoryService().deleteCategoryByUUID(
								selectedCategoryUUID);
						/*
						 * Remember to refresh the container, otherwise the old
						 * category will remain in the tree.
						 */
						container.refresh();
						/*
						 * Clear selection
						 */
						categoryTree.setValue(null);
					} catch (AccessDeniedException e) {
						if (logger.isDebugEnabled()) {
							logger.debug(
									"Access denied while attempting to delete category '"
											+ selectedCategoryUUID + "'", e);
						}
						getWindow()
								.showNotification(
										getApplication().getMessage(
												"main.accessDenied.title"),
										getApplication()
												.getMessage(
														"categories.delete.accessDenied.descr"),
										Notification.TYPE_WARNING_MESSAGE);
					} catch (OptimisticLockingFailureException e) {
						if (logger.isDebugEnabled()) {
							logger.debug(
									"Optimistic locking failure while attempting to delete category '"
											+ selectedCategoryUUID + "'", e);
						}
						getWindow().showNotification(
								getApplication().getMessage(
										"main.optLockFail.title"),
								getApplication().getMessage(
										"categories.delete.optLockFail.descr"),
								Notification.TYPE_WARNING_MESSAGE);
					} catch (Exception e) {
						if (logger.isErrorEnabled()) {
							logger.error(
									"Unexpected error while attempting to delete category '"
											+ selectedCategoryUUID + "'", e);
						}
						getWindow().showNotification(
								getApplication().getMessage(
										"main.unexpectedException.title"),
								getApplication().getMessage(
										"main.unexpectedException.descr",
										e.getMessage()),
								Notification.TYPE_ERROR_MESSAGE);
					}
				}

			}
		});
		toolbar.addComponent(deleteButton);

		final Button aclButton = new Button();
		aclButton.setIcon(new ThemeResource("icons/16/lock_edit.png"));
		aclButton.setStyleName("small");
		aclButton.setDescription(getApplication().getMessage(
				"categories.acl.descr"));
		aclButton.setEnabled(false);
		toolbar.addComponent(aclButton);

		final Button auditButton = new Button();
		auditButton.setIcon(new ThemeResource("icons/16/key.png"));
		auditButton.setStyleName("small");
		auditButton.setDescription(getApplication().getMessage(
				"categories.audit.descr"));
		auditButton.setEnabled(false);
		toolbar.addComponent(auditButton);

		final VerticalLayout browser = new VerticalLayout();
		browser.setSizeFull();
		browser.addComponent(categoryTree);
		browser.addComponent(toolbar);
		browser.setExpandRatio(categoryTree, 1.0f);
		browser.setComponentAlignment(toolbar, Alignment.BOTTOM_CENTER);

		// Register some listeners
		categoryTree.addListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				// Get selection
				boolean hasSelection = categoryTree.getValue() != null;
				/*
				 * In a real application, security checks should be included
				 * here as well in order to disable functions that the user is
				 * not allowed to perform. In this demo, it is possible to try
				 * to perform illegal operations just to demonstrate that the
				 * security features are working.
				 */
				editButton.setEnabled(hasSelection);
				deleteButton.setEnabled(hasSelection);
				aclButton.setEnabled(hasSelection);
				auditButton.setEnabled(hasSelection);
			}
		});

		return browser;
	}

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
