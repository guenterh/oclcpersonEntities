package org.swissbib.linked.oclc.entities;

import java.util.Map;
import java.util.Properties;

/**
 * Created by swissbib on 12/29/15.
 */
public abstract class ScanPerson  {


    protected String host = "localhost";
    protected Integer port = 9300;
    protected String clustername = "linked-swissbib";
    protected APISearch api;

    protected MongoDBWrapper mongoDBWrapper;



    public final void init (String host, String clustername) {

        this.init(host, 9300, clustername);
    }


    public final void init (String host) {

        this.init(host, 9300, "linked-swissbib");
    }


    public final void init (String host, Integer  port, String clustername   ) {

        this.host = host;
        this.port = port;
        this.clustername = clustername;
        this.connectToCluster();
    }

    public final void init (Properties configProps) {

        this.init(configProps.getProperty("hostES"),
                Integer.valueOf(configProps.getProperty("portES")),
                configProps.getProperty("clusterName"));


    }



    public void setAPI (APISearch api) {
        this.api = api;
    }
    public void setMongoWrapper (MongoDBWrapper mWrapper) {
        this.mongoDBWrapper = mWrapper;
    }

    protected abstract void connectToCluster();

    protected abstract void disconnectFromCluster();

    protected String prepareQuery (Map<String, Object> sourceMap, boolean personWithBirthYear) {
        StringBuilder queryTerm = new StringBuilder();

        if (personWithBirthYear) {
            if (sourceMap.containsKey("foaf:lastName") && sourceMap.containsKey("foaf:firstName")) {
                queryTerm.append((String) sourceMap.get("foaf:lastName")).append(" ").append((String) sourceMap.get("foaf:firstName"));
            } else if (sourceMap.containsKey("foaf:lastName")) {
                queryTerm.append((String) sourceMap.get("foaf:lastName"));
            } else if (sourceMap.containsKey("foaf:firstName")) {
                queryTerm.append((String) sourceMap.get("foaf:firstName"));
            }

        } else if (sourceMap.containsKey("rdfs:label")) {
            queryTerm.append((String) sourceMap.get("rdfs:label"));
        }

        return queryTerm.toString();


    }

}
