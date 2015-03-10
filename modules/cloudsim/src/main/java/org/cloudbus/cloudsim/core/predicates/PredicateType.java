/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core.predicates;

import org.cloudbus.cloudsim.core.SimEvent;

/**
 * A predicate to select events with specific tags.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 * @see PredicateNotType
 * @see Predicate
 */
public class PredicateType extends Predicate {

	/** The tags. */
	private final int tags;

	/**
	 * Constructor used to select events with the tag value <code>tags</code>.
	 * 
	 * @param tags an event tag value
	 */
	public PredicateType(int tags) {
		this.tags = tags;
	}

	/**
	 * The match function called by <code>Sim_system</code>, not used directly by the user.
	 * 
	 * @param ev the ev
	 * @return true, if match
	 */
	@Override
	public boolean match(SimEvent ev) {
		return tags == ev.getTag();
	}
}
