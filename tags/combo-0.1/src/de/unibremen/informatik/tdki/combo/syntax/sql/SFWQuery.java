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
import java.util.Iterator;
import java.util.List;

class FromEntry {

    Table table;
    String alias = "";

    public FromEntry(Table t) {
        this.table = t;
    }

    public FromEntry(Table t, String alias) {
        this.table = t;
        this.alias = alias;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.table != null ? this.table.hashCode() : 0);
        hash = 17 * hash + (this.alias != null ? this.alias.hashCode() : 0);
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
        final FromEntry other = (FromEntry) obj;
        if (this.table != other.table && (this.table == null || !this.table.equals(other.table))) {
            return false;
        }
        if ((this.alias == null) ? (other.alias != null) : !this.alias.equals(other.alias)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        table.accept(new TableVisitor() {
            @Override
            public void visitBaseTable(BaseTable t) {
                builder.append(table.toString());
            }

            @Override
            public void visitSFWQuery(SFWQuery t) {
                visitNonBaseTable(t);
            }

            @Override
            public void visitUnionQuery(UnionQuery t) {
                visitNonBaseTable(t);
            }

            private void visitNonBaseTable(Table t) {
                builder.append("(").append(table).append(")");
            }
        });
        if (!getAlias().isEmpty()) {
            builder.append(" AS ").append(getAlias());
        }
        return builder.toString();
    }
}

/**
 *
 * @author İnanç Seylan
 */
public class SFWQuery implements Table {

    private List<String> select = new ArrayList<String>();
    private List<FromEntry> from = new ArrayList<FromEntry>();
    private Where where;

    public SFWQuery() {
    }

    public static SFWQuery createDistinct(Table t) {
        SFWQuery result = new SFWQuery();
        result.addSelect("DISTINCT *");
        result.addFrom(t);
        return result;
    }

    public static SFWQuery createCount(Table t) {
        SFWQuery result = new SFWQuery();
        result.addSelect("COUNT(*)");
        result.addFrom(t);
        return result;
    }

    public void addSelect(String s) {
        select.add(s);
    }

    public List<String> getSelect() {
        return Collections.unmodifiableList(select);
    }

    public void addFrom(Table t, String alias) {
        from.add(new FromEntry(t, alias));
    }

    public void addFrom(Table t) {
        from.add(new FromEntry(t));
    }

    public int getNextFromId() {
        return from.size();
    }

    public Where getWhere() {
        return where;
    }

    public void setWhere(Where where) {
        this.where = where;
    }

    public boolean hasWhere() {
        return (where != null);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        selectToString(builder);
        fromToString(builder);
        whereToString(builder);
        return builder.toString();
    }

    private void whereToString(StringBuilder builder) {
        if (hasWhere()) {
            builder.append("\nWHERE ");
            builder.append(where.toString());
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.select != null ? this.select.hashCode() : 0);
        hash = 29 * hash + (this.from != null ? this.from.hashCode() : 0);
        hash = 29 * hash + (this.where != null ? this.where.hashCode() : 0);
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
        final SFWQuery other = (SFWQuery) obj;
        if (this.select != other.select && (this.select == null || !this.select.equals(other.select))) {
            return false;
        }
        if (this.from != other.from && (this.from == null || !this.from.equals(other.from))) {
            return false;
        }
        if (this.where != other.where && (this.where == null || !this.where.equals(other.where))) {
            return false;
        }
        return true;
    }

    private void fromToString(StringBuilder builder) {
        builder.append("\nFROM ");
        Iterator<FromEntry> i = from.iterator();
        while (i.hasNext()) {
            builder.append(i.next());
            if (i.hasNext()) {
                builder.append(",\n");
            }
        }
    }

    private void selectToString(StringBuilder builder) {
        builder.append("SELECT ");
        if (getArity() == 0) {
            builder.append("1");
        } else {
            Iterator<String> i = select.iterator();
            while (i.hasNext()) {
                builder.append(i.next());
                if (i.hasNext()) {
                    builder.append(", ");
                }
            }
        }
    }

    @Override
    public int getArity() {
        return select.size();
    }

    @Override
    public void accept(TableVisitor v) {
        v.visitSFWQuery(this);
    }
}
