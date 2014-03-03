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
import de.unibremen.informatik.tdki.combo.syntax.query.ConceptAtom;
import de.unibremen.informatik.tdki.combo.syntax.query.RoleAtom;
import de.unibremen.informatik.tdki.combo.syntax.sql.BaseTable;
import de.unibremen.informatik.tdki.combo.syntax.sql.SFWQuery;
import de.unibremen.informatik.tdki.combo.syntax.sql.WhereComposite;
import de.unibremen.informatik.tdki.combo.syntax.sql.WhereCondition;
import java.util.Map;

/**
 *
 * @author İnanç Seylan
 */
public class PredicateNameReplacingAdapter implements AtomicFormulaSQLAdapter {

    private Map<String, Integer> symbolIdMap;
    
    private String project;

    public PredicateNameReplacingAdapter(Map<String, Integer> mapping, String project) {
        this.symbolIdMap = mapping;
        this.project = project;
    }

    @Override
    public SFWQuery adapt(ConceptAtom ca) {
        SFWQuery result = new SFWQuery();
        if (!symbolIdMap.containsKey(ca.getConceptName())) {
            throw new RewritingException(ca.getConceptName() + " does not appear in the knowledge base.");
        }
        int id = symbolIdMap.get(ca.getConceptName());
        result.addSelect("individual");
        result.addFrom(new BaseTable(DBLayout.getTableConceptAssertions(project), 2));
        result.setWhere(new WhereCondition(WhereCondition.Operator.EQUAL, "concept", Integer.toString(id)));
        return result;
    }

    @Override
    public SFWQuery adapt(RoleAtom ra) {
        SFWQuery result = new SFWQuery();
        if (!symbolIdMap.containsKey(ra.getRoleName())) {
            throw new RewritingException(ra.getRoleName() + " does not appear in the knowledge base.");
        }
        int id = symbolIdMap.get(ra.getRoleName());
        result.addSelect("lhs");
        result.addSelect("rhs");
        result.addFrom(new BaseTable(DBLayout.getTableRoleAssertions(project), 3));
        WhereComposite where = new WhereComposite();
        result.setWhere(where);
        where.addConjunct(new WhereCondition(WhereCondition.Operator.EQUAL, "role", Integer.toString(id)));
        if (ra.getLeftTerm().equals(ra.getRightTerm())) {
            where.addConjunct(new WhereCondition(WhereCondition.Operator.EQUAL, "lhs", "rhs"));
        }
        return result;
    }
}
