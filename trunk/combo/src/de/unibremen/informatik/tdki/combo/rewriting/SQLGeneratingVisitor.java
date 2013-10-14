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

import java.util.Iterator;
import de.unibremen.informatik.tdki.combo.syntax.query.ConceptAtom;
import de.unibremen.informatik.tdki.combo.syntax.query.ConjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.query.DisjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.query.Query;
import de.unibremen.informatik.tdki.combo.syntax.query.QueryVisitor;
import de.unibremen.informatik.tdki.combo.syntax.query.RoleAtom;
import de.unibremen.informatik.tdki.combo.syntax.sql.SFWQuery;
import de.unibremen.informatik.tdki.combo.syntax.sql.Table;
import de.unibremen.informatik.tdki.combo.syntax.sql.UnionQuery;
import de.unibremen.informatik.tdki.combo.syntax.sql.WhereComposite;
import de.unibremen.informatik.tdki.combo.syntax.sql.WhereCondition;

/**
 *
 * @author İnanç Seylan
 */
public class SQLGeneratingVisitor implements QueryVisitor {

    private Table table;
    private AtomicFormulaSQLAdapter atomAdapter;
    private CQAtomNamingScheme cqNamingScheme;

    public SQLGeneratingVisitor(AtomicFormulaSQLAdapter atomAdapter, CQAtomNamingScheme cqNamingScheme) {
        this.atomAdapter = atomAdapter;
        this.cqNamingScheme = cqNamingScheme;
    }

    public Table getTable() {
        return table;
    }

    @Override
    public void visitConceptAtom(ConceptAtom ca) {
        SFWQuery sql = atomAdapter.adapt(ca);
        table = sql;
    }

    @Override
    public void visitRoleAtom(RoleAtom ra) {
        SFWQuery sql = atomAdapter.adapt(ra);
        table = sql;
    }

    @Override
    public void visitCQ(ConjunctiveQuery cq) {
        SFWQuery sql = new SFWQuery();
        generateSelect(cq, sql);
        generateFrom(cq, sql);
        generateWhere(cq, sql);
        table = sql;
    }

    @Override
    public void visitUCQ(DisjunctiveQuery dq) {
        UnionQuery ucq = new UnionQuery();
        for (Query q : dq.getQueries()) {
            q.accept(this);
            ucq.add(getTable());
        }
        table = ucq;
    }

    private SFWQuery generateWhere(ConjunctiveQuery cq, SFWQuery sql) {
        WhereComposite where = new WhereComposite();
        // handle appearences of the same variable in different conjuncts
        // note that atomic formulas of the form R(x,x) should be handled already
        for (String v : cq.getTerms()) {
            if (cq.getMentioningQueries(v).size() > 1) {
                Iterator<Query> queryIterator = cq.getMentioningQueries(v).iterator();
                Query referenceQ = queryIterator.next(); // the first atom which has this variable is our reference
                String referenceColumn = cqNamingScheme.getColumnName(v, referenceQ, cq);
                while (queryIterator.hasNext()) {
                    Query otherQ = queryIterator.next();
                    String otherColumn = cqNamingScheme.getColumnName(v, otherQ, cq);
                    where.addConjunct(new WhereCondition(WhereCondition.Operator.EQUAL, referenceColumn, otherColumn));
                }
            }
        }
        if (!where.isEmpty()) {
            sql.setWhere(where);
        }
        return sql;
    }

    private void generateSelect(ConjunctiveQuery cq, SFWQuery sql) {
        for (String v : cq.getHead().getVariables()) {
            Query referenceQ = cq.getMentioningQueries(v).iterator().next();
            String column = cqNamingScheme.getColumnName(v, referenceQ, cq);
            sql.addSelect(column);
        }
    }

    private void generateFrom(final ConjunctiveQuery cq, SFWQuery sql) {
        for (final Query atom : cq.getQueries()) {
            atom.accept(this);
            sql.addFrom(getTable(), cqNamingScheme.getTableSignature(atom, cq));
        }
    }
}
