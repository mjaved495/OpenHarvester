package edu.cornell.scholars.scopus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ScopusDataHarvester {

	private static final String apiKey = "aa7176962e8fe6a9d7b11d90075f3248";
	private static final String DOI_URL  =  "https://api.elsevier.com/content/abstract/doi/";
	//private static final String SCOPUS_URL  = "http://api.elsevier.com/content/abstract/scopus_id/";
	
	public static String qeuryScopusWithDOI(String id, String outputFolder){
		File folder = new File(outputFolder);
		if(!folder.exists()) {
			folder.mkdirs(); 
		}
		//https://api.elsevier.com/content/abstract/scopus_id/84861038998?apiKey=aa7176962e8fe6a9d7b11d90075f3248
		boolean done = true;
		String scopusFileName= null;
		try{
			String req = DOI_URL+id+"?apiKey="+apiKey;
			System.out.println("Scopus_query: "+req);
			URL url = new URL(req);
			HttpURLConnection connection =
					(HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "text/xml");

			InputStream xml = connection.getInputStream();

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(xml);
			String scopus_url = doc.getElementsByTagName("prism:url").item(0).getTextContent();
			String scopusId = scopus_url.substring(scopus_url.lastIndexOf("/")+1).trim();
			scopusFileName = scopusId+".xml";
			DOMSource source = new DOMSource(doc);
			FileWriter writer = new FileWriter(new File(folder, scopusFileName));
			StreamResult result = new StreamResult(writer);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(source, result);
		}catch(IOException | TransformerException | ParserConfigurationException | SAXException iox){
			//System.out.println(iox.getMessage());
			done = false;
		}
		System.out.println("......"+done);
		sleep();
		if(!done){
			return null;
		}else {
			return scopusFileName;
		}
		
	}
	
	private static void sleep(){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
