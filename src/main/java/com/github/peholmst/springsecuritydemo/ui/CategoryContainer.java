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

import java.util.Collection;
import java.util.List;

import com.github.peholmst.springsecuritydemo.domain.Category;
import com.github.peholmst.springsecuritydemo.services.CategoryService;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * 
 * @author Petter Holmström
 */
public class CategoryContainer implements Container.Hierarchical {

	private static final long serialVersionUID = 1197578205205304787L;

	private transient final CategoryService categoryService;
	
	public CategoryContainer(CategoryService categoryService) {
		assert categoryService != null : "categoryService must not be null";
		this.categoryService = categoryService;
	}
	
	public CategoryService getCategoryService() {
		return categoryService;
	}
	
	private List<Category> rootCategories;
	
	
	
	@Override
	public boolean areChildrenAllowed(Object itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<?> getChildren(Object itemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getParent(Object itemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRoot(Object itemId) {
		return rootItemIds().contains(itemId);
	}

	@Override
	public boolean removeItem(Object itemId)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Category> rootItemIds() {
		return getCategoryService().getRootCategories();
	}

	@Override
	public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setParent(Object itemId, Object newParentId)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addContainerProperty(Object propertyId, Class<?> type,
			Object defaultValue) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object addItem() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item addItem(Object itemId) throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsId(Object itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item getItem(Object itemId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<?> getItemIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getType(Object propertyId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAllItems() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeContainerProperty(Object propertyId)
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
