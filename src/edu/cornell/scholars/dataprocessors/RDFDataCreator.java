package edu.cornell.scholars.dataprocessors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.opencsv.CSVReader;

import edu.cornell.scholars.data.Article;
import edu.cornell.scholars.data.CoAuthor;
import edu.cornell.scholars.data.ISSN;
import edu.cornell.scholars.data.UserProfile;

public class RDFDataCreator {

	private static String VCARD_NS = "http://www.w3.org/2006/vcard/ns#";
	private static String VIVO_NS = "http://vivoweb.org/ontology/core#";
	private static String BIBO_NS = "http://purl.org/ontology/bibo/";
	private static String CONTACT_INFO = "http://purl.obolibrary.org/obo/ARG_2000028";
	private static String FOAF_PERSON = "http://xmlns.com/foaf/0.1/Person";
	private static String LOCAL_NS = "";
	private static String SCHOLARS_IND = "http://scholars.cornell.edu/individual/";

	private static Integer issnColumnIndex = 0;
	private static Integer eissnColumnIndex = 0;
	private static Integer uriColumnIndex = 0;


	
	private static Map<Integer, String> jrnlMap = new HashMap<Integer, String>();
	private static Map<Integer, Article> map = new HashMap<Integer, Article>();
	private static Map<String, String> ISSNtoURImap = new HashMap<String, String>();
	private static Map<String, String> EISSNtoURImap = new HashMap<String, String>();

	private static Set<Article> data;
	private static Model rdfModel;

	public void saveDataInRDF(Set<Article> claimedArticles, UserProfile user, String outputFile, String journalMappingFilePath){

		data = claimedArticles;
		readJournalMappingFile(journalMappingFilePath);

		rdfModel = ModelFactory.createDefaultModel();	
		Resource person = createPerson(user);
		LOCAL_NS = person.getNameSpace();
		Set<Integer> randomIntegers = gen();
	
		Resource Article = rdfModel.createResource(BIBO_NS + "Article");
		Property doi = rdfModel.createProperty(BIBO_NS + "doi");
		Property issnProp = rdfModel.createProperty(BIBO_NS + "issn");
		Property eissnProp = rdfModel.createProperty(BIBO_NS + "eissn");
		Property dtv = rdfModel.createProperty(VIVO_NS + "dateTimeValue");
		Resource DTV = rdfModel.createResource(VIVO_NS + "DateTimeValue");
		Property dtv_precision = rdfModel.createProperty(VIVO_NS + "dateTimePrecision");
		Resource year_precision = rdfModel.createResource(VIVO_NS + "yearPrecision");
		Property dateTime = rdfModel.createProperty(VIVO_NS + "dateTime");
		Property hasPublicationVenue = rdfModel.createProperty(VIVO_NS + "hasPublicationVenue");
		Property publicationVenueFor = rdfModel.createProperty(VIVO_NS + "publicationVenueFor");

		for(Article article: data) {
			try {
				Integer articleId = getUniqueId(randomIntegers);
				String articleURI = LOCAL_NS + "ART"+articleId;
				Resource newArticle = rdfModel.createResource(articleURI);
				newArticle.addProperty(RDF.type, Article);
				newArticle.addProperty(RDFS.label, article.getTitle());
				if(article.getDoi() != null) {
					newArticle.addProperty(doi, article.getDoi());
				}
				Resource article_dtv = rdfModel.createResource(articleURI + "-DTV");
				newArticle.addProperty(dtv, article_dtv);
				article_dtv.addProperty(RDF.type, DTV);
				article_dtv.addProperty(dtv_precision, year_precision);

				Calendar cal = Calendar.getInstance();
				//"2011-09-01T00:00:00"^^<http://www.w3.org/2001/XMLSchema#dateTime>
				cal.set(Integer.parseInt(article.getPubYear()), 0, 1, 0, 0, 0);
				Literal pubYear = rdfModel.createTypedLiteral(cal);
				article_dtv.addProperty(dateTime, pubYear);

				int rank = findUserRank(article, user);
				for(CoAuthor author: article.getAuthors()) {
					if(author.getRank() == rank) {
						//FOAF PERSON
						addFoafPerson(person, articleURI, author, newArticle);
					}else {
						// VCARD
						addVCARD(articleURI, author, newArticle);
					}
				}

				Set<ISSN> issns = article.getIssn();
				if(issns != null && issns.size() > 0) {
					String venue = null;
					a: for(ISSN issn: issns) {
						if(ISSNtoURImap.get(issn.getValue()) != null) {
							venue = ISSNtoURImap.get(issn.getValue());
							break a;
						}else if(EISSNtoURImap.get(issn.getValue()) != null) {
							venue = EISSNtoURImap.get(issn.getValue());
							break a;
						}
					}
					if(venue != null) {
						Resource venueURI = rdfModel.createResource(venue);
						newArticle.addProperty(hasPublicationVenue, venueURI);
						venueURI.addProperty(publicationVenueFor, newArticle);
					} else { // Journal not found
						Integer uniqueId = getUniqueIdForJournal(randomIntegers);
						String jrnlURI = LOCAL_NS + "JRNL-"+ uniqueId;
						Resource venueURI = rdfModel.createResource(jrnlURI);
						populateJournalData(venueURI, article, issnProp, eissnProp);
						newArticle.addProperty(hasPublicationVenue, venueURI);
						venueURI.addProperty(publicationVenueFor, newArticle);
						
						jrnlMap.put(uniqueId, jrnlURI);
					}
				}
				
				map.put(articleId, article);
			} catch(NullPointerException |NumberFormatException exp ) {
				System.out.println("ERROR: "+article.printArticle());
				System.out.println("Continuing..");
				continue;
			}
		}
		saveRDFModel(rdfModel, outputFile);
	}

