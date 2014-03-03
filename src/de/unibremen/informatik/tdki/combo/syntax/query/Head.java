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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author İnanç Seylan
 */
public class Head {

    private List<String> variables;
    private String predicateName;

    public Head() {
        variables = new ArrayList<String>();
    }

    public Head(String predicateName, String... variables) {
        this.predicateName = predicateName;
        this.variables = Arrays.asList(variables);
    }

    public void setPredicateName(String predicateName) {
        this.predicateName = predicateName;
    }

    public String getPredicateName() {
        return predicateName;
    }

    public boolean addVariable(String e) {
        return variables.add(e);
    }

    public boolean hasVariable(String v) {
        return variables.contains(v);
    }

    public List<String> getVariables() {
        return Collections.unmodifiableList(variables);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.variables != null ? this.variables.hashCode() : 0);
        hash = 97 * hash + (this.predicateName != null ? this.predicateName.hashCode() : 0);
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
        final Head other = (Head) obj;
        if (this.variables != other.variables && (this.variables == null || !this.variables.equals(other.variables))) {
            return false;
        }
        if ((this.predicateName == null) ? (other.predicateName != null) : !this.predicateName.equals(other.predicateName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getPredicateName());
        builder.append("(");
        for (int i = 0; i < variables.size(); i++) {
            builder.append(variables.get(i));
            if (i < variables.size() - 1) {
                builder.append(",");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    protected Head substitute(String x, String y) {
        Head result = new Head();
        result.setPredicateName(predicateName);
        List<String> newVars = new ArrayList<String>();
        for (String v : variables) {
            if (v.equals(x)) {
                newVars.add(y);
            } else {
                newVars.add(v);
            }
        }
        result.variables = newVars;
        return result;
    }
}
