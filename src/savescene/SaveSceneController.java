package savescene;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import application.Controller;
import edu.cornell.scholars.dataprocessors.SaveDataProcessor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SaveSceneController implements Initializable{
	
	@FXML 
	private Button browseS2Button, saveS2Button, closeS2Button;
	
	@FXML 
	private TextField personURITF, journalfileTF;
	
	@FXML
	private CheckBox csvChB, pdfChB, txtChB, rdfChB;
	
	@FXML
	private Label saveLabel;
	
	@Override
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

		rdfChB.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	if(rdfChB.isSelected()) {
		    		personURITF.setDisable(false);
		    		browseS2Button.setDisable(false);
		    		journalfileTF.setDisable(false);
		    	}else {
		    		personURITF.setDisable(true);
		    		browseS2Button.setDisable(true);
		    		journalfileTF.setDisable(true);
		    	}
		    }
		});
		
		assert browseS2Button != null : "fx:id=\"browseS2Button \" was not injected: check your FXML file 'org.fxml'.";
		browseS2Button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Chooser Button Pressed");
				FileChooser fileChooser = new FileChooser();
				File selectedFile = fileChooser.showOpenDialog(null);
                if(selectedFile == null){
                	System.out.println("No File selected");
                }else{
                	journalfileTF.setText(selectedFile.getAbsolutePath());
                }
			}
		});
		
		assert saveS2Button != null : "fx:id=\"saveS2Button \" was not injected: check your FXML file 'org.fxml'.";
		saveS2Button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try{
					System.out.println("Save Button S2 Pressed");
					SaveDataProcessor datasave = new SaveDataProcessor();
					String personURI = personURITF.getText().trim();
					
					datasave.saveData(personURI, csvChB, pdfChB, txtChB, rdfChB, journalfileTF);
					
					System.out.println("Data is Saved.");
					saveLabel.setText("Saved!");
				} catch(Exception exp) {
					System.out.println("Data not saved. Exception occured.");
					exp.printStackTrace();
				}
			}
		});
		
		assert closeS2Button != null : "fx:id=\"closeS2Button \" was not injected: check your FXML file 'org.fxml'.";
		closeS2Button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				 Stage stage = (Stage) closeS2Button.getScene().getWindow();
				 stage.close();
			}
		});
		
		
	}
}
