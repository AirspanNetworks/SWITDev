/**
 * 
 */
package Utils;

import java.util.Objects;

/**
 * Container to ease passing around a tuple of two objects. This object provides a sensible
 * implementation of equals(), returning true if equals() is true on each of the contained
 * objects.
 */
public class Pair<K, V> {

    private final K element0;
    private final V element1;

    /**
     * Convenience method for creating an appropriately typed pair.
     * @param a the first object in the Pair
     * @param b the second object in the pair
     * @return a Pair that is templatized with the types of a and b
     */
    public static <K, V> Pair<K, V> createPair(K element0, V element1) {
        return new Pair<K, V>(element0, element1);
    }

    /**
     * Constructor for a Pair.
     *
     * @param element0 the first object in the Pair
     * @param element1 the second object in the pair
     */
    public Pair(K element0, V element1) {
        this.element0 = element0;
        this.element1 = element1;
    }

    /**
     * Checks the two objects for equality by delegating to their respective
     * {@link Object#equals(Object)} methods.
     *
     * @param o the {@link Pair} to which this one is to be checked for equality
     * @return true if the underlying objects of the Pair are both considered
     *         equal
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        }
        
        Pair<?, ?> p = (Pair<?, ?>) o;
        return Objects.equals(p.element0, element0) && 
                       Objects.equals(p.element1, element1);
    }
    
    /**
     * Compute a hash code using the hash codes of the underlying objects
     *
     * @return a hash code of the Pair
     */
    @Override
    public int hashCode() {
        return (element0 == null ? 0 : element0.hashCode()) ^
        	           (element1 == null ? 0 : element1.hashCode());
    }
    
    public K getElement0() {
        return element0;
    }

    public V getElement1() {
        return element1;
    }

}
