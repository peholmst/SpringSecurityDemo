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
import javax.validation.constraints.Size;

/**
 * Domain class that represents a Ticket. Tickets always belong to exactly one
 * Category. A ticket can be either opened or closed, and always has a specific
 * status defined in {@link TicketStatus}.
 * 
 * @author Petter Holmström
 */
@Entity
public class Ticket extends AbstractEntity {

	private static final long serialVersionUID = 2302141076965571202L;

	@ManyToOne
	@JoinColumn(nullable = false)
	@NotNull
	private Category category;

	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	@Past
	private Date openedDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Past
	private Date closedDate;

	@Enumerated
	@NotNull
	private TicketStatus ticketStatus;

	@NotNull
	@Size(min = 2, max = 200)
	private String reporter;

	@NotNull
	@Size(min = 2, max = 200)
	private String subject;

	@NotNull
	private String description;

	public Ticket() {
		this("", "", "", TicketStatus.PENDING, null, null, null);
	}

	public Ticket(String subject, String description, String reporter,
			TicketStatus ticketStatus, Date openedDate, Date closedDate,
			Category category) {
		this.subject = subject;
		this.description = description;
		this.reporter = reporter;
		this.ticketStatus = ticketStatus;
		this.openedDate = openedDate;
		this.closedDate = closedDate;
		this.category = category;
	}

	/**
	 * Checks whether the ticket is open or closed.
	 * 
	 * @return true if the ticket is open, false if it is closed, or
	 *         <code>null</code> if undefined (i.e. no opened date has been
	 *         specified).
	 */
	public Boolean isOpen() {
		return getOpenedDate() == null ? null : getOpenedDate() != null
				&& getClosedDate() == null;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Date getOpenedDate() {
		return openedDate;
	}

	public void setOpenedDate(Date openedDate) {
		this.openedDate = openedDate;
	}

	public Date getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(Date closedDate) {
		this.closedDate = closedDate;
	}

	public TicketStatus getTicketStatus() {
		return ticketStatus;
	}

	public void setTicketStatus(TicketStatus ticketStatus) {
		this.ticketStatus = ticketStatus;
	}

	public String getReporter() {
		return reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		result = prime * result
				+ ((closedDate == null) ? 0 : closedDate.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((openedDate == null) ? 0 : openedDate.hashCode());
		result = prime * result
				+ ((reporter == null) ? 0 : reporter.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result
				+ ((ticketStatus == null) ? 0 : ticketStatus.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ticket other = (Ticket) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (closedDate == null) {
			if (other.closedDate != null)
				return false;
		} else if (!closedDate.equals(other.closedDate))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (openedDate == null) {
			if (other.openedDate != null)
				return false;
		} else if (!openedDate.equals(other.openedDate))
			return false;
		if (reporter == null) {
			if (other.reporter != null)
				return false;
		} else if (!reporter.equals(other.reporter))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		if (ticketStatus == null) {
			if (other.ticketStatus != null)
				return false;
		} else if (!ticketStatus.equals(other.ticketStatus))
			return false;
		return true;
	}

}
