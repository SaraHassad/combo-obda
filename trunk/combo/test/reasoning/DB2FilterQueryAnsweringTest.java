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
import de.unibremen.informatik.tdki.combo.data.DB2Interface;
import de.unibremen.informatik.tdki.combo.data.DBLayout;
import de.unibremen.informatik.tdki.combo.data.JdbcTemplate;
import de.unibremen.informatik.tdki.combo.data.MemToBulkFileWriter;
import de.unibremen.informatik.tdki.combo.rewriting.FilterRewriterDB2;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ConceptAssertion;
import de.unibremen.informatik.tdki.combo.syntax.axiom.GCI;
import de.unibremen.informatik.tdki.combo.syntax.axiom.RoleInclusion;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import de.unibremen.informatik.tdki.combo.syntax.concept.RoleRestriction;
import de.unibremen.informatik.tdki.combo.syntax.query.ConceptAtom;
import de.unibremen.informatik.tdki.combo.syntax.query.ConjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.query.Head;
import de.unibremen.informatik.tdki.combo.syntax.query.RoleAtom;
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
public class DB2FilterQueryAnsweringTest {

    private FilterRewriterDB2 rewriter;
    private JdbcTemplate jdbcTemplate;
    private DBLayout layout;
    private static final List<String> PROJECTS = Arrays.asList("test");
    private File dataFile;
    private MemToBulkFileWriter writer;

    public DB2FilterQueryAnsweringTest() {
        jdbcTemplate = DB2Interface.getJDBCTemplate();
    }

    private void addTBoxAxioms(MemToBulkFileWriter writer) {
        writer.add(new GCI(new ConceptName("A"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T"))));
        writer.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("T", true)), new ConceptName("B")));
        writer.add(new GCI(new ConceptName("B"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R"))));
        writer.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("R", true)), new ConceptName("A")));
    }

    private void addABoxAxioms(MemToBulkFileWriter writer) {
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.add(new ConceptAssertion(new ConceptName("A"), "b"));
    }

    private void createABox1InFile() throws IOException, InterruptedException {
        writer = new MemToBulkFileWriter(dataFile);
        addABoxAxioms(writer);
        writer.close();
    }
    
    private void createKBInFile() throws IOException, InterruptedException {
        addTBoxAxioms(writer);
        addABoxAxioms(writer);
        writer.close();
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        layout = new DBLayout(true);
        layout.initialize();
        layout.createProject(PROJECTS);

        dataFile = File.createTempFile("test", "combo");
        dataFile.deleteOnExit();
        writer = new MemToBulkFileWriter(dataFile);

        rewriter = new FilterRewriterDB2(PROJECTS.get(0));
    }

    private void loadAndCompleteData() {
        layout.loadProject(dataFile, PROJECTS.get(0));
        layout.completeData(PROJECTS);
    }

    @Test
    public void testQuery1() throws IOException, InterruptedException {
        createABox1InFile();
        loadAndCompleteData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q", "x"), new ConceptAtom("A", "x"));

        Multiset<Tuple<String>> tuples = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());
        Multiset<Tuple<String>> expected = HashMultiset.create();
        expected.add(new Tuple<String>("a"));
        expected.add(new Tuple<String>("b"));
        Assert.assertEquals(expected, tuples);

