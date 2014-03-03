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
 * Instances of this class represent terminological axioms of description
 * logics.
 * 
 * @author İnanç Seylan
 * 
 */
public class GCI extends Axiom {
	/**
	 * The type of a concept definition indicates whether this definition is a
	 * concept inclusion or a concept equality.
	 * 
	 */
	public enum Type {
		/**
		 * Represents a concept inclusion
		 */
		SUBSET,
		/**
		 * Represents a concept equality
		 */
		EQUIV
	};

	/**
	 * The type of this concept definition
	 */
	private Type type;

	/**
	 * The concept which constitutes the left-hand side of this definition
	 */
	private Concept lhs;

	/**
	 * The concept expression which constitutes the right-hand side of this
	 * definition
	 */
	private Concept rhs;

	/**
	 * Indicates whether this formula has a negation sign in front of it.
	 */
	private boolean negated;

	/**
	 * Default constructor
	 */
	public GCI() {

	}

	/**
	 * Constructor to initialize all the fields of this class by the given ones.
	 * 
	 * @param name
	 *            The name of the defined concept
	 * @param type
	 *            They type of this definition (inclusion or equality)
	 * @param description
	 *            The concept expression denoting (mostly) a complex concept
	 */
	public GCI(Type type, Concept lhs, Concept rhs) {
		this.type = type;
		this.lhs = lhs;
		this.rhs = rhs;
	}
        
        public GCI(Concept lhs, Concept rhs) {
            this.type = Type.SUBSET;
            this.lhs = lhs;
            this.rhs = rhs;
        }

	/**
	 * @return the type of this definition
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Sets the type of this definition.
	 * 
	 * @param type
	 *            The type to set. Concept inclusion is a primitive definition,
	 *            whereas concept equality is non-primitive.
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return the (complex) concept expression that constitutes the right-hand
	 *         side of this definition
	 */
	public Concept getRhs() {
		return rhs;
	}

	/**
	 * Sets the concept expression.
	 * 
	 * @param description
	 *            The concept expression
	 */
	public void setRhs(Concept description) {
		this.rhs = description;
	}

	public Concept getLhs() {
		return lhs;
	}

	public void setLhs(Concept lhs) {
		this.lhs = lhs;
	}

	public boolean isNegated() {
		return negated;
	}

	public Axiom toggleNegated() {
		negated = !negated;
		return this;
	}

	@Override
	public int hashCode() {
		return 17 * 37 + getLhs().hashCode() + getType().hashCode()
				+ getRhs().hashCode() + new Boolean(negated).hashCode();
	}

	public boolean equals(Object obj) {
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			GCI other = (GCI) obj;
			boolean typesEqual = getType().equals(other.getType());
			boolean operandsEqual = false;
			if (typesEqual) {
				if (getType().equals(Type.EQUIV)) {
					operandsEqual = (getLhs().equals(other.getLhs()) && getRhs()
							.equals(other.getRhs()))
							|| (getLhs().equals(other.getRhs()) && getRhs()
									.equals(other.getLhs()));
				} else {
					operandsEqual = getLhs().equals(other.getLhs())
							&& getRhs().equals(other.getRhs());
				}
			}
			return (negated == other.negated) && typesEqual && operandsEqual;
		}
		return false;
	}

	public GCI copy() {
		GCI cd = new GCI();
		cd.setType(getType());
		cd.setLhs(getLhs().copy());
		cd.setRhs(getRhs().copy());
		cd.negated = negated;
		return cd;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getLhs());
		if (getType() == Type.EQUIV)
			buffer.append(" = ");
		else
			buffer.append(" -> ");
		buffer.append(getRhs());
		if (isNegated()) {
			buffer.insert(0, "not(");
			buffer.append(")");
		}
		return buffer.toString();
	}

	public void accept(AxiomVisitor v) {
		v.visitConceptDefinition(this);
	}
}
