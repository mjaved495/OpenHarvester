
package edu.cornell.scholars.citeseerX;


import javax.swing.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CiteSeerXFetcher {

    protected static int MAX_PAGES_TO_LOAD = 100;
    final static String QUERY_MARKER = "-------QUERY-------";
    final static String URL_START = "http://citeseer.ist.psu.edu";
    final static String SEARCH_URL = URL_START+"/search?q="+QUERY_MARKER
            +"&submit=Search&sort=rlv&t=doc";
    final static Pattern CITE_LINK_PATTERN = Pattern.compile("<a class=\"remove doc_details\" href=\"(.*)\">");

    protected boolean stopFetching = false;

    public static void main (String args[]) {
    	CiteSeerXFetcher obj = new CiteSeerXFetcher();
    	obj.processQuery("Javed");
    }
    
    
    public boolean processQuery(String query) {
        stopFetching = false;
        try {
            List<String> citations = getCitations(query);
            for (String citation : citations) {
                if (stopFetching)
                    break;
                String entry = getSingleCitation(citation);
                //BibtexEntry entry = BibsonomyScraper.getEntry(citation);

                //dialog.setProgress(++i, citations.size());
                if (entry != null) {
                    //inspector.addEntry(entry);
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getTitle() {
        return "CiteSeer";
    }

    public String getKeyName() {
        return "CiteSeer";
    }

    public String getHelpPage() {
        return "CiteSeerHelp.html";
    }

    public JPanel getOptionsPanel() {
        return null;
    }

    public void stopFetching() {
        stopFetching = true;
    }

        /**
     *
     * @param query
     *            The search term to query JStor for.
     * @return a list of IDs
     * @throws java.io.IOException
     */
    protected List<String> getCitations(String query) throws IOException {
        String urlQuery;
        ArrayList<String> ids = new ArrayList<String>();
        try {
            urlQuery =  SEARCH_URL.replace(QUERY_MARKER, URLEncoder.encode(query, "UTF-8"));
            int count = 1;
            String nextPage = null;
            while (((nextPage = getCitationsFromUrl(urlQuery, ids)) != null)
                    && (count < MAX_PAGES_TO_LOAD)) {
                urlQuery = nextPage;
                count++;
                if (stopFetching)
                    break;
            }
            System.out.println(count);
            return ids;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getCitationsFromUrl(String urlQuery, List<String> ids) throws IOException {
        URL url = new URL(urlQuery);
        URLDownload ud = new URLDownload(url);
        ud.download();

        String cont = ud.getStringContent();
        //System.out.println(cont);
        Matcher m = CITE_LINK_PATTERN.matcher(cont);
        while (m.find()) {
            ids.add(URL_START+m.group(1));
        }

        return null;
    }

    final static String basePattern = "<meta name=\""+QUERY_MARKER+"\" content=\"(.*)\" />";
    final static Pattern titlePattern = Pattern.compile(basePattern.replace(QUERY_MARKER, "citation_title"));
    final static Pattern authorPattern = Pattern.compile(basePattern.replace(QUERY_MARKER, "citation_authors"));
    final static Pattern yearPattern = Pattern.compile(basePattern.replace(QUERY_MARKER, "citation_year"));
    final static Pattern abstractPattern = Pattern.compile("<h3>Abstract</h3>\\s*<p>(.*)</p>");

    protected String getSingleCitation(String urlString) throws IOException {

        URL url = new URL(urlString);
        URLDownload ud = new URLDownload(url);
        ud.setEncoding("UTF8");
        ud.download();

        String cont = ud.getStringContent();
        System.out.println(cont);
        return cont;
        // Find title, and create entry if we do. Otherwise assume we didn't get an entry:
//        Matcher m = titlePattern.matcher(cont);
//        if (m.find()) {
//            BibtexEntry entry = new BibtexEntry(Util.createNeutralId());
//            entry.setField("title", m.group(1));
//
//            // Find authors:
//            m = authorPattern.matcher(cont);
//            if (m.find()) {
//                String authors = m.group(1);
//                entry.setField("author", NameListNormalizer.normalizeAuthorList(authors));
//            }
//
//            // Find year:
//            m = yearPattern.matcher(cont);
//            if (m.find())
//                entry.setField("year", m.group(1));
//
//            // Find abstract:
//            m = abstractPattern.matcher(cont);
//            if (m.find())
//                entry.setField("abstract", m.group(1));
//
//            return entry;
//        }
//        else
//            return null;

    }

}
