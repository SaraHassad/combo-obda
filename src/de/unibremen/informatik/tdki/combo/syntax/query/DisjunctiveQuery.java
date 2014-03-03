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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author İnanç Seylan
 */
// TODO make this class immutable because of the associated checks when adding a head and atoms
public class DisjunctiveQuery implements Query {

    private List<Query> queries = new ArrayList<Query>();
    private Head head;
    private int arity = -1;

    public DisjunctiveQuery() {
    }

    public DisjunctiveQuery(Query... queries) {
        for (Query q : queries) {
            add(q);
        }
    }

    public void add(Query q) {
        if (arity == -1) {
            arity = q.getHead().getVariables().size();
        } else if (arity != q.getHead().getVariables().size()) {
            throw new IllegalArgumentException("The query " + q + " must have " + arity + " distinguished variable(s) in order to be added to this UCQ.");
        }
        queries.add(q);
    }

    public List<Query> getQueries() {
        return Collections.unmodifiableList(queries);
    }

    @Override
    public Head getHead() {
        return head;
    }

    final public void setHead(final Head head) {
        if (arity != head.getVariables().size()) {
            throw new IllegalArgumentException("The head " + head + " must have " + arity + " variable(s).");
        }
        this.head = head;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.queries != null ? this.queries.hashCode() : 0);
        hash = 59 * hash + (this.head != null ? this.head.hashCode() : 0);
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
        final DisjunctiveQuery other = (DisjunctiveQuery) obj;
        if (this.queries != other.queries && (this.queries == null || !this.queries.equals(other.queries))) {
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
        builder.append(this.getHead()).append(" [");
        for (Query q : queries) {
            builder.append(q);
            builder.append('\n');
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void accept(QueryVisitor v) {
        v.visitUCQ(this);
    }
}
