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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.unibremen.informatik.tdki.combo.common.Tuple;
import de.unibremen.informatik.tdki.combo.data.DBConfig;
import de.unibremen.informatik.tdki.combo.data.DBConnPool;
import de.unibremen.informatik.tdki.combo.data.DBLayout;
import de.unibremen.informatik.tdki.combo.data.MemToBulkFileWriter;
import de.unibremen.informatik.tdki.combo.rewriting.FilterRewriterDB2;
import de.unibremen.informatik.tdki.combo.rewriting.RewritingException;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ConceptAssertion;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ObjectRoleAssertion;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import de.unibremen.informatik.tdki.combo.syntax.query.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import org.apache.commons.dbutils.DbUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author İnanç Seylan
 */
public class BasicQueryRewriterTest {

    private FilterRewriterDB2 rewriter;
    
    private static final String PROJECT = "test";
    
    private File dataFile;
    
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
    public void setUp() {
        DBLayout layout = new DBLayout(true, connection);
        layout.initialize();
        layout.createProject(PROJECT);
        rewriter = new FilterRewriterDB2(PROJECT, connection);
    }
    
    private MemToBulkFileWriter getWriter() throws IOException, InterruptedException {
        dataFile = File.createTempFile("test", "combo");
        dataFile.deleteOnExit();
        return new MemToBulkFileWriter(dataFile);
    }
    
    private void loadData() {
        DBLayout layout = new DBLayout(true, connection);
        layout.loadProject(dataFile, PROJECT);
        // layout.completeData(PROJECTS);
    }
    
    private <E> Multiset<Tuple<E>> getTuples(String query) {
        return TestUtils.getTuples(connection, query);
    }

    @Test
    public void testQueryContainsSymbolNotInKB() throws IOException, InterruptedException {
        MemToBulkFileWriter writer = getWriter();
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.add(new ObjectRoleAssertion(new Role("R"), "a", "b"));
        writer.close();
        
        loadData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q", "x"), new ConceptAtom("A", "x"));
        rewriter.rewrite(query, false, false, false);

        query = new ConjunctiveQuery(new Head("Q", "x"), new ConceptAtom("B", "x"));
        boolean exceptionThrown = false;
        try {
            rewriter.rewrite(query, false, false, false);
        } catch (RewritingException e) {
            System.err.println(e.getMessage());
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        query = new ConjunctiveQuery(new Head("Q"), new ConceptAtom("A", "x"), new RoleAtom("S", "x", "y"));
        exceptionThrown = false;
        try {
            rewriter.rewrite(query, false, false, false);
        } catch (RewritingException e) {
            System.err.println(e.getMessage());
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }

    @Test
    public void testBasicQueries() throws IOException, InterruptedException {
        MemToBulkFileWriter writer = getWriter();
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.add(new ObjectRoleAssertion(new Role("R"), "a", "b"));
        writer.add(new ObjectRoleAssertion(new Role("R"), "a", "a"));
        writer.add(new ObjectRoleAssertion(new Role("S"), "b", "c"));
        writer.add(new ObjectRoleAssertion(new Role("S"), "d", "e"));
        writer.close();
        
        loadData();

        Query q = new ConceptAtom("A", "x");
        Multiset<Tuple<String>> rs = getTuples(rewriter.rewrite(q, true, false, false).toString());
        Multiset<Tuple<String>> expected = HashMultiset.create();
        expected.add(new Tuple<String>("a"));
        Assert.assertEquals(expected, rs);

        q = new RoleAtom("R", "x", "y");
        rs = getTuples(rewriter.rewrite(q, true, false, false).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("a", "a"));
        expected.add(new Tuple<String>("a", "b"));
        Assert.assertEquals(expected, rs);
        
        rs = getTuples(rewriter.rewrite(q, true, true, true).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("2"));
        Assert.assertEquals(expected, rs);

        q = new RoleAtom("R", "x", "x");
        rs = getTuples(rewriter.rewrite(q, true, false, false).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("a", "a"));
        Assert.assertEquals(expected, rs);

        q = new ConjunctiveQuery(new Head("Q", "x", "y"), new ConceptAtom("A", "x"),
                new RoleAtom("R", "x", "y"), new RoleAtom("S", "y", "z"));
        rs = getTuples(rewriter.rewrite(q, true, false, false).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("a", "b"));
        Assert.assertEquals(expected, rs);

        q = new ConjunctiveQuery(new Head("Q", "x"), new ConceptAtom("A", "x"), new RoleAtom("R", "x", "y"));
        rs = getTuples(rewriter.rewrite(q, true, false, false).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("a"));
        expected.add(new Tuple<String>("a"));
        Assert.assertEquals(expected, rs);
        
        rs = getTuples(rewriter.rewrite(q, true, false, true).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("a"));
        Assert.assertEquals(expected, rs);
        
        q = new DisjunctiveQuery(new ConjunctiveQuery(new Head("Q", "x"), new RoleAtom("R", "x", "y")), 
                new ConjunctiveQuery(new Head("Q", "x"), new RoleAtom("S", "x", "y")));
        rs = getTuples(rewriter.rewrite(q, true, false, false).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("a"));
        expected.add(new Tuple<String>("b"));
        expected.add(new Tuple<String>("d"));
        Assert.assertEquals(expected, rs);
        
        rs = getTuples(rewriter.rewrite(q, true, true, false).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("3"));
        Assert.assertEquals(expected, rs);
        
        rs = getTuples(rewriter.rewrite(q, true, true, true).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("3"));
        Assert.assertEquals(expected, rs);
    }
    
    // TODO fill this class with tests!
}