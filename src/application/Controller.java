package application; 

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.cornell.scholars.crossref.CrossRefDataExtracter;
import edu.cornell.scholars.data.Affiliation;
import edu.cornell.scholars.data.Article;
import edu.cornell.scholars.data.CoAuthor;
import edu.cornell.scholars.data.DataSource;
import edu.cornell.scholars.data.FilterOperations;
import edu.cornell.scholars.data.UserProfile;
import edu.cornell.scholars.dataprocessors.ClaimProcessor;
import edu.cornell.scholars.dataprocessors.RejectProcessor;
import edu.cornell.scholars.dataprocessors.UnrejectProcessor;
import edu.cornell.scholars.dblp.DblpDataExtracter;
import edu.cornell.scholars.pubmed.PubmedDataExtractor;
import harvester.crossref.QueryCrossRefData;
import harvester.dblp.DBLPDataHarvester;
import harvester.pubmed.PubmedDataHarvester;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

public class Controller implements Initializable {

	public static UserProfile profile;
	public static Map<String, String> scopusdocmap = new HashMap<String, String>();
	public static String selectedDatabase;

	private Stage stage;
	private JavaFXModel fxmodel;

	private ClaimProcessor claimProcessor;
	private RejectProcessor rejectProcessor;
	private UnrejectProcessor unrejectProcessor;

	public List<Article> resultantArticleList;

	private Set<Article> dblpResultantArticles;
	private Set<Article> crossrefResultantArticles;
	private Set<Article> pubmedResultantArticles;

	public static Set<Article> claimedArticles = new HashSet<Article>();
	public static Set<Article> rejectedArticles = new HashSet<Article>();

	public static Map<String, Article> allPubMap = new HashMap<String, Article>();

	public static Map<String, JSONObject> crossrefJSONMap = new HashMap<String, JSONObject>();
	public static Map<String, JSONObject> crossrefClaimedJSONMap = new HashMap<String, JSONObject>();
	public static Map<String, JSONObject> crossrefRejectedJSONMap = new HashMap<String, JSONObject>();

	public static Map<String, Document> dblpXMLMap = new HashMap<String, Document>();
	public static Map<String, Document> dblpClaimedXMLMap = new HashMap<String, Document>();
	public static Map<String, Document> dblpRejectedXMLMap = new HashMap<String, Document>();

	private static int clicksCounter;

	public static Set<String> stopWords = new HashSet<String>();
	private String[] stopWordsList = {"USA"};

	private static String PENDING = "pending";
	private static String CLAIMED = "claimed";
	private static String REJECTED = "rejected";

	public Controller(){
		fxmodel = new JavaFXModel();
	}

	public Controller(Stage stage) {
		this.stage = stage;
		fxmodel = new JavaFXModel();
	}

	@FXML
	private ListView<String> affiliationList, authorList, pendingList, claimList, rejectList, gNameList;

	@FXML
	private TextField folderPathTF, gNameTF, fNameTF, mNameTF, affiliationTF, 
	authorTF, startYearTF, searchDatabaseTF, searchTF;

	@FXML 
	private Button inputFolderButton, affiliatiobAddButton, authorAddButton, applyButton, 
	saveButton, clearButton, processButton, claimButton, loadDataButton, searchDatabaseButton, 
	rejectButton, unrejectButton;

	@FXML
	private Label claimPubCount, clicksCount, searchlabel, progressMonitor;

	@FXML
	private ComboBox<String> datasourceCB, searchDatabaseCB;

	@FXML
	private TextArea searchProgressTA;

	@FXML
	private ProgressBar progressBar;
	
	@FXML
	private Tab homeTab, searchDBTab, claimTab;
	
	@FXML
	private ProgressIndicator progressIndicator;

	private Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	private String inputFolder;
	private ObservableList<String> entries = FXCollections.observableArrayList();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override // This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

		init();
		for(String s: stopWordsList){
			stopWords.add(s);
		}
		// populate data sources in combo box
		for(DataSource datasource: DataSource.values()){
			datasourceCB.getItems().add(datasourceCB.getItems().size(), datasource.getDatasource());
		}
		// populate data sources in combo box
		for(DataSource datasource: DataSource.values()){
			searchDatabaseCB.getItems().add(searchDatabaseCB.getItems().size(), datasource.getDatasource());
		}

