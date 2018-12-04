package harvester.crossref;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CrossRefDataHarvester {

	public static void main(String[] args) {
//		if(args.length < 2) {
//			System.out.println("Run Parameters not found....");
//			return;
//		}
		String nextCursor = null;
		String folderName = "Yrjö+Gröhn"; 
				//args[0]; 
		String searchString = "";
		try {
			searchString = URLEncoder.encode(folderName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String outputFolderName = 
				"/Users/mj495/Documents/DataHarvesterApp/CROSSREF/"+folderName;
				//args[1];
		int counter = 1;
		if(args.length > 2) {
			nextCursor = args[2];
			counter = Integer.parseInt(args[3]);
		}

		QueryCrossRefData qObj = new QueryCrossRefData();
		qObj.process(searchString, outputFolderName, nextCursor, counter, null, null, null);

	}

}
