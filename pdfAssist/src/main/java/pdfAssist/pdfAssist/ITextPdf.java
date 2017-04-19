package pdfAssist.pdfAssist;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.FilteredTextRenderListener;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.RegionTextRenderFilter;
import com.itextpdf.text.pdf.parser.RenderFilter;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.metadata.model.DocumentAuthor;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

public class ITextPdf
{
  private static Logger logger = LoggerFactory.getLogger(ITextPdf.class);
  private Rectangle rectangle;
  private PdfReader pdfReader;
  private String firstPageText;
  private int pageNumber = 1;
  private ArrayList<Locale> locale;
  private HashMap<Integer, String> lines;
  private HashMap<Integer, String> probableLocationLines;
  int[] searchLines = { -1, 0, 1 };
  List<DocumentAuthor> authorNames;
  
  public ITextPdf(String pdfFileName, List<DocumentAuthor> authorNames)
    throws IOException
  {
    this.lines = new HashMap<Integer, String>();
    this.authorNames = authorNames;
    this.locale = Constants.getCountryNames();
    this.pdfReader = new PdfReader(pdfFileName);
    logger.debug("pdfFileName: " + pdfFileName);
    this.rectangle = this.pdfReader.getPageSize(this.pageNumber);
    
    RenderFilter filter = new RegionTextRenderFilter(this.rectangle);
    
    TextExtractionStrategy strategy = new FilteredTextRenderListener(new LocationTextExtractionStrategy(), new RenderFilter[] { filter });
    this.firstPageText = PdfTextExtractor.getTextFromPage(this.pdfReader, this.pageNumber, strategy);
    int lineNo = 1;
    for (String s : this.firstPageText.split("\n"))
    {
      this.lines.put(Integer.valueOf(lineNo), s);
      lineNo++;
    }
  }
  
  public int getTotalPage()
  {
    if (this.pdfReader != null) {
      return this.pdfReader.getNumberOfPages();
    }
    return 0;
  }
  
  public void selectProbaleTextForLocation()
  {
    this.probableLocationLines = new HashMap<Integer, String>();
    Iterator localIterator1;
    if ((this.lines != null) && (!this.lines.isEmpty())) {
      for (localIterator1 = this.lines.keySet().iterator(); localIterator1.hasNext();)
      {
        int lineNo = ((Integer)localIterator1.next()).intValue();
        for (Locale country : this.locale) {
          if (((String)this.lines.get(Integer.valueOf(lineNo))).contains(country.getDisplayCountry()))
          {
            this.probableLocationLines.put(Integer.valueOf(lineNo), this.lines.get(Integer.valueOf(lineNo)));
            logger.debug("lineNo: " + lineNo + " text: " + (String)this.lines.get(Integer.valueOf(lineNo)));
          }
        }
      }
    }
  }
  
  public String extractLocationFromProbableLines()
  {
    String location = "";
    String city = "";
    boolean shouldStop = false;
    if ((this.probableLocationLines != null) && (!this.probableLocationLines.isEmpty()))
    {
      for (Iterator localIterator1 = this.probableLocationLines.keySet().iterator(); localIterator1.hasNext();)
      {
        int lineNo = ((Integer)localIterator1.next()).intValue();
        
        String[] split = ((String)this.probableLocationLines.get(Integer.valueOf(lineNo))).split("[,.]+");
        for (Locale country : this.locale) {
          for (int i = 0; i < split.length; i++) {
            if ((split[i].trim().equals(country.getDisplayCountry())) || 
              (split[i].trim().equals(country.getDisplayCountry() + ".")))
            {
              shouldStop = true;
              if (i - 1 >= 0) {
                city = split[(i - 1)];
              }
              if (city.length() > 1) {
                location = city + ", " + split[i];
              } else {
                location = split[i];
              }
              logger.debug("city: " + city + "\tlocation: " + location);
              return location;
            }
          }
        }
        if (shouldStop) {
          break;
        }
      }
      return location;
    }
    return location;
  }
  
  public HashMap<Integer, String> getProbableLocation()
  {
    return this.probableLocationLines;
  }
  
  public void verifyProbableLocation()
  {
    Iterator localIterator1;
    if ((this.probableLocationLines != null) && (this.lines != null) && (!this.probableLocationLines.isEmpty())) {
      for (localIterator1 = this.probableLocationLines.keySet().iterator(); localIterator1.hasNext();)
      {
        int lineNo = ((Integer)localIterator1.next()).intValue();
        
        ArrayList<Integer> shouldBeRemoved = new ArrayList<Integer>();
        for (int sline : this.searchLines)
        {
          int currentSLine = lineNo + sline;
          if (this.lines.containsKey(Integer.valueOf(currentSLine)))
          {
            boolean removed = false;
            for (DocumentAuthor author : this.authorNames)
            {
              if ((author.getName() != null) && 
                (((String)this.lines.get(Integer.valueOf(currentSLine))).contains(author.getName())))
              {
                removed = true;
                
                shouldBeRemoved.add(Integer.valueOf(lineNo));
                break;
              }
              for (String email : author.getEmails()) {
            	  if (((String)this.lines.get(Integer.valueOf(currentSLine))).contains(email))
            	  {
            		  removed = true;
                
            		  shouldBeRemoved.add(Integer.valueOf(lineNo));
            		  break;
            	  }
              }
            }
            if (removed) {
              break;
            }
          }
        }
      }
    }
  }
  
  public static void main(String[] args)
    throws AnalysisException, IOException, TimeoutException
  {}
}
