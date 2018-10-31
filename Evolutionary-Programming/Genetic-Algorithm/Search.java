import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Models a search for a protein achieving a target fitness for a given
 * amino acid sequence. The search is performed in a separate thread to
 * facilitate the updating of the GUI.
 *
 * @author Franklin D. Worrell
 * @version 16 September 2018
 */
public class Search {

    /**
     * The <code>String</code> of amino acids in order that will comprise
     * the protein being sought. String should be specified as a sequence
     * of <code>h</code>s (or <code>H</code>s) and <code>p</code>s (or
     * <code>P</code>s) for hydrophobic and hydrophilic amino acids,
     * respectively.
     */
    private String acidString;

    /**
     * The goal fitness for the protein. The search will be halted as
     * soon as this fitness is reached. It should be specified as a
     * negative integer and represents the number of adjacencies
     * between noncovalent hydrophobic amino acids.
     */
    private int targetFitness;

    /**
     * The GUI class that needs to be updated whenever a new individual
     * more closely approximating the target fitness is bred during the
     * search.
     */
    private Observer observer;

    /**
     * The <code>ExecutorService</code> that will manage the execution
     * of the thread that this search takes place in.
     */
    private ExecutorService thread;

    /**
     * Constructs and begins a new search for a protein of the
     * desired amino acid sequence and the specified target fitness
     * in a new, separate thread. The specified <code>Observer</code>
     * will be updated when a new, fitter individual is found during
     * the course of the search.
     *
     * @param acidString the amino acid sequence who fitness is sought
     * @param targetFitness the target fitness of the protein to find
     * @param observer the <code>Observer</code> interested in results
     */
    public Search (String acidString, int targetFitness, Observer observer) {
        this.acidString = acidString;
        this.targetFitness = targetFitness;
        this.observer = observer;
        this.thread = Executors.newSingleThreadExecutor();
        this.thread.execute(() -> {
            Population population = new Population(this.acidString, this.targetFitness);
            population.addObserver(this.observer);
            population.evolve();
        });
    }

}