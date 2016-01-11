package org.swissbib.linked.oclc.entities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by swissbib on 1/11/16.
 */
public class AnalyzeCollectedData {


    MongoDBWrapper mongoDBWrapper = null;


    public void setMongoWrapper (MongoDBWrapper mWrapper) {
        this.mongoDBWrapper = mWrapper;
    }



    public static void main(String [] args) {

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

            if (!Utilities.checkProperties(configProps)) {

                System.err.println("properties not correct");
                System.exit(1);
            }

            MongoDBWrapper mDB = Utilities.createMongoWrapper(configProps);

            AnalyzeCollectedData aD = new AnalyzeCollectedData();
            aD.setMongoWrapper(mDB);

            mDB.analyzeData();




            mDB.disConnectDB();





        } catch (IOException ioException) {

            ioException.printStackTrace();
        }
    }
}
