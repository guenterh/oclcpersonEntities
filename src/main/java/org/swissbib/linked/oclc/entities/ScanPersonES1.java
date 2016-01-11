package org.swissbib.linked.oclc.entities;

import com.mongodb.DBObject;
import org.bson.BasicBSONObject;
import org.bson.Document;
import org.bson.types.BasicBSONList;
import org.elasticsearch.action.get.GetResponse;
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
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by swissbib on 12/29/15.
 */
public class ScanPersonES1  extends ScanPerson {

    private TransportClient client;
    private static final Logger logger = LogManager.getLogger(ScanPersonES1.class);

    @Override
    protected void connectToCluster() {

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", this.clustername)
                .build();


        this.client = new TransportClient(settings);
        client.addTransportAddress(new InetSocketTransportAddress(this.host,this.port));

    }


    public void openScan () {


        LocalDateTime dt1 = null;
        LocalDateTime dt2 = null;
        String timeDiff = null;

        ExistsFilterBuilder filter=FilterBuilders.existsFilter("dbp:birthYear");
        //QueryBuilder qb = QueryBuilders.filteredQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("type", "Person")),filter);
        QueryBuilder qb = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),filter);
        //QueryBuilder qb = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("type", "Person"));


        //qb =  QueryBuilders.matchAllQuery();



        logger.info("dies ist ein Test");
        logger.debug("noch ein Test");
        SearchResponse scrollResp = client.prepareSearch("testsb")
                .setTypes("person")
                .setSearchType(SearchType.SCAN)
                .setScroll(new TimeValue(60000))

                .setQuery(qb)
                .setSize(100).execute().actionGet(); //100 hits per shard will be returned for each scroll
        //Scroll until no hits are returned
        //long anzahl = scrollResp.getHits().getTotalHits();

        scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
        boolean personWithBirthYear = true;

        long sum = 0;
        long numberRequestsAgainstAPI = null != this.configProperties &&
                this.configProperties.containsKey("totalNumberOfRequestsAgainstOCLC") ?
                Long.valueOf(this.configProperties.getProperty("totalNumberOfRequestsAgainstOCLC")) :
                10;
        System.out.println("number of defined requests: " + String.valueOf(numberRequestsAgainstAPI) + "\n");

        Pattern searchedPerson = Pattern.compile("Giovanni Battista", Pattern.CASE_INSENSITIVE);

        while (sum < numberRequestsAgainstAPI) {

            for (SearchHit personHit : scrollResp.getHits().getHits()) {

                if (sum >= numberRequestsAgainstAPI) {
                    break;
                }

                MongoDocumentBuilder mongoDocBuilder = new MongoDocumentBuilder();
                Document personMongoDoc =  mongoDocBuilder.jsonToDocument(personHit.getSourceAsString());

                //String docIdPerson = personHit.getId();
                //Map<String, SearchHitField> fields = personHit.getFields();
                Map<String, Object> sourcemapPerson =  personHit.getSource();

                String personId =  sourcemapPerson.containsKey("@id") ? (String) sourcemapPerson.get("@id") : null;
                if (null != personId) {

                    String qTerm = this.prepareQuery(sourcemapPerson,personWithBirthYear);

                    if (qTerm.length() > 0) {
                        //qOclcApi = (String)sourcemapPerson.get("rdfs:label");

                        if (searchedPerson.matcher(qTerm).find()) {
                            System.out.println();
                        }


                    }

                }
                sum++;
            }


            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            if (sum >=  numberRequestsAgainstAPI || scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }

        System.out.println("number of requests against term API: " + String.valueOf(sum));

    }


    @Override
    protected void disconnectFromCluster() {
        if (this.client != null) {
            this.client.close();
        }
    }


}
