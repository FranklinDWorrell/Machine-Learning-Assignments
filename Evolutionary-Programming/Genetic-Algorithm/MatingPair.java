/**
 * An abstraction used for passing pairs of <code>Chromosomes</code>
 * between methods and classes. Specifically used to easily
 * facilitate passing <code>Chromosome</code> pairs to the method
 * that performs crossover.
 *
 * @author Franklin D. Worrell
 * @version 16 September 2018
 */
public class MatingPair {

    /**
     * One member of the <code>MatingPair</code>.
     */
    private Chromosome left;

    /**
     * The other member of the <code>MatingPair</code>.
     */
    private Chromosome right;

    /**
     * Constructs a <code>MatingPair</code> from the two
     * <code>Chromosome</code> instances passed as arguments.
     *
     * @param left one of the <code>Chromosomes</code> to mate
     * @param right the other <code>Chromosome</code> to mate
     */
    public MatingPair(Chromosome left, Chromosome right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the left-hand member of the pair to mate.
     *
     * @return the first member of the pair to mate
     */
    public Chromosome getLeft() {
        return left;
    }

    /**
     * Returns the right-hand member of the pair to mate.
     *
     * @return the second member of the pair to mate
     */
    public Chromosome getRight() {
        return right;
    }

}
