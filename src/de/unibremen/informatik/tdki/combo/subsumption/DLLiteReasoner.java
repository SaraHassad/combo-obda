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
package de.unibremen.informatik.tdki.combo.subsumption;

import java.util.*;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.axiom.GCI;
import de.unibremen.informatik.tdki.combo.syntax.axiom.RoleInclusion;
import de.unibremen.informatik.tdki.combo.syntax.concept.BooleanConcept;
import de.unibremen.informatik.tdki.combo.syntax.concept.Concept;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptVisitor;
import de.unibremen.informatik.tdki.combo.syntax.concept.Nominal;
import de.unibremen.informatik.tdki.combo.syntax.concept.QualifiedNoRestriction;
import de.unibremen.informatik.tdki.combo.syntax.concept.RoleRestriction;

/**
 *
 * @author İnanç Seylan
 */
public class DLLiteReasoner {

    private TBox tbox;
    private SimpleDirectedGraph<Concept, DefaultEdge> conceptInclusionGraph;
    private SimpleDirectedGraph<Role, DefaultEdge> roleInclusionGraph;

    public DLLiteReasoner(TBox tbox) {
        this.tbox = tbox;
    }

    public void classify() {
        roleInclusionGraph = new SimpleDirectedGraph<Role, DefaultEdge>(DefaultEdge.class);
        for (RoleInclusion ri : tbox.getRoleInclusions()) {
            // simple digraph does not allow loops
            if (ri.getLhs().equals(ri.getRhs())) {
                continue;
            }
            roleInclusionGraph.addVertex(ri.getLhs());
            roleInclusionGraph.addVertex(ri.getRhs());
            roleInclusionGraph.addEdge(ri.getLhs(), ri.getRhs());
            // add the inversed role inclusions, this is critical for inferences
            Role lhsInv = ri.getLhs().copy().toggleInverse();
            Role rhsInv = ri.getRhs().copy().toggleInverse();
            roleInclusionGraph.addVertex(lhsInv);
            roleInclusionGraph.addVertex(rhsInv);
            roleInclusionGraph.addEdge(lhsInv, rhsInv);
        }
        TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(roleInclusionGraph);

        conceptInclusionGraph = new SimpleDirectedGraph<Concept, DefaultEdge>(DefaultEdge.class);
        // add the concept inclusions coming from the TBox
        for (GCI gci : tbox.getConceptInclusions()) {
            // simple digraph does not allow loops
            if (gci.getLhs().equals(gci.getRhs())) {
                continue;
            }
            conceptInclusionGraph.addVertex(gci.getLhs());
            conceptInclusionGraph.addVertex(gci.getRhs());
            conceptInclusionGraph.addEdge(gci.getLhs(), gci.getRhs());
            // add any roles appearing in role restrictions to roleInclusionGraph
            addRoleRestrictionRoleToRIGraph(gci.getLhs());
            addRoleRestrictionRoleToRIGraph(gci.getRhs());
        }
        // add the existential restriction inclusions induced by the role hierarchy
        for (DefaultEdge edge : roleInclusionGraph.edgeSet()) {
            Role r = roleInclusionGraph.getEdgeSource(edge);
            Role s = roleInclusionGraph.getEdgeTarget(edge);
            RoleRestriction rR = new RoleRestriction(RoleRestriction.Constructor.SOME, r);
            RoleRestriction rS = new RoleRestriction(RoleRestriction.Constructor.SOME, s);
            conceptInclusionGraph.addVertex(rR);
            conceptInclusionGraph.addVertex(rS);
            conceptInclusionGraph.addEdge(rR, rS);
        }
        TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(conceptInclusionGraph);
    }
    
    private void addRoleRestrictionRoleToRIGraph(Concept c) {
        c.accept(new ConceptVisitor() {

            @Override
            public void visitConceptName(ConceptName c) {
                // do nothing on purpose
            }

            @Override
            public void visitBooleanConcept(BooleanConcept bd) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void visitRoleRestriction(RoleRestriction restriction) {
                roleInclusionGraph.addVertex(restriction.getRole());
            }

            @Override
            public void visitQualifiedNoRestriction(QualifiedNoRestriction restriction) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void visitNominal(Nominal nominal) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }
        });
    }

    public TBox getTBox() {
        return tbox;
    }

    /**
     * 
     * @param r
     * @return all superroles of the given role except the role itself
     */
    public Set<Role> getSuperRoles(Role r) {
        Set<Role> result = new HashSet<Role>();
        if (roleInclusionGraph.containsVertex(r)) {
            Set<DefaultEdge> edges = roleInclusionGraph.outgoingEdgesOf(r);
            for (DefaultEdge e : edges) {
                Role superRole = roleInclusionGraph.getEdgeTarget(e);
                result.add(superRole);
            }
        }
        return result;
    }

    /**
     * 
     * @param c
     * @return all superconcepts of c except c itself. 
     * All the returned concepts appear in the TBox. 
     */
    public Set<Concept> getSuperConcepts(Concept c) {
        Set<Concept> result = new HashSet<Concept>();
        if (conceptInclusionGraph.containsVertex(c)) {
            Set<DefaultEdge> edges = conceptInclusionGraph.outgoingEdgesOf(c);
            for (DefaultEdge e : edges) {
                Concept superCon = conceptInclusionGraph.getEdgeTarget(e);
                result.add(superCon);
            }
        } 
        return result;
    }

    public Set<Role> getRoles() {
        return roleInclusionGraph.vertexSet();
    }

    public Set<Concept> getTBoxConcepts() {
        return conceptInclusionGraph.vertexSet();
    }
}
