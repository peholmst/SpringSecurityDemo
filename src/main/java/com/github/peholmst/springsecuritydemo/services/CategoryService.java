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

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.peholmst.springsecuritydemo.domain.Category;

/**
 * TODO Document me!
 * 
 * @author Petter Holmström
 */
public interface CategoryService {

	// TODO add & document security annotations

	/**
	 * Gets all root categories, i.e. categories that do not have parents (
	 * {@link Category#getParent()} returns <code>null</code>).
	 * 
	 * @return an unmodifiable list containing all the root categories (never
	 *         <code>null</code>).
	 */
	@PreAuthorize("hasRole('ROLE_USER')")
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
	@PreAuthorize("hasRole('ROLE_USER')")
	public List<Category> getChildren(Category parent);

	/**
	 * Updates <code>category</code> in the database.
	 * 
	 * @param category
	 *            the category to update (must not be <code>null</code>).
	 * @return the updated Category instance (never <code>null</code>).
	 * @throws DataRetrievalFailureException
	 *             if the category was not found in the database.
	 * @throws OptimisticLockingFailureException
	 *             if the category has been altered by another user after it was
	 *             retrieved from the database.
	 */
	@PreAuthorize("hasRole('ROLE_USER')")
	public Category updateCategory(Category category)
			throws DataRetrievalFailureException,
			OptimisticLockingFailureException;

	/**
	 * Inserts <code>category</code> into the database.
	 * 
	 * @param category
	 *            the category to insert (must not be <code>null</code>).
	 * @return the inserted Category instance (never <code>null</code>).
	 * @throws IllegalStateException
	 *             if the category already exists in the database.
	 */
	@PreAuthorize("hasRole('ROLE_USER')")
	public Category insertCategory(Category category)
			throws IllegalStateException;

	/**
	 * Deletes <code>category</code>. If <code>category</code> has children,
	 * they are adopted by the parent of <code>category</code>. If
	 * <code>category</code> is a root and has children, they will become new
	 * roots. If <code>category</code> cannot be found, nothing happens.
	 * 
	 * @param category
	 *            the category to delete (must not be <code>null</code>).
	 * @throws OptimisticLockingFailureException
	 *             if the category has been altered by another user after it was
	 *             retrieved from the database.
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void deleteCategory(Category category)
			throws OptimisticLockingFailureException;

	/**
	 * Gets the category identified by <code>id</code>.
	 * 
	 * @param id
	 *            the id of the category (must not be <code>null</code>).
	 * @return the category, or <code>null</code> if not found.
	 */
	@PreAuthorize("hasRole('ROLE_USER')")
	public Category getCategoryById(Long id);
}
