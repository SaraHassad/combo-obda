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
package de.unibremen.informatik.tdki.combo.syntax.query;

import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author İnanç Seylan
 */
public class QueryParser {

    private String prefix = "";
    private String IMPLIES_SYMBOL = "<-";

    public QueryParser() {
    }

    public QueryParser(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public NRDatalogProgram parseDatalogFile(File file) throws FileNotFoundException, IOException {
        final NRDatalogProgram program = new NRDatalogProgram();
        BufferedReader input = new BufferedReader(new FileReader(file));
        try {
            String line;
            while ((line = input.readLine()) != null) {
                ConjunctiveQuery q = parse(line);
                program.addRule(q);
            }
        } finally {
            input.close();
        }
        prependPrefixToNonDefinedPredicates(program);
        return program;
    }

    public ConjunctiveQuery parse(String str) {
        ConjunctiveQuery result = new ConjunctiveQuery();
        int pos = str.indexOf(IMPLIES_SYMBOL);
        String headString = str.substring(0, pos).trim();
        String bodyString = str.substring(pos + 2).trim();
        parseBody(bodyString, result);
        parseHead(headString, result);
        return result;
    }

    private void parseBody(String body, ConjunctiveQuery q) {
        ArrayList<String> atoms = splitAtoms(body);
        for (int i = 0; i < atoms.size(); i++) {
            Head head = parseAtom(atoms.get(i));
            if (head.getVariables().size() == 1) {
                ConceptAtom ca = new ConceptAtom(head.getPredicateName(), head.getVariables().get(0));
                q.addQuery(ca);
            } else if (head.getVariables().size() == 2) {
                RoleAtom ra = new RoleAtom(head.getPredicateName(), head.getVariables().get(0), head.getVariables().get(1));
                q.addQuery(ra);
            } else {
                throw new UnsupportedOperationException("An atom in the body must have either 1 or 2 variables.");
            }
        }
    }

    private Head parseAtom(String s) {
        int pos1 = s.lastIndexOf("(");
        int pos2 = s.lastIndexOf(")");
        String predicate = s.substring(0, pos1).trim();
        String args = s.substring(pos1 + 1, pos2).trim();
        ArrayList<String> v = splitArguments(args);
        return new Head(predicate, v.toArray(new String[v.size()]));
    }

    private void parseHead(String atom, ConjunctiveQuery q) {
        Head head = parseAtom(atom);
        q.setHead(head);
    }

    private ArrayList<String> splitArguments(String s) {
        ArrayList<String> res = new ArrayList<String>();

        char[] arr = s.toCharArray();

        int c = 0;
        int start = 0;

        for (int i = 0; i < arr.length; i++) {
            boolean close = false;

            if (arr[i] == '(') {
                c++;
            } else if (arr[i] == ')') {
                c--;
                close = true;
            }

            if (c == 0 && arr[i] == ',') {
                res.add(s.substring(start, i).trim());
                i = i + 1;

                while (i < arr.length && (arr[i] == ' ')) {
                    i++;
                }
                start = i;

            }
            if (c == 0 && close) {
                res.add(s.substring(start, i + 1).trim());
                i = i + 1;

                while (i < arr.length && (arr[i] == ' ' || arr[i] == ',')) {
                    i++;
                }
                start = i;
            }

        }

        if (start <= arr.length - 1) {
            res.add(s.substring(start));
        }

        return res;
    }

    private ArrayList<String> splitAtoms(String s) {
        ArrayList<String> res = new ArrayList<String>();

        char[] arr = s.toCharArray();

        int c = 0;
        int start = 0;

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == '(') {
                c++;
            } else if (arr[i] == ')') {
                c--;
            }

            if (c == 0 && arr[i] == ',') {
                res.add(s.substring(start, i).trim());
                i = i + 1;

                while (i < arr.length && (arr[i] == ' ')) {
                    i++;
                }
                start = i;

            }

        }

        if (start <= arr.length - 1) {
            res.add(s.substring(start));
        }

        return res;
    }

    private void prependPrefixToNonDefinedPredicates(final NRDatalogProgram program) {
        for (ConjunctiveQuery q : program.getRules()) {
            for (Query atom : q.getQueries()) {
                atom.accept(new QueryVisitor() {

                    @Override
                    public void visitConceptAtom(ConceptAtom ca) {
                        if (!program.isDefinedPredicate(ca.getConceptName())) {
                            ca.setConceptName(getPrefix() + ca.getConceptName());
                        }
                    }

                    @Override
                    public void visitRoleAtom(RoleAtom ra) {
                        if (!program.isDefinedPredicate(ra.getRoleName())) {
                            ra.setRoleName(getPrefix() + ra.getRoleName());
                        }
                    }

                    @Override
                    public void visitCQ(ConjunctiveQuery cq) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void visitUCQ(DisjunctiveQuery ucq) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                });
            }
        }
    }
}