		pendingList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			public ListCell<String> call(ListView<String> param) {
				final Tooltip tooltip = new Tooltip();
				tooltip.setAutoHide(false);
				tooltip.setMaxWidth(800);
				final ListCell<String> cell = new ListCell<String>() {
					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						try {
							if (item != null) {
								Article article = findArticle(item, resultantArticleList);
								setText(article.getTitle());
								tooltip.setText(article.tooltipString());
								setTooltip(tooltip);
							}
						}catch(NullPointerException exp) {
							System.out.println("Exception on setCellFactory... Ignored. Should put If block");
						}

					}
				}; // ListCell
				return cell;
			}
		}); // setCellFactory

		searchTF.textProperty().addListener(new ChangeListener() {
			public void changed(ObservableValue observable, Object oldVal,
					Object newVal) {
				search((String) oldVal, (String) newVal);
			}

			public void search(String oldVal, String newVal) {
				if (oldVal != null && (newVal.length() < oldVal.length())) {
					if(entries != null && entries.size() > 0) {
						pendingList.setItems(entries);
					}
				}
				String value = newVal.toUpperCase();
				ObservableList<String> subentries = FXCollections.observableArrayList();
				for (Object entry : pendingList.getItems()) {
					boolean match = true;
					String entryText = (String) entry;
					if (entryText == null || !entryText.toUpperCase().contains(value)) {
						match = false;
					}
					if (match) {
						subentries.add(entryText);
					}
				}
				pendingList.setItems(subentries);
			}
		});

		pendingList.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
					if(mouseEvent.getClickCount() == 2){
						Article art = allPubMap.get(pendingList.getSelectionModel().getSelectedItem());
						if(art.getDoi() !=null) {
							URI uri;
							try {
								uri = new URI("https://doi.org/"+art.getDoi().trim());
								if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
									desktop.browse(uri);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}else {
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle("Information");
							alert.setHeaderText(null);
							alert.setContentText("NO DOI Found !!");
							alert.showAndWait();
							//System.out.println("NO DOI Found! "+art.toString());
						}
					}
					else {
						Article art = allPubMap.get(pendingList.getSelectionModel().getSelectedItem());
						if(art != null) {
							System.out.println(art.toString());
						}
					}
				}
			}
		});

		claimList.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
					if(mouseEvent.getClickCount() == 2){
						Article art = allPubMap.get(claimList.getSelectionModel().getSelectedItem());
						if(art.getDoi() !=null) {
							URI uri;
							try {
								uri = new URI("https://doi.org/"+art.getDoi().trim());
								if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
									desktop.browse(uri);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}else {
							System.out.println("NO DOI Found! "+art.toString());
						}
					}else {
						Article art = allPubMap.get(claimList.getSelectionModel().getSelectedItem());
						if(art != null) {
							System.out.println(art.toString());
						}

					}
				}
			}
		});

		assert inputFolderButton != null : "fx:id=\"input folder chooser button\" was not injected: check your FXML file 'org.fxml'.";
		inputFolderButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Input folder chooser Button Pressed");
				DirectoryChooser directoryChooser = new DirectoryChooser();
				File selectedDirectory = 
						directoryChooser.showDialog(stage);
				if(selectedDirectory == null){
					System.out.println("No Directory selected");
				}else{
					folderPathTF.setText(selectedDirectory.getAbsolutePath());
					Main.mainFolder = selectedDirectory.getAbsolutePath();
				}
			}
		});

		assert claimButton != null : "fx:id=\"claim button\" was not injected: check your FXML file 'org.fxml'.";
		claimButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Claim Button Pressed");
				String articleTitle = pendingList.getSelectionModel().getSelectedItem();
				claimSelectedPublications(articleTitle);
			}
		});

		assert rejectButton != null : "fx:id=\"reject button\" was not injected: check your FXML file 'org.fxml'.";
		rejectButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Reject Button Pressed");
				String articleTitle = pendingList.getSelectionModel().getSelectedItem();
				if(articleTitle == null) return;
				Article article = findArticle(articleTitle, resultantArticleList);
				rejectProcessor.rejectSelectedArticle(article, resultantArticleList);
			}
		});

		assert unrejectButton != null : "fx:id=\"unreject button\" was not injected: check your FXML file 'org.fxml'.";
		unrejectButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Un-Reject Button Pressed");
				String articleTitle = rejectList.getSelectionModel().getSelectedItem();
				if(articleTitle == null) return;
				List<Article> rejectedArticlesList = new ArrayList<Article>();
				rejectedArticlesList.addAll(rejectedArticles);
				Article article = findArticle(articleTitle, rejectedArticlesList);
				unrejectProcessor.unrejectSelectedArticle(article, resultantArticleList);
				Collections.<Article>sort(resultantArticleList);
				updatePendingArticleList(resultantArticleList);
			}
		});

		searchDatabaseCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> ov,
					final String oldvalue, final String newvalue) {
				if(newvalue.equals(DataSource.PUBMED.getDatasource())) {
					searchlabel.setText("Search by Last name and First name Initial. e.g. Goodman+L");
					searchDatabaseTF.setPromptText("Lastname+Initial");
				} else if(newvalue.equals(DataSource.DBLP.getDatasource())) {
					searchlabel.setText("Search by First name and Last name. e.g. Laura+Goodman");
					searchDatabaseTF.setPromptText("FirstName+Lastname");
				} else {	
					searchlabel.setText("Search by last name only. For example, Goodman");
					searchDatabaseTF.setPromptText("Lastname");
				}

			}});


		assert searchDatabaseButton != null : "fx:id=\"search database button\" was not injected: check your FXML file 'org.fxml'.";
		searchDatabaseButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Search Database Button Pressed");
				
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
				LocalDateTime start = LocalDateTime.now();
				searchProgressTA.appendText("\nStart Time: " + start);
				
				progressBar.setProgress(-1.0);
				progressIndicator.setProgress(-1.0);
				
				String searchString = searchDatabaseTF.getText().trim();
				selectedDatabase = searchDatabaseCB.getSelectionModel().getSelectedItem();
				Runnable task = new Runnable(){
					public void run(){
						long lStartTime = new Date().getTime();
						searchDatabaseButton.setDisable(true);

						if(selectedDatabase.equals(DataSource.CROSSREF.getDatasource())) {
							QueryCrossRefData dataExtractor = new QueryCrossRefData();
							String encodedSearchString = "";
							try {
								encodedSearchString = URLEncoder.encode(searchString, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							dataExtractor.process(encodedSearchString, 
									Main.mainFolder+"/"+selectedDatabase+"/"+searchString, null, 0, searchProgressTA, progressBar, progressIndicator);
						} else if(selectedDatabase.equals(DataSource.DBLP.getDatasource())) {
							DBLPDataHarvester dataExtractor = new DBLPDataHarvester();
							dataExtractor.process(searchString, Main.mainFolder+"/"+selectedDatabase+"/"+searchString, searchProgressTA, progressBar, progressIndicator);
						} else if(selectedDatabase.equals(DataSource.PUBMED.getDatasource())) {
							PubmedDataHarvester dataExtractor = new PubmedDataHarvester();
							dataExtractor.process(searchString, Main.mainFolder+"/"+selectedDatabase+"/"+searchString, searchProgressTA, progressBar, progressIndicator);
						}

						searchDatabaseButton.setDisable(false);
						long lEndTime = new Date().getTime();
						long output = lEndTime - lStartTime;
						double minutes = output/60000;
						DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
						LocalDateTime end = LocalDateTime.now(); 
						searchProgressTA.appendText("\nEnd Time: " + end );
						searchProgressTA.appendText("\nTotal Time: " + minutes + " minutes.");
					}
				};
				// Run the task in a background thread
				Thread backgroundThread = new Thread(task);
				// Terminate the running thread if the application exits
				backgroundThread.setDaemon(true);
				// Start the thread
				backgroundThread.start();
			}
		});

		assert applyButton != null : "fx:id=\"apply button\" was not injected: check your FXML file 'org.fxml'.";
		applyButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Apply Button Pressed");
				generateUserProfile();
			}
		});

		assert processButton != null : "fx:id=\"process button\" was not injected: check your FXML file 'org.fxml'.";
		processButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Process Button Pressed");
				Main.separateThread = true;

				Runnable task = new Runnable(){
					public void run(){
						runAutoClaimingProces();
					}
				};

				// Run the task in a background thread
				Thread backgroundThread = new Thread(task);
				// Terminate the running thread if the application exits
				backgroundThread.setDaemon(true);
				// Start the thread
				backgroundThread.start();
			} 

			private void runAutoClaimingProces() {
				int firsLineCounter = 0;
				int firstPageCounter = 0;

				Set<String> scholars_dois = scopusdocmap.keySet();
				int index = pendingList.getSelectionModel().getSelectedIndex();
				int counter = 0;
				while(pendingList.getItems().size() > 0 && counter < 10) {
					String articleTitle = pendingList.getSelectionModel().getSelectedItem();
					if(articleTitle == null) return;
					Article article = findArticle(articleTitle, resultantArticleList);

					if(article.getDoi() != null && scholars_dois.contains(article.getDoi().trim().toLowerCase())) {
						if(index == 0) {
							firsLineCounter++;
						}else {
							firstPageCounter++;
						}

						claimSelectedPublications(articleTitle);

						if(Main.separateThread) {
							// Update on the JavaFx Application Thread       
							int i = index;
							Platform.runLater(new Runnable(){
								@Override
								public void run() {
									pendingList.scrollTo(i);
									pendingList.getSelectionModel().select(i);
								}
							});
							sleep();
						}else {
							pendingList.scrollTo(index);
							pendingList.getSelectionModel().select(index);
						}
						index = 0;
						counter = 0;
					}else{
						++counter;
						if(Main.separateThread) {
							//Update on the JavaFx Application Thread       
							int i = ++index;
							Platform.runLater(new Runnable(){
								@Override
								public void run(){
									pendingList.scrollTo(i);
									pendingList.getSelectionModel().select(i);
								}
							});
							sleep();
						}else {
							pendingList.scrollTo(++index);
							sleep();
							pendingList.getSelectionModel().select(index);
							sleep();
						}		
					}
				}
				System.out.println(firsLineCounter +" - "+firstPageCounter);
				Main.separateThread = false;
			}
		});

		assert clearButton != null : "fx:id=\"clear button\" was not injected: check your FXML file 'org.fxml'.";
		clearButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Clear Button Pressed");
				clicksCounter = 0;
				profile = new UserProfile();
				gNameTF.clear();
				fNameTF.clear();
				gNameList.getItems().clear();
				affiliationTF.clear();
				affiliationList.getItems().clear();
				authorTF.clear();
				authorList.getItems().clear();
				resultantArticleList = new ArrayList<Article>();
				pendingList.getItems().clear();
				rejectList.getItems().clear();
				claimedArticles = new HashSet<Article>();
				rejectedArticles = new HashSet<Article>();
				claimList.getItems().clear();
			}
		});

		assert loadDataButton != null : "fx:id=\"loadDataButton button\" was not injected: check your FXML file 'org.fxml'.";
		loadDataButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Load Data Button Pressed");
				String selectedSource = datasourceCB.getSelectionModel().getSelectedItem();
				searchPublications(selectedSource);
			}
		});

		assert affiliatiobAddButton != null : "fx:id=\"Affiliation Add Button\" was not injected: check your FXML file 'org.fxml'.";
		affiliatiobAddButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Add Affiliations Button Pressed.");
				if(affiliationTF.getText() == null || affiliationTF.getText().isEmpty()) return;
				addAffiliation();
				affiliationTF.clear();
			}
		});

		assert authorAddButton != null : "fx:id=\"Auhtor Add Button\" was not injected: check your FXML file 'org.fxml'.";
		authorAddButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Add Author Button Pressed.");
				if(authorTF.getText() == null || authorTF.getText().isEmpty()) return;
				addAuthor();
				authorTF.clear(); 
			}
		});

		assert saveButton != null : "fx:id=\"Save Button\" was not injected: check your FXML file 'org.fxml'.";
		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent event) {
				try {
					selectedDatabase = datasourceCB.getSelectionModel().getSelectedItem();
					Parent root = FXMLLoader.load(new File("src/savescene/saveas.fxml").toURI().toURL());
					Scene scene = new Scene(root);
					//scene.getStylesheets().add(getClass().getResource("savescene/sceneapplication.css").toExternalForm());
					scene.getStylesheets().add(new File("src/savescene/sceneapplication.css").toURI().toURL().toExternalForm());
					Stage stage2 = new Stage(StageStyle.DECORATED);
					stage2.setTitle("Save as Dialog Box");
					stage2.setResizable(false);
					//stage2.getIcons().add(new Image(""));
					stage2.initModality(Modality.APPLICATION_MODAL);
					stage2.setScene(scene);
					stage2.show();
				} catch (IOException e) {
					e.printStackTrace();
				}	

			}
		});
	}

	private void init() {
		
		loadIcons();
		
		claimProcessor = new ClaimProcessor(claimList, claimedArticles, authorList, 
				pendingList, affiliationList, gNameList);
		rejectProcessor = new RejectProcessor(rejectList, rejectedArticles, pendingList);	
		unrejectProcessor = new UnrejectProcessor(rejectList, rejectedArticles, pendingList);
	}

	private void loadIcons() {
		Image imageBrowse = new Image(getClass().getResourceAsStream("opened-folder.png"));
		ImageView browseIconView = new ImageView(imageBrowse);
		browseIconView.setFitHeight(15);
		browseIconView.setFitWidth(16);
        inputFolderButton.setGraphic(browseIconView);
		
        Image imageClaim = new Image(getClass().getResourceAsStream("man-plus.png"));
		ImageView claimIconView = new ImageView(imageClaim);
		claimIconView.setFitHeight(17);
		claimIconView.setFitWidth(16);
		claimButton.setGraphic(claimIconView);
		
		Image imageDecline = new Image(getClass().getResourceAsStream("Trash.png"));
		ImageView cameraIconView = new ImageView(imageDecline);
        cameraIconView.setFitHeight(15);
        cameraIconView.setFitWidth(16);
		rejectButton.setGraphic(cameraIconView);
		
		Image imageHome = new Image(getClass().getResourceAsStream("home.png"));
		ImageView homeView = new ImageView(imageHome);
		homeView.setFitHeight(15);
		homeView.setFitWidth(16);
		homeTab.setGraphic(homeView);
		
		Image imageSearch = new Image(getClass().getResourceAsStream("search.png"));
		ImageView searchView = new ImageView(imageSearch);
		searchView.setFitHeight(15);
		searchView.setFitWidth(16);
		searchDBTab.setGraphic(searchView);
		
		Image claimSearch = new Image(getClass().getResourceAsStream("claim2.png"));
		ImageView claimView = new ImageView(claimSearch);
		claimView.setFitHeight(16);
		claimView.setFitWidth(17);
		claimTab.setGraphic(claimView);	
	}

	protected void searchPublications(String selectedSource) {

		if (selectedSource == null) return;

		inputFolder = folderPathTF.getText();

		//if(selectedSource.equals(DataSource.ALL.getDatasource())) {
		//			if(!new File(Main.mainFolder, "CROSSREF"+"/"+givenName+"+"+familyName).exists() 
		//					&&
		//					!new File(Main.mainFolder, "PUBMED"+"/"+givenName+"+"+familyName).exists()
		//					&&
		//					!new File(Main.mainFolder, "DBLP"+"/"+givenName+"+"+familyName).exists() ) {
		//				Alert alert = new Alert(AlertType.INFORMATION);
		//				alert.setTitle("Information");
		//				alert.setHeaderText(null);
		//				alert.setContentText("Queried data folders do not exist!! Search Not Permitted!");
		//				alert.showAndWait();
		//			}else {
		//				searchAllPublications();
		//			}
		//}else 
		if(selectedSource.equals(DataSource.CROSSREF.getDatasource())) {
			if(!new File(inputFolder).exists()){
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information");
				alert.setHeaderText(null);
				alert.setContentText("Queried data folder does not exist!! Search Not Permitted!");
				alert.showAndWait();
			}
			// RE-READ ALL FILES.			
			//			else if(new File(inputFolder, PENDING).exists() && new File(inputFolder, CLAIMED).exists()){
			//
			//				CrossrefDataProcessor processor = new CrossrefDataProcessor();
			//
			//				File cfolder = new File(inputFolder, CLAIMED);
			//				Set<Article> previouslyClaimedArticles = processor.processCrossRefClaimedPublications(cfolder.getAbsolutePath(), crossrefClaimedJSONMap, progressMonitor);
			//				for(Article article: previouslyClaimedArticles){
			//					claimList.getItems().add(claimList.getItems().size(), article.getTitle());
			//					claimedArticles.add(article);
			//					//Update the profile
			//					Set<CoAuthor> newCoauthors = claimProcessor.populateCoAuthorsInUserProfile(article);
			//					Set<Affiliation> newAff = claimProcessor.populateAffiliationsInUserProfile(article);
			//					updateCoAuthorsListView();
			//					updateAffiliationListView();
			//				}
			//
			//				File pfolder = new File(inputFolder, PENDING);
			//				resultantArticleList = processor.processCrossRefPendingPublications(pfolder.getAbsolutePath(), crossrefJSONMap, progressMonitor);
			//				FilterOperations.rankeArticlesBasedOnLangauge(resultantArticleList);
			//				rankArticlesBasedOnUserProfileData();
			//				Collections.<Article>sort(resultantArticleList);
			//				updatePendingArticleList(resultantArticleList);
			//
			//				File rfolder = new File(inputFolder, REJECTED);
			//				Set<Article> previouslyRejectedArticles = processor.processCrossRefRejectedPublications(rfolder.getAbsolutePath(), crossrefRejectedJSONMap, progressMonitor);
			//				for(Article article: previouslyRejectedArticles){
			//					rejectList.getItems().add(rejectList.getItems().size(), article.getTitle());
			//					rejectedArticles.add(article);
			//				}
			//			} 
			else {
				processCrossRefPublications();
				
			}
		}else if(selectedSource.equals(DataSource.DBLP.getDatasource())) {
			if(!new File(inputFolder).exists()){
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information");
				alert.setHeaderText(null);
				alert.setContentText("Queried data folder does not exist!!");
				alert.showAndWait();
			}
			// RE-READ ALL FILES.
			//			else if(new File(inputFolder, PENDING).exists() && new File(inputFolder, CLAIMED).exists()){
			//				DblpDataProcessor processor = new DblpDataProcessor();
			//
			//				File cfolder = new File(inputFolder, CLAIMED);
			//				Set<Article> previouslyClaimedArticles = processor.processDBLPClaimedPublications(cfolder.getAbsolutePath(), dblpClaimedXMLMap);
			//				for(Article article: previouslyClaimedArticles){
			//					claimList.getItems().add(claimList.getItems().size(), article.getTitle());
			//					claimedArticles.add(article);
			//					//Update the profile
			//					Set<CoAuthor> newCoauthors = claimProcessor.populateCoAuthorsInUserProfile(article);
			//					Set<Affiliation> newAff = claimProcessor.populateAffiliationsInUserProfile(article);
			//					updateCoAuthorsListView();
			//					updateAffiliationListView();
			//				}
			//
			//				File pfolder = new File(inputFolder, PENDING);
			//				resultantArticleList = processor.processDBLPPendingPublications(pfolder.getAbsolutePath(), dblpXMLMap);
			//				FilterOperations.rankeArticlesBasedOnLangauge(resultantArticleList);
			//				rankArticlesBasedOnUserProfileData();
			//				Collections.<Article>sort(resultantArticleList);
			//				updatePendingArticleList(resultantArticleList);
			//
			//
			//				File rfolder = new File(inputFolder, REJECTED);
			//				Set<Article> previouslyRejectedArticles = processor.processDBLPRejectedPublications(rfolder.getAbsolutePath(), dblpRejectedXMLMap);
			//				for(Article article: previouslyRejectedArticles){
			//					rejectList.getItems().add(rejectList.getItems().size(), article.getTitle());
			//					rejectedArticles.add(article);
			//				}
			//
			//			} 
			else {
				processDBLPPublications();
			}
		} else if(selectedSource.equals(DataSource.PUBMED.getDatasource())) {
			if(!new File(inputFolder).exists()){
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information");
				alert.setHeaderText(null);
				alert.setContentText("Queried data folder does not exist!!");
				alert.showAndWait();
			}
			// RE-READ ALL FILES.
			//			else if(new File(inputFolder, PENDING).exists() && new File(inputFolder, CLAIMED).exists()){
			//
			//				PubmedDataProcessor processor = new PubmedDataProcessor();
			//
			//				File cfolder = new File(inputFolder, CLAIMED);
			//				Set<Article> previouslyClaimedArticles = processor.processPubmedClaimedPublications(cfolder.getAbsolutePath(), pubmedClaimedXMLMap);
			//				for(Article article: previouslyClaimedArticles){
			//					claimList.getItems().add(claimList.getItems().size(), article.getTitle());
			//					claimedArticles.add(article);
			//					//Update the profile
			//					Set<CoAuthor> newCoauthors = claimProcessor.populateCoAuthorsInUserProfile(article);
			//					Set<Affiliation> newAff = claimProcessor.populateAffiliationsInUserProfile(article);
			//					updateCoAuthorsListView();
			//					updateAffiliationListView();
			//				}
			//
			//				File pfolder = new File(inputFolder, PENDING);
			//				resultantArticleList = processor.processPubmedPendingPublications(pfolder.getAbsolutePath(), pubmedXMLMap);
			//				FilterOperations.rankeArticlesBasedOnLangauge(resultantArticleList);
			//				rankArticlesBasedOnUserProfileData();
			//				Collections.<Article>sort(resultantArticleList);
			//				updatePendingArticleList(resultantArticleList);
			//
			//
			//				File rfolder = new File(inputFolder, REJECTED);
			//				Set<Article> previouslyRejectedArticles = processor.processPubmedRejectedPublications(rfolder.getAbsolutePath(), pubmedRejectedXMLMap);
			//				for(Article article: previouslyRejectedArticles){
			//					rejectList.getItems().add(rejectList.getItems().size(), article.getTitle());
			//					rejectedArticles.add(article);
			//				}
			//			}  
			else {
				processPubmedPublications();
			}
		}
	}

	private void claimSelectedPublications(String articleTitle) {
		if(articleTitle == null) return;
		Article article = findArticle(articleTitle, resultantArticleList);

		claimProcessor.claimSelectedArticle(article, resultantArticleList);

		updateCoAuthorsListView();
		updateAffiliationListView();
		clicksCount.setText(Integer.toString(++clicksCounter));
		Collections.<Article>sort(resultantArticleList);
		updatePendingArticleList(resultantArticleList);

	}

	protected void SortPendingListByRank() {
		System.out.println("List size:"+ resultantArticleList.size());
		Collections.<Article>sort(resultantArticleList);
		updatePendingArticleList(resultantArticleList);
	}

	protected Article findArticle(String articleTitle, List<Article> articleList2) {
		for(Article art :articleList2) {
			if (art.getTitle().equals(articleTitle)) {
				return art;
			}
		}
		return null;
	}

	public Set<CoAuthor> populateCoAuthorsInUserProfile(Article article) {
		Set<CoAuthor> newCoAuthors = new HashSet<CoAuthor>();

		for(CoAuthor author: article.getAuthors()){
			try {
				if(!author.getFamilyName().toUpperCase().equals(profile.getFamilyName().toUpperCase())){  //DO NOT ADD YOURSELF AS COAUTHOR
					if(!isAuthorNameVarianceExist(authorList, author.getFamilyName().trim()+", "+author.getGivenName().trim())){
						profile.addCoAuthor(author);
						newCoAuthors.add(author);
					}
				}
			}catch(NullPointerException exp) {
				System.err.println("Exception caught while adding a co-author..continuing");
				continue;
			}
		}
		updateCoAuthorsListView();
		return newCoAuthors;
	}

	private void updateAffiliationListView() {
		if(affiliationList == null) {
			System.out.println("author list is null");
		}
		affiliationList.getItems().clear();
		Set<Affiliation> affiliations = profile.getAffiliations();
		for(Affiliation affiliation: affiliations) {
			String name = affiliation.getName();
			affiliationList.getItems().add(affiliationList.getItems().size(), name);
		}
	}

	//	private void removeArticlefromPendingList(Article article) {
	//
	//		boolean isremoved = resultantArticleList.remove(article);
	//		if(isremoved) {
	//			int selectedIdx = pendingList.getSelectionModel().getSelectedIndex();
	//			if(Main.separateThread) {
	//				// Update on the JavaFx Application Thread       
	//				Platform.runLater(new Runnable(){
	//					@Override
	//					public void run(){
	//						pendingList.getItems().remove(selectedIdx);
	//					}
	//				});
	//				sleep();
	//			}else {
	//				pendingList.getItems().remove(selectedIdx);
	//			}
	//
	//		}	
	//	}

	private void updateCoAuthorsListView() {
		if(authorList == null) {
			System.out.println("author list is null");
		}
		authorList.getItems().clear();
		Set<CoAuthor> coauthors = profile.getCoauthors();
		for(CoAuthor author: coauthors) {
			String name = author.getFamilyName()+", "+ author.getGivenName();
			authorList.getItems().add(authorList.getItems().size(), name);
		}
	}


	public void processCrossRefPublications() {

		//read user profile
		//generateUserProfile();

		// search based on firstname and lastname
		CrossRefDataExtracter queryxref = new CrossRefDataExtracter();

		crossrefResultantArticles = 
				queryxref.processJSONFilesData(inputFolder, crossrefJSONMap, progressMonitor);

		//csvFileName = profile.getFamilyName()+".csv";
		//File file = new File(Main.mainFolder, csvFileName);

		resultantArticleList = new LinkedList<Article>();
		resultantArticleList.addAll(crossrefResultantArticles);

		FilterOperations.rankeArticlesBasedOnLangauge(resultantArticleList);
		rankArticlesBasedOnUserProfileData();
		Collections.<Article>sort(resultantArticleList);

		for(Article a: resultantArticleList) {
			System.out.println(a.getTitle());
		}
		
		
		// populate list view.
		updatePendingArticleList(resultantArticleList);
		
		//update claimed articles list
		updateClaimedArticleList(resultantArticleList, DataSource.CROSSREF.getDatasource());
	}

	private void processPubmedPublications() {
		//read user profile
		//generateUserProfile();

		PubmedDataExtractor obj = new PubmedDataExtractor();
		//		csvFileName = profile.getFamilyName()+".csv";
		//		File file = new File(Main.mainFolder, csvFileName);
		//		if(file.exists()) obj.readCSVFile(file);

		pubmedResultantArticles = obj.process(inputFolder);

		resultantArticleList = new LinkedList<Article>();  //emptied the current list of articles
		resultantArticleList.addAll(pubmedResultantArticles);

		rankArticlesBasedOnUserProfileData();
		Collections.<Article>sort(resultantArticleList);

		// populate list view.
		updatePendingArticleList(resultantArticleList);
		
		//update claimed articles list
		updateClaimedArticleList(resultantArticleList, DataSource.PUBMED.getDatasource());
	}

	public void processDBLPPublications() {

		//read user profile
		//generateUserProfile();

		DblpDataExtracter obj = new DblpDataExtracter();
		dblpResultantArticles = obj.process(inputFolder, dblpXMLMap);

		resultantArticleList = new LinkedList<Article>();  //emptied the current list of articles
		resultantArticleList.addAll(dblpResultantArticles);

		rankArticlesBasedOnUserProfileData();
		Collections.<Article>sort(resultantArticleList);

		// populate list view.
		updatePendingArticleList(resultantArticleList);
		
		//update claimed articles list
		updateClaimedArticleList(resultantArticleList, DataSource.DBLP.getDatasource());

	}

	//	public void searchAllPublications() {
	//		//read user profile
	//		generateUserProfile();
	//		Set<Article> mergedArticle = ProcessAllSources.searchALLPublications();
	//		resultantArticleList = new LinkedList<Article>();
	//		resultantArticleList.addAll(mergedArticle);
	//		rankArticlesBasedOnUserProfileData();
	//		Collections.<Article>sort(resultantArticleList);
	//		// populate list view.
	//		updatePendingArticleList(resultantArticleList);
	//	}

	//	private void compareDOIsSet(Set<String> scholarsDois, Set<String> allFoundDois) {
	//		for(String schDOI: scholarsDois) {
	//			if(!allFoundDois.contains(schDOI)) {
	//				System.out.println(schDOI+" : DOI not found.");
	//			}
	//		}	
	//	}

	private void rankArticlesBasedOnUserProfileData() {

		// rank pubs based on data given in profile.
		if(profile.getPubStartYear() != null){
			resultantArticleList = FilterOperations.filterOnPubStartYear(profile.getPubStartYear(), resultantArticleList);
			System.out.println("Current articles count: "+resultantArticleList.size());
		}

		FilterOperations.rankArticlesBasedOnUserFullName(resultantArticleList, profile);
		FilterOperations.rankArticlesBasedOnCoAuthors(resultantArticleList, profile);
		FilterOperations.rankArticlesBasedOnAffiliation(resultantArticleList, profile, null);
	}

	private void updateClaimedArticleList(List<Article> resultantArticleList2, String datasource) {
		File idFile = new File(inputFolder+"/CLAIMED/ClaimedArticleIDs.csv"); 
		Set<String> claimedIds = readClaimedIdsFiles(idFile);
		if(!idFile.exists() || claimedIds.size() == 0) {
			System.out.println("ClaimedId file not found");
			return;
		}
		for(Article article: resultantArticleList2) {
			String id = article.getIdentifiers(datasource);
			if(claimedIds.contains(id)) {
				if(Main.separateThread) {
					// Update on the JavaFx Application Thread       
					Platform.runLater(new Runnable(){
						@Override
						public void run(){
							claimSelectedPublications(article.getTitle());
						}
					});
				}
			}
		}
	}
	
	private void updatePendingArticleList(List<Article> articleList) {
		if(Main.separateThread) {
			// Update on the JavaFx Application Thread       
			Platform.runLater(new Runnable(){
				@Override
				public void run(){
					pendingList.getItems().clear();
				}
			});
		}else{
			pendingList.getItems().clear();
		}
		entries.clear();
		for(Article article: articleList) {
			String title = article.getTitle();
			if(Main.separateThread) {
				// Update on the JavaFx Application Thread       
				Platform.runLater(new Runnable(){
					@Override
					public void run(){
						pendingList.getItems().add(pendingList.getItems().size(), title);
					}
				});
			}else {
				pendingList.getItems().add(pendingList.getItems().size(), title);
			}
			entries.add(title);
		}
		
		//System.out.println(entries.size());
		//System.out.println(pendingList.getItems().size());
	}

	
	protected void addAffiliation() {
		String affiliation = affiliationTF.getText();
		affiliationList.getItems().add(affiliationList.getItems().size(), affiliation);
	}

	protected void addAuthor() {
		String coauthor = authorTF.getText();
		if(!isAuthorNameVarianceExist(authorList, coauthor)){
			authorList.getItems().add(authorList.getItems().size(), coauthor);
		}

	}

	protected void updateGivenNameVariant() {
		Set<String> givenNameVariants = profile.getGivenName();
		for(String givenNameVariant: givenNameVariants) {
			if(!gNameList.getItems().contains(givenNameVariant)) {
				gNameList.getItems().add(gNameList.getItems().size(), givenNameVariant);
			}
		}
	}

	private boolean isAuthorNameVarianceExist(ListView<String> authorList2, String coauthor) {
		ObservableList<String> items = authorList2.getItems();
		for(String namevar: items) {
			if(namevar.toLowerCase().equals(coauthor.toLowerCase())){
				return true;
			}
		}
		return false;
	}

	public void generateUserProfile() {

		profile = new UserProfile();
		String givenName =  gNameTF.getText();
		String middleInitial = mNameTF.getText();
		if(middleInitial != null && !middleInitial.isEmpty()) {
			String givenMiddleInitial = givenName+" "+middleInitial;
			profile.addGivenName(givenMiddleInitial);
		}
		profile.addGivenName(givenName!=null?givenName:null);
		if(profile.getFirstGivenName() == null) {
			profile.setFirstGivenName(givenName!=null?givenName:null);
		}

		String familyName = fNameTF.getText();
		profile.setFamilyName(familyName!=null?familyName:null);

		//Not using right now
		String startYear = startYearTF.getText();
		if(startYear != null && !startYear.isEmpty()) {
			profile.setPubStartYear(Long.valueOf(startYear));
		}

		Set<Affiliation> affiliations = getAffiliationsList(affiliationList);
		profile.getAffiliations().addAll(affiliations!=null?affiliations:null);
		Set<CoAuthor> coauthors = getCoAuthorList(authorList);
		profile.getCoauthors().addAll(coauthors!=null?coauthors:null);

		updateGivenNameVariant();

		if(resultantArticleList != null && resultantArticleList.size() > 0){
			rankArticlesBasedOnUserProfileData();
			Collections.<Article>sort(resultantArticleList);
			updatePendingArticleList(resultantArticleList);
		}
	}

	public Set<CoAuthor> getCoAuthorList(ListView<String> listView){
		Set<CoAuthor> list = new HashSet<CoAuthor>(); 
		ObservableList<String> listview = listView.getItems();
		for (String entry: listview)
		{
			String nameParts[] = entry.split(",");
			CoAuthor author = new CoAuthor(nameParts[1].trim(), nameParts[0].trim());
			list.add(author);
		}
		return list;
	}

	public Set<Affiliation> getAffiliationsList(ListView<String> listView){
		Set<Affiliation> list = new HashSet<Affiliation>(); 
		ObservableList<String> listview = listView.getItems();
		for (String entry: listview){
			list.add(new Affiliation(entry.trim()));
		}
		return list;
	}

	private Set<String> readClaimedIdsFiles(File idFile){
		Set<String> claimedIds = new HashSet<String>();
		if(idFile.exists()) {
			System.out.println(idFile.getAbsolutePath());
			FileInputStream is = null;
			BufferedReader buf = null;
			InputStreamReader isr = null;
			try {
				is = new FileInputStream(idFile.getAbsolutePath());
				isr = new InputStreamReader(is, "UTF-8");
				buf = new BufferedReader(isr);
				String lineJustFetched = null;
				while(true){
					lineJustFetched = buf.readLine();
					if(lineJustFetched == null){  
						break; 
					}else{
						if(lineJustFetched.trim().length() == 0){
							continue;
						}
						claimedIds.add(lineJustFetched);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
					isr.close();
					buf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return claimedIds;
	}
	
	private static void sleep(){
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
