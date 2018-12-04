package edu.cornell.scholars.citeseerX;

import java.io.*;
import java.util.*;



public class ImportFormatReader {

    public static String BIBTEX_FORMAT = "BibTeX";

  /** all import formats, in the default order of import formats */
//  private SortedSet<ImportFormat> formats = new TreeSet<ImportFormat>();
//
//  public ImportFormatReader() {
//    super();
//  }
//
//  public void resetImportFormats() {
//    formats.clear();
    
    // Add all our importers to the TreeMap. The map is used to build the import
    // menus, and .
//    formats.add(new CsaImporter());   
//    formats.add(new IsiImporter());
//    formats.add(new EndnoteImporter());
//    formats.add(new BibteXMLImporter());
//    formats.add(new BiblioscapeImporter());
//    formats.add(new SixpackImporter());
//    formats.add(new InspecImporter());
//    formats.add(new ScifinderImporter());
//    formats.add(new OvidImporter());
//    formats.add(new RisImporter());
//    formats.add(new JstorImporter());
//    formats.add(new SilverPlatterImporter());
//    formats.add(new BiomailImporter());
//    formats.add(new RepecNepImporter());  
//    formats.add(new PdfXmpImporter());
//    formats.add(new CopacImporter());
//    formats.add(new MsBibImporter());

    /**
     * Get import formats that are plug-ins
     */
//    JabRefPlugin jabrefPlugin = JabRefPlugin.getInstance(PluginCore.getManager());
//	if (jabrefPlugin != null){
//		for (ImportFormatExtension ext : jabrefPlugin.getImportFormatExtensions()){
//			ImportFormat importFormat = ext.getImportFormat();
//			if (importFormat != null){
//				formats.add(importFormat);
//			}
//		}
//	}
//	
//	/**
//	 * Get custom import formats
//	 */
//    for (CustomImportList.Importer importer : Globals.prefs.customImports){
//       try {
//        ImportFormat imFo = importer.getInstance();
//        formats.add(imFo);
//      } catch(Exception e) {
//        System.err.println("Could not instantiate " + importer.getName() + " importer, will ignore it. Please check if the class is still available.");
//        e.printStackTrace();
//      }      
//    }
//  }
  
  /**
   * Format for a given CLI-ID.
   * 
   * <p>Will return the first format according to the default-order of
   * format that matches the given ID.</p>
   * 
   * @param cliId  CLI-Id
   * @return  Import Format or <code>null</code> if none matches
   */
//  public ImportFormat getByCliId(String cliId) {
//    for (ImportFormat format : formats){
//      if (format.getCLIId().equals(cliId)) {
//        return format;
//      }
//    }
//    return null;
//  }
//  
//  public List<BibtexEntry> importFromStream(String format, InputStream in)
//    throws IOException {
//    ImportFormat importer = getByCliId(format);
//
//    if (importer == null)
//      throw new IllegalArgumentException("Unknown import format: " + format);
//
//    List<BibtexEntry> res = importer.importEntries(in);
//
//    // Remove all empty entries
//    if (res != null)
//      purgeEmptyEntries(res);
//
//    return res;
//  }

//  public List<BibtexEntry> importFromFile(String format, String filename)
//    throws IOException {
//    ImportFormat importer = getByCliId(format);
//
//    if (importer == null)
//      throw new IllegalArgumentException("Unknown import format: " + format);
//
//    return importFromFile(importer, filename);
//  }
//
//    public List<BibtexEntry> importFromFile(ImportFormat importer, String filename) throws IOException {
//        List<BibtexEntry> result = null;
//        InputStream stream = null;
//        try {
//            File file = new File(filename);
//            stream = new FileInputStream(file);
//
//            if (!importer.isRecognizedFormat(stream))
//                throw new IOException(Globals.lang("Wrong file format"));
//
//            stream = new FileInputStream(file);
//
//            result = importer.importEntries(stream);
//        } finally {
//
//            try {
//                if (stream != null)
//                    stream.close();
//            } catch (IOException ex) {
//                throw ex;
//            }
//        }
//
//        return result;
//    }
//
//  public static BibtexDatabase createDatabase(Collection<BibtexEntry> bibentries) {
//    purgeEmptyEntries(bibentries);
//
//    BibtexDatabase database = new BibtexDatabase();
//
//    for (Iterator<BibtexEntry> i = bibentries.iterator(); i.hasNext();) {
//      BibtexEntry entry = i.next();
//
//      try {
//        entry.setId(Util.createNeutralId());
//        database.insertEntry(entry);
//      } catch (KeyCollisionException ex) {
//        System.err.println("KeyCollisionException [ addBibEntries(...) ]");
//      }
//    }
//
//    return database;
//  }

