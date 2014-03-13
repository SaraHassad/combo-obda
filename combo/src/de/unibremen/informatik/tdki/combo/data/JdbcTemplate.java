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
package de.unibremen.informatik.tdki.combo.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

/**
 *
 * @author İnanç Seylan
 */
public class JdbcTemplate {

    private Connection connection;

    private QueryRunner qRunner;

    public JdbcTemplate(String url, String user, String password) {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            connection = DriverManager.getConnection(url, user, password);
            qRunner = new QueryRunner();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void execute(String query) {
        try {
            qRunner.update(connection, query);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == -204) {
                throw new DBObjectDoesNotExistException(ex);
            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    public void update(String query) {
        execute(query);
    }

    public int queryForInt(String query) {
        try {
            return qRunner.query(connection, query, new ScalarHandler<Integer>(1));
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List queryForList(String query, final Class elementType) {
        try {
            return qRunner.query(connection, query, new AbstractListHandler<Object>() {

                @Override
                protected Object handleRow(ResultSet rs) throws SQLException {
                    return rs.getObject(1, elementType);
                }

            });
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void query(String query, ResultSetHandler handler) {
        try {
            qRunner.query(connection, query, handler);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
