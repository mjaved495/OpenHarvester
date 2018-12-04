package edu.cornell.scholars.pubmed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.opencsv.CSVReader;

import application.Controller;
import application.Main;
import edu.cornell.scholars.data.Affiliation;
import edu.cornell.scholars.data.Article;
import edu.cornell.scholars.data.CoAuthor;
import edu.cornell.scholars.data.DataSource;
import edu.cornell.scholars.data.ISSN;
import edu.cornell.scholars.data.Identifier;
import edu.cornell.scholars.data.UserProfile;
import edu.cornell.scholars.scopus.ScopusDataHarvester;
import edu.cornell.scholars.scopus.ScopusFileReader;

public class PubmedDataExtractor {

	private static Integer doiColumnIndex = 0;
	private static  Integer scopusColumnIndex = 0;

	public static void main(String args[]) {
		//		PubmedDataExtractor obj = new PubmedDataExtractor();
		//		String searchString = "Muhammad+Javed";
		//		Set<Article> articles = obj.process(searchString);
	}

	public Set<Article> process(String inputFolder) {
		Set<Article> articles_data = new HashSet<Article>();
		File folder = new File(inputFolder);
		if(!folder.exists()){
			System.out.println(folder.getAbsolutePath() +" does not exist... returning");
			return articles_data;
		}

		readCSVFile(new File(Main.mainFolder, Controller.profile.getFamilyName()+".csv"));

		articles_data = readPubmedFiles(folder);

		return articles_data;
	}

	//	public Set<Article> process(String searchString) {
	//		Set<Article> articles_data = new HashSet<Article>();
	//		personFolder = Main.mainFolder+"/"+XMLFILEFOLDER +searchString;
	//		File folder = new File(personFolder);
	//		if(!folder.exists()){
	//			System.out.println(folder.getAbsolutePath() +" does not exist... returning");
	//			return articles_data;
	//		}
	//		articles_data = readPubmedFiles(folder);
	//		return articles_data;
	//	}


	private Set<Article> readPubmedFiles(File folder) {
		Set<Article> articles = new HashSet<Article>();
		File[] files = folder.listFiles();
		for(File file: files){
			if(file.getName().equals(".DS_Store") 
					|| file.isDirectory() 
					|| file.getName().contains("Pubmed") 
					|| !file.getName().endsWith(".xml")) continue;
			//String fileName = file.getName().substring(0, file.getName().indexOf(".xml"));
			//if(isInteger(fileName)) {
			System.out.println("processing..."+file.getName());
			Set<Article> articleSet = readArticleObjects(file);
			articles.addAll(articleSet);
			//}
		}
		return articles;
	}



