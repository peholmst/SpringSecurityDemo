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
	
	static class CategoryEntry {
		final Category category;
		final LinkedList<Category> children = new LinkedList<Category>();

		public CategoryEntry(Category category) {
			this.category = category;
		}
	}

	HashMap<String, CategoryEntry> categories = new HashMap<String, CategoryEntry>();
	// We need this map to be able to detect if the parent has changed when a
	// Category is saved.
	HashMap<String, Category> categoryToParentMap = new HashMap<String, Category>();

	static Category NULL_CATEGORY = new Category() {
		@Override
		public String getName() {
			return "NULL";
		}
		public String getUUID() {
			return "NULL";
		};
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			return obj.getClass() == getClass();
		};
		public int hashCode() {
			return getClass().hashCode();
		};
	};

	public CategoryServiceStub() {
		categories.put(NULL_CATEGORY.getUUID(), new CategoryEntry(NULL_CATEGORY));
	}
	
	@Override
	public void deleteCategory(Category category) {
		// Lookup entry from map
		CategoryEntry entry = categories.get(category.getUUID());
		if (entry != null) {
			CategoryEntry newParentEntry = null;
			if (category.getParent() != null) {
				newParentEntry = categories.get(category.getParent().getUUID());
			} else {
				newParentEntry = categories.get(NULL_CATEGORY.getUUID());
			}
			// Move all children to new parent
			for (Category child : entry.children) {
				child.setParent(category.getParent());
				newParentEntry.children.add(child);
				categoryToParentMap.put(child.getUUID(), newParentEntry.category);
			}
			if (category.getParent() == null) {
				categories.get(NULL_CATEGORY.getUUID()).children.remove(category);
			} else {
				categories.get(category.getParent().getUUID()).children.remove(category);
			}
			categories.remove(category.getUUID());
			categoryToParentMap.remove(category.getUUID());
		}
	}

	@Override
	public List<Category> getChildren(Category parent) {
		// Lookup entry from map
		CategoryEntry entry = categories.get(parent.getUUID());
		if (entry == null) {
			return null; // Parent not found
		} else {
			return Collections.unmodifiableList(entry.children);
		}
	}

	@Override
	public List<Category> getRootCategories() {
		// Lookup entry from map
		CategoryEntry entry = categories.get(NULL_CATEGORY.getUUID());
		return Collections.unmodifiableList(entry.children);
	}

	@Override
	public Category saveCategory(Category category) {
		// Check if the category has already been saved once?
		CategoryEntry entry = categories.get(category.getUUID());
		if (entry == null) {
			// New category
			entry = new CategoryEntry(category);
			categories.put(category.getUUID(), entry);
		}

		// Check that parent property points to a valid category
		if (category.getParent() != null
				&& !categories.containsKey(category.getParent().getUUID())) {
			throw new IllegalArgumentException("Invalid parent property");
		}

		// Check if the parent property has changed
		Category oldParent = categoryToParentMap.get(category.getUUID());
		if (oldParent != null && !oldParent.equals(category.getParent())) {
			categories.get(oldParent.getUUID()).children.remove(category);
			categoryToParentMap.remove(category.getUUID());
		}

		// Update the parent index
		Category parent = category.getParent();
		if (parent == null) {
			parent = NULL_CATEGORY;
		}
		categories.get(parent.getUUID()).children.add(category);
		categoryToParentMap.put(category.getUUID(), parent);

		return category;
	}
	
	@Override
	public Category getCategoryByUUID(String uuid) {
		CategoryEntry entry = categories.get(uuid);
		return entry == null ? null : entry.category;
	}

	@Override
	public void deleteCategoryByUUID(String uuid) {
		Category c = getCategoryByUUID(uuid);
		if (c != null) {
			deleteCategory(c);
		}		
	}
}
