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
package com.github.peholmst.springsecuritydemo.services.impl;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.github.peholmst.springsecuritydemo.domain.Category;
import com.github.peholmst.springsecuritydemo.services.CategoryService;

/**
 * TODO Document me!
 * 
 * @author peholmst
 * 
 */
@Repository("categoryService")
public class CategoryServiceImpl implements CategoryService {

	protected final Log logger = LogFactory.getLog(getClass());

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * TODO Document me!
	 * 
	 * @return
	 */
	protected EntityManager getEntityManager() {
		return entityManager;
	}

	// TODO Fix tickets that reference the category that is removed
	
	@Override
	@Transactional
	public void deleteCategory(Category category) {
		assert category != null : "category must not be null";
		if (logger.isDebugEnabled()) {
			logger.debug("Deleting category [" + category + "]");
		}
		// Category's parent adopts the orphans
		for (Category child : getChildren(category)) {
			child.setParent(category.getParent());
		}
		getEntityManager().remove(getEntityManager().merge(category));
		getEntityManager().flush();
	}
	
	@Override
	@Transactional(readOnly = true)
	public Category getCategoryById(Long id) {
		assert id != null : "id must not be null";
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving category identified by '" + id + "'");
		}
		return getEntityManager().find(Category.class, id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Category> getChildren(Category parent) {
		assert parent != null : "parent must not be null";
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving children for category [" + parent + "]");
		}
		TypedQuery<Category> query = getEntityManager()
			.createQuery(
				"SELECT c FROM Category c WHERE c.parent = :parent ORDER BY c.name",
				Category.class);
		query.setParameter("parent", parent);
		List<Category> result = query.getResultList();
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + result.size() + " children");
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Category> getRootCategories() {
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving root categories");
		}
		TypedQuery<Category> query = getEntityManager().createQuery(
			"SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name",
			Category.class);
		List<Category> result = query.getResultList();
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + result.size() + " root categories");
		}
		return Collections.unmodifiableList(result);
	}
	
	@Override
	@Transactional
	public Category insertCategory(Category category) {
		assert category != null : "category must not be null";
		if (logger.isDebugEnabled()) {
			logger.debug("Inserting category [" + category + "]");
		}
		if (containsCategory(category)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cannot insert category [" + category + "] as it already exists");
			}
			throw new IllegalStateException("Category already exists");
		} else {
			getEntityManager().persist(category);
			return category;
		}
	}
	
	@Override
	@Transactional
	public Category updateCategory(Category category) {
		assert category != null : "category must not be null";
		if (logger.isDebugEnabled()) {
			logger.debug("Updating category [" + category + "]");
		}
		if (!containsCategory(category)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cannot update category [" + category + "] as it could not be found");
			}
			throw new DataRetrievalFailureException("Could not find category to update");
		} else {
			Category merged = getEntityManager().merge(category);
			getEntityManager().flush();
			return merged;
		}
	}

	/**
	 * TODO Document me!
	 * 
	 * @param category
	 * @return
	 */
	protected boolean containsCategory(Category category) {
		assert category != null : "category must not be null";
		if (category.getId() == null) {
			return false;
		} else {
			Query query = getEntityManager().createQuery("SELECT count(c) FROM Category c WHERE c.id = :id");
			query.setParameter("id", category.getId());
			Object result = query.getSingleResult();
			if (result instanceof Integer) {
				return ((Integer) result).intValue() == 1;
			} else {
				return ((Long) result).longValue() == 1;
			}
		}
	}

}
