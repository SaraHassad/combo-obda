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

import de.unibremen.informatik.tdki.combo.syntax.concept.Concept;


/**
 * This class represents a type of assertional axiom in description logics that
 * is asserting that an individual is an instance of a concept.
 * 
 * @author İnanç Seylan
 * 
 */
public class ConceptAssertion extends Axiom {
	/**
	 * The concept that this individual is an instance of
	 */
	private Concept concept;

	/**
	 * The name of this individual
	 */
	private String name;

	/**
	 * Default constructor
	 * 
	 */
	public ConceptAssertion() {

	}

	/**
	 * Constructor to initialize all the fields of this class by the given ones.
	 * 
	 * @param concept
	 *            The concept
	 * @param name
	 *            The individual's name
	 */
	public ConceptAssertion(Concept concept, String name) {
		setConcept(concept);
		setIndividual(name);
	}

	/**
	 * @return the concept of this assertion. This concept can be composite,
	 *         i.e. made up of simpler concepts.
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * Sets the concept of this assertion.
	 * 
	 * @param concept
	 *            The concept
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * @return the name of the individual
	 */
	public String getIndividual() {
		return name.toString();
	}

	/**
	 * Sets the name of the individual
	 * 
	 * @param name
	 *            the name of the individual
	 */
	public void setIndividual(String name) {
		this.name = name;
	}

	/**
	 * @see tr.edu.ege.seagent.alcbi.syntax.Axiom#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			ConceptAssertion other = (ConceptAssertion) obj;
			return (this.getConcept().equals(other.getConcept()) && this.name
					.equals(other.name));
		}
		return false;
	}

	/**
	 * @see tr.edu.ege.seagent.alcbi.syntax.Axiom#hashCode()
	 */
	@Override
	public int hashCode() {
		return 17 * 37 + getConcept().hashCode() + getIndividual().hashCode();
	}

	public ConceptAssertion copy() {
		ConceptAssertion ca = new ConceptAssertion();
		ca.name = getIndividual();
		ca.setConcept(getConcept().copy());
		return ca;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getConcept());
		buffer.append("(");
		buffer.append(getIndividual());
		buffer.append(")");
		return buffer.toString();
	}

	public boolean isNegated() {
		return getConcept().isNegated();
	}

	public ConceptAssertion toggleNegated() {
		setConcept(getConcept().toggleNegated());
		return this;
	}

	public void accept(AxiomVisitor v) {
		v.visitConceptAssertion(this);
	}

}
