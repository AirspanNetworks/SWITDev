/**
 * 
 */
package Utils;

import java.util.Objects;

/**
 * Container to ease passing around a tuple of three objects. This object
 * provides a sensible implementation of equals(), returning true if equals() is
 * true on each of the contained objects.
 */
public class Triple<L, M, R> {
	private final L leftElement;
	private final M middleElement;
	private final R rightElement;

	/**
	 * Convenience method for creating an appropriately typed Triple.
	 * 
	 * @param <L>
	 *            the left element type
	 * @param <M>
	 *            the middle element type
	 * @param <R>
	 *            the right element type
	 */
	public static <L, M, R> Triple<L, M, R> createTriple(L leftElement, M middleElement, R rightElement) {
		return new Triple<L, M, R>(leftElement, middleElement, rightElement);
	}

	/**
	 * Constructor for a Triple.
	 *
	 * @param element0
	 *            the first object in the Triple
	 * @param element1
	 *            the second object in the Triple
	 */
	public Triple(L leftElement, M middleElement, R rightElement) {
		this.leftElement = leftElement;
		this.middleElement  = middleElement;
		this.rightElement = rightElement;
	}

	/**
	 * Checks the two objects for equality by delegating to their respective
	 * {@link Object#equals(Object)} methods.
	 *
	 * @param o
	 *            the {@link Triple} to which this one is to be checked for
	 *            equality
	 * @return true if the underlying objects of the Triple are both considered
	 *         equal
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Triple)) {
			return false;
		}

		Triple<?, ?, ?> p = (Triple<?, ?, ?>) o;
		return Objects.equals(p.leftElement,leftElement) && Objects.equals(p.middleElement,middleElement) && Objects.equals(p.rightElement,rightElement);
	}

	/**
	 * Compute a hash code using the hash codes of the underlying objects
	 *
	 * @return a hash code of the Triple
	 */
	@Override
    public int hashCode() {
        return (getLeftElement() == null ? 0 : getLeftElement().hashCode()) ^
            (getMiddleElement() == null ? 0 : getMiddleElement().hashCode()) ^
            (getRightElement() == null ? 0 : getRightElement().hashCode());
    }

	public L getLeftElement() {
		return leftElement;
	}
	public M getMiddleElement() {
		return middleElement;
	}
	public R getRightElement() {
		return rightElement;
	}
}
