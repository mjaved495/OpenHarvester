package edu.cornell.scholars.dataprocessors;

import java.io.File;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.cornell.scholars.data.Article;
import edu.cornell.scholars.data.CoAuthor;
import edu.cornell.scholars.data.UserProfile;

public class ElementsXMLSchemeDataGenerator {

	
	
	public void saveClaimedArticlesInElementsXMLScheme(String dataSource, Set<Article> claimedArticles, UserProfile user) {
		try {
			
			Article article = claimedArticles.iterator().next();

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("import-record");
			rootElement.setAttribute("xmlns", "http://www.symplectic.co.uk/publications/api");
			rootElement.setAttribute("type-id", "5");
			
			doc.appendChild(rootElement);

			// native elements
			Element nativeE = doc.createElement("native");
			rootElement.appendChild(nativeE);
			
			Element titleField = doc.createElement("field");
			titleField.setAttribute("name", "title");
			Element titleTextE = doc.createElement("text");
			titleTextE.appendChild(doc.createTextNode(article.getTitle()));
			titleField.appendChild(titleTextE);
			nativeE.appendChild(titleField);
			
			
			Element authorsField = addAuthors(doc, user, article.getAuthors());
			nativeE.appendChild(authorsField);
			
			
			
			if(true) {
				Element emailE = doc.createElement("email-address");
			}
			titleTextE.appendChild(doc.createTextNode(article.getTitle()));
			
			
			
//			<field name="authors">
//	         <people>
//	            <person>
//	               <last-name>Zhang</last-name>
//	               <initials>K</initials>
//	               <email-address>kz33@cornell.edu</email-address>
//	            </person>
			

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("C:\\file.xml"));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);
			System.out.println("File saved!");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	private Element addAuthors(Document doc, UserProfile user, Set<CoAuthor> authors) {
		Element authorsField = doc.createElement("field");
		authorsField.setAttribute("name", "authors");
		Element peopleE = doc.createElement("people");
		
		Element personE = doc.createElement("person");
		Element lnameE = doc.createElement("last-name");
		Element initialsE = doc.createElement("initials");
		
		return authorsField;
	}
}
