import java.util.ArrayList; 
import java.util.Comparator; 
import java.util.Observable; 
import java.util.TreeMap; 
import java.util.Random; 
import java.util.Set; 
import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 

/**
 * Adds a level of abstraction that assists in running the search. Creates 
 * initial generation and creates a new generation given a previous one. 
 * Implements population-level functions like crossover and mutation. 
 * @author Franklin D. Worrell
 * @version 4 March 2018
 */ 
public class Population extends Observable {
	private static final int POP_SIZE = 200; 
	private static final int ELITE_SIZE = 10; 
	private static final int CROSSOVER_SIZE = 160; 
	private static final int FILL_SIZE = 30; 
	private static final int MUTATION_NUMBER = 50; 
	
	private static Random random = new Random(); 
	
	private ArrayList<Chromosome> list; 
	private String acidString; 
	private int targetFitness; 
	private TreeMap<Integer, int[]> fitnessMap; 
	private int sumOfFitnesses; 
	
	
	/**
	 * Given a <code>String</code> of amino acids, builds a new, empty 
	 * <code>Population</code> instance. 
	 * @param acidString the sequence of amino acids compromising the protein 
	 */ 
	public Population(String acidString, int targetFitness) {
		this.list = new ArrayList<>(POP_SIZE); 
		this.acidString = acidString; 
		this.targetFitness = targetFitness; 
		this.fitnessMap = new TreeMap<>(); 
		this.sumOfFitnesses = 0; 
	} 

	
	/**
	 * Returns the amino acid sequence as a human readable <code>String</code> 
	 * for the proteins in this <code>Population</code>. 
	 * @return the amino acid sequence comprising the proteins
	 */ 
	public String getAcidString() {
		return this.acidString; 
	} 
	
	
	/**
	 * Returns an unordered <code>Set</code> of the fitnesses of the 
	 * <code>Chromosome</code> instances making up this <code>Population</code> 
	 * @return a <code>Set</code> of the fitnesses 
	 */ 
	public Set<Integer> getFitnesses() {
		return this.fitnessMap.keySet(); 
	} 
	
	
	/**
	 * Reports the best fitness score of this generation and the volume 
	 * of the population with that fitness. Used by <code>ChromosomeGUI</code> 
	 * to print progress of algorithm to the terminal as the search progresses. 
	 * @return a <code>String</code> reporting the highest fitness and the number with it
	 */ 
	public String reportFittestAndVolume() {
		String report = ""; 
		int bestFitness = this.getBest().getFitness(); 
		int[] indicesOfBest = this.fitnessMap.get(bestFitness); 
		int volumeAtBest = indicesOfBest[1] - indicesOfBest[0] + 1; 
		report += bestFitness + ": " + volumeAtBest + " / " + this.list.size(); 
		return report; 
	}
	
	
	/**
	 * Builds the map associating fitness with indices into the underlying 
	 * <code>ArrayList</code>. Additionally, it computes and stores the sum of 
	 * the fitnesses of the <code>Chromosome</code> instances. The 
	 * <code>ArrayList</code> <b>must be sorted</b> for this method to function 
	 * properly. 
	 */ 
	private void setFitnessMapAndSumOfFitnesses() {
		int currentFitness = this.getBest().getFitness(); 
		int start = 0; 
		int count = 0; 
		// Clear the fitness map for updates between generations. 
		this.fitnessMap.clear(); 
		
		for (Chromosome chromosome : this.list) {
			// No change in current fitness, just increment count. 
			if (currentFitness == chromosome.getFitness()) {
				count++; 
			} 
			
			// Fitness changed, so add the values to the map and continue. 
			else { 
				this.sumOfFitnesses += currentFitness; 
				this.fitnessMap.put(currentFitness, 
									new int[] {start, start + count - 1}); 
				currentFitness = chromosome.getFitness(); 
				start += count; 
				count = 0; 
			}
		}
		
		// Last fitness range not added, so add the current values to the map. 
		this.sumOfFitnesses += currentFitness; 
		this.fitnessMap.put(currentFitness, new int[] {start, start + count - 1}); 
	} 
	
	
	/**
	 * Returns a randomly selected <code>Chromosome</code> of the 
	 * <code>Population</code> with the requested fitness. Used for performing 
	 * the crossover operation. 
	 * @param desiredFitness the fitness the <code>Chromosome</code> should have 
	 * @return a randomly selected <code>Chromosome</code> with the desired fitness
	 */ 
	private Chromosome getChromosomeWithFitness(int desiredFitness) {
		int[] range = this.fitnessMap.get(desiredFitness); 
		int bound = range[1] - range[0]; 
		int index = (bound > 0) ? Population.random.nextInt(bound) + range[0] : range[0]; 
		return this.list.get(index); 
	} 

	
	/**
	 * Adds the provided <code>Chromosome</code> to the end of the 
	 * <code>Population</code>. The <code>Population</code> becomes unsorted 
	 * when this method is called. 
	 * @param chromosome the <code>Chromosome</code> to add 
	 */ 
	public void add(Chromosome chromosome) {
		this.list.add(chromosome); 
	} 

	
	/**
	 * Given an index and a <code>Chromosome</code>, sets the instance in the 
	 * underlying <code>ArrayList</code> to the one provided as an argument. 
	 * Used for in-place changes for mutation operation. 
	 * @param index the place in the <code>ArrayList</code> to change the value 
	 * @param chromosome the new instance to place in the <code>ArrayList</code> 
	 */ 
	public void set(int index, Chromosome chromosome) {
		this.list.set(index, chromosome); 
	} 

	
	/**
	 * Returns the <code>Chromosome</code> at the given index in the 
	 * <code>Population</code> 
	 * @param the index of the desired <code>Chromosome</code> 
	 * @return the <code>Chromosome</code> at <code>index</code> 
	 */ 
	public Chromosome get(int index) {
		return this.list.get(index); 
	} 
	
	
	/**
	 * Returns the fittest Chromosome in this <code>Population</code> 
	 * instance. 
	 * @return the fittest individual of the generation 
	 */ 
	public Chromosome getBest() {
		return this.list.get(0);
	} 
	
	
	/**
	 * Sorts the <code>Population</code> according to the fitness of each 
	 * <code>Chromosome</code> instance. 
	 */ 
	public void sort() {
		this.list.sort((o1, o2) -> o1.getFitness() - o2.getFitness()); 
	}
	
	
	/**
	 * Returns a <code>String</code> representation of the 
	 * <code>Population</code> with each Chromosome represented on its own 
	 * line. 
	 * @return a <code>String</code> representing <code>this Population</code>
	 */ 
	@Override
	public String toString() {
		String popString = ""; 
		
		for (int i = 0; i < this.list.size(); i++ ) {
			popString += this.list.get(i).toString() + '\n'; 
		} 
		
		return popString; 
	}


