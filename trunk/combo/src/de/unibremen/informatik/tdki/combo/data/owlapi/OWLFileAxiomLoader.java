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
package de.unibremen.informatik.tdki.combo.data.owlapi;

import de.unibremen.informatik.tdki.combo.data.MemToBulkFileWriter;
import de.unibremen.informatik.tdki.combo.syntax.axiom.*;

/**
 *
 * @author İnanç Seylan
 */
public class OWLFileAxiomLoader implements AxiomVisitor {
    
    private MemToBulkFileWriter kb;
    
    public OWLFileAxiomLoader(MemToBulkFileWriter kb) {
        this.kb = kb;
    }

    @Override
    public void visitConceptDefinition(GCI cd) {
        kb.add(cd);
    }

    @Override
    public void visitConceptAssertion(ConceptAssertion ca) {
        kb.add(ca);
    }

    @Override
    public void visitObjectRoleAssertion(ObjectRoleAssertion ra) {
        kb.add(ra);
    }

    @Override
    public void visitRoleInclusion(RoleInclusion ri) {
        kb.add(ri);
    }

    @Override
    public void visitSameIndividualAssertion(SameIndividualAssertion ia) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
