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

import org.junit.Assert;
import org.junit.Test;
import de.unibremen.informatik.tdki.combo.syntax.query.ConceptAtom;
import de.unibremen.informatik.tdki.combo.syntax.query.ConjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.query.Head;
import de.unibremen.informatik.tdki.combo.syntax.query.NRDatalogProgram;

/**
 *
 * @author İnanç Seylan
 */
public class NRDatalogProgramTest {
    
    @Test
    public void test() {
        NRDatalogProgram p = new NRDatalogProgram();
        p.addRule(new ConjunctiveQuery(new Head("Q", "x"), new ConceptAtom("A", "x")));
        boolean exceptionThrown = false;
        try {
            p.addRule(new ConjunctiveQuery(new Head("A", "x"), new ConceptAtom("Q", "x")));
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
            System.out.println(e.getMessage());
        }
        Assert.assertTrue(exceptionThrown);
    }
}