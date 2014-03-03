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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author İnanç Seylan
 */
public class DBLayout {
    
    private static final String DELTA_CONCEPT_ASSERTIONS = "DeltaConceptAssertions";
    private static final String DELTA_ROLE_ASSERTIONS = "DeltaRoleAssertions";
    private static final String PROJECTS = "Projects";
    private boolean useInsert;
    private JdbcTemplate jdbcTemplate;
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
    
    private void initDeltaTables() {
        DB2Interface.safeDropTable(DELTA_CONCEPT_ASSERTIONS);
        DB2Interface.safeDropTable(DELTA_ROLE_ASSERTIONS);
        DB2Interface.getJDBCTemplate().execute("CREATE TABLE " + DELTA_CONCEPT_ASSERTIONS + " (concept integer, individual integer)");
        DB2Interface.getJDBCTemplate().execute("CREATE TABLE " + DELTA_ROLE_ASSERTIONS + " (role integer, lhs integer, rhs integer)");
    }
    
    private void initRBox(String project) {
        String table = getTableRBox(project);
        DB2Interface.getJDBCTemplate().execute("CREATE TABLE " + table + " (lhs integer NOT NULL, rhs integer NOT NULL)");
        DB2Interface.getJDBCTemplate().execute("CREATE INDEX " + project + "_ri_lhs_rhs ON " + table + " (lhs, rhs)");
    }
    
    private void initTBox(String project) {
        String table = getTableTBox(project);
        DB2Interface.getJDBCTemplate().execute("CREATE TABLE " + table + " (lhs integer NOT NULL, rhs integer NOT NULL)");
        DB2Interface.getJDBCTemplate().execute("CREATE INDEX " + project + "_ia_lhs_rhs ON " + table + " (lhs, rhs)");
    }
    
    private void initGeneratingRoles(String project) {
        String table = getTableGenRoles(project);
        DB2Interface.getJDBCTemplate().execute("CREATE TABLE " + table + " (anonindv integer NOT NULL, role integer NOT NULL, lhs integer NOT NULL, rhs integer NOT NULL)");
        DB2Interface.getJDBCTemplate().execute("CREATE INDEX " + project + "_genroles_anonindv ON " + table + " (anonindv)");
    }
    
    private void initGeneratingConcepts(String project) {
        String table = getTableGenConcepts(project);
        DB2Interface.getJDBCTemplate().execute("CREATE TABLE " + table + " (concept integer NOT NULL, individual integer NOT NULL)");
        DB2Interface.getJDBCTemplate().execute("CREATE INDEX " + project + "_genconcepts_individual ON " + table + " (individual)");
    }
    
    private void initConceptAssertions(String project) {
        String table = getTableConceptAssertions(project);
        DB2Interface.getJDBCTemplate().execute("CREATE TABLE " + table + " (concept integer NOT NULL, individual integer NOT NULL)");
        DB2Interface.getJDBCTemplate().execute("CREATE INDEX " + project + "_oca_concept_individual ON " + table + " (concept, individual)");
    }
    
    private void initRoleAssertions(String project) {
        String table = getTableRoleAssertions(project);
        DB2Interface.getJDBCTemplate().execute("CREATE TABLE " + table + " (role integer NOT NULL, lhs integer NOT NULL, rhs integer NOT NULL)");
        DB2Interface.getJDBCTemplate().execute("CREATE INDEX " + project + "_ora_role_lhs_rhs ON " + table + " (role, lhs, rhs)");
        DB2Interface.getJDBCTemplate().execute("CREATE INDEX " + project + "_ora_role_rhs_lhs ON " + table + " (role, rhs, lhs)");
    }
    
    private void initSymbolsTable(String project) {
        String table = getTableSymbols(project);
        DB2Interface.getJDBCTemplate().execute("CREATE TABLE " + table + " (name character varying(150) NOT NULL PRIMARY KEY, id integer NOT NULL)");
        DB2Interface.getJDBCTemplate().execute("CREATE INDEX " + project + "_symbols_id_name ON " + table + " (id, name)");
    }
    
    private void initQualifiedExistentialsTable(String project) {
        String table = getTableQualifiedExistentials(project);
        DB2Interface.getJDBCTemplate().execute("CREATE TABLE " + table + " (newrole integer NOT NULL PRIMARY KEY, originalrole integer NOT NULL, conceptname integer NOT NULL)");
    }
    
    public DBLayout(boolean useInsert) {
        this.useInsert = useInsert;
        jdbcTemplate = DB2Interface.getJDBCTemplate();
    }
    
    public void createProject(List<String> projects) {
        List<String> existing = getProjects();
        for (String name : projects) {
            if (existing.contains(name)) {
                System.out.println(name + " already exists; not creating.");
            } else {
                createProject(name);
            }
        }
    }
    
    private boolean projectExists(String project) {
        return getProjects().contains(project);
    }
    
