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

import de.unibremen.informatik.tdki.combo.subsumption.AnonymousIndividual;
import de.unibremen.informatik.tdki.combo.syntax.Role;
import de.unibremen.informatik.tdki.combo.syntax.concept.Concept;
import de.unibremen.informatik.tdki.combo.syntax.concept.ConceptName;
import de.unibremen.informatik.tdki.combo.syntax.concept.RoleRestriction;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author İnanç Seylan
 */
public class EncodingManagerDB2 {

    private Map<String, Integer> mapNameToId = new HashMap<String, Integer>();
    private Map<Integer, String> mapIdToName = new HashMap<Integer, String>();
    private int symbolsCount = 1;
    private int individualsCount = 0;
    private boolean readOnly = false;

    public EncodingManagerDB2() {
    }

    /**
     * Initializes a read-only instance with the given mappings, i.e., new
     * mappings can not be done.
     *
     * @param mapNameToId
     */
    public EncodingManagerDB2(Map<String, Integer> mapNameToId) {
        readOnly = true;
        for (Map.Entry<String, Integer> entry : mapNameToId.entrySet()) {
            mapIdToName.put(entry.getValue(), entry.getKey());
        }
    }
    
    public static boolean isNamedIndividual(int id) {
        return id < 0;
    }
    
    public static boolean isCopyAnonymousIndividual(int id) {
        int type = id & 15;
        return (type == 9 || type == 11);
    }

    private int nextSymbolID() {
        int id = symbolsCount * 16;
        symbolsCount++;
        return id;
    }

    private int nextIndividualID() {
        individualsCount--;
        int id = individualsCount;
        return id;
    }
    
    public String getNamedIndividual(int id) {
        assert (id < 0);
        return mapIdToName.get(id);
    }

    public Concept getConcept(int id) {
        assert (id > 0); // not an id for an individual
        Concept result;
        int normalizedId = id & 0xfffffffc; // we only store names so normalize the id if it is for an inverse role or a copy of an anonymous individual
        String name = mapIdToName.get(normalizedId);
        int type = id & 0x0000000e;
        if (type == 0) { // concept name
            result = new ConceptName(name);
        } else { // role
            Role r = new Role(name);
            if (type == 10) {
                r.setInverse(true);
            }
            result = new RoleRestriction(RoleRestriction.Constructor.SOME, r);
        }
        return result;
    }

    public int getConceptID(Concept c) {
        ConceptTypeGeneratingVisitor v = new ConceptTypeGeneratingVisitor();
        c.accept(v);
        int id = mapNameToId.get(v.getName());
        if (v.getType() == ExpressionConstants.INV_ROLE) {
            id = id | 0x00000002;
        }
        return id;
    }

    public int getRoleID(Role r) {
        return getConceptID(new RoleRestriction(RoleRestriction.Constructor.SOME, r));
    }

    public int getAnonIndvID(AnonymousIndividual indv) {
        int roleId = getRoleID(indv.getRole());
        if (indv.isCopy()) {
            roleId = roleId ^ 1;
        }
        return roleId;
    }

    public int getIndvID(String indv) {
        return mapNameToId.get(indv);
    }

    /*
     * Does the concept mapping if this concept is not already mapped to an id.
     * Only works if the instance is not read-only
     */
    public void mapConcept(Concept c) {
        assert (readOnly == false);
        ConceptTypeGeneratingVisitor v = new ConceptTypeGeneratingVisitor();
        c.accept(v);
        if (!mapNameToId.containsKey(v.getName())) {
            int id = nextSymbolID();
            if (v.getType() == ExpressionConstants.INDIVIDUAL_NAME) {
                id = id | 4;
            } else if (v.getType() == ExpressionConstants.ROLE_NAME || v.getType() == ExpressionConstants.INV_ROLE) {
                id = id | 0x00000008;
            }
            mapNameToId.put(v.getName(), id);
            mapIdToName.put(id, v.getName());
        }
    }

    public void mapRole(Role r) {
        mapConcept(new RoleRestriction(RoleRestriction.Constructor.SOME, r));
    }

    public void mapIndividual(String individual) {
        assert (readOnly == false);
        if (mapNameToId.containsKey(individual)) {
            int id = mapNameToId.get(individual);
            assert (id < 0); // ensure that this is a valid id for an individual
        } else {
            int id = nextIndividualID();
            mapNameToId.put(individual, id);
            mapIdToName.put(id, individual);
        }
    }

    public Map<String, Integer> getNameToIdMap() {
        return mapNameToId;
    }
    
}
