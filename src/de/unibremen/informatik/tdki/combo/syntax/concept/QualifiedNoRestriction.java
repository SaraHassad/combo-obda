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
import de.unibremen.informatik.tdki.combo.syntax.Role;

/**
 * This class represents a qualified number restriction expression.
 *
 * @author İnanç Seylan
 */
public class QualifiedNoRestriction extends Concept {

    private InequalityConstructor constructor;
    private int no;
    private Role role;
    private Concept concept;

    public QualifiedNoRestriction() {
    }

    public QualifiedNoRestriction(InequalityConstructor constructor, int no, Role role,
            Concept concept) {
        setConstructor(constructor);
        setNo(no);
        setRole(role);
        setConcept(concept);
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public InequalityConstructor getConstructor() {
        return constructor;
    }

    public void setConstructor(InequalityConstructor constructor) {
        this.constructor = constructor;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Changes the expression to at least if it's at most restriction or vice
     * versa. There are three exceptions, though. If this is of the form <=(0,
     * R, C), it changes to some(R, C). If this is of the form >=(0, R, C), it
     * changes to bottom concept. If this is of the form >=(1, R, C), it changes
     * to all(R, ~C).
     */
    public Concept toggleNegated() {
        Concept result = this;
        if (getConstructor().equals(InequalityConstructor.AT_MOST)) {
            if (getNo() == 0) {
                result = new RoleRestriction(
                        RoleRestriction.Constructor.SOME, getRole(),
                        getConcept());
            } else {
                setConstructor(InequalityConstructor.AT_LEAST);
                no++;
            }
        } else {
            if (getNo() == 0) {
                result = ConceptName.bottomConcept();
            } else if (getNo() == 1) {
                result = new RoleRestriction(
                        RoleRestriction.Constructor.ALL, getRole(),
                        getConcept().copy().toggleNegated());
            } else {
                setConstructor(InequalityConstructor.AT_MOST);
                no--;
            }
        }
        return result;
    }

    /**
     * @return false
     */
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
        final QualifiedNoRestriction other = (QualifiedNoRestriction) obj;
        if (this.constructor != other.constructor) {
            return false;
        }
        if (this.no != other.no) {
            return false;
        }
        if (this.role != other.role && (this.role == null || !this.role.equals(other.role))) {
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
        hash = 29 * hash + (this.constructor != null ? this.constructor.hashCode() : 0);
        hash = 29 * hash + this.no;
        hash = 29 * hash + (this.role != null ? this.role.hashCode() : 0);
        hash = 29 * hash + (this.concept != null ? this.concept.hashCode() : 0);
        return hash;
    }

    /**
     * @see Copyable#copy()
     */
    public QualifiedNoRestriction copy() {
        QualifiedNoRestriction qr = new QualifiedNoRestriction();
        qr.setConstructor(getConstructor());
        qr.setNo(no);
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
        buffer.append(getNo());
        buffer.append(", ");
        buffer.append(getRole().toString());
        buffer.append(", ");
        buffer.append(getConcept());
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * Calls
     * {@link ConceptVisitor#visitQualifiedNoRestriction(QualifiedNoRestriction)}
     * on itself.
     */
    public void accept(ConceptVisitor v) {
        v.visitQualifiedNoRestriction(this);
    }
}
