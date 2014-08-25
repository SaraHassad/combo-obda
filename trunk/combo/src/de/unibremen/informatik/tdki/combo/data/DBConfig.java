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
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author İnanç Seylan
 */
public class DBConfig {

    public enum DB {

        DB2, POSTGRES
    };

    private String url;

    private String username;

    private String password;

    private DB db;

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static DBConfig fromPropertyFile() {
        Properties appProps = new Properties();
        FileInputStream fs = null;
        DBConfig dbConfig = new DBConfig();
        // log file config
        PropertyConfigurator.configure("config/log4j.properties");
        try {
            // applicaton config
            fs = new FileInputStream("config/app.properties");
            appProps.load(fs);
            dbConfig.setDb(DB.DB2);
            String dbUrl = appProps.getProperty("dburl");
            dbConfig.setUrl(dbUrl);
            String username = appProps.getProperty("user");
            dbConfig.setUsername(username);
            String password = appProps.getProperty("password");
            dbConfig.setPassword(password);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return dbConfig;
    }

}
