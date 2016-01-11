package org.swissbib.linked.oclc.entities;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by swissbib on 1/4/16.
 */
public class MongoDBWrapper {

    private String host;
    private String port;
    private String dataDB;
    private String dataCollection;
    private String user;
    private String password;
    private String authDB;
    private MongoCollection<Document> dbDataCollection = null;
    private MongoCollection<Document> dbEnrichedCollection = null;
    private Properties configProps = null;

    private MongoClient mClient = null;


    public MongoDBWrapper(Properties configProps,
                          String host,
                          String port,
                          String dataDB,
                          String dataCollection,
                          String authDB) {

         this(configProps,host, port, dataDB, dataCollection,authDB, null, null);



    }

    public MongoDBWrapper(Properties configProps,
                          String host,
                          String port,
                          String dataDB,
                          String dataCollection,
                          String authDB,
                          String user,
                          String password) {

        this.host = host;
        this.port = port;
        this.dataDB = dataDB;
        this.dataCollection = dataCollection;
        this.authDB = authDB;
        this.user = user;

        this.password = password;
        this.configProps = configProps;
        try {
            connectToDB();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    protected void connectToDB() throws Exception{

        ServerAddress server = new ServerAddress(this.host, Integer.valueOf(this.port));
        MongoClient mClient = null;

        MongoDatabase db = null;

        if (null != user && null != password) {
            MongoCredential credential = MongoCredential.createMongoCRCredential(this.user, this.authDB, this.password.toCharArray());
            mClient = new MongoClient(server, Arrays.asList(credential));
            db =  mClient.getDatabase(this.authDB);
        } else {
            mClient = new MongoClient( server );
            db =  mClient.getDatabase(this.authDB);
        }

        if (db.getName() == null ) {
            throw new Exception("authentication against database wasn't possible - no GND Processing will take place when type is called from XSLT templates");
        }

        MongoDatabase dataDb = mClient.getDatabase(this.dataDB);



        this.dbDataCollection =  dataDb.getCollection(this.dataCollection);
        this.dbEnrichedCollection = dataDb.getCollection(this.configProps.getProperty("mongoEnrichedCollection"));

        this.mClient = mClient;

        /*
        Document doc = new Document("name", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("info", new Document("x", 203).append("y", 102));
        this.dbDataCollection.insertOne(doc);
        */
        //this.dataCollection.

        System.out.println("initialization of MOngo DB ok");

    }

    public void writeToDB (Document objectsToSerialize) {

        this.dbDataCollection.insertOne(objectsToSerialize);

    }


    public void writeToEnriched (String id, Document objectsToSerialize) {

        if (this.dbEnrichedCollection.find(new Document("@id", id)).iterator().hasNext()) {
            this.dbEnrichedCollection.findOneAndReplace(new Document("@id", id),objectsToSerialize);
        } else {
            this.dbEnrichedCollection.insertOne(objectsToSerialize);
        }

    }



    public void disConnectDB () {

        if (null != this.mClient) {
            this.mClient.close();
        }
    }


    public void analyzeData () {


        FindIterable<Document> iter = this.dbDataCollection.find();

        MongoCursor<Document> cursor = iter.iterator();

        int sum = 0;

        while (cursor.hasNext()) {
            Document mongoDoc =  cursor.next();
            if (mongoDoc.containsKey("dbp:birthYear")) {

                if ((mongoDoc.get("dbp:birthYear") instanceof String) && mongoDoc.containsKey("oclcMappings")) {
                    ArrayList<Document> matchedDocs = this.getSamePersons(mongoDoc.getString("dbp:birthYear"),
                            (Document)mongoDoc.get("oclcMappings"));
                    if (null != matchedDocs && matchedDocs.size() > 0) {
                        mongoDoc.append("matchedNumberOfDocs",matchedDocs.size() );
                        ArrayList<ArrayList<String>> matchedOCLCDocs = new ArrayList<ArrayList<String>>();
                        for (Document matched:  matchedDocs) {

                            if (matched.containsKey("uri")) {
                                ArrayList<String> idsFromSingleDoc = new ArrayList<String>();
                                idsFromSingleDoc.add((String) matched.get(("uri")));
                                if (matched.containsKey("additionalIds")) {
                                    Document addIds = (Document) matched.get("additionalIds");
                                    if (addIds.containsKey("sameAs") && addIds.get("sameAs") instanceof ArrayList) {
                                        ArrayList<String> tsIds = (ArrayList<String>) addIds.get("sameAs");
                                        for (String tid : tsIds) {
                                            idsFromSingleDoc.add(tid);
                                        }
                                    }
                                }
                                matchedOCLCDocs.add(idsFromSingleDoc);
                            }


                        }
                        mongoDoc.append("oclcKeys", matchedOCLCDocs);
                        this.writeToEnriched(mongoDoc.getString("@id"),mongoDoc);

                        //mongoDoc.append()
                    }
                    String t = "";
                }

            }
            sum++;
        }

        System.out.print(sum);


    }

    private ArrayList<Document> getSamePersons (String year, Document personDoc) {

        ArrayList<Document> aL = new ArrayList<Document>();

        if (personDoc.containsKey("result") && personDoc.get("result") instanceof ArrayList) {
            ArrayList oclcPersons = (ArrayList) personDoc.get("result");
            for (Object oPerson : oclcPersons) {
                Document person = (Document) oPerson;
                if (person.containsKey("birthDate")) {
                    if (Pattern.compile(year).matcher((String) person.get("birthDate")).find() ) {
                        aL.add(person);
                    }

                }

            }
            String s = "";
        }


        return aL;


    }


}
