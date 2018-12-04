package edu.cornell.scholars.dataprocessors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import application.Controller;
import application.Main;
import edu.cornell.scholars.data.Affiliation;
import edu.cornell.scholars.data.Article;
import edu.cornell.scholars.data.CoAuthor;
import edu.cornell.scholars.data.FilterOperations;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

public class ClaimProcessor {

	private ListView<String> claimList;
	private Set<Article> claimedArticles;
	private List<Article> resultantArticleList;

	private ListView<String> authorList;
	private ListView<String> pendingList;
	private ListView<String> affiliationList;
	private ListView<String> gNameList;


	public ClaimProcessor(ListView<String> claimList, Set<Article> claimedArticles, 
			ListView<String> authorList, ListView<String> pendingList, ListView<String> affiliationList, 
			ListView<String> gNameList){

		this.claimList = claimList;
		this.claimedArticles = claimedArticles;
		this.authorList = authorList;
		this.pendingList = pendingList;
		this.affiliationList = affiliationList;
		this.gNameList = gNameList;
	}

	public void claimSelectedArticle(Article article, List<Article> resultantArticleList) {
		this.resultantArticleList = resultantArticleList;

		claimList.getItems().add(claimList.getItems().size(), article.getTitle());
		claimedArticles.add(article);

		//Remove the item from pending list
		removeArticlefromPendingList(article);

		//Update the profile
		Set<CoAuthor> newCoauthors = populateCoAuthorsInUserProfile(article);
		Set<Affiliation> newAff = populateAffiliationsInUserProfile(article);

		// Find new given name var
		Set<String> givenNames = getGivenNameForUser(article);
		// add them in profile
		if(givenNames.size() == 1){ // there is only one author with user's family name
			for(String gName: givenNames) {
				if(!Controller.profile.getGivenName().contains(gName)) {
					String gName2 = gName.replaceAll("[^a-zA-Z]", "").trim();  // not considering '-' within given name for now
					if(gName2.length() > 1 && !isAllUpperCase(gName2)) {
						Controller.profile.addGivenName(gName);
						gNameList.getItems().add(gNameList.getItems().size(), gName);
						// use them in ranking
						//FilterOperations.rankArticlesBasedOnUserFullName(resultantArticleList, Controller.profile);
					}
				}
			}
		}
		//Re-rank articles
		FilterOperations.rankArticlesBasedOnUserFullName(resultantArticleList, Controller.profile);
		//FilterOperations.rankArticlesBasedOnCoAuthors(resultantArticleList, Controller.profile, newCoauthors);
		FilterOperations.rankArticlesBasedOnCoAuthors(resultantArticleList, Controller.profile);
		//FilterOperations.rankArticlesBasedOnAffiliation(resultantArticleList, Controller.profile, newAff);
		FilterOperations.rankArticlesBasedOnAffiliation(resultantArticleList, Controller.profile);
	}

	private static boolean isAllUpperCase(String name) {
		String coGivenName = name.replaceAll("[^a-zA-Z]", "");
		for(char c : coGivenName.toCharArray()) {
			if(Character.isLetter(c) && Character.isLowerCase(c)) {
				return false;
			}
		}
		return true;
	}

	//	private void removeArticlefromPendingList(Article article) {
	//		// Update on the JavaFx Application Thread       
	//		boolean  isremoved = resultantArticleList.remove(article);
	//		if(isremoved) {
	//			int selectedIdx = pendingList.getSelectionModel().getSelectedIndex();
	//			pendingList.getItems().remove(selectedIdx);
	//		}
	//	}	

	private void removeArticlefromPendingList(Article article) {
		boolean isremoved = resultantArticleList.remove(article);
		if(isremoved) {
			int selectedIdx = pendingList.getSelectionModel().getSelectedIndex();
			//in cases where article is removed through automated processes and no selection is made.
			if(selectedIdx < 0) {
				selectedIdx = findArticleIndex(article, pendingList);
			}
			int selectedIndex = selectedIdx;
			//System.out.println(selectedIndex);
			
			if(Main.separateThread) {
				// Update on the JavaFx Application Thread       
				Platform.runLater(new Runnable(){
					@Override
					public void run(){
						//pendingList.getItems().remove(selectedIndex);
						pendingList.getItems().remove(article.getTitle());
					}
				});
				sleep();
			}else {
				pendingList.getItems().remove(selectedIndex);
			}

		}	
	}

