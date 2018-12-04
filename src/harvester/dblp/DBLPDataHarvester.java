package harvester.dblp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

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

public class DBLPDataHarvester {

	private String nameFile = "nameFile.xml";
	private static final String PERSON_QUERY = "https://dblp.uni-trier.de/search/author?xauthor="; // Step 1
	private static final String URLPT_QUERY =  "https://dblp.uni-trier.de/rec/pers/";
	private static final String DBLPKEY_QUERY = "https://dblp.uni-trier.de/rec/bibtex/";
	private static final String XK = "xk";
	private static int urlptCounter = 1;
	
	private  TextArea textArea;	
	private ProgressBar progressBar;
	private ProgressIndicator progressIndicator;
	
	private StringBuilder progress = new StringBuilder("");

	private double queryCountForProgress = 0;
	private static String OUTPUTFOLDER = "";
	
	public static void main(String[] args) {
		if(args.length < 2) {
			System.out.println("Run Parameters not found....");
			//return;
		}
		String searchString = 
				"David+Mimno";
				//args[0];
		String outputFolderName = 
				"/Users/mj495/Documents/DataHarvesterApp/DBLP";
				//args[1];
		
		//if(args.length > 2) {
			DBLPDataHarvester obj = new DBLPDataHarvester();
			obj.process(searchString, outputFolderName, null, null, null);
		//}
	}

	public void process(String searchString, String outputFolder, TextArea searchProgressTA, ProgressBar progressBar, ProgressIndicator progressIndicator) {
		this.textArea = searchProgressTA;
		this.progressBar = progressBar;
		this.progressIndicator = progressIndicator;
		
		File folder = new File(outputFolder);
		OUTPUTFOLDER = folder.getAbsolutePath();
		if (!folder.exists()){
			//System.out.println("Folder created: "+folder.mkdirs());
			printMessage("\nFolder created: "+folder.mkdirs());
		}
		quereyDBLP(searchString);
	}
	
	// NOT QUERYING ON THE FLY AT THIS MOMENT
		public void quereyDBLP(String searchString) {
			String query = PERSON_QUERY + searchString;
			try {
				printMessage("\n Query: "+query);
				Set<String> dblpKeys = new HashSet<String>();
				runQuery(query, new File(OUTPUTFOLDER+"/"+nameFile));
				Set<String> urlpts = readNameFile(OUTPUTFOLDER+"/"+nameFile); 
				printMessage("\nURLPT Count: "+ urlpts.size());
				int queryCounter = 0;
				queryCountForProgress = urlpts.size() * 2;
				
				for(String urlpt: urlpts) {
					String filename = processURLPTQuery((++queryCounter), urlpt);
					dblpKeys.addAll(getDBLPKeys(filename));
					Set<String> homonyms = getHomonyms(filename);
					if(homonyms.size() > 0) {
						for(String homonym: homonyms) {
							String homonysFilename = processURLPTQuery(0, homonym);
							dblpKeys.addAll(getDBLPKeys(homonysFilename));
						}
					}
					setProgress(queryCounter/queryCountForProgress);
				}// end of urlpt reader
				
				//System.out.println(dblpKeys.size());
				printMessage("\n DBLP Key Size: "+dblpKeys.size());
				
				int counter = 0;
				for(String dblpKey :dblpKeys) {
					processDBLPkey(dblpKey, ++counter);
					setProgress((double)counter/(dblpKeys.size()*2) + 0.5);
					//System.out.println((double)counter/dblpKeys.size() + 0.5);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private Set<String> readNameFile(String xmlFilePath) {		
			Set<String> urlpts = new HashSet<String>();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			try {
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(xmlFilePath);
				Node authorsNode = doc.getElementsByTagName("authors").item(0);
				Element authorsElement = (Element) authorsNode;
				NodeList authorList = authorsElement.getElementsByTagName("author");
				for(int index=0; index< authorList.getLength(); index++){
					Node authorNode = authorList.item(index);
					Element authorElement = (Element) authorNode;
					String authorName = authorElement.getTextContent();
					String urlpt = authorElement.getAttribute("urlpt");
					//TODO compare author name with queries name if(){}
					urlpts.add(urlpt);
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}
			return urlpts;
		}
		
		private void processDBLPkey(String dblpKey, int counter) {
			String query = DBLPKEY_QUERY+dblpKey+".xml";
			File filename = new File(OUTPUTFOLDER+"/"+counter+".xml");
			try {
				printMessage("\n"+query);
				runQuery(query, filename);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private static Set<String> getDBLPKeys(String xmlFilePath) {
			Set<String> dblpKeys = new HashSet<String>();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			try {
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(xmlFilePath);
				NodeList dblpNodes = doc.getElementsByTagName("dblpkey");
				if(dblpNodes != null && dblpNodes.getLength() > 0) {
					for(int index=0; index< dblpNodes.getLength(); index++){
						Node dblpNode = dblpNodes.item(index);
						Element dblpElement = (Element) dblpNode;
						if(dblpElement.getAttribute("type") == null || dblpElement.getAttribute("type").isEmpty()) {
							String key = dblpElement.getTextContent();
							dblpKeys.add(key);
						}	
					}
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}
			return dblpKeys;
		}

		private static Set<String> getHomonyms(String xmlFilePath) {
			Set<String> urlpts = new HashSet<String>();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			try {
				dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(xmlFilePath);
				NodeList homonymsNode = doc.getElementsByTagName("homonym");
				if(homonymsNode != null && homonymsNode.getLength() > 0) {
					for(int index=0; index< homonymsNode.getLength(); index++){
						Node homonymNode = homonymsNode.item(index);
						String urlpt = homonymNode.getTextContent();
						urlpts.add(urlpt);
					}
				}
			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			}
			return urlpts;
		}

		private String processURLPTQuery(int counter, String urlpt) throws IOException {
			String urlptQuery = URLPT_QUERY + urlpt +"/"+ XK;
			
			if(counter > 0) {
				printMessage("\n"+counter+"-urlptQuery:"+ urlptQuery);
			}else {
				printMessage("\nhomonym-urlptQuery:"+ urlptQuery);
			}
			
			String filename = OUTPUTFOLDER+"/urlpt-"+(++urlptCounter)+".xml";
			runQuery(urlptQuery, new File(filename));
			return filename;
		}
		
		private void runQuery(String query, File targetFile) throws IOException{
			URL url = new URL(query);
			HttpURLConnection connection =
					(HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			InputStream initialStream = connection.getInputStream();
			OutputStream outStream = new FileOutputStream(targetFile);
			IOUtils.copy(initialStream,outStream);
			initialStream.close();
			outStream.close();
			sleep();
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

		private void setProgress(double value) {
			Platform.runLater(new Runnable(){
				@Override
				public void run(){
					progressBar.setProgress(value);
					progressIndicator.setProgress(value);
				}
			});
		}
		
		private static void sleep(){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
}

	