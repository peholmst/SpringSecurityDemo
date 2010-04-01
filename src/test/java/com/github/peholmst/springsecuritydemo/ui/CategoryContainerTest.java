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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.github.peholmst.springsecuritydemo.domain.Category;
import com.github.peholmst.springsecuritydemo.services.CategoryService;
import com.github.peholmst.springsecuritydemo.services.stubs.CategoryServiceStub;

/**
 * Unit test for {@link CategoryContainer}.
 * 
 * @author Petter Holmström
 */
public class CategoryContainerTest {

	CategoryService serviceStub;
	
	CategoryContainer container;
	
	@Before
	public void setUp() {
		serviceStub = new CategoryServiceStub();		
		createTestData(serviceStub);
		container = new CategoryContainer(serviceStub);
	}
	
	private void createTestData(CategoryService service) {
		for (int i = 1; i<=5; ++i) {
			Category r = new Category();
			r.setName("Root" + i);
			service.saveCategory(r);
			for (int j = 1; j <= 5; ++j) {
				Category c = new Category();
				c.setName("Child" + i + "_" + j);
				c.setParent(r);
				service.saveCategory(c);
			}
		}
	}
	
	@Test
	public void testGetChildren() {
		// TODO Implement test!
	}
	
	@Test
	public void testHasChildren() {
		// TODO Implement test!
	}
	
	@Test
	public void testGetParent() {
		// TODO Implement test!
	}
	
	@Test
	public void testIsRoot() {
		assertFalse(serviceStub.getRootCategories().isEmpty());
		for (Category c : serviceStub.getRootCategories()) {
			assertTrue(container.isRoot(c));
		}
	}
	
	@Test
	public void testRootItemIds() {
		Collection<Category> rootItemIds = container.rootItemIds();
		assertEquals(rootItemIds, serviceStub.getRootCategories());
		assertFalse(rootItemIds.isEmpty());
	}
}
