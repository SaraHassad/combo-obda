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
package reasoning;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.Before;
import de.unibremen.informatik.tdki.combo.rewriting.AtomicFormulaSQLAdapter;
import de.unibremen.informatik.tdki.combo.rewriting.SQLGeneratingVisitor;
import de.unibremen.informatik.tdki.combo.rewriting.SimpleAtomicFormulaSQLAdapter;
import de.unibremen.informatik.tdki.combo.rewriting.SimpleCQAtomNamingScheme;
import de.unibremen.informatik.tdki.combo.syntax.query.ConceptAtom;
import de.unibremen.informatik.tdki.combo.syntax.query.ConjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.query.DisjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.query.Head;
import de.unibremen.informatik.tdki.combo.syntax.query.NRDatalogProgram;
import de.unibremen.informatik.tdki.combo.syntax.query.NRDatalogToPEQConverter;
import de.unibremen.informatik.tdki.combo.syntax.query.Query;
import de.unibremen.informatik.tdki.combo.syntax.query.QueryParser;
import de.unibremen.informatik.tdki.combo.syntax.query.RoleAtom;
import de.unibremen.informatik.tdki.combo.syntax.sql.BaseTable;
import de.unibremen.informatik.tdki.combo.syntax.sql.SFWQuery;
import de.unibremen.informatik.tdki.combo.syntax.sql.UnionQuery;
import de.unibremen.informatik.tdki.combo.syntax.sql.Where;
import de.unibremen.informatik.tdki.combo.syntax.sql.WhereComposite;
import de.unibremen.informatik.tdki.combo.syntax.sql.WhereCondition;

/**
 *
 * @author İnanç Seylan
 */
public class SQLGeneratingVisitorTest {

    private SQLGeneratingVisitor visitor;

    @Before
    public void setUp() {
        visitor = new SQLGeneratingVisitor(new SimpleAtomicFormulaSQLAdapter(), new SimpleCQAtomNamingScheme());
    }

