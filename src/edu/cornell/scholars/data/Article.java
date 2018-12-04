package edu.cornell.scholars.data;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Article implements Comparable<Article>{

	private String doi;
	private String title;
	private Set<CoAuthor> authors;
	private Set<Funder> funders;
	private String pubYear;
	private String publisher;
	private String publisher_location;
	private Long citationCount;
	private String type;
	private Set<String> isbn;
	private Set<ISSN> issn;
	private Set<String> subject;
	private String volume;

	private String pagination;
	private Long published_print;
	private Long published_online;
	private double rank;
	private String source;
	private Set<Identifier> ids;
	private String scopusId;
	private String language;
	private String publicationVenue;

	public Article() {  
		
	}
	
	public Article(Article copy) {
		super();
		this.doi = copy.doi;
		this.title = copy.title;
		//this.authors = copy.authors; Getting them from Copy instances
		this.funders = copy.funders;
		this.pubYear = copy.pubYear;
		this.publisher = copy.publisher;
		this.publisher_location = copy.publisher_location;
		this.citationCount = copy.citationCount;
		this.type = copy.type;
		this.isbn = copy.isbn;
		this.subject = copy.subject;
		this.volume = copy.volume;
		this.pagination = copy.pagination;
		this.published_print = copy.published_print;
		this.published_online = copy.published_online;
		this.rank = copy.rank;
		this.source = copy.source;
		this.ids = copy.ids;
		this.scopusId = copy.scopusId;
		this.language = copy.language;
		this.publicationVenue = copy.publicationVenue;
		this.issn = copy.issn;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}
	
	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public String getPagination() {
		return pagination;
	}

	public void setPagination(String pagination) {
		this.pagination = pagination;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public Set<CoAuthor> getAuthors() {
		return authors;
	}

	public void setAuthors(Set<CoAuthor> authors) {
		this.authors = authors;
	}
	
	public void addAuthor(CoAuthor author) {
		if(this.authors == null){
			this.authors = new HashSet<CoAuthor>();
		}
		this.authors.add(author);
	}

	public Set<Funder> getFunders() {
		return funders;
	}

	public void setFunders(Set<Funder> funders) {
		this.funders = funders;
	}

	public void addFunder(Funder funder) {
		if(this.funders == null){
			this.funders = new HashSet<Funder>();
		}
		this.funders.add(funder);
	}
	
	public String getPubYear() {
		return pubYear;
	}

	public void setPubYear(String pubYear) {
		this.pubYear = pubYear;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPublisher_location() {
		return publisher_location;
	}

	public void setPublisher_location(String publisher_location) {
		this.publisher_location = publisher_location;
	}

	public Long getCitationCount() {
		return citationCount;
	}

	public void setCitationCount(Long citationCount) {
		this.citationCount = citationCount;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Set<String> getIsbn() {
		return isbn;
	}

	public void setIsbn(Set<String> isbn) {
		this.isbn = isbn;
	}

	public void addIsbn(String isbn) {
		if(this.isbn == null){
			this.isbn = new HashSet<String>();
		}
		this.isbn.add(isbn);
	}
	
	public void addSubject(String sub) {
		if(this.subject == null){
			this.subject = new HashSet<String>();
		}
		this.subject.add(sub);
	}
	
	public Set<String> getSubject() {
		return subject;
	}

	public void setSubject(Set<String> subject) {
		this.subject = subject;
	}
	
	public double getRank() {
		return rank;
	}

	public void setRank(double rank) {
		this.rank = rank;
	}

	public Long getPublished_online() {
		return published_online;
	}

	public void setPublished_online(Long published_online) {
		this.published_online = published_online;
	}

	public Long getPublished_print() {
		return published_print;
	}

	public void setPublished_print(Long published_print) {
		this.published_print = published_print;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public Set<Identifier> getIdentifiers() {
		return ids;
	}

	public void setIdentifiers(Set<Identifier> ids) {
		this.ids = ids;
	}

	public void addIdentifier(Identifier id) {
		if(this.ids == null){
			this.ids = new HashSet<Identifier>();
		}
		this.ids.add(id);
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	@Override
	public int compareTo(Article o) {
		if(rank == o.getRank()){
			return 0;
		}else if(rank < o.getRank()){
			return 1;
		}else{
			return -1;
		}
	}

	public String printArticle() {
		String authorsList = getAuthorsList();
		String d  = doi != null? doi:"Null";
		String citation = getCitation();
		citation = citation.replaceAll("\\..", "\\.");
		return ("\""+d+"\",\""+citation+"\",\""+title+"\",\""+authorsList+"\",\""+ publicationVenue+"\",\""+ pubYear+"\",\""+source+"\"");
	}
	
	private String getCitation() {
		String citation="";
		String authorList ="";
		String [] a = new String[authors.size()];
		for(CoAuthor author: authors) {
			String fName = author.getFamilyName()!= null?author.getFamilyName():"";
			String gName = author.getGivenName()!= null?author.getGivenName():"";
			try {
				fName = URLDecoder.decode(fName, "UTF-8");
				gName = URLDecoder.decode(gName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			String authorName = "";
			if(gName.length() == 1 && !gName.endsWith(".")) {
				authorName = fName+", "+gName+".";
			}else {
				authorName = fName+", "+gName;
			}
			a[author.getRank()-1] = authorName;
		}
		for(int i = 0; i < a.length; i++) {
			authorList = authorList.concat(a[i]);
			if(i+1 != a.length) {
				authorList = authorList.concat(", ");
			}else {
				authorList = authorList.concat(" ");
			}
		}
		authorList = authorList.substring(0, authorList.length()-1);
		String year = pubYear != null ?pubYear:" ";
		String venue = publicationVenue != null ? publicationVenue: "";
		//citation = authorList+" \""+ title +"\". "+venue+" "+ year;
		citation = authorList+". ("+year+"). "+ title +". "+venue+".";
		return citation;
	}


	private String getAuthorsList() {
		String authorList ="[";
		for(CoAuthor author: authors) {
			String fName = author.getFamilyName()!= null?author.getFamilyName():"";
			String gName = author.getGivenName()!= null?author.getGivenName():"";
			authorList = authorList.concat("["+fName+", "+gName+"]");
		}
		authorList = authorList.concat("]");
		return authorList;
	}

	public String getScopusId() {
		return scopusId;
	}

	public void setScopusId(String scopusId) {
		this.scopusId = scopusId;
	}

	public String getIdentifiers(String datasource) {
		//if (ids == null) return null;
		for(Identifier id: ids) {
			if(id.getType().toUpperCase().equals(datasource.toUpperCase())){
				return id.getId();
			}
		}
		return null;
	}

	public String getPublicationVenue() {
		return publicationVenue;
	}

	public void setPublicationVenue(String publicationVenue) {
		this.publicationVenue = publicationVenue;
	}
	
	public Set<ISSN> getIssn() {
		return issn;
	}

	public void addIssn(ISSN newIssn) {
		if(this.issn == null) {
			this.issn = new HashSet<ISSN>();
		}
		this.issn.add(newIssn);
	}
	
	@Override
	public String toString() {
		return "Article [doi=" + doi + ", title=" + title + ", authors=" + authors + ", funders=" + funders
				+ ", pubYear=" + pubYear + ", publisher=" + publisher + ", publisher_location=" + publisher_location
				+ ", citationCount=" + citationCount + ", type=" + type + ", isbn=" + isbn + ", issn=" + issn
				+ ", subject=" + subject + ", volume=" + volume + ", pagination=" + pagination + ", published_print="
				+ published_print + ", published_online=" + published_online + ", rank=" + rank + ", source=" + source
				+ ", ids=" + ids + ", scopusId=" + scopusId + ", language=" + language + ", publicationVenue="
				+ publicationVenue + "]";
	}

	public String tooltipString() {
		return "doi=" + doi + ",\n title=" + title 
				+ ",\n authors="+ getOrderedAuthors(authors)
				+ ",\n venue=" + publicationVenue
				+ ",\n pubYear=" + pubYear
				+ ",\n type=" + type
				+ ",\n issn=" + issn
				+ ",\n published_print=" + published_print 
				+ ",\n published_online=" + published_online 
				+ ",\n rank=" + rank 
				+ ",\n source=" + source 
				+ ",\n ids=" + ids 
				+ ",\n scopusId=" + scopusId
				+ ",\n language=" + language
				+ ",\n subejct=" + subject;
	}
	

	private String getOrderedAuthors(Set<CoAuthor> authors2) {
		String names = "";
		CoAuthor[] authors = new CoAuthor[authors2.size()];
		for(CoAuthor co: authors2) {
			authors[co.getRank()-1] = co;
		}
		for(CoAuthor co : authors) {
			String name = "";
			if(co.getMiddleName() == null) {
				name = co.getGivenName() +" "+ co.getFamilyName();
			}else {
				name = co.getGivenName() +" "+ co.getMiddleName() + " " + co.getFamilyName();
			}
			if(co.getAffiliations() != null) {
				name = name+co.getAffiliations();
			}
			
			names = names + name + ", ";
		}
		
		return names.trim().substring(0, names.length()-2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authors == null) ? 0 : authors.hashCode());
		result = prime * result + ((citationCount == null) ? 0 : citationCount.hashCode());
		result = prime * result + ((doi == null) ? 0 : doi.hashCode());
		result = prime * result + ((funders == null) ? 0 : funders.hashCode());
		result = prime * result + ((ids == null) ? 0 : ids.hashCode());
		result = prime * result + ((isbn == null) ? 0 : isbn.hashCode());
		result = prime * result + ((issn == null) ? 0 : issn.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((pagination == null) ? 0 : pagination.hashCode());
		result = prime * result + ((pubYear == null) ? 0 : pubYear.hashCode());
		result = prime * result + ((publicationVenue == null) ? 0 : publicationVenue.hashCode());
		result = prime * result + ((published_online == null) ? 0 : published_online.hashCode());
		result = prime * result + ((published_print == null) ? 0 : published_print.hashCode());
		result = prime * result + ((publisher == null) ? 0 : publisher.hashCode());
		result = prime * result + ((publisher_location == null) ? 0 : publisher_location.hashCode());
		long temp;
		temp = Double.doubleToLongBits(rank);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((scopusId == null) ? 0 : scopusId.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((volume == null) ? 0 : volume.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Article other = (Article) obj;
		if (authors == null) {
			if (other.authors != null)
				return false;
		} else if (!authors.equals(other.authors))
			return false;
		if (citationCount == null) {
			if (other.citationCount != null)
				return false;
		} else if (!citationCount.equals(other.citationCount))
			return false;
		if (doi == null) {
			if (other.doi != null)
				return false;
		} else if (!doi.equals(other.doi))
			return false;
		if (funders == null) {
			if (other.funders != null)
				return false;
		} else if (!funders.equals(other.funders))
			return false;
		if (ids == null) {
			if (other.ids != null)
				return false;
		} else if (!ids.equals(other.ids))
			return false;
		if (isbn == null) {
			if (other.isbn != null)
				return false;
		} else if (!isbn.equals(other.isbn))
			return false;
		if (issn == null) {
			if (other.issn != null)
				return false;
		} else if (!issn.equals(other.issn))
			return false;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (pagination == null) {
			if (other.pagination != null)
				return false;
		} else if (!pagination.equals(other.pagination))
			return false;
		if (pubYear == null) {
			if (other.pubYear != null)
				return false;
		} else if (!pubYear.equals(other.pubYear))
			return false;
		if (publicationVenue == null) {
			if (other.publicationVenue != null)
				return false;
		} else if (!publicationVenue.equals(other.publicationVenue))
			return false;
		if (published_online == null) {
			if (other.published_online != null)
				return false;
		} else if (!published_online.equals(other.published_online))
			return false;
		if (published_print == null) {
			if (other.published_print != null)
				return false;
		} else if (!published_print.equals(other.published_print))
			return false;
		if (publisher == null) {
			if (other.publisher != null)
				return false;
		} else if (!publisher.equals(other.publisher))
			return false;
		if (publisher_location == null) {
			if (other.publisher_location != null)
				return false;
		} else if (!publisher_location.equals(other.publisher_location))
			return false;
		if (Double.doubleToLongBits(rank) != Double.doubleToLongBits(other.rank))
			return false;
		if (scopusId == null) {
			if (other.scopusId != null)
				return false;
		} else if (!scopusId.equals(other.scopusId))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (volume == null) {
			if (other.volume != null)
				return false;
		} else if (!volume.equals(other.volume))
			return false;
		return true;
	}
	
	
}
