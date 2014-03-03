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
import org.junit.Assert;
import org.junit.Test;
import de.unibremen.informatik.tdki.combo.subsumption.AnonymousCanonicalModel;
import de.unibremen.informatik.tdki.combo.subsumption.AnonymousIndividual;
import de.unibremen.informatik.tdki.combo.subsumption.AnonymousRoleAssertion;
import de.unibremen.informatik.tdki.combo.subsumption.DLLiteReasoner;
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
public class AnonymousCanonicalModelTest {
    
    @Test
    public void testConceptAssertions() {
        TBox tbox = new MemTBox();
        tbox.add(new GCI(new ConceptName("A"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"))));
        tbox.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true)), new ConceptName("B")));
        tbox.add(new GCI(new ConceptName("B"), new ConceptName("C")));
        tbox.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true)), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("S"))));
        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();
        AnonymousCanonicalModel model = new AnonymousCanonicalModel(reasoner);
        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptName("B"), new ConceptName("C")), model.getConcepts(new AnonymousIndividual(new Role("R"))));
    }

    @Test
    public void testSelfSuccessor() {
        TBox tbox = new MemTBox();
        RoleRestriction ex = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"));
        RoleRestriction exInv = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true));
        tbox.add(new GCI(ex, ex));
        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();
        AnonymousCanonicalModel model = new AnonymousCanonicalModel(reasoner);
        Assert.assertEquals(Collections.<AnonymousRoleAssertion>emptySet(), model.getRoleAssertions(new AnonymousIndividual(exInv.getRole())));
    }

    @Test
    public void testNoGenRole() {
        TBox tbox = new MemTBox();
        final Role R = new Role("R");
        RoleRestriction ex = new RoleRestriction(RoleRestriction.Constructor.SOME, R);
        tbox.add(new GCI(new ConceptName("A"), ex));
        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();
        AnonymousCanonicalModel model = new AnonymousCanonicalModel(reasoner);

        Assert.assertEquals(Collections.<AnonymousRoleAssertion>emptySet(), model.getRoleAssertions(new AnonymousIndividual(R)));
    }

    @Test
    public void testSingleStepGenRole() {
        TBox tbox = DLLiteReasonerTest.createTBox1InMemory();
        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();
        AnonymousCanonicalModel model = new AnonymousCanonicalModel(reasoner);

        Role R = new Role("R");
        Role T = new Role("T");

        Set<AnonymousRoleAssertion> actual = model.getRoleAssertions(new AnonymousIndividual(T));
        Set<AnonymousRoleAssertion> expected = new HashSet<AnonymousRoleAssertion>();
        expected.add(new AnonymousRoleAssertion(R, new AnonymousIndividual(T), new AnonymousIndividual(R)));
        expected.add(new AnonymousRoleAssertion(T, new AnonymousIndividual(R), new AnonymousIndividual(T)));
        Assert.assertEquals(expected, actual);

        actual = model.getRoleAssertions(new AnonymousIndividual(R));
        Assert.assertEquals(expected, actual);

        actual = model.getRoleAssertions(new AnonymousIndividual(R.copy().toggleInverse()));
        Assert.assertEquals(Collections.<AnonymousRoleAssertion>emptySet(), actual);
    }

    @Test
    public void testMultiStepGenRoles() {
        final Role R = new Role("R");
        final Role S = new Role("S");
        final Role T = new Role("T");

        Concept A = new ConceptName("A");
        Concept B = new ConceptName("B");
        Concept C = new ConceptName("C");

        RoleRestriction existsR = new RoleRestriction(RoleRestriction.Constructor.SOME, R);
        RoleRestriction existsInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true));
        RoleRestriction existsS = new RoleRestriction(RoleRestriction.Constructor.SOME, S);
        RoleRestriction existsInvT = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T", true));
        RoleRestriction existsInvS = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("S", true));
        RoleRestriction existsT = new RoleRestriction(RoleRestriction.Constructor.SOME, T);
        RoleRestriction existsP = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("P"));

        TBox tbox = new MemTBox();
        tbox.add(new GCI(A, existsR));
        tbox.add(new GCI(existsInvR, existsS));
        tbox.add(new GCI(existsInvS, existsT));
        tbox.add(new GCI(existsInvT, existsR));
        tbox.add(new GCI(A, B));
        tbox.add(new GCI(C, existsP));
        tbox.add(new GCI(A, existsP));

        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();
        AnonymousCanonicalModel model = new AnonymousCanonicalModel(reasoner);


        Set<AnonymousRoleAssertion> expected = new HashSet<AnonymousRoleAssertion>();
        expected.add(new AnonymousRoleAssertion(S, new AnonymousIndividual(R), new AnonymousIndividual(S)));
        expected.add(new AnonymousRoleAssertion(T, new AnonymousIndividual(S), new AnonymousIndividual(T)));
        expected.add(new AnonymousRoleAssertion(R, new AnonymousIndividual(T), new AnonymousIndividual(R)));
        Set<AnonymousRoleAssertion> actual = model.getRoleAssertions(new AnonymousIndividual(R));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testMultiStepGenRolesWithRoleHierarchy() {
        final ConceptName A = new ConceptName("A");
        final Role R = new Role("R");
        final Role S = new Role("S");
        final Role invS = new Role("S", true);
        final Role T = new Role("T");
        final Role U = new Role("U");
        final Role invU = new Role("U", true);
        final Role P = new Role("P");

        final RoleRestriction someInvS = new RoleRestriction(RoleRestriction.Constructor.SOME, invS);
        final RoleRestriction someT = new RoleRestriction(RoleRestriction.Constructor.SOME, T);
        final RoleRestriction someInvU = new RoleRestriction(RoleRestriction.Constructor.SOME, invU);
        final RoleRestriction someP = new RoleRestriction(RoleRestriction.Constructor.SOME, P);
        final RoleRestriction someR = new RoleRestriction(RoleRestriction.Constructor.SOME, R);

        TBox tbox = new MemTBox();
        tbox.add(new GCI(A, someR));
        tbox.add(new GCI(someInvS, someT));
        tbox.add(new GCI(someInvU, someP));
        tbox.add(new RoleInclusion(R, S));
        tbox.add(new RoleInclusion(T, U));

        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();
        AnonymousCanonicalModel model = new AnonymousCanonicalModel(reasoner);

        Set<AnonymousRoleAssertion> expected = new HashSet<AnonymousRoleAssertion>();
        expected.add(new AnonymousRoleAssertion(T, new AnonymousIndividual(R), new AnonymousIndividual(T)));
        expected.add(new AnonymousRoleAssertion(U, new AnonymousIndividual(R), new AnonymousIndividual(T)));
        expected.add(new AnonymousRoleAssertion(P, new AnonymousIndividual(T), new AnonymousIndividual(P)));
        expected.add(new AnonymousRoleAssertion(invS, new AnonymousIndividual(R), new AnonymousIndividual(invS)));
        expected.add(new AnonymousRoleAssertion(invU, new AnonymousIndividual(T), new AnonymousIndividual(invU)));
        expected.add(new AnonymousRoleAssertion(U, new AnonymousIndividual(R), new AnonymousIndividual(U)));
        expected.add(new AnonymousRoleAssertion(P, new AnonymousIndividual(U), new AnonymousIndividual(P)));
        Set<AnonymousRoleAssertion> actual = model.getRoleAssertions(new AnonymousIndividual(R));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLoopGenRoles() {
        Role R = new Role("R");
        Role invR = new Role("R", true);
        Role S = new Role("S");
        Role invS = new Role("S", true);
        Role U = new Role("U");
        Role invU = new Role("U", true);
        RoleRestriction existsR = new RoleRestriction(RoleRestriction.Constructor.SOME, R);
        RoleRestriction existsS = new RoleRestriction(RoleRestriction.Constructor.SOME, S);

        TBox tbox = new MemTBox();
        tbox.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, invR), existsS));
        tbox.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, invS), existsR));
        tbox.add(new RoleInclusion(invS, invU));
        tbox.add(new RoleInclusion(R, invU));

        DLLiteReasoner reasoner = new DLLiteReasoner(tbox);
        reasoner.classify();
        AnonymousCanonicalModel model = new AnonymousCanonicalModel(reasoner);

        Set<AnonymousRoleAssertion> expected = new HashSet<AnonymousRoleAssertion>();
        expected.add(new AnonymousRoleAssertion(U, new AnonymousIndividual(R), new AnonymousIndividual(U)));
        expected.add(new AnonymousRoleAssertion(S, new AnonymousIndividual(R), new AnonymousIndividual(S, true)));
        expected.add(new AnonymousRoleAssertion(U, new AnonymousIndividual(R), new AnonymousIndividual(S, true)));
        expected.add(new AnonymousRoleAssertion(invU, new AnonymousIndividual(S, true), new AnonymousIndividual(invU, true)));
        expected.add(new AnonymousRoleAssertion(R, new AnonymousIndividual(S, true), new AnonymousIndividual(R, true)));
        expected.add(new AnonymousRoleAssertion(invU, new AnonymousIndividual(S, true), new AnonymousIndividual(R, true)));
        expected.add(new AnonymousRoleAssertion(U, new AnonymousIndividual(R, true), new AnonymousIndividual(U, true)));
        expected.add(new AnonymousRoleAssertion(S, new AnonymousIndividual(R, true), new AnonymousIndividual(S)));
        expected.add(new AnonymousRoleAssertion(U, new AnonymousIndividual(R, true), new AnonymousIndividual(S)));
        expected.add(new AnonymousRoleAssertion(invU, new AnonymousIndividual(S), new AnonymousIndividual(invU)));
        expected.add(new AnonymousRoleAssertion(R, new AnonymousIndividual(S), new AnonymousIndividual(R)));
        expected.add(new AnonymousRoleAssertion(invU, new AnonymousIndividual(S), new AnonymousIndividual(R)));
        Set<AnonymousRoleAssertion> actual = model.getRoleAssertions(new AnonymousIndividual(R));
        Assert.assertEquals(expected, actual);
    }
}
