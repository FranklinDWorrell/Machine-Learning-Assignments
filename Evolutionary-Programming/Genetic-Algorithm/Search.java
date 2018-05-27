import java.util.Observer; 

/**
 * Implements ...
 * @author Franklin D. Worrell
 * @version 27 May 2018
 */ 
public class Search implements Runnable {
	private String acidString; 
	private int targetFitness; 
	private Observer observer; 
	
	/**
	 * 
	 * @param acidString the String who fitness is sought
	 * @param targetFitness the target fitness of the protein to find 
	 * @param observer the <code>Observer</code> interested in results
	 */ 
	public Search (String acidString, int targetFitness, Observer observer) {
		this.acidString = acidString; 
		this.targetFitness = targetFitness; 
		this.observer = observer; 
	}
	
	
	@Override
	public void run() {
		Population population = Population.getInitialPopulation(
				this.acidString, this.targetFitness);
		population.addObserver(this.observer); 
		population.evolve(); 
	} 
}