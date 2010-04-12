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

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;

import com.github.peholmst.springsecuritydemo.domain.Category;
import com.github.peholmst.springsecuritydemo.services.CategoryService;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItem;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;

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
public final class CategoryBrowser {

	/*
	 * Note: I'm sure there are nicer ways of implementing the category browser,
	 * but at least the implementation is hidden inside this class and can be
	 * changed without affecting the rest of the application.
	 */

	/**
	 * Listener interface to be implemented by classes that want to know when
	 * the currently selected category is changed.
	 * 
	 * @see CategoryBrowser#addListener(CategorySelectionListener)
	 * @see CategoryBrowser#removeListener(CategorySelectionListener)
	 * 
	 * @author Petter Holmström
	 */
	public static interface CategorySelectionListener {
		/**
		 * Notifies the listener that the currently selected category is
		 * <code>newCategory</code>.
		 * 
		 * @param newCategory
		 *            the selected category, or <code>null</code> if no category
		 *            is selected at all.
		 */
		public void selectedCategoryChanged(Category newCategory);
	}

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

	private LinkedList<CategorySelectionListener> listeners;

	/**
	 * Adds <code>listener</code> to the list of listeners to be notified when
	 * the category selection is changed. If the listener is <code>null</code>,
	 * nothing happens.
	 * 
	 * @param listener
	 *            the listener to add.
	 */
	public void addListener(CategorySelectionListener listener) {
		if (listener == null) {
			return;
		}
		if (listeners == null) {
			listeners = new LinkedList<CategorySelectionListener>();
		}
		listeners.add(listener);
	}

