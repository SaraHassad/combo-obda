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

import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;

/**
 *
 * @author İnanç Seylan
 */
public class EncodingInfo {

    private Role newRole;
    private Role originalRole;
    private ConceptName conceptName;
    
    public EncodingInfo(Role newRole, Role originalRole, ConceptName cn) {
        this.newRole = newRole;
        this.originalRole = originalRole;
        this.conceptName = cn;
    }

    public Role getNewRole() {
        return newRole;
    }

    public void setNewRole(Role newRole) {
        this.newRole = newRole;
    }

    public Role getOriginalRole() {
        return originalRole;
    }

    public void setOriginalRole(Role originalRole) {
        this.originalRole = originalRole;
    }

    public ConceptName getConceptName() {
        return conceptName;
    }

    public void setConceptName(ConceptName conceptName) {
        this.conceptName = conceptName;
    }
}
