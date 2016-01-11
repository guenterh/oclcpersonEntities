package org.swissbib.linked.oclc.entities;

import java.util.Properties;

/**
 * Created by swissbib on 1/11/16.
 */
public class Utilities {

    public static boolean checkProperties(Properties props) {

        String[] manProps = new String[] {"hostES","portES","clusterName", "oclcKey", "hostMongo", "portMongo",
                "mongoDataDB", "mongoAuthDB", "mongoDataCollection", "oclcRequestURL", "oclcIDURL","numberBibResources",
                "mongoEnrichedCollection"};

        for (String prop: manProps) {
            if (!props.containsKey(prop)) {
                System.err.println("property " + prop + " is missing");
                return false;
            }
        }

        return true;

    }


    public static MongoDBWrapper createMongoWrapper( Properties configProps) {

        MongoDBWrapper mDB = null;

        // a little bit silly to read single props and to handover the reference
        // but this code is quick an dirty

        if (configProps.containsKey("mongoUser") && configProps.containsKey("mongoPassword")) {
            mDB = new MongoDBWrapper(configProps,
                    configProps.getProperty("hostMongo"),
                    configProps.getProperty("portMongo"),
                    configProps.getProperty("mongoDataDB"),
                    configProps.getProperty("mongoDataCollection"),
                    configProps.getProperty("mongoAuthDB"),
                    configProps.getProperty("mongoUser"),
                    configProps.getProperty("mongoPassword"));
        } else {

            mDB = new MongoDBWrapper(configProps,
                    configProps.getProperty("hostMongo"),
                    configProps.getProperty("portMongo"),
                    configProps.getProperty("mongoDataDB"),
                    configProps.getProperty("mongoDataCollection"),
                    configProps.getProperty("mongoAuthDB"));
        }

        return mDB;


    }



}
