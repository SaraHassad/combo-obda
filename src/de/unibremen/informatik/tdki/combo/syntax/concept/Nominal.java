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
 * This class represents a singleton named individual set.
 *
 * @author İnanç Seylan
 */
public class Nominal extends Concept {

    private boolean negated;
    private String name;

    /**
     * Default constructor
     */
    public Nominal() {
    }

    /**
     * Constructor to set the name field
     *
     * @param name The concept name to be set
     */
    public Nominal(String name) {
        setName(name);
    }

    public String getName() {
        return name.toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see Negatable#isNegated()
     */
    public boolean isNegated() {
        return negated;
    }

    /**
     * @see Negatable#toggleNegated()
     */
    public Concept toggleNegated() {
        negated = !negated;
        return this;
    }

    /**
     * @see Copyable#copy()
     */
    public Nominal copy() {
        Nominal n = new Nominal();
        n.name = getName();
        n.negated = negated;
        return n;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Nominal other = (Nominal) obj;
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
        int hash = 7;
        hash = 19 * hash + (this.negated ? 1 : 0);
        hash = 19 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String name = "{" + getName() + "}";
        return (isNegated() ? "~" + name : name);
    }

    /**
     * Calls {@link ConceptVisitor#visitNominal(Nominal)} on itself.
     */
    public void accept(ConceptVisitor v) {
        v.visitNominal(this);
    }
}
