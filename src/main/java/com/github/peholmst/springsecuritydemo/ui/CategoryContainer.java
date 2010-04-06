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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.peholmst.springsecuritydemo.domain.Category;
import com.github.peholmst.springsecuritydemo.services.CategoryService;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;

/**
 * TODO Document me!
 * 
 * @author Petter Holmström
 */
public class CategoryContainer implements Container.Hierarchical, Container.ItemSetChangeNotifier {

	private static final long serialVersionUID = 1197578205205304787L;

	private transient final CategoryService categoryService;

	private final Map<String, Class<?>> propertyIds;
	
	public CategoryContainer(CategoryService categoryService) {
		assert categoryService != null : "categoryService must not be null";
		this.categoryService = categoryService;
		
		propertyIds = new HashMap<String, Class<?>>();
		try {
			BeanInfo bi = Introspector.getBeanInfo(Category.class);
			for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
				if (!pd.getName().equals("class")) {
					propertyIds.put(pd.getName(), pd.getPropertyType());
				}
			}
		} catch (Exception e) {
			// Should never happen
			throw new RuntimeException("Could not read properties from Category class", e);
		}
	}

	public CategoryService getCategoryService() {
		return categoryService;
	}

	/**
	 * TODO Document me!
	 * 
	 * @author peholmst
	 */
	protected class Node {
		private Node parent;
		private List<Node> children;
		private List<String> childrenUUIDs;
		private long lastUpdated;
		private Category category;

		public Node(Node parent, Category category) {
			this.parent = parent;
			this.category = category;
			this.lastUpdated = System.currentTimeMillis();
		}

		public Node getParent() {
			return parent;
		}

		public List<Node> getChildren() {
			if (children == null) {
				children = new LinkedList<Node>();
				childrenUUIDs = new LinkedList<String>();
				List<Category> childCategories = getCategoryService()
						.getChildren(category);
				for (Category c : childCategories) {
					children.add(getNode(c));
					childrenUUIDs.add(c.getUUID());
				}
				children = Collections.unmodifiableList(children);
				childrenUUIDs = Collections.unmodifiableList(childrenUUIDs);
			}
			return children;
		}

		public List<String> getChildrenUUIDs() {
			if (childrenUUIDs == null) {
				getChildren();
			}
			return childrenUUIDs;
		}

		public long getLastUpdated() {
			return lastUpdated;
		}

		public Category getCategory() {
			return category;
		}
	}

	private Map<String, Node> uuidToNodeMap = Collections.emptyMap();

	private List<String> rootUUIDs = Collections.emptyList();
	
	public void refresh() {
		rootUUIDs = new LinkedList<String>();
		uuidToNodeMap = new HashMap<String, Node>();
		for (Category c : getCategoryService().getRootCategories()) {
			rootUUIDs.add(c.getUUID());
		}
		rootUUIDs = Collections.unmodifiableList(rootUUIDs);
		fireItemSetChange(new ContainerRefreshedEvent());
	}

	/**
	 * TODO Document me!
	 * 
	 * @param uuid
	 * @return
	 */
	protected Node getNode(String uuid) {
		Node node = uuidToNodeMap.get(uuid);
		if (node == null) {
			Category c = getCategoryService().getCategoryByUUID(uuid);
			if (c == null) {
				return null;
			}
			Node parentNode = c.getParent() == null ? null : getNode(c
					.getParent().getUUID());
			node = new Node(parentNode, c);
			uuidToNodeMap.put(uuid, node);
		}
		return node;
	}

	/**
	 * TODO Document me!
	 * 
	 * @param category
	 * @return
	 */
	protected Node getNode(Category category) {
		Node node = uuidToNodeMap.get(category.getUUID());
		if (node == null) {
			Node parentNode = category.getParent() == null ? null
					: getNode(category.getParent().getUUID());
			node = new Node(parentNode, category);
			uuidToNodeMap.put(category.getUUID(), node);
		}
		return node;

	}

	@Override
	public boolean areChildrenAllowed(Object itemId) {
		return hasChildren(itemId);
	}

	@Override
	public Collection<?> getChildren(Object itemId) {
		Node node = getNode((String) itemId);
		return node == null ? null : node.getChildrenUUIDs();
	}

	@Override
	public Object getParent(Object itemId) {
		Node node = getNode((String) itemId);
		return (node == null || node.getParent() == null) ? null : node
				.getParent().getCategory().getUUID();
	}

	@Override
	public boolean hasChildren(Object itemId) {
		Node node = getNode((String) itemId);
		return node == null ? false : !node.getChildren().isEmpty();
	}

	@Override
	public boolean isRoot(Object itemId) {
		return rootItemIds().contains(itemId);
	}

	@Override
	public boolean removeItem(Object itemId)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException(); // Not implemented
	}

	@Override
	public Collection<String> rootItemIds() {
		return rootUUIDs;
	}

	@Override
	public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed)
			throws UnsupportedOperationException {
		return false; // Not implemented
	}

	@Override
	public boolean setParent(Object itemId, Object newParentId)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException(); // Not implemented
	}

	@Override
	public boolean addContainerProperty(Object propertyId, Class<?> type,
			Object defaultValue) throws UnsupportedOperationException {
		throw new UnsupportedOperationException(); // Not implemented
	}

	@Override
	public Object addItem() throws UnsupportedOperationException {
		throw new UnsupportedOperationException(); // Not implemented
	}

	@Override
	public Item addItem(Object itemId) throws UnsupportedOperationException {
		throw new UnsupportedOperationException(); // Not implemented
	}

	@Override
	public boolean containsId(Object itemId) {
		return getNode((String) itemId) != null;
	}

	@Override
	public Property getContainerProperty(Object itemId, Object propertyId) {
		Item item = getItem(itemId);
		return item == null ? null : item.getItemProperty(propertyId);
	}

	@Override
	public Collection<?> getContainerPropertyIds() {
		return Collections.unmodifiableCollection(propertyIds.keySet());
	}

	@Override
	public Item getItem(Object itemId) {
		Node node = getNode((String) itemId);
		return node == null ? null : new BeanItem<Category>(node.getCategory());
	}

	@Override
	public Collection<?> getItemIds() {
		throw new UnsupportedOperationException(); // Not implemented
	}

	@Override
	public Class<?> getType(Object propertyId) {
		return propertyIds.get(propertyId);
	}

	@Override
	public boolean removeAllItems() throws UnsupportedOperationException {
		throw new UnsupportedOperationException(); // Not implemented
	}

	@Override
	public boolean removeContainerProperty(Object propertyId)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException(); // Not implemented
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException(); // Not implemented
	}
	
	private LinkedList<ItemSetChangeListener> listeners;

	/**
	 * TODO Document me!
	 * 
	 * @author peholmst
	 *
	 */
	public class ContainerRefreshedEvent implements ItemSetChangeEvent {

		private static final long serialVersionUID = 2098472936710486939L;

		@Override
		public Container getContainer() {
			return CategoryContainer.this;
		}
		
	};
	
	/**
	 * TODO Document me!
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	protected void fireItemSetChange(ItemSetChangeEvent event) {
		if (event == null || listeners == null) {
			return;
		}
		/*
		 * Iterate over a cloned list instead of the list itself to
		 * avoid strange behavior if any of the listeners add additional
		 * listeners or remove existing ones.
		 */
		LinkedList<ItemSetChangeListener> clonedList = (LinkedList<ItemSetChangeListener>) listeners.clone();
		for (ItemSetChangeListener listener : clonedList) {
			listener.containerItemSetChange(event);
		}
	}
	
	@Override
	public void addListener(ItemSetChangeListener listener) {
		if (listener == null) {
			return;
		}
		if (listeners == null) {
			listeners = new LinkedList<ItemSetChangeListener>();
		}
		listeners.add(listener);
	}
	@Override
	public void removeListener(ItemSetChangeListener listener) {
		if (listener == null || listeners == null) {
			return;
		}
		listeners.remove(listener);
		if (listeners.isEmpty()) {
			listeners = null;
		}
	}

}
