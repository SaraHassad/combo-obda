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

import de.unibremen.informatik.tdki.combo.subsumption.*;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ConceptAssertion;
import de.unibremen.informatik.tdki.combo.syntax.axiom.GCI;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ObjectRoleAssertion;
import de.unibremen.informatik.tdki.combo.syntax.axiom.RoleInclusion;
import de.unibremen.informatik.tdki.combo.syntax.concept.Concept;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author İnanç Seylan
 */
public class MemToBulkFileWriter {

    private TBox tbox = new MemTBox();
    private EncodingManagerDB2 manager;
    private BulkFile bulkFile;

    public MemToBulkFileWriter(File file) throws IOException, InterruptedException {
        bulkFile = new BulkFile(file);
        bulkFile.open(BulkFile.Open.WRITE);
        manager = new EncodingManagerDB2();
    }

    public void add(GCI gci) {
        tbox.add(gci);
    }

    public void add(RoleInclusion ri) {
        tbox.add(ri);
    }

    public void add(ConceptAssertion ca) {
        manager.mapConcept(ca.getConcept());
        manager.mapIndividual(ca.getIndividual());

        int cid = manager.getConceptID(ca.getConcept());
        int iid = manager.getIndvID(ca.getIndividual());
        bulkFile.writeConceptAssertion(cid, iid);
    }

    public void add(ObjectRoleAssertion ra) {
        manager.mapRole(ra.getRole());
        manager.mapIndividual(ra.getLhs());
        manager.mapIndividual(ra.getRhs());

        int rid = manager.getRoleID(ra.getRole());
        int lhs = manager.getIndvID(ra.getLhs());
        int rhs = manager.getIndvID(ra.getRhs());
        bulkFile.writeRoleAssertion(rid, lhs, rhs);
    }

    public void close() throws IOException, InterruptedException {
        QualifiedExistentialEncoder encoder = new QualifiedExistentialEncoder();
        TBox newTBox = encoder.encode(tbox);
        DLLiteReasoner reasoner = new DLLiteReasoner(newTBox);
        reasoner.classify();
        generateIdsForTBoxSymbols(reasoner);
        commitSymbolsToFile();
        addTBox(reasoner);
        addGenerating(reasoner);
        addQualifiedExistentials(encoder);
        bulkFile.close();
    }

    private void commitSymbolsToFile() {
        Map<String, Integer> map = manager.getNameToIdMap();
        for (String key : map.keySet()) {
            bulkFile.writeSymbol(key, map.get(key));
        }
    }

    private void addQualifiedExistentials(QualifiedExistentialEncoder encoder) {
        for (EncodingInfo i : encoder.getEncodingInfoList()) {
            int newroleid = manager.getRoleID(i.getNewRole());
            int originalroleid = manager.getRoleID(i.getOriginalRole());
            int cnid = manager.getConceptID(i.getConceptName());
            bulkFile.writeQualifiedExistential(newroleid, originalroleid, cnid);
        }

    }

    private void addTBox(DLLiteReasoner reasoner) {
        for (Concept c : reasoner.getTBoxConcepts()) {
            for (Concept d : reasoner.getSuperConcepts(c)) {
                bulkFile.writeInclusionAxiom(manager.getConceptID(c), manager.getConceptID(d));
            }
        }
        for (Role r : reasoner.getRoles()) {
            for (Role s : reasoner.getSuperRoles(r)) {
                bulkFile.writeRoleInclusion(manager.getRoleID(r), manager.getRoleID(s));
            }
        }
    }

    private void generateIdsForTBoxSymbols(DLLiteReasoner reasoner) {
        for (Concept c : reasoner.getTBoxConcepts()) {
            manager.mapConcept(c);
        }
        for (Role r : reasoner.getRoles()) {
            manager.mapRole(r);
        }
    }

    private void addGenerating(DLLiteReasoner reasoner) {
        AnonymousCanonicalModel model = new AnonymousCanonicalModel(reasoner);
        for (AnonymousIndividual indv : model.getAnonymousIndividuals()) {
            int anonindv = manager.getAnonIndvID(indv);
            for (ConceptName c : model.getConcepts(indv)) {
                int cid = manager.getConceptID(c);
                bulkFile.writeGenConcept(cid, anonindv);
            }
            for (AnonymousRoleAssertion ra : model.getRoleAssertions(indv)) {
                int roleid = manager.getRoleID(ra.getRole());
                int lhs = manager.getAnonIndvID(ra.getLhs());
                int rhs = manager.getAnonIndvID(ra.getRhs());
                bulkFile.writeGenRole(anonindv, roleid, lhs, rhs);
            }
        }
    }
}
