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

package eugen;

import de.unibremen.informatik.tdki.combo.ui.EUGenToCombo;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author İnanç Seylan
 */
public class UniToDBFileExtractTest {
    

    public UniToDBFileExtractTest() {
    }

    private void extractData(int uni, int hole, int subclass) {
        final String OWL_DATA_PATH = "/tmp/modlubm/" + uni + "." + hole + "." + subclass + "/";
        final String DB_DATA_PATH = OWL_DATA_PATH + "db/";
        
        EUGenToCombo.extract(new File("test/modifiedlubm/carstenlubm_def_exists_20.owl"), new File(OWL_DATA_PATH), new File(DB_DATA_PATH));
    }

    @Test
    public void testExtract_1_5_20() {
        extractData(1, 5, 20);
    }

    @Test
    public void testExtract_10_5_20() {
        extractData(10, 5, 20);
    }

    private void genOntSubclasses(int subclasses) throws IOException {
        EUGenToCombo.addSubclasses("test/modifiedlubm/carstenlubm_def_exists_0.owl", subclasses);
    }

    @Test
    public void genOntSubclasses() throws IOException {
        genOntSubclasses(20);
        genOntSubclasses(40);
        genOntSubclasses(60);
        genOntSubclasses(80);
        genOntSubclasses(100);
    }
}
