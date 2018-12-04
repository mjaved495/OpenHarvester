package edu.cornell.scholars.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilterOperations { 

	public static List<Article> filterOnPubStartYear(Long pubStartYear, List<Article> articles) {
		List<Article> filteredArticles = new ArrayList<Article>();
		for(Article a: articles){
			// we have no info on publication year
			System.out.println(a.getPubYear());
			if(a.getPubYear() == null){
				filteredArticles.add(a);  
			} else if(Long.parseLong(a.getPubYear()) >= pubStartYear){
				filteredArticles.add(a); // print year is found and under range
			}
		}
		return filteredArticles;
	}

	public static void rankArticlesBasedOnUserFullName(List<Article> articles1, UserProfile user) {
		Set<String> gNames = user.getGivenName();
		String fName = user.getFamilyName();

		for(Article art: articles1){
			
			art.setRank(0.0);
			
			Set<CoAuthor> authors = art.getAuthors();
			if(authors == null){
				//System.out.println(art.getDoi());
				continue;
			}

			if (art.getDoi() != null && art.getDoi().equalsIgnoreCase("10.1146/annurev.micro.58.030603.123749")){
				System.out.println(art.getDoi());
			}
			
			if (art.getDoi() != null && art.getDoi().equalsIgnoreCase("10.1111/tpj.12706")){
				System.out.println(art.getDoi());
			}
			
			// If family name has an exact match, +5
			// If given name has an exact match,
			// if family name has also an exact match +10
			// if family name does not match +0.5
			// If given name contains user's given name +0.5
			// If only initial is given (one character) and the initial matches, +0.5

			List<Double> counter = new ArrayList<Double>();
			for(CoAuthor author: authors){
				double d = 0; 

				if(author.getGivenName() != null){

					String coAuthorGivenName = author.getGivenName().replaceAll("[^a-zA-Z ]", "").trim();

					// Maria J Harrison is the example here
					// If it is all upper case, then it is initial only
					// if it is of length 2, then given-name middle-name initials (M J)
					// if it is of length 1, then only given-name initial (M)
					// else-if it has a space, then given-name and middle-name are given (Maria J)
					// else only given-name is given (Maria)	
					
					Set<Double> allWeights = new HashSet<Double>();
					Double givenNameWeight = 0.0;
					a: for(String gName: gNames) {
						if(isAllUpperCase(coAuthorGivenName)){   		// all uppercase
							String cogivenName = coAuthorGivenName.replaceAll("[^a-zA-Z]", ""); //remove all spaces
							if(cogivenName.length() == 2) { 			//Given + MiddleInitial  "MJ" 
								String initialsgName = getCapitalLetters(gName);
								if(initialsgName.equals(cogivenName)) {
									// rank MJ
									if(author.getFamilyName() != null && author.getFamilyName().toUpperCase().equals(fName.toUpperCase())){
										givenNameWeight=15d;
										//break a;
									}
								}
							}
							if(cogivenName.length() == 1) { //Initial Only "M"
								String initialsgName = gName.substring(0,1);
								if(initialsgName.equals(cogivenName)) {
									// rank M
									givenNameWeight = 0.5;
									//break a; // ???
								}
							}
						}else if(coAuthorGivenName.indexOf(" ") > 0) {   // has a space  (Given-Name + Middle-Initial)  "Maria J", "Maria Jane"
							String given = coAuthorGivenName.substring(0, coAuthorGivenName.indexOf(" ")).trim();	
							String initial = coAuthorGivenName.substring(coAuthorGivenName.indexOf(" ")).trim();
							String coName = given+initial;
							gName = gName.replaceAll("[^a-zA-Z]", "");
							if(coName.equals(gName)){
								// rank MariaJ
								if(author.getFamilyName() != null && author.getFamilyName().toUpperCase().equals(fName.toUpperCase())){
									givenNameWeight=20.0;
									//break a;
								}
							} else {                                 // new else block
								// rank "Maria J" with user "Maria"
								if(given.equals(gName)){
									givenNameWeight = 5.0;
								}
							}                                         // till here
						}else {   // Given-Name only "Maria"
							if(coAuthorGivenName.equals(gName)){
								// rank Maria
								if(author.getFamilyName() != null && author.getFamilyName().toUpperCase().equals(fName.toUpperCase())){
									givenNameWeight = 10.0;
								}else {
									givenNameWeight=0.5;
								}
							}
						}
						allWeights.add(givenNameWeight);
					}
					
					d = Collections.max(allWeights);
					
//					a: for(String gName: gNames) {
//						if(isAllUpperCase(coAuthorGivenName)){   		// all uppercase
//							String cogivenName = coAuthorGivenName.replaceAll("[^a-zA-Z]", ""); //remove all spaces
//							if(cogivenName.length() == 2) { 			//Given + MiddleInitial  "MJ" 
//								String initialsgName = getCapitalLetters(gName);
//								if(initialsgName.equals(cogivenName)) {
//									// rank MJ
//									if(author.getFamilyName() != null && author.getFamilyName().toUpperCase().equals(fName.toUpperCase())){
//										d+=15;
//										break a;
//									}else {
//										//d+=0.5;// ??
//										//break a;
//									}
//								}
//							}
//							if(cogivenName.length() == 1) { //Initial Only "M"
//								String initialsgName = gName.substring(0,1);
//								if(initialsgName.equals(cogivenName)) {
//									// rank M
//									d+=0.5;
//									break a; // ???
//								}
//							}
//						}else if(coAuthorGivenName.indexOf(" ") > 0) {   // has a space  (Given-Name + Middle-Initial)  "Maria J", "Maria Jane"
//							String given = coAuthorGivenName.substring(0, coAuthorGivenName.indexOf(" ")).trim();	
//							String initial = coAuthorGivenName.substring(coAuthorGivenName.indexOf(" ")).trim();
//							String coName = given+initial;
//							gName = gName.replaceAll("[^a-zA-Z]", "");
//							if(coName.equals(gName)){
//								// rank MariaJ
//								if(author.getFamilyName() != null && author.getFamilyName().toUpperCase().equals(fName.toUpperCase())){
//									d+=20;
//									break a;
//								}
////								else {
////									d+=0.5;
////									break a;
////								}
//							} else {                                 // new else block
//								// rank "Maria J" with user "Maria"
//								if(given.equals(gName)){
//									d += 5.0;
//								}
//							}                                         // till here
//						}else {   // Given-Name only "Maria"
//							if(coAuthorGivenName.equals(gName)){
//								// rank Maria
//								if(author.getFamilyName() != null && author.getFamilyName().toUpperCase().equals(fName.toUpperCase())){
//									d+=10;
//									break a;
//								}else {
//									d+=0.5;
//									break a;
//								}
//							}
//						}
//					}
					
				}// end of given-name processing

				if(author.getFamilyName() != null){
					if(author.getFamilyName().toUpperCase().equals(fName.toUpperCase())){
						d+=5;
					}
				}
				
				counter.add(d);
			}
			//System.out.println(art.getRank()+Collections.max(counter));
			art.setRank(art.getRank()+Collections.max(counter));
		}
	}

	private static String getCapitalLetters(String gName) {
		String uppercaseLetters = "";
		String coGivenName = gName.replaceAll("[^a-zA-Z]", "");
		for(char c : coGivenName.toCharArray()) {
			if(Character.isLetter(c) && Character.isUpperCase(c)) {
				uppercaseLetters += c;
			}
		}
		return uppercaseLetters;
	}

	private static boolean isAllUpperCase(String coAuthorGivenName) {
		String coGivenName = coAuthorGivenName.replaceAll("[^a-zA-Z]", "");
		for(char c : coGivenName.toCharArray()) {
			if(Character.isLetter(c) && Character.isLowerCase(c)) {
				return false;
			}
		}
		return true;
	}

	public static void rankArticlesBasedOnAffiliation(List<Article> resultantArticleList, UserProfile user) {
		
		a:	for(Article art: resultantArticleList){
			Set<CoAuthor> authors = art.getAuthors();
			if(authors == null) continue;
			for(CoAuthor author: authors){

				if (art.getDoi() != null && art.getDoi().equalsIgnoreCase("10.1146/annurev.micro.58.030603.123749")){
					System.out.println(art.getDoi());
				}
				
				if (art.getDoi() != null && art.getDoi().equalsIgnoreCase("10.1111/tpj.12706")){
					System.out.println(art.getDoi());
				}

				if(author.getFamilyName() == null || author.getGivenName() == null) continue;

				String authorGivenName = author.getGivenName().replaceAll("[^a-zA-Z]", "");

				if(user.getFamilyName().toUpperCase().equals(author.getFamilyName().toUpperCase())) {

					Set<String> givenNames = user.getGivenName();

					//for(String givenName: givenNames) {				
						//if(givenName.toUpperCase().contains(authorGivenName.toUpperCase())) {
							Set<Affiliation> authAffiliations = author.getAffiliations();
							if(authAffiliations == null) continue;

							for(Affiliation authAffiliation: authAffiliations){
								for(Affiliation userAffiliation: user.getAffiliations()){ // author affiliation CONTAINS user affiliation string
									if(authAffiliation.getName().toUpperCase().equals(userAffiliation.getName().toUpperCase())){
										art.setRank(art.getRank()+20); //10
										continue a;
									}else if(authAffiliation.getName().toUpperCase().contains(userAffiliation.getName().toUpperCase())){
										art.setRank(art.getRank()+10); //05
										continue a;
									}
								}
							}
						//}	
					//}	
				}
			}
		}
		
	}
	
	// if last name matches and affiliation matches , +1
	public static void rankArticlesBasedOnAffiliation(List<Article> articles1, UserProfile user, Set<Affiliation> newAffiliations) {
		if(newAffiliations == null) {
			newAffiliations = user.getAffiliations();
		}


		a:	for(Article art: articles1){
			Set<CoAuthor> authors = art.getAuthors();
			if(authors == null) continue;
			for(CoAuthor author: authors){

				if (art.getDoi() != null && art.getDoi().equalsIgnoreCase("10.1146/annurev.micro.58.030603.123749")){
					System.out.println(art.getDoi());
				}
				
				if (art.getDoi() != null && art.getDoi().equalsIgnoreCase("10.1111/tpj.12706")){
					System.out.println(art.getDoi());
				}

				if(author.getFamilyName() == null || author.getGivenName() == null) continue;

				String authorGivenName = author.getGivenName().replaceAll("[^a-zA-Z]", "");

				if(user.getFamilyName().toUpperCase().equals(author.getFamilyName().toUpperCase())) {

					Set<String> givenNames = user.getGivenName();

					//for(String givenName: givenNames) {				
						//if(givenName.toUpperCase().contains(authorGivenName.toUpperCase())) {
							Set<Affiliation> authAffiliations = author.getAffiliations();
							if(authAffiliations == null) continue;

							for(Affiliation authAffiliation: authAffiliations){
								for(Affiliation userAffiliation: newAffiliations){ // author affiliation CONTAINS user affiliation string
									if(authAffiliation.getName().toUpperCase().equals(userAffiliation.getName().toUpperCase())){
										art.setRank(art.getRank()+20); //10
										continue a;
									}else if(authAffiliation.getName().toUpperCase().contains(userAffiliation.getName().toUpperCase())){
										art.setRank(art.getRank()+10); //5
										continue a;
									}
								}
							}
						//}	
					//}	
				}
			}
		}
	}

	public static void rankArticlesBasedOnCoAuthors(List<Article> articleList, UserProfile profile, Set<CoAuthor> newCoAuthors) {
		for(CoAuthor userCoauthor: newCoAuthors){

			//don't rank based on current user
			if(ifCoAuthorisTheUser(userCoauthor, profile)) continue;

			for(Article art: articleList){
				Set<CoAuthor> authors = art.getAuthors();
				if(authors == null){
					//System.out.println(art.getDoi());
					continue;
				}

				//				if(art.getDoi().equals("10.1045/april2003-staples")){
				//					System.out.println(art.getDoi());
				//				}

				List<Double> counter = new ArrayList<Double>();
				for(CoAuthor author: authors){
					double d = 0.0;

					if(author.getFamilyName() != null){
						if(author.getFamilyName().toUpperCase().equals(userCoauthor.getFamilyName().toUpperCase())){
							if(author.getGivenName() != null){
								if(author.getGivenName().toUpperCase().equals(userCoauthor.getGivenName().toUpperCase())){
									d+=1;
								}
							}
						}
					}
					counter.add(d);
				}
				double r = art.getRank()+Collections.max(counter);
				//System.out.println(r);
				art.setRank(r);
			}
		}
	}

	/**
	 * Currently we are match last name and initial
	 * There may be the case where given name is given and get wrong match
	 * For example, "Bettina, Wagner" will match with "B. Wagner", "Bettina Wagner" and also with "Bruce Wagner".
	 * This limitation can be remove by having a deep given name match.
	 * 
	 * @param userCoauthor
	 * @param user
	 * @return
	 */
	private static boolean ifCoAuthorisTheUser(CoAuthor userCoauthor, UserProfile user) {

		if(userCoauthor.getFamilyName() != null){
			if(userCoauthor.getFamilyName().toUpperCase().equals(user.getFamilyName().toUpperCase())){
				return true;
				//				if(userCoauthor.getGivenName() != null){
				//					String gName = userCoauthor.getGivenName();
				//					//int len = gName.length();
				//					String initial = gName.substring(0, 1);
				//					if(user.getGivenName().toUpperCase().startsWith(initial)){
				//						return true;
				//					}
				//				}
			}
		}
		return false;
	}

	public static void rankArticlesBasedOnCoAuthors(List<Article> articleList, UserProfile userProfile) {
		//if Co-Author is available in user profile, increment  the rank of each article +1 for each co-author.

		for(CoAuthor userCoauthor: userProfile.getCoauthors()){

			for(Article art: articleList){

				//				if(art.getDoi().equalsIgnoreCase("10.1016/s0099-1333(98)90172-0")){
				//					System.out.println(art);
				//				}

				Set<CoAuthor> authors = art.getAuthors();
				if(authors == null){
					//System.out.println(art.getDoi());
					continue;
				}
				List<Double> counter = new ArrayList<Double>();
				for(CoAuthor author: authors){
					double d = 0.0;

					if(author.getFamilyName() != null){
						if(author.getFamilyName().toUpperCase().equals(userCoauthor.getFamilyName().toUpperCase())){
							if(author.getGivenName() != null){
								if(author.getGivenName().toUpperCase().equals(userCoauthor.getGivenName().toUpperCase())){
									d+=1;
								}
							}
						}
					}
					counter.add(d);
				}
				art.setRank(art.getRank()+Collections.max(counter));
			}
		}
	}

	public static void rankeArticlesBasedOnLangauge(List<Article> resultantArticleList) {


	}

}
