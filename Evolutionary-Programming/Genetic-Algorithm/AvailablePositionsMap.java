import java.util.HashMap;

/**
 * Used to determine possible directions to place the next amino acid given
 * the placement of the previous amino acids. Directions are abstracted as
 * integer values.
 *
 * @author Franklin D. Worrell
 * @version 16 September 2018
 */
public class AvailablePositionsMap {
    private HashMap<Integer, int[]> moveMap;

    /**
     * Constructs a new map for ease in building a protein structure one
     * amino acid at a time.
     */
    public AvailablePositionsMap() {
        this.moveMap = new HashMap<>();
        moveMap.put(1, new int[] {1, 2, 4});
        moveMap.put(2, new int[] {2, 3, 4});
        moveMap.put(3, new int[] {1, 2, 3});
        moveMap.put(4, new int[] {1, 2, 4});
    }

    /**
     * Given a previous direction of a bond in the protein structure,
     * returns a numerically coded array of possible directions for
     * subsequent bonds.
     *
     * @param previousDirection the direction of the previous bond in the protein
     * @return an array of available directions for subsequent bonds
     */
    public int[] getAvailableNextDirection(int previousDirection) {
        return this.moveMap.get(previousDirection);
    }
}
