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
package syntax;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import de.unibremen.informatik.tdki.combo.syntax.query.ConceptAtom;
import de.unibremen.informatik.tdki.combo.syntax.query.ConjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.query.DisjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.query.Head;
import de.unibremen.informatik.tdki.combo.syntax.query.NRDatalogProgram;
import de.unibremen.informatik.tdki.combo.syntax.query.NRDatalogToPEQConverter;
import de.unibremen.informatik.tdki.combo.syntax.sql.BaseTable;
import de.unibremen.informatik.tdki.combo.syntax.sql.SFWQuery;
import de.unibremen.informatik.tdki.combo.syntax.sql.UnionQuery;
import de.unibremen.informatik.tdki.combo.syntax.sql.WhereComposite;
import de.unibremen.informatik.tdki.combo.syntax.sql.WhereCondition;

/**
 *
 * @author İnanç Seylan
 */
public class SQLTest {

    SFWQuery q;

    @Before
    public void setUp() {
        q = new SFWQuery();
    }

    @Test
    public void testBasicCondition() {
        q.addSelect("name");
        q.addFrom(new BaseTable("People", 2));
        q.setWhere(new WhereCondition(WhereCondition.Operator.EQUAL, "ssn", "100"));
        String expected = "SELECT name\nFROM People\nWHERE ssn=100";
        Assert.assertEquals(expected, q.toString());
    }

    @Test
    public void testCompositeWhere() {
        q.addSelect("name");
        q.addFrom(new BaseTable("People", 2));
        WhereComposite where = new WhereComposite();
        where.addConjunct(new WhereCondition(WhereCondition.Operator.EQUAL, "ssn", "100"));
        q.setWhere(where);

        String expected = "SELECT name\nFROM People\nWHERE (ssn=100)";
        Assert.assertEquals(expected, q.toString());
    }

    @Test
    public void testWhereWithAndOr() {
        q.addSelect("name");
        q.addSelect("address");
        q.addFrom(new BaseTable("People people", 2));
        q.addFrom(new BaseTable("Patient patient", 2));
        WhereComposite where = new WhereComposite();
        final WhereCondition c1 = new WhereCondition(WhereCondition.Operator.EQUAL, "people.ssn", "patient.ssn");
        final WhereCondition c2 = new WhereCondition(WhereCondition.Operator.EQUAL, "patient.diagnosis", "\'cancer\'");
        where.addConjunct(c1);
        where.addConjunct(c2);
        q.setWhere(where);

        String expected = "SELECT name, address\nFROM People people,\nPatient patient\nWHERE (people.ssn=patient.ssn AND\npatient.diagnosis=\'cancer\')";
        Assert.assertEquals(expected, q.toString());

        where = new WhereComposite();
        where.addDisjunct(c1);
        where.addDisjunct(c2);
        q.setWhere(where);
        expected = "SELECT name, address\nFROM People people,\nPatient patient\nWHERE (people.ssn=patient.ssn OR\npatient.diagnosis=\'cancer\')";
        Assert.assertEquals(expected, q.toString());
    }

    @Test
    public void testNoWhere() {
        q.addSelect("name");
        q.addFrom(new BaseTable("People", 2));
        String expected = "SELECT name\nFROM People";
        Assert.assertEquals(expected, q.toString());
    }
    
    @Test
    public void testArity() {
        BaseTable bt = new BaseTable("R", 1);
        Assert.assertEquals(1, bt.getArity());

        SFWQuery sfw = new SFWQuery();
        org.junit.Assert.assertEquals(0, sfw.getArity());
        sfw.addSelect("2");
        Assert.assertEquals(1, sfw.getArity());

        UnionQuery uq = new UnionQuery();
        uq.add(bt);
        uq.add(sfw);
        Assert.assertEquals(1, uq.getArity());
    }
}
