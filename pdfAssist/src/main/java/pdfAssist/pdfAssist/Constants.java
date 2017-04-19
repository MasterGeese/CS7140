package pdfAssist.pdfAssist;

import java.util.ArrayList;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants
{
  public static String testPdfName = "/Users/sarker/WorkSpaces/EclipseNeon/PDF-Renamer/src/main/resources/pdfs/testpdf/1603.02754.pdf";
  public static String testFileDir = "/Users/sarker/WorkSpaces/EclipseNeon/PDF-Renamer/src/main/resources/pdfs/testpdf/";
  public static String outputFileDir = "/Users/sarker/WorkSpaces/EclipseNeon/PDF-Renamer/result/";
  public static ArrayList<Locale> countryNames = new ArrayList<Locale>();
  private static Logger logger = LoggerFactory.getLogger(Constants.class);
  
  public static ArrayList<Locale> getCountryNames()
  {
    String[] cCodes = Locale.getISOCountries();
    
    String[] arrayOfString1 = cCodes;
    int i = arrayOfString1.length;
    for (int str1 = 0; str1 < i; str1++)
    {
      String cCode = arrayOfString1[str1];
      Locale country = new Locale("", cCode);
      countryNames.add(country);
    }
    String[] arr = { "USA", "UAE", "UK" };
    String[] arrayOfString2 = arr;
    int str1 = arrayOfString2.length;
    for (int cCode = 0; cCode < str1; cCode++)
    {
      String a = arrayOfString2[cCode];
      Locale country = new Locale("", a);
      countryNames.add(country);
    }
    return countryNames;
  }
  
  public static void main(String[] args)
  {
    ArrayList<Locale> countryNames = getCountryNames();
    for (Locale l : countryNames) {
      logger.debug("country: " + l.getCountry() + "\t name: " + l.getDisplayName());
    }
  }
}