    public void loadProject(File bulkFile, String project) {
        if (!projectExists(project)) {
            System.err.println("Project " + project + " does not exist.");
            return;
        }
        try {
            BulkFile f = new BulkFile(bulkFile.getCanonicalFile());
            f.open(BulkFile.Open.READ);
            DB2Interface.bulkLoad(f.getConceptAssertions().getCanonicalPath(), getTableConceptAssertions(project));
            DB2Interface.bulkLoad(f.getRoleAssertions().getCanonicalPath(), getTableRoleAssertions(project));
            DB2Interface.bulkLoad(f.getSymbols().getCanonicalPath(), getTableSymbols(project), getTableSymbols(project) + "_EXCEPTION");
            DB2Interface.bulkLoad(f.getGenRoles().getCanonicalPath(), getTableGenRoles(project));
            DB2Interface.bulkLoad(f.getGenConcepts().getCanonicalPath(), getTableGenConcepts(project));
            DB2Interface.bulkLoad(f.getQualifiedExistentials().getCanonicalPath(), getTableQualifiedExistentials(project));
            DB2Interface.bulkLoad(f.getInclusionAxioms().getCanonicalPath(), getTableTBox(project));
            DB2Interface.bulkLoad(f.getRoleInclusions().getCanonicalPath(), getTableRBox(project));
            f.close();
        } catch (InterruptedException ex) {
            Logger.getLogger(DBLayout.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DBLayout.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void exportProject(String project, File bulkFile) {
        if (!projectExists(project)) {
            System.err.println("Project " + project + " does not exist.");
            return;
        }
        try {
            BulkFile f = new BulkFile(bulkFile.getCanonicalFile());
            f.open(BulkFile.Open.COMPRESS);
            DB2Interface.bulkExport(f.getConceptAssertions().getCanonicalPath(), getTableConceptAssertions(project));
            DB2Interface.bulkExport(f.getRoleAssertions().getCanonicalPath(), getTableRoleAssertions(project));
            DB2Interface.bulkExport(f.getSymbols().getCanonicalPath(), getTableSymbols(project));
            DB2Interface.bulkExport(f.getGenRoles().getCanonicalPath(), getTableGenRoles(project));
            DB2Interface.bulkExport(f.getGenConcepts().getCanonicalPath(), getTableGenConcepts(project));
            DB2Interface.bulkExport(f.getQualifiedExistentials().getCanonicalPath(), getTableQualifiedExistentials(project));
            DB2Interface.bulkExport(f.getInclusionAxioms().getCanonicalPath(), getTableTBox(project));
            DB2Interface.bulkExport(f.getRoleInclusions().getCanonicalPath(), getTableRBox(project));
            f.close();
        } catch (InterruptedException ex) {
            Logger.getLogger(DBLayout.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DBLayout.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void createProject(String project) {
        jdbcTemplate.update("INSERT INTO Projects VALUES ('" + project + "')");
        initRBox(project);
        initTBox(project);
        initGeneratingRoles(project);
        initGeneratingConcepts(project);
        initConceptAssertions(project);
        initRoleAssertions(project);
        initSymbolsTable(project);
        initQualifiedExistentialsTable(project);
    }
    
    public void dropProject(List<String> projects) {
        for (String name : projects) {
            dropProject(name);
        }
    }
    
    private void dropProject(String project) {
        jdbcTemplate.update("DELETE FROM Projects WHERE name='" + project + "'");
        
        DB2Interface.safeDropTable(getTableConceptAssertions(project));
        DB2Interface.safeDropTable(getTableGenConcepts(project));
        DB2Interface.safeDropTable(getTableGenRoles(project));
        DB2Interface.safeDropTable(getTableQualifiedExistentials(project));
        DB2Interface.safeDropTable(getTableRBox(project));
        DB2Interface.safeDropTable(getTableRoleAssertions(project));
        DB2Interface.safeDropTable(getTableSymbols(project));
        DB2Interface.safeDropTable(getTableSymbolsException(project));
        DB2Interface.safeDropTable(getTableTBox(project));
    }
    
    private List<String> getProjects() {
        return jdbcTemplate.queryForList("SELECT * FROM " + PROJECTS, String.class);
    }
    
    public void listProjects() {
        List<String> projects = getProjects();
        for (String p : projects) {
            System.out.println(p);
        }
    }
    
    public void completeData(List<String> projects) {
        for (String p : projects) {
            jdbcTemplate.execute("CALL complete_data('" + p + "')");
        }
    }
    
    public void initialize() {
        try {
            dropProject(getProjects());
        } catch (BadSqlGrammarException e) {
            // should come here when no Projects table exists
        }
        DB2Interface.safeDropTable(PROJECTS);
        jdbcTemplate.execute("CREATE TABLE " + PROJECTS + " (name character varying(20) NOT NULL PRIMARY KEY)");
        
        initDeltaTables();
        
        DB2Interface.safeDropProcedure("insert_concept_assertions");
        DB2Interface.safeDropProcedure("insert_role_assertions");
        DB2Interface.safeDropProcedure("complete_data");
        if (useInsert) {
            loadProcedureFromFile("insert_concept_assertions_insert.sql");
            loadProcedureFromFile("insert_role_assertions_insert.sql");
        } else {
            loadProcedureFromFile("insert_concept_assertions_load.sql");
            loadProcedureFromFile("insert_role_assertions_load.sql");
        }
        loadProcedureFromFile("complete_data.sql");
    }
    
    public void updateStatistics(List<String> projects) {
        for (String p : projects) {
            DB2Interface.updateStatistics(getTableConceptAssertions(p));
            DB2Interface.updateStatistics(getTableRoleAssertions(p));
            DB2Interface.updateStatistics(getTableSymbols(p));
            DB2Interface.updateStatistics(getTableTBox(p));
            DB2Interface.updateStatistics(getTableRBox(p));
            DB2Interface.updateStatistics(getTableGenRoles(p));
            DB2Interface.updateStatistics(getTableGenConcepts(p));
            DB2Interface.updateStatistics(getTableQualifiedExistentials(p));
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
                jdbcTemplate.execute(builder.toString());
            } finally {
                input.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
