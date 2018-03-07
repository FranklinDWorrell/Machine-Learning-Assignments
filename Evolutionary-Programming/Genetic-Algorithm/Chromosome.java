import java.util.Arrays; 
import java.util.HashMap;
import java.util.HashSet;  
import java.util.Random; 

/**
 * Models a chromosome for use in a genetic algorithm. Each instance is a 
 * complete protein structure that is a self-avoiding walk. 
 * @author Franklin D. Worrell
 * @version 21 February 2018 
 */ 
public class Chromosome {
	private static Random random; 
	private static HashMap<Integer, int[]> moveMap; 
	static {
		random = new Random(); 
		moveMap = new HashMap<>(); 
		moveMap.put(1, new int[] {1, 2, 4}); 	// Given a previous direction, 
		moveMap.put(2, new int[] {2, 3, 4});	// this map returns an array of 
		moveMap.put(3, new int[] {1, 2, 3}); 	// of valid next directions to
		moveMap.put(4, new int[] {1, 2, 4}); 	// move. 
	}
	
	private Location[] locations; 	// Coordinates of amino acids
	private boolean[] acids; 		// h is true; p is false 
	private int fitness; 

	/**
	 * Randomly generates a new, valid instance from the provided 
	 * <code>String<code> of amino acids, specified as 'h', 'H', 'p', or 'P'. 
	 * @param acidString the sequence of amino acids to configure 
	 * @return a valid <code>Chromosome</code> 
	 */ 
	public Chromosome(String acidString) { 
		// Size for all the arrays. 
		int proteinLength = acidString.length(); 

		// Generate the acids array from the String. 
		this.acids = new boolean[proteinLength]; 
		for (int i = 0; i < proteinLength; i++) {
			if ('h' == acidString.charAt(i) || 'H' == acidString.charAt(i)) {
				this.acids[i] = true; 
			} else {
				this.acids[i] = false; 
			} 
		} 
		
		// Randomly generate a valid structure for the chromosome. 
		this.locations = Chromosome.generateRandomLocations(proteinLength); 
		
		// Given the acids and locations, calculate the fitness. 
		this.fitness = Chromosome.computeFitness(this.acids, this.locations);
	} 
	
	
	/**
	 * Returns a new instance of the given amino acid sequence with the 
	 * given configuration and fitness. 
	 * @param acids the sequence of constituent amino acids 
	 * @param locations the coordinates of the amino acids 
	 * @param fitness the fitness, given the structure of the instance 
	 * @return a new instance with the given structure and fitness 
	 */ 
	public Chromosome(boolean[] acids, Location[] locations, int fitness) {
		this.acids = acids; 
		this.locations = locations; 
		this.fitness = fitness; 
	} 
	
	
	/**
	 * Returns the <b>already computed</b> fitness of this 
	 * <code>Chromosome</code>. 
	 * @return <code>this.fitness</code>
	 */ 
	public int getFitness() {
		return this.fitness; 
	} 
	
	
	/**
	 * Returns the 2D structure of this protein, i.e., the coordinates of 
	 * each amino acid in the protein. 
	 * @return the array containing the integral coordinates of each amino acid 
	 */ 
	public Location[] getLocations() {
		return this.locations; 
	} 
	
	
	/**
	 * Returns the sequence of amino acids coded as boolean values. 
	 * @return the sequence of amino acids composing this instance 
	 */ 
	public boolean[] getAcids() {
		return this.acids; 
	} 
	
	
	/**
	 * Returns a <code>String</code> representation of the 
	 * <code>Chromosome</code> instance. 
	 * @return a <code>String</code> representing the <code>Chromosome</code>
	 */ 
	@Override 
	public String toString() {
		String chromosomeString = "["; 
		
		// Add each location to the String. 
		for (int i = 0; i < this.locations.length; i++) {
			chromosomeString += this.locations[i]; 
			chromosomeString += (i == this.locations.length - 1) ? "] " : ", "; 
		} 
		
		// Add the fitness to the String. 
		chromosomeString += this.fitness; 
		
		return chromosomeString; 
	} 
	
		
	/**
	 * Given a candidate <code>Chromosome</code> that may or may not be a 
	 * valid protein structure, determines whether or not the candidate's 
	 * structure is a self-avoiding-walk. 
	 * @param candidate the <code>Chromosome</code> to validate
	 * @return whether the <code>Chromosome</code>'s structure is valid 
	 */ 
	public static boolean validate(Chromosome candidate) {
		return validate(candidate.locations); 
	}
	
	
	/**
	 * Returns whether or not the structure specified by the provided 
	 * <code>Location[]</code> is a self-avoiding-walk. 
	 * @param candidate the <code>Location[]</code> to check 
	 * @return whether <code>candidate</code> is a self-avoiding walk 
	 */ 
	public static boolean validate(Location[] candidate) {
		// Attempt to find duplicate locations. 
		for (int i = 0; i < (candidate.length - 1); i++) {
			for (int j = i + 1; j < candidate.length; j++) {
				if (candidate[i].equals(candidate[j])) {
					// A single duplicate location invalidates. 
					return false; 
				} 
			}
		}
		
		// No duplicates found--valid structure. 
		return true; 
	}


