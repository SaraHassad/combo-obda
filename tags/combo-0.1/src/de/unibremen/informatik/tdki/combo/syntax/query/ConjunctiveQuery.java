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

import java.util.*;

/**
 *
 * @author İnanç Seylan
 */
// TODO make this class immutable because of the associated checks when adding a head and atoms
public class ConjunctiveQuery implements Query {

    private List<Query> atoms = new ArrayList<Query>();
    /**
     * Defining terms as LinkedHashSet is very important because we later use
     * the order of the items in terms for rewriting to SQL.
     */
    private LinkedHashSet<String> terms = new LinkedHashSet<String>();
    private Map<String, List<Query>> termFormulaMap = new HashMap<String, List<Query>>();
    private Head head;

    /**
     * Default constructor
     */
    public ConjunctiveQuery() {
    }

    public ConjunctiveQuery(Head head, Query... formulaArray) {
        for (Query f : formulaArray) {
            addQuery(f);
        }
        setHead(head);
    }

    public boolean isBoolean() {
        return head.getVariables().isEmpty();
    }

    @Override
    public Head getHead() {
        return head;
    }

    final public void setHead(final Head head) {
        // check that the head does not appear in the body
        for (Query f : atoms) {
            if (f.getHead().getPredicateName().equals(head.getPredicateName())) {
                throw new IllegalArgumentException(head + " already appears in the body of " + f);
            }
        }
        // check that the head variables appear in the body
        for (String distinguished : head.getVariables()) {
            if (!terms.contains(distinguished)) {
                throw new IllegalArgumentException("The head variable " + distinguished + " does not exist in the query " + this);
            }
        }
        this.head = head;
    }

    public List<Query> getQueries() {
        return Collections.unmodifiableList(atoms);
    }

    public boolean hasQuery(Query q) {
        return atoms.contains(q);
    }

    final public void addQuery(Query q) {
        TermCollectionVisitor v = new TermCollectionVisitor(terms, termFormulaMap);
        q.accept(v);
        atoms.add(q);
    }

    public List<Query> getMentioningQueries(String t) {
        if (!terms.contains(t)) {
            throw new IllegalArgumentException("The variable " + t + " does not exist in the query.");
        }
        return termFormulaMap.get(t);
    }

    public Set<String> getTerms() {
        return Collections.unmodifiableSet(terms);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.atoms != null ? this.atoms.hashCode() : 0);
        hash = 67 * hash + (this.head != null ? this.head.hashCode() : 0);
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
        final ConjunctiveQuery other = (ConjunctiveQuery) obj;
        if (this.atoms != other.atoms && (this.atoms == null || !this.atoms.equals(other.atoms))) {
            return false;
        }
        if (this.head != other.head && (this.head == null || !this.head.equals(other.head))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(head);
        builder.append(" <- ");
        for (Query f : atoms) {
            builder.append(f);
            builder.append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        return builder.toString();
    }

    @Override
    public void accept(QueryVisitor v) {
        v.visitCQ(this);
    }
}
