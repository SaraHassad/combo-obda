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

import de.unibremen.informatik.tdki.combo.data.DBConfig;
import de.unibremen.informatik.tdki.combo.data.DBConnPool;
import de.unibremen.informatik.tdki.combo.data.DBLayout;
import de.unibremen.informatik.tdki.combo.data.MemToBulkFileWriter;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ConceptAssertion;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author İnanç Seylan
 */
public class DBLayoutTest {

    private DBLayout layout;

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
        layout = new DBLayout(true, connection);
        layout.initialize();
    }

    @Test
    public void testInitCreateDelete() {
        assertFalse(layout.createProject("test"));
        assertTrue(layout.createProject("test"));
        assertFalse(layout.createProject("deneme"));
        assertTrue(layout.createProject("deneme"));

        List<String> projectsAlt1 = new ArrayList<String>();
        projectsAlt1.add("test");
        projectsAlt1.add("deneme");
        List<String> projectsAlt2 = new ArrayList<String>();
        projectsAlt2.add("deneme");
        projectsAlt2.add("test");
        Assert.assertThat(layout.getProjects(),
                CoreMatchers.anyOf(CoreMatchers.is(projectsAlt1), CoreMatchers.is(projectsAlt2)));

        layout.dropProject("test");
        assertFalse(layout.createProject("test"));
        assertTrue(layout.createProject("test"));
        layout.updateStatistics("test");

        layout.initialize();
        assertFalse(layout.createProject("test"));
    }

    @Test
    public void testExport() throws IOException, InterruptedException {
        layout.createProject("test");

        File input = File.createTempFile("test", "combo");
        input.deleteOnExit();
        MemToBulkFileWriter writer = new MemToBulkFileWriter(input);
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.close();

        layout.loadProject(input, "test");

        File output = File.createTempFile("test", "combo");
        output.deleteOnExit();
        layout.exportProject("test", output);
    }
    
    // TODO: if you have time, you can improve the import and export tests
    // by checking that the expected data is actually there
}
