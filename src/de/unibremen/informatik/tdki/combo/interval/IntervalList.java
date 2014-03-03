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

import java.util.Iterator;
import java.util.TreeSet;

enum PointType {

    LOW, HIGH
};

class Point implements Comparable<Point> {

    int value;
    PointType type;

    public Point(int value, PointType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Point other = (Point) obj;
        if (this.value != other.value) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.value;
        hash = 53 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Point p) {
        if (p == null) {
            throw new NullPointerException();
        }

        int tcomp = type.compareTo(p.type);
        if (tcomp == 0) {
            return new Integer(value).compareTo(p.value); // guarantees that the method is consistent with equals
        } else if (tcomp == -1) {
            if (value - 1 <= p.value) {
                return -1;
            } else {
                return 1;
            }
        } else {
            if (p.value - 1 <= value) {
                return 1;
            } else {
                return -1;
            }
        }

//        if (value < p.value) {
//            return -1;
//        } else if (value > p.value) {
//            return 1;
//        } else { // equal values
//            return type.compareTo(p.type);
//        }
    }
}

class IntervalListIterator implements Iterator<Interval> {
    private Iterator<Point> iterator;
    
    public IntervalListIterator(Iterator<Point> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Interval next() {
        int low = iterator.next().value;
        int high = iterator.next().value;
        return new Interval(low, high);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

/**
 *
 * @author İnanç Seylan
 */
public class IntervalList {

    private TreeSet<Point> points = new TreeSet<Point>();

    public void add(Interval i) {
        points.add(new Point(i.getLow(), PointType.LOW));
        points.add(new Point(i.getHigh(), PointType.HIGH));
    }

    public Iterator<Interval> iterator() {
        return new IntervalListIterator(points.iterator());
    }

    public IntervalList normalize() {
        IntervalList result = new IntervalList();
        int counter = 0;
        Iterator<Point> i = points.iterator();
        boolean reset = true;
        while (i.hasNext()) {
            Point p = i.next();
            counter = p.type.equals(PointType.LOW)
                    ? counter + 1
                    : counter - 1;
            if (counter == 0) {
                result.points.add(p);
                reset = true;
            } else if(counter == 1 && reset) {
                result.points.add(p);
                reset = false;
            } 
        }
        return result;
    }
}
