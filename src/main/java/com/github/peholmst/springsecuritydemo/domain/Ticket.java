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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

/**
 * 
 * 
 * @author Petter Holmström
 */
@Entity
public class Ticket extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	@NotNull
	private Category category;
	
	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	@Past
	private Date openedDate = new Date();
	
	@Temporal(TemporalType.TIMESTAMP)
	@Past
	private Date closedDate = null;
	
	@Enumerated
	@NotNull
	private TicketStatus ticketStatus = TicketStatus.PENDING;
	
	public boolean isOpen() {
		return openedDate != null && closedDate == null;
	}
}
