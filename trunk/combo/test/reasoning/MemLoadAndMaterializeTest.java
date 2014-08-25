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
import de.unibremen.informatik.tdki.combo.data.DBConfig;
import de.unibremen.informatik.tdki.combo.data.DBConnPool;
import de.unibremen.informatik.tdki.combo.data.DBLayout;
import de.unibremen.informatik.tdki.combo.data.DBToMemLoader;
import de.unibremen.informatik.tdki.combo.data.MemToBulkFileWriter;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ConceptAssertion;
import de.unibremen.informatik.tdki.combo.syntax.axiom.GCI;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ObjectRoleAssertion;
import de.unibremen.informatik.tdki.combo.syntax.axiom.RoleInclusion;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import de.unibremen.informatik.tdki.combo.syntax.concept.RoleRestriction;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Assert;
import org.apache.commons.dbutils.DbUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author İnanç Seylan
 */
public class MemLoadAndMaterializeTest {
    private static final String PROJECT = "test";
    
    private DBLayout layout;
    
    private File dataFile;
    
    private MemToBulkFileWriter writer;
    
    private static Connection connection;

    @BeforeClass
    public static void setUpClass() {
        DBConnPool pool = new DBConnPool(DBConfig.fromPropertyFile());
        connection = pool.getConnection();
    }

    @AfterClass
    public static void tearDownClass() {
        DbUtils.closeQuietly(connection);
    }
    
     @Before
    public void setUp() throws IOException, InterruptedException {
        layout = new DBLayout(true, connection);
        layout.initialize();
        layout.createProject(PROJECT);

        dataFile = File.createTempFile("test", "combo");
        dataFile.deleteOnExit();
        writer = new MemToBulkFileWriter(dataFile);
    }
     
     private void loadData() {
        layout.loadProject(dataFile, PROJECT);
        layout.completeData(PROJECT);
    }

    @Test
    public void testAddTBox() throws IOException, InterruptedException {
        final ConceptName A = new ConceptName("A");
        final RoleRestriction someT = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T"));
        final RoleRestriction someInvT = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T", true));
        final ConceptName B = new ConceptName("B");
        final RoleRestriction someR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"));
        final RoleRestriction someInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true));

        GCI A_someT = new GCI(A, someT);
        GCI someInvT_B = new GCI(someInvT, B);
        GCI B_someR = new GCI(B, someR);
        GCI someInvR_A = new GCI(someInvR, A);

        writer.add(A_someT);
        writer.add(someInvT_B);
        writer.add(B_someR);
        writer.add(someInvR_A);
        writer.close();
        
        loadData();

        Set<GCI> expected = new HashSet<GCI>();
        expected.add(A_someT);
        expected.add(someInvT_B);
        expected.add(B_someR);
        expected.add(someInvR_A);
        expected.add(new GCI(someInvR, someT));
        expected.add(new GCI(someInvT, someR));

        DBToMemLoader materializer = new DBToMemLoader(PROJECT, connection);
        Assert.assertEquals(expected, materializer.getConceptInclusions());
    }

    @Test
    public void testTransitivelyCloseTBox() throws IOException, InterruptedException {
        ConceptName A = new ConceptName("A");
        ConceptName B = new ConceptName("B");
        RoleRestriction existsInvR = new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true));

        writer.add(new GCI(A, existsInvR));
        writer.add(new GCI(existsInvR, B));
        writer.close();
        
        loadData();

        Set<GCI> expected = new HashSet<GCI>();
        expected.add(new GCI(A, existsInvR));
        expected.add(new GCI(existsInvR, B));
        expected.add(new GCI(A, B));

        DBToMemLoader materializer = new DBToMemLoader(PROJECT, connection);
        Assert.assertEquals(expected, materializer.getConceptInclusions());
    }

    @Test
    public void testAddABox() throws IOException, InterruptedException {
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.add(new ObjectRoleAssertion(new Role("R"), "a", "b"));
        writer.close();
        
        loadData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECT, connection);

        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(new ConceptName("A"), "a")), materializer.getConceptAssertions());

        Assert.assertEquals(CollectionUtils.newHashSet(new ObjectRoleAssertion(new Role("R"), "a", "b")), materializer.getRoleAssertions());
    }

    @Test
    public void testAddRBox() throws IOException, InterruptedException {
        Role R = new Role("R");
        Role invR = new Role("R", true);
        Role S = new Role("S");
        Role invS = new Role("S", true);
        Role T = new Role("T");
        Role invT = new Role("T", true);

        writer.add(new RoleInclusion(R, invS));
        writer.add(new RoleInclusion(S, invT));
        writer.close();
        
        loadData();

        Set<RoleInclusion> expected = new HashSet<RoleInclusion>();
        expected.add(new RoleInclusion(R, invS));
        expected.add(new RoleInclusion(S, invT));
        expected.add(new RoleInclusion(invR, S));
        expected.add(new RoleInclusion(invS, T));
        expected.add(new RoleInclusion(R, T));
        expected.add(new RoleInclusion(invR, invT));

        DBToMemLoader materializer = new DBToMemLoader(PROJECT, connection);

        Assert.assertEquals(expected, materializer.getRoleInclusions());
    }

    @Test
    public void testAddTBoxABoxWithOneRoleInCommon() throws IOException, InterruptedException {
        GCI ASomeRInv = new GCI(new ConceptName("A"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true)));

        writer.add(ASomeRInv);
        writer.add(new ObjectRoleAssertion(new Role("R"), "a", "b"));
        writer.add(new ConceptAssertion(new ConceptName("B"), "b"));
        writer.close();
        
        loadData();

        DBToMemLoader materializer = new DBToMemLoader(PROJECT, connection);

        Assert.assertEquals(CollectionUtils.newHashSet(ASomeRInv), materializer.getConceptInclusions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ConceptAssertion(new ConceptName("B"), "b")), materializer.getConceptAssertions());
        Assert.assertEquals(CollectionUtils.newHashSet(new ObjectRoleAssertion(new Role("R"), "a", "b")), materializer.getRoleAssertions());
    }

}
