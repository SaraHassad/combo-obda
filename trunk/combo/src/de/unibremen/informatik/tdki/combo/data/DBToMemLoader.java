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

import de.unibremen.informatik.tdki.combo.subsumption.MemTBox;
import de.unibremen.informatik.tdki.combo.subsumption.TBox;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ConceptAssertion;
import de.unibremen.informatik.tdki.combo.syntax.axiom.GCI;
import de.unibremen.informatik.tdki.combo.syntax.axiom.ObjectRoleAssertion;
import de.unibremen.informatik.tdki.combo.syntax.axiom.RoleInclusion;
import de.unibremen.informatik.tdki.combo.syntax.concept.Concept;
import de.unibremen.informatik.tdki.combo.syntax.concept.RoleRestriction;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 *
 * @author İnanç Seylan
 */
public class DBToMemLoader {
    // TODO change the following to multisets

    private Set<ConceptAssertion> conceptAssertions = new HashSet<ConceptAssertion>();
    private Set<ObjectRoleAssertion> roleAssertions = new HashSet<ObjectRoleAssertion>();
    private TBox tbox = new MemTBox();
    private String project;

    public DBToMemLoader(String project) {
        this.project = project;
        materialize();
    }

    final public void materialize() {
        EncodingManagerDB2 manager = materializeSymbols();
        conceptAssertions.clear();
        roleAssertions.clear();
        tbox = new MemTBox();
        materializeConceptInclusions(manager);
        materializeRoleInclusions(manager);
        materializeConceptAssertions(manager);
        materializeRoleAssertions(manager);
    }

    public Set<ConceptAssertion> getConceptAssertions() {
        return conceptAssertions;
    }

    public Set<ObjectRoleAssertion> getRoleAssertions() {
        return roleAssertions;
    }

    public Set<RoleInclusion> getRoleInclusions() {
        return tbox.getRoleInclusions();
    }

    public Set<GCI> getConceptInclusions() {
        return tbox.getConceptInclusions();
    }

    private EncodingManagerDB2 materializeSymbols() {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        DB2Interface.getJDBCTemplate().query("SELECT name, id FROM " + DBLayout.getTableSymbols(project), new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                String name = rs.getString(1);
                int id = rs.getInt(2);
                map.put(name, id);
            }
        });
        EncodingManagerDB2 result = new EncodingManagerDB2(map);
        return result;
    }

    private void materializeConceptAssertions(final EncodingManagerDB2 manager) {
        String selectConceptAssertions = "SELECT concept, individual FROM " + DBLayout.getTableConceptAssertions(project);
        DB2Interface.getJDBCTemplate().query(selectConceptAssertions, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                ConceptAssertion ca = new ConceptAssertion();
                ca.setConcept(manager.getConcept(rs.getInt(1)));
                ca.setIndividual(materializeIndividual(rs.getInt(2), manager));
                conceptAssertions.add(ca);
            }
        });
    }

    private void materializeRoleAssertions(final EncodingManagerDB2 manager) {
        String selectRoleAssertions = "SELECT role, lhs, rhs FROM " + DBLayout.getTableRoleAssertions(project);
        DB2Interface.getJDBCTemplate().query(selectRoleAssertions, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                ObjectRoleAssertion ra = new ObjectRoleAssertion();
                RoleRestriction exists = (RoleRestriction) manager.getConcept(rs.getInt(1));
                ra.setRole(exists.getRole());
                ra.setLhs(materializeIndividual(rs.getInt(2), manager));
                ra.setRhs(materializeIndividual(rs.getInt(3), manager));
                roleAssertions.add(ra);
            }
        });
    }

    private String materializeIndividual(int id, EncodingManagerDB2 manager) {
        if (EncodingManagerDB2.isNamedIndividual(id)) {
            return manager.getNamedIndividual(id);
        } else {
            StringBuilder builder = new StringBuilder();
            RoleRestriction exists = (RoleRestriction) manager.getConcept(id);
            builder.append("c_").append(exists.getRole().getName());
            if (exists.getRole().isInverse()) {
                builder.append("-");
            }
            if (EncodingManagerDB2.isCopyAnonymousIndividual(id)) { // copy of an anonymous individual
                builder.append("1");
            } else {
                builder.append("0");
            }
            return builder.toString();
        }
    }

    private void materializeRoleInclusions(final EncodingManagerDB2 manager) {
        String selectMaterializeInclusionAxioms = "SELECT lhs, rhs FROM " + DBLayout.getTableRBox(project);
        DB2Interface.getJDBCTemplate().query(selectMaterializeInclusionAxioms, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                RoleRestriction lhs = (RoleRestriction) manager.getConcept(rs.getInt(1));
                RoleRestriction rhs = (RoleRestriction) manager.getConcept(rs.getInt(2));
                tbox.add(new RoleInclusion(lhs.getRole(), rhs.getRole()));
            }
        });
    }

    private void materializeConceptInclusions(final EncodingManagerDB2 manager) {
        String selectMaterializeInclusionAxioms = "SELECT lhs, rhs FROM " + DBLayout.getTableTBox(project);
        DB2Interface.getJDBCTemplate().query(selectMaterializeInclusionAxioms, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                Concept lhs = manager.getConcept(rs.getInt(1));
                Concept rhs = manager.getConcept(rs.getInt(2));
                GCI gci = new GCI(lhs, rhs);
                tbox.add(gci);
            }
        });
    }
}
