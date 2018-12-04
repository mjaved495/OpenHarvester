package harvester.crossref;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;

public class QueryCrossRefData {

	private static final String QUERY_BASE = "https://api.crossref.org/works?query.author=";
	private static final String AND = "&";
	private static final String SUMMARY = "rows=0";

	private TextArea textArea;
	private ProgressBar progressBar;
	private ProgressIndicator progressIndicator;
	
	private StringBuilder progress = new StringBuilder("");

	private int counter = 1;
	private double queryCountForProgress = 0;
	private String cursor = "*"; 

	public void process(String searchString, String outputFolder, String nextCursor, int count, 
			TextArea searchProgressTA, ProgressBar progressBar, ProgressIndicator progressIndicator) {
		
		this.textArea = searchProgressTA;
		this.progressBar = progressBar;
		this.progressIndicator = progressIndicator;
		
		if(nextCursor != null) {
			cursor = nextCursor;
			counter = count;
			try {
				IterativeQuery(outputFolder, searchString);  // search string and output folder name are same.
			} catch (org.json.simple.parser.ParseException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else {
			queryAndSaveData(searchString, outputFolder);
		}

	}

	public void queryAndSaveData(String searchString, String outputFolder){
		try {
			quereyCrossRef(outputFolder, searchString);
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}
	}

	private static void sleep(){
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void quereyCrossRef(String outputfolder , String searchString) throws org.json.simple.parser.ParseException {

		File file = new File(outputfolder);
		if (!file.exists()){
			//System.out.println("Folder created: "+file.mkdirs());
			printMessage("\nFolder created: "+file.mkdirs());
		}
		String query = QUERY_BASE+searchString+AND+SUMMARY;
		try {
			File summaryFile = new File(file, "summary.json");
			query = query.replaceAll(" ", "%20");

			//System.out.println("Query: "+query);
			printMessage("\nQuery: "+query);

			runQuery(query, summaryFile);
			Long recordCount = getRecordCount(summaryFile);
			queryCountForProgress = (recordCount/1000) + 2;
			
			//System.out.println("Expecting to have article set size:"+ recordCount);
			printMessage("\nExpecting to have article set size:"+ recordCount);
			printMessage("\nQuery Count:"+ queryCountForProgress);
			
			IterativeQuery(outputfolder, searchString);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	private void IterativeQuery(String outputfolder, String searchString) throws org.json.simple.parser.ParseException, UnsupportedEncodingException {
		do {
			//https://api.crossref.org/works?query=allen+renear&cursor=<cursor>
			cursor =  URLEncoder.encode(cursor, "UTF-8");
			String query = QUERY_BASE+searchString+AND+"rows=1000"+AND+"cursor="+cursor;
			try {
				//System.out.println("query: "+query);
				printMessage("\nQuery:"+query);

				String filename = counter+".json";
				runQuery(query, new File(outputfolder, filename));
				File file = new File(outputfolder, filename);
				cursor = getNextCursor(file);
				sleep();
				counter++;
				setProgress(counter/queryCountForProgress);	
			} catch (Exception e) {
				//System.out.println("Query did not go through:");
				//System.out.println(e.getMessage());
				printMessage("\nQuery did not go through:");
				printMessage("\n"+e.getMessage());
				break;
			}
			//System.out.println("Done.");
			printMessage("\nDONE");
		}while(cursor != null);
	}

	private String getNextCursor(File file) throws org.json.simple.parser.ParseException {
		String cursor = null;
		JSONParser parser = new JSONParser();
		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) parser.parse(new FileReader(file));
			JSONObject message = (JSONObject) jsonObject.get("message");
			JSONArray items = (JSONArray) message.get("items");

			//System.out.println("items size in json file: "+items.size());
			printMessage("\nitems size in json file:"+items.size());

			if(items.size() == 0) {
				//System.out.println("Returning with cursor null...");
				printMessage("\nReturning with cursor null...");
				return null;
			}else {
				cursor = (String) message.get("next-cursor");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return cursor;
	}

	private Long getRecordCount(File summaryFile) throws FileNotFoundException, IOException, ParseException, org.json.simple.parser.ParseException {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(summaryFile));
		JSONObject message = (JSONObject) jsonObject.get("message");
		Long count = (Long) message.get("total-results");
		//System.out.println("Record Count:"+ count);
		printMessage("\nRecord Count:"+ count);	
		return count;
	}

	private void runQuery(String query, File targetFile) throws IOException{	
		URL url = new URL(query);
		HttpURLConnection connection =
				(HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		InputStream initialStream = connection.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(initialStream, StandardCharsets.UTF_8));

		FileOutputStream outputStream = new FileOutputStream(targetFile);
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
		BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
		String line="";
		while ((line = bufferedReader.readLine()) != null) {
			bufferedWriter.write(line);
		}
		bufferedWriter.close();
		bufferedReader.close();
		initialStream.close();

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

}
