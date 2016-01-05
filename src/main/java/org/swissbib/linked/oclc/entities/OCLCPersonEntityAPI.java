package org.swissbib.linked.oclc.entities;

import com.google.common.io.CharStreams;
import com.google.common.net.UrlEscapers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;




/**
 * Created by swissbib on 1/4/16.
 */
public class OCLCPersonEntityAPI implements APISearch {



    private String apiQueryURL = "http://experiment.worldcat.org/entity/lookup/?q=%s&wskey=%s";
    private String apiIdURL = "http://experiment.worldcat.org/entity/lookup/?id=%s&wskey=%s";
    private String apiKey = null;

    public OCLCPersonEntityAPI(String key) {
        this.apiKey = key;
    }


    public OCLCPersonEntityAPI(String key, String queryURL, String idURL) {
        this.apiKey = key;
        this.apiQueryURL = queryURL;
        this.apiIdURL = idURL;
    }




    public String executeSearch(String query, OCLCQueryType type) {
        /*
        startOCLCSearch("Mario Cuenca Sandoval",key);
        startOCLCSearch("Cuenca Sandoval, Mario", key);
        startOCLCSearch("martin walser", key);

        */

        String oclcResponse = null;

        String urlToUse = type == OCLCQueryType.termLookUp ?
                String.format(this.apiQueryURL,query,this.apiKey):
                String.format(this.apiIdURL,query,this.apiKey);

        HttpURLConnection uc = null;
        try {

            urlToUse = UrlEscapers.urlFragmentEscaper().escape(urlToUse);
            URL u = new URL(urlToUse);
            uc = (HttpURLConnection)u.openConnection();

            uc.setRequestMethod("GET");
            //uc.setRequestProperty("ACCEPT", "application/ld+json");
            if (type == OCLCQueryType.termLookUp) {
                uc.setRequestProperty("ACCEPT", "application/json");
            } else {
                uc.setRequestProperty("ACCEPT","application/ld+json");
            }
            uc.connect();
            InputStream contentStream = (InputStream) uc.getContent();

            //String oclcResponse = CharStreams.toString( new InputStreamReader( contentStream, "UTF-8" ) );
            oclcResponse = CharStreams.toString( new InputStreamReader( contentStream ) );

            //System.out.println(oclcResponse);



            contentStream.close();
            uc.disconnect();


        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return oclcResponse;
    }
}