	/**
	 * Calculates the fitness for a given <code>Chromosome</code>. Used as a 
	 * helper method during the generation of new <code>Chromosome</code> 
	 * instances, so that their <code>fitness</code> instance variable is 
	 * set properly after creation and validation. Returns the number of 
	 * topologically neighboring hydrophobic-hydrophobic contacts that are not 
	 * covalent. 
	 * @param chromosome the <code>Chromosome</code> whose fitness to compute 
	 * @return the <code>Chromosome</code>'s fitness
	 */
	public static int computeFitness(Chromosome chromosome) {
		return computeFitness(chromosome.acids, chromosome.locations); 
	}
	
	
	/**
	 * Returns the fitness for a <code>Chromosome</code> if it possessed 
	 * the structure specified by the given acid sequence and configuration. 
	 * Returns the number of topologically neighboring hydrophobic-hydrophobic 
	 * contacts that are not covalent. 
	 * @param acids a boolean array listing the amino acids in order 
	 * @param locations the coordinates of each acid in order
	 * @return the fitness of the structure
	 */ 
	public static int computeFitness(boolean[] acids, Location[] locations) {
		int contacts = 0; 
		
		// Generate map for easy neighbor lookup. 
		HashMap<Location, Integer> hydrophobicLookup = new HashMap<>(); 
		for (int i = 0; i < locations.length; i++) {
			// Only add the hydrophobic acids--that's all we care about here. 
			if (acids[i]) {
				hydrophobicLookup.put(locations[i], i); 
			}
		}
		
		// Calculate neighbors for each hydrophobic acid. 
		for (int i = 0; i < locations.length; i++) {
			if (acids[i]) {
				int x = locations[i].getX(); 
				int y = locations[i].getY(); 
				HashSet<Location> candidateNeighbors = new HashSet<>(); 
				candidateNeighbors.add(new Location(x + 1, y)); 	// right
				candidateNeighbors.add(new Location(x - 1, y)); 	// left
				candidateNeighbors.add(new Location(x, y + 1)); 	// up
				candidateNeighbors.add(new Location(x, y - 1)); 	// down
				
				// See if a hydrophobic, non-covalent neighbor exists. 
				for (Location neighbor : candidateNeighbors) {
					Integer found = hydrophobicLookup.get(neighbor);
					if ((found != null) && ((int) found > i + 1)) {
						contacts--; 
					}
				}
			}
		}		
		return contacts; 
	}
	
	
	/**
	 * Performs a crossover operation at a random spot on the given 
	 * <code>Chromosome</code> instances by <b>altering both instances</b>. 
	 * Hence, this method should be used only when creating a new 
	 * <code>Population</code>. 
	 * @param left one <code>Chromosome</code> for crossing over 
	 * @param right the other <code>Chromosome</code> for crossing 
	 * @param pivot the spot at which to perform the crossover 
	 * @return a new <code>Chromosome</code> or <code>null</code> 
	 */ 
	public static Chromosome crossover(Chromosome left, Chromosome right, int pivot) {
		// Get the subsection to work with. 
		Location[] bottom = Arrays.copyOfRange(right.locations, pivot + 1, 
											   right.locations.length); 
											   
		// Get a new direction and try to point crossover that way. 
		int previousDirection = Chromosome.findPreviousDirection(left, pivot); 
		int[] availableDirections = Chromosome.moveMap.get(previousDirection); 
		boolean[] wasTried = {false, false, false}; 
		int numberAttempted = 0; 
		while (numberAttempted < 3) {
			int direction = Chromosome.random.nextInt(3); 
			
			// Ensure direction wasn't previously tried. If so, try again. 
			if (wasTried[direction]) { continue; } 
			
			// Untried direction generated. Shift and rotate bottom of protein. 
			wasTried[direction] = true; 
			numberAttempted++; 
			// Find the new starting point for the bottom. 
			Location origin = Location.createNextLocation(left.locations[pivot], 
														  direction);  
			Location[] newBottom = Chromosome.shiftAndRotate(bottom, 
					previousDirection, direction, origin); 
			
			// Build a new full sequence. 
			Location[] newFull = new Location[left.locations.length]; 
			for (int i = 0; i < newFull.length; i++) {
				if (i <= pivot) {
					newFull[i] = left.locations[i];
				} else {
					newFull[i] = newBottom[i - (pivot + 1)];
				}
			} 
			
			// Validate full sequence and return if valid. 
			if (Chromosome.validate(newFull)) {
				int newFitness = Chromosome.computeFitness(left.acids, newFull); 
				return new Chromosome(left.acids, newFull, newFitness); 
			}
		}
		
		// Could not generate a valid crossover, return null to caller. 
		return null; 
	} 
	
	
	/**
	 * Mutates a <code>Chromosome</code> by performing a rotation of the 
	 * subsequence after the specified pivot point. Returns <code>null</code> 
	 * if no valid <code>Chromosome</code> could be produced by rotation at 
	 * the given pivot. 
	 * @param chromosome 
	 * @param pivot
	 * @return a new, mutated <code>Chromosome</code> or <code>null</code>
	 */ 
	public static Chromosome mutate(Chromosome chromosome, int pivot) {
		// Get the subsection to work with. 
		Location[] bottom = Arrays.copyOfRange(chromosome.locations, pivot + 1, 
											   chromosome.locations.length); 
		
		// Determine the possible directions for the mutation to bend. 
		int previousDirection = Chromosome.findPreviousDirection(chromosome, pivot); 
		int[] availableDirections = Chromosome.moveMap.get(previousDirection); 
		boolean[] wasTried = {false, false, false}; 	// Track failed attempts. 
		int numberAttempted = 0; 
		
		// Keep trying the mutation while there are still untried directions. 
		while (numberAttempted < 3) { 
			int direction = Chromosome.random.nextInt(3); 
			
			// Ensure direction wasn't previously tried. If so, try again. 
			if (wasTried[direction]) { continue; } 
			
			// Untried direction generated. Shift and rotate bottom of protein. 
			wasTried[direction] = true; 
			numberAttempted++; 
			// Find the new starting point for the bottom. 
			Location origin = Location.createNextLocation(
					chromosome.locations[pivot], direction);  
			Location[] newBottom = Chromosome.shiftAndRotate(bottom, 
					previousDirection, direction, origin); 
			
			// Build a new full sequence. 
			Location[] newFull = new Location[chromosome.locations.length]; 
			for (int i = 0; i < newFull.length; i++) {
				if (i <= pivot) {
					newFull[i] = chromosome.locations[i];
				} else {
					newFull[i] = newBottom[i - (pivot + 1)];
				}
			} 
			
			// Validate full sequence and return if valid. 
			if (Chromosome.validate(newFull)) {
				int newFitness = Chromosome.computeFitness(chromosome.acids, 
														   newFull); 
				return new Chromosome(chromosome.acids, newFull, newFitness); 
			}
		}
		
		// Could not generate a valid mutation, return null to caller. 
		return null; 
	} 
	
	
	/**
	 * Given a <code>Chromosome</code> and a point in the structure, 
	 * determines what the last direction of amino acid addition was in the 
	 * structure and returns that direction. 
	 * @param protein the <code>Chromosome</code> under consideration
	 * @param point the desired end of an amino acid subsequence 
	 * @return the direction of growth leading to <code>point</code> 
	 */ 
	private static int findPreviousDirection(Chromosome protein, int point) {
		Location current = protein.locations[point]; 
		Location previous = protein.locations[point - 1]; 
		
		// Find location delta. 
		int xShift = current.getX() - previous.getX(); 
		int yShift = current.getY() - previous.getY(); 
		
		// Parse the position change. 
		if (xShift == 1) {
			return 1; 
		} else if (xShift == -1) {
			return 2; 
		} else if (yShift == 1) {
			return 3; 
		} else {
			return 4; 
		}
	}
	
	
	/**
	 * Given a subsequence of a <code>Chromosome</code> performs a rotation 
	 * in the given direction on that subsequence and aligns it to begin at 
	 * the specified starting <code>Location</code>. 
	 * @param original the subsequence of a protein to rotate and shift 
	 * @param oldDirection the direction the subsequence was originally pointing 
	 * @param newDirection the new starting direction of the subsequence 
	 * @param newOrigin the <code>Location</code> where the rotated subsequence should begin 
	 * @return a newly rotated version of the protein subsequence 
	 */ 
	private static Location[] shiftAndRotate(Location[] original, 
			int oldDirection, int newDirection, Location newOrigin) {
		Location[] shiftedToZero = Chromosome.shiftToZero(original); 
		Location[] rotated = Chromosome.rotate(shiftedToZero, oldDirection, 
											   newDirection); 
		Location[] shifted = Chromosome.shiftTo(original, newOrigin); 
		return shifted; 
	} 
	
	
	/**
	 * Moves a <code>Location[]</code> to begin at the origin. 
	 * @param original the array to shift
	 * @return a new <code>Location[]</code> that is <code>original</code> shifted to (0, 0)
	 */ 
	private static Location[] shiftToZero(Location[] original) {
		return shiftTo(original, new Location(0, 0)); 
	} 
	
	
	/**
	 * Shifts a subsequence of a <code>Chromosome</code> to start at the 
	 * provided new <code>Location</code>. 
	 * @param original the array of locations to shift 
	 * @param destination a location matching the new start of the array
	 * @return a shift of <code>original</code> to the desired starting point 
	 */
	private static Location[] shiftTo(Location[] original, Location destination) {
		Location[] shifted = new Location[original.length]; 
		int xOffset = destination.getX() - original[0].getX(); 
		int yOffset = destination.getY() - original[0].getY(); 
		
		// Add offsets to all Locations. 
		for (int i = 0; i < original.length; i++) {
			shifted[i] = new Location(original[i].getX() + xOffset, 
									  original[i].getY() + yOffset); 
		}
		
		return shifted; 		
	}


