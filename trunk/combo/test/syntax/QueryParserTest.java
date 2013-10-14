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

import de.unibremen.informatik.tdki.combo.syntax.query.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author İnanç Seylan
 */
public class QueryParserTest {

    private QueryParser parser;

    public QueryParserTest() {
    }

    @Before
    public void setUp() {
        parser = new QueryParser();
    }

    @Test
    public void testTrim() {
        String s = " hello";
        Assert.assertEquals("hello", s.trim());
    }

    @Test
    public void testQuery1() {
        ConjunctiveQuery q = parser.parse("Q(?0) <- worksFor(?0,?1), affiliatedOrganizationOf(?1,?2)");
        ConjunctiveQuery expected = new ConjunctiveQuery(new Head("Q", "?0"), new RoleAtom("worksFor", "?0", "?1"), new RoleAtom("affiliatedOrganizationOf", "?1", "?2"));

        Assert.assertEquals(expected, q);
    }

    @Test
    public void testQuery2() {
        ConjunctiveQuery q = parser.parse(" Q( ?0, ?1 ,?2)  <-  ResearchAssistant(?0), advisor( ?0, ?1), takesCourse(?0,?2), teacherOf(?1,?2)");

        ConjunctiveQuery expected = new ConjunctiveQuery(new Head("Q", "?0", "?1", "?2"), new ConceptAtom("ResearchAssistant", "?0"), new RoleAtom("advisor", "?0", "?1"), new RoleAtom("takesCourse", "?0", "?2"), new RoleAtom("teacherOf", "?1", "?2"));

        Assert.assertEquals(expected, q);
    }

    @Test
    public void testQuery3() {
        ConjunctiveQuery q = parser.parse("Q(?0,?1) <- ResearchAssistant(?0), TeachingAssistant(?1), advisor(?0,?2), advisor(?1,?2), memberOf(?2,?3), Subj20Department(?3) ");

        ConjunctiveQuery expected = new ConjunctiveQuery(new Head("Q", "?0", "?1"), new ConceptAtom("ResearchAssistant", "?0"), new ConceptAtom("TeachingAssistant", "?1"),
                new RoleAtom("advisor", "?0", "?2"), new RoleAtom("advisor", "?1", "?2"), new RoleAtom("memberOf", "?2", "?3"), new ConceptAtom("Subj20Department", "?3"));

        System.out.println(q.getMentioningQueries("?3"));

        Assert.assertEquals(expected, q);
    }

    @Test
    public void testRewritingParse1() throws FileNotFoundException, IOException {
        NRDatalogProgram expected = new NRDatalogProgram();

        ConjunctiveQuery q1 = new ConjunctiveQuery(new Head("Q", "?0"), new RoleAtom("headOf", "?0", "?1"), new RoleAtom("affiliatedOrganizationOf", "?1", "?2"));
        expected.addRule(q1);

        ConjunctiveQuery q2 = new ConjunctiveQuery(new Head("Q", "?0"), new RoleAtom("worksFor", "?0", "?1"), new RoleAtom("affiliatedOrganizationOf", "?1", "?2"));
        expected.addRule(q2);

        NRDatalogProgram query = parser.parseDatalogFile(new File("test/syntax/requiem1_rap_f.txt"));
        Assert.assertEquals(query, expected);
    }
    
    @Test
    public void testRewritingPresto() throws FileNotFoundException, IOException {
        NRDatalogProgram query = parser.parseDatalogFile(new File("test/syntax/presto_rewriting.txt"));
        System.out.println(query);
    }
}
