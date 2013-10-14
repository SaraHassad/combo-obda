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
 * This class represents a concept expression formed by a binary constructor of
 * union or intersection from description logics.
 *
 * @author İnanç Seylan
 *
 */
public class BooleanConcept extends Concept {

    /**
     * Binary Constructor
     *
     */
    public enum Constructor {

        /**
         * Intersection concept constructor
         */
        INTERSECTION,
        /**
         * Union concept constructor
         */
        UNION;
    }
    /**
     * The binary constructor used in this concept expression
     */
    private Constructor constructor;
    /**
     * The concept that is on the left-hand side of this binary concept
     * expression
     */
    private Concept lhs;
    /**
     * The concept that is on the right-hand side of this binary concept
     * expression
     */
    private Concept rhs;

    /**
     * Default constructor
     */
    public BooleanConcept() {
    }

    /**
     * Constructor to initialize all the fields of this class by the given ones.
     *
     * @param constructor The type of the binary constructor in the concept
     * expression
     * @param lhs The concept that is on the left-hand side of the binary
     * constructor
     * @param rhs The concept that is on the right-hand side of the binary
     * constructor
     */
    public BooleanConcept(Constructor constructor, Concept lhs, Concept rhs) {
        setConstructor(constructor);
        setLhs(lhs);
        setRhs(rhs);
    }

    /**
     * @return the binary constructor used in this concept expression.
     */
    public Constructor getConstructor() {
        return constructor;
    }

    /**
     * Sets the binary constructor used in this concept expression.
     *
     * @param constructor The binary constructor
     */
    public void setConstructor(Constructor constructor) {
        this.constructor = constructor;
    }

    /**
     * @return the left-hand side concept of this binary concept expression.
     */
    public Concept getLhs() {
        return lhs;
    }

    /**
     * Sets the left-hand side concept of this binary concept expression.
     *
     * @param lhs The left-hand side concept
     */
    public void setLhs(Concept lhs) {
        this.lhs = lhs;
    }

    /**
     * @return the right-hand side concept of this binary concept expression.
     */
    public Concept getRhs() {
        return rhs;
    }

    /**
     * Sets the right-hand side concept of this binary concept expression.
     *
     * @param rhs The right-hand side concept
     */
    public void setRhs(Concept rhs) {
        this.rhs = rhs;
    }

    /**
     * @return false
     */
    public boolean isNegated() {
        return false;
    }

    /**
     * Changes the expression to union if it's an intersection or vice versa.
     */
    public Concept toggleNegated() {
        if (getConstructor() == Constructor.INTERSECTION) {
            setConstructor(Constructor.UNION);
        } else {
            setConstructor(Constructor.INTERSECTION);
        }
        setLhs(getLhs().toggleNegated());
        setRhs(getRhs().toggleNegated());
        return this;
    }

    /**
     * Calls {@link ConceptVisitor#visitBooleanConcept(BooleanConcept)} on
     * itself.
     */
    public void accept(ConceptVisitor v) {
        v.visitBooleanConcept(this);
    }

    /**
     * @see Copyable#copy()
     */
    public BooleanConcept copy() {
        BooleanConcept bd = new BooleanConcept();
        bd.setConstructor(getConstructor());
        bd.setLhs(getLhs().copy());
        bd.setRhs(getRhs().copy());
        return bd;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        buffer.append(getLhs());
        if (getConstructor() == Constructor.INTERSECTION) {
            buffer.append(" & ");
        } else {
            buffer.append(" | ");
        }
        buffer.append(getRhs());
        buffer.append(")");
        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BooleanConcept other = (BooleanConcept) obj;
        if (this.constructor != other.constructor) {
            return false;
        }
        if (this.lhs != other.lhs && (this.lhs == null || !this.lhs.equals(other.lhs))) {
            return false;
        }
        if (this.rhs != other.rhs && (this.rhs == null || !this.rhs.equals(other.rhs))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + (this.constructor != null ? this.constructor.hashCode() : 0);
        hash = 19 * hash + (this.lhs != null ? this.lhs.hashCode() : 0);
        hash = 19 * hash + (this.rhs != null ? this.rhs.hashCode() : 0);
        return hash;
    }
}
