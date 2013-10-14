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
package de.unibremen.informatik.tdki.combo.ui;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import de.unibremen.informatik.tdki.combo.data.DB2Interface;
import de.unibremen.informatik.tdki.combo.data.DBLayout;
import de.unibremen.informatik.tdki.combo.rewriting.FilterRewriterDB2;
import de.unibremen.informatik.tdki.combo.syntax.query.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 *
 * @author İnanç Seylan
 */
@Parameters(commandDescription = "Completes the ABox assertions w.r.t. the TBox")
class CommandComplete {

    @Parameter(description = "A list of project names")
    private List<String> projects;

    public List<String> getProjects() {
        return projects;
    }
}

/**
 *
 * @author İnanç Seylan
 */
@Parameters(commandDescription = "Creates a project")
class CommandCreate {

    @Parameter(description = "A list of project names")
    private List<String> projects;

    public List<String> getProjects() {
        return projects;
    }
}

/**
 *
 * @author İnanç Seylan
 */
@Parameters(commandDescription = "Deletes a project")
class CommandDelete {

    @Parameter(description = "A list of project names")
    private List<String> projects;

    public List<String> getProjects() {
        return projects;
    }
}

/**
 *
 * @author İnanç Seylan
 */
@Parameters(commandDescription = "Drops all filters")
class CommandDropFilters {
}

/**
 *
 * @author İnanç Seylan
 */
@Parameters(commandDescription = "Exports the given project into the given combo bulk file")
class CommandExport {

    @Parameter(names = "--project", description = "Name of the project")
    private String project;
    @Parameter(names = "--file", description = "Path of the file")
    private String file;

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}

/**
 *
 * @author İnanç Seylan
 */
@Parameters(commandDescription = "Prints the usage of the program")
class CommandHelp {
}

/**
 *
 * @author İnanç Seylan
 */
@Parameters(commandDescription = "Initializes the database. Note that all previous projects are deleted.")
class CommandInit {
}

/**
 *
 * @author İnanç Seylan
 */
@Parameters(commandDescription = "Lists all the projects")
class CommandList {
}

/**
 *
 * @author İnanç Seylan
 */
@Parameters(commandDescription = "Loads the given combo bulk load file into the given project")
class CommandLoad {

    @Parameter(names = "--file", description = "Path of the file")
    private String file;
    @Parameter(names = "--project", description = "Name of the project")
    private String project;
    //@Parameter(names = "--exception", description = "Name of the project to log exceptions, e.g., violated integrity constraints")
    //private String exception = "";

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
//    public String getException() {
//        return exception;
//    }
//
//    public void setException(String exception) {
//        this.exception = exception;
//    }
}

/**
 *
 * @author İnanç Seylan
 */
@Parameters(commandDescription = "Rewrites the Datalog program in the given file to SQL")
class CommandRewriteDatalog {

    @Parameter(names = "--uri", description = "URI to prepend for the concept/role names used in the query")
    private String uri;
    @Parameter(names = "--file", description = "Path of the file")
    private String filename;
    @Parameter(names = "--project", description = "The project to query")
    private String project;
    @Parameter(names = "--count", arity = 1, description = "With this option, a count query asking for the number of individuals is generated")
    private boolean count = true;
    @Parameter(names = "--names", arity = 1, description = "With this option, a query asking for the names of individuals (instead of their ids) is generated")
    private boolean names = false;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public boolean isCount() {
        return count;
    }

    public void setCount(boolean count) {
        this.count = count;
    }

    public boolean isNames() {
        return names;
    }

    public void setNames(boolean names) {
        this.names = names;
    }
}

/**
 *
 * @author İnanç Seylan
 */
@Parameters(commandDescription = "Rewrites the CQ in the given file to SQL by generating a filter")
class CommandRewriteFilter {

    @Parameter(names = "--uri", description = "URI to prepend for the concept/role names used in the query")
    private String uri;
    @Parameter(names = "--file", description = "Path of the file")
    private String filename;
    @Parameter(names = "--avo", arity = 1, description = "Optimizes answer variable matching by adding extra conditions to the WHERE clause")
    private boolean avo = true;
    @Parameter(names = "--count", arity = 1, description = "With this option, a count query asking for the number of individuals is generated")
    private boolean count = true;
    @Parameter(names = "--names", arity = 1, description = "With this option, a query asking for the names of individuals (instead of their ids) is generated")
    private boolean names = false;
    @Parameter(names = "--project", description = "The project to query")
    private String project;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isCount() {
        return count;
    }

    public void setCount(boolean count) {
        this.count = count;
    }

    public boolean isNames() {
        return names;
    }

