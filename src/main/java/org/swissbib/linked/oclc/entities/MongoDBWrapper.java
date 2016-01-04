package org.swissbib.linked.oclc.entities;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.bson.Document;

import java.util.Arrays;

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
    private MongoCollection<Document> dbDataCollection;


    public MongoDBWrapper(String host,
                          String port,
                          String dataDB,
                          String dataCollection,
                          String authDB) {

         this(host, port, dataDB, dataCollection,authDB, null, null);



    }

    public MongoDBWrapper(String host,
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




}
