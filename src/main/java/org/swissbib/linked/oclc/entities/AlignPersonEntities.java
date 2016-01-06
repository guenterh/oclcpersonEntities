package org.swissbib.linked.oclc.entities;


import java.io.*;
import java.util.Properties;

/**
 * Created by swissbib on 12/28/15.
 */
public class AlignPersonEntities {



    public static void main (String[] args) {

        Properties props = System.getProperties();
        if (!props.containsKey("configFile")) {
            System.err.println("missing property configFile");
            System.exit(1);
        }

        try {
            File configFile = new File((String) props.get("configFile"));
            FileInputStream fi = new FileInputStream(configFile);
            Properties configProps = new Properties();
            configProps.load(fi);

            if (!checkProperties(configProps)) {

                System.err.println("properties not correct");
                System.exit(1);
            }


            OCLCPersonEntityAPI oclcAPI = new OCLCPersonEntityAPI(configProps.getProperty("oclcKey"),
                    configProps.getProperty("oclcRequestURL"), configProps.getProperty("oclcIDURL"));

            ScanPersonES1 personES1 = new ScanPersonES1();
            personES1.setAPI(oclcAPI);

            personES1.init(configProps);

            MongoDBWrapper mDB = null;

            if (configProps.containsKey("mongoUser") && configProps.containsKey("mongoPassword")) {
                mDB = new MongoDBWrapper(configProps.getProperty("hostMongo"),
                        configProps.getProperty("portMongo"),
                        configProps.getProperty("mongoDataDB"),
                        configProps.getProperty("mongoDataCollection"),
                        configProps.getProperty("mongoAuthDB"),
                        configProps.getProperty("mongoUser"),
                        configProps.getProperty("mongoPassword"));
            } else {

                mDB = new MongoDBWrapper(configProps.getProperty("hostMongo"),
                        configProps.getProperty("portMongo"),
                        configProps.getProperty("mongoDataDB"),
                        configProps.getProperty("mongoDataCollection"),
                        configProps.getProperty("mongoAuthDB"));
            }

            personES1.setMongoWrapper(mDB);

            personES1.openScan();

            personES1.disconnectFromCluster();





        } catch (IOException ioException ) {

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


    private static boolean checkProperties(Properties props) {

        String[] manProps = new String[] {"hostES","portES","clusterName", "oclcKey", "hostMongo", "portMongo",
        "mongoDataDB", "mongoAuthDB", "mongoDataCollection", "oclcRequestURL", "oclcIDURL","numberBibResources" };

        for (String prop: manProps) {
            if (!props.containsKey(prop)) {
                System.err.println("property " + prop + " is missing");
                return false;
            }
        }

        return true;

    }

}
