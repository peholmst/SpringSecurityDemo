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
package com.github.peholmst.springsecuritydemo.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Base class for unit tests of {@link AbstractEntity}-classes. Subclasses
 * should implement the {@link #createEntity()} method to return instances of
 * the concrete entity to be tested. Also check the JavaDocs of
 * {@link #doCreateRandomValue(Class)} in case it needs to be overridden as well.
 * 
 * @author Petter Holmström
 * 
 * @param <T>
 *            the type of the entity to test.
 */
public abstract class AbstractEntityTest<T extends AbstractEntity> {

	private T entity;

	/**
	 * Creates a new, clean entity instance that will be used for testing.
	 * 
	 * @return a new entity.
	 */
	protected abstract T createEntity();

	@Before
	public void setUp() {
		entity = createEntity();
	}

	/**
	 * Gets the entity that should be used for testing. This instance is
	 * recreated before every test.
	 * 
	 * @return the entity instance.
	 */
	protected T getEntity() {
		return entity;
	}

	@Test
	public void testInitialUUIDValue() {
		System.out.println("testInitialUUIDValue");
		assertNotNull("Initial UUID should not be null", getEntity().getUUID());
	}

	@Test
	public void testInitialVersionValue() {
		System.out.println("testInitialVersionValue");
		assertNull("Initial version should be null", getEntity().getVersion());
	}

	@Test
	public void testWritableProperties() throws Exception {
		System.out.println("testWritableProperties");
		BeanInfo info = Introspector.getBeanInfo(getEntity().getClass());
		for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
			if (pd.getWriteMethod() != null) {
				Object testData = createRandomValue(pd.getPropertyType());
				System.out.println("  Testing property " + pd.getName());
				pd.getWriteMethod().invoke(getEntity(), testData);
				assertEquals(
						"Returned property value should be equal to the set property value",
						testData, pd.getReadMethod().invoke(getEntity()));
			}
		}
	}

	@Test
	public void testEqualsSelf() {
		System.out.println("testEqualsSelf");
		assertEquals("Entity should be equal to itself", getEntity(), getEntity());
	}
	
	@Test
	public void testEqualsDefault() {
		System.out.println("testEqualsDefault");
		assertEquals("Two entities with default values should be equal", getEntity(), createEntity());
	}
	
	@Test
	public void testEqualsNull() {
		System.out.println("testEqualsNull");
		assertFalse("Entity is never equal to null", getEntity().equals(null));
	}

	@Test
	public void testEqualsDifferentType() {
		System.out.println("testEqualsDifferentType");
		assertFalse("Entity is never equal to a String", getEntity().equals(
				"Hello World"));
	}

	@Test
	public void testEquals() throws Exception {
		System.out.println("testEquals");
		T otherBean = createEntity();
		BeanInfo info = Introspector.getBeanInfo(getEntity().getClass());
		for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
			if (pd.getWriteMethod() != null) {
				Object testData = createRandomValue(pd.getPropertyType());
				// Set value on entity
				pd.getWriteMethod().invoke(getEntity(), testData);
				// Set different value on other bean
				pd.getWriteMethod().invoke(otherBean,
						createRandomValue(pd.getPropertyType()));
				assertFalse(
						pd.getName()
								+ " should have different values on both beans => not equal",
						otherBean.equals(getEntity()));
				// Set value on other bean to null
				pd.getWriteMethod().invoke(otherBean, (Object) null);
				assertFalse(pd.getName()
						+ " should be null on one of the beans => not equal",
						otherBean.equals(getEntity()));
				// Reset value on other bean
				pd.getWriteMethod().invoke(otherBean, testData);
				assertEquals(pd.getName()
						+ " should have the same value on both beans => equal",
						otherBean, getEntity());
			}
		}
	}

	private Random rnd = new Random();

	private Map<Class<?>, Object> previousRandomValues = new HashMap<Class<?>, Object>();
	
	/**
	 * Does the same as {@link #createRandomValue(Class)}, but ensures that two subsequent
	 * calls to this method with the same class parameters never return
	 * values that are equal to each other.
	 * 
	 * @param <E>
	 *            the type of the value to create.
	 * @param valueClass
	 *            the class of the value to create.
	 * @return a random value of the specified type, never <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if a value could not be created for the specified class.
	 */
	protected final <E> E createRandomValue(Class<E> valueClass)
			throws IllegalArgumentException {
		E value = doCreateRandomValue(valueClass);
		Object previous = previousRandomValues.get(valueClass);
		if (previous != null) {
			while (previous.equals(value)) {
				value = doCreateRandomValue(valueClass);
			}
		}
		previousRandomValues.put(valueClass, value);
		return value;
	}
	
	/**
	 * Creates a new random value of <code>valueClass</code>.
	 * <p>
	 * This implementation supports creating values of the following types:
	 * string, integer, long, double, float, boolean, {@link java.util.Date},
	 * enum and the class of the instance returned by {@link #createEntity()}.
	 * 
	 * @param <E>
	 *            the type of the value to create.
	 * @param valueClass
	 *            the class of the value to create.
	 * @return a random value of the specified type, never <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if a value could not be created for the specified class.
	 */
	protected <E> E doCreateRandomValue(Class<E> valueClass)
			throws IllegalArgumentException {
		if (valueClass == String.class) {
			return valueClass.cast(Long.toString(rnd.nextLong()));
		} else if (valueClass == Integer.class) {
			return valueClass.cast(rnd.nextInt());
		} else if (valueClass == Long.class) {
			return valueClass.cast(rnd.nextLong());
		} else if (valueClass == Double.class) {
			return valueClass.cast(rnd.nextDouble());
		} else if (valueClass == Float.class) {
			return valueClass.cast(rnd.nextFloat());
		} else if (valueClass == Boolean.class) {
			return valueClass.cast(rnd.nextBoolean());
		} else if (valueClass == Date.class) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(rnd.nextLong());
			return valueClass.cast(cal.getTime());
		} else if (valueClass == getEntity().getClass()) {
			T newEntity = createEntity();
			try {
				populateEntityWithTestData(newEntity);
				return valueClass.cast(newEntity);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Cannot create random value of class " + valueClass, e);
			}
		} else if (valueClass.isEnum()) {
			return valueClass.cast(valueClass.getEnumConstants()[rnd
					.nextInt(valueClass.getEnumConstants().length)]);
		} else {
			throw new IllegalArgumentException(
					"Cannot generate random value of class " + valueClass);
		}
	}

	
	/**
	 * This method invokes {@link #createRandomValue(Class)} on all the writable
	 * properties of <code>entity</code>, excluding any properties whose types
	 * are the same as <code>entity</code>.
	 * 
	 * @param entity
	 *            the entity to populate with data.
	 * @throws Exception
	 *             if something went wrong setting the property values.
	 */
	protected void populateEntityWithTestData(Object entity) throws Exception {
		BeanInfo info = Introspector.getBeanInfo(entity.getClass());
		for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
			// Don't generate a random value for properties of the same type as
			// the entity, as this will lead to an infinite loop.
			if (pd.getWriteMethod() != null
					&& pd.getPropertyType() != entity.getClass()) {
				pd.getWriteMethod().invoke(entity,
						createRandomValue(pd.getPropertyType()));
			}
		}
	}

	@Test
	public void testHashCodeDefaultPropertyValues() {
		System.out.println("testHashCodeDefaultPropertyValues");
		assertEquals(
				"Two empty instances of the same class should have the same hash codes",
				getEntity().hashCode(), createEntity().hashCode());
	}

	@Test
	public void testHashCode() throws Exception {
		System.out.println("testHashCode");
		T otherBean = createEntity();
		BeanInfo info = Introspector.getBeanInfo(getEntity().getClass());
		for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
			if (pd.getWriteMethod() != null) {
				Object testData = createRandomValue(pd.getPropertyType());
				// Set value on entity
				pd.getWriteMethod().invoke(getEntity(), testData);
				// Set different value on other bean
				pd.getWriteMethod().invoke(otherBean,
						createRandomValue(pd.getPropertyType()));
				assertFalse(
						pd.getName()
								+ " should have different values on both beans => hashCodes not equal",
						otherBean.hashCode() == getEntity().hashCode());
				// Set value on other bean to null
				pd.getWriteMethod().invoke(otherBean, (Object) null);
				assertFalse(
						pd.getName()
								+ " should be null on one of the beans => hashCodes not equal",
						otherBean.hashCode() == getEntity().hashCode());
				// Reset value on other bean
				pd.getWriteMethod().invoke(otherBean, testData);
				assertEquals(
						pd.getName()
								+ " should have the same value on both beans => hashCodes equal",
						otherBean.hashCode(), getEntity().hashCode());
			}
		}
	}
}
