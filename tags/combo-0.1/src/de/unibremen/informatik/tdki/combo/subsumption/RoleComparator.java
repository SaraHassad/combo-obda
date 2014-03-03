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
package de.unibremen.informatik.tdki.combo.subsumption;

import java.util.Comparator;
import de.unibremen.informatik.tdki.combo.syntax.Role;

/**
 *
 * @author İnanç Seylan
 */
public class RoleComparator implements Comparator<Role>{

    @Override
    public int compare(Role o1, Role o2) {
        if (o1.equals(o2)) {
            return 0;
        }
        // roles either have 
        // 1. different name or 
        // 2. the same name but one is inverse and the other not
        if (o1.getName().equals(o2.getName())) {
            if (o2.isInverse()) {
                return -1;
            } else {
                return 1;
            }
        }
        // role have a different name
        return o1.getName().compareTo(o2.getName());
    }
    
}
