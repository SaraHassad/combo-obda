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
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 *
 * @author İnanç Seylan
 */
public class TestUtils {

    public static Multiset<Tuple<String>> getTuples(String query) {
        Multiset<Tuple<String>> result = HashMultiset.create();
        SqlRowSet rs = DB2Interface.getJDBCTemplate().queryForRowSet(query);
        int columns = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            Tuple<String> t = new Tuple<String>();
            for (int i = 1; i <= columns; i++) {
                t.add(rs.getString(i));
            }
            result.add(t);
        }
        return result;
    }
}