	private Integer getUniqueIdForJournal(Set<Integer> randomIntegers) {
		for(Integer i: randomIntegers){
			if(jrnlMap.get(i) == null) {
				return i;
			}
		}
		return null;
	}

	private void populateJournalData(Resource venueURI, Article article, Property issnProp, Property eissnProp) {
		Set<ISSN> issns = article.getIssn();
		for(ISSN issn: issns) {
			String type = issn.getType();
			switch(type.toUpperCase()) {
			case "PRINT":
				String pissn = issn.getValue();
				venueURI.addProperty(issnProp, pissn);
				break;
			case "ELECTRONIC":
				String eissn = issn.getValue();
				venueURI.addProperty(eissnProp, eissn);
				break;
			}
		}
		String title = article.getPublicationVenue();
		if(title != null) {
			venueURI.addProperty(RDFS.label, title);
		}
	}

	private void readJournalMappingFile(String journalMappingFilePath) {
		File file = new File(journalMappingFilePath);
		if(!file.exists()) return;
		BufferedReader br = null;
		String line = "";
		long lineCount = 0;
		try {
			br = new BufferedReader(new FileReader(file));
			a: while ((line = br.readLine()) != null) {
				lineCount++;
				if(line.trim().length() == 0) continue;
				@SuppressWarnings("resource")
				CSVReader reader = new CSVReader(new StringReader(line),',','\"');	
				String[] tokens = null;
				while ((tokens = reader.readNext()) != null) {
					try {

						if(lineCount == 1 ) {
							getColumnIndex(tokens);
							continue a;
						}

						String issn = tokens[issnColumnIndex].trim();
						String eissn = tokens[eissnColumnIndex].trim();
						String uri = tokens[uriColumnIndex].trim();

						if(issn != null && !issn.equals("null") && !issn.isEmpty()) {
							ISSNtoURImap.put(issn, uri);
						}
						if(eissn != null && !eissn.equals("null") && !eissn.isEmpty()) {
							EISSNtoURImap.put(eissn, uri);
						}
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

	private void getColumnIndex(String[] tokens) {
		for(int i = 0; i< tokens.length; i++) {
			String str = tokens[i];
			if(str.equals("issn")) {
				issnColumnIndex = i;
			} else if(str.equals("uri")) {
				uriColumnIndex = i;
			} else if(str.equals("eissn")) {
				eissnColumnIndex = i;
			}
		}
	}

	private static void addVCARD(String articleURI, CoAuthor author, Resource newArticle) {
		Property rank = rdfModel.createProperty(VIVO_NS+"rank");
		Property relates = rdfModel.createProperty(VIVO_NS+"relates");
		Property relatedBy = rdfModel.createProperty(VIVO_NS+"relatedBy");
		Resource Authorship = rdfModel.createResource(VIVO_NS+"Authorship");

		Resource Individual = rdfModel.createResource(VCARD_NS+"Individual");
		Property hasName = rdfModel.createProperty(VCARD_NS+"hasName");
		Resource Name = rdfModel.createResource(VCARD_NS+"Name");
		Property familyName = rdfModel.createProperty(VCARD_NS+"familyName");
		Property givenName = rdfModel.createProperty(VCARD_NS+"givenName");

		String authorship = articleURI+"-AUTH"+author.getRank();
		Resource auth = rdfModel.createResource(authorship);
		auth.addProperty(RDF.type, Authorship);
		auth.addProperty(rank, Integer.toString(author.getRank()));
		auth.addProperty(relates, newArticle);
		newArticle.addProperty(relatedBy, auth);

		Resource vcard = rdfModel.createResource(articleURI+"-VI"+author.getRank());
		vcard.addProperty(RDF.type, Individual);
		Resource name = rdfModel.createResource(articleURI+"-VN"+author.getRank());
		vcard.addProperty(hasName, name);
		name.addProperty(RDF.type, Name);
		name.addProperty(familyName, author.getFamilyName());
		name.addProperty(givenName,author.getGivenName());

		auth.addProperty(relates, vcard);
		vcard.addProperty(relatedBy, auth);
	}

	private static void addFoafPerson(Resource person, String articleURI, CoAuthor author, Resource newArticle) {
		Property rank = rdfModel.createProperty(VIVO_NS+"rank");
		Property relates = rdfModel.createProperty(VIVO_NS+"relates");
		Property relatedBy = rdfModel.createProperty(VIVO_NS+"relatedBy");
		Resource Authorship = rdfModel.createResource(VIVO_NS+"Authorship");

		String authorship = articleURI+"-AUTH"+author.getRank();
		Resource auth = rdfModel.createResource(authorship);
		auth.addProperty(RDF.type, Authorship);
		auth.addProperty(rank, Integer.toString(author.getRank()));
		auth.addProperty(relates, person);
		auth.addProperty(relates, newArticle);
		person.addProperty(relatedBy, auth);
		newArticle.addProperty(relatedBy, auth);
	}

	private static int findUserRank(Article article, UserProfile user) {
		int rank = 0;
		Set<CoAuthor> authors = article.getAuthors();
		Set<CoAuthor> users = new HashSet<CoAuthor>();
		for(CoAuthor author: authors) {
			if(author.getFamilyName().toUpperCase().equals(user.getFamilyName().toUpperCase())) {
				users.add(author);
			}
		}
		if(users.size() == 1){
			rank = users.iterator().next().getRank();
		}else {
			//TODO 
			//cases where multiple co-authors have same family name as the user.
		}
		return rank;
	}

	private static Integer getUniqueId(Set<Integer> randomIntegers) {
		for(Integer i: randomIntegers){
			if(map.get(i) == null) {
				return i;
			}
		}
		return null;
	}

	private static Resource createPerson(UserProfile user) {
		String per = user.getURI();
		Resource person = rdfModel.createResource(per);
		Resource Individual = rdfModel.createResource(VCARD_NS+"Individual");
		Property contactInfo = rdfModel.createProperty(CONTACT_INFO);
		Resource vcard = rdfModel.createResource(per+"-VI");
		Property hasName = rdfModel.createProperty(VCARD_NS+"hasName");
		Resource name = rdfModel.createResource(per+"-VN");
		Resource Name = rdfModel.createResource(VCARD_NS+"Name");
		Property familyName = rdfModel.createProperty(VCARD_NS+"familyName");
		Property givenName = rdfModel.createProperty(VCARD_NS+"givenName");
		//Property middleName = rdfModel.createProperty(VIVO_NS+"middleName");

		person.addProperty(RDF.type, rdfModel.createResource(FOAF_PERSON));
		person.addProperty(RDFS.label, user.getFamilyName() +", "+user.getFirstGivenName());
		person.addProperty(contactInfo, vcard);
		vcard.addProperty(RDF.type, Individual);
		vcard.addProperty(hasName, name);
		name.addProperty(RDF.type, Name);
		name.addProperty(familyName, user.getFamilyName());
		name.addProperty(givenName, user.getFirstGivenName());

		return person;
	}

	private static HashSet<Integer> gen() {
		HashSet<Integer> set = new HashSet<>();
		SecureRandom randomGenerator;
		try {
			randomGenerator = SecureRandom.getInstance("SHA1PRNG");       
			while(set.size() < data.size()){
				int s = randomGenerator.nextInt(99999);
				if(s > 9999){
					set.add(s);
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}  
		return set;
	}

	private static void saveRDFModel(Model rdfModel, String filePath){
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(filePath);
			rdfModel.write(printer, "N-Triples");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			printer.flush();
			printer.close();
		}
	}

}
