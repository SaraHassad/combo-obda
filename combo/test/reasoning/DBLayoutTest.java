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

import de.unibremen.informatik.tdki.combo.data.DBLayout;
import de.unibremen.informatik.tdki.combo.data.MemToBulkFileWriter;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ConceptAssertion;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.*;

/**
 *
 * @author İnanç Seylan
 */
public class DBLayoutTest {
    
    private static final List<String> PROJECTS = Arrays.asList("test");
    
    private DBLayout layout;

    @Before
    public void setUp() {
        layout = new DBLayout(true);
        layout.initialize();
        layout.createProject(PROJECTS);
    }

    @Test
    public void testExport() throws IOException, InterruptedException {
        File input = File.createTempFile("test", "combo");
        input.deleteOnExit();
        MemToBulkFileWriter writer = new MemToBulkFileWriter(input);
        writer.add(new ConceptAssertion(new ConceptName("A"), "a"));
        writer.close();
        
        layout.loadProject(input, PROJECTS.get(0));
        
        File output = File.createTempFile("test", "combo");
        output.deleteOnExit();
        layout.exportProject(PROJECTS.get(0), output);
    }
}
