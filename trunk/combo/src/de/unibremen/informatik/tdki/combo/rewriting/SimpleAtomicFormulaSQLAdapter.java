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

import de.unibremen.informatik.tdki.combo.syntax.query.ConceptAtom;
import de.unibremen.informatik.tdki.combo.syntax.query.RoleAtom;
import de.unibremen.informatik.tdki.combo.syntax.sql.BaseTable;
import de.unibremen.informatik.tdki.combo.syntax.sql.SFWQuery;
import de.unibremen.informatik.tdki.combo.syntax.sql.WhereCondition;

/**
 *
 * @author İnanç Seylan
 */
public class SimpleAtomicFormulaSQLAdapter implements AtomicFormulaSQLAdapter {

    @Override
    public SFWQuery adapt(ConceptAtom ca) {
        SFWQuery result = new SFWQuery();
        result.addSelect("individual");
        result.addFrom(new BaseTable(ca.getConceptName(), 1));
        return result;
    }

    @Override
    public SFWQuery adapt(RoleAtom ra) {
        SFWQuery result = new SFWQuery();
        result.addSelect("lhs");
        result.addSelect("rhs");
        result.addFrom(new BaseTable(ra.getRoleName(), 2));
        if (ra.getLeftTerm().equals(ra.getRightTerm())) {
            result.setWhere(new WhereCondition(WhereCondition.Operator.EQUAL, "lhs", "rhs"));
        }
        return result;
    }
}
