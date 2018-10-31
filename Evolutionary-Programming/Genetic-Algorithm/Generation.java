import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

/**
 * A single generation of the <code>Population</code>.
 *
 * @author Franklin D. Worrell
 * @version 16 September 2018
 */
public class Generation {

    /**
     * A random generator used in producing mutations and
     * selecting breeding pairs.
     */
    private static Random random = new Random();

    /**
     * The individuals comprising this <code>Generation</code> of
     * the <code>Population</code>.
     */
    private Chromosome[] chromosomes;

    /**
     * The sequence of amino acids that is the search subject.
     */
    private String acidString;

    /**
     * A map from a protein fitness (a negative <code>Integer</code>)
     * to an array of two <code>ints</code> that represent the beginning
     * and ending indices of the <code>Chromosomes</code> in the
     * <code>Generation's</code> array of <code>Chromosome</code>.
     * Used in selecting breeding pairs with a degree of randomness
     * while using a specified fitness.
     */
    private TreeMap<Integer, int[]> fitnessMap;

    /**
     * The sum of each distinct fitness found in a generation. This
     * negative number is used in Roulette-Wheel Selection.
     */
    private int sumOfFitnesses;


    /**
     * Given a <code>String</code> of amino acids, builds a new, empty
     * <code>Population</code> instance.
     *
     * @param acidString the sequence of amino acids compromising the protein
     */
    public Generation(String acidString) {
        this.chromosomes = new Chromosome[Population.POP_SIZE];
        this.acidString = acidString;
        this.fitnessMap = new TreeMap<>();
        this.sumOfFitnesses = 0;
    }


    /**
     * Returns the amino acid sequence as a human readable <code>String</code>
     * for the proteins in this <code>Population</code>.
     *
     * @return the amino acid sequence comprising the proteins
     */
    public String getAcidString() {
        return this.acidString;
    }


    /**
     * Returns an unordered <code>Set</code> of the fitnesses of the
     * <code>Chromosome</code> instances making up this <code>Population</code>.
     *
     * @return a <code>Set</code> of the fitnesses
     */
    public Set<Integer> getFitnesses() {
        return this.fitnessMap.keySet();
    }


