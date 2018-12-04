package edu.cornell.scholars.dataprocessors;

import java.util.List;
import java.util.Set;

import application.Main;
import edu.cornell.scholars.data.Article;
import javafx.application.Platform;
import javafx.scene.control.ListView;

public class RejectProcessor {

	private List<Article> resultantArticleList;
	private Set<Article> rejectedArticles;
	
	private ListView<String> rejectList;
	private ListView<String> pendingList;
	
	public RejectProcessor(ListView<String> rejectList, Set<Article> rejectedArticles, ListView<String> pendingList) {
		this.rejectedArticles = rejectedArticles;
		this.pendingList = pendingList;
		this.rejectList = rejectList;
	}
	
	public void rejectSelectedArticle(Article article, List<Article> resultantArticleList) {
		this.resultantArticleList = resultantArticleList;
		rejectList.getItems().add(rejectList.getItems().size(), article.getTitle());
		rejectedArticles.add(article);
		//Remove the item from pending list
		removeArticlefromPendingList(article);
	}
	
	private void removeArticlefromPendingList(Article article) {
		boolean isremoved = resultantArticleList.remove(article);
		if(isremoved) {
			int selectedIdx = pendingList.getSelectionModel().getSelectedIndex();
			if(Main.separateThread) {
				// Update on the JavaFx Application Thread       
				Platform.runLater(new Runnable(){
					@Override
					public void run(){
						pendingList.getItems().remove(selectedIdx);
					}
				});
				sleep();
			}else {
				pendingList.getItems().remove(selectedIdx);
			}

		}	
	}

	private static void sleep(){
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