    public void setNames(boolean names) {
        this.names = names;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isAvo() {
        return avo;
    }

    public void setAvo(boolean answerVar) {
        this.avo = answerVar;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}

/**
 *
 * @author İnanç Seylan
 */
@Parameters(commandDescription = "Updates the table statistics of the given project")
class CommandRunStats {

    @Parameter(description = "A list of project names")
    private List<String> projects;

    public List<String> getProjects() {
        return projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }
}

/**
 *
 * @author İnanç Seylan
 */
public class CombinedAppInterface {

    private static JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        jdbcTemplate = DB2Interface.getJDBCTemplate();
        DBLayout layout = new DBLayout(false);

        JCommander jc = new JCommander();
        CommandComplete complete = new CommandComplete();
        jc.addCommand("complete", complete);
        CommandCreate create = new CommandCreate();
        jc.addCommand("create", create);
        CommandDelete delete = new CommandDelete();
        jc.addCommand("delete", delete);
        CommandDropFilters dropFilter = new CommandDropFilters();
        jc.addCommand("dropf", dropFilter);
        CommandExport export = new CommandExport();
        jc.addCommand("export", export);
        CommandHelp help = new CommandHelp();
        jc.addCommand("help", help);
        CommandInit init = new CommandInit();
        jc.addCommand("init", init);
        CommandList list = new CommandList();
        jc.addCommand("list", list);
        CommandLoad load = new CommandLoad();
        jc.addCommand("load", load);
        CommandRewriteDatalog rwDatalog = new CommandRewriteDatalog();
        jc.addCommand("rwd", rwDatalog);
        CommandRewriteFilter rwFilter = new CommandRewriteFilter();
        jc.addCommand("rwf", rwFilter);
        CommandRunStats runStats = new CommandRunStats();
        jc.addCommand("runstats", runStats);

        try {
            jc.parse(args);
            String command = jc.getParsedCommand();
            if (command.equals("complete")) {
                layout.completeData(complete.getProjects());
            } else if (command.equals("create")) {
                layout.createProject(create.getProjects());
            } else if (command.equals("delete")) {
                layout.dropProject(delete.getProjects());
            } else if (command.equals("dropf")) {
                dropFilter();
            } else if (command.equals("export")) {
                layout.exportProject(export.getProject(), new File(export.getFile()));
            } else if (command.equals("help")) {
                jc.usage();
            } else if (command.equals("init")) {
                layout.initialize();
            } else if (command.equals("list")) {
                layout.listProjects();
            } else if (command.equals("load")) {
                layout.loadProject(new File(load.getFile()), load.getProject());
            } else if (command.equals("rwd")) {
                generateDatalog(rwDatalog);
            } else if (command.equals("rwf")) {
                generateFilter(rwFilter);
            } else if (command.equals("runstats")) {
                layout.updateStatistics(runStats.getProjects());
            }
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            System.err.println("Try the 'help' command for usage.");
        }
    }

    private static void generateFilter(CommandRewriteFilter rw) {
        try {
            NRDatalogProgram program = new QueryParser(rw.getUri()).parseDatalogFile(new File(rw.getFilename()));
            assert (program.getRules().size() == 1);
            ConjunctiveQuery q = program.getRules().get(0);
            FilterRewriterDB2 qCompiler = new FilterRewriterDB2(rw.getProject());
            String sql = qCompiler.filter(q, rw.isNames(), rw.isCount(), rw.isAvo()).toString();
            System.out.println(sql);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CombinedAppInterface.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CombinedAppInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void generateDatalog(CommandRewriteDatalog rw) {
        try {
            NRDatalogProgram program = new QueryParser(rw.getUri()).parseDatalogFile(new File(rw.getFilename()));
            program.setHeadPredicate("Q");
            NRDatalogToPEQConverter converter = new NRDatalogToPEQConverter();
            Query q = converter.convert(program);
            FilterRewriterDB2 qCompiler = new FilterRewriterDB2(rw.getProject());
            String sql = qCompiler.rewrite(q, rw.isNames(), rw.isCount(), true).toString();
            System.out.println(sql);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CombinedAppInterface.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CombinedAppInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void dropFilter() {
        dropFilter("IS_REAL_TUPLE");
        dropFilter("FAKE_FILTER");
    }

    private static void dropFilter(String namePattern) {
        jdbcTemplate.query("SELECT funcname, parm_count FROM Syscat.Functions WHERE funcname LIKE '" + namePattern + "%'", new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                StringBuilder query = new StringBuilder();
                query.append("DROP FUNCTION ").append(rs.getString(1)).append("(");
                int noOfParams = rs.getInt(2);
                for (int i = 0; i < noOfParams; i++) {
                    query.append("INTEGER");
                    if (i != noOfParams - 1) {
                        query.append(",");
                    }
                }
                query.append(")");
                jdbcTemplate.execute(query.toString());
            }
        });
    }
}
