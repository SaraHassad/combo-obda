/**
 * This file is part of combo-obda.
 *
 * combo-obda is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * combo-obda is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * combo-obda. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unibremen.informatik.tdki.combo.syntax.concept;

import de.unibremen.informatik.tdki.combo.common.Copyable;
import de.unibremen.informatik.tdki.combo.syntax.Negatable;


/**
 * This class is the ancestor of all concept expressions.
 * 
 * @author İnanç Seylan
 * 
 */
public abstract class Concept implements Negatable<Concept>, VisitableConcept,
		Copyable<Concept> {

	/**
	 * Default constructor
	 * 
	 */
	public Concept() {

	}


}
