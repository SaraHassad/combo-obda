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
import java.sql.SQLException;
import org.apache.commons.dbutils.DbUtils;

/**
 *
 * @author İnanç Seylan
 */
public class DBConnPool {

    private Connection connection;
    
    private final DBConfig config;

    public DBConnPool(DBConfig dbConfig) {
        this.config = dbConfig;
    }
    
    public Connection getConnection() {
        if (connection != null)
            return connection;
        
        switch (config.getDb()) {
            case DB2:
                assert DbUtils.loadDriver("com.ibm.db2.jcc.DB2Driver");
                break;
            case POSTGRES:
                break;
        }
        try {
            connection = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return connection;
    }
    
    public void releaseConnection(Connection c) {
        if (c != null && c == connection) {
            DbUtils.closeQuietly(connection);
        }
    }

}
