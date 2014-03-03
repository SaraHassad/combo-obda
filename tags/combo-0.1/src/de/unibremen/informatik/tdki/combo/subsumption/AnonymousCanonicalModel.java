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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import de.unibremen.informatik.tdki.combo.syntax.Role;
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
public class AnonymousCanonicalModel {

    private SimpleDirectedGraph<Role, DefaultEdge> leadsTo;
    private SimpleDirectedGraph<AnonymousIndividual, RoleEdge> qualifiedLeadTo;
    private DLLiteReasoner reasoner;
    private Map<AnonymousIndividual, Set<AnonymousRoleAssertion>> generatingRoles;
    private Map<AnonymousIndividual, Set<ConceptName>> conceptAssertions;

    public AnonymousCanonicalModel(DLLiteReasoner reasoner) {
        this.reasoner = reasoner;
        generateLeadsTo();
        generateQualifiedLeadsTo();
        createGeneratingRoles();
        createConceptAssertions();
    }

    public Set<AnonymousIndividual> getAnonymousIndividuals() {
        return qualifiedLeadTo.vertexSet();
    }

    public Set<AnonymousRoleAssertion> getRoleAssertions(AnonymousIndividual reference) {
        Set<AnonymousRoleAssertion> result = Collections.<AnonymousRoleAssertion>emptySet();
        if (generatingRoles.containsKey(reference)) {
            result = generatingRoles.get(reference);
        }
        return result;
    }

    public Set<ConceptName> getConcepts(AnonymousIndividual indv) {
        Set<ConceptName> result = Collections.<ConceptName>emptySet();
        if (conceptAssertions.containsKey(indv)) {
            result = conceptAssertions.get(indv);
        }
        return result;
    }

