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
import de.unibremen.informatik.tdki.combo.data.DBLayout;
import de.unibremen.informatik.tdki.combo.data.DBToMemLoader;
import de.unibremen.informatik.tdki.combo.data.MemToBulkFileWriter;
import de.unibremen.informatik.tdki.combo.subsumption.AnonymousIndividual;
import de.unibremen.informatik.tdki.combo.subsumption.QualifiedExistentialEncoder;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ConceptAssertion;
import de.unibremen.informatik.tdki.combo.syntax.axiom.GCI;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ObjectRoleAssertion;
import de.unibremen.informatik.tdki.combo.syntax.axiom.RoleInclusion;
import de.unibremen.informatik.tdki.combo.syntax.concept.Concept;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import de.unibremen.informatik.tdki.combo.syntax.concept.RoleRestriction;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author İnanç Seylan
 */
public class BitRepDataCompletionStrategyTest {
    
    private static final List<String> PROJECTS = Arrays.asList("test");

    private DBLayout layout;
    
    private MemToBulkFileWriter writer;
    
    private File dataFile;

    private void loadAndCompleteData() {
        layout.loadProject(dataFile, PROJECTS.get(0));
        layout.completeData(PROJECTS);
    }

    @Before
    public void setup() throws IOException, InterruptedException {
        layout = new DBLayout(true);
        layout.initialize();
        layout.createProject(PROJECTS);
        
        dataFile = File.createTempFile("test", "combo");
        dataFile.deleteOnExit();
        writer = new MemToBulkFileWriter(dataFile);
    }