        Multiset<Tuple<Integer>> tuples2 = TestUtils.getTuples(rewriter.filter(query, true, true, false).toString());
        Multiset<Tuple<Integer>> expected2 = HashMultiset.create();
        expected2.add(new Tuple<Integer>(new Integer(2)));
        Assert.assertEquals(expected2, tuples2);
    }

    @Test
    public void testQuery2() throws IOException, InterruptedException {
        createKBInFile();
        loadAndCompleteData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q", "x", "y"), new RoleAtom("R", "x", "y"));

        Multiset<Tuple<String>> rs = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());
        Assert.assertTrue(rs.isEmpty());
    }

    @Test
    public void testQuery3() throws IOException, InterruptedException {
        createKBInFile();
        loadAndCompleteData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q", "x", "y"), new RoleAtom("T", "x", "y"));

        Multiset<Tuple<String>> rs = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());
        Assert.assertTrue(rs.isEmpty());
    }

    @Test
    public void testQuery4() throws IOException, InterruptedException {
        createKBInFile();
        loadAndCompleteData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q", "x"), new RoleAtom("T", "x", "y"));

        Multiset<Tuple<String>> tuples = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());

        Multiset<Tuple<String>> expected = HashMultiset.create();
        expected.add(new Tuple<String>("a"));
        expected.add(new Tuple<String>("b"));
        Assert.assertEquals(expected, tuples);
    }

    @Test
    public void testQuery5() throws IOException, InterruptedException {
        createKBInFile();
        loadAndCompleteData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q", "y"), new RoleAtom("T", "x", "y"));

        Multiset<Tuple<String>> rs = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());
        Assert.assertTrue(rs.isEmpty());
    }

    @Test
    public void testQuery6() throws IOException, InterruptedException {
        createKBInFile();
        loadAndCompleteData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q"), new RoleAtom("T", "x", "y"));
        System.out.println(rewriter.filter(query, true, false, false).toString());
        Multiset<Tuple<String>> rs = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());
        Assert.assertEquals(1, rs.size());
    }

    @Test
    public void testQuery7() throws IOException, InterruptedException {
        createKBInFile();
        loadAndCompleteData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q"), new ConceptAtom("A", "x"));
        Multiset<Tuple<String>> rs = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());
        Assert.assertEquals(1, rs.size());
    }

    @Test
    public void testQuery8() throws IOException, InterruptedException {
        createKBInFile();
        loadAndCompleteData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q"), new RoleAtom("R", "x", "y"));
        Multiset<Tuple<String>> rs = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());
        Assert.assertEquals(1, rs.size());
    }

    // fork shaped query
    // Example 9 from the IJCAI11 paper of Carsten
    @Test
    public void testQuery9() throws IOException, InterruptedException {
        createKBInFile();
        loadAndCompleteData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q", "x1", "x2"), new RoleAtom("T", "x1", "y"), new RoleAtom("T", "x2", "y"));

        Multiset<Tuple<String>> rs = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());
        Multiset<Tuple<String>> expected = HashMultiset.create();
        expected.add(new Tuple<String>("a", "a"));
        expected.add(new Tuple<String>("b", "b"));
        Assert.assertEquals(expected, rs);
    }

    // Example 10 from the IJCAI11 paper of Carsten
    @Test
    public void testQuery10() throws IOException, InterruptedException {
        createKBInFile();
        loadAndCompleteData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q", "x"), new RoleAtom("T", "x", "y"), new RoleAtom("R", "y", "z"), new RoleAtom("T", "z", "y"));

        Multiset<Tuple<String>> rs = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());
        Assert.assertTrue(rs.isEmpty());
    }

    // Example 7 from SSWS+HPCSW 2012 paper 
    @Test
    public void testQuery11() throws IOException, InterruptedException {
        writer.add(new ConceptAssertion(new ConceptName("Faculty"), "a"));
        writer.add(new GCI(new ConceptName("Faculty"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("degreeFrom"))));
        writer.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("degreeFrom", true)), new ConceptName("Univ")));
        writer.add(new GCI(new ConceptName("Univ"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("deptOf", true))));
        writer.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("deptOf")), new ConceptName("Dept")));
        writer.add(new GCI(new ConceptName("Dept"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("teachesAt", true))));
        writer.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("teachesAt")), new ConceptName("Faculty")));
        writer.close();

        loadAndCompleteData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q"), new ConceptAtom("Faculty", "x"), new RoleAtom("degreeFrom", "x", "y"),
                new ConceptAtom("Univ", "y"), new RoleAtom("deptOf", "z", "y"), new ConceptAtom("Dept", "z"), new RoleAtom("teachesAt", "x", "z"));


        Multiset<Tuple<String>> rs = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());
        Assert.assertTrue(rs.isEmpty());
    }

    // Example 8 from SSWS+HPCSW 2012 paper 
    @Test
    public void testQuery12() throws IOException, InterruptedException {
        writer.add(new ConceptAssertion(new ConceptName("Employee"), "a"));
        writer.add(new GCI(new ConceptName("Employee"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("worksFor"))));
        writer.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("worksFor", true)), new ConceptName("Employer")));
        writer.add(new GCI(new ConceptName("Employer"), new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("paysSalaryOf"))));
        writer.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role("paysSalaryOf", true)), new ConceptName("Employee")));
        writer.add(new RoleInclusion(new Role("worksFor", true), new Role("isAffiliatedWith")));
        writer.add(new RoleInclusion(new Role("paysSalaryOf"), new Role("isAffiliatedWith")));
        writer.close();

        loadAndCompleteData();

        ConjunctiveQuery query = new ConjunctiveQuery(new Head("Q", "x"), new RoleAtom("worksFor", "x", "y"),
                new RoleAtom("paysSalaryOf", "y", "z"), new RoleAtom("isAffiliatedWith", "u", "z"));

        Multiset<Tuple<String>> rs = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());
        Multiset<Tuple<String>> expected = HashMultiset.create();
        expected.add(new Tuple<String>("a"));
        Assert.assertEquals(expected, rs);

        query = new ConjunctiveQuery(new Head("Q", "x"), new RoleAtom("worksFor", "x", "y"),
                new RoleAtom("paysSalaryOf", "y", "z"), new RoleAtom("worksFor", "z", "u"));
        rs = TestUtils.getTuples(rewriter.filter(query, true, false, false).toString());
        Assert.assertEquals(expected, rs);
    }
}
