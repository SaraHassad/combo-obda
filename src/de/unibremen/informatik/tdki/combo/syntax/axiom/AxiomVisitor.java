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
package de.unibremen.informatik.tdki.combo.syntax.axiom;


/**
 * 
 * @author İnanç Seylan
 *
 */
public interface AxiomVisitor {

	/**
	 * Visits the given concept definition.
	 * 
	 * @param cd
	 *            The concept definition
	 */
	void visitConceptDefinition(GCI cd);

	/**
	 * Visits the given concept assertion.
	 * 
	 * @param ca
	 *            The concept assertion
	 */
	void visitConceptAssertion(ConceptAssertion ca);

	/**
	 * Visits the given role assertion.
	 * 
	 * @param ra
	 *            The role assertion
	 */
	void visitObjectRoleAssertion(ObjectRoleAssertion ra);
	
	void visitRoleInclusion(RoleInclusion ri);
	
	void visitSameIndividualAssertion(SameIndividualAssertion ia);
}
