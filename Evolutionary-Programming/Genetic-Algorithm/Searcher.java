import javafx.application.Application; 
import javafx.event.ActionEvent; 
import javafx.event.EventHandler; 
import javafx.geometry.Insets; 
import javafx.geometry.Pos; 
import javafx.scene.Scene; 
import javafx.scene.canvas.Canvas; 
import javafx.scene.canvas.GraphicsContext; 
import javafx.scene.control.Button; 
import javafx.scene.control.Label; 
import javafx.scene.control.TextField; 
import javafx.scene.layout.BorderPane; 
import javafx.scene.layout.HBox; 
import javafx.scene.layout.VBox; 
import javafx.scene.paint.Color; 
import javafx.scene.text.Font; 
import javafx.scene.text.Text; 
import javafx.stage.Stage; 

/**
 * A basic GUI for drawing a <code>Chromosome</code> structure to the screen 
 * and displaying its <code>fitness</code>. Implements the main loop of the 
 * genetic algorithm by building the consecutive generations. 
 * @author Franklin D. Worrell
 * @version 4 March 2018 
 */ 
public class Searcher extends Application {
	private static final double DIAMETER = 30; 
	private static final int OFFSET = 300; 
	private static final int CANVAS_DIMENSION = 600; 
	
	/**
	 * Builds the buttons, text fields, and canvas required to run the 
	 * nondeterministic search using our genetic algorithm. Contains the 
	 * algorithm's main loop. 
	 * @param primaryStage the <code>Stage</code> used as the root container
	 */ 
	@Override
	public void start(Stage primaryStage) {
		// Canvas for drawing the protein. 
		final Canvas canvas = new Canvas(CANVAS_DIMENSION, CANVAS_DIMENSION); 
		canvas.getGraphicsContext2D().setFont(new Font("Arial", 22)); 
		
		// Create text field for amino acid sequence. 
		HBox acidGetter = new HBox(); 
		Label acidPrompt = new Label("Amino acids: "); 
		acidPrompt.setFont(new Font("Arial", 18)); 
		final TextField acidField = new TextField(); 
		acidField.setPrefWidth(300); 
		acidGetter.getChildren().addAll(acidPrompt, acidField); 
		
		// Create text field for target fitness. 
		HBox fitnessGetter = new HBox(); 
		Label fitnessPrompt = new Label("Target fitness: "); 
		fitnessPrompt.setFont(new Font("Arial", 18)); 
		final TextField fitnessField = new TextField(); 
		fitnessGetter.getChildren().addAll(fitnessPrompt, fitnessField); 
		
		// Box to hold these text fields and labels. 
		VBox fields = new VBox(); 
		fields.setPadding(new Insets(10.0, 10.0, 10.0, 10.0)); 
		fields.getChildren().addAll(acidGetter, fitnessGetter); 
		
		// Create button to start search with genetic algorithm. 
		Button searchBtn = new Button(); 
		searchBtn.setText("Start"); 
		searchBtn.setOnAction(new EventHandler<ActionEvent>() {
			
			/**
			 * Implements the main loop of the genetic algorithm. 
			 * The search begins when the button is pressed and the
			 * relevant parameters are read in from the text fields. 
			 * @param event the button click event
			 */ 
			@Override 
			public void handle(ActionEvent event) {
				// Start a run of the genetic algorithm with text from above.
				String acids = acidField.getText(); 
				int targetFitness = Integer.parseInt(fitnessField.getText()); 

				// Build the initial, randomly generated population. 
				Population population = Population.getInitialPopulation(acids); 
				Chromosome currentBest = population.getBest(); 
				int currentFitness = currentBest.getFitness(); 
				int iteration = 0; 	// the generation of the protein
				System.out.println(population.reportFittestAndVolume()); 
								
				// Create successive generations until target fitness reached. 
				while (currentFitness > targetFitness) {
					iteration++; 
					population = Population.getNextGeneration(population); 
					currentBest = population.getBest(); 
					currentFitness = currentBest.getFitness(); 
					System.out.println("Generation " + iteration + '\t' + 
									   population.reportFittestAndVolume()); 
				}
				
				// Draw the winning chromosome. 
				drawChromosome(currentBest, iteration, 
							   canvas.getGraphicsContext2D()); 
			} 
		}); 
		
		Label diagramLabel = new Label("First acid will be filled."); 
		diagramLabel.setFont(new Font("Arial", 18)); 
		VBox buttonBox = new VBox(); 
		buttonBox.setAlignment(Pos.BASELINE_RIGHT); 
		buttonBox.setPadding(new Insets(10.0, 10.0, 10.0, 10.0)); 
		buttonBox.getChildren().addAll(diagramLabel, searchBtn); 
		
		// Container for TextFields and Button. 
		HBox startOptions = new HBox(); 
		startOptions.getChildren().addAll(fields, buttonBox); 
		
		VBox all = new VBox(); 
		all.getChildren().addAll(startOptions, canvas); 
		all.setAlignment(Pos.CENTER); 
		// Configure the display window. 
		Text initialLabel = new Text("Please enter required information" + 
									 " and press Start."); 
		initialLabel.setFont(new Font("Arial", 18)); 
		BorderPane root = new BorderPane(); 
		root.setPadding(new Insets(10.0, 10.0, 10.0, 10.0)); 
		root.setTop(initialLabel); 
		root.setCenter(all); 
		Scene scene = new Scene(root); 
		primaryStage.setTitle("Genetic Algorithm"); 
		primaryStage.setScene(scene); 
		primaryStage.show(); 
	} 
	
	
	/**
	 * Draws a <code>Chromosome</code>'s protein structure to the 
	 * <code>Canvas</code> along with information about the generation 
	 * that spawned it and the fitness of that <code>Chromosome</code>. 
	 * @param chromosome the protein structure to draw 
	 * @param iteration the generation that spawned the <code>Chromosome</code>
	 * @param gc the <code>GraphicsContext</code> of the <code>Canvas</code> 
	 */ 
	public void drawChromosome(Chromosome chromosome, int iteration, 
							   GraphicsContext gc) {
		// Clear and format canvas. 
		gc.clearRect(0, 0, 600, 600); 	// Clear graphic of previous iteration. 
		gc.setLineWidth(3); 
		gc.setFill(Color.LIGHTSTEELBLUE); 
		gc.fillRect(0, 0, 600, 600); 
		
		// Grab Chromosome bits so code is more compact. 
		boolean[] acids = chromosome.getAcids(); 
		Location[] locations = chromosome.getLocations(); 
		
		// TODO: Implement flexible scaling based on protein size. 
		
		// Draw the first acid. 
		Location previous = locations[0]; 
		double prevX = previous.getX() + OFFSET; 
		double prevY = previous.getY() + OFFSET; 
		gc.setFill((acids[0]) ? Color.BLACK : Color.WHITE); 
		gc.fillOval(prevX, prevY, DIAMETER - 10,  DIAMETER - 10); 
		
		// Draw all subsequent acids with connecting lines. 
		for (int i = 1; i < locations.length; i++) {
			Location current = locations[i]; 
			double currX = current.getX() * DIAMETER + OFFSET; 
			double currY = current.getY() * DIAMETER + OFFSET; 
			// Draw acid. 
			gc.setStroke((acids[i]) ? Color.BLACK : Color.WHITE); 
			gc.strokeOval(currX, currY, DIAMETER - 10,  DIAMETER - 10); 
			// Draw covalent bond. 
			gc.setStroke(Color.BURLYWOOD); 
			gc.strokeLine(prevX + 10, prevY + 10, currX + 10, currY + 10); 
			// Retain previous for drawing next bond. 
			prevX = currX; 
			prevY = currY; 
		}

		// Draw the iteration number and current fitness to the canvas. 
		gc.setFill(Color.BLACK); 
		gc.setLineWidth(1); 
		gc.fillText("Generation: " + iteration, 25, 45); 
		gc.fillText(((Integer) chromosome.getFitness()).toString(), 550, 575); 
	} 
	
	
	/**
	 * Given the structure of a protein, returns the required window dimensions 
	 * for drawing the protein to the screen. These must be later adjusted to 
	 * accommodate lines between covalent acids and the size of the acids when 
	 * drawn. 
	 * 
	 * Currently unused. Implemented as part of dynamic scaling of drawing of 
	 * protein structure. 
	 * 
	 * @param locations the coordinates of each amino acid in the protein 
	 * @return an array containing the horizontal and vertical spans of the protein 
	 */ 
	private int[] getRequiredWindowDimensions(Location[] locations) {
		int[] xAndY = {0, 0}; 
		int minX = 0; 
		int maxX = 0; 
		int minY = 0; 
		int maxY = 0; 
		
		// Find the most extreme x and y values. 
		for (Location location : locations) {
			int x = location.getX(); 
			int y = location.getY(); 
			if (x < minX) { minX = x; } 
			if (x > maxX) { maxX = x; } 
			if (y < minY) { minY = y; } 
			if (y > maxY) { maxY = y; } 
		} 
		
		xAndY[0] = maxX - minX; 	// the horizontal width 
		xAndY[1] = maxY - minY; 	// the vertical height 
		return xAndY; 
	} 
	
	
	public static void main(String[] args) {
		Application.launch(args); 
	}
	
} 
