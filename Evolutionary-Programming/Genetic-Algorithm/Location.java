/**
 * Models an x, y coordinate for an amino acid on a plane defined by integers
 * (i.e., not in real space). This class introduces a layer of abstraction that
 * permits simpler processing of x and y coordinates.
 *
 * @author Franklin D. Worrell
 * @version 17 February 2018
 */
public class Location {
    private int x;
    private int y;


    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }


    public Location(Location other) {
        this.x = other.x;
        this.y = other.y;
    }


    public int getX() {
        return this.x;
    }


    public int getY() {
        return this.y;
    }


    public void setX(int newX) {
        this.x = newX;
    }


    public void setY(int newY) {
        this.y = newY;
    }


    @Override
    public boolean equals(Object o) {
        // Null and type check.
        if ((o == null) || !(o instanceof Location)) {
            return false;
        }

        // Check x- and y-coordinates.
        Location other = (Location) o;
        if ((this.x == other.x) && (this.y == other.y)) {
            return true;
        }

        return false;
    }


    /**
     * Returns a hash code for the instance. This implementation is a bit
     * strange, because we need to differentiate between the ordered pairs
     * (2, 4) and (4, 2). Hence, the <code>String</code> representing the
     * pair is used, as the <code>hashCode</code> method of <code>String</code>
     * takes the order of the characters into account.
     *
     * @return the hash code for this instance
     */
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }


    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }


    /**
     * Returns a new <code>Location</code> that is the coordinate following
     * the given <code>Location</code> in the specified direction.
     *
     * @param previous the <code>Location preceding the one to be produced
     * @param direction the orientation of the new <code>Location</code> from the prior
     * @return a new <code>Location</code> at the coordinate specified
     */
    public static Location createNextLocation(Location previous,
                                              int direction) {
        int previousX = previous.getX();
        int previousY = previous.getY();
        int newX;
        int newY;

        // Given the direction to move, update x- and y-coordinate.
        if (direction == 1) {
            newX = previousX + 1;
            newY = previousY;
        } else if (direction == 2) {
            newX = previousX - 1;
            newY = previousY;
        } else if (direction == 3) {
            newX = previousX;
            newY = previousY + 1;
        } else {
            newX = previousX;
            newY = previousY - 1;
        }

        return new Location(newX, newY);
    }

}
