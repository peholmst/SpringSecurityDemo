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
package com.github.peholmst.springsecuritydemo.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.github.peholmst.springsecuritydemo.domain.Category;
import com.github.peholmst.springsecuritydemo.services.CategoryService;
import com.github.peholmst.springsecuritydemo.services.stubs.CategoryServiceStub;
import com.vaadin.data.Item;

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
		container.refresh();
		assertFalse(serviceStub.getRootCategories().isEmpty());
	}
	
	private void createTestData(CategoryService service) {
		for (int i = 1; i<=5; ++i) {
			Category r = new Category();
			r.setName("Root" + i);
			service.insertCategory(r);
			for (int j = 1; j <= 5; ++j) {
				Category c = new Category();
				c.setName("Child" + i + "_" + j);
				c.setParent(r);
				service.insertCategory(c);
			}
		}
	}
	
	@Test
	public void testGetChildren() {
		System.out.println("testGetChildren");
		for (Category root : serviceStub.getRootCategories()) {
			Collection<?> childrenIds = container.getChildren(root.getId());
			List<Category> children = serviceStub.getChildren(root);
			assertEquals(children.size(), childrenIds.size());
			Iterator<?> idIterator = childrenIds.iterator();
			Iterator<Category> childIterator = children.iterator();
			while (idIterator.hasNext() && childIterator.hasNext()) {
				assertEquals(idIterator.next(), childIterator.next().getId());
			}
		}
	}
	
	@Test
	public void testGetChildren_Nonexistent() {
		System.out.println("testGetChildren_Nonexistent");
		assertNull(container.getChildren(-123l));
	}
	
	@Test
	public void testHasChildren() {
		System.out.println("testHasChildren");
		for (Category root : serviceStub.getRootCategories()) {
			assertTrue(container.hasChildren(root.getId()));
			for (Category c : serviceStub.getChildren(root)) {
				assertFalse(container.hasChildren(c.getId()));
			}
		}
	}
	
	@Test
	public void testHasChildren_Nonexistent() {
		System.out.println("testHasChildren_Nonexistent");
		assertFalse(container.hasChildren(-123l));
	}
	
	@Test
	public void testGetParent() {
		System.out.println("testGetParent");
		for (Category root : serviceStub.getRootCategories()) {
			assertNull(container.getParent(root.getId()));
			for (Category child : serviceStub.getChildren(root)) {
				assertEquals(root.getId(), container.getParent(child.getId()));
			}
		}
	}
	
	@Test
	public void testIsRoot() {
		System.out.println("testIsRoot");
		for (Category c : serviceStub.getRootCategories()) {
			assertTrue(container.isRoot(c.getId()));
		}
	}
	
	@Test
	public void testRootItemIds() {
		System.out.println("testRootItemIds");
		Collection<Long> rootItemIds = container.rootItemIds();
		assertFalse(rootItemIds.isEmpty());
		for (Category c : serviceStub.getRootCategories()) {
			assertTrue(rootItemIds.contains(c.getId()));
		}
	}
	
	@Test
	public void testAreChildrenAllowed() {
		System.out.println("testAreChildrenAllowed");
		for (Category root : serviceStub.getRootCategories()) {
			assertTrue(container.areChildrenAllowed(root.getId()));
			for (Category c : serviceStub.getChildren(root)) {
				/*
				 * Even though it is legal to add children to any category,
				 * this method should return false for categories without children
				 * to make sure they show up as leaves.
				 */
				assertFalse(container.areChildrenAllowed(c.getId()));
			}
		}
		assertFalse(container.areChildrenAllowed(-123l));
	}
	
	@Test
	public void testGetContainerProperty() {
		System.out.println("testGetItem");
		for (Category root : serviceStub.getRootCategories()) {
			assertEquals(root.getId(), container.getContainerProperty(root.getId(), "id").getValue());
			assertEquals(root.getVersion(), container.getContainerProperty(root.getId(), "version").getValue());
			assertEquals(root.getName(), container.getContainerProperty(root.getId(), "name").getValue());
			assertEquals(root.getDescription(), container.getContainerProperty(root.getId(), "description").getValue());
			assertNull(container.getContainerProperty(root.getId(), "parent").getValue());
			for (Category c : serviceStub.getChildren(root)) {
				// Assume the rest of the properties are working
				assertEquals(c.getParent(), container.getContainerProperty(c.getId(), "parent").getValue());
			}
		}
	}
	
	@Test
	public void testGetContainerProperty_NonexistentItem() {
		System.out.println("testGetContainerProperty_NonexistentItem");
		assertNull(container.getContainerProperty(-123l, "id"));
	}
	
	@Test
	public void testGetContainerProperty_NonexistentProperty() {
		System.out.println("testGetContainerProperty_NonexistentItem");
		assertNull(container.getContainerProperty(serviceStub.getRootCategories().get(0).getId(), "nonexistent"));
	}
	
	@Test
	public void testGetContainerPropertyIds() throws Exception {
		System.out.println("testGetContainerPropertyIds");
		Collection<?> propertyIds = container.getContainerPropertyIds();
		assertEquals(5, propertyIds.size());
		assertTrue(propertyIds.contains("id"));
		assertTrue(propertyIds.contains("version"));
		assertTrue(propertyIds.contains("name"));
		assertTrue(propertyIds.contains("description"));
		assertTrue(propertyIds.contains("parent"));
	}
	
	@Test
	public void testGetType() {
		System.out.println("testGetType");
		assertEquals(Long.class, container.getType("id"));
		assertEquals(Long.class, container.getType("version"));
		assertEquals(String.class, container.getType("name"));
		assertEquals(String.class, container.getType("description"));
		assertEquals(Category.class, container.getType("parent"));
	}
	
	@Test
	public void testGetType_Nonexistent() {
		System.out.println("testGetType_Nonexistent");
		assertNull(container.getType("nonexistent"));
	}
	
	@Test
	public void testGetItem() {
		System.out.println("testGetItem");
		for (Category root : serviceStub.getRootCategories()) {
			Item item = container.getItem(root.getId());
			assertEquals(root.getId(), item.getItemProperty("id").getValue());
			assertEquals(root.getVersion(), item.getItemProperty("version").getValue());
			assertEquals(root.getName(), item.getItemProperty("name").getValue());
			assertEquals(root.getDescription(), item.getItemProperty("description").getValue());
			assertNull(item.getItemProperty("parent").getValue());
			for (Category c : serviceStub.getChildren(root)) {
				Item childItem = container.getItem(c.getId());
				// Assume the rest of the properties are working
				assertEquals(c.getParent(), childItem.getItemProperty("parent").getValue());
			}
		}
	}	
	
	@Test
	public void testGetItem_Nonexistent() {
		System.out.println("testGetItem_Nonexistent");
		assertNull(container.getItem(-123l));		
	}
	
	@Test
	public void testSetChildrenAllowed() {
		System.out.println("testSetChildrenAllowed");
		for (Category root : serviceStub.getRootCategories()) {
			// The method is not implemented, should always return false
			assertFalse(container.setChildrenAllowed(root, true));
		}
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testSetParent() {
		System.out.println("testSetParent");
		container.setParent(null, null); // This method is not implemented.
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testSize() {
		System.out.println("testSize");
		container.size(); // This method is not implemented
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testRemoveContainerProperty() {
		System.out.println("testRemoveContainerProperty");
		container.removeContainerProperty("blah"); // This method is not implemented
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testRemoveAllItems() {
		System.out.println("testRemoveAllItems");
		container.removeAllItems(); // This method is not implemented
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testGetItemIds() {
		System.out.println("testGetItemIds");
		container.getItemIds(); // This method is not implemented
	}
	
	@Test
	public void testContainsId() {
		System.out.println("testContainsId");
		for (Category root : serviceStub.getRootCategories()) {
			assertTrue(container.containsId(root.getId()));
			for (Category c : serviceStub.getChildren(root)) {
				assertTrue(container.containsId(c.getId()));
			}
		}
		assertFalse(container.containsId(-123l));
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testAddItem1() {
		System.out.println("testAddItem1");
		container.addItem("itemId"); // This method is not implemented
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testAddItem2() {
		System.out.println("testAddItem2");
		container.addItem(); // This method is not implemented
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testAddContainerProperty() {
		System.out.println("testAddContainerProperty");
		container.addContainerProperty("propId", String.class, "blah"); // This method is not implemented
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testRemoveItem() {
		System.out.println("testRemoveItem");
		container.removeItem("blah"); // This method is not implemented
	}
}
