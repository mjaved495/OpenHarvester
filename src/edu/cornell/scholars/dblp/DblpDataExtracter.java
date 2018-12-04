package edu.cornell.scholars.dblp;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import application.Controller;
import edu.cornell.scholars.data.Article;
import edu.cornell.scholars.data.CoAuthor;
import edu.cornell.scholars.data.DataSource;
import edu.cornell.scholars.data.Identifier;

public class DblpDataExtracter {

	private static final String XMLFILEFOLDER = "DBLP" ;


	public static void main(String args[]) {
//		DblpDataExtracter obj = new DblpDataExtracter();
//		Set<Article> art = obj.process("Corson-Rikert");
	}

	public Set<Article> process(String inputFolder, Map<String, Document> map) {
		Set<Article> articles_data = new HashSet<Article>();
		System.out.println(inputFolder);
		if(!new File(inputFolder).exists()){
//						File folder = new File(XMLFILEFOLDER+"/"+searchString);
//						OUTPUTFOLDER = folder.getAbsolutePath();
//						if (!folder.exists()){
//							System.out.println("Folder created: "+folder.mkdirs());
//						}
//						articles_data = quereyDBLP(searchString);
//			return articles_data;
		}else {
			articles_data = readDblpKeyFiles(inputFolder, map);
		}
		return articles_data;
	}

	private Set<Article> readDblpKeyFiles(String folder, Map<String, Document> map) {
		Set<Article> articles = new HashSet<Article>();
		File[] files = new File(folder).listFiles();
		for(File file: files){
			//System.out.println("processing "+file.getName());

			if(!file.getName().endsWith(".xml")) continue;
			String fileName = file.getName().substring(0, file.getName().indexOf(".xml"));
			if(isInteger(fileName)) {
				//System.out.println("processing..."+file.getName());
				Article article = readXMLFile(file, map);
				if(Controller.allPubMap.get(article.getTitle()) == null) {
					articles.add(article);
					Controller.allPubMap.put(article.getTitle(), article);
				}

				articles.add(article);
			}
		}
		return articles;
	}

	private Article readXMLFile(File xmlFilePath, Map<String, Document> map) {
		Article article = new Article();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFilePath);
			System.out.println(xmlFilePath.getAbsolutePath());
			
			Node node = doc.getElementsByTagName("dblp").item(0);
	        NodeList list = node.getChildNodes();
	        a : for(int x = 0; x < list.getLength(); x++) {
	        	Node childNode = list.item(x);
	        	NamedNodeMap nodeMap = childNode.getAttributes();
	        	if(nodeMap != null && nodeMap.getNamedItem("key") != null) {
	        		String key = childNode.getAttributes().getNamedItem("key").getNodeValue();
	        		System.out.println(key);
	        		article.addIdentifier(new Identifier(DataSource.DBLP.getDatasource(), key));
	        		break a;
	        	}
	        }
			
			
			String title = doc.getElementsByTagName("title").item(0).getTextContent();
			System.out.println(xmlFilePath +"-"+ title);
			NodeList authorNodes = doc.getElementsByTagName("author");
			if(authorNodes != null && authorNodes.getLength() > 0) {
				for(int index=0; index< authorNodes.getLength(); index++){
					Node authorNode = authorNodes.item(index);
					String name = authorNode.getTextContent();
					//System.out.println(name);
					name = name.replaceAll("[^a-zA-Z -\\.]", "").trim();
					//System.out.println(name);
					String gName, fName = "";
					if(name.lastIndexOf(" ") > 0) {
						gName = name.substring(0, name.lastIndexOf(" ")).trim();
						fName = name.substring(name.lastIndexOf(" ")).trim();
					}else {
						gName = name;
					}
					CoAuthor author = new CoAuthor();
					author.setGivenName(gName);
					author.setFamilyName(fName);
					author.setRank(index+1);
					article.addAuthor(author);
				}
			}
			String year =  doc.getElementsByTagName("year").item(0).getTextContent();
			article.setTitle(title);
			article.setPubYear(year);
			article.setSource("DBLP");
			article.setRank(0);
			map.put(title, doc);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		
		return article;
	}

	public static boolean isInteger(String s) {
		return isInteger(s,10);
	}

	public static boolean isInteger(String s, int radix) {
		if(s.isEmpty()) return false;
		for(int i = 0; i < s.length(); i++) {
			if(i == 0 && s.charAt(i) == '-') {
				if(s.length() == 1) return false;
				else continue;
			}
			if(Character.digit(s.charAt(i),radix) < 0) return false;
		}
		return true;
	}
}
