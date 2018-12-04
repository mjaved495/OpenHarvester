package edu.cornell.scholars.dataprocessors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import application.Controller;
import application.Main;
import edu.cornell.scholars.data.Article;
import edu.cornell.scholars.data.CoAuthor;
import edu.cornell.scholars.data.DataSource;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class SaveDataProcessor {

	private static String CLAIMED = "claimed";
	private String source;

	public void saveData(String personURI, CheckBox csvChB, CheckBox pdfChB, 
			CheckBox txtChB, CheckBox rdfChB, TextField journalfileTF) {
		source = Controller.selectedDatabase;

		if(source.equals(DataSource.CROSSREF.getDatasource())) { 
			saveClaimedIDs(DataSource.CROSSREF.getDatasource(), Controller.claimedArticles);
		}else if(source.equals(DataSource.DBLP.getDatasource())) {
			saveClaimedIDs(DataSource.DBLP.getDatasource(), Controller.claimedArticles);
		} else if(source.equals(DataSource.PUBMED.getDatasource())) {
			saveClaimedIDs(DataSource.PUBMED.getDatasource(), Controller.claimedArticles);
		}

		if(txtChB.isSelected()) {
			saveClaimedArticlesInTXT(source, Controller.claimedArticles);
		}
		if(csvChB.isSelected()) {
			saveClaimedArticlesInCSV(source, Controller.claimedArticles);
		}
		if(rdfChB.isSelected()) {
			saveClaimedArticlesInRDF(source, Controller.claimedArticles, personURI, journalfileTF.getText());
		}
		if(pdfChB.isSelected()) {
			saveClaimedArticlesInPDF(source, Controller.claimedArticles);
		}

	}

	private void saveClaimedIDs(String datasource, Set<Article> claimedArticles) {
		File folder = new File(Main.mainFolder+"/CLAIMED");
		if(!folder.exists()) {
			folder.mkdirs();
		}		
		File file = new File(folder, "ClaimedArticleIDs.csv");
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(file);
			for(Article a: claimedArticles) {
				printer.println(a.getIdentifiers(datasource));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			printer.close();
		}
	}

	public void saveClaimedArticlesInCSV(String source, Set<Article> claimedArticles) {
		File dir = new File(Main.mainFolder+"/"+CLAIMED);
		if(!dir.exists()) dir.mkdirs();

		File outputFile = new File(dir, "ClaimedArticles.csv");
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(outputFile);
			printer.println("\"DOI\",\"CITATION\",\"Title\",\"Authors\",\"Venue\",\"Year\",\"Source\"");
			for(Article article: claimedArticles) {
				printer.println(article.printArticle());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			printer.close();
		}
	}

	public void saveClaimedArticlesInRDF(String source, Set<Article> claimedArticles, String personURI, String journalMappingFilePath) {
		String outputFilePath = Main.mainFolder+"/"+CLAIMED+"/ClaimedArticles.nt";
		Controller.profile.setURI(personURI);
		RDFDataCreator rdfBuilder = new RDFDataCreator();
		rdfBuilder.saveDataInRDF(claimedArticles, Controller.profile, outputFilePath, journalMappingFilePath);
	}


	private void saveClaimedArticlesInPDF(String source, Set<Article> claimedArticles) {
		Document document = new Document(PageSize.A4, 20, 20, 20, 20);

		Font font_times = FontFactory.getFont(FontFactory.TIMES, 10, BaseColor.BLACK);
		//		Font font_times_italic = FontFactory.getFont(FontFactory.TIMES_ITALIC, 10, BaseColor.BLACK);

		Font font_title = FontFactory.getFont(FontFactory.COURIER_BOLD, 14, BaseColor.BLACK);

		com.itextpdf.text.List list = new com.itextpdf.text.List(com.itextpdf.text.List.ORDERED);
		String author = Controller.profile.getFirstGivenName().trim()+" "+Controller.profile.getFamilyName().trim()+"\n\n";
		try {
			String outputFilePath = Main.mainFolder+"/"+CLAIMED+"/ClaimedArticles.pdf";
			File file = new File(outputFilePath);
			file.createNewFile();

			PdfWriter.getInstance(document, new FileOutputStream(outputFilePath));
			document.open();
			Paragraph para1 = new Paragraph(20);
			para1.add(new Chunk(author, font_title));
			document.add(para1);

			Article[] sortedArticles = sortArticlesByPubYear(claimedArticles);
			for(Article article: sortedArticles) {
				String citation = getCitation(article);
				if(article.getDoi() != null) {
					citation = citation + " doi: " + "https://doi.org/"+article.getDoi();
				}
				Chunk chunk = new Chunk(citation, font_times);
				ListItem item = new ListItem(chunk);
				item.setAlignment(Element.ALIGN_JUSTIFIED);
				list.add(item);
			}
			document.add(list);

		} catch (FileNotFoundException | DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			document.close();
		}
	}

	private String getCitation(Article article) {
		String citation="";
		String authorList ="";
		String [] a = new String[article.getAuthors().size()];
		for(CoAuthor author: article.getAuthors()) {
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
		String year = article.getPubYear() != null ? " ("+article.getPubYear()+"). ": "";
		String venue = article.getPublicationVenue() != null ? article.getPublicationVenue(): "";
		//citation = authorList+" \""+ title +"\". "+venue+" "+ year;
		citation = authorList+"."+ year + article.getTitle() +". "+venue+".";

		return citation;
	}

	private Article[] sortArticlesByPubYear(Set<Article> claimedArticles) {
		Article[] array = claimedArticles.toArray(new Article[0]);
		for(int i = 0; i < array.length-1; i++) {
			b:	for(int j = 0; j < array.length-1; j++) {
				try {
					if(Integer.parseInt(array[j].getPubYear()) < Integer.parseInt(array[j+1].getPubYear())) {
						Article tmp = array[j];
						array[j] = array[j+1];
						array[j+1] = tmp;
					}
				} catch (Exception exp) {
					continue b;
				}
			}
		}

		return array;
	}


	private void saveClaimedArticlesInTXT(String source2, Set<Article> claimedArticles) {
		// TODO Auto-generated method stub

	}


	//	private void saveClaimedArticlesInJSON(String source, Set<Article> claimedArticles) {
	//		// "message" > "items" > []
	//		JSONArray list = new JSONArray();
	//		for(Article article: claimedArticles) {
	//			JSONObject obj = Controller.crossrefJSONMap.get(article.getTitle());
	//			if(obj == null) {
	//				obj = Controller.crossrefClaimedJSONMap.get(article.getTitle());
	//			}
	//			if(obj != null) {
	//				list.add(obj);
	//			}
	//		}
	//
	//		JSONObject items = new JSONObject();
	//		items.put("items", list);
	//		JSONObject message = new JSONObject();
	//		message.put("message", items);
	//
	//		File personalFolder = new File(Main.mainFolder, CLAIMED);
	//		if(!personalFolder.exists()) {
	//			personalFolder.mkdirs();
	//		}
	//		File outputFile = new File(personalFolder, "ClaimedArticles.json");
	//		try (FileWriter file = new FileWriter(outputFile)) {
	//			file.write(message.toJSONString());
	//			file.flush();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		//System.out.print(message);
	//	}
	//
	//	private void saveRejectedArticlesInJSON(String source, Set<Article> rejectedArticles) {
	//		// "message" > "items" > []
	//		JSONArray list = new JSONArray();
	//		for(Article article: rejectedArticles) {
	//			JSONObject obj = Controller.crossrefJSONMap.get(article.getTitle());
	//			if(obj == null) {
	//				obj = Controller.crossrefRejectedJSONMap.get(article.getTitle());
	//			}
	//			if(obj != null) {
	//				list.add(obj);
	//			}
	//		}
	//
	//		JSONObject items = new JSONObject();
	//		items.put("items", list);
	//		JSONObject message = new JSONObject();
	//		message.put("message", items);
	//
	//		File personalFolder = new File(Main.mainFolder, REJECTED);
	//		if(!personalFolder.exists()) {
	//			personalFolder.mkdirs();
	//		}
	//		File outputFile = new File(personalFolder, "RejectedArticles.json");
	//		try (FileWriter file = new FileWriter(outputFile)) {
	//			file.write(message.toJSONString());
	//			file.flush();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		//System.out.print(message);
	//	}
	//


}
