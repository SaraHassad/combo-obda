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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

/**
 *
 * @author İnanç Seylan
 */
public class DBLayout {

    private static final String PROJECTS = "Projects";
    private boolean useInsert;
    private Connection connection;
    private QueryRunner qRunner;
    private static final String CONCEPT_ASSERTIONS = "ConceptAssertions";
    private static final String ROLE_ASSERTIONS = "RoleAssertions";
    private static final String CONCEPT_INCLUSIONS = "InclusionAxioms";
    private static final String ROLE_INCLUSIONS = "RoleInclusions";
    private static final String GENERATING_ROLES = "GeneratingRoles";
    private static final String GENERATING_CONCEPTS = "GeneratingConcepts";
    private static final String SYMBOLS = "Symbols";
    private static final String QUALIFIED_EXISTENTIALS = "QualifiedExistentials";

    private static String getTable(String project, String type) {
        return project + "_" + type;
    }

    public static String getTableRBox(String project) {
        return getTable(project, ROLE_INCLUSIONS);
    }

    public static String getTableTBox(String project) {
        return getTable(project, CONCEPT_INCLUSIONS);
    }

    public static String getTableGenRoles(String project) {
        return getTable(project, GENERATING_ROLES);
    }

    public static String getTableGenConcepts(String project) {
        return getTable(project, GENERATING_CONCEPTS);
    }

    public static String getTableConceptAssertions(String project) {
        return getTable(project, CONCEPT_ASSERTIONS);
    }

    public static String getTableRoleAssertions(String project) {
        return getTable(project, ROLE_ASSERTIONS);
    }

    public static String getTableSymbols(String project) {
        return getTable(project, SYMBOLS);
    }

    public static String getTableSymbolsException(String project) {
        return getTable(project, SYMBOLS) + "_EXCEPTION";
    }

    public static String getTableQualifiedExistentials(String project) {
        return getTable(project, QUALIFIED_EXISTENTIALS);
    }

    public DBLayout(boolean useInsert, Connection connection) {
        this.useInsert = useInsert;
        this.connection = connection;
        qRunner = new QueryRunner();
    }

    /**
     *
     * @param project
     * @return true if the project already exists
     */
    public boolean createProject(String project) {
        CallableStatement callableStatement = null;
        boolean projectExists = false;
        try {
            callableStatement = connection.prepareCall("CALL combo_create_project('" + project + "',?)");
            callableStatement.registerOutParameter(1, java.sql.Types.INTEGER);
            callableStatement.executeUpdate();

            projectExists = (callableStatement.getInt(1) != 0);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            DbUtils.closeQuietly(callableStatement);
        }
        return projectExists;
    }

    private boolean projectExists(String project) {
        CallableStatement callableStatement = null;
        boolean projectExists = false;
        try {
            callableStatement = connection.prepareCall("CALL combo_project_exists('" + project + "',?)");
            callableStatement.registerOutParameter(1, java.sql.Types.INTEGER);
            callableStatement.executeUpdate();

            projectExists = (callableStatement.getInt(1) != 0);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            DbUtils.closeQuietly(callableStatement);
        }
        return projectExists;
    }

