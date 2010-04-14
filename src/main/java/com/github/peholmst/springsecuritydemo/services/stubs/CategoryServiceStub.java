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
package com.github.peholmst.springsecuritydemo.services.stubs;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.DataRetrievalFailureException;

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
	
	protected long nextId = 1;

	HashMap<Long, CategoryEntry> categories = new HashMap<Long, CategoryEntry>();
	// We need this map to be able to detect if the parent has changed when a
	// Category is saved.
	HashMap<Long, Category> categoryToParentMap = new HashMap<Long, Category>();

	static Category NULL_CATEGORY = new Category() {
		@Override
		public String getName() {
			return "NULL";
		}
		public Long getId() {
			return 0l;
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
		categories.put(NULL_CATEGORY.getId(), new CategoryEntry(NULL_CATEGORY));
	}
	
	@Override
	public void deleteCategory(Category category) {
		// Lookup entry from map
		CategoryEntry entry = categories.get(category.getId());
		if (entry != null) {
			CategoryEntry newParentEntry = null;
			if (category.getParent() != null) {
				newParentEntry = categories.get(category.getParent().getId());
			} else {
				newParentEntry = categories.get(NULL_CATEGORY.getId());
			}
			// Move all children to new parent
			for (Category child : entry.children) {
				child.setParent(category.getParent());
				newParentEntry.children.add(child);
				categoryToParentMap.put(child.getId(), newParentEntry.category);
			}
			if (category.getParent() == null) {
				categories.get(NULL_CATEGORY.getId()).children.remove(category);
			} else {
				categories.get(category.getParent().getId()).children.remove(category);
			}
			categories.remove(category.getId());
			categoryToParentMap.remove(category.getId());
		}
	}

	@Override
	public List<Category> getChildren(Category parent) {
		// Lookup entry from map
		CategoryEntry entry = categories.get(parent.getId());
		if (entry == null) {
			return null; // Parent not found
		} else {
			return Collections.unmodifiableList(entry.children);
		}
	}

	@Override
	public List<Category> getRootCategories() {
		// Lookup entry from map
		CategoryEntry entry = categories.get(NULL_CATEGORY.getId());
		return Collections.unmodifiableList(entry.children);
	}
	
	@Override
	public Category insertCategory(Category category) {
		if (category.getId() != null && categories.containsKey(category.getId())) {
			throw new IllegalStateException("Category already exists");
		}
		// Check that parent property points to a valid category
		if (category.getParent() != null
				&& !categories.containsKey(category.getParent().getId())) {
			throw new IllegalArgumentException("Invalid parent property");
		}		
		
		// Generate ID and save in map
		category.setId(nextId++);
		CategoryEntry entry = new CategoryEntry(category);
		categories.put(category.getId(), entry);
		
		// Update parent index
		Category parent = category.getParent();
		if (parent == null) {
			parent = NULL_CATEGORY;
		}
		
		categories.get(parent.getId()).children.add(category);
		categoryToParentMap.put(category.getId(), parent);
		
		return category;
	}
	
	@Override
	public Category updateCategory(Category category) {
		if (category.getId() == null || !categories.containsKey(category.getId())) {
			throw new DataRetrievalFailureException("Category could not be found");
		}
		// Check that parent property points to a valid category
		if (category.getParent() != null
				&& !categories.containsKey(category.getParent().getId())) {
			throw new IllegalArgumentException("Invalid parent property");
		}

		Category parent = category.getParent();
		if (parent == null) {
			parent = NULL_CATEGORY;
		}		
		
		// Remove reference to old parent if the parent property has changed
		Category oldParent = categoryToParentMap.get(category.getId());
		if (oldParent != null && !oldParent.equals(parent)) {
			categories.get(oldParent.getId()).children.remove(category);
			categoryToParentMap.remove(category.getId());
		}

		// Update the parent index if the parent property has changed
		if (!parent.equals(oldParent)) {
			categories.get(parent.getId()).children.add(category);
			categoryToParentMap.put(category.getId(), parent);
		}
		return category;	
	}
	
	@Override
	public Category getCategoryById(Long id) {
		CategoryEntry entry = categories.get(id);
		return entry == null ? null : entry.category;
	}
}