	/**
	 * Creates a population with the given amino acid sequence each member 
	 * of which has a randomly generated structure. 
	 * @param acidString the amino acid sequence for the proteins 
	 * @return a full protein population with randomly generated structures 
	 */ 
	public static Population getInitialPopulation(String acidString, 
												  int targetFitness) {
		Population initial = new Population(acidString, targetFitness); 
		
		// Populate the first generation with entirely random instances. 
		for (int i = 0; i < POP_SIZE; i++) {
			initial.add(new Chromosome(acidString)); 
		} 
		
		// Calculate the needed population statistics. 
		initial.sort();
		initial.setFitnessMapAndSumOfFitnesses(); 
		return initial; 
	} 
	
	
	/**
	 * Implements the main loop of the genetic algorithm. Given a target to 
	 * search for, it generates successive generations until a solution is
	 * reached. 
	 * @param targetFitness the desired <code>Chromosome</code> fitness
	 */ 
	public void evolve() {
		// Spawning a new thread permits display of intermediate results in GUI. 
		ExecutorService pool = Executors.newSingleThreadExecutor(); 
		pool.execute(new Runnable() {
			/**
			 * The main loop of the genetic algorithm runs in a separate thread. 
			 */ 
			@Override
			public void run() {
				Chromosome currentBest = Population.this.getBest(); 
				int currentFitness = currentBest.getFitness(); 
				int iteration = 0; 	// the generation of the protein
										
				// Create successive generations until target fitness reached. 
				while (currentFitness > Population.this.targetFitness) {
					iteration++; 
					Population.this.produceNextGeneration(); 
					currentBest = Population.this.getBest(); 
					if (currentBest.getFitness() < currentFitness) {
						setChanged(); 
						notifyObservers(new Chromosome(currentBest)); 
					} 
					currentFitness = currentBest.getFitness(); 
					System.out.println("Generation " + iteration + '\t' + 
									   Population.this.reportFittestAndVolume()); 
				}
			}
		}); 
	}


