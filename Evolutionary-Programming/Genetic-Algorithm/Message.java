/**
 * A message that the <code>Observable</code> can pass to the registered
 * <code>Observer</code> that contains a <code>Chromosome</code> and the
 * generation of that member of the <code>Population</code>.
 *
 * @author Franklin D. Worrell
 * @version 27 May 2018
 */
public class Message {
    private Chromosome chromosome;
    private int generation;

    /**
     * Builds a new <code>Message</code> with the provided population member
     * and generation number.
     *
     * @param chromosome the <code>Chromosome</code> to draw
     * @param generation the generation that produced the <code>Chromosome</code>
     */
    public Message(Chromosome chromosome, int generation) {
        this.chromosome = chromosome;
        this.generation = generation;
    }

    /**
     * Returns the <code>Chromosome</code> packed in this <code>Message</code>.
     *
     * @return the <code>Chromosome for transmission to the GUI
     */
    public Chromosome getChromosome() {
        return this.chromosome;
    }

    /**
     * Returns the generation associated with the <code>Chromosome</code>
     * packed in this <code>Message</code>.
     *
     * @return the generation of the associated <code>Chromosome</code>
     */
    public int getGeneration() {
        return this.generation;
    }

}
