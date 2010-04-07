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

/**
 * Enumeration defining the different states a {@link Ticket} can be in.
 * 
 * @author Petter Holmström
 */
public enum TicketStatus {

	/**
	 * The ticket is waiting for somebody to do something about it.
	 */
	PENDING,
	/**
	 * The ticket has been assigned to a person, but no action has been taken
	 * yet.
	 */
	ASSIGNED,
	/**
	 * Actions are currently being taken to resolve the ticket.
	 */
	ACTIVE,
	/**
	 * The ticket has been resolved (i.e. the problem has been solved).
	 */
	RESOLVED,
	/**
	 * The ticket has been canceled (i.e. no actions will be taken to resolve
	 * it).
	 */
	CANCELED

}