    /**
     * Reports the best fitness score of this generation and the volume
     * of the population with that fitness. Used by <code>ChromosomeGUI</code>
     * to print progress of algorithm to the terminal as the search progresses.
     *
     * @return a <code>String</code> reporting the highest fitness and the number with it
     */
    public String reportFittestAndVolume() {
        String report = "";
        int bestFitness = this.getBest().getFitness();
        int[] indicesOfBest = this.fitnessMap.get(bestFitness);
        int volumeAtBest = indicesOfBest[1] - indicesOfBest[0] + 1;
        report += bestFitness + ": " + volumeAtBest + " / " + this.chromosomes.length;
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

        for (Chromosome chromosome : this.chromosomes) {
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
     *
     * @param desiredFitness the fitness the <code>Chromosome</code> should have
     * @return a randomly selected <code>Chromosome</code> with the desired fitness
     */
    private Chromosome getChromosomeWithFitness(int desiredFitness) {
        int[] range = this.fitnessMap.get(desiredFitness);
        int bound = range[1] - range[0];
        int index = (bound > 0) ? this.random.nextInt(bound) + range[0] : range[0];
        return this.chromosomes[index];
    }


    /**
     * Given an index and a <code>Chromosome</code>, sets the instance in the
     * underlying <code>ArrayList</code> to the one provided as an argument.
     * Used for in-place changes for mutation operation.
     *
     * @param index the place in the <code>ArrayList</code> to change the value
     * @param chromosome the new instance to place in the <code>ArrayList</code>
     */
    public void set(int index, Chromosome chromosome) {
        this.chromosomes[index] = chromosome;
    }


    /**
     * Returns the <code>Chromosome</code> at the given index in the
     * <code>Population</code>.
     *
     * @param index the index of the desired <code>Chromosome</code>
     * @return the <code>Chromosome</code> at <code>index</code>
     */
    public Chromosome get(int index) {
        return this.chromosomes[index];
    }


    /**
     * Returns the fittest Chromosome in this <code>Population</code>
     * instance.
     *
     * @return the fittest individual of the generation
     */
    public Chromosome getBest() {
        return this.chromosomes[0];
    }


    /**
     * Sorts the <code>Population</code> according to the fitness of each
     * <code>Chromosome</code> instance.
     */
    public void sort() {
        Arrays.parallelSort(this.chromosomes,
                Comparator.comparing(Chromosome::getFitness));
    }


    /**
     * Returns a <code>String</code> representation of the
     * <code>Population</code> with each Chromosome represented on its own
     * line.
     *
     * @return a <code>String</code> representing <code>this Population</code>
     */
    @Override
    public String toString() {
        String generationString = "";

        for (int i = 0; i < this.chromosomes.length; i++ ) {
            generationString += this.chromosomes[i].toString() + '\n';
        }

        return generationString;
    }


    /**
     * Creates a population with the given amino acid sequence each member
     * of which has a randomly generated structure.
     *
     * @param acidString the amino acid sequence for the proteins
     * @return a full protein population with randomly generated structures
     */
    public static Generation getRandomGeneration(String acidString) {
        Generation generation = new Generation(acidString);

        // Populate the first generation with entirely random instances.
        for (int i = 0; i < Population.POP_SIZE; i++) {
            generation.set(i, new Chromosome(acidString));
        }

        // Calculate the needed population statistics.
        generation.sort();
        generation.setFitnessMapAndSumOfFitnesses();
        return generation;
    }


    /**
     * Given a population, generates the next generation by retaining an
     * elite percentage, crossing over a set percentage, and randomly
     * generating the remainder.
     *
     * @param applyDoublePointMutation whether or not to perform multi-point mutations
     */
    public Generation produceNextGeneration(boolean applyDoublePointMutation) {
        Generation nextGeneration = new Generation(this.acidString);

        // Number of Chromosomes generated so for for the next generation.
        int individualsBred = 0;

        // Transfer elites and crossover pool to next generation.
        while (individualsBred < Population.ELITE_SIZE) {
            nextGeneration.set(individualsBred, this.get(individualsBred));
            individualsBred++;
        }

        nextGeneration.performAllCrossovers(this);
        individualsBred += Population.CROSSOVER_SIZE;

        // Generate random remaining.
        while (individualsBred < Population.POP_SIZE) {
            nextGeneration.set(individualsBred, new Chromosome(this.getAcidString()));
            individualsBred++;
        }

        nextGeneration.performAllMutations(applyDoublePointMutation);

        // Update the population and sort the new generation for processing.
        nextGeneration.sort();
        nextGeneration.setFitnessMapAndSumOfFitnesses();
        return nextGeneration;
    }


    /**
     * Returns the fitnesses of two <code>Chromosome</code> instances in this
     * <code>Population</code> to apply crossover to. Utilizes the Roulette
     * Wheel selection procedure. The particular <code>Chromosome</code>
     * instances that will be used in the crossover are picked by the
     * <code>getChromosomeWithFitness</code>.
     *
     * @return an <code>int[]</code> containing the fitnesses of two chromosomes
     */
    private MatingPair spinRouletteWheel() {
        int leftFitness = 0;
        int rightFitness = 0;
        int first = this.random.nextInt(Math.abs(this.sumOfFitnesses)) + 1;
        int second = this.random.nextInt(Math.abs(this.sumOfFitnesses)) + 1;

        // Get an ordered list of fitness to subtract from the random numbers.
        ArrayList<Integer> sortedFitnesses = new ArrayList<>(
                this.fitnessMap.keySet());

        // Pick the fitness of the first chromosome to crossover.
        for (Integer fitness : this.fitnessMap.keySet()) {
            first += fitness; 	// Fitnesses are stored as negative integers.
            if (first <= 0) {	// Once the random is reduced to zero, pick.
                leftFitness = fitness;
                break;
            }
        }

        // Pick the fitness of the second chromosome to crossover.
        for (Integer fitness : this.fitnessMap.keySet()) {
            second += fitness; 	// Fitnesses are stored as negative integers.
            if (second <= 0) { 	// Once the random is reduced to zero, pick.
                rightFitness = fitness;
                break;
            }
        }

        return new MatingPair(this.getChromosomeWithFitness(leftFitness),
                this.getChromosomeWithFitness(rightFitness));
    }


    /**
     * Produces two new individuals in a <code>Generation</code>
     * that are the results of "breeding" two individuals from the
     * previous <code>Generation</code>. The ends of the bred
     * <code>Chromosomes</code> are switched at a random pivot in
     * the next <code>Generation</code>.
     *
     * @param previousGeneration the <code>Generation</code> breeding pairs are selected from
     */
    private void performAllCrossovers(Generation previousGeneration) {
        int crossed = 0;
        while (crossed < Population.CROSSOVER_SIZE) {
            MatingPair toMate = previousGeneration.spinRouletteWheel();
            int pivot = random.nextInt(this.acidString.length() - 2) + 1;
            Chromosome left = toMate.getLeft();
            Chromosome right = toMate.getRight();
            Chromosome newLeft = Chromosome.crossover(left, right, pivot);
            Chromosome newRight = Chromosome.crossover(right, left, pivot);

            // If crossover created valid proteins, update the new generation.
            if (newLeft != null && newRight != null) {
                this.set(Population.ELITE_SIZE + crossed, newLeft);
                this.set(Population.ELITE_SIZE + crossed + 1, newRight);
                crossed +=2;
            }
        }
    }


    /**
     * Performs a mutation on the number of <code>Chromosomes</code>
     * specified in the population. A <code>Chromosome</code> is
     * mutated by bending it at a random point. If double-point mutation
     * is applied, the <code>Chromosome</code> is bent at two random
     * points.
     *
     * @param applyDoublePointMutation whether or not to apply double-point mutation
     */
    private void performAllMutations(boolean applyDoublePointMutation) {
        int mutated = 0;
        while (mutated < Population.MUTATION_NUMBER) {
            // Do not mutate elite or new randomly generated Chromosomes.
            int toMutate = random.nextInt(Population.CROSSOVER_SIZE) + Population.ELITE_SIZE;
            int pivot = random.nextInt(this.acidString.length() - 2) + 1;
            Chromosome afterMutation = Chromosome.mutate(this.get(toMutate), pivot);

            // Apply mutation at a second point in the Chromosome if specified.
            if (afterMutation != null && applyDoublePointMutation) {
                int secondPivot = random.nextInt(this.acidString.length() - 2) + 1;
                afterMutation = Chromosome.mutate(afterMutation, secondPivot);
            }

            // Make sure the mutation(s) produced a valid result and add.
            if (afterMutation != null) {
                this.set(toMutate, afterMutation);
                mutated++;
            }
        }
    }

    private synchronized void threadSafeAdd(Chromosome chromosome, int index) {
        // Before advancing, check again to make sure the count hasn't been met.
        // if (
    }
}