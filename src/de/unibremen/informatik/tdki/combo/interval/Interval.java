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
package de.unibremen.informatik.tdki.combo.interval;

/**
 *
 * @author İnanç Seylan
 */
public class Interval {

    private int low, high;

    public Interval(int lhs, int rhs) {
        assert (lhs >= 0) && (rhs >= 0) && (lhs <= rhs);
        this.low = lhs;
        this.high = rhs;
    }

    public int getLow() {
        return low;
    }

    public int getHigh() {
        return high;
    }

    public boolean overlaps(Interval other) {
        return (this.low <= other.high) && (other.low <= this.high);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Interval other = (Interval) obj;
        if (this.low != other.low) {
            return false;
        }
        if (this.high != other.high) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.low;
        hash = 37 * hash + this.high;
        return hash;
    }
}