    public boolean loadProject(File bulkFile, String project) {
        if (!projectExists(project)) {
            return false;
        }
        try {
            BulkFile f = new BulkFile(bulkFile.getCanonicalFile());
            f.open(BulkFile.Open.READ);
            bulkLoadFromFile(f.getConceptAssertions().getCanonicalPath(), getTableConceptAssertions(project));
            bulkLoadFromFile(f.getRoleAssertions().getCanonicalPath(), getTableRoleAssertions(project));
            bulkLoadFromFile(f.getSymbols().getCanonicalPath(), getTableSymbols(project));
            bulkLoadFromFile(f.getGenRoles().getCanonicalPath(), getTableGenRoles(project));
            bulkLoadFromFile(f.getGenConcepts().getCanonicalPath(), getTableGenConcepts(project));
            bulkLoadFromFile(f.getQualifiedExistentials().getCanonicalPath(), getTableQualifiedExistentials(project));
            bulkLoadFromFile(f.getInclusionAxioms().getCanonicalPath(), getTableTBox(project));
            bulkLoadFromFile(f.getRoleInclusions().getCanonicalPath(), getTableRBox(project));
            f.close();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return true;
    }

    public boolean exportProject(String project, File bulkFile) {
        if (!projectExists(project)) {
            return false;
        }
        try {
            BulkFile f = new BulkFile(bulkFile.getCanonicalFile());
            f.open(BulkFile.Open.COMPRESS);
            bulkExportToFile(f.getConceptAssertions().getCanonicalPath(), getTableConceptAssertions(project));
            bulkExportToFile(f.getRoleAssertions().getCanonicalPath(), getTableRoleAssertions(project));
            bulkExportToFile(f.getSymbols().getCanonicalPath(), getTableSymbols(project));
            bulkExportToFile(f.getGenRoles().getCanonicalPath(), getTableGenRoles(project));
            bulkExportToFile(f.getGenConcepts().getCanonicalPath(), getTableGenConcepts(project));
            bulkExportToFile(f.getQualifiedExistentials().getCanonicalPath(), getTableQualifiedExistentials(project));
            bulkExportToFile(f.getInclusionAxioms().getCanonicalPath(), getTableTBox(project));
            bulkExportToFile(f.getRoleInclusions().getCanonicalPath(), getTableRBox(project));
            f.close();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return true;
    }

    public void dropProject(String project) {
        try {
            qRunner.update(connection, "CALL combo_drop_project('" + project + "')");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<String> getProjects() {
        try {
            return qRunner.query(connection, "SELECT * FROM " + PROJECTS, new AbstractListHandler<String>() {
                @Override
                protected String handleRow(ResultSet rs) throws SQLException {
                    return rs.getString(1);
                }
            });
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void completeData(String project) {
        try {
            qRunner.update(connection, "CALL combo_complete_data('" + project + "')");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void initialize() {
        // no need for dropping procedures first because they are defined with
        // CREATE OR REPLACE
        if (useInsert) {
            loadProcedureFromFile("combo_insert_insert.sql");
            loadProcedureFromFile("combo_bulk_load_file_import.sql");
        } else {
            loadProcedureFromFile("combo_insert_load.sql");
            loadProcedureFromFile("combo_bulk_load_file_load.sql");
        }
        loadProcedureFromFile("combo_drop.sql");
        loadProcedureFromFile("combo_create_project.sql");
        loadProcedureFromFile("combo_drop_project.sql");
        loadProcedureFromFile("combo_init_layout.sql");
        loadProcedureFromFile("combo_updatestats.sql");
        loadProcedureFromFile("combo_updatestats_project.sql");
        loadProcedureFromFile("combo_project_exists.sql");
        loadProcedureFromFile("combo_bulk_export_file.sql");
        loadProcedureFromFile("combo_complete_init_helpertables.sql");
        loadProcedureFromFile("combo_complete_riclosure.sql");
        loadProcedureFromFile("combo_complete_cnclosure.sql");
        loadProcedureFromFile("combo_complete_firstlevel.sql");
        loadProcedureFromFile("combo_complete_redundant.sql");
        loadProcedureFromFile("combo_complete_stage3.sql");
        loadProcedureFromFile("combo_complete_anonymous.sql");
        loadProcedureFromFile("combo_complete_data.sql");
        try {
            qRunner.update(connection, "CALL combo_init_layout");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void bulkLoadFromFile(String filename, String tablename) {
        try {
            qRunner.update(connection, "CALL combo_bulk_load_file('" + filename + "', '" + tablename + "')");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void bulkExportToFile(String filename, String tablename) {
        try {
            qRunner.update(connection, "CALL combo_bulk_export_file('" + filename + "', '" + tablename + "')");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void updateStatistics(String project) {
        try {
            qRunner.update(connection, "CALL combo_updatestats_project('" + project + "')");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void loadProcedureFromFile(String file) {
        try {
            String path = "udf/" + file;
            BufferedReader input = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)));
            try {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = input.readLine()) != null) {
                    if (!line.equals("@")) {
                        builder.append(line).append('\n');
                    }
                }
                qRunner.update(connection, builder.toString());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } finally {
                input.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: Add an integrity checking functionality. We can check data integrity once data is bulk loaded
}
