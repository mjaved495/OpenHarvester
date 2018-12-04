package edu.cornell.scholars.data;

import java.util.HashSet;
import java.util.Set;

public class UserProfile {
	
	private Set<String> givenName;
	private String firstGivenName;
	private String familyName;
	private String middleName;
	private Set<Affiliation> affiliations;
	private Set<CoAuthor> coauthors;
	private Set<Funder> funders;
	private Long pubStartYear;
	private Set<String> Ids;
	private Set<String> dois;
	private Set<Article> articles;
	private Set<String> altGivenName;
	private String uri;
	
	
	public UserProfile(Set<String> givenName, String familyName, String middleName, Set<Affiliation> affiliations, Set<CoAuthor> coauthors,
			Set<Funder> funders, Long pubStartYear, Set<String> ids, String uri) {
		super();
		this.givenName = givenName;
		this.familyName = familyName;
		this.middleName = middleName;
		this.affiliations = affiliations;
		this.coauthors = coauthors;
		this.funders = funders;
		this.pubStartYear = pubStartYear;
		Ids = ids;
		this.uri = uri;
	}
	
	public UserProfile() {

	}

	public Set<String> getGivenName() {
		return givenName;
	}
	public void addGivenName(String givenName) {
		if(this.givenName == null) {
			this.givenName = new HashSet<String>();
		}
		if(!this.givenName.contains(givenName)) {
			this.givenName.add(givenName);
		}
	}
	
	public String getFamilyName() {
		return familyName;
	}
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	public Set<Affiliation> getAffiliations() {
		if(this.affiliations == null){
			this.affiliations = new HashSet<Affiliation>();
		}
		return affiliations;
	}
	public void setAffiliations(Set<Affiliation> affiliations) {
		this.affiliations = affiliations;
	}
	public void addAffiliation(Affiliation affiliation) {
		if(this.affiliations == null){
			this.affiliations = new HashSet<Affiliation>();
		}
		this.affiliations.add(affiliation);
	}
	
	
	public Set<CoAuthor> getCoauthors() {
		if(this.coauthors == null){
			this.coauthors = new HashSet<CoAuthor>();
		}
		return coauthors;
	}
	public void setCoauthors(Set<CoAuthor> coauthors) {
		this.coauthors = coauthors;
	}
	public void addCoAuthor(CoAuthor author) {
		if(this.coauthors == null){
			this.coauthors = new HashSet<CoAuthor>();
		}
		this.coauthors.add(author);
	}
	
	public Set<Funder> getFunders() {
		return funders;
	}
	public void setFunders(Set<Funder> funders) {
		this.funders = funders;
	}
	
	public Long getPubStartYear() {
		return pubStartYear;
	}
	public void setPubStartYear(Long pubStartYear) {
		this.pubStartYear = pubStartYear;
	}
	public Set<String> getIds() {
		return Ids;
	}
	public void setIds(Set<String> ids) {
		Ids = ids;
	}
	public void addDOIs(String doi) {
		if(this.dois == null){
			this.dois = new HashSet<String>();
		}
		this.dois.add(doi);
	}
	public Set<String> getDois() {
		return dois;
	}

	public void setDois(Set<String> dois) {
		this.dois = dois;
	}

	public Set<Article> getArticles() {
		return articles;
	}

	public void setArticles(Set<Article> articles) {
		this.articles = articles;
	}
	
	public void addArticle(Article article) {
		if(this.articles == null){
			this.articles = new HashSet<Article>();
		}
		this.articles.add(article);
	}

	public String getFirstGivenName() {
		return firstGivenName;
	}
	
	public void setFirstGivenName(String name) {
		firstGivenName =  name;
	}

	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	
}
