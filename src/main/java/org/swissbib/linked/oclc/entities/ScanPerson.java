package org.swissbib.linked.oclc.entities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by swissbib on 12/29/15.
 */
public abstract class ScanPerson  {


    protected String host = "localhost";
    protected Integer port = 9300;
    protected String clustername = "linked-swissbib";
    protected APISearch api;

    protected Pattern timeDiffPattern = Pattern.compile("PT-([0-9.]+)S");

    protected MongoDBWrapper mongoDBWrapper;

    protected Properties configProperties = null;



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

        this.configProperties = configProps;


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



    protected String getNumericTimeDifference(LocalDateTime dt1, LocalDateTime dt2 ) {

        Duration duration =  Duration.between(dt2,dt1);

        Matcher m =  timeDiffPattern.matcher(duration.toString());

        return  m.find() ?  m.group(1) : duration.toString();

    }


}
