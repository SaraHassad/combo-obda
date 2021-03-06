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


/**
 * The visitor interface that allows to visit the type of concept expressions we
 * have.
 * 
 * @author İnanç Seylan
 * 
 */
public interface ConceptVisitor {
	/**
	 * Visits the given atomic concept.
	 * @param c The atomic concept to visit
	 */
	void visitConceptName(ConceptName c);

	/**
	 * Visits the given concept expression formed by a binary constructor.
	 * @param bd The binary concept description to visit
	 */
	void visitBooleanConcept(BooleanConcept bd);

	/**
	 * Visits the given quantified restriction.
	 * @param restriction The quantified restriction to visit.
	 */
	void visitRoleRestriction(RoleRestriction restriction);
	
	void visitQualifiedNoRestriction(QualifiedNoRestriction restriction);
	
	void visitNominal(Nominal nominal);
}
