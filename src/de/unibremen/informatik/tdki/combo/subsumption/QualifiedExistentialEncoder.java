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

import java.util.ArrayList;
import java.util.List;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.axiom.GCI;
import de.unibremen.informatik.tdki.combo.syntax.axiom.RoleInclusion;
import de.unibremen.informatik.tdki.combo.syntax.concept.BooleanConcept;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptVisitor;
import de.unibremen.informatik.tdki.combo.syntax.concept.Nominal;
import de.unibremen.informatik.tdki.combo.syntax.concept.QualifiedNoRestriction;
import de.unibremen.informatik.tdki.combo.syntax.concept.RoleRestriction;

/**
 *
 * @author İnanç Seylan
 */
public class QualifiedExistentialEncoder {

    public final static String URI = "http://www.informatik.uni-bremen.de/tdki#EncodedRole";
    private int counter;
    private List<EncodingInfo> encodingInfoList;

    public List<EncodingInfo> getEncodingInfoList() {
        return encodingInfoList;
    }

    public TBox encode(TBox tbox) {
        counter = 0;
        encodingInfoList = new ArrayList<EncodingInfo>();
        final TBox result = new MemTBox();
        for (RoleInclusion ri : tbox.getRoleInclusions()) {
            result.add(ri);
        }
        for (final GCI gci : tbox.getConceptInclusions()) {
            gci.getRhs().accept(new ConceptVisitor() {
                @Override
                public void visitConceptName(ConceptName c) {
                    result.add(gci);
                }

                @Override
                public void visitBooleanConcept(BooleanConcept bd) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void visitRoleRestriction(RoleRestriction restriction) {
                    if (!restriction.getConcept().equals(ConceptName.topConcept())) {
                        Role rPrime = new Role(URI + counter++);
                        result.add(new RoleInclusion(rPrime, restriction.getRole()));
                        result.add(new GCI(gci.getLhs(), new RoleRestriction(RoleRestriction.Constructor.SOME, rPrime)));
                        Role rPrimeInv = rPrime.copy().toggleInverse();
                        result.add(new GCI(new RoleRestriction(RoleRestriction.Constructor.SOME, rPrimeInv), restriction.getConcept()));
                        encodingInfoList.add(new EncodingInfo(rPrime, restriction.getRole(), (ConceptName) restriction.getConcept()));
                    } else {
                        result.add(gci);
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
        return result;
    }
}
