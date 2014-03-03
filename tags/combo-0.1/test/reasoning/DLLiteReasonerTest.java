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

import de.unibremen.informatik.tdki.combo.common.CollectionUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.*;
import de.unibremen.informatik.tdki.combo.subsumption.AnonymousCanonicalModel;
import de.unibremen.informatik.tdki.combo.subsumption.DLLiteReasoner;
import de.unibremen.informatik.tdki.combo.subsumption.AnonymousIndividual;
import de.unibremen.informatik.tdki.combo.subsumption.AnonymousRoleAssertion;
import de.unibremen.informatik.tdki.combo.subsumption.MemTBox;
import de.unibremen.informatik.tdki.combo.subsumption.TBox;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.axiom.GCI;
import de.unibremen.informatik.tdki.combo.syntax.axiom.RoleInclusion;
import de.unibremen.informatik.tdki.combo.syntax.concept.Concept;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import de.unibremen.informatik.tdki.combo.syntax.concept.RoleRestriction;

/**
 *
 * @author İnanç Seylan
 */
public class DLLiteReasonerTest {

    public static TBox createTBox1InMemory() {
        TBox tbox = new MemTBox();
        tbox.add(new GCI(new ConceptName("A"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T"))));
        tbox.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T", true)), new ConceptName("B")));
        tbox.add(new GCI(new ConceptName("B"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"))));
        tbox.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true)), new ConceptName("A")));
        return tbox;
    }

    @Test
    public void testConceptNotImpliesItself() {
        Concept A = new ConceptName("A");

        TBox tbox = new MemTBox();
        tbox.add(new GCI(A, A));

        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();

        Set<Concept> actual = reasoner.getSuperConcepts(A);
        Assert.assertEquals(Collections.<Concept>emptySet(), actual);
    }

    @Test
    public void testTwoEquivalentRoles() {
        Role R = new Role("R");
        Role S = new Role("S");

        TBox tbox = new MemTBox();
        tbox.add(new RoleInclusion(R, R));
        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();
        Set<Role> actual = reasoner.getSuperRoles(R);
        Assert.assertEquals(Collections.<Role>emptySet(), actual);

        tbox = new MemTBox();
        tbox.add(new RoleInclusion(R, S));
        tbox.add(new RoleInclusion(S, R));
        reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();
        actual = reasoner.getSuperRoles(R);
        Assert.assertEquals(CollectionUtils.newHashSet(S), actual);
    }

    @Test
    public void testConceptDeductiveClosure() {
        Concept A = new ConceptName("A");
        Concept B = new ConceptName("B");
        Concept existsR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"));

        TBox tbox = new MemTBox();
        tbox.add(new GCI(A, existsR));
        tbox.add(new GCI(existsR, B));

        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();

        Set<Concept> actual = reasoner.getSuperConcepts(A);
        Set<Concept> expected = new HashSet<Concept>();
        expected.add(existsR);
        expected.add(B);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testTBoxSubsumption() {
        TBox tbox = createTBox1InMemory();
        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();

        RoleRestriction existsR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"));
        RoleRestriction existsT = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T"));
        RoleRestriction existsInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true));
        RoleRestriction existsInvT = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T", true));

        Set<Concept> expected = new HashSet<Concept>();
        expected.add(new ConceptName("A"));
        expected.add(existsT);
        Assert.assertEquals(expected, reasoner.getSuperConcepts(existsInvR));

        expected = new HashSet<Concept>();
        expected.add(new ConceptName("B"));
        expected.add(existsR);
        Assert.assertEquals(expected, reasoner.getSuperConcepts(existsInvT));
    }

    @Test
    public void testRoleDeductiveClosure() {
        Role R = new Role("R");
        Role invR = new Role("R", true);
        Role S = new Role("S");
        Role invS = new Role("S", true);
        Role T = new Role("T");
        Role invT = new Role("T", true);

        TBox rbox = new MemTBox();
        rbox.add(new RoleInclusion(R, invS));
        rbox.add(new RoleInclusion(S, invT));

        DLLiteReasoner reasoner = new DLLiteReasoner(rbox);
        reasoner.classify();

        Set<Role> inferredRBoxRoles = new HashSet<Role>();
        inferredRBoxRoles.add(R);
        inferredRBoxRoles.add(invR);
        inferredRBoxRoles.add(S);
        inferredRBoxRoles.add(invS);
        inferredRBoxRoles.add(T);
        inferredRBoxRoles.add(invT);
        Assert.assertEquals(inferredRBoxRoles, reasoner.getRoles());

        Set<Role> actual = reasoner.getSuperRoles(R);
        Set<Role> expected = new HashSet<Role>();
        expected.add(invS);
        expected.add(T);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConceptSubsumptionWithRoleHieararchies1() {
        Role P = new Role("P");
        Role invP = new Role("P", true);
        Role R = new Role("R");
        Role invR = new Role("R", true);
        Role S = new Role("S");

        final RoleRestriction someInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, invR);
        final RoleRestriction someS = new RoleRestriction(RoleRestriction.Constructor.SOME, S);
        final RoleRestriction someInvP = new RoleRestriction(RoleRestriction.Constructor.SOME, invP);

        TBox tbox = new MemTBox();
        tbox.add(new RoleInclusion(P, R));
        tbox.add(new GCI(someInvR, someS));

        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();

        Set<Concept> expected = new HashSet<Concept>();
        expected.add(someS);
        expected.add(someInvR);
        Set<Concept> actual = reasoner.getSuperConcepts(someInvP);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConceptSubsumptionWithRoleHieararchies2() {
        Role P = new Role("P");
        Role R = new Role("R");
        Role invS = new Role("S", true);

        final RoleRestriction someInvS = new RoleRestriction(RoleRestriction.Constructor.SOME, invS);
        final RoleRestriction someP = new RoleRestriction(RoleRestriction.Constructor.SOME, P);
        final RoleRestriction someR = new RoleRestriction(RoleRestriction.Constructor.SOME, R);

        TBox tbox = new MemTBox();
        tbox.add(new RoleInclusion(P, R));
        tbox.add(new GCI(someInvS, someP));

        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();

        Set<Concept> expected = new HashSet<Concept>();
        expected.add(someP);
        expected.add(someR);
        Set<Concept> actual = reasoner.getSuperConcepts(someInvS);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConceptSubsumptionWithRoleHieararchies3() {
        Role P = new Role("P");
        Role R = new Role("R");
        Role invS = new Role("S", true);
        Role invT = new Role("T", true);

        final RoleRestriction someInvS = new RoleRestriction(RoleRestriction.Constructor.SOME, invS);
        final RoleRestriction someInvT = new RoleRestriction(RoleRestriction.Constructor.SOME, invT);
        final RoleRestriction someP = new RoleRestriction(RoleRestriction.Constructor.SOME, P);
        final RoleRestriction someR = new RoleRestriction(RoleRestriction.Constructor.SOME, R);

        TBox tbox = new MemTBox();
        tbox.add(new RoleInclusion(invS, invT));
        tbox.add(new GCI(someInvS, someR));
        tbox.add(new GCI(someInvT, someP));

        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();

        Set<Concept> expected = new HashSet<Concept>();
        expected.add(someP);
        expected.add(someR);
        expected.add(someInvT);
        Set<Concept> actual = reasoner.getSuperConcepts(someInvS);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConceptSubsumptionWithRoleHierarchies4() {
        Role R = new Role("R");
        Role S = new Role("S");
        RoleRestriction existsR = new RoleRestriction(RoleRestriction.Constructor.SOME, R);
        RoleRestriction existsS = new RoleRestriction(RoleRestriction.Constructor.SOME, S);
        ConceptName A = new ConceptName("A");
        ConceptName B = new ConceptName("B");
        ConceptName C = new ConceptName("C");

        TBox tbox = new MemTBox();
        tbox.add(new GCI(A, existsR));
        tbox.add(new GCI(existsS, B));
        tbox.add(new GCI(A, C));
        tbox.add(new RoleInclusion(R, S));

        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();

        Set<Concept> expected = new HashSet<Concept>();
        expected.add(existsS);
        expected.add(B);
        Assert.assertEquals(expected, reasoner.getSuperConcepts(existsR));

        expected.clear();
        expected.add(existsR);
        expected.add(C);
        expected.add(existsS);
        expected.add(B);
        Assert.assertEquals(expected, reasoner.getSuperConcepts(A));
    }
}
