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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.github.peholmst.springsecuritydemo.domain.Category;

/**
 * Unit test for {@link CategoryServiceStub}.
 * 
 * @author Petter Holmström
 */
public class CategoryServiceStubTest {

	CategoryServiceStub service;
	
	@Before
	public void setUp() {
		service = new CategoryServiceStub();
	}
		
	@Test
	public void testGetEmptyRootCategories() {
		System.out.println("testGetEmptyRootCategories");
		assertTrue(service.getRootCategories().isEmpty());
	}
	
	@Test
	public void testGetChildrenForInvalidCategory() {
		System.out.println("testGetChildrenForInvalidCategory");
		assertNull(service.getChildren(new Category()));
	}
	
	@Test
	public void testSaveCategory_NewRoots() {
		System.out.println("testSaveCategory_NewRoots");
		Category root1 = new Category("Root1", "Description");
		service.saveCategory(root1);
		Category root2 = new Category("Root2", "Description");
		service.saveCategory(root2);
		
		assertEquals(2, service.getRootCategories().size());
		assertEquals(root1, service.getRootCategories().get(0));
		assertEquals(root2, service.getRootCategories().get(1));
	}
	
	@Test
	public void testSaveCategory_NewChild() {
		System.out.println("testSaveCategory_NewChild");
		Category root1 = new Category("Root1", "Description");
		service.saveCategory(root1);
		Category child1 = new Category("Child1", "Description", root1);
		service.saveCategory(child1);
		
		assertEquals(1, service.getRootCategories().size());
		assertTrue(service.getChildren(root1).contains(child1));
	}
	
	@Test
	public void testSaveCategory_RootToChild() {
		System.out.println("testSaveCategory_RootToChild");
		Category root1 = new Category("Root1", "Description");
		service.saveCategory(root1);
		Category root2 = new Category("Root2", "Description");
		service.saveCategory(root2);

		root2.setParent(root1);
		service.saveCategory(root2);

		assertEquals(1, service.getRootCategories().size());
		assertTrue(service.getChildren(root1).contains(root2));
	}
	
	@Test
	public void testSaveCategory_ChildToRoot() {
		System.out.println("testSaveCategory_ChildToRoot");
		Category root1 = new Category("Root1", "Description");
		service.saveCategory(root1);
		Category child1 = new Category("Child1", "Description", root1);
		service.saveCategory(child1);

		child1.setParent(null);
		service.saveCategory(child1);
		
		assertEquals(2, service.getRootCategories().size());
		assertEquals(root1, service.getRootCategories().get(0));
		assertEquals(child1, service.getRootCategories().get(1));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSaveCategory_InvalidParent() {
		System.out.println("testSaveCategory_InvalidParent");
		Category invalid = new Category("Invalid", "Description");
		Category child1 = new Category("Child1", "Description", invalid);
		service.saveCategory(child1);
	}
	
	@Test
	public void testDeleteCategory_Leaf() {
		System.out.println("testDeleteCategory_Leaf");
		Category root1 = new Category("Root1", "Description");
		service.saveCategory(root1);
		Category child1 = new Category("Child1", "Description", root1);
		service.saveCategory(child1);

		assertFalse(service.getChildren(root1).isEmpty());
		service.deleteCategory(child1);
		assertTrue(service.getChildren(root1).isEmpty());
	}
	
	@Test
	public void testDeleteCategory_Parent() {
		System.out.println("testDeleteCategory_Parent");
		Category root1 = new Category("Root1", "Description");
		service.saveCategory(root1);
		Category child1 = new Category("Child1", "Description", root1);
		service.saveCategory(child1);
		Category grandChild1 = new Category("GrandChild1", "Description", child1);
		service.saveCategory(grandChild1);
		
		service.deleteCategory(child1);
		assertFalse(service.getChildren(root1).contains(child1));
		assertTrue(service.getChildren(root1).contains(grandChild1));
		assertSame(root1, grandChild1.getParent());
	}
	
	@Test
	public void testDeleteCategory_Root() {
		System.out.println("testDeleteCategory_Root");
		Category root1 = new Category("Root1", "Description");
		service.saveCategory(root1);
		Category child1 = new Category("Child1", "Description", root1);
		service.saveCategory(child1);

		service.deleteCategory(root1);
		assertFalse(service.getRootCategories().contains(root1));
		assertTrue(service.getRootCategories().contains(child1));
		assertNull(child1.getParent());
	}
	
	@Test
	public void testDeleteCategory_NonExistent() {
		System.out.println("testDeleteCategory_NonExistent");
		service.deleteCategory(new Category());
	}
	
	@Test
	public void testDeleteCategoryByUUID() {
		System.out.println("testDeleteCategoryByUUID");
		Category root1 = new Category("Root1", "Description");
		service.saveCategory(root1);
		Category child1 = new Category("Child1", "Description", root1);
		service.saveCategory(child1);

		assertFalse(service.getChildren(root1).isEmpty());
		service.deleteCategoryByUUID(child1.getUUID());
		assertTrue(service.getChildren(root1).isEmpty());	
	}
	
	@Test
	public void testDeleteCategoryByUUID_NonExistent() {
		System.out.println("testDeleteCategoryByUUID_NonExistent");
		service.deleteCategoryByUUID("nonexistent");
	}
	
	@Test
	public void testGetCategoryByUUID() {
		System.out.println("testGetCategoryByUUID");
		Category cat = new Category("Cat", "Description");
		assertSame(cat, service.saveCategory(cat));
		assertSame(cat, service.getCategoryByUUID(cat.getUUID()));
	}
	
	@Test
	public void testGetCategoryByUUID_NonExistent() {
		System.out.println("testGetCategoryByUUID_NonExistent");
		assertNull(service.getCategoryByUUID("nonexistent"));
	}
	
}
