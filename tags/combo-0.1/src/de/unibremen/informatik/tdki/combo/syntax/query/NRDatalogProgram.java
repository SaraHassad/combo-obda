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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 *
 * @author İnanç Seylan
 */
public class NRDatalogProgram {

    private List<ConjunctiveQuery> rules = new ArrayList<ConjunctiveQuery>();
    private DirectedGraph<String, DefaultEdge> predicateDependencyGraph;
    private String headPredicate;
    private List<String> definedPredicates = new ArrayList<String>();

    public NRDatalogProgram() {
        createPredicateDependencyGraph();
    }
    
    public List<ConjunctiveQuery> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public String getHeadPredicate() {
        return headPredicate;
    }

    public void setHeadPredicate(String p) {
        if (!predicateDependencyGraph.containsVertex(p)) {
            throw new IllegalArgumentException("There is no rule with the head " + p);
        }
        this.headPredicate = p;
    }
    
    public List<String> getDefinedPredicates() {
        return Collections.unmodifiableList(definedPredicates);
    }
    
    public boolean isDefinedPredicate(String predicate) {
        return definedPredicates.contains(predicate);
    }

    public List<ConjunctiveQuery> getRulesWithHead(String predicate) {
        List<ConjunctiveQuery> result = new ArrayList<ConjunctiveQuery>();
        for (ConjunctiveQuery rule : rules) {
            if (rule.getHead().getPredicateName().equals(predicate)) {
                result.add(rule);
            }
        }
        return result;
    }

    private void createPredicateDependencyGraph() {
        predicateDependencyGraph = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        for (ConjunctiveQuery rule : rules) {
            addRuleToPredicateDependencyGraph(rule);
        }
    }

    private void addRuleToPredicateDependencyGraph(ConjunctiveQuery rule) {
        final String headPredicate = rule.getHead().getPredicateName();
        predicateDependencyGraph.addVertex(headPredicate);
        for (Query f : rule.getQueries()) {
            f.accept(new QueryVisitor() {
                @Override
                public void visitConceptAtom(ConceptAtom ca) {
                    String bodyPredicate = ca.getConceptName();
                    predicateDependencyGraph.addVertex(bodyPredicate);
                    predicateDependencyGraph.addEdge(headPredicate, bodyPredicate);
                }

                @Override
                public void visitRoleAtom(RoleAtom ra) {
                    final String bodyPredicate = ra.getRoleName();
                    predicateDependencyGraph.addVertex(bodyPredicate);
                    predicateDependencyGraph.addEdge(headPredicate, bodyPredicate);
                }

                @Override
                public void visitUCQ(DisjunctiveQuery ucq) {
                    throw new UnsupportedOperationException("Not supported yet."); 
                }

                @Override
                public void visitCQ(ConjunctiveQuery cq) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            });
        }
    }

    private boolean areRulesCyclic() {
        return new CycleDetector<String, DefaultEdge>(predicateDependencyGraph).detectCycles();
    }

    private void sanityCheck(ConjunctiveQuery q) {
        if (areRulesCyclic()) {
            createPredicateDependencyGraph(); // regenerate the dependency graph to take it back to its previous state
            throw new IllegalArgumentException("The following rule makes the program cyclic: " + q);
        }

        for (ConjunctiveQuery rule : rules) {
            if (rule.getHead().getPredicateName().equals(q.getHead().getPredicateName()) && rule.getHead().getVariables().size() != q.getHead().getVariables().size()) {
                throw new IllegalArgumentException(q.getHead().getPredicateName() + " is used with different no of variables on two rule heads.");
            }
        }
    }

    public void addRule(ConjunctiveQuery q) {
        addRuleToPredicateDependencyGraph(q);
        sanityCheck(q);
        rules.add(q);
        definedPredicates.add(q.getHead().getPredicateName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + (this.rules != null ? this.rules.hashCode() : 0);
        hash = 73 * hash + (this.headPredicate != null ? this.headPredicate.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NRDatalogProgram other = (NRDatalogProgram) obj;
        if (this.rules != other.rules && (this.rules == null || !this.rules.equals(other.rules))) {
            return false;
        }
        if ((this.headPredicate == null) ? (other.headPredicate != null) : !this.headPredicate.equals(other.headPredicate)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (ConjunctiveQuery q : rules) {
            result.append(q).append("\n");
        }
        return result.toString();
    }
    
    
}