	private Set<Article> readArticleObjects(File xmlFilePath) {
		Set<Article> articles = new HashSet<Article>();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFilePath);
			NodeList pubmedArticleNodes = doc.getElementsByTagName("PubmedArticle");
			for(int index=0; index< pubmedArticleNodes.getLength(); index++){
				Article article = new Article();
				Node pubmedArticleNode = pubmedArticleNodes.item(index);
				Element pubmedArticleElement = (Element) pubmedArticleNode;
				Node medlineN = pubmedArticleElement.getElementsByTagName("MedlineCitation").item(0);
				Element medlineE = (Element) medlineN;
				String pmid = medlineE.getElementsByTagName("PMID").item(0).getTextContent();

				article.addIdentifier(new Identifier(DataSource.PUBMED.getDatasource(), pmid));
				Node articleN = medlineE.getElementsByTagName("Article").item(0);
				Element articleE = (Element) articleN;
				String title = articleE.getElementsByTagName("ArticleTitle").item(0).getTextContent();
				article.setTitle(title);
				//System.out.println(title);
				if(pmid.equals("29846660")) {
					System.out.println(title);
				}

				Node dateN = pubmedArticleElement.getElementsByTagName("PubMedPubDate").item(0);
				Element dateE = (Element) dateN;
				String date = dateE.getElementsByTagName("Year").item(0).getTextContent();
				//System.out.println(date);
				if(date != null && !date.isEmpty()) {
					article.setPubYear(date);
				}
				String doi = getDOI(pubmedArticleElement);
				if(doi != null){
					article.setDoi(doi);
					article.addIdentifier(new Identifier(DataSource.CROSSREF.getDatasource(), doi));
				}

				NodeList langNodes = articleE.getElementsByTagName("Language");
				for(int index0 = 0; index0 < langNodes.getLength(); index0++){
					String lang = langNodes.item(index0).getTextContent();
					article.setLanguage(lang);
				}

				Node journalN = articleE.getElementsByTagName("Journal").item(0);
				Element journalElement = (Element) journalN;
				Node issn = journalElement.getElementsByTagName("ISSN").item(0);
				Element issnElement = (Element) issn;
				if(issnElement != null && issnElement.getAttribute("IssnType") != null) {
					String issnType = issnElement.getAttribute("IssnType");  // Electronic OR 
					String issnValue = issnElement.getTextContent();
					ISSN issnObj = new ISSN(issnType, issnValue);
					article.addIssn(issnObj);
				}
				String journalTitle = journalElement.getElementsByTagName("Title").item(0).getTextContent();
				article.setPublicationVenue(journalTitle);

				Node journalIssueN = journalElement.getElementsByTagName("JournalIssue").item(0);
				Element journalIssueE = (Element) journalIssueN;
				Node volumeN = journalIssueE.getElementsByTagName("Volume").item(0);
				String volume = volumeN.getTextContent();
				article.setVolume(volume);

				Node PubTypeN = articleE.getElementsByTagName("PublicationTypeList").item(0);
				Element pubtypeE = (Element) PubTypeN;
				NodeList pubTypeNodes = pubtypeE.getElementsByTagName("PublicationType");
				for(int index1 = 0; index1 < pubTypeNodes.getLength(); index1++){
					Node typeNode = pubTypeNodes.item(index1);
					Element typeElement = (Element) typeNode;
					String docType = typeElement.getTextContent();
					article.setType(docType);
					if(docType.equals("Journal Article")) break;
				}

				Node paginationN = articleE.getElementsByTagName("Pagination").item(0);
				if(paginationN != null) {
					Element paginationE = (Element) paginationN;
					Node medN = paginationE.getElementsByTagName("MedlinePgn").item(0);
					Element medE = (Element) medN;
					String pagination = medE.getTextContent();
					article.setPagination(pagination);
				}

				Node authorListN = articleE.getElementsByTagName("AuthorList").item(0);
				Element authorListE = (Element) authorListN;
				NodeList authorNodes = authorListE.getElementsByTagName("Author");
				for(int index1 = 0; index1 < authorNodes.getLength(); index1++){

					CoAuthor author = new CoAuthor();
					author.setRank(index1+1);

					Node authorNode = authorNodes.item(index1);
					Element authorElement = (Element) authorNode;

					NodeList fName = authorElement.getElementsByTagName("LastName");

					if(fName != null && fName.getLength()>0) {
						String familyName = authorElement.getElementsByTagName("LastName")
								.item(0).getTextContent();
						author.setFamilyName(familyName);
					}

					NodeList gName = authorElement.getElementsByTagName("ForeName");
					if(gName != null && gName.getLength()>0) {
						String givenName = authorElement.getElementsByTagName("ForeName")
								.item(0).getTextContent();
						author.setGivenName(givenName);
					}

					Node affInfoNode = authorElement.getElementsByTagName("AffiliationInfo").item(0);
					Element affInfoElement = (Element) affInfoNode;	
					if(affInfoElement != null) {
						NodeList affiliationList = affInfoElement.getElementsByTagName("Affiliation");
						Set<Affiliation> affiliations = new HashSet<Affiliation>();
						for(int index2 = 0; index2<affiliationList.getLength(); index2++){
							Node affNode = affiliationList.item(index2);
							Element affElement = (Element) affNode;	
							String affName = affElement.getTextContent();
							Affiliation aff = new Affiliation();
							aff.setName(affName);
							affiliations.add(aff);
						}
						if(affiliations.size()>0) {
							author.setAffiliations(affiliations);
						}
					}
					article.addAuthor(author);
				}

				if(Controller.allPubMap.get(title) == null) {
					if(isUserFound(Controller.profile, article.getAuthors()) && article.getDoi() != null) {
						populateAffiliationData(article);
					}
					articles.add(article);
					Controller.allPubMap.put(title, article);
				}

				article.setSource(DataSource.PUBMED.getDatasource());
				articles.add(article);
			}

		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
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

