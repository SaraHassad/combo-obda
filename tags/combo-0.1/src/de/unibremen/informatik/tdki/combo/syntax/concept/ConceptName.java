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

/**
 * The basic concept expression, i.e., a concept name.
 *
 * @author İnanç Seylan
 *
 */
public class ConceptName extends Concept {

    private static ConceptName TOP = new ConceptName(
            "http://www.w3.org/2002/07/owl#Thing");
    private static ConceptName BOTTOM = new ConceptName(
            "http://www.w3.org/2002/07/owl#Nothing");
    private boolean negated;

    public static ConceptName topConcept() {
        return TOP;
    }

    public static ConceptName bottomConcept() {
        return BOTTOM;
    }
    /**
     * The name of this concept
     */
    private String name;

    /**
     * Default constructor
     */
    public ConceptName() {
    }

    /**
     * Constructor to set the name field
     *
     * @param name The concept name to be set
     */
    public ConceptName(String name) {
        setName(name);
    }

    /**
     * @return the name of this concept.
     */
    public String getName() {
        return name.toString();
    }

    /**
     * Sets the name of this concept.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConceptName other = (ConceptName) obj;
        if (this.negated != other.negated) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.negated ? 1 : 0);
        hash = 23 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    /**
     * Calls {@link ConceptVisitor#visitConceptName(ConceptName)}
     *
     * @see
     * tr.edu.ege.seagent.alcbi.syntax.description.VisitableConcept#accept(tr.edu.ege.seagent.alcbi.syntax.description.ConceptVisitor)
     */
    public void accept(ConceptVisitor v) {
        v.visitConceptName(this);
    }

    /**
     * @return true if this concept expression has a negation sign in front of
     * it.
     */
    public boolean isNegated() {
        return negated;
    }

    /**
     * Toggles the value of negated field.
     */
    public ConceptName toggleNegated() {
        ConceptName result = null;
        if (this.equals(TOP)) {
            result = BOTTOM;
        } else if (this.equals(BOTTOM)) {
            result = TOP;
        } else {
            negated = !negated;
            result = this;
        }
        return result;
    }

    /**
     * @see Copyable#copy()
     */
    public ConceptName copy() {
        ConceptName result = null;
        if (this.equals(TOP) || this.equals(BOTTOM)) {
            result = this;
        } else {
            result = new ConceptName();
            result.name = getName();
            result.negated = negated;
        }
        return result;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return (isNegated() ? "~(" + name + ")" : name);
    }
}
