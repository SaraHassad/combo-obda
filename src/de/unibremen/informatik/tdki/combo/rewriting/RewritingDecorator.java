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
package de.unibremen.informatik.tdki.combo.rewriting;

import de.unibremen.informatik.tdki.combo.data.DBLayout;
import de.unibremen.informatik.tdki.combo.syntax.sql.*;

/**
 *
 * @author İnanç Seylan
 */
public class RewritingDecorator {

    public static Table withIndividualNames(Table t, String project) {
        if (t.getArity() == 0) {
            return t;
        } else {
            SFWQuery result = new SFWQuery();
            WhereComposite where = new WhereComposite();
            result.setWhere(where);
            StringBuilder tableAlias = new StringBuilder();
            tableAlias.append("Q(");
            for (int i = 0; i < t.getArity(); i++) {
                // create alias for the columns of the original query
                tableAlias.append("p");
                tableAlias.append(i);
                if (i < t.getArity() - 1) {
                    tableAlias.append(",");
                }
                // add selects and joins to the Symbols table
                result.addSelect("S" + i + ".name");
                BaseTable symbols = new BaseTable(DBLayout.getTableSymbols(project), 2);
                result.addFrom(symbols, "S" + i);
                where.addConjunct(new WhereCondition(WhereCondition.Operator.EQUAL, "S" + i + ".id", "Q." + "p" + i));
            }
            tableAlias.append(")");
            result.addFrom(t, tableAlias.toString());
            return result;
        }
    }

    public static SFWQuery withCount(Table t) {
        return SFWQuery.createCount(t);
    }

    public static SFWQuery withDistinct(Table t) {
        return SFWQuery.createDistinct(t);
    }
}
