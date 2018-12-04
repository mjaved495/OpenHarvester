package edu.cornell.scholars.dblp;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import edu.cornell.scholars.data.Article;

public class DblpDataProcessor { 

	public Set<Article> processDBLPClaimedPublications(String claimedfolder, Map<String, Document> dblpClaimedXMLMap){ 
		DblpDataExtracter process = new DblpDataExtracter();
		Set<Article> articles = 
				process.process(claimedfolder, dblpClaimedXMLMap);
		return articles;
	}
	
	public List<Article> processDBLPPendingPublications(String pendingfolder, Map<String, Document> dblpXMLMap){
		DblpDataExtracter process = new DblpDataExtracter();
		Set<Article> articles = 
				process.process(pendingfolder, dblpXMLMap);
		List<Article> resultantArticleList = new LinkedList<Article>();
		resultantArticleList.addAll(articles);
		
		return resultantArticleList;
	}
	
	public Set<Article> processDBLPRejectedPublications(String rejectedfolder, Map<String, Document> dblpRejectedXMLMap){
		DblpDataExtracter process = new DblpDataExtracter();
		Set<Article> articles = 
				process.process(rejectedfolder, dblpRejectedXMLMap);
		return articles;
	}
	
}
