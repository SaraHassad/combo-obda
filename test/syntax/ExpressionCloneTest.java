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

import de.unibremen.informatik.tdki.combo.syntax.concept.Concept;
import de.unibremen.informatik.tdki.combo.syntax.concept.InequalityConstructor;
import de.unibremen.informatik.tdki.combo.syntax.concept.QualifiedNoRestriction;
import de.unibremen.informatik.tdki.combo.syntax.concept.BooleanConcept;
import de.unibremen.informatik.tdki.combo.syntax.concept.RoleRestriction;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import org.junit.Assert;
import org.junit.Test;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ConceptAssertion;

/**
 * 
 * @author İnanç Seylan
 */
public class ExpressionCloneTest {
    
    @Test
    public void testStringSemantics() {
        String s1 = "hello";
        String s2 = "hello";
        Assert.assertTrue(s1 == s2);
        s2 = s1;
        s1 = "bye";
        Assert.assertTrue(s2 == "hello");
    }

    @Test
    public void testConceptClone() throws CloneNotSupportedException {
        Concept c1 = new ConceptName("Book").toggleNegated();
        Concept c2 = c1.copy();
        Assert.assertNotSame(c1, c2);
        Assert.assertSame(((ConceptName) c1).getName(), ((ConceptName) c2).getName());
        Assert.assertEquals(c1, c2);
    }

    @Test
    public void testBooleanConceptClone() throws CloneNotSupportedException {
        BooleanConcept bd = new BooleanConcept(
                BooleanConcept.Constructor.INTERSECTION, new ConceptName("C"),
                new ConceptName("D"));
        BooleanConcept bdClone = bd.copy();
        Assert.assertNotSame(bd, bdClone);
        // IMPORTANT: the below assertion always fails so it is commented.
        // assertNotSame(bd.getConstructor(), bdClone.getConstructor());
        Assert.assertNotSame(bd.getLhs(), bdClone.getLhs());
        Assert.assertNotSame(bd.getRhs(), bdClone.getRhs());
        Assert.assertEquals(bd, bdClone);
        // Because of the commented assertion above, here we check whether
        // bdClone's constructor changes when bd's changes. If it doesn't
        // change, then clone() works well.
        bd.setConstructor(BooleanConcept.Constructor.UNION);
        Assert.assertNotSame(bd.getConstructor(), bdClone.getConstructor());
        Assert.assertFalse(bd.getConstructor().equals(
                bdClone.getConstructor()));
    }

    @Test
    public void testRoleQuantification() throws CloneNotSupportedException {
        BooleanConcept bd = new BooleanConcept(
                BooleanConcept.Constructor.INTERSECTION, new ConceptName("C"),
                new ConceptName("D"));
        RoleRestriction qr1 = new RoleRestriction(
                RoleRestriction.Constructor.SOME, new Role("R"), bd);
        RoleRestriction qr2 = qr1.copy();
        Assert.assertNotSame(qr1, qr2);
        // The line below is commented because of the same reason as the above
        // test
        // assertNotSame(qr1.getConstructor(), qr2.getConstructor());
        Assert.assertNotSame(qr1.getRole(), qr2.getRole());
        Assert.assertNotSame(qr1.getConcept(), qr2.getConcept());
        Assert.assertEquals(qr1, qr2);
        // test constructor
        qr1.setConstructor(RoleRestriction.Constructor.ALL);
        Assert.assertNotSame(qr1.getConstructor(), qr2.getConstructor());
        Assert.assertFalse(qr1.getConstructor().equals(qr2.getConstructor()));
    }

    @Test
    public void testConceptAssertion() throws CloneNotSupportedException {
        ConceptAssertion ca = new ConceptAssertion(new ConceptName("Movie"),
                "harryPotter");
        ConceptAssertion caClone = ca.copy();
        Assert.assertNotSame(ca, caClone);
        Assert.assertSame(ca.getIndividual(), caClone.getIndividual());
        Assert.assertNotSame(ca.getConcept(), caClone.getConcept());
        Assert.assertEquals(ca, caClone);
    }

    @Test
    public void testQualifyingNoRestriction() throws CloneNotSupportedException {
        QualifiedNoRestriction qr = new QualifiedNoRestriction(
                InequalityConstructor.AT_MOST, 3, new Role("R"),
                new ConceptName("C"));
        QualifiedNoRestriction qrClone = qr.copy();
        Assert.assertNotSame(qr.getRole(), qrClone.getRole());
        Assert.assertNotSame(qr.getConcept(), qrClone.getConcept());
        Assert.assertEquals(qr, qrClone);
    }
}
