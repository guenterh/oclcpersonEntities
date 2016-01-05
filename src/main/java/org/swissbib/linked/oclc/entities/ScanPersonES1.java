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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by swissbib on 12/29/15.
 */
public class ScanPersonES1  extends ScanPerson {

    private TransportClient client;

    @Override
    protected void connectToCluster() {

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", this.clustername)
                .build();


        this.client = new TransportClient(settings);
        client.addTransportAddress(new InetSocketTransportAddress(this.host,this.port));

    }


    public void openScan () {


        ExistsFilterBuilder filter=FilterBuilders.existsFilter("dbp:birthYear");
        //QueryBuilder qb = QueryBuilders.filteredQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("type", "Person")),filter);
        QueryBuilder qb = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),filter);
        //QueryBuilder qb = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("type", "Person"));


        //qb =  QueryBuilders.matchAllQuery();

        SearchResponse scrollResp = client.prepareSearch("testsb")
                .setTypes("person")
                .setSearchType(SearchType.SCAN)
                .setScroll(new TimeValue(60000))

                .setQuery(qb)
                .setSize(100).execute().actionGet(); //100 hits per shard will be returned for each scroll
        //Scroll until no hits are returned
        long anzahl = scrollResp.getHits().getTotalHits();

        scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
        boolean personWithBirthYear = true;

        long sum = 0;
        while (true) {
            long i = 0;
            for (SearchHit personHit : scrollResp.getHits().getHits()) {

                i++;
                MongoDocumentBuilder mongoDocBuilder = new MongoDocumentBuilder();
                Document personMongoDoc =  mongoDocBuilder.jsonToDocument(personHit.getSourceAsString());

                //String docIdPerson = personHit.getId();
                //Map<String, SearchHitField> fields = personHit.getFields();
                Map<String, Object> sourcemapPerson =  personHit.getSource();

                String personId =  sourcemapPerson.containsKey("@id") ? (String) sourcemapPerson.get("@id") : null;
                if (null != personId) {
                    String qOclcApi = null;

                    String qTerm = this.prepareQuery(sourcemapPerson,personWithBirthYear);

                    if (qTerm.length() > 0) {
                        //qOclcApi = (String)sourcemapPerson.get("rdfs:label");
                        String response = this.api.executeSearch(qTerm,OCLCQueryType.termLookUp);

                        Document oclcTermQueryMongoDoc =  mongoDocBuilder.jsonToDocument(response);

                        ArrayList<Document> resultList = (ArrayList<Document>) oclcTermQueryMongoDoc.get("result");


                        if (null != resultList) {
                            for (Document itemDoc : resultList) {
                                if (null != itemDoc.getString("uri")) {

                                    String oclcPersonID = itemDoc.getString("uri");

                                    String additionalIds = this.api.executeSearch(oclcPersonID, OCLCQueryType.idLookUp);
                                    if (null != additionalIds) {
                                        Document bbAddIds = mongoDocBuilder.jsonToDocument(additionalIds);
                                        itemDoc.put("additionalIds", bbAddIds);
                                    }
                                }
                            }

                        }
                        personMongoDoc.append("oclcMappings",oclcTermQueryMongoDoc);


                        QueryBuilder q =  QueryBuilders.matchQuery("dc:contributor.foaf:Person.@id", personId);

                        SearchResponse bibResourcesOfCurrentPerson = client.prepareSearch("testsb")
                                .setTypes("bibliographicResource")
                                //.setSearchType(SearchType.)
                                //.setScroll(new TimeValue(60000))
                                .setQuery(q)
                                .setSize(1000).execute().actionGet(); //100 hits per shard will be returned for each scroll

                        ArrayList<Document> listResources = new ArrayList<>();

                        for (SearchHit bibResource : bibResourcesOfCurrentPerson.getHits().getHits()) {
                            //String response = this.api.executeSearch(qOclcApi);
                            Document bibDocument = mongoDocBuilder.jsonToDocument(bibResource.getSourceAsString());

                            String bibId = bibResource.getId();


                            GetResponse getResponse = client.prepareGet("testsb", "document", bibId)
                                    .execute()
                                    .actionGet();


                            if (null != getResponse.getSourceAsString()) {
                                bibDocument.append("docForBib", mongoDocBuilder.jsonToDocument(getResponse.getSourceAsString()));
                            }
                            listResources.add(bibDocument);


                        }


                        personMongoDoc.append("relatedBibResources",listResources);

                        this.mongoDBWrapper.writeToDB(personMongoDoc);
                    }

                }

            }
            sum += i;

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
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
