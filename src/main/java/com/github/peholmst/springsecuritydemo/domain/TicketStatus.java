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
