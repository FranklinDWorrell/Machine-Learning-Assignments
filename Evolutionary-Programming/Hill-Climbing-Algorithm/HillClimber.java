import java.util.BitSet; 
import java.util.Random; 

/**
 * A program demonstrating the use of a simple, iterative hillclimber 
 * algorithm. Generates a 40 bit binary input, produces a neighborhood 
 * of 40 neighbors, and iterates 100 times. The maximum of the prescribed 
 * objective function discovered by each iteration is printed in a comma-
 * separated list. 
 * @author Franklin D. Worrell
 * @version 20 February 2018
 */ 
public class HillClimber {
	public static final int MAX = 100; 				// Iterations of algorithm.
	public static final int NUMBER_OF_BITS = 40; 	// Size of input variable. 
	public static final int NUMBER_OF_NEIGHBORS = 40; 	// Number of neighbors. 
	
	private Random random; 	// Used to populate initial input variable. 
	
	/**
	 * Initializes the <code>Random</code> instance that is used 
	 * to populate the <code>BitSet</code> instances used in the 
	 * hillclimbing algorithm.
	 */ 
	public HillClimber() {
		this.random = new Random(); 
	} 
	
	/**
	 * Implements a simple iterated hillclimber like that found in 
	 * Figure 2 of the lecture for Chapter 3. 
	 * @return a <code>String</code> that reports the maximum found in each iteration
	 */ 
	public String climbHill() {
		StringBuilder results = new StringBuilder(); 	// Formatted results. 
		
		for (int t = 0; t < MAX; t++) {
			// Whether a superior neighbor has been found or not. 
			boolean local = false; 
			
			// Create and randomly populate a binary input variable. 
			BitSet current = this.generateInput(); 
			
			// Evaluate the result of the current BitSet
			int currentF = f(current); 
			
			// Loop until a superior neighbor has been found. 
			while (!local) {
				// Create the neighborhood. 
				BitSet[] neighbors = this.initializeNeighbors(current); 

				// Find the best neighbor and compare it with current. 
				int indexOfBest = this.findFittest(neighbors); 
				
				// New current best found, continue looping. 
				if (currentF < this.f(neighbors[indexOfBest])) {
					current = neighbors[indexOfBest]; 
					currentF = this.f(current); 
				}
				
				// No new current best found, stop this iteration. 
				else {
					local = true; 
					// Append results for reporting. 
					results.append(t < (MAX -1) ? currentF + ", " : currentF);
				} 
			}
		}
		
		return results.toString(); 
	} 
	
	
	/**
	 * Creates and randomly populates a new binary input variable. 
	 * @return a new BitSet that is randomly populated 
	 */ 
	private BitSet generateInput() {
		BitSet current = new BitSet(NUMBER_OF_BITS); 
		
		// Randomly flip some of the bits in the BitSet. 
		for (int i = 0; i < NUMBER_OF_BITS; i++) {
			if (this.random.nextBoolean()) {
				current.set(i); 
			} 
		}
		
		return current; 
	}
	
	
	/**
	 * Given a <code>BitSet</code> binary input, generates an array of 
     * <code>BitSet</code> objects to serve as neighbors, initializes 
	 * each element of the array as a clone of the original input 
	 * with one bit flipped, and returns the array. 
	 * @param current the <code>BitSet</code> to base neighborhood upon 
	 * @return a <code>BitSet[]</code> that holds the neighbors
	 */ 
	private BitSet[] initializeNeighbors(BitSet current) {
		BitSet[] neighbors = new BitSet[NUMBER_OF_NEIGHBORS]; 

		for (int i = 0; i < NUMBER_OF_NEIGHBORS; i++) {
			// Each BitSet neighbor is mostly identical to the current BitSet. 
            BitSet bitSet = (BitSet) current.clone(); 
			bitSet.flip(i); 	// Flip the i-th bit. 
			neighbors[i] = bitSet; 
		}
		
		return neighbors;
	}
	
	
	/**
	 * Given a <code>BitSet[]</code>, finds the <code>BitSet</code> 
	 * element that maximizes the value of the objective function 
	 * <code>this.f</code>, and returns the index of the fittest element. 
	 * @param neighbors the <code>BitSet[]</code> from which a fittest is needed
	 * @return the index of the fittest <code>BitSet</code> in the array
	 */ 
	private int findFittest(BitSet[] neighbors) {
		int best = 0; 
		
		// Find the maximizing BitSet in the array. 
		for (int i = 1; i < NUMBER_OF_NEIGHBORS; i++) {
			if (this.f(neighbors[i]) > this.f(neighbors[best])) {
				best = i; 
			}
		}
		
		return best; 
	}
			
	
	/**
	 * The objective function whose maximum-value is being sought:  
	 * f = |12 * one(v) - 160|
	 * @param bits the BitSet whose 1s need to be counted
	 * @return the value of function f, given the binary input
	 */ 
	private int f(BitSet bits) {
		return Math.abs(12 * bits.cardinality() - 160); 
	} 
	
	
	/**
	 * Runs the hill climbing algorithm and prints the results. 
	 */ 
	public static void main(String[] args) {
		HillClimber hc = new HillClimber(); 
		String results = hc.climbHill(); 
		System.out.println("Results of " + MAX + " runs of the algorithm: "); 
		System.out.println(results); 
	} 
	
} 