    private void createConceptAssertions() {
        conceptAssertions = new HashMap<AnonymousIndividual, Set<ConceptName>>();
        for (AnonymousIndividual i : getAnonymousIndividuals()) {
            final Set<ConceptName> conceptSet = new HashSet<ConceptName>();
            conceptAssertions.put(i, conceptSet);
            RoleRestriction rr = new RoleRestriction(RoleRestriction.Constructor.SOME, i.getRole().copy().toggleInverse());
            for (Concept superConcept : reasoner.getSuperConcepts(rr)) {
                superConcept.accept(new ConceptVisitor() {
                    @Override
                    public void visitConceptName(ConceptName c) {
                        conceptSet.add(c);
                    }

                    @Override
                    public void visitBooleanConcept(BooleanConcept bd) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void visitRoleRestriction(RoleRestriction restriction) {
                        // avoiding this on purpose
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
        }
    }

    private void createGeneratingRoles() {
        generatingRoles = new HashMap<AnonymousIndividual, Set<AnonymousRoleAssertion>>();
        // first pass - generate non-transitive gen roles
        for (AnonymousIndividual source : getAnonymousIndividuals()) {
            Set<AnonymousRoleAssertion> roleAssertions = new HashSet<AnonymousRoleAssertion>();
            generatingRoles.put(source, roleAssertions);
            for (RoleEdge edge : qualifiedLeadTo.outgoingEdgesOf(source)) {
                AnonymousIndividual target = qualifiedLeadTo.getEdgeTarget(edge);
                for (Role r : edge.getRoleSet()) {
                    roleAssertions.add(new AnonymousRoleAssertion(r, source, target));
                }
            }
        }
        // second pass - transitively close gen roles
        boolean assignmentDone = true;
        while (assignmentDone) {
            assignmentDone = false;
            for (AnonymousIndividual source : getAnonymousIndividuals()) {
                for (RoleEdge edge : qualifiedLeadTo.outgoingEdgesOf(source)) {
                    AnonymousIndividual target = qualifiedLeadTo.getEdgeTarget(edge);
                    Set<AnonymousRoleAssertion> sourceAssertions = generatingRoles.get(source);
                    Set<AnonymousRoleAssertion> targetAssertions = generatingRoles.get(target);
                    int initialSize = sourceAssertions.size();
                    sourceAssertions.addAll(targetAssertions);
                    int endSize = sourceAssertions.size();
                    if (endSize > initialSize) {
                        assignmentDone = true;
                    }
                }
            }
        }
    }

    private void generateQualifiedLeadsTo() {
        qualifiedLeadTo = new SimpleDirectedGraph<AnonymousIndividual, RoleEdge>(RoleEdge.class);
        RoleComparator comparator = new RoleComparator();
        for (Role T : reasoner.getRoles()) {
            // populate the nodes with all possible anonymous individuals
            AnonymousIndividual cT0 = new AnonymousIndividual(T);
            AnonymousIndividual cT1 = new AnonymousIndividual(T, true);
            qualifiedLeadTo.addVertex(cT0);
            qualifiedLeadTo.addVertex(cT1);
            Role invT = T.copy().toggleInverse();
            if (leadsTo.containsVertex(invT)) {
                for (DefaultEdge edge : leadsTo.outgoingEdgesOf(invT)) {
                    Role S = leadsTo.getEdgeTarget(edge);
                    AnonymousIndividual cS0 = new AnonymousIndividual(S);
                    AnonymousIndividual cS1 = new AnonymousIndividual(S, true);
                    qualifiedLeadTo.addVertex(cS0);
                    qualifiedLeadTo.addVertex(cS1);
                    if (!isInLoop(S, T) || (comparator.compare(S, T) < 0)) {
                        addRoleLabelledEdge(cT0, cS0, S);
                        addRoleLabelledEdge(cT1, cS1, S);
                    } else {
                        addRoleLabelledEdge(cT0, cS1, S);
                        addRoleLabelledEdge(cT1, cS0, S);
                    }
                }
            }
        }
    }

    private void addRoleLabelledEdge(AnonymousIndividual source, AnonymousIndividual target, Role s) {
        RoleEdge edge;
        if (qualifiedLeadTo.containsEdge(source, target)) {
            edge = qualifiedLeadTo.getEdge(source, target);
        } else {
            edge = qualifiedLeadTo.addEdge(source, target);
        }
        edge.add(s);
        for (Role r : reasoner.getSuperRoles(s)) {
            edge.add(r);
        }
    }

    private boolean isInLoop(Role r, Role s) {
        Role invR = r.copy().toggleInverse();
        Role invS = s.copy().toggleInverse();
        if (r.equals(invS) || !leadsTo.containsEdge(invR, s) || !leadsTo.containsEdge(invS, r)) {
            return false;
        }
        Set<Role> superRoles = reasoner.getSuperRoles(invS);
        // find the common super roles of S^- and R
        superRoles.retainAll(reasoner.getSuperRoles(r));
        return !superRoles.isEmpty();
    }

    private void generateLeadsTo() {
        leadsTo = new SimpleDirectedGraph<Role, DefaultEdge>(DefaultEdge.class);
        for (Concept c : reasoner.getTBoxConcepts()) {
            c.accept(new ConceptVisitor() {
                @Override
                public void visitConceptName(ConceptName c) {
                    // intentionally do nothing
                }

                @Override
                public void visitBooleanConcept(BooleanConcept bd) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void visitRoleRestriction(RoleRestriction e) {
                    for (Concept c : reasoner.getSuperConcepts(e)) {
                        final Role r = e.getRole();
                        c.accept(new ConceptVisitor() {
                            @Override
                            public void visitConceptName(ConceptName c) {
                                // intentionally do nothing
                            }

                            @Override
                            public void visitBooleanConcept(BooleanConcept bd) {
                                throw new UnsupportedOperationException("Not supported yet.");
                            }

                            @Override
                            public void visitRoleRestriction(RoleRestriction f) {
                                Role s = f.getRole();
                                leadsTo.addVertex(r);
                                leadsTo.addVertex(s);
                                leadsTo.addEdge(r, s);
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
    }
}
