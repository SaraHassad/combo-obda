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

import de.unibremen.informatik.tdki.combo.syntax.query.Query;
import de.unibremen.informatik.tdki.combo.syntax.query.RoleAtom;
import de.unibremen.informatik.tdki.combo.syntax.query.QueryVisitor;
import de.unibremen.informatik.tdki.combo.syntax.query.ConceptAtom;
import de.unibremen.informatik.tdki.combo.syntax.query.DisjunctiveQuery;
import de.unibremen.informatik.tdki.combo.syntax.query.ConjunctiveQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;
import de.unibremen.informatik.tdki.combo.syntax.Role;

/**
 *
 * @author İnanç Seylan
 */
public class TreeWitnessFinder {

    private Map<String, List<Role>> varConstantMap;
    private DirectedGraph<String, RoleLabelledEdge> graph;
    boolean newAssignment;

    public Map<String, List<Role>> findTreeWitness(ConjunctiveQuery q, Role role, String startVar) {
        varConstantMap = new HashMap<String, List<Role>>();
        graph = new DirectedMultigraph<String, RoleLabelledEdge>(new ClassBasedEdgeFactory<String, RoleLabelledEdge>(RoleLabelledEdge.class));

        initGraph(q, graph);
        List<Role> list = new ArrayList<Role>();
        list.add(NodeLabellingConstants.EMPTYWORD);
        varConstantMap.put(startVar, list);

        do {
            newAssignment = false;

            for (String v : graph.vertexSet()) {
                if (varConstantMap.containsKey(v)) {
                    List<Role> vConstants = varConstantMap.get(v);
                    // when the variable is mapped to the empty role
                    if (vConstants.size() == 1) {
                        for (RoleLabelledEdge e : graph.outgoingEdgesOf(v)) {
                            String target = graph.getEdgeTarget(e);
                            if (!target.equals(v) && e.getLabel().equals(role)) {
                                list = new ArrayList<Role>(vConstants);
                                list.add(role);
                                if (!putConstantsSafely(target, list)) {
                                    // basically we are here when there exists no tree witness
                                    return null;
                                }
                            }
                        }
                        for (RoleLabelledEdge e : graph.incomingEdgesOf(v)) {
                            String source = graph.getEdgeSource(e);
                            if (!source.equals(v) && e.getLabel().equals(role.copy().toggleInverse())) {
                                list = new ArrayList<Role>(vConstants);
                                list.add(role);
                                if (!putConstantsSafely(source, list)) {
                                    return null;
                                }
                            }
                        }
                    } // when the variable is mapped to a non-empty role
                    else {
                        for (RoleLabelledEdge e : graph.outgoingEdgesOf(v)) {
                            String target = graph.getEdgeTarget(e);
                            if (!target.equals(v)) {
                                list = new ArrayList<Role>(vConstants);
                                if (list.get(list.size() - 1).equals(e.getLabel().copy().toggleInverse())) {
                                    list.remove(list.size() - 1);
                                } else {
                                    list.add(e.getLabel());
                                }
                                if (!putConstantsSafely(target, list)) {
                                    return null;
                                }
                            }
                        }
                        for (RoleLabelledEdge e : graph.incomingEdgesOf(v)) {
                            String source = graph.getEdgeSource(e);
                            if (!source.equals(v)) {
                                list = new ArrayList<Role>(vConstants);
                                if (list.get(list.size() - 1).equals(e.getLabel())) {
                                    list.remove(list.size() - 1);
                                } else {
                                    list.add(e.getLabel().copy().toggleInverse());
                                }
                                if (!putConstantsSafely(source, list)) {
                                    return null;
                                }
                            }
                        }
                    }
                }
            }
        } while (newAssignment == true);
        return varConstantMap;
    }

    private boolean putConstantsSafely(String node, List<Role> constants) {
        if (!varConstantMap.containsKey(node)) {
            varConstantMap.put(node, constants);
            newAssignment = true;
        } else {
            // if we are trying to map the same variable to two different constants
            if (!constants.equals(varConstantMap.get(node))) {
                return false;
            }
        }
        return true;
    }

    private void initGraph(ConjunctiveQuery q, final DirectedGraph<String, RoleLabelledEdge> graph) {
        for (Query atom : q.getQueries()) {
            atom.accept(new QueryVisitor() {
                @Override
                public void visitConceptAtom(ConceptAtom ca) {
                    // do nothing
                }

                @Override
                public void visitRoleAtom(RoleAtom ra) {
                    graph.addVertex(ra.getLeftTerm());
                    graph.addVertex(ra.getRightTerm());
                    graph.addEdge(ra.getLeftTerm(), ra.getRightTerm(), new RoleLabelledEdge(ra.getLeftTerm(), ra.getRightTerm(), new Role(ra.getRoleName())));
                }

                @Override
                public void visitUCQ(DisjunctiveQuery ucq) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void visitCQ(ConjunctiveQuery cq) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        }
    }
}
