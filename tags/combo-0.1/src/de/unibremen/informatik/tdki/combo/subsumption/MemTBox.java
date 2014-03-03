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

import java.util.HashSet;
import java.util.Set;
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
public class MemTBox implements TBox {

    private Set<RoleInclusion> roleInclusions = new HashSet<RoleInclusion>();
    private Set<GCI> conceptInclusions = new HashSet<GCI>();

    @Override
    public void add(GCI gci) {
        // accept only concept names or unqualified role restrictions on the left
        gci.getLhs().accept(new ConceptVisitor() {
            @Override
            public void visitConceptName(ConceptName c) {
                // do nothing
            }

            @Override
            public void visitBooleanConcept(BooleanConcept bd) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void visitRoleRestriction(RoleRestriction restriction) {
                if (!restriction.getConcept().equals(ConceptName.topConcept())) {
                    throw new UnsupportedOperationException("Not supported yet.");
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
        // accept only concept names, unqualified concept restrictions, or qualified restrictions with concept names on the right 
        gci.getRhs().accept(new ConceptVisitor() {

            @Override
            public void visitConceptName(ConceptName c) {
                // do nothing
            }

            @Override
            public void visitBooleanConcept(BooleanConcept bd) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public void visitRoleRestriction(RoleRestriction restriction) {
                Concept c = restriction.getConcept();
                if (!c.equals(ConceptName.topConcept()) && !(c instanceof ConceptName)) {
                    throw new UnsupportedOperationException("Not supported yet.");
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
        conceptInclusions.add(gci);
    }

    @Override
    public void add(RoleInclusion ri) {
        roleInclusions.add(ri);
    }

    @Override
    public Set<GCI> getConceptInclusions() {
        return conceptInclusions;
    }

    @Override
    public Set<RoleInclusion> getRoleInclusions() {
        return roleInclusions;
    }
}
