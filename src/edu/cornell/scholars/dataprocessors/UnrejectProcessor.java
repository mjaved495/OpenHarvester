package edu.cornell.scholars.dataprocessors;

import java.util.List;
import java.util.Set;

import application.Main;
import edu.cornell.scholars.data.Article;
import javafx.application.Platform;
import javafx.scene.control.ListView;

public class UnrejectProcessor {
	
	private Set<Article> rejectedArticles;
	
	private ListView<String> rejectList;
	private ListView<String> pendingList;
	
	public UnrejectProcessor(ListView<String> rejectList, Set<Article> rejectedArticles, ListView<String> pendingList) {
		this.rejectedArticles = rejectedArticles;
		this.pendingList = pendingList;
		this.rejectList = rejectList;
	}
	
	public void unrejectSelectedArticle(Article article, List<Article> resultantArticleList) {
		pendingList.getItems().add(pendingList.getItems().size(), article.getTitle());
		resultantArticleList.add(article);
		//remove the item from rejected list
		removeArticlefromRejectedList(article);
	}

	private void removeArticlefromRejectedList(Article article) {
		boolean isremoved = rejectedArticles.remove(article);
		if(isremoved) {
			int selectedIdx = rejectList.getSelectionModel().getSelectedIndex();
			if(Main.separateThread) {
				// Update on the JavaFx Application Thread       
				Platform.runLater(new Runnable(){
					@Override
					public void run(){
						rejectList.getItems().remove(selectedIdx);
					}
				});
				sleep();
			}else {
				rejectList.getItems().remove(selectedIdx);
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
