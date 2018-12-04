package edu.cornell.scholars.crossref;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import com.opencsv.CSVReader;

import application.Controller;
import application.Main;
import edu.cornell.scholars.data.Affiliation;
import edu.cornell.scholars.data.Article;
import edu.cornell.scholars.data.CoAuthor;
import edu.cornell.scholars.data.DataSource;
import edu.cornell.scholars.data.Funder;
import edu.cornell.scholars.data.ISSN;
import edu.cornell.scholars.data.Identifier;
import edu.cornell.scholars.data.UserProfile;
import edu.cornell.scholars.scopus.ScopusDataHarvester;
import edu.cornell.scholars.scopus.ScopusFileReader;
import harvester.crossref.QueryCrossRefData;
import harvester.dblp.DBLPDataHarvester;
import harvester.pubmed.PubmedDataHarvester;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;

public class CrossRefDataExtracter { 

	private static final String QUERY_BASE = "https://api.crossref.org/works?query.author=";
	private static final String AND = "&";
	private static final String SUMMARY = "rows=0";

	private static final String JSONFILEFOLDER = "CROSSREF/" ; 
	private static String personFolder = "";
	private static String scopusFolder = "/Users/mj495/Documents/DataHarvesterApp/scopus";
	
	private static String AUTHOR = "";  // default author 

	public static Set<String> allDois =  new HashSet<String>();
	public static Set<String> scholarsDois = new HashSet<String>();

	private Label progressMonitor;
	
	public Set<Article> processJSONFilesData(String authorFolder, Map<String, JSONObject> map, Label progressMonitor){
		Set<Article> articles_data = new HashSet<Article>();
		AUTHOR = authorFolder;
		this.progressMonitor = progressMonitor;
		
		readCSVFile(new File(Main.mainFolder, Controller.profile.getFamilyName()+".csv"));
		
		articles_data = readJSONFiles(authorFolder, map);
		
		System.out.println("Articles count: "+articles_data.size());
		return articles_data;
	}

