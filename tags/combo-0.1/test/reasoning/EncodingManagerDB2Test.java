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
import de.unibremen.informatik.tdki.combo.data.EncodingManagerDB2;
import de.unibremen.informatik.tdki.combo.data.DBLayout;
import org.junit.Before;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.concept.Concept;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import de.unibremen.informatik.tdki.combo.syntax.concept.RoleRestriction;

/**
 *
 * @author İnanç Seylan
 */
public class EncodingManagerDB2Test {
    
    @Before
    public void setUp() {
        new DBLayout(true).initialize();
    }

    @Test
    public void testIDGeneration() {
        Concept A = new ConceptName("A");
        Concept B = new ConceptName("B");
        Role R = new Role("R");
        Role invR = new Role("R", true);
        Role S = new Role("S");
        Role invS = new Role("S", true);
        RoleRestriction existsR = new RoleRestriction(RoleRestriction.Constructor.SOME, R);

        EncodingManagerDB2 manager = new EncodingManagerDB2();
        manager.mapConcept(A);
        manager.mapConcept(existsR);
        manager.mapRole(invS);
        manager.mapRole(R);
        manager.mapConcept(B);
        manager.mapRole(S);
        manager.mapIndividual("b");
        manager.mapIndividual("a");
        manager.mapIndividual("b");

        Assert.assertEquals(16, manager.getConceptID(A));
        Assert.assertEquals(40, manager.getConceptID(existsR));
        Assert.assertEquals(40, manager.getRoleID(R));
        Assert.assertEquals(58, manager.getRoleID(invS));
        Assert.assertEquals(56, manager.getRoleID(S));
        Assert.assertEquals(42, manager.getRoleID(invR));
        Assert.assertEquals(64, manager.getConceptID(B));
        Assert.assertEquals(-1, manager.getIndvID("b"));
        Assert.assertEquals(-2, manager.getIndvID("a"));

        Assert.assertEquals(A, manager.getConcept(16));
        Assert.assertEquals(existsR, manager.getConcept(40));
        Assert.assertEquals(new RoleRestriction(RoleRestriction.Constructor.SOME, invS), manager.getConcept(58));
        Assert.assertEquals(new RoleRestriction(RoleRestriction.Constructor.SOME, S), manager.getConcept(56));
        Assert.assertEquals(new RoleRestriction(RoleRestriction.Constructor.SOME, invR), manager.getConcept(42));
        Assert.assertEquals(B, manager.getConcept(64));
    }
}
