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
package de.unibremen.informatik.tdki.combo.syntax.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author İnanç Seylan
 */
public class TermCollectionVisitor implements QueryVisitor {

    private Set<String> terms;
    private Map<String, List<Query>> termFormulaMap;

    public TermCollectionVisitor(Set<String> terms, Map<String, List<Query>> termFormulaMap) {
        this.termFormulaMap = termFormulaMap;
        this.terms = terms;
    }

    @Override
    public void visitConceptAtom(ConceptAtom ca) {
        terms.add(ca.getTerm());
        mapTermToFormula(ca.getTerm(), ca);
    }

    @Override
    public void visitRoleAtom(RoleAtom ra) {
        terms.add(ra.getLeftTerm());
        mapTermToFormula(ra.getLeftTerm(), ra);
        // if you get something like R(x,x), then do not associate x with R(x,x) twice
        if (!ra.getLeftTerm().equals(ra.getRightTerm())) {
            terms.add(ra.getRightTerm());
            mapTermToFormula(ra.getRightTerm(), ra);
        }
    }

    public Set<String> getTerms() {
        return terms;
    }

    private void mapTermToFormula(String t, Query f) {
        List<Query> l;
        if (termFormulaMap.containsKey(t)) {
            l = termFormulaMap.get(t);
        } else {
            l = new ArrayList<Query>();
            termFormulaMap.put(t, l);
        }
        l.add(f);
    }

    @Override
    public void visitUCQ(DisjunctiveQuery ucq) {
        terms.addAll(ucq.getHead().getVariables());
        for (String v : ucq.getHead().getVariables()) {
            mapTermToFormula(v, ucq);
        }
    }

    @Override
    public void visitCQ(ConjunctiveQuery cq) {
        terms.addAll(cq.getHead().getVariables());
        for (String v : cq.getHead().getVariables()) {
            mapTermToFormula(v, cq);
        }
    }
}
