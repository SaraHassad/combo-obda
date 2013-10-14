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
package de.unibremen.informatik.tdki.combo.syntax;

import de.unibremen.informatik.tdki.combo.common.Copyable;

/**
 *
 * @author İnanç Seylan
 *
 */
public class Role implements Copyable<Role> {

    private String name;
    private boolean isInverse;

    public Role() {
    }

    public Role(String name) {
        setName(name);
    }

    public Role(String name, boolean inverse) {
        setName(name);
        setInverse(inverse);
    }

    public Role toggleInverse() {
        setInverse(!isInverse());
        return this;
    }

    /**
     * @return the isInverse
     */
    public boolean isInverse() {
        return isInverse;
    }

    /**
     * @param isInverse the isInverse to set
     */
    public void setInverse(boolean isInverse) {
        this.isInverse = isInverse;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name.toString();
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public Role copy() {
        Role result = new Role();
        result.name = this.name;
        result.isInverse = this.isInverse;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Role other = (Role) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.isInverse != other.isInverse) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + (this.isInverse ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        return (isInverse() ? getName() + "-" : name);
    }
}
