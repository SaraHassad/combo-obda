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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author İnanç Seylan
 */
public class DB2Interface {

    private static JdbcTemplate instance = null;
    private static String username; // necessary for calling sysproc.admin_cmd with RUNSTATS

    private DB2Interface() {
    }

    public static JdbcTemplate getJDBCTemplate() {
        if (instance == null) {
            Properties appProps = new Properties();
            FileInputStream fs;
            try {
                // log file config
                PropertyConfigurator.configure("config/log4j.properties");
                // applicaton config
                fs = new FileInputStream("config/app.properties");
                appProps.load(fs);
                String dbUrl = appProps.getProperty("dburl");
                String username = appProps.getProperty("user");
                String password = appProps.getProperty("password");
                instance = new JdbcTemplate(dbUrl, username, password);
                DB2Interface.username = username;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DB2Interface.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DB2Interface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return instance;
    }

    private static void bulkLoad(String command, String filename, String tablename, String exceptionTable) {
        boolean useExceptionTable = (!exceptionTable.equals(""));
        StringBuilder buffer = new StringBuilder();
        buffer.append("CALL sysproc.admin_cmd('");
        buffer.append(command);
        buffer.append(" FROM ");
        buffer.append(filename);
        buffer.append(" OF DEL MODIFIED BY NOCHARDEL COLDELx09 REPLACE INTO ");
        buffer.append(tablename);
        if (useExceptionTable) {
            safeDropTable(exceptionTable);
            getJDBCTemplate().execute("CREATE TABLE " + exceptionTable + " LIKE " + tablename);
            buffer.append(" FOR EXCEPTION ");
            buffer.append(exceptionTable);
        }
        buffer.append("')");
        getJDBCTemplate().execute(buffer.toString());
        if (useExceptionTable) {
            int noOfExceptTuples = getJDBCTemplate().queryForInt("SELECT COUNT(*) FROM " + exceptionTable);
            if (noOfExceptTuples > 0) {
                throw new InconsistentDataException(tablename + " is in an inconsistent state.");
            }
        }
    }

    public static void bulkLoad(String filename, String tablename) {
        bulkLoad(filename, tablename, "");
    }
    
    public static void bulkLoad(String filename, String tablename, String exceptionTable) {
        bulkLoad("LOAD", filename, tablename, exceptionTable);
    }
    
    public static void bulkImport(String filename, String tablename) {
        bulkLoad("IMPORT", filename, tablename, "");
    }

    public static void bulkExport(String filename, String tablename) {
        getJDBCTemplate().execute("CALL sysproc.admin_cmd('EXPORT TO " + filename + " OF DEL MODIFIED BY NOCHARDEL COLDELx09 SELECT * FROM " + tablename + "')");
    }

    public static void updateStatistics(String tablename) {
        getJDBCTemplate().execute("CALL sysproc.admin_cmd('RUNSTATS ON TABLE " + username + "." + tablename + " WITH DISTRIBUTION AND DETAILED INDEXES ALL')");
    }

    public static void safeDropTable(String tablename) {
        // remember that when a table is dropped also its indices are dropped
        try {
            getJDBCTemplate().execute("DROP TABLE " + tablename);
        } catch (DBObjectDoesNotExistException e) {
            // table does not exist
        }
    }

    public static void safeDropView(String viewname) {
//        try {
            getJDBCTemplate().execute("DROP VIEW " + viewname);
//        } catch (BadSqlGrammarException e) {
//            // object does not exist
//        }
    }
    
    public static void safeDropProcedure(String name) {
//        try {
            getJDBCTemplate().execute("DROP PROCEDURE " + name);
//        } catch (BadSqlGrammarException e) {
//            // object does not exist
//        }
    }

    public static void loadReplaceIntoTable(String cursor, String table) {
        getJDBCTemplate().execute("CALL sysproc.admin_cmd('LOAD FROM (" + cursor + ") OF CURSOR REPLACE INTO " + table + "')");
    }

    public static void loadInsertIntoTable(String cursor, String table) {
        getJDBCTemplate().execute("CALL sysproc.admin_cmd('LOAD FROM (" + cursor + ") OF CURSOR INSERT INTO " + table + "')");
    }
}
