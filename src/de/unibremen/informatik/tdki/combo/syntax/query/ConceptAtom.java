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
public class ConceptAtom implements Query {

    private String cn;
    private String term;

    public ConceptAtom() {
    }

    public ConceptAtom(String cn, String term) {
        setConceptName(cn);
        setTerm(term);
    }

    /**
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    /**
     * @param term the term to set
     */
    public void setTerm(String term) {
        this.term = term;
    }

    /**
     * @return the cn
     */
    public String getConceptName() {
        return cn;
    }

    /**
     * @param cn the cn to set
     */
    public void setConceptName(String cn) {
        this.cn = cn;
    }

    @Override
    public void accept(QueryVisitor v) {
        v.visitConceptAtom(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConceptAtom other = (ConceptAtom) obj;
        if ((this.cn == null) ? (other.cn != null) : !this.cn.equals(other.cn)) {
            return false;
        }
        if ((this.term == null) ? (other.term != null) : !this.term.equals(other.term)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.cn != null ? this.cn.hashCode() : 0);
        hash = 53 * hash + (this.term != null ? this.term.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return cn + "(" + term + ")";
    }

    @Override
    public Head getHead() {
        return new Head(getConceptName(), getTerm());
    }
}
