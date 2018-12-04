package edu.cornell.scholars.data;

import java.util.HashSet;
import java.util.Set;

public class CoAuthor {

	private String givenName;
	private String familyName;
	private String middleName;
	
	
	private Set<Affiliation> affiliations;
	private int rank;

	
	public CoAuthor(String givenName, String familyName, String middleName, Set<Affiliation> affiliations, int rank) {
		super();
		this.givenName = givenName;
		this.familyName = familyName;
		this.middleName = middleName;
		this.affiliations = affiliations;
		this.rank = rank;
	}

	public CoAuthor(String givenName, String familyName){
		super(); 
		this.givenName = givenName;
		this.familyName = familyName;
	}
	
	public CoAuthor(String givenName, String middleName, String familyName){
		super(); 
		this.givenName = givenName;
		this.middleName = middleName;
		this.familyName = familyName;
	}
	
	public CoAuthor(CoAuthor author){
		this.givenName = author.givenName;
		this.familyName = author.familyName;
		this.middleName = author.middleName;
		this.rank = author.rank;
		this.affiliations = author.affiliations;
	}
	
	public CoAuthor() {

	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getGivenName() {
		return givenName;
	}
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	public String getFamilyName() {
		return familyName;
	}
	public void setFamilyName(String familyName) {
		this.familyName = familyName; 
	}
	public Set<Affiliation> getAffiliations() {
		return affiliations;
	}
	public void setAffiliations(Set<Affiliation> affiliations) {
		this.affiliations = affiliations;
	}
	public void addAffiliation(Affiliation affiliation){
		if(this.affiliations == null){
			this.affiliations = new HashSet<Affiliation>();
		}
		this.affiliations.add(affiliation);
	}

	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((affiliations == null) ? 0 : affiliations.hashCode());
		result = prime * result + ((familyName == null) ? 0 : familyName.hashCode());
		result = prime * result + ((givenName == null) ? 0 : givenName.hashCode());
		result = prime * result + ((middleName == null) ? 0 : middleName.hashCode());
		result = prime * result + rank;
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
		CoAuthor other = (CoAuthor) obj;
		if (affiliations == null) {
			if (other.affiliations != null)
				return false;
		} else if (!affiliations.equals(other.affiliations))
			return false;
		if (familyName == null) {
			if (other.familyName != null)
				return false;
		} else if (!familyName.equals(other.familyName))
			return false;
		if (givenName == null) {
			if (other.givenName != null)
				return false;
		} else if (!givenName.equals(other.givenName))
			return false;
		if (middleName == null) {
			if (other.middleName != null)
				return false;
		} else if (!middleName.equals(other.middleName))
			return false;
		if (rank != other.rank)
			return false;
		return true;
	}

	@Override
	public String toString() {
		
		String name = "";
		if(middleName == null) {
			name = givenName +" "+ familyName;
		}else {
			name = givenName +" "+ middleName + " " + familyName;
		}
		if(affiliations != null) {
			return "\n "+ rank +". "+ name +" "+ affiliations ;
		}else {
			return "\n "+ rank +". "+ name;
		}
	}
	
	
	
	
}
