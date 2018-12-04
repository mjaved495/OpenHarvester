package edu.cornell.scholars.pubmed;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import edu.cornell.scholars.data.Article;

public class PubmedDataProcessor {

	public Set<Article> processPubmedClaimedPublications(String claimedfolder) {
		PubmedDataExtractor process = new PubmedDataExtractor();
		Set<Article> articles = 
				process.process(claimedfolder);
		return articles;
	}

	public List<Article> processPubmedPendingPublications(String pendingfolder) {
		PubmedDataExtractor process = new PubmedDataExtractor();
		Set<Article> articles = 
				process.process(pendingfolder);
		List<Article> resultantArticleList = new LinkedList<Article>();
		resultantArticleList.addAll(articles);
		return resultantArticleList;
	}

	public Set<Article> processPubmedRejectedPublications(String rejectedfolder) {
		PubmedDataExtractor process = new PubmedDataExtractor();
		Set<Article> articles = 
				process.process(rejectedfolder);
		return articles;
	}

}