	/**
	 * Removes <code>listener</code> from the list of listeners. If the listener
	 * is <code>null</code> or was never added, nothing happens.
	 * 
	 * @param listener
	 *            the listener to remove.
	 */
	public void removeListener(CategorySelectionListener listener) {
		if (listener == null || listeners == null) {
			return;
		}
		listeners.remove(listener);
		if (listeners.isEmpty()) {
			listeners = null;
		}
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
	private CategoryForm categoryForm;

	private static String[] visibleCategoryFormPropertyes = { "name",
			"description" };

	/**
	 * This inner class defines a form that can be used to edit existing
	 * Categories and add new ones.
	 */
	private final class CategoryForm {

		private static final long serialVersionUID = -3572755537037975014L;
		private Form form;
		private VerticalLayout layout;
		private Category category;
		private boolean isNew;

		public CategoryForm() {
			layout = new VerticalLayout();
			layout.setVisible(false);
			layout.setMargin(true);
		}

		@SuppressWarnings("serial")
		private void createForm() {
			layout.removeAllComponents();

			form = new Form();
			form.setImmediate(true);
			form.setCaption(getI18nProvider().getMessage(
				"categories.form.caption"));
			form.setWriteThrough(false);
			form.setInvalidCommitted(false);

			form.setFormFieldFactory(new DefaultFieldFactory() {
				public Field createField(Item item, Object propertyId,
						Component uiContext) {
					Field field = super
						.createField(item, propertyId, uiContext);
					field.setWidth("100%");
					if ("name".equals(propertyId)) {
						field.setRequired(true);
						field.setRequiredError(getI18nProvider().getMessage(
							"categories.form.nameEmpty"));
					}
					return field;
				};
			});

			final HorizontalLayout buttons = new HorizontalLayout();
			buttons.setSpacing(true);

			final Button saveButton = new Button("Save",
				new Button.ClickListener() {

					@Override
					public void buttonClick(ClickEvent event) {
						/*
						 * First, we have to commit the form. This will validate
						 * the data and update the underlying category instance
						 * upon success.
						 */
						try {
							form.commit();
						} catch (Exception e) {
							/*
							 * The form contains errors, so we abort here and
							 * let the user fix them. The form will take care of
							 * displaying the error messages to the user.
							 */
							return;
						}
						/*
						 * We now have a category instance with the updated
						 * data, so now we have to save it using the category
						 * service.
						 */
						try {
							category = getCategoryService().saveCategory(category);
							/*
							 * Remember to refresh the container, otherwise the
							 * new/updated category won't show up in the tree.
							 */
							categoryContainer.refresh();
							/*
							 * Update selection, will update the enablement
							 * state
							 */
							categoryTree.setValue(category.getId());

							setVisible(false);
							getComponent().getWindow().showNotification(
								getI18nProvider().getMessage(
									"categories.categorySaved"),
								Notification.TYPE_TRAY_NOTIFICATION);
						} catch (AccessDeniedException e) {
							if (logger.isDebugEnabled()) {
								logger.debug(
									"Access denied while attempting to save category ["
											+ category + "]", e);
							}
							if (isNew) {
								ExceptionUtils.handleException(getComponent()
									.getWindow(), e, getI18nProvider()
									.getMessage(
										"categories.add.accessDenied.descr"));
							} else {
								ExceptionUtils.handleException(getComponent()
									.getWindow(), e, getI18nProvider()
									.getMessage(
										"categories.save.accessDenied.descr"));
							}
						} catch (OptimisticLockingFailureException e) {
							if (logger.isDebugEnabled()) {
								logger.debug(
									"Optimistic locking failure while attempting to save category ["
											+ category + "]", e);
							}
							ExceptionUtils.handleException(getComponent()
								.getWindow(), e, getI18nProvider().getMessage(
								"categories.save.optLockFail.descr"));
						} catch (Exception e) {
							if (logger.isErrorEnabled()) {
								logger.error(
									"Error while attempting to save category ["
											+ category + "]", e);
							}
							ExceptionUtils.handleException(getComponent()
								.getWindow(), e);
						}
						/*
						 * Data integrity violation should never occur.
						 */
					}
				});
			saveButton.setStyleName("primary");
			buttons.addComponent(saveButton);

			final Button cancelButton = new Button("Cancel",
				new Button.ClickListener() {

					@Override
					public void buttonClick(ClickEvent event) {
						form.discard();
						setVisible(false);
					}
				});
			buttons.addComponent(cancelButton);

			layout.addComponent(form);
			layout.addComponent(buttons);
			layout.setExpandRatio(form, 1.0f);
			layout.setComponentAlignment(buttons, Alignment.BOTTOM_RIGHT);
		}

		/**
		 * Gets the category form component that can be used in user interfaces.
		 * 
		 * @return a layout containing the form and buttons.
		 */
		public Component getComponent() {
			return layout;
		}

		/**
		 * Shows the form and binds it to <code>category</code>.
		 * 
		 * @param category
		 *            the category to edit.
		 */
		public void editCategory(Category category) {
			createForm();
			this.category = category;
			isNew = false;
			form.setItemDataSource(new BeanItem<Category>(category));
			form.setVisibleItemProperties(visibleCategoryFormPropertyes);
			setVisible(true);
		}

		/**
		 * Shows the form and binds it to a newly created category.
		 * 
		 * @param parent
		 *            the parent of the category, <code>null</code> to make a
		 *            new root category.
		 */
		public void newCategory(Category parent) {
			editCategory(new Category());
			this.category.setParent(parent);
			isNew = true;
		}

		/**
		 * Shows or hides the form.
		 * 
		 * @param visible
		 *            <code>true</code> to show the form, <code>false</code> to
		 *            hide it.
		 */
		public void setVisible(boolean visible) {
			getComponent().setVisible(visible);
		}
	}

	/**
	 * Refreshes the category browser.
	 */
	private void actionRefresh() {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Attempting to refresh the category browser");
			}
			categoryContainer.refresh();
			updateEnablementState();
			getComponent().getWindow().showNotification(
				getI18nProvider().getMessage("categories.browserRefreshed"),
				Notification.TYPE_TRAY_NOTIFICATION);
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger
					.error(
						"Error while attempting to refresh the category browser",
						e);
			}
			ExceptionUtils.handleException(getComponent().getWindow(), e);
		}
	}

	/**
	 * Edits the selected category. If no category is selected, this method does
	 * nothing.
	 */
	private void actionEdit() {
		final Long selectedCategoryId = (Long) categoryTree.getValue();
		if (selectedCategoryId != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Attempting to show edit form for category '"
						+ selectedCategoryId + "'");
			}
			BeanItem<Category> categoryItem = categoryContainer
				.getItem(selectedCategoryId);
			if (categoryItem != null) {
				categoryForm.editCategory(categoryItem.getBean());
			} else {
				actionRefresh();
			}
		}
	}

	/**
	 * Adds a new sub category to the selected category. If no category is
	 * selected, a new root category is created.
	 */
	private void actionAdd() {
		final Long selectedCategoryId = (Long) categoryTree.getValue();
		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to show edit form for a new category");
		}
		if (selectedCategoryId == null) {
			// new root item
			categoryForm.newCategory(null);
		} else {
			BeanItem<Category> categoryItem = categoryContainer
				.getItem(selectedCategoryId);
			if (categoryItem != null) {
				categoryForm.newCategory(categoryItem.getBean());
			} else {
				actionRefresh();
			}
		}
	}

	/**
	 * Deletes the selected category. If no category is selected, this method
	 * does nothing.
	 */
	private void actionDelete() {
		final Long selectedCategoryId = (Long) categoryTree.getValue();
		if (selectedCategoryId != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Attempting to delete category '"
						+ selectedCategoryId + "'");
			}
			try {
				Category c = getCategoryService().getCategoryById(selectedCategoryId);
				if (c != null) {
					getCategoryService().deleteCategory(c);
				}
				/*
				 * Remember to refresh the container, otherwise the old category
				 * will remain in the tree.
				 */
				categoryContainer.refresh();
				/*
				 * Clear selection, will update the enablement state
				 */
				categoryTree.setValue(null);
				getComponent().getWindow().showNotification(
					getI18nProvider().getMessage("categories.categoryDeleted"),
					Notification.TYPE_TRAY_NOTIFICATION);
			} catch (AccessDeniedException e) {
				if (logger.isDebugEnabled()) {
					logger.debug(
						"Access denied while attempting to delete category '"
								+ selectedCategoryId + "'", e);
				}
				ExceptionUtils.handleException(getComponent().getWindow(), e,
					getI18nProvider().getMessage(
						"categories.delete.accessDenied.descr"));
			} catch (OptimisticLockingFailureException e) {
				if (logger.isDebugEnabled()) {
					logger.debug(
						"Optimistic locking failure while attempting to delete category '"
								+ selectedCategoryId + "'", e);
				}
				ExceptionUtils.handleException(getComponent().getWindow(), e,
					getI18nProvider().getMessage(
						"categories.delete.optLockFail.descr"));
			} catch (Exception e) {
				if (logger.isErrorEnabled()) {
					logger.error("Error while attempting to delete category '"
							+ selectedCategoryId + "'", e);
				}
				ExceptionUtils.handleException(getComponent().getWindow(), e);
			}
			/*
			 * Data integrity violation should never occur.
			 */
		}
	}

	/**
	 * Shows the access control list for the selected category. If no category
	 * is selected, this method does nothing.
	 */
	private void actionACL() {
		// TODO Implement me!
		getComponent().getWindow().showNotification("Not implemented yet!");
	}

	/**
	 * Shows the audit log for the selected category. If no category is
	 * selected, this method does nothing.
	 */
	private void actionAudit() {
		// TODO Implement me!
		getComponent().getWindow().showNotification("Not implemented yet!");
	}

	/**
	 * Updates the enablement state of the toolbar buttons and hides the
	 * category form.
	 */
	private void updateEnablementState() {
		// Get selection
		boolean hasSelection = categoryTree.getValue() != null;
		/*
		 * In a real application, security checks should be included here as
		 * well in order to disable functions that the user is not allowed to
		 * perform. In this demo, it is possible to try to perform illegal
		 * operations just to demonstrate that the security features are
		 * working.
		 */
		editButton.setEnabled(hasSelection);
		deleteButton.setEnabled(hasSelection);
		aclButton.setEnabled(hasSelection);
		auditButton.setEnabled(hasSelection);
		categoryForm.setVisible(false);
	}

	/**
	 * Creates the category browser component.
	 */
	@SuppressWarnings("serial")
	private void createComponent() {
		categoryContainer = new CategoryContainer(getCategoryService());
		categoryContainer.refresh();

		/*
		 * The tree will show all the categories returned from the category
		 * service.
		 */
		categoryTree = new Tree();
		{
			categoryTree.setSizeFull();
			categoryTree.setContainerDataSource(categoryContainer);
			categoryTree.setItemCaptionPropertyId("name");
			categoryTree.setImmediate(true);
		}

		/*
		 * The form for editing categories is hidden by default and is shown
		 * when the user clicks the edit or add button.
		 */
		categoryForm = new CategoryForm();

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
					actionRefresh();
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
					actionAdd();
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
					actionEdit();
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
					actionDelete();
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
					actionACL();
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
					actionAudit();
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
		browserComponent.addComponent(categoryForm.getComponent());
		browserComponent.addComponent(toolbar);
		browserComponent.setExpandRatio(categoryTree, 1.0f);
		browserComponent
			.setComponentAlignment(toolbar, Alignment.BOTTOM_CENTER);

		/*
		 * Register a listener that updates the enablement state every time the
		 * selection changes.
		 */
		categoryTree.addListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				updateEnablementState();
				Long newCategoryId = (Long) event.getProperty()
					.getValue();
				Category newCategory = null;
				if (newCategoryId != null) {
					BeanItem<Category> item = categoryContainer
						.getItem(newCategoryId);
					if (item != null) {
						newCategory = item.getBean();
					}
				}
				fireCategorySelectionChanged(newCategory);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void fireCategorySelectionChanged(Category newCategory) {
		if (listeners == null) {
			return;
		}
		/*
		 * Iterate over a cloned list instead of the list itself to avoid
		 * strange behavior if any of the listeners add additional listeners or
		 * remove existing ones.
		 */
		LinkedList<CategorySelectionListener> clonedList = (LinkedList<CategorySelectionListener>) listeners
			.clone();
		for (CategorySelectionListener listener : clonedList) {
			listener.selectedCategoryChanged(newCategory);
		}

	}

}
