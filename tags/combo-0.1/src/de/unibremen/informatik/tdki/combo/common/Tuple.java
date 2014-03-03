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
package de.unibremen.informatik.tdki.combo.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author İnanç Seylan
 */
public class Tuple<T> {

    private List<T> list;

    public Tuple(T... elements) {
        list = new ArrayList<T>();
        list.addAll(Arrays.asList(elements));
    }

    public boolean add(T e) {
        return list.add(e);
    }

    public List<T> getElements() {
        return Collections.unmodifiableList(list);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.list != null ? this.list.hashCode() : 0);
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
        final Tuple<T> other = (Tuple<T>) obj;
        if (this.list != other.list && (this.list == null || !this.list.equals(other.list))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Tuple{" + "list=" + list + '}';
    }
}
