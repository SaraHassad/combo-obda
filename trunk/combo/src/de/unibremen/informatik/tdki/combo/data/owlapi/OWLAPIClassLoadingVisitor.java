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

import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.concept.Concept;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import de.unibremen.informatik.tdki.combo.syntax.concept.RoleRestriction;
import org.semanticweb.owlapi.model.*;

/**
 *
 * @author İnanç Seylan
 */
public class OWLAPIClassLoadingVisitor implements OWLClassExpressionVisitor {

    private Concept concept;

    public Concept getConcept() {
        return concept;
    }

    @Override
    public void visit(OWLClass owlc) {
        if (owlc.isOWLThing()) {
            concept = ConceptName.topConcept();
        } else if (owlc.isOWLNothing()) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            concept = new ConceptName(owlc.getIRI().toString());
        }
    }

    @Override
    public void visit(OWLObjectIntersectionOf owloio) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLObjectUnionOf owlouo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLObjectComplementOf owloco) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom owlsvf) {
        OWLObjectPropertyExpression re = owlsvf.getProperty().getSimplified();
        Role r = new Role();
        if (re.isAnonymous()) { // inverse of some role name
            r.setInverse(true);
            r.setName(re.getInverseProperty().getNamedProperty().getIRI().toString());
        } else { // role name
            r.setName(re.getNamedProperty().getIRI().toString());
        }
        OWLClassExpression ce = owlsvf.getFiller();
        assert (!ce.isAnonymous());
        OWLClass c = ce.asOWLClass();
        c.accept(this);
        concept = new RoleRestriction(RoleRestriction.Constructor.SOME, r, getConcept());
    }

    @Override
    public void visit(OWLObjectAllValuesFrom owlvf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLObjectHasValue owlohv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLObjectMinCardinality owlomc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLObjectExactCardinality owloec) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLObjectMaxCardinality owlomc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLObjectHasSelf owlohs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLObjectOneOf owlooo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDataSomeValuesFrom o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDataAllValuesFrom owldvf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDataHasValue owldhv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDataMinCardinality owldmc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDataExactCardinality owldec) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDataMaxCardinality owldmc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
