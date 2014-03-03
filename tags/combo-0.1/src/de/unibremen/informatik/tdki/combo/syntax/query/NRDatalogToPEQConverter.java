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

/**
 *
 * @author İnanç Seylan
 */
public class NRDatalogToPEQConverter {

    public NRDatalogToPEQConverter() {
    }

    public DisjunctiveQuery convert(final NRDatalogProgram program) {
        return generateUCQ(program.getHeadPredicate(), program);
    }

    private DisjunctiveQuery generateUCQ(String predicate, final NRDatalogProgram program) {
        DisjunctiveQuery ucq = new DisjunctiveQuery();
        for (ConjunctiveQuery cq : program.getRulesWithHead(predicate)) {
            ConjunctiveQuery newCq = new ConjunctiveQuery();
            for (Query atom : cq.getQueries()) {
                if (program.isDefinedPredicate(atom.getHead().getPredicateName())) {
                    DisjunctiveQuery newQuery = generateUCQ(atom.getHead().getPredicateName(), program);
                    newQuery.setHead(atom.getHead());
                    newCq.addQuery(newQuery);
                } else {
                    newCq.addQuery(atom);
                }
            }
            newCq.setHead(cq.getHead());
            ucq.add(newCq);
        }
        return ucq;
    }
}