  /**
   * All custom importers.
   * 
   * <p>Elements are in default order.</p>
   * 
   * @return all custom importers, elements are of type InputFormat
   */
//  public SortedSet<ImportFormat> getCustomImportFormats() {
//    SortedSet<ImportFormat> result = new TreeSet<ImportFormat>();
//    for (ImportFormat format : formats){
//      if (format.getIsCustomImporter()) {
//        result.add(format);  
//      }
//    }
//    return result;
//  }
//  
//  /**
//   * All built-in importers.
//   * 
//   * <p>Elements are in default order.</p>
//   * 
//   * @return all custom importers, elements are of type InputFormat
//   */
//  public SortedSet<ImportFormat> getBuiltInInputFormats() {
//		SortedSet<ImportFormat> result = new TreeSet<ImportFormat>();
//		for (ImportFormat format : formats) {
//			if (!format.getIsCustomImporter()) {
//				result.add(format);
//			}
//		}
//		return result;
//	}
  
  /**
	 * All importers.
	 * 
	 * <p>
	 * Elements are in default order.
	 * </p>
	 * 
	 * @return all custom importers, elements are of type InputFormat
	 */
//  public SortedSet<ImportFormat> getImportFormats() {
//    return this.formats;
//  }
//
//  /**
//   * Human readable list of all known import formats (name and CLI Id).
//   * 
//   * <p>List is in default-order.</p>
//   * 
//   * @return  human readable list of all known import formats
//   */
//  public String getImportFormatList() {
//    StringBuffer sb = new StringBuffer();
//
//    for (ImportFormat imFo : formats){
//      int pad = Math.max(0, 14 - imFo.getFormatName().length());
//      sb.append("  ");
//      sb.append(imFo.getFormatName());
//
//      for (int j = 0; j < pad; j++)
//        sb.append(" ");
//
//      sb.append(" : ");
//      sb.append(imFo.getCLIId());
//      sb.append("\n");
//    }
//
//    String res = sb.toString();
//
//    return res; //.substring(0, res.length()-1);
//  }


    /**
     * Expand initials, e.g. EH Wissler -> E. H. Wissler or Wissler, EH -> Wissler, E. H.
     * @param name
     * @return The name after expanding initials.
     */
//    public static String expandAuthorInitials(String name) {
//      String[] authors = name.split(" and ");
//      StringBuffer sb = new StringBuffer();
//      for (int i=0; i<authors.length; i++) {
//          if (authors[i].indexOf(", ") >= 0) {
//              String[] names = authors[i].split(", ");
//              if (names.length > 0) {
//                  sb.append(names[0]);
//                  if (names.length > 1)
//                    sb.append(", ");
//              }
//              for (int j=1; j<names.length; j++) {
//                  if (j == 1)
//                    sb.append(expandAll(names[j]));
//                  else
//                    sb.append(names[j]);
//                  if (j < names.length-1)
//                      sb.append(", ");
//              }
//
//          } else {
//              String[] names = authors[i].split(" ");
//              if (names.length > 0) {
//                  sb.append(expandAll(names[0]));
//              }
//              for (int j=1; j<names.length; j++) {
//                  sb.append(" ");
//                  sb.append(names[j]);
//              }
//          }
//          if (i < authors.length-1)
//              sb.append(" and ");
//      }
//
//      return sb.toString().trim();
//  }
  
//------------------------------------------------------------------------------


