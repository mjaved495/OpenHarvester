package edu.cornell.scholars.scopus;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.cornell.scholars.data.Affiliation;
import edu.cornell.scholars.data.Article;
import edu.cornell.scholars.data.CoAuthor;

public class ScopusFileReader {


	private static boolean sameArticle;

	public static Map<String, String> readInputFileAndGenerateMaps(String doi, String xmlFilePath) throws IOException, ParserConfigurationException, SAXException {

		//		if(xmlFilePath.endsWith("0029083097.xml")) {
		//			System.out.println(xmlFilePath);
		//		}

		sameArticle = true;
		Map<String, String> rank_affilMap = new HashMap<String, String>();
		try {		
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			File file = new File(xmlFilePath);

			if(!file.exists()) { // xml file does not exist
				return rank_affilMap;
			}
			Document doc = dBuilder.parse(xmlFilePath);
			//			NodeList entryList = doc.getElementsByTagName("coredata");
			//			for(int index=0; index< entryList.getLength(); index++){
			//				Node node = entryList.item(index);
			//				//System.out.println(node.getNodeName());
			//				Element eElement = (Element) node;
			//				String prism_doi = eElement.getElementsByTagName("prism:doi").item(0).getTextContent();
			//				if(prism_doi.equalsIgnoreCase(doi)) {
			//					sameArticle = true;
			//				}
			//			}// end of reading entries.
			if(sameArticle){
				Map<String, String> affIdMap = new HashMap<String, String>();

				NodeList affiliationList = doc.getElementsByTagName("affiliation");
				for(int index1=0; index1< affiliationList.getLength(); index1++){
					Node affiliationNode = affiliationList.item(index1);
					//System.out.println(node.getNodeName());
					Element affiliationElement = (Element) affiliationNode;
					String affilid = affiliationElement.getAttribute("id");
					NodeList affiliationNodeList = affiliationElement.getElementsByTagName("affilname");
					for(int index2=0; index2< affiliationNodeList.getLength(); index2++){
						Node affiliationNameNode = affiliationNodeList.item(index2);
						String affilname = affiliationNameNode.getTextContent();
						affIdMap.put(affilid, affilname);
						//System.out.println(affilname);
					}

				}// end of reading affiliation entries.

				Node authorsNode = doc.getElementsByTagName("authors").item(0);
				Element authorsElement = (Element) authorsNode;
				NodeList authorList = authorsElement.getElementsByTagName("author");
				for(int index=0; index< authorList.getLength(); index++){
					Node authorNode = authorList.item(index);
					//System.out.println(node.getNodeName());
					Element authorElement = (Element) authorNode;
					Element affilElement = (Element) authorElement.getElementsByTagName("affiliation").item(0);
					String id = affilElement.getAttribute("id");
					String seq = authorElement.getAttribute("seq");
					rank_affilMap.put(seq, affIdMap.get(id));
				}// end of reading affiliation entries.
			}
		}catch(NullPointerException exp) {
			System.out.println("NullPointerException Caught");
			System.out.println("Error:"+xmlFilePath);
		}

		return rank_affilMap;
	}

	//	String authorGivenName = (String) author.get("given");
	//	String authorFamilyName = (String) author.get("family");
	//	coauthor.setFamilyName(authorFamilyName);
	//	coauthor.setGivenName(authorGivenName);
	//	coauthor.setRank(++order);

	public static void populateAuthorsData(Article article, String xmlFilePath) {
		try {		
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			File file = new File(xmlFilePath);

			if(!file.exists()) { // xml file does not exist
				return;
			}
			Document doc = dBuilder.parse(xmlFilePath);
				Node authorsNode = doc.getElementsByTagName("authors").item(0);
				Element authorsElement = (Element) authorsNode;
				NodeList authorList = authorsElement.getElementsByTagName("author");
				for(int index=0; index< authorList.getLength(); index++){
					Node authorNode = authorList.item(index);
					//System.out.println(node.getNodeName());
					Element authorElement = (Element) authorNode;
					String seq = authorElement.getAttribute("seq");
					String surname = authorElement.getElementsByTagName("ce:surname").item(0).getTextContent();
					String givenname = authorElement.getElementsByTagName("ce:given-name").item(0).getTextContent();
					CoAuthor author = new CoAuthor();
					author.setRank(Integer.parseInt(seq));
					author.setFamilyName(surname);
					author.setGivenName(givenname);
					article.addAuthor(author);
				}// end of reading affiliation entries.
		}catch(NullPointerException exp) {
			System.out.println("NullPointerException Caught while reading scopus file");
			System.out.println("Error:"+xmlFilePath);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}


