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

import de.unibremen.informatik.tdki.combo.data.DBLayout;
import de.unibremen.informatik.tdki.combo.data.RowCallbackHandler;
import de.unibremen.informatik.tdki.combo.syntax.query.*;
import de.unibremen.informatik.tdki.combo.syntax.sql.*;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.dbutils.QueryRunner;

/**
 *
 * @author İnanç Seylan
 */
public class FilterRewriterDB2 {

    private ConjunctiveQuery query;
    private String compileOutputDir;
    private long libNo;
    private List<String> variables;
    private Map<String, Integer> symbolIdMap;
    private String project;
    private Connection connection;
    private QueryRunner qRunner;

    public FilterRewriterDB2(String project, Connection connection) {
        this.connection = connection;
        qRunner = new QueryRunner();
        // TODO: remove this at some point
        //initParameters();
        this.project = project;
    }

// TODO: the following code seems to be unnecessary and commented it out. If no problems, remove it
//    private void initParameters() {
//        FileInputStream fs = null;
//        try {
//            Properties appProps = new Properties();
//            fs = new FileInputStream("config/app.properties");
//            appProps.load(fs);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(FakeFilterRewriter.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(FakeFilterRewriter.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                fs.close();
//            } catch (IOException ex) {
//                Logger.getLogger(FakeFilterRewriter.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
    /**
     * gets the ids of the symbols used in the query from the database
     */
    private void initSymboldIDs(Query query) {
        final Set<String> predicates = new HashSet<String>();
        final SFWQuery select = new SFWQuery();
        select.addSelect("name");
        select.addSelect("id");
        select.addFrom(new BaseTable(DBLayout.getTableSymbols(project), 2));
        final WhereComposite where = new WhereComposite();
        select.setWhere(where);

        query.accept(new QueryVisitor() {

            @Override
            public void visitConceptAtom(ConceptAtom ca) {
                if (!predicates.contains(ca.getConceptName())) {
                    where.addDisjunct(new WhereCondition(WhereCondition.Operator.EQUAL, "name", "\'" + ca.getConceptName() + "\'"));
                    predicates.add(ca.getConceptName());
                }
            }

            @Override
            public void visitRoleAtom(RoleAtom ra) {
                if (!predicates.contains(ra.getRoleName())) {
                    where.addDisjunct(new WhereCondition(WhereCondition.Operator.EQUAL, "name", "\'" + ra.getRoleName() + "\'"));
                    predicates.add(ra.getRoleName());
                }
            }

            @Override
            public void visitUCQ(DisjunctiveQuery ucq) {
                for (Query q : ucq.getQueries()) {
                    q.accept(this);
                }
            }

            @Override
            public void visitCQ(ConjunctiveQuery cq) {
                for (Query q : cq.getQueries()) {
                    q.accept(this);
                }
            }
        });
        SFWQuery selectDistinct = SFWQuery.createDistinct(select);

        symbolIdMap = new HashMap<String, Integer>();
        try {
            qRunner.query(connection, selectDistinct.toString(), new RowCallbackHandler() {

                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    symbolIdMap.put(rs.getString(1), rs.getInt(2));
                }
            });
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void appendComparisonForAnswerVariables(ConjunctiveQuery q, WhereComposite where) {
        for (String v : q.getHead().getVariables()) {
            Query atom = q.getMentioningQueries(v).iterator().next();
            where.addConjunct(new WhereCondition(WhereCondition.Operator.LESS_THAN, new SimpleCQAtomNamingScheme().getColumnName(v, atom, q), "0"));
        }
    }

    private Table getTable(Query q) {
        initSymboldIDs(q);
        SQLGeneratingVisitor visitor = new SQLGeneratingVisitor(new PredicateNameReplacingAdapter(symbolIdMap, project), new SimpleCQAtomNamingScheme());
        q.accept(visitor);
        return visitor.getTable();
    }

    public Table rewrite(Query q, boolean askForNames, boolean count, boolean distinct) {
        Table table = getTable(q);
        table = decorate(table, askForNames, distinct, count);
        return table;
    }

    public SFWQuery noFilter(ConjunctiveQuery q, boolean askForNames, boolean count) {
        SFWQuery sql = (SFWQuery) getTable(q); // must return a SFWQuery!
        if (sql.getWhere() == null) {
            sql.setWhere(new WhereComposite());
        }
        WhereComposite where = (WhereComposite) sql.getWhere(); // type casting here should work without any problem
        appendComparisonForAnswerVariables(q, where);
        sql = (SFWQuery) decorate(sql, askForNames, true, count); // must return a SFWQuery!
        return sql;
    }

    public SFWQuery filter(ConjunctiveQuery q, boolean askForNames, boolean count, boolean bitops) {
        SFWQuery sql = (SFWQuery) getTable(q); // must return a SFWQuery!
        try {
            this.query = q;
            createLibraryIDAndCompileDirectory();
            variables = new ArrayList<String>(q.getTerms()); // this position in the list will determine the order of the variables
            generateGlobalDataStructuresForFilter();
            generateFilterCode(variables.size());
            compileCode();
            createFilterInDB2(variables.size());
            if (sql.getWhere() == null) {
                sql.setWhere(new WhereComposite());
            }
            WhereComposite where = (WhereComposite) sql.getWhere(); // type casting here should work without any problem
            if (bitops) {
                appendComparisonForAnswerVariables(q, where);
            }
            appendFilterCondition(q, where);
            sql = (SFWQuery) decorate(sql, askForNames, true, count); // must return a SFWQuery!
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return sql;
    }

    private void createLibraryIDAndCompileDirectory() {
        try {
            libNo = System.currentTimeMillis();
            File outputDir = new File("udf/tmp/filter" + libNo);
            compileOutputDir = outputDir.getCanonicalPath() + "/";
            outputDir.mkdirs();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private int getVarIndex(String v) {
        return variables.indexOf(v);
    }

    private void createFilterInDB2(int varcount) throws SQLException {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE FUNCTION ");
        builder.append("is_real_tuple").append(libNo).append("(");
        for (int i = 0; i < varcount; i++) {
            builder.append("inParm").append(i).append(" INTEGER");
            if (i < varcount - 1) {
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
        builder.append("EXTERNAL NAME \'").append(compileOutputDir).append("libDB2Filter").append(".dylib!is_real_tuple").append(libNo).append("\'");
        qRunner.update(connection, builder.toString());
    }

    private void appendFilterCondition(ConjunctiveQuery q, WhereComposite where) {
        // for calling the filter in the SQL
        StringBuilder filterCall = new StringBuilder();
        filterCall.append("is_real_tuple").append(libNo).append("(");
        for (int i = 0; i < variables.size(); i++) {
            String v = variables.get(i);
            Query atom = q.getMentioningQueries(v).iterator().next();
            filterCall.append(new SimpleCQAtomNamingScheme().getColumnName(v, atom, q));
            if (i < variables.size() - 1) {
                filterCall.append(",");
            }
        }
        filterCall.append(")");
        where.addConjunct(new WhereCondition(WhereCondition.Operator.GREATER_THAN, filterCall.toString(), "0"));
    }

    private void generateFilterCode(int varcount) throws IOException {
        final PrintWriter headerFile = new PrintWriter(new FileWriter(compileOutputDir + "db2interface.cpp"));
        try {
            headerFile.println("#include <sqludf.h>");
            headerFile.println("#include \"filter.h\"");
            headerFile.println("extern \"C\" SQL_API_RC SQL_API_FN is_real_tuple" + libNo + "(");
            for (int i = 0; i < varcount; i++) {
                headerFile.println("SQLUDF_INTEGER *inParm" + i + ",");
            }
            headerFile.println("SQLUDF_INTEGER *outParm,");
            for (int i = 0; i < varcount; i++) {
                headerFile.println("SQLUDF_INTEGER *inParmNullInd" + i + ",");
            }
            headerFile.println("SQLUDF_NULLIND *outParmNullInd,");
            headerFile.println("SQLUDF_TRAIL_ARGS) {");
            headerFile.println("if (");
            for (int i = 0; i < varcount; i++) {
                headerFile.print("(*inParmNullInd" + i + " != -1)");
                if (i != varcount - 1) {
                    headerFile.println(" && ");
                }
            }
            headerFile.println(") {");
            headerFile.println("int values[" + varcount + "];");
            for (int i = 0; i < varcount; i++) {
                headerFile.println("values[" + i + "] = *inParm" + i + ";");
            }
            headerFile.println("*outParm = filter(values, " + varcount + ");");
            headerFile.println("*outParmNullInd = 0;");
            headerFile.println("} else {");
            headerFile.println("*outParmNullInd = -1;");
            headerFile.println("}");
            headerFile.println("return (0);");
            headerFile.println("}");
        } finally {
            headerFile.close();
        }
    }

    private void generateGlobalDataStructuresForFilter() throws IOException, SQLException {
        final PrintWriter headerFile = new PrintWriter(new FileWriter(compileOutputDir + "globalvars.h"));
        try {
            headerFile.println("#ifndef GLOBALVARS_H");
            headerFile.println("#define GLOBALVARS_H");
            headerFile.println("#include <boost/assign/list_of.hpp>");
            headerFile.println("using namespace boost::assign;");

            // the query
            headerFile.print("const vector<RoleAtom> roleAtoms");
            final List<String> atoms = new ArrayList<String>();
            for (Query atom : query.getQueries()) {
                atom.accept(new QueryVisitor() {

                    @Override
                    public void visitConceptAtom(ConceptAtom ca) {
                        // do nothing
                    }

                    @Override
                    public void visitRoleAtom(RoleAtom ra) {
                        int roleId = symbolIdMap.get(ra.getRoleName());
                        int lhsIndex = getVarIndex(ra.getLeftTerm());
                        int rhsIndex = getVarIndex(ra.getRightTerm());
                        atoms.add("(RoleAtom(" + roleId + "," + lhsIndex + "," + rhsIndex + "))");
                    }

                    @Override
                    public void visitUCQ(DisjunctiveQuery ucq) {
                        throw new UnsupportedOperationException("Not supported yet."); // should not come here
                    }

                    @Override
                    public void visitCQ(ConjunctiveQuery cq) {
                        throw new UnsupportedOperationException("Not supported yet."); // should not come here
                    }
                });
            }
            safeWriteListOf(atoms, headerFile);

            // the \leadsto_R relation
            atoms.clear();
            headerFile.print("const set<RoleAtom> genRoles");
            qRunner.query(connection, "SELECT DISTINCT role, lhs, rhs FROM " + DBLayout.getTableGenRoles(project), new RowCallbackHandler() {

                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    atoms.add("(RoleAtom(" + rs.getInt(1) + "," + rs.getInt(2) + "," + rs.getInt(3) + "))");
                }
            });
            safeWriteListOf(atoms, headerFile);

            // the free variables
            atoms.clear();
            headerFile.print("const vector<int> freeVars");
            for (String v : query.getHead().getVariables()) {
                atoms.add("(" + getVarIndex(v) + ")");
            }
            safeWriteListOf(atoms, headerFile);

            headerFile.println("#endif");
        } finally {
            headerFile.close();
        }
    }

    private void safeWriteListOf(final List<String> atoms, final PrintWriter headerFile) {
        if (atoms.size() > 0) {
            headerFile.println(" = list_of");
            for (String atom : atoms) {
                headerFile.println(atom);
            }
        }
        headerFile.println(";");
    }

    private void compileCode() throws InterruptedException, IOException {
        // TODO: The following might be a problem in non Unix platforms. 
        // But already demanding GNU compilers and make is a problem.
        Process p = Runtime.getRuntime().exec("cp udf/Makefile udf/filter.h udf/filter.cpp " + compileOutputDir);
        printOutput(p);
        p.waitFor();

        // The following only works in Java 7
        // Files.copy(new File("udf/Makefile").toPath(), new File(compileOutputDir, "Makefile").toPath());
        // Files.copy(new File("udf/filter.h").toPath(), new File(compileOutputDir, "filter.h").toPath());
        // Files.copy(new File("udf/filter.cpp").toPath(), new File(compileOutputDir, "filter.cpp").toPath());
        p = Runtime.getRuntime().exec("make -C " + compileOutputDir + " filter");
        printOutput(p);
        p.waitFor();
    }

    private void printOutput(Process p) throws IOException {
        atomicPrintOutput(new BufferedReader(new InputStreamReader(p.getInputStream())), false);
        atomicPrintOutput(new BufferedReader(new InputStreamReader(p.getErrorStream())), true);
    }

    private void atomicPrintOutput(BufferedReader reader, boolean checkForMessage) throws IOException {
        StringBuilder message = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                message.append(line).append("\n");
            }
        } finally {
            reader.close();
        }
        if (checkForMessage && message.length() != 0) {
            message.delete(message.length() - 1, message.length());
            throw new RewritingException(message.toString());
        }
    }

    private Table decorate(Table table, boolean askForNames, boolean distinct, boolean count) {
        if (askForNames) {
            table = RewritingDecorator.withIndividualNames(table, project);
        }
        if (distinct) {
            table = RewritingDecorator.withDistinct(table);
        }
        if (count) {
            table = RewritingDecorator.withCount(table);
        }
        return table;
    }
}
