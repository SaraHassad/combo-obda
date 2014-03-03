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
 * @author İnanç Seylan
 */
public class CardinalityRestriction extends Concept {

    private InequalityConstructor constructor;
    private int no;
    private Concept concept;

    public CardinalityRestriction(InequalityConstructor constructor, int no,
            Concept concept) {
        super();
        this.constructor = constructor;
        this.no = no;
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

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    @Override
    public boolean isNegated() {
        return false;
    }

    @Override
    public Concept toggleNegated() {
        Concept result = this;
        if (getConstructor().equals(InequalityConstructor.AT_MOST)) {
            setConstructor(InequalityConstructor.AT_LEAST);
            no++;
        } else {
            if (getNo() == 0) {
                result = ConceptName.bottomConcept();
            } else {
                setConstructor(InequalityConstructor.AT_MOST);
                no--;
            }
        }
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
        final CardinalityRestriction other = (CardinalityRestriction) obj;
        if (this.constructor != other.constructor) {
            return false;
        }
        if (this.no != other.no) {
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
        hash = 29 * hash + (this.concept != null ? this.concept.hashCode() : 0);
        return hash;
    }

    @Override
    public void accept(ConceptVisitor v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @see Copyable#copy()
     */
    public QualifiedNoRestriction copy() {
        QualifiedNoRestriction qr = new QualifiedNoRestriction();
        qr.setConstructor(getConstructor());
        qr.setNo(no);
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
        buffer.append(getConcept());
        buffer.append(")");
        return buffer.toString();
    }
}
