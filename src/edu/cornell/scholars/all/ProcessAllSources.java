package edu.cornell.scholars.all;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import application.Controller;
import edu.cornell.scholars.crossref.CrossRefDataExtracter;
import edu.cornell.scholars.data.Affiliation;
import edu.cornell.scholars.data.Article;
import edu.cornell.scholars.data.CoAuthor;
import edu.cornell.scholars.data.DataSource;
import edu.cornell.scholars.dblp.DblpDataExtracter;
import edu.cornell.scholars.pubmed.PubmedDataExtractor;

public class ProcessAllSources {

	private static Map<String, Article> pubmedMap = new HashMap<String, Article>();
	private static Map<String, Article> crosrefMap = new HashMap<String, Article>();
	
//	public static Set<Article> searchALLPublications() {
//
//		CrossRefDataExtracter queryxref = new CrossRefDataExtracter();
//		Set<Article> crossrefArticles = 
//				queryxref.processJSONFilesData(Controller.profile.getFirstGivenName().trim()+"+"+Controller.profile.getFamilyName().trim(), null);
//		generateCrossRefDataMap(crossrefArticles);
//		
//		PubmedDataExtractor obj = new PubmedDataExtractor();
//		Set<Article> pubmedArticles = 
//				obj.process(Controller.profile.getFirstGivenName()+"+"+Controller.profile.getFamilyName(), null);
//
//		DblpDataExtracter dblp = new DblpDataExtracter();
//		Set<Article> dblpAriclest = dblp.process(Controller.profile.getFirstGivenName(), null);
//		
//		//Not uberizing DBLP data so far but adding them as a separate entry.
//		
//		Map<String, String> doi2pubmedMap = getDoiToPubmedMap(pubmedArticles);
//		Set<Article> mergedArticles = mergePublications(crossrefArticles, pubmedArticles, dblpAriclest, doi2pubmedMap);
//		
//		
//		Map<String, Article> mergeMap = new HashMap<String, Article>();
//		for(Article art : mergedArticles) {
//			mergeMap.put(art.getTitle(), art);
//			if(art.getDoi() != null && art.getDoi().equals("10.1128/aem.04257-14")) {
//				System.out.println(art.toString());
//			}
//		}
//		Controller.allPubMap = mergeMap;
//		
//		return mergedArticles;
//	}

	private static void generateCrossRefDataMap(Set<Article> crossrefArticles) {
		System.out.println("Crossref count: "+crossrefArticles.size());
		for(Article art: crossrefArticles) {
			//System.out.println(art.toString());
			crosrefMap.put(art.getDoi(), art);
		}
		System.out.println("Crossref Map count: "+crosrefMap.size());
	}

//	private static Set<Article> mergePublications(Set<Article> crossrefArticles, 
//			Set<Article> pubmedArticles, 
//			Set<Article> dbplArticles,
//			Map<String, String> doi2pubmedMap) {
//		
//		Set<Article> mergedArticles = new HashSet<Article>();
//		
//		Set<String> sharedDOIs = doi2pubmedMap.keySet();
//		Collection<String> sharedPubmedIds = doi2pubmedMap.values();
//		
//		mergedArticles.addAll(dbplArticles);
//		
//		int count = 0;
//		for(Article art: crossrefArticles){
//			if(!sharedDOIs.contains(art.getIdentifiers(DataSource.CROSSREF.getDatasource()))) {
//				if(art.getDoi() != null && art.getDoi().equals("10.1128/aem.04257-14")) {
//					System.out.println(art.toString());
//				}
//				mergedArticles.add(art);
//				count++;
//			}
//		}
//		System.out.println("Distinct Articles from CrossRef: "+count);
//		
//		count = 0;
//		for(Article art: pubmedArticles){
//			if(!sharedPubmedIds.contains(art.getIdentifiers(DataSource.PUBMED.getDatasource()))) {
//				if(art.getDoi() != null && art.getDoi().equals("10.1128/aem.04257-14")) {
//					System.out.println(art.toString());
//				}
//				mergedArticles.add(art);
//				count++;
//			}
//		}
//		System.out.println("Distinct Articles from Pubmed: "+count);
//		
//		count = 0;
//		for(Article art: crossrefArticles){
//			Article mergedArticle = new Article(art);
//			String doi = art.getIdentifiers(DataSource.CROSSREF.getDatasource());
//			if(art.getDoi() != null && art.getDoi().equals("10.1128/aem.04257-14")) {
//				System.out.println(art.toString());
//			}
//			
//			if(sharedDOIs.contains(doi)) {
//				Article pubmedVersion = pubmedMap.get(doi2pubmedMap.get(doi));
//				Set<CoAuthor> crossRefAuthors = art.getAuthors();
//				Set<CoAuthor> pubmedAuthors = pubmedVersion.getAuthors();
//				if(crossRefAuthors.size() == pubmedAuthors.size()) {
//					for(CoAuthor author: crossRefAuthors) {
//						CoAuthor copyCoAuthor = new CoAuthor(author);
//						int rank = author.getRank();
//						Set<Affiliation> pubmedAffiliation = getCorrespondingPubmedAffiliation(pubmedAuthors, rank);
//						copyCoAuthor.setAffiliations(pubmedAffiliation);
//						mergedArticle.addAuthor(copyCoAuthor); // get new co authors data
//					}
//				}else {
//					mergedArticle.setAuthors(crossRefAuthors); // keep the old data
//					System.out.println("Author Count do not match: "+doi+"-"+doi2pubmedMap.get(doi));
//				}
//				mergedArticle.setSource(DataSource.ALL.getDatasource());
//				count++;
//				mergedArticles.add(mergedArticle);
//			}
//		}
//		System.out.println("Shared Articles count: "+count);
//		
//		System.out.println("Complete Articles Set size: "+mergedArticles.size());
//		return mergedArticles;
//	}

	private static Set<Affiliation> getCorrespondingPubmedAffiliation(Set<CoAuthor> pubmedAuthors, int rank) {
		for(CoAuthor author : pubmedAuthors){
			if(author.getRank() == rank) {
				return author.getAffiliations();
			}
		}
		return null;
	}

	private static Map<String, String> getDoiToPubmedMap(Set<Article> pubmedArticles) {
		Map<String, String> doi2pubmedMap = new HashMap<String, String>();
		System.out.println("Pubmed count: "+pubmedArticles.size());
		for(Article art: pubmedArticles) {
			//System.out.println(art.toString());
			String pmdID = art.getIdentifiers(DataSource.PUBMED.getDatasource());
			pubmedMap.put(pmdID, art);
			String doi = art.getIdentifiers(DataSource.CROSSREF.getDatasource());
			if(doi != null && pmdID != null) {
				doi2pubmedMap.put(doi, pmdID);
			}
			
		}
		System.out.println("Pubmed Map count: "+pubmedMap.size());
		System.out.println("DOI to Pubmed count: "+doi2pubmedMap.size());
		return doi2pubmedMap;
	}
}
