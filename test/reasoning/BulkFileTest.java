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

import de.unibremen.informatik.tdki.combo.data.BulkFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author İnanç Seylan
 */
public class BulkFileTest {

    @Test
    public void testWriteAndRead() throws IOException, InterruptedException {
        File f = File.createTempFile("bulk", "combo");
        f.deleteOnExit();
        BulkFile bulk = new BulkFile(f);
        bulk.open(BulkFile.Open.WRITE);
        bulk.writeConceptAssertion(1, 2);
        bulk.close();

        bulk.open(BulkFile.Open.READ);
        File ca = bulk.getConceptAssertions();

        BufferedReader input = new BufferedReader(new FileReader(ca));
        int linesRead = 0;
        String line;
        try {
            while ((line = input.readLine()) != null) {
                linesRead++;
                String[] fields = line.split("\t");
                Assert.assertEquals(fields.length, 2);
                Assert.assertEquals(fields[0], "1");
                Assert.assertEquals(fields[1], "2");
            }
        } finally {
            input.close();
        }
        Assert.assertEquals(linesRead, 1);
        
        bulk.close();
    }
}
