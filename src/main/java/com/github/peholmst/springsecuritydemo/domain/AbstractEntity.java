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

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * Base class for entities. When a new entity instance is created, it is
 * automatically assigned a UUID that will be used as the entity's ID (and the
 * database's primary key).
 * 
 * @author Petter Holmström
 */
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

	private static final long serialVersionUID = 992875744546231123L;

	@Id
	private String uuid;

	@Version
	private Long version;

	/**
	 * Creates a new <code>AbstractEntity</code>.
	 */
	public AbstractEntity() {
		uuid = UUID.randomUUID().toString();
	}

	/**
	 * Gets the UUID of this entity.
	 * 
	 * @return the UUID, never <code>null</code>.
	 */
	public String getUUID() {
		return uuid;
	}

	/**
	 * Gets the version of this entity that may be used for optimistic
	 * transaction locking.
	 * 
	 * @return the version, may be <code>null</code> if optimistic transaction
	 *         locking is not used or the entity has not yet been persisted.
	 */
	public Long getVersion() {
		return version;
	}
}
