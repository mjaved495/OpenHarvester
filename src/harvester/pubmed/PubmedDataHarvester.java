package harvester.pubmed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;

public class PubmedDataHarvester {
	private static final String BASE_SEARCH = 	"https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?"
			+ "db=pubmed&term=";
	//	private static final String BASE_SUMMARY = 	"https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?"
	//			+ "db=pubmed&id=";
	private static final String BASE_FETCH = 	"https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?"
			+ "db=pubmed&id=";

	private static final String FIELD_AUTHOR = "field=author";
	private static final String RETMAX = "retmax";
	private static final String RETMODE_XML = "retmode=xml";
	private static final String AND = "&";
	private static final String EQUAL = "=";

	private static Integer idGroupMax = 10;
	private static final String retmaxValue = "3";
	private  TextArea textArea;
	private ProgressBar progressBar;
	private ProgressIndicator progressIndicator;
	
	private StringBuilder progress = new StringBuilder("");
	private double queryCountForProgress = 0;

	public void process(String searchString, String outputFolder, TextArea searchProgressTA, ProgressBar progressBar, ProgressIndicator progressIndicator) {
		this.textArea = searchProgressTA;
		this.progressBar = progressBar;
		this.progressIndicator = progressIndicator;
		
		File folder = new File(outputFolder);
		if (!folder.exists()){
			//System.out.println("Folder created: "+folder.mkdirs());
			printMessage("\nFolder created: "+folder.mkdirs());
		}

		String baseQuery = BASE_SEARCH + searchString+ AND + FIELD_AUTHOR + AND + RETMAX + EQUAL + retmaxValue;
		//System.out.println(baseQuery);
		printMessage("\n"+baseQuery);
		
		File summaryFile = new File(folder, searchString+"Pubmed.xml");
		int articleCount = getArticleCount(baseQuery, summaryFile);
		//System.out.println("Article Count: "+articleCount);
		printMessage("\nArticle Count: "+articleCount);
		queryCountForProgress = (articleCount/10) + 1;
		
		fetchDataFiles(searchString, summaryFile, folder.getAbsolutePath(), articleCount);
		//System.out.println("Data Fetch Completed.");
		printMessage("\nData Fetch Completed.");
	}

	private void fetchDataFiles(String namedToBeQueried,  File summaryFile, String outputFolderName, int articleCount) {
		String articleIdExtractionQuery = BASE_SEARCH + namedToBeQueried+ AND + FIELD_AUTHOR + AND + RETMAX + EQUAL +articleCount;
		try {
			List<Integer> pubmedIds = getPubmedIds(articleIdExtractionQuery, summaryFile);	
			String idList = "";
			int fileCounter = 0;
			int counter = idGroupMax;
			for(int index=0; index < pubmedIds.size(); index++) {
				idList = idList+pubmedIds.get(index)+",";
				if(index+1 == counter) {
					idList = idList.substring(0, idList.length()-1);
					String fetchQuery = BASE_FETCH + idList+ AND + RETMODE_XML;
					
					//System.out.println(fetchQuery);
					printMessage("\n"+fetchQuery);
					
					runQuery(fetchQuery, new File(outputFolderName, ++fileCounter+".xml"));
					counter+=idGroupMax;
					idList = "";
					setProgress(fileCounter/queryCountForProgress);	
				}	
			}
			idList = idList.substring(0, idList.length());
			String fetchQuery = BASE_FETCH + idList+ AND + RETMODE_XML;
			runQuery(fetchQuery, new File(outputFolderName, ++fileCounter+".xml"));
			setProgress(fileCounter/queryCountForProgress);	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<Integer> getPubmedIds(String articleIdExtractionQuery, File summaryFile) throws IOException {
		List<Integer> pubmedIds = new ArrayList<Integer>();

		runQuery(articleIdExtractionQuery, summaryFile);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(summaryFile);
			Node idListNode = doc.getElementsByTagName("IdList").item(0);
			Element idListElement = (Element) idListNode;
			NodeList idList = idListElement.getElementsByTagName("Id");
			for(int index=0; index< idList.getLength(); index++){
				Node idNode = idList.item(index);
				String id = idNode.getTextContent();
				pubmedIds.add(Integer.parseInt(id));
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		//System.out.println("Total Pubmed Ids captured: "+ pubmedIds.size());
		printMessage("\nTotal Pubmed Ids captured: "+ pubmedIds.size());
		return pubmedIds;
	}

	private int getArticleCount(String baseQuery, File outputFile) {
		runQuery(baseQuery, outputFile);

		int count = getArticleCount(outputFile);
		return count;
	}

	private int getArticleCount(File xmlFilePath) {	
		int count = 0;	
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFilePath);
			String countValue = doc.getElementsByTagName("Count").item(0).getTextContent();
			count = Integer.parseInt(countValue);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		return count;
	}

	private void runQuery(String query, File targetFile) {
		InputStream initialStream = null;
		OutputStream outStream = null;
		try {
			URL url = new URL(query);
			HttpURLConnection connection =
					(HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			initialStream = connection.getInputStream();
			outStream = new FileOutputStream(targetFile);
			IOUtils.copy(initialStream,outStream);
			outStream.close();
			initialStream.close();
			sleep();
		} catch(IOException exp) {
			System.out.println("Error occured while fetching data!");
			printMessage("Error occured while fetching data!");
		}

	}	

	public void printMessage(String message) {
		Platform.runLater(new Runnable(){
			@Override
			public void run(){
				progress.append(message);
				textArea.setText(progress.toString());
			}
		});
		sleep();
	}

	private static void sleep(){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void setProgress(double value) {
		Platform.runLater(new Runnable(){
			@Override
			public void run(){
				progressBar.setProgress(value);
				progressIndicator.setProgress(value);
			}
		});
	}
}
