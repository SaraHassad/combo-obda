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
package de.unibremen.informatik.tdki.combo.syntax.axiom;


/**
 * 
 * @author İnanç Seylan
 *
 */
public class SameIndividualAssertion extends Axiom {
	private String lhs;

	private String rhs;

	private boolean negated;

	public SameIndividualAssertion() {

	}

	public SameIndividualAssertion(String lhs, String rhs) {
		setLhs(lhs);
		setRhs(rhs);
	}

	public String getLhs() {
		return lhs.toString();
	}

	public void setLhs(String lhs) {
		this.lhs = lhs;
	}

	public String getRhs() {
		return rhs.toString();
	}

	public void setRhs(String rhs) {
		this.rhs = rhs;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			SameIndividualAssertion other = (SameIndividualAssertion) obj;
			boolean operandsEqual = (lhs.equals(other.lhs) && rhs
					.equals(other.rhs))
					|| (lhs.equals(other.rhs) && rhs.equals(other.lhs));
			result = operandsEqual && (isNegated() == other.isNegated());
		}
		return result;
	}

	@Override
	public int hashCode() {
		return 17 * 37 + lhs.hashCode() + rhs.hashCode()
				+ new Boolean(negated).hashCode();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getLhs());
		buffer.append(" == ");
		buffer.append(getRhs());
		if (isNegated()) {
			buffer.insert(0, "not(");
			buffer.append(")");
		}
		return buffer.toString();
	}

	public void accept(AxiomVisitor v) {
		v.visitSameIndividualAssertion(this);
	}

	public boolean isNegated() {
		return negated;
	}

	public SameIndividualAssertion toggleNegated() {
		negated = !negated;
		return this;
	}

	public SameIndividualAssertion copy() {
		SameIndividualAssertion result = new SameIndividualAssertion();
		result.lhs = getLhs();
		result.rhs = getRhs();
		result.negated = negated;
		return result;
	}
}
