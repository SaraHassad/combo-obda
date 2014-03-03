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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author İnanç Seylan
 */
public class WhereComposite implements Where {

    private enum Separator {

        AND, OR;
    }
    private List<Where> whereConditions = new ArrayList<Where>();
    private Map<Integer, Separator> separators = new HashMap<Integer, Separator>();
    private int currentPosition;

    public boolean isEmpty() {
        return whereConditions.isEmpty();
    }

    public void addConjunct(Where c) {
        addSeparator(Separator.AND);
        whereConditions.add(c);
    }

    public void addDisjunct(Where c) {
        addSeparator(Separator.OR);
        whereConditions.add(c);
    }

    public List<Where> getConditions() {
        return Collections.unmodifiableList(whereConditions);
    }

    private void addSeparator(Separator s) {
        if (!whereConditions.isEmpty()) {
            currentPosition++;
            separators.put(currentPosition, s);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (int i = 0; i < whereConditions.size(); i++) {
            if (separators.containsKey(i)) {
                switch (separators.get(i)) {
                    case AND:
                        builder.append(" AND\n");
                        break;
                    case OR:
                        builder.append(" OR\n");
                        break;
                }
            }
            builder.append(whereConditions.get(i));
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.whereConditions != null ? this.whereConditions.hashCode() : 0);
        hash = 37 * hash + (this.separators != null ? this.separators.hashCode() : 0);
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
        final WhereComposite other = (WhereComposite) obj;
        if (this.whereConditions != other.whereConditions && (this.whereConditions == null || !this.whereConditions.equals(other.whereConditions))) {
            return false;
        }
        if (this.separators != other.separators && (this.separators == null || !this.separators.equals(other.separators))) {
            return false;
        }
        return true;
    }
    
    
}
