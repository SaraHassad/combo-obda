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


import de.unibremen.informatik.tdki.combo.syntax.Role;
import junit.framework.Assert;
import org.junit.Test;
import de.unibremen.informatik.tdki.combo.syntax.axiom.GCI;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ObjectRoleAssertion;
import de.unibremen.informatik.tdki.combo.syntax.concept.BooleanConcept;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import de.unibremen.informatik.tdki.combo.syntax.concept.Nominal;

/**
 * 
 * @author İnanç Seylan
 */
public class ExpressionEqualsTest {

    @Test
    public void testBooleanConcept() {
        ConceptName c = new ConceptName("C");
        ConceptName d = new ConceptName("D");
        BooleanConcept bd = new BooleanConcept(BooleanConcept.Constructor.UNION, c, d);
        BooleanConcept bdCopy = new BooleanConcept(BooleanConcept.Constructor.UNION, c,
                d);
        BooleanConcept bdInv = new BooleanConcept(BooleanConcept.Constructor.UNION, d, c);
        Assert.assertEquals(bd, bdCopy);
        Assert.assertTrue(!bd.equals(bdInv));
    }

    @Test
    public void testNominal() {
        Nominal a = new Nominal("a");
        Nominal acopy = new Nominal("a");
        Assert.assertEquals(a, acopy);
        a.toggleNegated();
        Assert.assertFalse(a.equals(acopy));
        a.toggleNegated();
        Assert.assertEquals(a, acopy);
    }

    @Test
    public void testGCISubset() {
        GCI cd1 = new GCI(GCI.Type.SUBSET,
                new ConceptName("C"), new ConceptName("D"));
        GCI cd2 = new GCI(GCI.Type.SUBSET,
                new ConceptName("C"), new ConceptName("D"));
        Assert.assertEquals(cd1, cd2);
        GCI cd3 = new GCI(GCI.Type.SUBSET,
                new ConceptName("D"), new ConceptName("C"));
        Assert.assertFalse(cd2.equals(cd3));
    }

    @Test
    public void testGCIEquiv() {
        GCI cd1 = new GCI(GCI.Type.EQUIV,
                new ConceptName("C"), new ConceptName("D"));
        GCI cd2 = new GCI(GCI.Type.EQUIV,
                new ConceptName("C"), new ConceptName("D"));
        GCI cd3 = new GCI(GCI.Type.EQUIV,
                new ConceptName("D"), new ConceptName("C"));
        Assert.assertEquals(cd1, cd2);
        Assert.assertEquals(cd1, cd3);

        Assert.assertEquals(cd1.toggleNegated(), cd3.toggleNegated());
    }

    @Test
    public void testObjectRoleAssertion() {
        ObjectRoleAssertion ra = new ObjectRoleAssertion(new Role("R"), "a",
                "b");
        ObjectRoleAssertion invRa = new ObjectRoleAssertion(new Role("R").toggleInverse(), "b", "a");
        Assert.assertEquals(ra, invRa);
    }

    @Test
    public void testRole() {
        Role r = new Role("R");
        Role otherR = new Role("R");
        Assert.assertEquals(r, otherR);
        Assert.assertSame(r.getName(), otherR.getName());
        r.setInverse(true);
        Assert.assertFalse(r.equals(otherR));
        Assert.assertSame(r.getName(), otherR.getName());
    }
}
