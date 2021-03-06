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

import de.unibremen.informatik.tdki.ExtendedGenerator;
import org.junit.Test;

/**
 *
 * @author İnanç Seylan
 */
public class ModifiedDataGeneratingTest {

    private void generateNUni(int uni, int hole, int subclass) {
        String args[] = {"-univ", Integer.toString(uni), "-onto", "http://swat.cse.lehigh.edu/onto/univ-bench.owl", "-hole", Integer.toString(hole),
            "-subclass", Integer.toString(subclass), "-dir", "/tmp/modlubm/" + uni + "." + hole + "." + subclass};
        ExtendedGenerator.main(args);
        // no need for the following anymore because I changed the path where the owl files are created.
        // DataGeneratingTest.rename("/home/seylan/NetBeansProjects");
    }

    @Test
    public void printuserdir() {
        System.out.println(System.getProperty("user.dir"));
    }

    @Test
    public void testGenerate_1_5_20() {
        generateNUni(1, 5, 20);
    }
    
    @Test
    public void testGenerate_10_5_20() {
        generateNUni(10, 5, 20);
    }

    @Test
    public void testGenerate_1_50_20() {
        generateNUni(1, 50, 20);
    }

    @Test
    public void testGenerate1UniWithVaryingSubclasses() {
        String args[] = {"-univ", Integer.toString(1), "-onto", "http://swat.cse.lehigh.edu/onto/univ-bench.owl", "-hole", Integer.toString(5), "-subclass", Integer.toString(1)};
        ExtendedGenerator.main(args);
    }
}
