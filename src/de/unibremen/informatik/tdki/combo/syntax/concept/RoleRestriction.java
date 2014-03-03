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

import de.unibremen.informatik.tdki.combo.syntax.Role;

/**
 * This class represents a concept expression formed by some or all values
 * restrictions from description logics.
 *
 * @author İnanç Seylan
 *
 */
public class RoleRestriction extends Concept {

    /**
     * The restriction constructor
     *
     */
    public enum Constructor {

        /**
         * Some values from restriction constructor
         */
        SOME,
        /**
         * All values from restriction constructor
         */
        ALL;
    }
    /**
     * The role name which this constructor is applied to
     */
    private Role role;
    /**
     * The type of the constructor
     */
    private Constructor constructor;
    /**
     * The concept that the values of this role come from
     */
    private Concept concept;

    /**
     * Default constructor
     */
    public RoleRestriction() {
    }

    /**
     * Constructor to initialize all the fields of this class by the given ones.
     *
     * @param constructor The type of the constructor
     * @param role The role name
     * @param concept The concept expression
     */
    public RoleRestriction(Constructor constructor, Role role,
            Concept concept) {
        this.constructor = constructor;
        this.role = role;
        this.concept = concept;
    }
    
    public RoleRestriction(Constructor constructor, Role role) {
        this.constructor = constructor;
        this.role = role;
        this.concept = ConceptName.topConcept();
    }

    /**
     * @return the type of the constructor of this restriction.
     */
    public Constructor getConstructor() {
        return constructor;
    }

    /**
     * Sets the type of the constructor of this restriction.
     *
     * @param constructor The constructor to set.
     */
    public void setConstructor(Constructor constructor) {
        this.constructor = constructor;
    }

    /**
     * @return the concept expression.
     */
    public Concept getConcept() {
        return concept;
    }

    /**
     * Sets the concept expression.
     *
     * @param concept The concept expression to set
     */
    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    /**
     * @return the name of the role that this restriction is applied to.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the name of the role that this restriction is applied to.
     *
     * @param role The role name to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * @see
     * tr.edu.ege.seagent.alcbi.syntax.description.VisitableConcept#accept(tr.edu.ege.seagent.alcbi.syntax.description.ConceptVisitor)
     */
    public void accept(ConceptVisitor v) {
        v.visitRoleRestriction(this);
    }

    public Concept toggleNegated() {
        if (getConstructor().equals(Constructor.ALL)) {
            setConstructor(Constructor.SOME);
        } else {
            setConstructor(Constructor.ALL);
        }
        setConcept(getConcept().toggleNegated());
        return this;
    }

    public boolean isNegated() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RoleRestriction other = (RoleRestriction) obj;
        if (this.role != other.role && (this.role == null || !this.role.equals(other.role))) {
            return false;
        }
        if (this.constructor != other.constructor) {
            return false;
        }
        if (this.concept != other.concept && (this.concept == null || !this.concept.equals(other.concept))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.role != null ? this.role.hashCode() : 0);
        hash = 67 * hash + (this.constructor != null ? this.constructor.hashCode() : 0);
        hash = 67 * hash + (this.concept != null ? this.concept.hashCode() : 0);
        return hash;
    }

    public RoleRestriction copy() {
        RoleRestriction qr = new RoleRestriction();
        qr.setConstructor(getConstructor());
        qr.setRole(getRole().copy());
        qr.setConcept(getConcept().copy());
        return qr;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getConstructor());
        buffer.append("(");
        buffer.append(getRole().toString());
        buffer.append(", ");
        buffer.append(getConcept());
        buffer.append(")");
        return buffer.toString();
    }
}
