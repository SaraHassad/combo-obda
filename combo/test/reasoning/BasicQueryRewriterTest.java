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
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author İnanç Seylan
 */
public class BasicQueryRewriterTest {

    private FilterRewriterDB2 rewriter;
    
    private static final List<String> PROJECTS = Arrays.asList("test");
    
    private File dataFile;

    @Before
    public void setUp() {
        DBLayout layout = new DBLayout(true);
        layout.initialize();
        layout.createProject(PROJECTS);
        rewriter = new FilterRewriterDB2(PROJECTS.get(0));
    }
    
    private MemToBulkFileWriter getWriter() throws IOException, InterruptedException {
        dataFile = File.createTempFile("test", "combo");
        dataFile.deleteOnExit();
        return new MemToBulkFileWriter(dataFile);
    }
    
    private void loadData() {
        DBLayout layout = new DBLayout(true);
        layout.loadProject(dataFile, PROJECTS.get(0));
        // layout.completeData(PROJECTS);
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
        Multiset<Tuple<String>> rs = TestUtils.getTuples(rewriter.rewrite(q, true, false, false).toString());
        Multiset<Tuple<String>> expected = HashMultiset.create();
        expected.add(new Tuple<String>("a"));
        Assert.assertEquals(expected, rs);

        q = new RoleAtom("R", "x", "y");
        rs = TestUtils.getTuples(rewriter.rewrite(q, true, false, false).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("a", "a"));
        expected.add(new Tuple<String>("a", "b"));
        Assert.assertEquals(expected, rs);
        
        rs = TestUtils.getTuples(rewriter.rewrite(q, true, true, true).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("2"));
        Assert.assertEquals(expected, rs);

        q = new RoleAtom("R", "x", "x");
        rs = TestUtils.getTuples(rewriter.rewrite(q, true, false, false).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("a", "a"));
        Assert.assertEquals(expected, rs);

        q = new ConjunctiveQuery(new Head("Q", "x", "y"), new ConceptAtom("A", "x"),
                new RoleAtom("R", "x", "y"), new RoleAtom("S", "y", "z"));
        rs = TestUtils.getTuples(rewriter.rewrite(q, true, false, false).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("a", "b"));
        Assert.assertEquals(expected, rs);

        q = new ConjunctiveQuery(new Head("Q", "x"), new ConceptAtom("A", "x"), new RoleAtom("R", "x", "y"));
        rs = TestUtils.getTuples(rewriter.rewrite(q, true, false, false).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("a"));
        expected.add(new Tuple<String>("a"));
        Assert.assertEquals(expected, rs);
        
        rs = TestUtils.getTuples(rewriter.rewrite(q, true, false, true).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("a"));
        Assert.assertEquals(expected, rs);
        
        q = new DisjunctiveQuery(new ConjunctiveQuery(new Head("Q", "x"), new RoleAtom("R", "x", "y")), 
                new ConjunctiveQuery(new Head("Q", "x"), new RoleAtom("S", "x", "y")));
        rs = TestUtils.getTuples(rewriter.rewrite(q, true, false, false).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("a"));
        expected.add(new Tuple<String>("b"));
        expected.add(new Tuple<String>("d"));
        Assert.assertEquals(expected, rs);
        
        rs = TestUtils.getTuples(rewriter.rewrite(q, true, true, false).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("3"));
        Assert.assertEquals(expected, rs);
        
        rs = TestUtils.getTuples(rewriter.rewrite(q, true, true, true).toString());
        expected = HashMultiset.create();
        expected.add(new Tuple<String>("3"));
        Assert.assertEquals(expected, rs);
    }
    
    // TODO fill this class with tests!
}