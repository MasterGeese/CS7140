package pdfAssist.pdfAssist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;

import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.metadata.model.DocumentAuthor;
import pl.edu.icm.cermine.metadata.model.DocumentDate;
import pl.edu.icm.cermine.metadata.model.DocumentMetadata;

public class PDFAssist 
{
	
	/**
	 * 
	 * @param args
	 */
    public static void main( String[] args )
    {
    	Scanner input = new Scanner(System.in);
    	
    	System.out.println("Hello and welcome to PDFAssist, a tool for converting the names of academic papers in PDF form"
    			+ "based on their metadata.");
    	System.out.println();
    	System.out.println("First, please input the path to the directory containing all pdf files you wish to rename.");
    	File dir = new File(input.nextLine());
    	System.out.println("Next, please input your desired format for the resulting files to take. Enter 'help' for more details");
    	
    	String nextCommand = input.nextLine();
    	if (nextCommand == "help") {
    		displayHelp();
    		nextCommand = input.nextLine();
    	}
    	
    	File[] filesInDir = dir.listFiles();
    	
    	for (File f : filesInDir) {
    		DocumentData d = extract(f);	
    		String outputFormat = nextCommand;
    		String formattedFileName = format(outputFormat, d);
    		rename(formattedFileName, f); // If any file is not renamed correctly, this will be false.
    	}

    	
    }
    
    private static void displayHelp() {
    	System.out.println("In your format, replace each of your desired attributes as follows:");
    	System.out.println("Topic(s): $t");
    	System.out.println("Publication year: $y");
    	System.out.println("Author: $a");
    	System.out.println("Journal: $j");
    	System.out.println("Location: $l");
    	System.out.println("Type: $t");
    	System.out.println("Volume: $v");
    	System.out.println("Issue: $i");
    	System.out.println("Publisher Name: $p");
    	System.out.println("For example: the string,");
    	System.out.println("$t $l $v");
    	System.out.println("will result in a document being renamed,");
    	System.out.println("Scissors Shinjuku 432.pdf");
    	System.out.println("if the file has a Topic of Scissors, a Location of Shinjuku, and a Volume of 432.");
    }
    
