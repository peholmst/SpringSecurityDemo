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

	/**
	 * 
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_USER'")
	@PostFilter("hasPermission(filterObject, 'read')")
	public List<Category> getRootCategories();
	
	/**
	 * 
	 * @param parent
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_USER') and hasPermission(#parent, 'read')")
	public List<Category> getChildren(Category parent);
		
}
