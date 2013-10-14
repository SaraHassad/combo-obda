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
package de.unibremen.informatik.tdki.combo.syntax.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author İnanç Seylan
 */
public class UnionQuery implements Table {

    private List<Table> tables = new ArrayList<Table>();
    
    private int arity;

    public void add(Table e) {
        if (tables.isEmpty()) {
            arity = e.getArity();
        } else {
            assert (arity == e.getArity());
        }
        tables.add(e);
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tables.size(); i++) {
            builder.append(tables.get(i).toString());
            if (i < tables.size() - 1) {
                builder.append('\n');
                builder.append("UNION\n");
            }
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.tables != null ? this.tables.hashCode() : 0);
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
        final UnionQuery other = (UnionQuery) obj;
        if (this.tables != other.tables && (this.tables == null || !this.tables.equals(other.tables))) {
            return false;
        }
        return true;
    }

    @Override
    public int getArity() {
        return arity;
    }

    @Override
    public void accept(TableVisitor v) {
        v.visitUnionQuery(this);
    }

}
