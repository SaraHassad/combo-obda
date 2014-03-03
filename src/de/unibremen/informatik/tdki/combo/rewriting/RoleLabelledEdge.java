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
package de.unibremen.informatik.tdki.combo.rewriting;

import org.jgrapht.graph.DefaultEdge;
import de.unibremen.informatik.tdki.combo.syntax.Role;

/**
 *
 * @author İnanç Seylan
 */
public class RoleLabelledEdge<V> extends DefaultEdge {

    private V source;
    private V target;
    private Role label;
    
    public RoleLabelledEdge(V source, V target, Role label) {
        setSource(source);
        setTarget(target);
        setLabel(label);
    }

    /**
     * @return the source
     */
    public V getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(V source) {
        this.source = source;
    }

    /**
     * @return the target
     */
    public V getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(V target) {
        this.target = target;
    }

    /**
     * @return the label
     */
    public Role getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(Role label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RoleLabelledEdge<V> other = (RoleLabelledEdge<V>) obj;
        if (this.source != other.source && (this.source == null || !this.source.equals(other.source))) {
            return false;
        }
        if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
            return false;
        }
        if ((this.label == null) ? (other.label != null) : !this.label.equals(other.label)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.source != null ? this.source.hashCode() : 0);
        hash = 37 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 37 * hash + (this.label != null ? this.label.hashCode() : 0);
        return hash;
    }
    
    
}
