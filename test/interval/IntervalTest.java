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
package interval;

import de.unibremen.informatik.tdki.combo.interval.Interval;
import de.unibremen.informatik.tdki.combo.interval.IntervalList;
import java.util.Iterator;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author İnanç Seylan
 */
public class IntervalTest {
    
    @Test
    public void testInterval() {
        boolean invalidInterval = false;
        try {
            new Interval(4, 3);
        } catch (AssertionError e) {
            invalidInterval = true;
        }
        Assert.assertTrue(invalidInterval);
        
        Interval i1 = new Interval(1, 5);
        Interval i2 = new Interval(2, 2);
        Assert.assertTrue(i1.overlaps(i2));
        Interval i3 = new Interval(6, 7);
        Assert.assertFalse(i1.overlaps(i3));
    }
    
    @Test
    public void testNormalize1() {
        IntervalList il = new IntervalList();
        il.add(new Interval(2, 6));
        il.add(new Interval(1, 4));
        il.add(new Interval(8, 12));
        
        IntervalList normalized = il.normalize();
        Iterator<Interval> i = normalized.iterator();
        Assert.assertEquals(i.next(), new Interval(1, 6));
        Assert.assertEquals(i.next(), new Interval(8, 12));
        Assert.assertFalse(i.hasNext());
    }
    
    @Test
    public void testNormalize2() {
        IntervalList il = new IntervalList();
        il.add(new Interval(4, 7));
        il.add(new Interval(2, 6));
        il.add(new Interval(1, 4));
        il.add(new Interval(8, 12));
        il.add(new Interval(7, 9));
        
        IntervalList normalized = il.normalize();
        Iterator<Interval> i = normalized.iterator();
        Assert.assertEquals(i.next(), new Interval(1, 12));
        Assert.assertFalse(i.hasNext());
    }
    
    @Test
    public void testNormalize3() {
        IntervalList il = new IntervalList();
        il.add(new Interval(4, 5));
        il.add(new Interval(1, 1));
        il.add(new Interval(2, 2));
        
        IntervalList normalized = il.normalize();
        Iterator<Interval> i = normalized.iterator();
        Assert.assertEquals(i.next(), new Interval(1, 2));
        Assert.assertEquals(i.next(), new Interval(4, 5));
        Assert.assertFalse(i.hasNext());
    }
}
