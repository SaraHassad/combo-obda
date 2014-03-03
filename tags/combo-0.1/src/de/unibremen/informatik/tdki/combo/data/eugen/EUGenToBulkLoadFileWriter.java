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
package de.unibremen.informatik.tdki.combo.data.eugen;

import de.unibremen.informatik.tdki.combo.data.BulkFile;
import de.unibremen.informatik.tdki.combo.data.EncodingManagerDB2;
import de.unibremen.informatik.tdki.combo.subsumption.*;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ConceptAssertion;
import de.unibremen.informatik.tdki.combo.syntax.axiom.GCI;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ObjectRoleAssertion;
import de.unibremen.informatik.tdki.combo.syntax.axiom.RoleInclusion;
import de.unibremen.informatik.tdki.combo.syntax.concept.Concept;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author İnanç Seylan
 */
public class EUGenToBulkLoadFileWriter {

    private EncodingManagerDB2 manager;
    private Map<String, Integer> indvIdMap;
    private Map<String, Integer> problematicIndividualsMap;
    private int indvCount;
    private TBox tbox;
    private int universitySymbolId;
    private File outputFile;
    private BulkFile bulkFile;

    public EUGenToBulkLoadFileWriter(File output) throws IOException, InterruptedException {
        this.outputFile = output;
        bulkFile = new BulkFile(outputFile);
        bulkFile.open(BulkFile.Open.WRITE);

        indvIdMap = new HashMap<String, Integer>();
        problematicIndividualsMap = new HashMap<String, Integer>();
        indvCount = 0;

        manager = new EncodingManagerDB2();
        tbox = new MemTBox();
    }

    public EncodingManagerDB2 getManager() {
        return manager;
    }

    public void clearIndvIdMapping() {
        indvIdMap = new HashMap<String, Integer>();
    }

    private int nextIndvId() {
        indvCount--;
        return indvCount;
    }

    private int getNormalIndvId(String name) {
        int iid;
        if (!indvIdMap.containsKey(name)) {
            iid = nextIndvId();
            // do the mapping
            indvIdMap.put(name, iid);
            bulkFile.writeSymbol(name, iid);
        } else {
            iid = indvIdMap.get(name);
        }
        return iid;
    }

    /*
     * In the end, the problematic individuals are universities. They have a
     * name of the form http://www.UniversityN.edu, where N is the university
     * number.
     */
    private boolean isProblematicIndividual(String indvName) {
        return (indvName.indexOf("Department") == -1);
    }

    private int getIndvId(String indvName) {
        int result;
        if (isProblematicIndividual(indvName)) {
            result = getProblematicIndvId(indvName);
        } else {
            result = getNormalIndvId(indvName);
        }
        return result;
    }

    private int getProblematicIndvId(String indvName) {
        int result;
        if (problematicIndividualsMap.containsKey(indvName)) {
            result = problematicIndividualsMap.get(indvName);
        } else {
            result = nextIndvId();
            problematicIndividualsMap.put(indvName, result);
            bulkFile.writeSymbol(indvName, result);
        }
        return result;
    }

    public void add(ConceptAssertion ca) {
        manager.mapConcept(ca.getConcept());
        int cid = manager.getConceptID(ca.getConcept());

        int indvID = getIndvId(ca.getIndividual());
        // only add the concept assertion if it is not of the form University(http://www.UniversityN.edu)
        if (!isProblematicIndividual(ca.getIndividual())) {
            bulkFile.writeConceptAssertion(cid, indvID);
        } else {
            // we keep the id of the concept name University for adding the University concept assertions later
            universitySymbolId = cid;
        }
    }

    public void add(ObjectRoleAssertion ra) {
        manager.mapRole(ra.getRole());
        int rid = manager.getRoleID(ra.getRole());
        int subjectID = getIndvId(ra.getLhs());
        int objectID = getIndvId(ra.getRhs());
        bulkFile.writeRoleAssertion(rid, subjectID, objectID);
    }

    private void addUniversityAssertions() {
        for (String indvname : problematicIndividualsMap.keySet()) {
            int indvid = problematicIndividualsMap.get(indvname);
            bulkFile.writeConceptAssertion(universitySymbolId, indvid);
        }
    }

    public void finishLoading() throws FileNotFoundException, IOException, InterruptedException {
        addUniversityAssertions();
        QualifiedExistentialEncoder encoder = new QualifiedExistentialEncoder();
        TBox newTBox = encoder.encode(tbox);
        DLLiteReasoner reasoner = new DLLiteReasoner(newTBox);
        reasoner.classify();
        generateIdsForConceptAndRoleNames(reasoner);
        // put the generated ids for concept and role names to the symbols file
        // note that individual names and their ids are already in that file
        Map<String, Integer> map = manager.getNameToIdMap();
        for (String name : map.keySet()) {
            bulkFile.writeSymbol(name, map.get(name));
        }
        addTBox(reasoner);
        addGeneratingRolesTable(reasoner);
        addQualifiedExistentials(encoder);
        bulkFile.close();
    }

    private void addQualifiedExistentials(QualifiedExistentialEncoder encoder) throws FileNotFoundException {
        for (EncodingInfo i : encoder.getEncodingInfoList()) {
            int newroleid = manager.getRoleID(i.getNewRole());
            int originalroleid = manager.getRoleID(i.getOriginalRole());
            int cnid = manager.getConceptID(i.getConceptName());
            bulkFile.writeQualifiedExistential(newroleid, originalroleid, cnid);
        }
    }

    private void addGeneratingRolesTable(DLLiteReasoner reasoner) throws FileNotFoundException {
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

    private void addTBox(DLLiteReasoner reasoner) throws FileNotFoundException {
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

    private void generateIdsForConceptAndRoleNames(DLLiteReasoner reasoner) {
        for (Concept c : reasoner.getTBoxConcepts()) {
            manager.mapConcept(c);
        }
        for (Role r : reasoner.getRoles()) {
            manager.mapRole(r);
        }
    }

    public void add(GCI gci) {
        tbox.add(gci);
    }

    public void add(RoleInclusion ri) {
        tbox.add(ri);
    }
}
