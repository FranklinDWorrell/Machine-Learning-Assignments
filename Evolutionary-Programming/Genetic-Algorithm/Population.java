import java.util.Observable;

/**
 * Adds a level of abstraction that assists in running the search. Creates
 * initial generation and creates a new generation given a previous one.
 * Implements the main search loop by spawning successive generations until
 * the target fitness for the search is reached.
 *
 * @author Franklin D. Worrell
 * @version 16 September 2018
 */
public class Population extends Observable {
	
    /**
     * How many individuals each <code>Generation</code> should contain
     * in total.
     */
    public static final int POP_SIZE = 500;

    /**
     * How many of the fittest individuals should be copied from one
     * generation to the next.
     */
    public static final int ELITE_SIZE = POP_SIZE / 20;

    /**
     * The number of individuals who will be produced by "breeding"
     * pairs of individuals from the previous generation.
     */
    public static final int CROSSOVER_SIZE = POP_SIZE - ELITE_SIZE - POP_SIZE / 5;

    /**
     * The number of individuals to randomly mutate in a given
     * <code>Generation</code>. Double-point mutation is applied
     * if enough subsequent <code>Generations</code> fail to produce
     * an improvement in protein fitness.
     */
    public static final int MUTATION_NUMBER = POP_SIZE / 4;

    /**
     * The current <code>Generation</code> that is used to produce
     * a subsequent <code>Generation</code>.
     */
    private Generation currentGeneration;

    /**
     * The next <code>Generation</code> of individuals built by
     * copying, breeding, and manipulating individuals from the
     * <code>currentGeneration</code>.
     */
    private Generation nextGeneration;

    /**
     * The amino acid sequence that each individual of any
     * <code>Generation</code> will be built of.
     */
    private String acidString;

    /**
     * The fitness sought for this protein in this search.
     */
    private int targetFitness;


    /**
     * Given a <code>String</code> of amino acids, builds a new
     * <code>Population</code> instance with a randomly generated
     * initial population that will continue to evolve until the
     * provided target fitness is reached.
     *
     * @param acidString the sequence of amino acids compromising the protein
     * @param targetFitness the goal fitness for the search being performed
     */
    public Population(String acidString, int targetFitness) {
        this.currentGeneration = Generation.getRandomGeneration(acidString);
        this.acidString = acidString;
        this.targetFitness = targetFitness;
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
     * Returns the target fitness that this <code>Population</code>
     * is being used to search for.
     *
     * @return the target fitness for this search
     */
    public int getTargetFitness() {
        return this.targetFitness;
    }


    /**
     * Implements the main loop of the genetic algorithm. Generates
     * successive generations until a solution is reached.
     */
    public void evolve() {
        Chromosome currentBest = this.currentGeneration.getBest();
        int currentFitness = currentBest.getFitness();
        // The number of Generations of the protein bred thus far.
        int numberOfGenerations = 0;
        // The last Generation that showed improved fitness.
        int lastImprovement = 0;

        // Create successive generations until target fitness reached.
        while (currentFitness > Population.this.targetFitness) {
            numberOfGenerations++;
            // Introduce double-point mutation after stretches without improvement.
            if ((numberOfGenerations - lastImprovement) >= 150) {
                this.nextGeneration = this.currentGeneration.produceNextGeneration(true);
            } else {
                this.nextGeneration = this.currentGeneration.produceNextGeneration(false);
            }

            // Check the results of producing a subsequent generation.
            currentBest = this.nextGeneration.getBest();
            if (currentBest.getFitness() < currentFitness) {
                lastImprovement = numberOfGenerations;
                // Update GUI upon improvement.
                setChanged();
                notifyObservers(new Message(new Chromosome(currentBest), numberOfGenerations));
            }

            // Update the generations and print tracking date to terminal.
            this.currentGeneration = this.nextGeneration;
            currentFitness = currentBest.getFitness();
            System.out.println("Generation " + numberOfGenerations + '\t' +
                    this.nextGeneration.reportFittestAndVolume());
        }
    }

}
