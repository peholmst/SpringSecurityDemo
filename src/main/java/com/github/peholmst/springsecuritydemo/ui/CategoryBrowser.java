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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;

import com.github.peholmst.springsecuritydemo.services.CategoryService;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * This class implements the category browser. Features:
 * <ul>
 * <li>Localized user interface</li>
 * <li>Show categories in a tree</li>
 * <li>Add, edit and remove categories</li>
 * <li>Modify access control lists for categories</li>
 * <li>Show audit log for categories</li>
 * <li>Notifies listeners when the current category is changed</li>
 * </ul>
 * The actual category browser component is returned by {@link #getComponent()}.
 * 
 * @author Petter Holmström
 */
public class CategoryBrowser {

	/**
	 * Apache Commons logger for logging stuff.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	private final CategoryService categoryService;

	private final I18nProvider i18nProvider;

	/**
	 * Creates a new <code>CategoryBrowser</code>.
	 * 
	 * @param categoryService
	 *            the category service to use for fetching and modifying
	 *            categories (must not be <code>null</code>).
	 * @param i18nProvider
	 *            the i18n provider to use for fetching localized strings (must
	 *            not be <code>null</code>).
	 */
	public CategoryBrowser(CategoryService categoryService,
			I18nProvider i18nProvider) {
		assert categoryService != null : "categoryService must not be null";
		assert i18nProvider != null : "i18nProvider must not be null";
		this.categoryService = categoryService;
		this.i18nProvider = i18nProvider;
		createComponent();
	}

	/**
	 * Gets the category service that should be used when retrieving or
	 * modifying categories.
	 * 
	 * @return the category service (never <code>null</code>).
	 */
	protected CategoryService getCategoryService() {
		return categoryService;
	}

	/**
	 * Gets the i18n provider that should be used to fetch localized strings.
	 * 
	 * @return the i18n provider (never <code>null</code>).
	 */
	protected I18nProvider getI18nProvider() {
		return i18nProvider;
	}

	/**
	 * Gets the category browser component that can be used in user interfaces.
	 * 
	 * @return the category browser component (never <code>null</code>).
	 */
	public Component getComponent() {
		return browserComponent;
	}

	private Button refreshButton;
	private Button addButton;
	private Button editButton;
	private Button deleteButton;
	private Button aclButton;
	private Button auditButton;
	private CategoryContainer categoryContainer;
	private Tree categoryTree;
	private VerticalLayout browserComponent;

	@SuppressWarnings("serial")
	private void createComponent() {
		categoryContainer = new CategoryContainer(getCategoryService());
		categoryContainer.refresh();

		/*
		 * The tree will show all the categories returned from the category
		 * service.
		 */
		categoryTree = new Tree();
		categoryTree.setSizeFull();
		categoryTree.setContainerDataSource(categoryContainer);
		categoryTree.setItemCaptionPropertyId("name");
		categoryTree.setImmediate(true);

		/*
		 * The toolbar will be placed at the bottom of the browser and contains
		 * buttons for refreshing the category tree, and adding, editing and
		 * removing categories.
		 */
		final HorizontalLayout toolbar = new HorizontalLayout();
		{
			/*
			 * Button: Refresh the category browser
			 */
			refreshButton = new Button();
			refreshButton.setIcon(new ThemeResource("icons/16/refresh.png"));
			refreshButton.setStyleName("small");
			refreshButton.setDescription(getI18nProvider().getMessage(
					"categories.refresh.descr"));
			refreshButton.addListener(new Button.ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					try {
						if (logger.isDebugEnabled()) {
							logger
									.debug("Attempting to refresh the category browser");
						}
						categoryContainer.refresh();
					} catch (Exception e) {
						if (logger.isErrorEnabled()) {
							logger
									.error(
											"Error while attempting to refresh the category browser",
											e);
						}
						ExceptionUtils.handleException(getComponent()
								.getWindow(), e);
					}
				}
			});
			toolbar.addComponent(refreshButton);

			/*
			 * Button: Add a new category. The currently selected category will
			 * be used as parent.
			 */
			addButton = new Button();
			addButton.setIcon(new ThemeResource("icons/16/add.png"));
			addButton.setStyleName("small");
			addButton.setDescription(getI18nProvider().getMessage(
					"categories.add.descr"));
			addButton.addListener(new Button.ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					// TODO Implement me!
					getComponent().getWindow().showNotification(
							"Not implemented yet!");
				}
			});
			toolbar.addComponent(addButton);

			/*
			 * Button: Edit the selected category
			 */
			editButton = new Button();
			editButton.setIcon(new ThemeResource("icons/16/pencil.png"));
			editButton.setStyleName("small");
			editButton.setDescription(getI18nProvider().getMessage(
					"categories.edit.descr"));
			editButton.setEnabled(false);
			editButton.addListener(new Button.ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					// TODO Implement me!
					getComponent().getWindow().showNotification(
							"Not implemented yet!");
				}
			});
			toolbar.addComponent(editButton);

			/*
			 * Button: Delete the selected category
			 */
			deleteButton = new Button();
			deleteButton.setIcon(new ThemeResource("icons/16/delete.png"));
			deleteButton.setStyleName("small");
			deleteButton.setDescription(getI18nProvider().getMessage(
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
							 * Remember to refresh the container, otherwise the
							 * old category will remain in the tree.
							 */
							categoryContainer.refresh();
							/*
							 * Clear selection
							 */
							categoryTree.setValue(null);
						} catch (AccessDeniedException e) {
							if (logger.isDebugEnabled()) {
								logger
										.debug(
												"Access denied while attempting to delete category '"
														+ selectedCategoryUUID
														+ "'", e);
							}
							ExceptionUtils
									.handleException(
											getComponent().getWindow(),
											e,
											getI18nProvider()
													.getMessage(
															"categories.delete.accessDenied.descr"));
						} catch (OptimisticLockingFailureException e) {
							if (logger.isDebugEnabled()) {
								logger
										.debug(
												"Optimistic locking failure while attempting to delete category '"
														+ selectedCategoryUUID
														+ "'", e);
							}
							ExceptionUtils
									.handleException(
											getComponent().getWindow(),
											e,
											getI18nProvider()
													.getMessage(
															"categories.delete.optLockFail.descr"));
						} catch (Exception e) {
							if (logger.isErrorEnabled()) {
								logger
										.error(
												"Error while attempting to delete category '"
														+ selectedCategoryUUID
														+ "'", e);
							}
							ExceptionUtils.handleException(getComponent()
									.getWindow(), e);
						}
					}

				}
			});
			toolbar.addComponent(deleteButton);

			/*
			 * Button: Show/edit the access control list for the selected
			 * category
			 */
			aclButton = new Button();
			aclButton.setIcon(new ThemeResource("icons/16/lock_edit.png"));
			aclButton.setStyleName("small");
			aclButton.setDescription(getI18nProvider().getMessage(
					"categories.acl.descr"));
			aclButton.setEnabled(false);
			aclButton.addListener(new Button.ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					// TODO Implement me!
					getComponent().getWindow().showNotification(
							"Not implemented yet!");
				}
			});
			toolbar.addComponent(aclButton);

			/*
			 * Button: Show the audit log for the selected category
			 */
			auditButton = new Button();
			auditButton.setIcon(new ThemeResource("icons/16/key.png"));
			auditButton.setStyleName("small");
			auditButton.setDescription(getI18nProvider().getMessage(
					"categories.audit.descr"));
			auditButton.setEnabled(false);
			auditButton.addListener(new Button.ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					// TODO Implement me!
					getComponent().getWindow().showNotification(
							"Not implemented yet!");
				}
			});
			toolbar.addComponent(auditButton);
		}

		/*
		 * The browser layout contains the category tree and the toolbar.
		 */
		browserComponent = new VerticalLayout();
		browserComponent.setSizeFull();
		browserComponent.addComponent(categoryTree);
		browserComponent.addComponent(toolbar);
		browserComponent.setExpandRatio(categoryTree, 1.0f);
		browserComponent
				.setComponentAlignment(toolbar, Alignment.BOTTOM_CENTER);

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
	}

}