    private static DocumentData extract(File f) {
    	DocumentData data = new DocumentData();
    	ContentExtractor extractor;
    	try {
    		extractor = new ContentExtractor();
    		extractor.setPDF(new FileInputStream(f));
    		DocumentMetadata d = extractor.getMetadata();
    		
    		
    		// topic, keywords
    		String keywordString = "";
    		List<String> keywords = d.getKeywords();
			for (String s : keywords) {
				keywordString = keywordString + s + " ";
			}
			data.setTopic(keywordString);
			
			// publishing year
			DocumentDate date = d.getDate("published");
			if (date != null) {
				data.setYear(date.getYear());
			} else {
				data.setYear("");
			}
			
			// authors
			String authorString = "";
			List<DocumentAuthor> authors = d.getAuthors();
			for (DocumentAuthor da : authors) {
				authorString = authorString + da.getName() + " ";
			}
			data.setAuthors(authorString);
			
			// journal
			if (d.getJournal() == null) {
				data.setJournal("");
			} else {
				data.setJournal(d.getJournal());
			}
			
			// pages
			if (d.getFirstPage() == null || d.getLastPage() == null) {
				data.setPages("");
			} else {
				data.setPages(d.getFirstPage() + " - " + d.getLastPage());
			}
			// location 
			/**
			 * The following code was borrowed from the Domain Analysis work of 
			 * Md Kamruzzaman Sarker.
			 */
		    ITextPdf iTextPdf = new ITextPdf(f.getPath(), d.getAuthors());
		    iTextPdf.selectProbaleTextForLocation();
		    
		    if (iTextPdf.extractLocationFromProbableLines() == null) {
		    	data.setLocation("");
		    } else {
		    	data.setLocation(iTextPdf.extractLocationFromProbableLines());
		    }
		    
			// type
			data.setType(extractType(f));
			
			// volume
			if (d.getVolume() == null) {
				data.setVolume("");
			} else {
				data.setVolume(d.getVolume());
			}
			
			// issue 
			if (d.getIssue() == null) {
				data.setIssue("");
			} else {
				data.setIssue(d.getIssue());
			}
			
			// publisher
			if (d.getPublisher() == null) {
				data.setPublisher("");
			} else {
				data.setPublisher(d.getPublisher());
			}
    	} catch (AnalysisException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return data;
    }

    private static String extractType(File f) {
    	try {
    		PDFParser parser = new PDFParser(new RandomAccessFile(f, "r"));
    		PDFTextStripper stripper = new PDFTextStripper();
    		parser.parse();
    		COSDocument cosDoc = parser.getDocument();
    		stripper = new PDFTextStripper();
    		PDDocument pdDoc = new PDDocument(cosDoc);
    		pdDoc.getNumberOfPages();
    		stripper.setStartPage(1);
    		stripper.setEndPage(4);
    		if (stripper.getText(pdDoc).toLowerCase().contains("bachelor thesis")) {
    			return "Bachelor Thesis";
    		} else if (stripper.getText(pdDoc).toLowerCase().contains("ieee")) {
    			return "Regular Paper";
    		} else if (stripper.getText(pdDoc).toLowerCase().contains("acm")) {
    			return "Regular Paper";
    		} else if (stripper.getText(pdDoc).toLowerCase().contains("publication")) {
    			return "Regular Paper";
    		} else if (stripper.getText(pdDoc).toLowerCase().contains("journal")) {
    			return "Regular Paper";
    		} else if (stripper.getText(pdDoc).toLowerCase().contains("phd thesis")) {
    			return "PhD Thesis/Dissertation";
    		} else if (stripper.getText(pdDoc).toLowerCase().contains("phd dissertation")) {
    			return "PhD Thesis/Dissertation";
    		} else if (stripper.getText(pdDoc).toLowerCase().contains("in partial fulfillment of")) {
    			return "PhD Thesis/Dissertation";
    		}
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return "Unknown";
    }
    
    public static void rename(String formattedFileName,  File f) {
    	String newFileName = f.getParent() + formattedFileName + ".pdf";
    	if (new File(newFileName).exists()) {
    		int i = 0;
    		while (new File(newFileName).exists()) {
    			newFileName = f.getParent() + formattedFileName + "(" + i + ").pdf";
    		}
    		System.out.println("Error: File " + f.getName() + " already exists, renaming to " + newFileName);
    	} 
    	
    	if (!f.renameTo(new File(newFileName))) {
    		System.out.println("Error: File could not be correctly renamed");
    	}
    	
    }
    
    public static String format(String format, DocumentData d) {
    	String formattedFileName = "";
    
    	final char topicVarName = 't';
    	final char pubYearVarName = 'y';
    	final char authorVarName = 'a';
    	final char journalVarName = 'j';
    	final char pagesVarName = 'p';
    	final char locationVarName = 'n';
    	final char typeVarName = 'e';
    	final char volumeVarName = 'v';
    	final char issueVarName = 'i';
    	final char publisherVarName = 'u';
    	
    	
    	// Delimiter is set to $ so that we know the succeeding character is one of the fields.
    
    	String delimiter = "\\$";
    	String[] tokens = format.split(delimiter);
    	for (String s : tokens) {
    		if (!s.equals("")) {
    			switch (s.charAt(0)) {
    				case (topicVarName): 
    					formattedFileName += d.getTopic();
    				break;
    				case (pubYearVarName):
    					formattedFileName += d.getYear();
    				break;
    				case (authorVarName):
    					formattedFileName += d.getAuthors();
    				break;
    				case (journalVarName):
    					formattedFileName += d.getJournal();
    				break;
    				case (pagesVarName):
    					formattedFileName += d.getPages();
    				break;
    				case (locationVarName):
    					formattedFileName += d.getLocation();
    				break;
    				case (typeVarName):
    					formattedFileName += d.getType();
    				break;
    				case (volumeVarName):
    					formattedFileName += d.getVolume();
    				break;
    				case (issueVarName):
    					formattedFileName += d.getIssue();
    				break;
    				case (publisherVarName):
    					formattedFileName += d.getPublisher();
    				break;
    				default:
    					formattedFileName += "$" + s.charAt(0);
    				break;
    			}
    			formattedFileName += s.substring(1, s.length());
    		}
    		
    	}
    	formattedFileName = formattedFileName.trim();
    	return formattedFileName;
    	
    	
    }
}
