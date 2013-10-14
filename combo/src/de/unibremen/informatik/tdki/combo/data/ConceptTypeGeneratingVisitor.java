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
package de.unibremen.informatik.tdki.combo.data;

import de.unibremen.informatik.tdki.combo.syntax.concept.*;

/**
 *
 * @author İnanç Seylan
 */
public class ConceptTypeGeneratingVisitor implements ConceptVisitor {
    
    private String name;
    private int type;

    @Override
    public void visitConceptName(ConceptName c) {
        type = ExpressionConstants.CONCEPT_NAME;
        name = c.getName();
    }

    @Override
    public void visitBooleanConcept(BooleanConcept bd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visitRoleRestriction(RoleRestriction restriction) {
        if (restriction.getConstructor().equals(RoleRestriction.Constructor.ALL)) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        if (!restriction.getConcept().equals(ConceptName.topConcept())) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        name = restriction.getRole().getName();
        if (restriction.getRole().isInverse()) {
            type = ExpressionConstants.INV_ROLE;
        } else {
            type = ExpressionConstants.ROLE_NAME;
        }
    }

    @Override
    public void visitQualifiedNoRestriction(QualifiedNoRestriction restriction) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visitNominal(Nominal nominal) {
        throw new UnsupportedOperationException("Not supported yet.");
        // type = ExpressionConstants.INDIVIDUAL_NAME;
        // name = nominal.getName();
    }

    /**
     * @return the conceptName
     */
    public String getName() {
        return name;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }
}