	public void readCSVFile(File file) {
		if(!file.exists()) return;
		BufferedReader br = null;
		String line = "";
		long lineCount = 0;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				lineCount++;
				if(line.trim().length() == 0) continue;
				@SuppressWarnings("resource")
				CSVReader reader = new CSVReader(new StringReader(line),',','\"');	
				String[] tokens;
				while ((tokens = reader.readNext()) != null) {
					try {
						//String title = tokens[0];
						String doi = tokens[1].toLowerCase();
						String scopusId = tokens[2].substring(tokens[2].lastIndexOf("-")+1);
						Controller.scopusdocmap.put(doi, scopusId+".xml");
					}catch (ArrayIndexOutOfBoundsException exp) {
						for (String s : tokens) {
							System.out.println("Exception: "+ lineCount+" :"+ s);
						}
						System.out.println(); 
						continue;
					}
				}
			}
		}catch (FileNotFoundException e) {
			System.err.println(line);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(line);
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Set<Article> readJSONFiles(String folderPath, Map<String, JSONObject> map) {
		File folder = new File(folderPath); 
		File files[] = folder.listFiles();
		Set<Article> articles = new HashSet<Article>();
		System.out.println(files.length);
		int count = 0;
		for(File file: files){
			try {
				if(!file.getName().endsWith(".json") || file.getName().equals("summary.json")) continue;
				System.out.println("Processing file:"+ (++count));
				String message = "Processing file:"+ (count);
				
				//printMessage(message);
			
				articles.addAll(processFile(file.getAbsolutePath(), map));
				System.out.println("Filtered Article count: "+ articles.size());
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
		}
		System.out.println("files processed: "+ count);
		return articles;
	}

	public Set<Article> processFile(String jsonFilePath, Map<String, JSONObject> map) throws FileNotFoundException, IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(jsonFilePath));
		JSONObject message = (JSONObject) jsonObject.get("message");
		JSONArray items = (JSONArray) message.get("items");
		@SuppressWarnings("unchecked")
		Iterator<Object> iterator = items.iterator();
		Set<Article> articles = new HashSet<Article>();
		System.out.println("articles count in file:"+items.size());
		while (iterator.hasNext()) {
			Article article = new Article();
			String title = null;

			JSONObject item = (JSONObject) iterator.next();

			// IT IS AN ARRAY - FOR NOW GETTING FIRST ENTRY
			JSONArray titleArray = (JSONArray) item.get("title");
			if(titleArray != null){
				@SuppressWarnings("unchecked")
				Iterator<Object> titleIt = titleArray.iterator();
				//System.out.println("\t-- TITLES ");
				while (titleIt.hasNext()) {
					title = (String) titleIt.next();
					//System.out.println("\t\t"+title);
					if(title != null && !title.isEmpty() && !title.equals("null")) {
						article.setTitle(title);
						break;
					}
				}
			}
			
			if(article.getTitle() == null || article.getTitle().equals("null")) continue;
			
			String DOI = (String) item.get("DOI");
			//System.out.println(DOI);
			allDois.add(DOI.toLowerCase());

			article.setDoi(DOI);
			article.addIdentifier(new Identifier(DataSource.CROSSREF.getDatasource(), DOI));

			String type = (String) item.get("type");
			//System.out.println("\ttype: "+ type);
			article.setType(type);

			JSONArray ISBNArray = (JSONArray) item.get("ISBN");
			if(ISBNArray != null){
				@SuppressWarnings("unchecked")
				Iterator<Object> isbnIt = ISBNArray.iterator();
				while (isbnIt.hasNext()) {
					String isbn = (String)isbnIt.next();
					//System.out.println("\tISBN: "+ isbn);
					article.addIsbn(isbn);
				}
			}
			
			//Subject
			String subjectArea = null;
			JSONArray subjectArray = (JSONArray) item.get("subject");
			if(subjectArray != null){
				@SuppressWarnings("unchecked")
				Iterator<String> subjectIt = subjectArray.iterator();
				//System.out.println("\t-- Subject ");
				while (subjectIt.hasNext()) {
					subjectArea = subjectIt.next();
					//System.out.println("\t\t"+subjectArea);
					if(subjectArea != null && !subjectArea.isEmpty() && !subjectArea.equals("null")) {
						article.addSubject(subjectArea);
					}
				}
			}
			
			if(article.getDoi().equalsIgnoreCase("10.1094/MPMI-09-11-0240")){
				System.out.println(DOI);
			}

			JSONObject onlinePubDate = (JSONObject) item.get("published-online");
			if(onlinePubDate != null){
				JSONArray onlinePubYear = (JSONArray) onlinePubDate.get("date-parts");
				if(onlinePubYear != null){
					@SuppressWarnings("unchecked")
					Iterator<Object> onlinePubYrIt = onlinePubYear.iterator();
					JSONArray dataArray = (JSONArray) onlinePubYrIt.next();
					if(dataArray != null){
						@SuppressWarnings("unchecked")
						Iterator<Object> dataArrayIt = dataArray.iterator();
						Long year = (Long) dataArrayIt.next();
						if(year != null){
							//System.out.println("\tPublished-Online: "+ year);
							article.setPublished_online(year);
							article.setPubYear(Long.toString(year));
						}
					}
				}
			}

			JSONObject printPubDate = (JSONObject) item.get("published-print");
			if(printPubDate != null){
				JSONArray printPubYear = (JSONArray) printPubDate.get("date-parts");
				if(printPubYear != null){
					@SuppressWarnings("unchecked")
					Iterator<Object> printPubYrIt = printPubYear.iterator();
					JSONArray dataArray = (JSONArray) printPubYrIt.next();
					if(dataArray != null){
						@SuppressWarnings("unchecked")
						Iterator<Object> dataArrayIt = dataArray.iterator();
						Long year = (Long) dataArrayIt.next();
						if(year != null){
							//System.out.println("\tPublished-Print: "+ year);
							article.setPublished_print(year);
							article.setPubYear(Long.toString(year));
						}
					}
				}
			}

			JSONArray issnArray = (JSONArray) item.get("issn-type");
			if(titleArray != null && issnArray != null){
				@SuppressWarnings("unchecked")
				Iterator<Object> issnIt = issnArray.iterator();
				//System.out.println("\t-- ISSN ");
				while (issnIt.hasNext()) {
					JSONObject issnEntry= (JSONObject) issnIt.next();
					String issnType = (String) issnEntry.get("type");
					String issnValue = (String) issnEntry.get("value");
					ISSN issn = new ISSN(issnType, issnValue);
					article.addIssn(issn);
				}
			}
			
			int order = 0;
			JSONArray authorArray = (JSONArray) item.get("author");
			if(titleArray != null && authorArray != null){
				@SuppressWarnings("unchecked")
				Iterator<Object> authorIt = authorArray.iterator();
				//System.out.println("\t-- AUTHORS ");
				while (authorIt.hasNext()) {
					CoAuthor coauthor = new CoAuthor();
					JSONObject author = (JSONObject) authorIt.next();
					String authorGivenName = (String) author.get("given");
					if(authorGivenName!= null && authorGivenName.contains("\\u")) {
						authorGivenName = URLDecoder.decode(authorGivenName, "UTF-8");
					}
					String authorFamilyName = (String) author.get("family");
					if(authorFamilyName!= null && authorFamilyName.contains("\\u")) {
						authorFamilyName = URLDecoder.decode(authorFamilyName, "UTF-8");
					}
					coauthor.setFamilyName(authorFamilyName);
					coauthor.setGivenName(authorGivenName);
					coauthor.setRank(++order);

					JSONArray authoraffiliationArray = (JSONArray) author.get("affiliation");
					Set<String> affiliations = new HashSet<String>();
					if(authoraffiliationArray != null){
						@SuppressWarnings("unchecked")
						Iterator<Object> affIt = authoraffiliationArray.iterator();
						while (affIt.hasNext()) {
							JSONObject affiliationObject = (JSONObject) affIt.next();
							String affiliationName = (String) affiliationObject.get("name");
							affiliations.add("\""+affiliationName+"\"");
							Affiliation aff = new Affiliation();
							aff.setName(affiliationName);
							coauthor.addAffiliation(aff);
						}	
					}
					//System.out.println("\t\t: "+authorFamilyName+", "+authorGivenName+"("+affiliations.toString()+")");
					article.addAuthor(coauthor);
				}
			}

			JSONArray journalArray = (JSONArray) item.get("container-title");
			if(journalArray != null) {
				@SuppressWarnings("unchecked")
				Iterator<String> jrnlIt = journalArray.iterator();
				while (jrnlIt.hasNext()) {
					String jrnl = (String) jrnlIt.next();
					article.setPublicationVenue(jrnl);
				}
			}
			if(journalArray == null) {  // in cases where container-title is not available, 
				// plan b to get journal label.
				JSONArray journalArray2 = (JSONArray) item.get("short-container-title");
				if(journalArray2 != null) {
					@SuppressWarnings("unchecked")
					Iterator<String> jrnlIt = journalArray2.iterator();
					while (jrnlIt.hasNext()) {
						String jrnl = (String) jrnlIt.next();
						article.setPublicationVenue(jrnl);
					}
				}
			}
			
			
			String publisherName = (String) item.get("publisher");
			//System.out.println("\tpublisher: "+publisherName);
			article.setPublisher(publisherName);

			String publisherLocation = (String) item.get("publisher-location");
			//System.out.println("\tpublisher-location: "+publisherLocation);
			article.setPublisher_location(publisherLocation);

			Long citationCount = (Long) item.get("is-referenced-by-count");
			//System.out.println("\tis-referenced-by-count: "+citationCount);
			article.setCitationCount(citationCount);

			JSONArray funderArray = (JSONArray) item.get("funder");
			if(funderArray != null){
				@SuppressWarnings("unchecked")
				Iterator<Object> funderIt = funderArray.iterator();
				//System.out.println("\t-- FUNDERS ");
				while (funderIt.hasNext()) {
					Funder fun = new Funder();
					JSONObject funder = (JSONObject) funderIt.next();
					String funderName = (String) funder.get("name");
					String funderDOI = (String) funder.get("DOI");
					//System.out.println("\t\tname: "+funderName);
					//System.out.println("\t\tDOI: "+funderDOI);
					fun.setName(funderName);
					fun.setDoi(funderDOI);
					article.addFunder(fun);
				}
			}
			article.setRank(0);
			article.setSource(DataSource.CROSSREF.getDatasource());
			
			if(Controller.allPubMap.get(title) == null) {
				if(authorArray == null) {
					//populateAuthorsData(article); // from Scopus
				}
				if(isUserFound(Controller.profile, article.getAuthors())) {
					//populateAffiliationData(article); // from Scopus
					articles.add(article);
					Controller.allPubMap.put(title, article);
				}
			}
		}
		
		System.out.println(jsonFilePath+": article count:"+ articles.size());
		return articles;
	}

	private boolean isUserFound(UserProfile profile, Set<CoAuthor> authors) {
		try {
			for(CoAuthor author: authors) { 
				if(author.getFamilyName().trim().equalsIgnoreCase(profile.getFamilyName().trim())) {
					Set<String> givenNames = profile.getGivenName();
					for(String givenName: givenNames) {
						String initials = givenName.substring(0, 1).toUpperCase();
						if(author.getGivenName().toUpperCase().startsWith(initials)) {
							return true;
						}
					}
				} 
			}
		}catch (NullPointerException exp){
			return false;
		}
		return false;
	}

	public boolean containsSpecialCharacter(String s) {
	    return (s == null) ? false : s.matches("[^A-Za-z0-9 ]");
	}
	
	private void populateAuthorsData(Article article) {
		if(article.getTitle() == null) return;
		String scopusFilename = Controller.scopusdocmap.get(article.getDoi().toLowerCase());
		String scopusFolder = personFolder+"/scopus";
		if(scopusFilename == null && Main.searchScopus) {
			scopusFilename = ScopusDataHarvester.qeuryScopusWithDOI(article.getDoi(), scopusFolder);
		}
		if (scopusFilename == null) return;
		String scopusFile = scopusFolder+"/"+scopusFilename;
		article.setScopusId(scopusFilename.substring(0, scopusFilename.indexOf(".xml")));
		ScopusFileReader.populateAuthorsData(article, scopusFile);
		System.out.println(article.getDoi()+"- Authors filled in from Scopus.");
	}

	private void populateAffiliationData(Article article) {
		if(article.getTitle() == null) return;
		String scopusFilename = Controller.scopusdocmap.get(article.getDoi().toLowerCase());
		if(scopusFilename == null && Main.searchScopus) {
			scopusFilename = ScopusDataHarvester.qeuryScopusWithDOI(article.getDoi(), scopusFolder);
		}
		if (scopusFilename == null) return;
		String scopusFile = scopusFolder+"/"+scopusFilename;
		article.setScopusId(scopusFilename.substring(0, scopusFilename.indexOf(".xml")));
		try {
			Map<String, String> affiliations;
			try {
				affiliations = ScopusFileReader.readInputFileAndGenerateMaps(article.getDoi(), scopusFile);
				fillAffiliations(affiliations, article);
				System.out.println(article.getDoi()+"- Affiliations filled in from Scopus.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}

	}

	private void fillAffiliations(Map<String, String> affiliations, Article article) {
		Set<CoAuthor> authors = article.getAuthors();
		for(CoAuthor author: authors) {
			int rank = author.getRank();
			String aff = affiliations.get(Integer.toString(rank));
			if (aff != null && !aff.isEmpty()){
				author.addAffiliation(new Affiliation(aff));
			}
		}
	}


	public void printMessage(String message) {
		Platform.runLater(new Runnable(){
			@Override
			public void run(){
				progressMonitor.setText(message);
			}
		});
		sleep(100);
	}
	
	private static void sleep(long time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void quereyCrossRef(String outputfolder) {
		File file = new File(outputfolder);
		if (!file.exists()){
			System.out.println("Folder created: "+file.mkdirs());
		}
		String query = QUERY_BASE+AUTHOR+AND+SUMMARY;
		try {
			File summaryFile = new File(file, "summary.json");
			query = query.replaceAll(" ", "%20");
			System.out.println("Query: "+query);
			runQuery(query, summaryFile);
			Long recordCount = getRecordCount(summaryFile);
			System.out.println("Expecting to have article set size:"+ recordCount);
			IterativeQuery(outputfolder);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	private void IterativeQuery(String outputfolder) {
		int counter = 1;
		String cursor = "*";
		do {
			//https://api.crossref.org/works?query=allen+renear&cursor=<cursor>
			String query = QUERY_BASE+AUTHOR+AND+"cursor="+cursor;
			try {
				System.out.println("query: "+query);
				String filename = counter+".json";
				runQuery(query, new File(outputfolder, filename));
				File file = new File(outputfolder, filename);
				cursor = getNextCursor(file);
				sleep(1000);
				cursor = URLEncoder.encode(cursor, "UTF-8");
				counter++;
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Done.");
		}while(cursor != null);
	}

	private String getNextCursor(File file) {
		String cursor = null;
		JSONParser parser = new JSONParser();
		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) parser.parse(new FileReader(file));
			JSONObject message = (JSONObject) jsonObject.get("message");
			JSONArray items = (JSONArray) message.get("items");
			System.out.println("items size in json file: "+items.size());
			if(items.size() == 0) {
				System.out.println("Returning with cursor null...");
				return null;
			}else {
				cursor = (String) message.get("next-cursor");
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}	
		return cursor;
	}

	private Long getRecordCount(File summaryFile) throws FileNotFoundException, IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(summaryFile));
		JSONObject message = (JSONObject) jsonObject.get("message");
		Long count = (Long) message.get("total-results");
		System.out.println("Record Coun:"+ count);
		return count;
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
	}
}
