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
package com.github.peholmst.springsecuritydemo.services;

import java.util.List;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.peholmst.springsecuritydemo.domain.Category;

/**
 * TODO Document me!
 * 
 * @author Petter Holmström
 */
public interface CategoryService {

	// TODO document the security annotations

	/**
	 * Gets all root categories, i.e. categories that do not have parents (
	 * {@link Category#getParent()} returns <code>null</code>).
	 * 
	 * @return an unmodifiable list containing all the root categories (never
	 *         <code>null</code>).
	 */
	@PreAuthorize("hasRole('ROLE_USER'")
	@PostFilter("hasPermission(filterObject, 'read')")
	public List<Category> getRootCategories();

	/**
	 * Gets all the children of <code>parent</code>.
	 * 
	 * @param parent
	 *            the parent whose children should be fetched (must not be
	 *            <code>null</code>).
	 * @return an unmodifiable list containing all the children of
	 *         <code>parent</code>, or <code>null</code> if <code>parent</code>
	 *         could not be found.
	 */
	@PreAuthorize("hasRole('ROLE_USER') and hasPermission(#parent, 'read')")
	@PostFilter("hasPermission(filterObject, 'read'")
	public List<Category> getChildren(Category parent);

	/**
	 * Saves <code>category</code>.
	 * 
	 * @param category
	 *            the category to save (must not be <code>null</code>).
	 * @return the saved Category instance (never <code>null</code>).
	 */
	public Category saveCategory(Category category);

	/**
	 * Deletes <code>category</code>. If <code>category</code> has children,
	 * they are adopted by the parent of <code>category</code>. If
	 * <code>category</code> is a root and has children, they will become new
	 * roots. If <code>category</code> cannot be found, nothing happens.
	 * 
	 * @param category
	 *            the category to delete (must not be <code>null</code>).
	 */
	public void deleteCategory(Category category);

	/**
	 * Deletes the category identified by <code>uuid</code>. If the category has
	 * children, they are adopted by the parent of the category. If the category
	 * is a root and has children, they will become new roots. If no category is
	 * found, nothing happens.
	 * 
	 * @param uuid
	 *            the UUID of the category to delete (must not be
	 *            <code>null</code>).
	 */
	public void deleteCategoryByUUID(String uuid);

	/**
	 * Gets the category identified by <code>uuid</code>.
	 * 
	 * @param uuid
	 *            the UUID of the category (must not be <code>null</code>).
	 * @return the category, or <code>null</code> if not found.
	 */
	public Category getCategoryByUUID(String uuid);
}