    @Test
    public void testCompleteDataCNCN1() throws IOException, InterruptedException {
        writer.add(new GCI(new ConceptName("A"), new ConceptName("B")));
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new GCI(new ConceptName("A"), new ConceptName("B"))), materializer.getConceptInclusions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(new ConceptName("A"), "a"), new ConceptAssertion(new ConceptName("B"), "a")),
                materializer.getConceptAssertions());
    }

    @Test
    public void testCompleteDataCNCN2() throws IOException, InterruptedException {
        writer.add(new GCI(new ConceptName("A"), new ConceptName("B")));
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.add(new ConceptAssertion(new ConceptName("B"), "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new GCI(new ConceptName("A"), new ConceptName("B"))), materializer.getConceptInclusions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(new ConceptName("A"), "a"), new ConceptAssertion(new ConceptName("B"), "a")),
                materializer.getConceptAssertions());
        Assert.assertEquals(Collections.<ObjectRoleAssertion>emptySet(), materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataCNRN1() throws IOException, InterruptedException {
        RoleRestriction someR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"));

        writer.add(new GCI(new ConceptName("A"), someR));
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new GCI(new ConceptName("A"), someR)), materializer.getConceptInclusions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(new ConceptName("A"), "a")), materializer.getConceptAssertions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ObjectRoleAssertion(new Role("R"), "a", "c_R0")), materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataCNRN2() throws IOException, InterruptedException {
        RoleRestriction someR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"));

        writer.add(new GCI(new ConceptName("A"), someR));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new GCI(new ConceptName("A"), someR)), materializer.getConceptInclusions());
        Assert.assertEquals(Collections.<ConceptAssertion>emptySet(), materializer.getConceptAssertions());
        Assert.assertEquals(Collections.<ObjectRoleAssertion>emptySet(), materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataCNInvR1() throws IOException, InterruptedException {
        RoleRestriction someInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true));

        writer.add(new GCI(new ConceptName("A"), someInvR));
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new GCI(new ConceptName("A"), someInvR)), materializer.getConceptInclusions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(new ConceptName("A"), "a")), materializer.getConceptAssertions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ObjectRoleAssertion(new Role("R"), "c_R-0", "a")), materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataCNInvR2() throws IOException, InterruptedException {
        RoleRestriction someInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true));

        writer.add(new GCI(new ConceptName("A"), someInvR));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new GCI(new ConceptName("A"), someInvR)), materializer.getConceptInclusions());
        Assert.assertEquals(Collections.<ConceptAssertion>emptySet(), materializer.getConceptAssertions());
        Assert.assertEquals(Collections.<ObjectRoleAssertion>emptySet(), materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataRNCN() throws IOException, InterruptedException {
        RoleRestriction someR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"));

        writer.add(new GCI(someR, new ConceptName("A")));
        writer.add(new ObjectRoleAssertion(new Role("R"), "a", "b"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new GCI(someR, new ConceptName("A"))), materializer.getConceptInclusions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(new ConceptName("A"), "a")), materializer.getConceptAssertions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ObjectRoleAssertion(new Role("R"), "a", "b")), materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataRNRN() throws IOException, InterruptedException {
        RoleRestriction existsR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"));
        RoleRestriction existsT = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T"));

        writer.add(new GCI(existsR, existsT));
        writer.add(new ObjectRoleAssertion(new Role("R"), "a", "b"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new GCI(existsR, existsT)), materializer.getConceptInclusions());
        Assert.assertEquals(Collections.<ConceptAssertion>emptySet(), materializer.getConceptAssertions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ObjectRoleAssertion(new Role("R"), "a", "b"), new ObjectRoleAssertion(new Role("T"), "a", "c_T0")),
                materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataRNInvR() throws IOException, InterruptedException {
        RoleRestriction existsR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"));
        RoleRestriction existsInvT = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T", true));

        writer.add(new GCI(existsR, existsInvT));
        writer.add(new ObjectRoleAssertion(new Role("R"), "a", "b"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new GCI(existsR, existsInvT)), materializer.getConceptInclusions());
        Assert.assertEquals(Collections.<ConceptAssertion>emptySet(), materializer.getConceptAssertions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ObjectRoleAssertion(new Role("R"), "a", "b"), new ObjectRoleAssertion(new Role("T"), "c_T-0", "a")),
                materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataInvRCN() throws IOException, InterruptedException {
        RoleRestriction someInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true));

        writer.add(new GCI(someInvR, new ConceptName("A")));
        writer.add(new ObjectRoleAssertion(new Role("R"), "b", "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new GCI(someInvR, new ConceptName("A"))), materializer.getConceptInclusions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(new ConceptName("A"), "a")), materializer.getConceptAssertions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ObjectRoleAssertion(new Role("R"), "b", "a")), materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataInvRRN() throws IOException, InterruptedException {
        RoleRestriction existsInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true));
        RoleRestriction existsT = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T"));

        writer.add(new GCI(existsInvR, existsT));
        writer.add(new ObjectRoleAssertion(new Role("R"), "b", "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new GCI(existsInvR, existsT)), materializer.getConceptInclusions());
        Assert.assertEquals(Collections.<ConceptAssertion>emptySet(), materializer.getConceptAssertions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ObjectRoleAssertion(new Role("R"), "b", "a"), new ObjectRoleAssertion(new Role("T"), "a", "c_T0")),
                materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataInvRInvR() throws IOException, InterruptedException {
        RoleRestriction existsInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true));
        RoleRestriction existsInvT = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T", true));

        writer.add(new GCI(existsInvR, existsInvT));
        writer.add(new ObjectRoleAssertion(new Role("R"), "b", "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new GCI(existsInvR, existsInvT)), materializer.getConceptInclusions());
        Assert.assertEquals(Collections.<ConceptAssertion>emptySet(), materializer.getConceptAssertions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ObjectRoleAssertion(new Role("R"), "b", "a"), new ObjectRoleAssertion(new Role("T"), "c_T-0", "a")),
                materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataRoleInclusion_RNRN() throws IOException, InterruptedException {
        Role R = new Role("R");
        Role S = new Role("S");

        writer.add(new RoleInclusion(R, S));
        writer.add(new ObjectRoleAssertion(R, "a", "b"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Set<RoleInclusion> expectedRI = new HashSet<RoleInclusion>();
        expectedRI.add(new RoleInclusion(R, S));
        expectedRI.add(new RoleInclusion(R.copy().toggleInverse(), S.copy().toggleInverse()));
        Assert.assertEquals(expectedRI, materializer.getRoleInclusions());

        Set<ObjectRoleAssertion> expectedRA = new HashSet<ObjectRoleAssertion>();
        expectedRA.add(new ObjectRoleAssertion(R, "a", "b"));
        expectedRA.add(new ObjectRoleAssertion(S, "a", "b"));
        Assert.assertEquals(expectedRA, materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataRoleInclusion_RNInvR() throws IOException, InterruptedException {
        Role R = new Role("R");
        Role S = new Role("S");
        Role invS = new Role("S", true);

        writer.add(new RoleInclusion(R, invS));
        writer.add(new ObjectRoleAssertion(R, "a", "b"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Set<RoleInclusion> expectedRI = new HashSet<RoleInclusion>();
        expectedRI.add(new RoleInclusion(R, invS));
        expectedRI.add(new RoleInclusion(R.copy().toggleInverse(), S));
        Assert.assertEquals(expectedRI, materializer.getRoleInclusions());

        Set<ObjectRoleAssertion> expectedRA = new HashSet<ObjectRoleAssertion>();
        expectedRA.add(new ObjectRoleAssertion(R, "a", "b"));
        expectedRA.add(new ObjectRoleAssertion(S, "b", "a"));
        Assert.assertEquals(expectedRA, materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataRoleInclusion_InvRRN() throws IOException, InterruptedException {
        Role R = new Role("R");
        Role invR = new Role("R", true);
        Role S = new Role("S");

        writer.add(new RoleInclusion(invR, S));
        writer.add(new ObjectRoleAssertion(R, "b", "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));

        Set<RoleInclusion> expectedRI = new HashSet<RoleInclusion>();
        expectedRI.add(new RoleInclusion(invR, S));
        expectedRI.add(new RoleInclusion(R, S.copy().toggleInverse()));
        Assert.assertEquals(expectedRI, materializer.getRoleInclusions());

        Set<ObjectRoleAssertion> expectedRA = new HashSet<ObjectRoleAssertion>();
        expectedRA.add(new ObjectRoleAssertion(R, "b", "a"));
        expectedRA.add(new ObjectRoleAssertion(S, "a", "b"));
        Assert.assertEquals(expectedRA, materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataRoleInclusion_InvRInvR() throws IOException, InterruptedException {
        Role R = new Role("R");
        Role invR = new Role("R", true);
        Role S = new Role("S");
        Role invS = new Role("S", true);

        writer.add(new RoleInclusion(invR, invS));
        writer.add(new ObjectRoleAssertion(R, "a", "b"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));

        Set<RoleInclusion> expectedRI = new HashSet<RoleInclusion>();
        expectedRI.add(new RoleInclusion(invR, invS));
        expectedRI.add(new RoleInclusion(R, S));
        Assert.assertEquals(expectedRI, materializer.getRoleInclusions());

        Set<ObjectRoleAssertion> expectedRA = new HashSet<ObjectRoleAssertion>();
        expectedRA.add(new ObjectRoleAssertion(R, "a", "b"));
        expectedRA.add(new ObjectRoleAssertion(S, "a", "b"));
        Assert.assertEquals(expectedRA, materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataGenRoles() throws IOException, InterruptedException {
        final Role P = new Role("P");
        final Role R = new Role("R");
        final Role S = new Role("S");
        final Role T = new Role("T");
        final RoleRestriction someP = new RoleRestriction(RoleRestriction.Constructor.SOME, P);
        final RoleRestriction someInvP = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("P", true));
        final RoleRestriction someR = new RoleRestriction(RoleRestriction.Constructor.SOME, R);
        final RoleRestriction someInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true));
        final RoleRestriction someS = new RoleRestriction(RoleRestriction.Constructor.SOME, S);
        final RoleRestriction someInvS = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("S", true));
        final RoleRestriction someT = new RoleRestriction(RoleRestriction.Constructor.SOME, T);
        final ConceptName A = new ConceptName("A");
        final ConceptName B = new ConceptName("B");

        writer.add(new GCI(A, someR));
        writer.add(new GCI(someInvR, someS));
        writer.add(new GCI(someInvS, someT));
        writer.add(new GCI(B, someP));
        writer.add(new GCI(someInvP, someS));
        writer.add(new ConceptAssertion(A, "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));

        Set<ObjectRoleAssertion> expected = new HashSet<ObjectRoleAssertion>();
        expected.add(new ObjectRoleAssertion(R, "a", "c_R0"));
        expected.add(new ObjectRoleAssertion(S, "c_R0", "c_S0"));
        expected.add(new ObjectRoleAssertion(T, "c_S0", "c_T0"));
        Assert.assertEquals(expected, materializer.getRoleAssertions());
    }

    @Test
    public void testLoadFromDB() throws IOException, InterruptedException {
        ConceptName A = new ConceptName("A");
        Role R = new Role("R");
        Role S = new Role("S");
        Role invR = new Role("R", true);
        Role invS = new Role("S", true);
        Concept existsR = new RoleRestriction(RoleRestriction.Constructor.SOME, R);
        Concept existsS = new RoleRestriction(RoleRestriction.Constructor.SOME, S);
        Concept existsInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, invR);
        Concept existsInvS = new RoleRestriction(RoleRestriction.Constructor.SOME, invS);

        writer.add(new ConceptAssertion(A, "a"));
        writer.add(new ObjectRoleAssertion(R, "a", "b"));
        writer.add(new GCI(A, existsR));
        writer.add(new RoleInclusion(R, S));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));

        Set<GCI> expectedCI = new HashSet<GCI>();
        expectedCI.add(new GCI(A, existsR));
        expectedCI.add(new GCI(A, existsS));
        expectedCI.add(new GCI(existsR, existsS));
        expectedCI.add(new GCI(existsInvR, existsInvS));
        Assert.assertEquals(expectedCI, materializer.getConceptInclusions());

        Assert.assertEquals(CollectionUtils.newHashSet(new RoleInclusion(R, S), new RoleInclusion(invR, invS)),
                materializer.getRoleInclusions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(A, "a")), materializer.getConceptAssertions());

        Set<ObjectRoleAssertion> raSet = new HashSet<ObjectRoleAssertion>();
        raSet.add(new ObjectRoleAssertion(R, "a", "b"));
        raSet.add(new ObjectRoleAssertion(S, "a", "b"));
        Assert.assertEquals(raSet, materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataInvolved() throws IOException, InterruptedException {
        RoleRestriction someR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"));
        RoleRestriction someInvR = someR.copy();
        someInvR.getRole().toggleInverse();
        RoleRestriction someS = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("S"));
        RoleRestriction someInvS = someS.copy();
        someInvS.getRole().toggleInverse();
        RoleRestriction someT = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T"));

        writer.add(new GCI(new ConceptName("A"), someR));
        writer.add(new GCI(someInvR, someS));
        writer.add(new GCI(someInvS, someT));
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));

        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(new ConceptName("A"), "a")), materializer.getConceptAssertions());

        Set<ObjectRoleAssertion> expected = new HashSet<ObjectRoleAssertion>();
        expected.add(new ObjectRoleAssertion(new Role("R"), "a", "c_R0"));
        expected.add(new ObjectRoleAssertion(new Role("S"), "c_R0", "c_S0"));
        expected.add(new ObjectRoleAssertion(new Role("T"), "c_S0", "c_T0"));
        Assert.assertEquals(expected, materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataInvolvedInv() throws IOException, InterruptedException {
        RoleRestriction someR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"));
        RoleRestriction someInvR = someR.copy();
        someInvR.getRole().toggleInverse();
        RoleRestriction someS = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("S"));
        RoleRestriction someInvS = someS.copy();
        someInvS.getRole().toggleInverse();
        RoleRestriction someInvT = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T", true));

        writer.add(new GCI(new ConceptName("A"), someInvR));
        writer.add(new GCI(someR, someInvS));
        writer.add(new GCI(someS, someInvT));
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));

        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(new ConceptName("A"), "a")), materializer.getConceptAssertions());

        Set<ObjectRoleAssertion> expected = new HashSet<ObjectRoleAssertion>();
        expected.add(new ObjectRoleAssertion(new Role("R"), "c_R-0", "a"));
        expected.add(new ObjectRoleAssertion(new Role("S"), "c_S-0", "c_R-0"));
        expected.add(new ObjectRoleAssertion(new Role("T"), "c_T-0", "c_S-0"));
        Assert.assertEquals(expected, materializer.getRoleAssertions());
    }

    private void putLoopData() throws IOException, InterruptedException {
        Role R = new Role("R");
        Role invR = new Role("R", true);
        Role S = new Role("S");
        Role invS = new Role("S", true);
        Role U = new Role("U");
        RoleRestriction existsR = new RoleRestriction(RoleRestriction.Constructor.SOME, R);
        RoleRestriction existsS = new RoleRestriction(RoleRestriction.Constructor.SOME, S);

        writer.add(new GCI(new ConceptName("A"), existsR));
        writer.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, invR), existsS));
        writer.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, invS), existsR));
        writer.add(new RoleInclusion(invS, U));
        writer.add(new RoleInclusion(R, U));
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.close();
    }

    @Test
    public void testCompleteDataInvolvedWithLoop() throws IOException, InterruptedException {
        Role R = new Role("R");
        Role S = new Role("S");
        Role U = new Role("U");

        Set<ConceptAssertion> expectedCA = CollectionUtils.newHashSet(new ConceptAssertion(new ConceptName("A"), "a"));
        Set<ObjectRoleAssertion> expectedRA = new HashSet<ObjectRoleAssertion>();
        // generating role completion
        expectedRA.add(new ObjectRoleAssertion(R, "a", "c_R0"));
        expectedRA.add(new ObjectRoleAssertion(S, "c_R0", "c_S1"));
        expectedRA.add(new ObjectRoleAssertion(U, "c_U-0", "c_R0"));
        expectedRA.add(new ObjectRoleAssertion(R, "c_S1", "c_R1"));
        expectedRA.add(new ObjectRoleAssertion(U, "c_S1", "c_U1"));
        expectedRA.add(new ObjectRoleAssertion(S, "c_R1", "c_S0"));
        expectedRA.add(new ObjectRoleAssertion(U, "c_U-1", "c_R1"));
        expectedRA.add(new ObjectRoleAssertion(R, "c_S0", "c_R0"));
        expectedRA.add(new ObjectRoleAssertion(U, "c_S0", "c_U0"));
        expectedRA.add(new ObjectRoleAssertion(U, "a", "c_U0"));
        // role inclusion completion
        expectedRA.add(new ObjectRoleAssertion(U, "a", "c_R0"));
        expectedRA.add(new ObjectRoleAssertion(U, "c_S1", "c_R1"));
        expectedRA.add(new ObjectRoleAssertion(U, "c_S0", "c_R0"));
        expectedRA.add(new ObjectRoleAssertion(U, "c_S1", "c_R0"));
        expectedRA.add(new ObjectRoleAssertion(U, "c_S0", "c_R1"));

        putLoopData();
        loadAndCompleteData();
        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(expectedCA, materializer.getConceptAssertions());
        Assert.assertEquals(expectedRA, materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteGenRoleForEmptyABox() throws IOException, InterruptedException {
        writer.add(new GCI(new ConceptName("A"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T"))));
        writer.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T", true)), new ConceptName("B")));
        writer.add(new GCI(new ConceptName("B"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"))));
        writer.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true)), new ConceptName("A")));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(Collections.<ObjectRoleAssertion>emptySet(), materializer.getRoleAssertions());
    }

    @Test
    public void testValidateCompletionOrdering() throws IOException, InterruptedException {
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

        writer.add(new GCI(A, new RoleRestriction(RoleRestriction.Constructor.SOME, R)));
        writer.add(new GCI(someInvS, someT));
        writer.add(new GCI(someInvU, someP));
        writer.add(new RoleInclusion(R, S));
        writer.add(new RoleInclusion(T, U));
        writer.add(new ConceptAssertion(A, "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));

        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(A, "a")), materializer.getConceptAssertions());

        Set<ObjectRoleAssertion> expected = new HashSet<ObjectRoleAssertion>();
        // generating role completion
        expected.add(new ObjectRoleAssertion(R, "a", "c_R0"));
        expected.add(new ObjectRoleAssertion(T, "c_R0", "c_T0"));
        expected.add(new ObjectRoleAssertion(P, "c_T0", "c_P0"));
        expected.add(new ObjectRoleAssertion(U, "c_R0", "c_U0"));
        expected.add(new ObjectRoleAssertion(P, "c_U0", "c_P0"));
        expected.add(new ObjectRoleAssertion(S, "c_S-0", "c_R0"));
        expected.add(new ObjectRoleAssertion(U, "c_U-0", "c_T0"));
        expected.add(new ObjectRoleAssertion(S, "a", "c_S0"));
        expected.add(new ObjectRoleAssertion(T, "c_S0", "c_T0"));
        expected.add(new ObjectRoleAssertion(U, "c_S0", "c_U0"));
        // role inclusion completion        
        expected.add(new ObjectRoleAssertion(S, "a", "c_R0"));
        expected.add(new ObjectRoleAssertion(U, "c_R0", "c_T0"));
        expected.add(new ObjectRoleAssertion(U, "c_S0", "c_T0"));
        Assert.assertEquals(expected, materializer.getRoleAssertions());
    }

    @Test
    public void testInferredTBoxInclusionFromRoleHiearchy() throws IOException, InterruptedException {
        Role R = new Role("R");
        Role S = new Role("S");
        RoleRestriction existsR = new RoleRestriction(RoleRestriction.Constructor.SOME, R);
        RoleRestriction existsS = new RoleRestriction(RoleRestriction.Constructor.SOME, S);
        RoleRestriction existsInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true));
        RoleRestriction existsInvS = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("S", true));
        ConceptName A = new ConceptName("A");
        ConceptName B = new ConceptName("B");
        ConceptName C = new ConceptName("C");

        writer.add(new GCI(A, existsR));
        writer.add(new GCI(existsS, B));
        writer.add(new GCI(A, C));
        writer.add(new RoleInclusion(R, S));
        writer.add(new ConceptAssertion(A, "a"));
        writer.close();

        loadAndCompleteData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));

        Set<GCI> expectedCI = new HashSet<GCI>();
        expectedCI.add(new GCI(A, existsR));
        expectedCI.add(new GCI(existsS, B));
        expectedCI.add(new GCI(A, C));
        // inferred GCIs
        expectedCI.add(new GCI(existsR, existsS));
        expectedCI.add(new GCI(existsInvR, existsInvS));
        expectedCI.add(new GCI(existsR, B));
        expectedCI.add(new GCI(A, existsS));
        expectedCI.add(new GCI(A, B));
        Assert.assertEquals(expectedCI, materializer.getConceptInclusions());

        Set<ConceptAssertion> expectedCA = new HashSet<ConceptAssertion>();
        expectedCA.add(new ConceptAssertion(A, "a"));
        expectedCA.add(new ConceptAssertion(B, "a"));
        expectedCA.add(new ConceptAssertion(C, "a"));
        expectedCA.add(new ConceptAssertion(B, "c_S-0"));
        Assert.assertEquals(expectedCA, materializer.getConceptAssertions());

        Set<ObjectRoleAssertion> expectedRA = new HashSet<ObjectRoleAssertion>();
        expectedRA.add(new ObjectRoleAssertion(R, "a", "c_R0"));
        expectedRA.add(new ObjectRoleAssertion(S, "a", "c_S0"));
        expectedRA.add(new ObjectRoleAssertion(S, "a", "c_R0"));
        expectedRA.add(new ObjectRoleAssertion(S, "c_S-0", "c_R0"));
        Assert.assertEquals(expectedRA, materializer.getRoleAssertions());
    }

    @Test
    public void testCompleteDataCNQRN() throws IOException, InterruptedException {
        ConceptName A = new ConceptName("A");
        ConceptName B = new ConceptName("B");
        Role R = new Role("R");
        RoleRestriction existsRB = new RoleRestriction(RoleRestriction.Constructor.SOME, R, B);

        writer.add(new GCI(A, existsRB));
        writer.add(new ConceptAssertion(A, "a"));
        writer.close();

        loadAndCompleteData();

        Role newRole = new Role(QualifiedExistentialEncoder.URI + "0");
        final RoleRestriction existsNewRole = new RoleRestriction(RoleRestriction.Constructor.SOME, newRole);
        final RoleRestriction existsNewRoleInv = new RoleRestriction(RoleRestriction.Constructor.SOME, newRole.copy().toggleInverse());
        final RoleRestriction existsOriginalRole = new RoleRestriction(RoleRestriction.Constructor.SOME, R);
        final RoleRestriction existsOriginalRoleInv = new RoleRestriction(RoleRestriction.Constructor.SOME, R.copy().toggleInverse());
        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));

        Set<GCI> expectedCI = new HashSet<GCI>();
        expectedCI.add(new GCI(A, existsNewRole));
        expectedCI.add(new GCI(existsNewRoleInv, B));
        expectedCI.add(new GCI(existsNewRole, existsOriginalRole));
        expectedCI.add(new GCI(existsNewRoleInv, existsOriginalRoleInv));
        expectedCI.add(new GCI(A, existsOriginalRole));
        Assert.assertEquals(expectedCI, materializer.getConceptInclusions());

        Assert.assertEquals(CollectionUtils.newHashSet(new RoleInclusion(newRole, R),
                new RoleInclusion(newRole.copy().toggleInverse(), R.copy().toggleInverse())),
                materializer.getRoleInclusions());
        
        AnonymousIndividual anonIndv = new AnonymousIndividual(newRole);
        
        Assert.assertEquals(CollectionUtils.newHashSet(new ObjectRoleAssertion(R, "a", anonIndv.toString()), 
                new ObjectRoleAssertion(R, "a", "c_R0"),
                new ObjectRoleAssertion(R, "c_R-0", anonIndv.toString())),
                materializer.getRoleAssertions());
        
        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(A, "a"), 
                new ConceptAssertion(B, anonIndv.toString())),
                materializer.getConceptAssertions());
    }
    
    @Test
    public void testCompleteDataRNQRN() throws IOException, InterruptedException {
        ConceptName A = new ConceptName("A");
        Role R = new Role("R");
        Role S = new Role("S");
        RoleRestriction existsS = new RoleRestriction(RoleRestriction.Constructor.SOME, S);
        RoleRestriction existsRA = new RoleRestriction(RoleRestriction.Constructor.SOME, R, A);

        writer.add(new GCI(existsS, existsRA));
        writer.add(new ObjectRoleAssertion(S, "a", "b"));
        writer.close();

        loadAndCompleteData();

        Role newRole = new Role(QualifiedExistentialEncoder.URI + "0");
        AnonymousIndividual anonIndv = new AnonymousIndividual(newRole);
        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Set<ObjectRoleAssertion> assertions = materializer.getRoleAssertions();
        Assert.assertTrue(assertions.contains(new ObjectRoleAssertion(R, "a", anonIndv.toString())));
        Assert.assertFalse(assertions.contains(new ObjectRoleAssertion(newRole, "a", anonIndv.toString())));
        Assert.assertTrue(materializer.getConceptAssertions().contains(new ConceptAssertion(A, anonIndv.toString())));
    }
    
    @Test
    public void testCompleteDataInvQRN() throws IOException, InterruptedException {
        ConceptName A = new ConceptName("A");
        Role R = new Role("R");
        Role S = new Role("S");
        Role invS = new Role("S", true);
        RoleRestriction existsInvS = new RoleRestriction(RoleRestriction.Constructor.SOME, invS);
        RoleRestriction existsRA = new RoleRestriction(RoleRestriction.Constructor.SOME, R, A);
        
        writer.add(new GCI(existsInvS, existsRA));
        writer.add(new ObjectRoleAssertion(S, "a", "b"));
        writer.close();

        loadAndCompleteData();

        Role newRole = new Role(QualifiedExistentialEncoder.URI + "0");
        AnonymousIndividual anonIndv = new AnonymousIndividual(newRole);
        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Set<ObjectRoleAssertion> assertions = materializer.getRoleAssertions();
        Assert.assertTrue(assertions.contains(new ObjectRoleAssertion(R, "b", anonIndv.toString())));
        Assert.assertFalse(assertions.contains(new ObjectRoleAssertion(newRole, "b", anonIndv.toString())));
        Assert.assertTrue(materializer.getConceptAssertions().contains(new ConceptAssertion(A, anonIndv.toString())));
    }
    
    @Test
    public void testCompleteDataCNQInv() throws IOException, InterruptedException {
        ConceptName A = new ConceptName("A");
        ConceptName B = new ConceptName("B");
        Role R = new Role("R");
        Role invR = new Role("R", true);
        RoleRestriction existsInvRB = new RoleRestriction(RoleRestriction.Constructor.SOME, invR, B);

        writer.add(new GCI(A, existsInvRB));
        writer.add(new ConceptAssertion(A, "a"));
        writer.close();

        loadAndCompleteData();

        Role newRole = new Role(QualifiedExistentialEncoder.URI + "0");
        AnonymousIndividual anonIndv = new AnonymousIndividual(newRole);
        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Set<ObjectRoleAssertion> assertions = materializer.getRoleAssertions();
        Assert.assertTrue(assertions.contains(new ObjectRoleAssertion(R, anonIndv.toString(), "a")));
        Assert.assertFalse(assertions.contains(new ObjectRoleAssertion(newRole, "a", anonIndv.toString())));
        Assert.assertTrue(materializer.getConceptAssertions().contains(new ConceptAssertion(B, anonIndv.toString())));
    }
    
    @Test
    public void testCompleteDataRNQInv() throws IOException, InterruptedException {
        ConceptName A = new ConceptName("A");
        Role R = new Role("R");
        Role invR = new Role("R", true);
        Role S = new Role("S");
        RoleRestriction existsS = new RoleRestriction(RoleRestriction.Constructor.SOME, S);
        RoleRestriction existsInvRA = new RoleRestriction(RoleRestriction.Constructor.SOME, invR, A);

        writer.add(new GCI(existsS, existsInvRA));
        writer.add(new ObjectRoleAssertion(S, "a", "b"));
        writer.close();

        loadAndCompleteData();

        Role newRole = new Role(QualifiedExistentialEncoder.URI + "0");
        AnonymousIndividual anonIndv = new AnonymousIndividual(newRole);
        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Set<ObjectRoleAssertion> assertions = materializer.getRoleAssertions();
        Assert.assertTrue(assertions.contains(new ObjectRoleAssertion(R, anonIndv.toString(), "a")));
        Assert.assertFalse(assertions.contains(new ObjectRoleAssertion(newRole, "a", anonIndv.toString())));
        Assert.assertTrue(materializer.getConceptAssertions().contains(new ConceptAssertion(A, anonIndv.toString())));
    }
    
    @Test
    public void testCompleteDataInvQInv() throws IOException, InterruptedException {
        ConceptName A = new ConceptName("A");
        Role R = new Role("R");
        Role invR = new Role("R", true);
        Role S = new Role("S");
        Role invS = new Role("S", true);
        RoleRestriction existsInvS = new RoleRestriction(RoleRestriction.Constructor.SOME, invS);
        RoleRestriction existsInvRA = new RoleRestriction(RoleRestriction.Constructor.SOME, invR, A);
        
        writer.add(new GCI(existsInvS, existsInvRA));
        writer.add(new ObjectRoleAssertion(S, "a", "b"));
        writer.close();

        loadAndCompleteData();

        Role newRole = new Role(QualifiedExistentialEncoder.URI + "0");
        AnonymousIndividual anonIndv = new AnonymousIndividual(newRole);
        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Set<ObjectRoleAssertion> assertions = materializer.getRoleAssertions();
        Assert.assertTrue(assertions.contains(new ObjectRoleAssertion(R, anonIndv.toString(), "b")));
        Assert.assertFalse(assertions.contains(new ObjectRoleAssertion(newRole, "b", anonIndv.toString())));
        Assert.assertTrue(materializer.getConceptAssertions().contains(new ConceptAssertion(A, anonIndv.toString())));
    }
    
    @Test
    public void testCompleteRedundantCNForQualified() throws IOException, InterruptedException {
        Role R = new Role("R");
        ConceptName A = new ConceptName("A");
        ConceptName B = new ConceptName("B");
        ConceptName C = new ConceptName("C");
        
        writer.add(new GCI(A, B));
        writer.add(new GCI(C, new RoleRestriction(RoleRestriction.Constructor.SOME, R, B)));
        writer.add(new ObjectRoleAssertion(R, "a", "b"));
        writer.add(new ConceptAssertion(C, "a"));
        writer.add(new ConceptAssertion(A, "b"));
        writer.close();
        
        loadAndCompleteData();
        
        DBToMemLoader materializer = new DBToMemLoader(PROJECTS.get(0));
        Assert.assertEquals(CollectionUtils.newHashSet(new ObjectRoleAssertion(R, "a", "b")), materializer.getRoleAssertions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(A, "b"), new ConceptAssertion(B, "b"), new ConceptAssertion(C, "a")), materializer.getConceptAssertions());
    }
}
