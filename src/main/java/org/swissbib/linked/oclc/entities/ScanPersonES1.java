package org.swissbib.linked.oclc.entities;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.netty.util.internal.SystemPropertyUtil;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by swissbib on 12/29/15.
 */
public class ScanPersonES1 extends ScanPerson {

    private TransportClient client;

    @Override
    protected void connectToCluster() {

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", this.clustername)
                .build();


        this.client = new TransportClient(settings);
        client.addTransportAddress(new InetSocketTransportAddress(this.host,this.port));




        /*
        SearchResponse response = client.prepareSearch("testsb")
                .setTypes("person")
                .execute()
                .actionGet();

        client.close();
        */


    }


    public void openScan () {


        QueryBuilder qb = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("type", "Person"));


        qb =  QueryBuilders.matchAllQuery();


        SearchResponse scrollResp = client.prepareSearch("testsb")
                .setTypes("person")
                .setSearchType(SearchType.SCAN)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).execute().actionGet(); //100 hits per shard will be returned for each scroll
        //Scroll until no hits are returned
        //long anzahl = scrollResp.getHits().getTotalHits();

        scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();


        long sum = 0;
        while (true) {
            long i = 0;
            for (SearchHit hit : scrollResp.getHits().getHits()) {

                i++;

                String id = hit.getId();
                Map<String, SearchHitField> fields = hit.getFields();
                Map<String, Object> sourcemap =  hit.getSource();

                String personId =  sourcemap.containsKey("@id") ? (String) sourcemap.get("@id") : null;
                if (null != personId) {
                    //QueryBuilder q =  QueryBuilders.("dc:contributor.foaf:Person", QueryBuilders.matchQuery("@id", personId));
                    /*
                    SearchResponse nestedPerson = client.prepareSearch("testsb")
                            .setTypes("bibliographicResource")
                            //.setSearchType(SearchType.)
                            //.setScroll(new TimeValue(60000))
                            .setQuery(q)
                            .setSize(1000).execute().actionGet(); //100 hits per shard will be returned for each scroll

                    long allBooksFromPerson =  nestedPerson.getHits().getTotalHits();
                    System.out.println(allBooksFromPerson);
                    */

                }

                System.out.println(hit.getSourceAsString());

                //String personIdentifier = hit.field("@id").getValue();
                //System.out.println(personIdentifier);
                //String source = hit.getSourceAsString();
                //System.out.println(source);

                //Handle the hit...
            }

            sum += i;

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }

        System.out.println(sum);



    }


    @Override
    protected void disconnectFromCluster() {
        if (this.client != null) {
            this.client.close();
        }
    }


}
