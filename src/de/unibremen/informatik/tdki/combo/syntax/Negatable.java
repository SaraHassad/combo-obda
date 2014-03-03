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
package de.unibremen.informatik.tdki.combo.syntax;

/**
 * An instance of this interface is an expression that can be negated.
 * 
 * @author İnanç Seylan
 */
public interface Negatable<E> {
	/**
	 * @return true if the expression is negated. 
	 */
	public boolean isNegated();

	/**
	 * Toggles the negation status of the expression.
	 * @return this, i.e., the negated expression.
	 */
	public E toggleNegated();
}
