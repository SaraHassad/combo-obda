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
package de.unibremen.informatik.tdki.combo.syntax.sql;

/**
 *
 * @author İnanç Seylan
 */
public class WhereCondition implements Where {

    public enum Operator {

        EQUAL, NOT_EQUAL, LEQ, GEQ, LESS_THAN, GREATER_THAN;
    }
    private String lhs, rhs;
    private Operator operator;

    public WhereCondition() {
    }

    public WhereCondition(Operator op, String lhs, String rhs) {
        this.operator = op;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public String getLhs() {
        return lhs;
    }

    public void setLhs(String lhs) {
        this.lhs = lhs;
    }

    public String getRhs() {
        return rhs;
    }

    public void setRhs(String rhs) {
        this.rhs = rhs;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.lhs != null ? this.lhs.hashCode() : 0);
        hash = 23 * hash + (this.rhs != null ? this.rhs.hashCode() : 0);
        hash = 23 * hash + (this.operator != null ? this.operator.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WhereCondition other = (WhereCondition) obj;
        if ((this.lhs == null) ? (other.lhs != null) : !this.lhs.equals(other.lhs)) {
            return false;
        }
        if ((this.rhs == null) ? (other.rhs != null) : !this.rhs.equals(other.rhs)) {
            return false;
        }
        if (this.operator != other.operator) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(lhs);
        switch (operator) {
            case EQUAL:
                builder.append("=");
                break;
            case NOT_EQUAL:
                builder.append("!=");
                break;
            case GEQ:
                builder.append(">=");
                break;
            case GREATER_THAN:
                builder.append(">");
                break;
            case LEQ:
                builder.append("<=");
                break;
            case LESS_THAN:
                builder.append("<");
                break;
        }
        builder.append(rhs);
        return builder.toString();
    }
}
