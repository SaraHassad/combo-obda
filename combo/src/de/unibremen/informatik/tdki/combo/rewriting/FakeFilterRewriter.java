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
package de.unibremen.informatik.tdki.combo.rewriting;

import de.unibremen.informatik.tdki.combo.syntax.sql.SFWQuery;
import java.io.*;
import java.util.Properties;
import de.unibremen.informatik.tdki.combo.syntax.query.ConjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.sql.WhereComposite;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbutils.QueryRunner;

/**
 *
 * @author İnanç Seylan
 */
public class FakeFilterRewriter {

    private ConjunctiveQuery query;
    private SFWQuery sql;
    private WhereComposite where;
    private final static String outputDir = "/tmp/";
    private final static String sourceDir = "udf/";
    private boolean bitopEnabled;
    private long libNo;
    private String db2IncludeDir;
    private Connection connection;

    public boolean isBitopEnabled() {
        return bitopEnabled;
    }

    public void setBitopEnabled(boolean bitopEnabled) {
        this.bitopEnabled = bitopEnabled;
    }

    public FakeFilterRewriter(Connection connection) {
        this.connection = connection;
        bitopEnabled = true;
        setDB2IncludeDir();
    }

    public FakeFilterRewriter(boolean bitopEnabled) {
        this.connection = connection;
        this.bitopEnabled = bitopEnabled;
        setDB2IncludeDir();
    }

    private void setDB2IncludeDir() {
        FileInputStream fs = null;
        try {
            Properties appProps = new Properties();
            fs = new FileInputStream("config/app.properties");
            appProps.load(fs);
            db2IncludeDir = appProps.getProperty("db2headerdir");
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
    }

    public SFWQuery compile(ConjunctiveQuery q) {
        try {
            this.query = q;
            where = (WhereComposite) sql.getWhere();
            libNo = System.currentTimeMillis();
            compileCode();
            createFilterInDB2();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return sql;
    }

    private void createFilterInDB2() {
        for (int j = 1; j < 6; j++) {
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE FUNCTION ");

            builder.append("fake_filter").append(libNo).append("(");
            for (int i = 0; i < j; i++) {
                builder.append("inParm").append(i).append(" INTEGER");
                if (i < j - 1) {
                    builder.append(", ");
                }
            }
            builder.append(")\n");
            builder.append("RETURNS INTEGER\n");
            builder.append("LANGUAGE C\n");
            builder.append("PARAMETER STYLE SQL\n");
            builder.append("NO SQL\n");
            builder.append("NOT FENCED\n");
            builder.append("THREADSAFE\n");
            builder.append("DETERMINISTIC\n");
            builder.append("RETURNS NULL ON NULL INPUT\n");
            builder.append("NO EXTERNAL ACTION\n");
            builder.append("EXTERNAL NAME \'").append(outputDir).append("libDB2FakeFilter").append(libNo).append(".dylib!fake_filter").append(libNo).append("\'");
            QueryRunner qRunner = new QueryRunner();
            try {
                qRunner.update(connection, builder.toString());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void compileCode() throws InterruptedException, IOException {
        Process p = Runtime.getRuntime().exec("cc -c " + sourceDir + "db2ffinterface.c -I" + db2IncludeDir + " -fpic -D_REENTRANT -m64 -o " + outputDir + "db2ffinterface.o");
        printOutput(p);
        p.waitFor();
        p = Runtime.getRuntime().exec("cc -shared -lpthread -o " + outputDir + "libDB2FakeFilter" + libNo + ".dylib " + outputDir + "db2ffinterface.o");
        printOutput(p);
        p.waitFor();
    }

    private void printOutput(Process p) throws IOException {
        String line;
        BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ((line = bri.readLine()) != null) {
            System.out.println(line);
        }
        bri.close();
        while ((line = bre.readLine()) != null) {
            System.out.println(line);
        }
        bre.close();
    }
}
