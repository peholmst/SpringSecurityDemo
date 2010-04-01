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
package com.github.peholmst.springsecuritydemo.services.stubs;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.springframework.util.ObjectUtils;

import com.github.peholmst.springsecuritydemo.domain.Category;
import com.github.peholmst.springsecuritydemo.services.CategoryService;

/**
 * A stub implementation of {@link CategoryService} that can be used for unit
 * tests and user interface design.
 * 
 * @author Petter Holmström
 */
@SuppressWarnings("serial")
public class CategoryServiceStub implements CategoryService {

	// TODO Add test for this class!
	
	static class CategoryEntry {
		final Category category;
		final LinkedList<Category> children = new LinkedList<Category>();

		public CategoryEntry(Category category) {
			this.category = category;
		}
	}

	HashMap<Category, CategoryEntry> categories = new HashMap<Category, CategoryEntry>();
	// We need this map to be able to detect if the parent has changed when a
	// Category is saved.
	HashMap<Category, Category> categoryToParentMap = new HashMap<Category, Category>();

	static Category NULL_CATEGORY = new Category() {
		@Override
		public String getName() {
			return "NULL";
		}
	};

	public CategoryServiceStub() {
		categories.put(NULL_CATEGORY, new CategoryEntry(NULL_CATEGORY));
	}
	
	@Override
	public void deleteCategory(Category category) {
		// Lookup entry from map
		CategoryEntry entry = categories.get(category);
		if (entry != null) {
			CategoryEntry newParentEntry = null;
			if (category.getParent() != null) {
				newParentEntry = categories.get(category.getParent());
			} else {
				newParentEntry = categories.get(NULL_CATEGORY);
			}
			// Move all children to new parent
			for (Category child : entry.children) {
				child.setParent(category.getParent());
				newParentEntry.children.add(child);
				categoryToParentMap.put(child, newParentEntry.category);
			}
			categories.remove(category);
			categoryToParentMap.remove(category);
		}
	}

	@Override
	public List<Category> getChildren(Category parent) {
		// Lookup entry from map
		CategoryEntry entry = categories.get(parent);
		if (entry == null) {
			return null; // Parent not found
		} else {
			return Collections.unmodifiableList(entry.children);
		}
	}

	@Override
	public List<Category> getRootCategories() {
		// Lookup entry from map
		CategoryEntry entry = categories.get(NULL_CATEGORY);
		if (entry == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(entry.children);
		}
	}

	@Override
	public Category saveCategory(Category category) {
		// Check if the category has already been saved once?
		CategoryEntry entry = categories.get(category);
		if (entry == null) {
			// New category
			entry = new CategoryEntry(category);
			categories.put(category, entry);
		}

		// Check that parent property points to a valid category
		if (category.getParent() != null
				&& !categories.containsKey(category.getParent())) {
			throw new IllegalArgumentException("Invalid parent property");
		}

		// Check if the parent property has changed
		Category oldParent = categoryToParentMap.get(category);
		if (oldParent != null && !ObjectUtils.nullSafeEquals(oldParent, category.getParent())) {
			categories.get(oldParent).children.remove(category);
			categoryToParentMap.remove(category);
		}

		// Update the parent index
		Category parent = category.getParent();
		if (parent == null) {
			parent = NULL_CATEGORY;
		}
		categories.get(parent).children.add(category);
		categoryToParentMap.put(category, parent);

		return category;
	}

}
