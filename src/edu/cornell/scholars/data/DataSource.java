package edu.cornell.scholars.data;

public enum DataSource {

	    CROSSREF ("CROSSREF"), 
	    DBLP ("DBLP"), 
	    PUBMED ("PUBMED");
//	    SCOPUS ("SCOPUS"),
//	    ALL ("ALL");
	
	private String datasource;
	
	
	DataSource(String datasource) {
        this.datasource = datasource;
    }

	public String getDatasource() {
		return datasource;
	}
	
}
