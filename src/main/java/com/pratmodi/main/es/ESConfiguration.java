package com.pratmodi.main.es;

import com.google.gson.Gson;
import com.pratmodi.main.dto.Item;
import com.pratmodi.main.service.CraigsListService;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component("esConfiguration")
public class ESConfiguration {

    private Logger logger = Logger.getLogger(ESConfiguration.class.getName());

    public String getES_INDEX() {
        return ES_INDEX;
    }

    public void setES_INDEX(String ES_INDEX) {
        this.ES_INDEX = ES_INDEX;
    }

    private String ES_INDEX = "myindex1";

    public void createIndex() throws IOException {
        Gson gson = new Gson();
        boolean flag = false;
        CreateIndexRequest request = new CreateIndexRequest(ES_INDEX);

        if(request!=null){
            flag=true;
            this.isIndexIsCreated(flag);
        }

        request.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        );

//        request.mapping(
//                "{\n" +
//                        "  \"title\": \"text\"{\n" +
//                        "    \"description\": \"text\"{\n" +
//                        "      \"price\": \"text\"{\n" +
//                        "      \"url\": \"text\"{\n" +
//                        "      \"hideThisPosting\": \"text\"{\n" +
//                        "    }\n" +
//                        "    }\n" +
//                        "    }\n" +
//                        "  }\n" +
//                        "  }\n" +
//                        "}",
//                XContentType.JSON);

    //    request.mapping("{\"title\":\"text\",\"description\":\"text\",\"price\":\"Number\",\"url\":\"text\",\"hideThisPosting\":\"text\"}", XContentType.JSON);

        request.mapping("_doc","{\n" +
                "      \"properties\": {\n" +
                "        \"title\": {\n" +
                "          \"type\": \"text\"\n" +
                "        },\n" +
                "        \"description\": {\n" +
                "          \"type\": \"text\"\n" +
                "        }\n" +
                "        ,\"price\": {\n" +
                "          \"type\": \"Number\"\n" +
                "        }\n" +
                        ",\"url\": {\n" +
                "          \"type\": \"text\"\n" +
                "        }\n" +
                "        ,\"hideThisPosting\": {\n" +
                "          \"type\": \"text\"\n" +
                "        }\n" +
                "      }\n" +
                "}",XContentType.JSON);


        //    request.mapping(gson.toJson(Item.class), XContentType.JSON);

        request.alias(new Alias("twitter_alias").filter(QueryBuilders.termQuery("user", "pratmodi")));

    }

    public boolean isIndexIsCreated(boolean flag){
        return flag;
    }

    public RestHighLevelClient getESClient(){
        ClientConfiguration clientConfiguration =
                ClientConfiguration.builder().connectedTo("localhost:9200").build();
        RestHighLevelClient client = RestClients.create(clientConfiguration).rest();

        return client;
    }

    public void givenJsonString_whenJavaObject_thenIndexDocument() throws IOException {
        String jsonObject = "{\"age\":10,\"dateOfBirth\":1471466076564,"
                +"\"fullName\":\"John Doe\"}";
        IndexRequest request = new IndexRequest(ES_INDEX);
        request.source(jsonObject, XContentType.JSON);

        IndexResponse response = this.getESClient().index(request, RequestOptions.DEFAULT);
        String index = response.getIndex();
        long version = response.getVersion();

        assertEquals(UpdateResponse.Result.CREATED, response.getResult());
        assertEquals(1, (int) version);
        assertEquals("myindex1", index);

        System.out.println("givenJsonString_whenJavaObject_thenIndexDocument RESULT: "+assertEquals(UpdateResponse.Result.CREATED, response.getResult())+" "+
        assertEquals(1, (int) version)+" "+
        assertEquals("myindex1", index));
    }

    public IndexResponse getResponse() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("fullName", "Test")
                .field("dateOfBirth", new Date())
                .field("age", "10")
                .endObject();

        IndexRequest indexRequest = new IndexRequest(ES_INDEX);
        indexRequest.source(builder);

        IndexResponse response = this.getESClient().index(indexRequest, RequestOptions.DEFAULT);
        assertEquals(UpdateResponse.Result.CREATED, response.getResult());

        return response;
    }

    public List<Item> searchItem() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        SearchResponse response = this.getESClient().search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = response.getHits().getHits();

        Gson gson = new Gson();

        List<Item> results =
                Arrays.stream(searchHits)
                        .map(hit -> gson.fromJson(hit.getSourceAsString(),Item.class))
                        .collect(Collectors.toList());

        return results;
    }

    public SearchResponse searchByPrice(String param,int upperBoundOfrange, int lowerBoundOfrange) throws IOException {
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .postFilter(QueryBuilders.rangeQuery("age").from(lowerBoundOfrange).to(upperBoundOfrange));

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequest.source(builder);

        SearchResponse response = this.getESClient().search(searchRequest, RequestOptions.DEFAULT);

        return response;
    }

    public GetResponse getJSONDocument(String clusterID) throws IOException {
        GetRequest getRequest = new GetRequest("people");
        getRequest.id(clusterID);

        GetResponse getResponse = this.getESClient().get(getRequest, RequestOptions.DEFAULT);

        return getResponse;
    }

    public DeleteResponse deleteJSONDocument(String clusterID) throws IOException {

        DeleteRequest deleteRequest = new DeleteRequest("people");
        deleteRequest.id(clusterID);

        DeleteResponse deleteResponse = this.getESClient().delete(deleteRequest, RequestOptions.DEFAULT);

        return deleteResponse;
    }

    public SearchResponse getResultUsingQueryBuilder() throws IOException {

        QueryBuilder matchAllQuery = QueryBuilders.matchAllQuery();
        matchAllQuery.queryName(QueryBuilders.matchQuery("price", 10934).queryName());

                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchAllQuery);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(ES_INDEX);

        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);

        SearchResponse response = this.getESClient().search(searchRequest, RequestOptions.DEFAULT);

        return response;

    }

    public SearchResponse getResultUsingQueryBuilderByRange() throws IOException {

        QueryBuilder matchDocumentsWithinRange = QueryBuilders
                .rangeQuery("price").from(15).to(100);

        QueryBuilder matchAllQuery = QueryBuilders.matchAllQuery();

        matchAllQuery.queryName(matchDocumentsWithinRange.queryName());

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchAllQuery);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(ES_INDEX);

        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);

        SearchResponse response = this.getESClient().search(searchRequest, RequestOptions.DEFAULT);

        return response;
    }

    public SearchResponse getResultUsingQueryBuilderByAttribute(String attribute,String str) throws IOException {
        QueryBuilder matchSpecificFieldQuery= QueryBuilders
                .matchQuery(attribute, str);

        QueryBuilder matchAllQuery = QueryBuilders.matchAllQuery();

        matchAllQuery.queryName(matchSpecificFieldQuery.queryName());

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchAllQuery);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(ES_INDEX);

        searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH);

        SearchResponse response = this.getESClient().search(searchRequest, RequestOptions.DEFAULT);

        return response;
    }

    private boolean assertEquals(String people, String index) {
        boolean flag = false;

        if(people.equals(index)){
            flag = true;
        }
        return flag;
    }

    private boolean assertEquals(int i, int version) {
        boolean flag = false;

        if(i==version){
            flag = true;
        }
        return flag;
    }

    private boolean assertEquals(UpdateResponse.Result created, DocWriteResponse.Result result) {
        boolean flag = false;

        if(created.equals(result)){
            flag = true;
        }
        return flag;
    }

}
