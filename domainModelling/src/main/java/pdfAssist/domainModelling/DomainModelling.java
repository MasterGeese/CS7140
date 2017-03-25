package pdfAssist.domainModelling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.metadata.model.DocumentMetadata;

public class DomainModelling
{
    public static void main(String[] args) throws IOException {
    	File dir = new File(args[0]);
    	File[] filesInDir = dir.listFiles();
    	for (File f : filesInDir) {
    		extract(f);
    	}
    }
    private static void extract(File f) {
    	ContentExtractor extractor;
		try {
			extractor = new ContentExtractor();
			extractor.setPDF(new FileInputStream(f));
			DocumentMetadata d = extractor.getMetadata();
			String journal = d.getJournal();
			List<String> keywords = d.getKeywords();
			
			System.out.println("Original Filename: " + f.getName());
			System.out.println("Journal: " + journal);
			System.out.println("Keywords: ");
			for (String keyword : keywords) {
				System.out.print(keyword + " ");
			}
			System.out.println();
		} catch (AnalysisException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}   	
    }
}
