package edu.cornell.scholars.crossref;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;

import edu.cornell.scholars.data.Article;
import javafx.scene.control.Label;

public class CrossrefDataProcessor {
	
	public CrossrefDataProcessor() { 
		
	}
 
	public Set<Article> processCrossRefClaimedPublications(String claimedFolderPath, Map<String, JSONObject> crossrefClaimedJSONMap, Label progressMonitor) {
		CrossRefDataExtracter process = new CrossRefDataExtracter();
		Set<Article> previouslyClaimedArticles = 
				process.processJSONFilesData(claimedFolderPath, crossrefClaimedJSONMap, progressMonitor);
		
		return previouslyClaimedArticles;
	}
	
	public List<Article> processCrossRefPendingPublications(String pendingFolderPath, Map<String, JSONObject> crossrefJSONMap, Label progressMonitor) {
		CrossRefDataExtracter process = new CrossRefDataExtracter();
		Set<Article> articles = 
				process.processJSONFilesData(pendingFolderPath, crossrefJSONMap, progressMonitor);
		List<Article> resultantArticleList = new LinkedList<Article>();
		resultantArticleList.addAll(articles);
		
		return resultantArticleList;
	}

	public Set<Article> processCrossRefRejectedPublications(String rejectedFolderPath, Map<String, JSONObject> crossrefRejectedJSONMap, Label progressMonitor) {
		CrossRefDataExtracter process = new CrossRefDataExtracter();
		Set<Article> previouslyRejectedArticles = 
				process.processJSONFilesData(rejectedFolderPath, crossrefRejectedJSONMap, progressMonitor);
		return previouslyRejectedArticles;
	}

	
}
