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

import de.unibremen.informatik.tdki.combo.syntax.query.ConjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.query.DisjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.query.Query;
import de.unibremen.informatik.tdki.combo.syntax.query.Head;
import de.unibremen.informatik.tdki.combo.syntax.query.RoleAtom;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.*;

/**
 *
 * @author İnanç Seylan
 */
public class QueryTest {

    @Test
    public void testCreation() {
        RoleAtom r1 = new RoleAtom("T", "x1", "y");
        RoleAtom r2 = new RoleAtom("T", "x2", "y");
        ConjunctiveQuery q = new ConjunctiveQuery(new Head("Q", "x1", "x2"), r1, r2);
        List<Query> expected = new ArrayList<Query>();
        expected.add(r1);
        assertEquals(expected, q.getMentioningQueries("x1"));
        expected.add(r2);
        assertEquals(expected, q.getMentioningQueries("y"));
        // it should not be possible that a head variable does not appear in the body
        try {
            q.setHead(new Head("Q", "z"));
            assertTrue(false); // should not come here
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
            assertTrue(true);
        }
        // it should not be possible that the head predicate appears in the body
        try {
            q.setHead(new Head("T", "y"));
            assertTrue(false); // should not come here
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
            assertTrue(true);
        }
    }

    @Test
    public void testUCQ() {
        ConjunctiveQuery q1 = new ConjunctiveQuery();
        q1.addQuery(new RoleAtom("headOf", "x", "y"));
        q1.addQuery(new RoleAtom("affiliatedOrganizationOf", "y", "z"));
        q1.setHead(new Head("Q", "x"));

        ConjunctiveQuery q2 = new ConjunctiveQuery();
        q2.addQuery(new RoleAtom("worksFor", "x", "y"));
        q2.addQuery(new RoleAtom("affiliatedOrganizationOf", "y", "z"));
        q2.setHead(new Head("Q", "x"));

        // no exception should be thrown
        DisjunctiveQuery ucq = new DisjunctiveQuery();
        ucq.add(q1);
        ucq.add(q2);
        ucq.setHead(new Head("R", "x"));
        ucq.setHead(new Head("R", "y"));

        // no exception should be thrown
        q2.setHead(new Head("Q", "y"));
        ucq = new DisjunctiveQuery();
        ucq.add(q1);
        ucq.add(q2);
        
        // an exception should be thrown
        q2.setHead(new Head("Q", "x", "x"));
        ucq = new DisjunctiveQuery();
        ucq.add(q1);
        try {
            ucq.add(q2);
            assertTrue(false);
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
            assertTrue(true);
        }
        
        // an exception should be thrown
        q2.setHead(new Head("Q", "y"));
        ucq = new DisjunctiveQuery();
        ucq.add(q1);
        ucq.add(q2);
        try {
            ucq.setHead(new Head("R", "x", "y"));
            assertTrue(false);
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
            assertTrue(true);
        }

        
    }
}