	/**
	 * Rotates a subsequence of a <code>Chromosome</code> to proceed in the 
	 * specified direction given its previous orientation. 
	 * @param original the <code>Location[]</code> to rotate, shifted to the origin
	 * @param oldDir the direction of <code>original</code>
	 * @param newDir the direction to rotate the <code>Location[]</code>
	 * @return a new <code>Location[]</code> that is the original rotated in the specified direction
	 */ 
	private static Location[] rotate(Location[] original, int oldDir, int newDir) {
		boolean flipXY = false; 
		boolean negateX = false; 
		boolean negateY = false; 
		
		// Each change of direction requires a different set of transforms. 
		if (oldDir == 1) {
			if (newDir == 2) {
				negateX = true; 
				negateY = true; 
			} else if (newDir == 3) {
				flipXY = true; 
				negateX = true; 
			} else {
				flipXY = true; 
				negateY = true; 
			}
		} else if (oldDir == 2) {
			if (newDir == 1) {
				negateX = true; 
				negateY = true; 
			} else if (newDir == 3) {
				flipXY = true; 
				negateY = true; 
			} else { 
				flipXY = true; 
				negateX = true; 
			} 
		} else if (oldDir == 3) {
			if (newDir == 1) {
				flipXY = true; 
				negateY = true; 
			} else if (newDir == 2) {
				flipXY = true; 
				negateX = true; 
			} else {
				negateX = true; 
				negateY = true; 
			} 
		} else {
			if (newDir == 1) {
				flipXY = true; 
				negateX = true; 
			} else if (newDir == 2) {
				flipXY = true; 
				negateY = true; 
			} else {
				negateX = true; 
				negateY = true; 
			}
		} 
		
		return mathematicallyRotate(original, flipXY, negateX, negateY); 
	} 
	
	
	/**
	 * Performs the mathematical transforms needed to rotate a subsequence 
	 * of a <code>Chromosome</code> and returns a new instance that is the 
	 * provided subsequence manipulated accordingly. 
	 * @param original the subsequence to transform
	 * @param flipXY whether to switch the x- and y-values of each coordinate 
	 * @param negateX whether to negate the x-value of each coordinate 
	 * @param negateY whether to negate the y-value of each coordinate 
	 * @return a new, transformed subsequence 
	 */ 
	private static Location[] mathematicallyRotate(Location[] original, 
			boolean flipXY, boolean negateX, boolean negateY) {
		Location[] rotated = new Location[original.length]; 
		
		// Create the new coordinates and perform the desired transforms on them. 
		for (int i = 0; i < original.length; i++) {
			int x; 
			int y; 
			
			if (flipXY) {
				x = original[i].getY(); 
				y = original[i].getX(); 
			} else {
				x = original[i].getX(); 
				y = original[i].getY(); 
			}
			
			if (negateX) { x *= -1; } 
			if (negateY) { y *= -1; } 
			
			rotated[i] = new Location(x, y); 
		}

		return rotated; 
	}
	
	
	/**
	 * Helper method used to initialize the positions for a newly generated 
	 * <code>Chromosome</code> instance. This method is <code>static</code> 
	 * because it is called from the constructor of <code>Chromosome</code> 
	 * and is <code>private</code> because this is the only legitimate use 
	 * of the method. The returned configuration is a self-avoiding-walk. 
	 * @param size the length of the amino acid sequence being built
	 * @return a possible configuration of the amino acids. 
	 */ 
	private static Location[] generateRandomLocations(int size) {
		Location[] structure = null; 
		boolean isValid = false; 
		
		while (!isValid) {
			structure = new Location[size]; 
			
			// First two locations are always the same. 
			structure[0] = new Location(0, 0); 
			structure[1] = new Location(1, 0); 
			int previousDir = 1; 
			
			for (int i = 2; i < size; i++) {
				int nextDir = moveMap.get(previousDir)[random.nextInt(3)]; 
				structure[i] = Location.createNextLocation(structure[i - 1], 
															 nextDir); 
				previousDir = nextDir; 
			}
						
			isValid = Chromosome.validate(structure); 
		}
		
		return structure; 
	}
	
}