	private int findArticleIndex(Article article, ListView<String> pendingList2) {
		ObservableList<String> items = pendingList2.getItems();
		return items.indexOf(article.getTitle());
	}

	public Set<CoAuthor> populateCoAuthorsInUserProfile(Article article) {
		Set<CoAuthor> newCoAuthors = new HashSet<CoAuthor>();

		for(CoAuthor author: article.getAuthors()){
			try {
				if(!author.getFamilyName().toUpperCase().equals(Controller.profile.getFamilyName().toUpperCase())){  //DO NOT ADD YOURSELF AS COAUTHOR
					if(!isAuthorNameVarianceExist(authorList, author.getFamilyName().trim()+", "+author.getGivenName().trim())){
						Controller.profile.addCoAuthor(author);
						newCoAuthors.add(author);
					}
				}
			}catch(NullPointerException exp) {
				System.err.println("Exception caught while adding a co-author..continuing");
				continue;
			}
		}
		//updateCoAuthorsListView();
		return newCoAuthors;
	}

	public Set<Affiliation> populateAffiliationsInUserProfile(Article article) {
		Set<Affiliation> affiliations = null;
		for(CoAuthor author: article.getAuthors()){
			try {
				if(author.getFamilyName().toUpperCase().equals(Controller.profile.getFamilyName().toUpperCase())){  
					//ADD ONLY IF FAMILYNAME EQUAL AND FIRSNT NAME "CONTAINED"

					Set<String> givenNames = Controller.profile.getGivenName();
					for(String givenName : givenNames) {
						if(author.getGivenName().toUpperCase().contains(givenName.toUpperCase())) {
							//TODO IN FUTURE ADD CHECK TO FIRST NAME OR INITIAL IN A BETTER WAY
							if(author.getAffiliations() != null) {
								affiliations = affiliationNameExist(affiliationList, author.getAffiliations());
								if(affiliations.size() > 0){
									for(Affiliation aff: affiliations) {
										if(!Controller.stopWords.contains(aff.getName().trim())) {  // we found some general affiliations, like "USA"
											Controller.profile.addAffiliation(aff);
										}
									}
								}
							}
						}
					}
				}
			}catch(NullPointerException exp) {
				System.err.println("Name issue: nullpointer exception caught..continuing");
				continue;
			}	
		}
		return affiliations;
	}

	private Set<String> getGivenNameForUser(Article article) {
		Set<String> givenNames = new HashSet<String>();
		try {
			for(CoAuthor author: article.getAuthors()) {
				if(author.getFamilyName().equals(Controller.profile.getFamilyName())) {
					givenNames.add(author.getGivenName().trim());
				}
			}
		} catch (NullPointerException exp) {
			System.err.println("Null pointer error for reading author names");
		}
		return givenNames;
	}

	private Set<Affiliation> affiliationNameExist(ListView<String> affList, Set<Affiliation> affiliations) {
		Set<Affiliation> newAff = new HashSet<Affiliation>();
		ObservableList<String> items = affList.getItems();
		for(Affiliation a: affiliations) {
			if(items.size() == 0) newAff.add(a);   // list is empty
			for(String affvar: items) {
				if(!a.getName().equalsIgnoreCase(affvar)) {
					newAff.add(a);
				}
			}
		}
		return newAff;
	}

	private boolean isAuthorNameVarianceExist(ListView<String> authorList2, String coauthor) {
		ObservableList<String> items = authorList2.getItems();
		for(String namevar: items) {
			if(namevar.toLowerCase().equals(coauthor.toLowerCase())){
				return true;
			}
		}
		return false;
	}

	private static void sleep(){
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
