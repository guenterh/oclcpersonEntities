package org.swissbib.linked.oclc.entities;

/**
 * Created by swissbib on 12/29/15.
 */
public abstract class ScanPerson {


    protected String host = "localhost";
    protected Integer port = 9300;
    protected String clustername = "linked-swissbib";


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

    protected abstract void connectToCluster();

    protected abstract void disconnectFromCluster();



}