	public void readCSVFile(File file) {
		if(!file.exists()) return;
		BufferedReader br = null;
		String line = "";
		long lineCount = 0;

		try {
			br = new BufferedReader(new FileReader(file));
			a:	while ((line = br.readLine()) != null) {
				lineCount++;
				if(line.trim().length() == 0) continue;

				@SuppressWarnings("resource")
				CSVReader reader = new CSVReader(new StringReader(line),',','\"');	
				String[] tokens = null;
				while ((tokens = reader.readNext()) != null) {
					if(lineCount == 1 ) {
						getColumnIndex(tokens);
						continue a;
					}

					try {
						//String title = tokens[0];
						String doi = tokens[doiColumnIndex].toLowerCase();
						String scopusId = tokens[scopusColumnIndex].substring(tokens[2].lastIndexOf("-")+1);
						Controller.scopusdocmap.put(doi, scopusId+".xml");
					}catch (ArrayIndexOutOfBoundsException|StringIndexOutOfBoundsException exp) {
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

	private void getColumnIndex(String[] tokens) {
		for(int i = 0; i< tokens.length; i++) {
			String str = tokens[i];
			if(str.equals("crossrefId")) {
				doiColumnIndex = i;
			}else if(str.equals("scopusId")) {
				scopusColumnIndex = i;
			}
		}
	}

	private void populateAffiliationData(Article article) {
		if(article.getTitle() == null) return;
		String scopusFilename = Controller.scopusdocmap.get(article.getDoi().toLowerCase());
		//String scopusFolder = personFolder+"/scopus";
		String scopusFolder = "/scopus";
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
				//System.out.println(affiliations.size());
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

	private String getDOI(Element pubmedArticleElement) {
		String doi = null;
		Node medlineN = pubmedArticleElement.getElementsByTagName("MedlineCitation").item(0);
		Element medlineE = (Element) medlineN;
		Node articleN = medlineE.getElementsByTagName("Article").item(0);
		Element articleE = (Element) articleN;
		NodeList eLocations = articleE.getElementsByTagName("ELocationID");
		if(eLocations != null) {
			for(int index = 0; index< eLocations.getLength(); index++) {
				Node eLocation = eLocations.item(index);
				Element eLocationElement = (Element) eLocation;
				if(eLocationElement.getAttribute("EIdType") != null &&
						eLocationElement.getAttribute("EIdType").equals("doi")) {
					doi = eLocationElement.getTextContent();
				}
			}
		}
		if(doi == null){  // OPTION B for DOI
			Node pubmedDataN = pubmedArticleElement.getElementsByTagName("PubmedData").item(0);
			Element pubmedDataE = (Element) pubmedDataN;
			Node articleIdListN = pubmedDataE.getElementsByTagName("ArticleIdList").item(0);
			Element articleIdListE = (Element) articleIdListN;
			NodeList articleIdNodes = articleIdListE.getElementsByTagName("ArticleId");
			if(articleIdNodes != null) {
				for(int index1 = 0; index1 < articleIdNodes.getLength(); index1++) {
					Node articleIdNode = articleIdNodes.item(index1);
					Element articleIdE = (Element) articleIdNode;
					if(articleIdE.getAttribute("IdType") != null &&
							articleIdE.getAttribute("IdType").equals("doi")) {
						doi = articleIdE.getTextContent();
					}
				}
			}	
		}
		return doi;
	}

	public static boolean isInteger(String s) {
		return isInteger(s,10);
	}

	public static boolean isInteger(String s, int radix) {
		if(s.isEmpty()) return false;
		for(int i = 0; i < s.length(); i++) {
			if(i == 0 && s.charAt(i) == '-') {
				if(s.length() == 1) return false;
				else continue;
			}
			if(Character.digit(s.charAt(i),radix) < 0) return false;
		}
		return true;
	}
}