	/**
	 * Given a population, generates the next generation by retaining an 
	 * elite percentage, crossing over a set percentage, and randomly 
	 * generating the remainder. 
	 * @param old the prior generation of <code>Chromosome</code>s
	 * @return the next generation of <code>Chromosome</code>s
	 */ 
	public void produceNextGeneration() {
		ArrayList<Chromosome> newList = new ArrayList<>(); 
		
		// Transfer elites and crossover pool to next generation. 
		for (int i = 0; i < ELITE_SIZE; i++) {
			newList.add(this.get(i)); 
		}
		
		// Compute crossovers in crossover pool. 
		int crossed = 0; 
		while (crossed < CROSSOVER_SIZE) { 
			int leftAndRight[] = this.spinRouletteWheel();
			int pivot = random.nextInt(this.acidString.length() - 2) + 1; 
			Chromosome left = this.getChromosomeWithFitness(leftAndRight[0]); 
			Chromosome right = this.getChromosomeWithFitness(leftAndRight[1]); 
			Chromosome newLeft = Chromosome.crossover(left, right, pivot); 
			Chromosome newRight = Chromosome.crossover(right, left, pivot); 
			
			// If crossover created valid proteins, update the new generation. 
			if (newLeft != null && newRight != null) {
				newList.add(newLeft); 
				newList.add(newRight); 
				crossed += 2; 
			}
		}
		
		// Generate random remaining. 
		for (int i = 0; i < FILL_SIZE; i++) {
			newList.add(new Chromosome(this.getAcidString())); 
		} 
		
		// Apply mutations to non-elites. 
		int mutated = 0; 
		while (mutated < MUTATION_NUMBER) {
			// Do not mutate elite or new randomly generated Chromosomes. 
			int toMutate = random.nextInt(CROSSOVER_SIZE) + ELITE_SIZE; 
			int pivot = random.nextInt(this.acidString.length() - 2) + 1;
			Chromosome afterMutation = Chromosome.mutate(newList.get(toMutate), pivot); 
			if (afterMutation != null) {
				newList.set(toMutate, afterMutation); 
				mutated++; 
			}
		} 
		
		// Update the population and sort the new generation for processing. 
		this.list = newList; 
		this.sort(); 
		this.setFitnessMapAndSumOfFitnesses(); 
	}
	
	
	/**
	 * Returns the fitnesses of two <code>Chromosome</code> instances in this 
	 * <code>Population</code> to apply crossover to. Utilizes the Roulette 
	 * Wheel selection procedure. The particular <code>Chromosome</code>
	 * instances that will be used in the crossover are picked by the 
	 * <code>getChromosomeWithFitness</code>. 
	 * @return an <code>int[]</code> containing the fitnesses of two chromosomes 
	 */ 
	private int[] spinRouletteWheel() {
		int[] toCross = {0, 0}; 
		int first = Population.random.nextInt(Math.abs(this.sumOfFitnesses)) + 1; 
		int second = Population.random.nextInt(Math.abs(this.sumOfFitnesses)) + 1; 

		// Get an ordered list of fitness to subtract from the random numbers. 
		ArrayList<Integer> sortedFitnesses = new ArrayList<>(
				this.fitnessMap.keySet()); 

		// Pick the fitness of the first chromosome to crossover. 
		for (Integer fitness : this.fitnessMap.keySet()) {
			first += fitness; 	// Fitnesses are stored as negative integers. 
			if (first <= 0) {	// Once the random is reduced to zero, pick. 
				toCross[0] = fitness; 
				break; 
			}
		}
		
		// Pick the fitness of the second chromosome to crossover. 
		for (Integer fitness : this.fitnessMap.keySet()) {
			second += fitness; 	// Fitnesses are stored as negative integers. 
			if (second <= 0) { 	// Once the random is reduced to zero, pick. 
				toCross[1] = fitness; 
				break; 
			} 
		}
		
		return toCross;
	} 

} 
