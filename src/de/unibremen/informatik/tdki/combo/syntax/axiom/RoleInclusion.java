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

import de.unibremen.informatik.tdki.combo.syntax.Role;


/**
 * 
 * @author İnanç Seylan
 *
 */
public class RoleInclusion extends Axiom {
	private Role lhs, rhs;

	private boolean negated;

	public RoleInclusion() {

	}

	public RoleInclusion(Role lhs, Role rhs) {
		setLhs(lhs);
		setRhs(rhs);
	}

	public Role getLhs() {
		return lhs;
	}

	public void setLhs(Role lhs) {
		this.lhs = lhs;
	}

	public Role getRhs() {
		return rhs;
	}

	public void setRhs(Role rhs) {
		this.rhs = rhs;
	}

	public RoleInclusion toggleNegated() {
		negated = !negated;
		return this;
	}

	public boolean isNegated() {
		return negated;
	}

	@Override
	public int hashCode() {
		return 17 * 37 + getLhs().hashCode() + getRhs().hashCode()
				+ +new Boolean(negated).hashCode();
	}

	public boolean equals(Object obj) {
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			RoleInclusion other = (RoleInclusion) obj;
			return isNegated() == other.isNegated()
					&& getLhs().equals(other.getLhs())
					&& getRhs().equals(other.getRhs());
		}
		return false;
	}

	public RoleInclusion copy() {
		RoleInclusion ri = new RoleInclusion();
		ri.setLhs(getLhs().copy());
		ri.setRhs(getRhs().copy());
		ri.negated = negated;
		return ri;
	}

	public void accept(AxiomVisitor v) {
		v.visitRoleInclusion(this);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getLhs().getName());
		buffer.append(" >- ");
		buffer.append(getRhs().getName());
		if (isNegated()) {
			buffer.insert(0, "not(");
			buffer.append(")");
		}
		return buffer.toString();
	}

}
