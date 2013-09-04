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
import edu.lehigh.swat.bench.uba.Generator;
import java.io.File;
import org.junit.Test;

/**
 *
 * @author İnanç Seylan
 */
public class DataGeneratingTest {
    
    private static final String base = "/Users/seylan/NetBeansProjects";

    public DataGeneratingTest() {
    }

    public static void rename(String base) {
        File folder = new File(base);
        File[] listOfFiles = folder.listFiles();

        for (File f : listOfFiles) {
            if (f.isFile()) {
                String name = f.getName();
                if (name.startsWith("uba1.7\\")) {
                    System.out.println(name);
                    File newFile = new File(base + "/" + name.substring(7));
                    f.renameTo(newFile);
                    System.out.println(newFile.getName());
                }
            }
        }
    }

    @Test
    public void testGenerate1Uni() {
        String args[] = {"-univ", "1", "-onto", "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl"};
        Generator.main(args);
        rename(base);
    }

    @Test
    public void testGenerate5Uni() {
        String args[] = {"-univ", "5", "-onto", "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl"};
        Generator.main(args);
        rename(base);
    }
    
    @Test
    public void testGenerate10Uni() {
        String args[] = {"-univ", "10", "-onto", "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl"};
        Generator.main(args);
        rename(base);
    }
    
    @Test
    public void testGenerate20Uni() {
        String args[] = {"-univ", "20", "-onto", "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl"};
        Generator.main(args);
        rename(base);
    }
    
    @Test
    public void testGenerate40Uni() {
        String args[] = {"-univ", "40", "-onto", "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl"};
        Generator.main(args);
        rename(base);
    }
}
