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
package de.unibremen.informatik.tdki.combo.syntax.query;

/**
 *
 * @author İnanç Seylan
 */
public class RoleAtom implements Query {

    private String roleName;
    private String leftTerm;
    private String rightTerm;

    public RoleAtom() {
    }

    public RoleAtom(String roleName, String leftTerm, String rightTerm) {
        setRoleName(roleName);
        setLeftTerm(leftTerm);
        setRightTerm(rightTerm);
    }

    /**
     * @return the roleName
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * @param roleName the roleName to set
     */
    final public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * @return the leftTerm
     */
    public String getLeftTerm() {
        return leftTerm;
    }

    /**
     * @param leftTerm the leftTerm to set
     */
    final public void setLeftTerm(String leftTerm) {
        this.leftTerm = leftTerm;
    }

    /**
     * @return the rightTerm
     */
    public String getRightTerm() {
        return rightTerm;
    }

    /**
     * @param rightTerm the rightTerm to set
     */
    final public void setRightTerm(String rightTerm) {
        this.rightTerm = rightTerm;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RoleAtom other = (RoleAtom) obj;
        if ((this.roleName == null) ? (other.roleName != null) : !this.roleName.equals(other.roleName)) {
            return false;
        }
        if ((this.leftTerm == null) ? (other.leftTerm != null) : !this.leftTerm.equals(other.leftTerm)) {
            return false;
        }
        if ((this.rightTerm == null) ? (other.rightTerm != null) : !this.rightTerm.equals(other.rightTerm)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.roleName != null ? this.roleName.hashCode() : 0);
        hash = 23 * hash + (this.leftTerm != null ? this.leftTerm.hashCode() : 0);
        hash = 23 * hash + (this.rightTerm != null ? this.rightTerm.hashCode() : 0);
        return hash;
    }

    @Override
    public void accept(QueryVisitor v) {
        v.visitRoleAtom(this);
    }

    @Override
    public String toString() {
        return roleName + "(" + leftTerm + "," + rightTerm + ")";
    }

    @Override
    public Head getHead() {
        return new Head(getRoleName(), getLeftTerm(), getRightTerm());
    }
}
