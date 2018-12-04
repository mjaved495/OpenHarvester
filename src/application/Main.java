package application;
	
import java.io.File;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

	private Stage primaryStage;
	private Controller controller;
	public static String mainFolder;
	public static Boolean searchScopus = false;
	public static Boolean separateThread = true;
	 
	@Override
	public void start(Stage stage) {
		try {	
			controller = new Controller(stage);
			this.primaryStage = stage;
			VBox root = (VBox) FXMLLoader.load(new File("src/application/org.fxml").toURI().toURL());
			primaryStage.setTitle("OpenHarvester");
			primaryStage.setResizable(false);
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace(); 
		}
	}
	
	public static void main(String[] args) {
		if(args.length > 0) {
			mainFolder = args[0];
		} else {
			mainFolder = "/Users/mj495/Documents/DataHarvesterApp";
		}
		launch(args);
	}
}
