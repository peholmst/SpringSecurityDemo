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
package com.github.peholmst.springsecuritydemo.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

/**
 * Unit test for {@link Ticket}.
 * 
 * @author Petter Holmström
 */
public class TicketTest extends AbstractEntityTest<Ticket> {

	@Override
	protected Ticket createEntity() {
		return new Ticket();
	}

	@Override
	protected <E> E doCreateRandomValue(Class<E> valueClass)
			throws IllegalArgumentException {
		if (valueClass == Category.class) {
			Category cat = new Category();
			try {
				populateEntityWithTestData(cat);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Cannot create random value of class " + valueClass, e);
			}
			return valueClass.cast(cat);
		} else
			return super.doCreateRandomValue(valueClass);
	}
	
	@Test
	public void testIsOpen() {
		System.out.println("testIsOpen");
		assertNull(getEntity().isOpen());
		getEntity().setOpenedDate(new Date());
		assertTrue(getEntity().isOpen());
		getEntity().setClosedDate(new Date());
		assertFalse(getEntity().isOpen());
	}
}
