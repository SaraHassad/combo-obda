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
public class ObjectRoleAssertion extends Axiom {

	private Role role;

	private String owner;

	private String filler;

	public ObjectRoleAssertion() {

	}

	public ObjectRoleAssertion(Role role, String owner, String filler) {
		setRole(role);
		setLhs(owner);
		setRhs(filler);
	}

	public String getRhs() {
		return filler.toString();
	}

	public String getLhs() {
		return owner.toString();
	}

	public Role getRole() {
		return role;
	}

	public void setRhs(String filler) {
		this.filler = filler;
	}

	public void setLhs(String owner) {
		this.owner = owner;
	}

	public void setRole(Role role) {
		this.role = role;
	}


	@Override
	public boolean equals(Object obj) {
		if (obj != null && (obj.getClass().equals(this.getClass()))) {
			ObjectRoleAssertion other = (ObjectRoleAssertion) obj;
			return (this.getRole().equals(other.getRole())
					&& owner.equals(other.owner) && filler.equals(other.filler))
					|| (this.getRole().copy().toggleInverse().equals(
							other.getRole())
							&& owner.equals(other.filler) && filler
							.equals(other.owner));
		}
		return false;
	}


	@Override
	public int hashCode() {
		return 17 * 37 + getRole().hashCode() + getLhs().hashCode()
				+ getRhs().hashCode();
	}

	public ObjectRoleAssertion copy() {
		ObjectRoleAssertion ra = new ObjectRoleAssertion();
		ra.role = role.copy();
		ra.owner = getLhs();
		ra.filler = getRhs();
		return ra;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getRole().getName());
		buffer.append("(");
		buffer.append(getLhs());
		buffer.append(", ");
		buffer
				.append(getRhs());
		buffer.append(")");
		if (isNegated()) {
			buffer.insert(0, "not(");
			buffer.append(")");
		}
		return buffer.toString();
	}

	public void accept(AxiomVisitor v) {
		v.visitObjectRoleAssertion(this);
	}

	public ObjectRoleAssertion toggleNegated() {
		return this;
	}

	public boolean isNegated() {
		return false;
	}

	public ObjectRoleAssertion normalize(String expectedOwner) {
		ObjectRoleAssertion result = this.copy();
		if (getRhs().equals(expectedOwner)) {
			result.setLhs(getRhs());
			result.setRhs(getLhs());
			result.setRole(getRole().copy().toggleInverse());
		}
		return result;
	}

}
