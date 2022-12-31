package com.pratmodi.main.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.google.gson.*;
import com.pratmodi.main.dto.ItemList;
import org.elasticsearch.action.bulk.BulkItemRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.pratmodi.main.dto.Item;
import com.pratmodi.main.es.ESConfiguration;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpHost;
import org.apache.kafka.common.protocol.types.Field;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.compress.CompressorFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.PluginsService;
import org.elasticsearch.xcontent.ToXContent;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.elasticsearch.xcontent.XContentType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.json.Json;
import javax.json.JsonException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.elasticsearch.xcontent.XContentFactory.jsonBuilder;

@Component
public class CraigsListService {

    String baseURL = "https://bangalore.craigslist.org/search/ppa?";
    //   String baseURL = "https://newyork.craigslist.org/search/moa?query=";
    @Autowired
    private ESConfiguration esConfiguration;
    private Logger logger = Logger.getLogger(CraigsListService.class.getName());

    public void prepareClient() throws IOException {
        // Define the search term
        String searchQuery = "iphone 13";

        // Instantiate the client
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);

        // Set up the URL with the search term and send the request
        //    String searchUrl = baseURL + URLEncoder.encode(searchQuery, "UTF-8");
        String searchUrl = baseURL;
        HtmlPage page = client.getPage(searchUrl);
        this.extractProductAndProperties(page);
    }

    public void extractProductAndProperties(HtmlPage page) throws IOException {
// Retrieve all <li> elements
        List<HtmlElement> items = page.getByXPath("//li[@class='result-row']");
        int i = 0;
        if (!items.isEmpty()) {
            // Iterate over all elements
            for (HtmlElement item : items) {

                // Get the details from <p class="result-info"><a href=""></a></p>
                HtmlDivision itemAnchor = ((HtmlDivision) item.getFirstByXPath(".//div[@class='result-info']"));

                // Get the price from <a><span class="result-price"></span></a>
                HtmlElement spanPrice = ((HtmlElement) item.getFirstByXPath(".//a/span[@class='result-price']"));

                String itemName = itemAnchor.asNormalizedText();
                String itemUrl = itemAnchor.getAttributeDirect("href");

                // It is possible that an item doesn't have any price
                String itemPrice = spanPrice == null ? "0.0" : spanPrice.asNormalizedText();

                System.out.println(String.format("String.format "+"Name : %s Url : %s Price : %s", itemName, itemPrice, itemUrl)+ " END String.format");

            }
            this.convertToJSON(items);
        } else {
            System.out.println("No items found !");
        }
    }

    public StringBuilder convertToJSON(List<HtmlElement> items) throws IOException {
        StringBuilder sb = new StringBuilder();
        String finalJSON = "";
        JsonObject convertedObject = null;
        Item item = new Item();
        JSONObject completeJSONdata = new JSONObject();
        Gson gson = new GsonBuilder().setLenient().create();
        try {

            JSONArray jsonArray = new JSONArray();

            for (HtmlElement htmlItem : items) {
                HtmlDivision itemAnchor = ((HtmlDivision) htmlItem.getFirstByXPath(".//div[@class='result-info']"));
                HtmlDivision desc = ((HtmlDivision) htmlItem.getFirstByXPath(".//div/h3/a[@class='result-info']"));
                DomAttr url = ((DomAttr) htmlItem.getFirstByXPath(".//div/h3/a/@href"));
   //             HtmlElement spanPrice = ((HtmlElement) htmlItem.getFirstByXPath(".//a/span[@class='result-price']"));
                HtmlElement price = ((HtmlElement) htmlItem.getFirstByXPath(".//div/span[@class='result-meta']/span[@class='result-price']"));
                HtmlSpan hide = ((HtmlSpan) htmlItem.getFirstByXPath(".//div/span/span[@class='result-hood']"));

                // It is possible that an item doesn't have any
                // price, we set the price to 0.0 in this case
    //            String itemPrice = spanPrice == null ? "0.0" : spanPrice.asNormalizedText();

                if(itemAnchor!=null) {
                    item.setTitle(itemAnchor.getVisibleText().replaceAll("^\"|\"$", "").replace("\n","").strip().trim());
                }else{
                    item.setTitle("null");
                }
                if(url!=null) {
                    item.setUrl(this.baseURL + url);
                }else{
                    item.setUrl("null");
                }
                if (desc != null) {
                    item.setDescription(desc.getTextContent());
                } else {
                    item.setDescription("NULL");
                }
                if (hide != null) {
                    item.setHideThisPosting(hide.getTextContent());
                } else {
                    item.setHideThisPosting("NULL");
                }
                //    item.setPrice(new BigDecimal(itemPrice.replace("Rs", "")));

                DecimalFormat fmt = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
                fmt.setCurrency(Currency.getInstance(new Locale("en", "in")));
                fmt.setParseBigDecimal(true);
                                    // BigDecimal n = (BigDecimal) fmt.parse("10934,375");

                if(price!=null) {
                    item.setPrice((BigDecimal) fmt.parse("10934,375"));
                }else{
                    item.setPrice(null);
                }

//                BigDecimal n = (BigDecimal) fmt.parse(itemPrice);
//                item.setPrice(n);


//                ObjectMapper mapper = new ObjectMapper();
//                mapper.enable(SerializationFeature.INDENT_OUTPUT);

                Gson outerJSON = new GsonBuilder().setPrettyPrinting().create();

   //             jsonString = mapper.writeValueAsString(item);

                String jsonString = outerJSON.toJson(item);

                String temp = jsonString.replace("^\"|\"$", "");
                finalJSON = StringEscapeUtils.unescapeJava(temp);

                item.setI(item.getI());
                item.getI().incrementAndGet();


//                if (finalJSON != null) {
//                    finalJSON = StringEscapeUtils.unescapeJava(temp);
//                    sb = sb.append(finalJSON.strip().trim());
//                }

                sb = sb.append(finalJSON.strip().trim());





            //    convertedObject = new GsonBuilder().setLenient().create().fromJson(sb.toString(), JsonObject.class);
            //    completeJSONdata = completeJSONdata + gson.toJson(finalJSON.strip().trim());
            //    completeJSONdata = gson.fromJson(finalJSON.strip().trim(),JSONObject.class);

                JSONObject jo = new JSONObject(item);





                jsonArray.put(jo);

            }
            JSONObject finalObject = new JSONObject();

            finalObject.put("items", jsonArray );

            System.out.println("|||||||||||||"+finalObject+"|||||||||||||");
         //   this.persistAll(finalJSON);
            this.persistAll(String.valueOf(finalObject));

        //    this.bulkInsert(String.valueOf(finalObject));
        } catch (JsonException je) {
            je.printStackTrace();
            logger.info("JSON Exception: " + je);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("^^^^^^^^^^^^" + sb + "^^^^^^^^^^^^");
        this.writeTOCSV(sb);
        return sb;
    }

    public String writeTOCSV(StringBuilder str) throws IOException {
        FileWriter recipesFile = null;
        String jsonString = "";
        recipesFile = new FileWriter("C:\\Users\\pratm\\OneDrive\\Documents\\IDEA Intellij\\UBS\\ac41e4ba-375c-4bc7-b755-6ef39923aa97\\JavaCraigslistScraper\\recipes.json", true);
        Gson gson = new Gson();
        jsonString = gson.toJson(str);
    //    this.persistAll(jsonString);
        recipesFile.write(jsonString);
        recipesFile.flush();
        recipesFile.close();

        return jsonString;
    }

    public void persistAll(String object/*String str*/) throws IOException {
        try {
            Item itemObject = new Item();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

           Item item = gson.fromJson(object, Item.class);

            itemObject.setI(itemObject.getI());
            itemObject.getI().incrementAndGet();

            XContentBuilder builder = jsonBuilder()
                    .startObject()
                    .field(String.valueOf(itemObject.getI().toString()!=null?itemObject.getI().toString():0), item.getI().toString())
                    .field(itemObject.getDescription()!=null?itemObject.getDescription():"null", item.getDescription())
                    .field(itemObject.getTitle()!=null?itemObject.getTitle():"null", item.getTitle())
                    .field(itemObject.getUrl()!=null?itemObject.getUrl():"null", item.getUrl())
                    .field(itemObject.getHideThisPosting()!=null?itemObject.getHideThisPosting():"null", item.getHideThisPosting())
                    .field(itemObject.getPrice()!=null? String.valueOf(itemObject.getPrice()) :"null", item.getPrice())
                    .endObject().prettyPrint();

            IndexRequest indexRequest = new IndexRequest(esConfiguration.getES_INDEX());

            indexRequest.source(object, Item.class);

            IndexResponse response = esConfiguration.getESClient().index(indexRequest, RequestOptions.DEFAULT);

            System.out.println("^^^^^^^^&&&&&&&&&&&&&&^^^^^^^^^^" + response.getIndex() + "    " + response.getResult() + "    " + response.getId() + "    " + "^^^^^^^^&&&&&&&&&&&&&&^^^^^^^^^^");

        } catch (IllegalStateException | JsonSyntaxException exception) {
                logger.info(" EXCEPTION OCCURRED!!!!!!!!!!!!"+exception);
        }
    }

//    public void bulkInsert(String object) throws JSONException, IOException {
//        Map<String,List<Item>> items = this.convertDataToListUsingObjectMapper(object);
//
//        BulkRequest br = new BulkRequest();
//
//        for(Map.Entry<String,List<Item>> m:items.entrySet()){
//            br.add(new IndexRequest(esConfiguration.getES_INDEX()).id(String.valueOf(m.getValue().get(0).getI().incrementAndGet()))
//                    .source(m.getValue(),XContentType.JSON));
//        }
//
//
//        BulkResponse result = esConfiguration.getESClient().bulk(br,RequestOptions.DEFAULT);
//
//        System.out.println(result.getItems()+" "+result.getTook());
//
//        // Log errors, if any
//        if (result.hasFailures()) {
//            logger.info("Bulk had errors");
//            for (BulkItemResponse item: result.getItems()) {
//                if (item.getFailure() != null) {
//                    logger.info(item.getFailure().getMessage());
//                }
//            }
//        }
//    }

//    public List<Item> convertDataToList(String object) throws JSONException {
//        Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
//
//        JSONObject jsnobject = new JSONObject(gson.toJson(object));
//        //Printing JSON object
//        System.out.println("JSON Object");
//        System.out.println(jsnobject);
//        //Getting languages JSON array from the JSON object
//        JSONArray jsonArray = jsnobject.getJSONArray("items");
//        //Printing JSON array
//        System.out.println("JSON Array");
//        System.out.println(jsonArray);
//        //Creating an empty ArrayList of type Object
//        ArrayList<Item> listdata = new ArrayList<Item>();
//
//
//
//        JsonObject jsonObject = new JsonParser().parse(object).getAsJsonObject();
//        final JsonArray data = jsonObject.getAsJsonArray("items");
//
//        Item items = gson.fromJson(object,Item.class);
//
//        //Checking whether the JSON array has some value or not
//        if (jsonArray != null) {
//
//            //Iterating JSON array
//            for (int i=0;i<jsonArray.length();i++){
//                //Adding each element of JSON array into ArrayList
//                listdata.add((Item)jsonArray.get(i));
//            }
//        }
//        return listdata;
//    }

    public Map<String,List<Item>> convertDataToListUsingObjectMapper(String object) throws JSONException, JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        Map<String,List<Item>> hashmap = new HashMap<>();
        hashmap  = mapper.readValue(object, new TypeReference<Map<String,List<Item>>>() {});
        return hashmap;
    }



}
