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
package de.unibremen.informatik.tdki.combo.subsumption;

import de.unibremen.informatik.tdki.combo.syntax.Role;

/**
 *
 * @author İnanç Seylan
 */
public class AnonymousRoleAssertion {

    private Role role;
    private AnonymousIndividual lhs, rhs;

    public AnonymousRoleAssertion() {
    }

    public AnonymousRoleAssertion(Role role, AnonymousIndividual lhs, AnonymousIndividual rhs) {
        this.role = role;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AnonymousIndividual getLhs() {
        return lhs;
    }

    public void setLhs(AnonymousIndividual lhs) {
        this.lhs = lhs;
    }

    public AnonymousIndividual getRhs() {
        return rhs;
    }

    public void setRhs(AnonymousIndividual rhs) {
        this.rhs = rhs;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.role != null ? this.role.hashCode() : 0);
        hash = 97 * hash + (this.lhs != null ? this.lhs.hashCode() : 0);
        hash = 97 * hash + (this.rhs != null ? this.rhs.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnonymousRoleAssertion other = (AnonymousRoleAssertion) obj;
        if (this.role != other.role && (this.role == null || !this.role.equals(other.role))) {
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
    public String toString() {
        return role + "(" + lhs + ", " + rhs + ")";
    }
}
