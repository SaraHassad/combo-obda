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
import de.unibremen.informatik.tdki.combo.syntax.axiom.*;
import de.unibremen.informatik.tdki.combo.syntax.concept.Concept;
import de.unibremen.informatik.tdki.combo.syntax.concept.RoleRestriction;
import org.semanticweb.owlapi.model.*;

/**
 *
 * @author İnanç Seylan
 */
public class OWLAPIAxiomLoadingVisitor implements OWLAxiomVisitor {

    private AxiomVisitor axiomVisitor;

    public OWLAPIAxiomLoadingVisitor(AxiomVisitor av) {
        this.axiomVisitor = av;
    }
    
    @Override
    public void visit(OWLDeclarationAxiom owlda) {
        // intentionally ignoring these
        // do not print message because this can confuse the user: as if we are not taking into account symbols
    }

    @Override
    public void visit(OWLSubClassOfAxiom owlsc) {
        OWLAPIClassLoadingVisitor v = new OWLAPIClassLoadingVisitor();
        owlsc.getSubClass().accept(v);
        Concept lhs = v.getConcept();
        owlsc.getSuperClass().accept(v);
        Concept rhs = v.getConcept();
        new GCI(lhs, rhs).accept(axiomVisitor);
    }

    @Override
    public void visit(OWLNegativeObjectPropertyAssertionAxiom owlnp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLAsymmetricObjectPropertyAxiom owlp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLReflexiveObjectPropertyAxiom owlrp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDisjointClassesAxiom owldca) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDataPropertyDomainAxiom owldpd) {
        // we do not support data properties
        System.out.println("Ignoring " + owldpd);
    }

    @Override
    public void visit(OWLObjectPropertyDomainAxiom owlpd) {
        OWLObjectPropertyExpression e = owlpd.getProperty().getSimplified();
        if (e.isAnonymous()) { // inverse of a property
            throw new UnsupportedOperationException("Not supported yet.");
        } else { // named property
            OWLAPIClassLoadingVisitor v = new OWLAPIClassLoadingVisitor();
            owlpd.getDomain().accept(v);
            GCI gci = new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role(e.getNamedProperty().getIRI().toString())), v.getConcept());
            gci.accept(axiomVisitor);;
        }
    }

    @Override
    public void visit(OWLEquivalentObjectPropertiesAxiom owlp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLNegativeDataPropertyAssertionAxiom owlndp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDifferentIndividualsAxiom owldia) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDisjointDataPropertiesAxiom owldp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDisjointObjectPropertiesAxiom owldp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLObjectPropertyRangeAxiom owlpr) {
        OWLObjectPropertyExpression e = owlpr.getProperty().getSimplified();
        if (e.isAnonymous()) { // inverse of a property
            throw new UnsupportedOperationException("Not supported yet.");
        } else { // named property
            OWLAPIClassLoadingVisitor v = new OWLAPIClassLoadingVisitor();
            owlpr.getRange().accept(v);
            GCI gci = new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, new Role(e.getNamedProperty().getIRI().toString(), true)), v.getConcept());
            gci.accept(axiomVisitor);;
        }
    }

    @Override
    public void visit(OWLObjectPropertyAssertionAxiom owlp) {
        String subject = owlp.getSubject().asOWLNamedIndividual().getIRI().toString();
        String object = owlp.getObject().asOWLNamedIndividual().getIRI().toString();
        String role = owlp.getProperty().getNamedProperty().getIRI().toString();
        ObjectRoleAssertion ra = new ObjectRoleAssertion(new Role(role), subject, object);
        ra.accept(axiomVisitor);;
    }

    @Override
    public void visit(OWLFunctionalObjectPropertyAxiom owlfp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom owlsp) {
        OWLObjectPropertyExpression lhs = owlsp.getSubProperty().getSimplified();
        OWLObjectPropertyExpression rhs = owlsp.getSuperProperty().getSimplified();
        if (lhs.isAnonymous() || rhs.isAnonymous()) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            Role l = new Role(lhs.getNamedProperty().getIRI().toString());
            Role r = new Role(rhs.getNamedProperty().getIRI().toString());
            new RoleInclusion(l, r).accept(axiomVisitor);;
        }
    }

    @Override
    public void visit(OWLDisjointUnionAxiom owldua) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLSymmetricObjectPropertyAxiom owlsp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDataPropertyRangeAxiom owldpr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLFunctionalDataPropertyAxiom owlfdp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLEquivalentDataPropertiesAxiom owldp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLClassAssertionAxiom owlcaa) {
        OWLAPIClassLoadingVisitor v = new OWLAPIClassLoadingVisitor();
        owlcaa.getClassExpression().accept(v);
        Concept c = v.getConcept();

        String indvName = owlcaa.getIndividual().asOWLNamedIndividual().getIRI().toString();
        ConceptAssertion ca = new ConceptAssertion(c, indvName);
        ca.accept(axiomVisitor);;
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom owleca) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDataPropertyAssertionAxiom owldp) {
        System.out.println("Ignoring " + owldp);
    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom owltp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLIrreflexiveObjectPropertyAxiom owlp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLSubDataPropertyOfAxiom owlsdp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLInverseFunctionalObjectPropertyAxiom owlfp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLSameIndividualAxiom owlsia) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLSubPropertyChainOfAxiom owlspc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLInverseObjectPropertiesAxiom owlp) {
        OWLObjectPropertyExpression lhs = owlp.getFirstProperty().getSimplified();
        OWLObjectPropertyExpression rhs = owlp.getSecondProperty().getSimplified();
        if (lhs.isAnonymous() || rhs.isAnonymous()) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            Role l = new Role(lhs.getNamedProperty().getIRI().toString());
            Role r = new Role(rhs.getNamedProperty().getIRI().toString(), true);
            new RoleInclusion(l, r).accept(axiomVisitor);;
            new RoleInclusion(r, l).accept(axiomVisitor);;
        }
    }

    @Override
    public void visit(OWLHasKeyAxiom owlhka) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLDatatypeDefinitionAxiom owldda) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(SWRLRule swrlr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLAnnotationAssertionAxiom owlaaa) {
        // intentionally ignoring theses
    }

    @Override
    public void visit(OWLSubAnnotationPropertyOfAxiom owlsp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLAnnotationPropertyDomainAxiom owlpd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OWLAnnotationPropertyRangeAxiom owlpr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
