package org.swissbib.linked.oclc.entities;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by swissbib on 12/29/15.
 */
public class ScanPersonES2 extends ScanPerson{

    TransportClient client = null;

    public void init() {



    }


    @Override
    protected void connectToCluster() {


        try {
            Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", this.clustername)
                    .put("client.transport.sniff", true).build();
            client = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(this.host), this.port));



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



    }

    @Override
    protected void disconnectFromCluster() {
        if (this.client != null) {
            this.client.close();
        }

    }
}
