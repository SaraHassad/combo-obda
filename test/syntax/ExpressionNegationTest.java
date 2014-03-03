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

/**
 * 
 * @author İnanç Seylan
 */
public class ExpressionNegationTest {

    @Test
    public void testIntersection() {
        Concept desc = new BooleanConcept(BooleanConcept.Constructor.INTERSECTION,
                new ConceptName("Male"), new ConceptName("Person")).toggleNegated();
        Concept expected = new BooleanConcept(BooleanConcept.Constructor.UNION,
                new ConceptName("Male").toggleNegated(), new ConceptName(
                "Person").toggleNegated());
        Assert.assertEquals(expected, desc);
    }

    @Test
    public void testSomeValues() {
        Concept desc = new RoleRestriction(
                RoleRestriction.Constructor.SOME,
                new Role("R"), new ConceptName("C")).toggleNegated();
        Concept expected = new RoleRestriction(
                RoleRestriction.Constructor.ALL,
                new Role("R"), new ConceptName("C").toggleNegated());
        Assert.assertEquals(expected, desc);
    }

    @Test
    public void testAllValues() {
        Concept desc = new RoleRestriction(
                RoleRestriction.Constructor.ALL,
                new Role("R"), new ConceptName("C")).toggleNegated();
        Concept expected = new RoleRestriction(
                RoleRestriction.Constructor.SOME,
                new Role("R"), new ConceptName("C").toggleNegated());
        Assert.assertEquals(expected, desc);
    }

    @Test
    public void testQualifiedNoRestriction() {
        Concept desc = new QualifiedNoRestriction(
                InequalityConstructor.AT_MOST, 2, new Role("R"),
                new ConceptName("C")).toggleNegated();
        Concept expected = new QualifiedNoRestriction(
                InequalityConstructor.AT_LEAST, 3, new Role("R"),
                new ConceptName("C"));
        Assert.assertEquals(expected, desc);

        desc = new QualifiedNoRestriction(InequalityConstructor.AT_LEAST, 2,
                new Role("R"), new ConceptName("C")).toggleNegated();
        expected = new QualifiedNoRestriction(InequalityConstructor.AT_MOST, 1,
                new Role("R"), new ConceptName("C"));
        Assert.assertEquals(expected, desc);
    }

    @Test
    public void testAtLeastRestrictionForNEqualTo0() {
        Concept desc = new QualifiedNoRestriction(
                InequalityConstructor.AT_LEAST, 0, new Role("R"),
                new ConceptName("C")).toggleNegated();
        Concept expected = ConceptName.bottomConcept();
        Assert.assertEquals(expected, desc);
    }

    @Test
    public void testAtLeastRestrictionForNEqualTo1() {
        Concept desc = new QualifiedNoRestriction(
                InequalityConstructor.AT_LEAST, 1, new Role("R"),
                new ConceptName("C")).toggleNegated();
        Concept expected = new RoleRestriction(
                RoleRestriction.Constructor.ALL,
                new Role("R"), new ConceptName("C").toggleNegated());
        Assert.assertEquals(expected, desc);
    }

    @Test
    public void testAtMostRestrictionForNEqualTo0() {
        Concept desc = new QualifiedNoRestriction(
                InequalityConstructor.AT_MOST, 0, new Role("R"),
                new ConceptName("C")).toggleNegated();
        Concept expected = new RoleRestriction(
                RoleRestriction.Constructor.SOME,
                new Role("R"), new ConceptName("C"));
        Assert.assertEquals(expected, desc);
    }
}