    public static String expandAll(String s) {
        //System.out.println("'"+s+"'");
        // Avoid arrayindexoutof.... :
        if (s.length() == 0)
          return s;
        // If only one character (uppercase letter), add a dot and return immediately:
        if ((s.length() == 1) && (Character.isLetter(s.charAt(0)) &&
                Character.isUpperCase(s.charAt(0))))
          return s+".";
        StringBuffer sb = new StringBuffer();
        char c = s.charAt(0), d = 0;
        for (int i=1; i<s.length(); i++) {
            d = s.charAt(i);
            if (Character.isLetter(c) && Character.isUpperCase(c) &&
                    Character.isLetter(d) && Character.isUpperCase(d)) {
                sb.append(c);
                sb.append(". ");
            }
            else {
                sb.append(c);
            }
            c = d;
        }
        if (Character.isLetter(c) && Character.isUpperCase(c) &&
              Character.isLetter(d) && Character.isUpperCase(d)) {
            sb.append(c);
            sb.append(". ");
        }
        else {
            sb.append(c);
        }
        return sb.toString().trim();
    }


//  static File checkAndCreateFile(String filename) {
//    File f = new File(filename);
//
//    if (!f.exists() && !f.canRead() && !f.isFile()) {
//      System.err.println("Error " + filename
//        + " is not a valid file and|or is not readable.");
//      Globals.logger("Error " + filename + " is not a valid file and|or is not readable.");
//
//      return null;
//    } else
//
//      return f;
//  }
//
//  //==================================================
//  // Set a field, unless the string to set is empty.
//  //==================================================
//  public static void setIfNecessary(BibtexEntry be, String field, String content) {
//    if (!content.equals(""))
//      be.setField(field, content);
//  }



    public static Reader getReader(File f, String encoding)
      throws IOException {
      InputStreamReader reader;
      reader = new InputStreamReader(new FileInputStream(f), encoding);

      return reader;
    }

  public static Reader getReaderDefaultEncoding(InputStream in)
    throws IOException {
    InputStreamReader reader;
    reader = new InputStreamReader(in);
    return reader;
  }

  /**
   * Receives an ArrayList of BibtexEntry instances, iterates through them, and
   * removes all entries that have no fields set. This is useful for rooting out
   * an unsucessful import (wrong format) that returns a number of empty entries.
   */
//  public static void purgeEmptyEntries(Collection<BibtexEntry> entries) {
//    for (Iterator<BibtexEntry> i = entries.iterator(); i.hasNext();) {
//      BibtexEntry entry = i.next();
//
//      // If there are no fields, remove the entry:
//      if (entry.getAllFields().size() == 0)
//        i.remove();
//    }
//  }

  /**
	 * Tries to import a file by iterating through the available import filters,
	 * and keeping the import that seems most promising.
	 * 
	 * If all fails this method attempts to read this file as bibtex.
	 * 
	 * @throws IOException 
	 */
//	public Pair<String, ParserResult> importUnknownFormat(String filename) {
//
//		Pair<String, ParserResult> result = null;
//		
//		// Cycle through all importers:
//		int bestResult = 0;
//
//        for (ImportFormat imFo : getImportFormats()) {
//
//            try {
//
//                List<BibtexEntry> entries = importFromFile(imFo, filename);
//
//                if (entries != null)
//                    purgeEmptyEntries(entries);
//
//                int entryCount = ((entries != null) ? entries.size() : 0);
//
//                if (entryCount > bestResult) {
//                    bestResult = entryCount;
//
//                    result = new Pair<String, ParserResult>(imFo.getFormatName(),
//                    new ParserResult(entries));
//                }
//            } catch (IOException ex) {
//                // The import didn't succeed. Go on.
//            }
//        }
//		
//		if (result != null)
//			return result;
//
//      // Finally, if all else fails, see if it is a BibTeX file:
//      try {
//          ParserResult pr = OpenDatabaseAction.loadDatabase(new File(filename),
//                  Globals.prefs.get("defaultEncoding"));
//          if ((pr.getDatabase().getEntryCount() > 0)
//                  || (pr.getDatabase().getStringCount() > 0)) {
//              pr.setFile(new File(filename));
//
//              return new Pair<String, ParserResult>(BIBTEX_FORMAT, pr);
//          }
//      } catch (Throwable ex) {
//          return null;
//      }
//
//      return null;
//	}
}
