package org.swissbib.linked.oclc.entities;

import com.google.common.io.CharStreams;
import com.google.common.net.UrlEscapers;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Created by swissbib on 12/28/15.
 */
public class AlignPersonEntities {



    //Json: https://github.com/google/gson/blob/master/UserGuide.md
    //https://github.com/google/gson
    //http://mvnrepository.com/artifact/com.google.code.gson/gson/2.5


    //private static String oclcURL = "http://experiment.worldcat.org/entity/lookup/?q=%s&wskey=%s&size=100";
    private static String oclcURL = "http://experiment.worldcat.org/entity/lookup/?q=%s&wskey=%s";

    public static void main (String[] args) {

        String host = null;
        Integer port = null;
        String clusterName = null;
        String oclcKey = null;

        if (args.length == 4) {
            try {
                host = args[0];
                port = Integer.parseInt(args[1]);
                clusterName = args[2];
                oclcKey = args[3];
            } catch (NumberFormatException e) {
                System.err.println("Argument" + args[0] + " must be an integer.");
                System.exit(1);
            }
        }else {
            System.out.println(String.format("Three args are necessary: HOST: %s, PORT: %s, CLUSTERNAME: %s,  KEY: %s","?", "?", "?", "?")  );
            System.exit(0);
        }


        /*
        ScanPersonES1 personES1 = new ScanPersonES1();
        personES1.init(host, port, clusterName);

        personES1.openScan();

        personES1.disconnectFromCluster();

        */


        ScanPersonES2 personES2 = new ScanPersonES2();
        personES2.init(host,port,clusterName);
        personES2.disconnectFromCluster();

        System.out.println("alles ok");

    }

    public static void startOCLCSearch (String query, String key) {

        /*
        startOCLCSearch("Mario Cuenca Sandoval",key);
        startOCLCSearch("Cuenca Sandoval, Mario", key);
        startOCLCSearch("martin walser", key);

        /*



        String urlToUse = String.format(oclcURL,query,key);
        HttpURLConnection uc = null;
        try {

            urlToUse = UrlEscapers.urlFragmentEscaper().escape(urlToUse);
            URL u = new URL(urlToUse);
            uc = (HttpURLConnection)u.openConnection();

            uc.setRequestMethod("GET");
            //uc.setRequestProperty("ACCEPT", "application/ld+json");
            uc.setRequestProperty("ACCEPT", "application/json");
            uc.connect();
            InputStream contentStream = (InputStream) uc.getContent();

            //String oclcResponse = CharStreams.toString( new InputStreamReader( contentStream, "UTF-8" ) );
            String oclcResponse = CharStreams.toString( new InputStreamReader( contentStream ) );

            System.out.println(oclcResponse);



            contentStream.close();
            uc.disconnect();


        } catch (IOException ioException) {
            ioException.printStackTrace();
        }




    }



    private static void connectWithES_2_1 () {

        /*
        TransportClient client = null;
        try {
            Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", "linked-swissbib-2")
                    .put("client.transport.sniff", true).build();
            client = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("sb-s9"), 9300));



            SearchResponse response = client.prepareSearch("testsb_151222")
                    .setTypes("person")
                    .execute()
                    .actionGet();

            System.out.println( response.getHits().getTotalHits());


        } catch (UnknownHostException unknownHost) {
            unknownHost.printStackTrace();
        } finally {
            if (null != client) {
                client.close();
            }
        }

        */

    }

}
