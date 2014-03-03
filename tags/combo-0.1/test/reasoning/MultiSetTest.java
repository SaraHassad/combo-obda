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
package reasoning;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author İnanç Seylan
 */
public class MultiSetTest {
    
    
     @Test
     public void testEqualsForCardinality() {
         Multiset<Integer> bag1 = HashMultiset.create();
         bag1.add(1);
         
         Multiset<Integer> bag2 = HashMultiset.create();
         bag2.add(1);
         bag2.add(1);
         
         Multiset<Integer> bag3 = HashMultiset.create();
         bag3.add(1);
         
         Assert.assertTrue(!bag1.equals(bag2));
         Assert.assertTrue(bag1.equals(bag3));
     }
     
     @Test
     public void testEqualsForOrder() {
         Multiset<Integer> bag1 = HashMultiset.create();
         bag1.add(1);
         bag1.add(2);
         
         Multiset<Integer> bag2 = HashMultiset.create();
         bag2.add(2);
         bag2.add(1);
         
         Assert.assertEquals(bag1, bag2);
     }
}