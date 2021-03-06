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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author İnanç Seylan
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    syntax.SQLTest.class,
    syntax.ExpressionEqualsTest.class,
    syntax.QueryTest.class,
    syntax.NRDatalogProgramTest.class, 
    syntax.NRDatalogToPEQConverterTest.class,
    syntax.ExpressionToStringTest.class,
    syntax.ExpressionNegationTest.class,
    syntax.ExpressionCloneTest.class,
    QueryParserTest.class
})
public class SyntaxTestSuit {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
}