    @Test
    public void testSimpleAtomicFormulaSQLAdapter() {
        AtomicFormulaSQLAdapter adapter = new SimpleAtomicFormulaSQLAdapter();
        SFWQuery expected = new SFWQuery();
        expected.addSelect("individual");
        expected.addFrom(new BaseTable("A", 1));
        SFWQuery actual = adapter.adapt(new ConceptAtom("A", "x"));
        Assert.assertEquals(expected, actual);

        expected = new SFWQuery();
        expected.addSelect("lhs");
        expected.addSelect("rhs");
        expected.addFrom(new BaseTable("R", 2));
        actual = adapter.adapt(new RoleAtom("R", "x", "y"));
        Assert.assertEquals(expected, actual);

        expected = new SFWQuery();
        expected.addSelect("lhs");
        expected.addSelect("rhs");
        expected.addFrom(new BaseTable("R", 2));
        expected.setWhere(new WhereCondition(WhereCondition.Operator.EQUAL, "lhs", "rhs"));
        actual = adapter.adapt(new RoleAtom("R", "x", "x"));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testUCQ() {
        // I have already tested the correctness of SQL generation for atomic formulas in the previous method.
        // Therefore I don't here.
        AtomicFormulaSQLAdapter adapter = new SimpleAtomicFormulaSQLAdapter();

        ConjunctiveQuery cq1 = new ConjunctiveQuery(new Head("Q", "x"),
                new RoleAtom("R", "x", "y"), new RoleAtom("S", "y", "z"), new ConceptAtom("A", "z"), new ConceptAtom("B", "z"));
        SFWQuery sfw1 = new SFWQuery();
        SFWQuery rxy = adapter.adapt(new RoleAtom("R", "x", "y"));
        sfw1.addFrom(rxy, "T0(p0,p1)");
        SFWQuery syz = adapter.adapt(new RoleAtom("S", "y", "z"));
        sfw1.addFrom(syz, "T1(p0,p1)");
        SFWQuery az = adapter.adapt(new ConceptAtom("A", "z"));
        sfw1.addFrom(az, "T2(p0)");
        SFWQuery bz = adapter.adapt(new ConceptAtom("B", "z"));
        sfw1.addFrom(bz, "T3(p0)");
        sfw1.addSelect("T0.p0");
        WhereComposite where = new WhereComposite();
        where.addConjunct(new WhereCondition(WhereCondition.Operator.EQUAL, "T0.p1", "T1.p0"));
        where.addConjunct(new WhereCondition(WhereCondition.Operator.EQUAL, "T1.p1", "T2.p0"));
        where.addConjunct(new WhereCondition(WhereCondition.Operator.EQUAL, "T1.p1", "T3.p0"));
        sfw1.setWhere(where);

        ConjunctiveQuery cq2 = new ConjunctiveQuery(new Head("Q", "x"), new RoleAtom("U", "x", "x"));
        SFWQuery sfw2 = new SFWQuery();
        SFWQuery uxx = adapter.adapt(new RoleAtom("U", "x", "x"));
        sfw2.addFrom(uxx, "T0(p0,p1)");
        sfw2.addSelect("T0.p0");

        DisjunctiveQuery ucq = new DisjunctiveQuery();
        ucq.add(cq1);
        ucq.add(cq2);
        UnionQuery unq = new UnionQuery();
        unq.add(sfw1);
        unq.add(sfw2);
        ucq.accept(visitor);

        Assert.assertEquals(unq, visitor.getTable());
    }

    @Test
    public void testNoHeadVariable() {
        ConjunctiveQuery q = new ConjunctiveQuery(new Head("Q"), new RoleAtom("R", "x", "y"));
        q.accept(visitor);

        SFWQuery expected = new SFWQuery();
        AtomicFormulaSQLAdapter adapter = new SimpleAtomicFormulaSQLAdapter();
        SFWQuery table = adapter.adapt(new RoleAtom("R", "x", "y"));
        expected.addFrom(table, "T0(p0,p1)");

        Assert.assertEquals(expected, visitor.getTable());
    }

    @Test
    public void testDatalogToSql() {
        ConjunctiveQuery q1 = new ConjunctiveQuery(new Head("Q", "?1", "?4"), new RoleAtom("memberOf_..", "?3", "?1"), new RoleAtom("memberOf_..", "?3", "?4"));
        ConjunctiveQuery q2 = new ConjunctiveQuery(new Head("memberOf_..", "?x", "?y"), new RoleAtom("memberOf", "?x", "?y"));
        NRDatalogProgram program = new NRDatalogProgram();
        program.addRule(q1);
        program.addRule(q2);
        program.setHeadPredicate("Q");
        NRDatalogToPEQConverter converter = new NRDatalogToPEQConverter();
        Query q = converter.convert(program);
        System.out.println(q);
        
        q.accept(visitor);
        
        SFWQuery inner = new SFWQuery();
        inner.addSelect("T0.p0");
        inner.addSelect("T0.p1");
        AtomicFormulaSQLAdapter adapter = new SimpleAtomicFormulaSQLAdapter();
        SFWQuery table = adapter.adapt(new RoleAtom("memberOf", "x", "y"));
        inner.addFrom(table, "T0(p0,p1)");
        
        UnionQuery u1 = new UnionQuery();
        u1.add(inner);
        
        UnionQuery u2 = new UnionQuery();
        u2.add(inner);
        
        SFWQuery sfw = new SFWQuery();
        sfw.addSelect("T0.p1");
        sfw.addSelect("T1.p1");
        sfw.addFrom(u1, "T0(p0,p1)");
        sfw.addFrom(u2, "T1(p0,p1)");
        WhereComposite where = new WhereComposite();
        where.addConjunct(new WhereCondition(WhereCondition.Operator.EQUAL, "T0.p0", "T1.p0"));
        sfw.setWhere(where);
        
        UnionQuery expected = new UnionQuery();
        expected.add(sfw);
        
        Assert.assertEquals(expected, visitor.getTable());
    }
}
