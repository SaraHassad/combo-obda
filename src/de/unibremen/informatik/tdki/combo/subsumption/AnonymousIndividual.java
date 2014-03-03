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
public class AnonymousIndividual {

    private Role role;
    private boolean copy;

    public AnonymousIndividual(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        String result = "c_" + role;
        if (copy) {
            result += "1";
        } else {
            result += "0";
        }
        return result;
    }

    public AnonymousIndividual(Role role, boolean copy) {
        this.role = role;
        this.copy = copy;
    }

    public boolean isCopy() {
        return copy;
    }

    public void setCopy(boolean copy) {
        this.copy = copy;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnonymousIndividual other = (AnonymousIndividual) obj;
        if (this.role != other.role && (this.role == null || !this.role.equals(other.role))) {
            return false;
        }
        if (this.copy != other.copy) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.role != null ? this.role.hashCode() : 0);
        hash = 79 * hash + (this.copy ? 1 : 0);
        return hash;
    }
    
    

    
}
