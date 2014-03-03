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
import de.unibremen.informatik.tdki.combo.syntax.query.Head;
import de.unibremen.informatik.tdki.combo.syntax.query.NRDatalogProgram;
import de.unibremen.informatik.tdki.combo.syntax.query.RoleAtom;
import de.unibremen.informatik.tdki.combo.syntax.query.NRDatalogToPEQConverter;
import de.unibremen.informatik.tdki.combo.syntax.query.DisjunctiveQuery;

/**
 *
 * @author seylan
 */
public class NRDatalogToPEQConverterTest {

    private NRDatalogToPEQConverter converter;
    
    @Before
    public void setUp() {
        converter = new NRDatalogToPEQConverter();
    }
    
    @Test
    public void test1() {
        NRDatalogProgram p = new NRDatalogProgram();
        p.addRule(new ConjunctiveQuery(new Head("S", "x"), new ConceptAtom("Student", "x")));
        p.addRule(new ConjunctiveQuery(new Head("S", "x"), new ConceptAtom("Subj10Student", "x")));
        p.addRule(new ConjunctiveQuery(new Head("m", "x", "y"), new RoleAtom("memberOf", "x", "y")));
        p.addRule(new ConjunctiveQuery(new Head("m", "x", "y"), new RoleAtom("member", "y", "x")));
        p.addRule(new ConjunctiveQuery(new Head("Q", "?0", "?1"), new ConceptAtom("S", "?0"), new RoleAtom("m", "?0", "?1")));
        p.setHeadPredicate("Q");
        DisjunctiveQuery peq = converter.convert(p);
        
        DisjunctiveQuery concepts = new DisjunctiveQuery();
        concepts.add(new ConjunctiveQuery(new Head("S", "x"), new ConceptAtom("Student", "x")));
        concepts.add(new ConjunctiveQuery(new Head("S", "x"), new ConceptAtom("Subj10Student", "x")));
        concepts.setHead(new Head("S", "?0"));
        
        DisjunctiveQuery roles = new DisjunctiveQuery();
        roles.add(new ConjunctiveQuery(new Head("m", "x", "y"), new RoleAtom("memberOf", "x", "y")));
        roles.add(new ConjunctiveQuery(new Head("m", "x", "y"), new RoleAtom("member", "y", "x")));
        roles.setHead(new Head("m", "?0", "?1"));
        
        ConjunctiveQuery cq = new ConjunctiveQuery(new Head("Q", "?0", "?1"), concepts, roles);
        DisjunctiveQuery expected = new DisjunctiveQuery();
        expected.add(cq);
        
        Assert.assertEquals(expected, peq);
    }
}