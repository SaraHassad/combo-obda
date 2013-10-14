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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.*;
import de.unibremen.informatik.tdki.combo.rewriting.NodeLabellingConstants;
import de.unibremen.informatik.tdki.combo.rewriting.TreeWitnessFinder;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.query.ConjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.query.Head;
import de.unibremen.informatik.tdki.combo.syntax.query.RoleAtom;

/**
 *
 * @author İnanç Seylan
 */
public class TreeWitnessTest {

    @Test
    public void testTreeWitness() {
        String y1 = "y1";
        String y2 = "y2";
        String y3 = "y3";
        String y4 = "y4";
        String R = "R";
        String S = "S";

        RoleAtom y1Ry2 = new RoleAtom(R, y1, y2);

        ConjunctiveQuery q = new ConjunctiveQuery(new Head("Q"), y1Ry2, new RoleAtom(S, y2, y3), new RoleAtom(S, y4, y3));

        // expected witness for (R,y1)
        Map<String, List<Role>> expected = new HashMap<String, List<Role>>();
        List<Role> list = new ArrayList<Role>();
        list.add(NodeLabellingConstants.EMPTYWORD);
        expected.put(y1, list);
        list = new ArrayList<Role>();
        list.add(NodeLabellingConstants.EMPTYWORD);
        list.add(new Role(R));
        expected.put(y2, list);
        list = new ArrayList<Role>();
        list.add(NodeLabellingConstants.EMPTYWORD);
        list.add(new Role(R));
        list.add(new Role(S));
        expected.put(y3, list);
        list = new ArrayList<Role>();
        list.add(NodeLabellingConstants.EMPTYWORD);
        list.add(new Role(R));
        expected.put(y4, list);

        TreeWitnessFinder finder = new TreeWitnessFinder();
        Map<String, List<Role>> actual = finder.findTreeWitness(q, new Role(R), y1);
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);

        // expected witness for (S,y4)
        expected = new HashMap<String, List<Role>>();
        list = new ArrayList<Role>();
        list.add(NodeLabellingConstants.EMPTYWORD);
        expected.put(y2, list);
        list = new ArrayList<Role>();
        list.add(NodeLabellingConstants.EMPTYWORD);
        list.add(new Role(S));
        expected.put(y3, list);
        list = new ArrayList<Role>();
        list.add(NodeLabellingConstants.EMPTYWORD);
        expected.put(y4, list);

        finder = new TreeWitnessFinder();
        actual = finder.findTreeWitness(q, new Role(S), y4);
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testNonTreeWitnessCyclicQuery() {
        String x = "x";
        String y = "y";
        String z = "z";
        String R = "R";
        String T = "T";

        ConjunctiveQuery q = new ConjunctiveQuery(new Head("Q"), new RoleAtom(T, x, y), new RoleAtom(R, y, z), new RoleAtom(T, z, y));

        TreeWitnessFinder finder = new TreeWitnessFinder();
        Assert.assertNull(finder.findTreeWitness(q, new Role(R), y));
        Assert.assertNull(finder.findTreeWitness(q, new Role(R).toggleInverse(), z));
        Assert.assertNull(finder.findTreeWitness(q, new Role(T), z));
        Assert.assertNull(finder.findTreeWitness(q, new Role(T).toggleInverse(), y));
    }
}
