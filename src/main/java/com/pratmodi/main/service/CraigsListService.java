package com.pratmodi.main.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.google.gson.Gson;
import com.pratmodi.main.dto.Item;
import org.springframework.stereotype.Component;

import javax.json.Json;
import javax.json.JsonException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.*;
import java.util.*;
import java.util.logging.Logger;

@Component
public class CraigsListService {

 //   String baseURL = "https://newyork.craigslist.org/search/moa?query=";

    String baseURL = "https://bangalore.craigslist.org/search/ppa?";

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
        int i =0;
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

                System.out.println(String.format("Name : %s Url : %s Price : %s", itemName, itemPrice, itemUrl));

            }
            this.convertToJSON(items);
        } else {
            System.out.println("No items found !");
        }
    }

    public StringBuilder convertToJSON(List<HtmlElement> items) throws IOException {
        StringBuilder sb = new StringBuilder("");
        String jsonString = null;
        try {
            for (HtmlElement htmlItem : items) {
                HtmlDivision itemAnchor = ((HtmlDivision) htmlItem.getFirstByXPath(".//div[@class='result-info']"));
                HtmlDivision desc = ((HtmlDivision) htmlItem.getFirstByXPath(".//div/h3/a[@class='result-info']"));
                HtmlElement spanPrice = ((HtmlElement) htmlItem.getFirstByXPath(".//a/span[@class='result-price']"));
                HtmlSpan hide = ((HtmlSpan) htmlItem.getFirstByXPath(".//div/span/span[@class='result-hood']"));

                // It is possible that an item doesn't have any
                // price, we set the price to 0.0 in this case
                String itemPrice = spanPrice == null ? "0.0" : spanPrice.asNormalizedText();

                Item item = new Item();

                item.setTitle(itemAnchor.asNormalizedText());
                item.setUrl(this.baseURL + itemAnchor.getAttribute("href"));
                if(desc!=null) {
                    item.setDescription(desc.getVisibleText());
                }else {
                    item.setDescription("NULL");
                }
                if(hide!=null) {
                    item.setHideThisPosting(hide.getTextContent());
                }else {
                    item.setHideThisPosting("NULL");
                }
            //    item.setPrice(new BigDecimal(itemPrice.replace("Rs", "")));

                DecimalFormat fmt = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
                fmt.setCurrency(Currency.getInstance(new Locale("en", "in")));
                fmt.setParseBigDecimal(true);
                BigDecimal n = (BigDecimal) fmt.parse("10934,375");
                System.out.println(fmt.format(new BigDecimal("100000000")));
                item.setPrice(n);

//                BigDecimal n = (BigDecimal) fmt.parse(itemPrice);
//                item.setPrice(n);


                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);

                jsonString = mapper.writeValueAsString(item);

                sb.append(jsonString);

                System.out.println(jsonString);
            }
        } catch (JsonException je) {
            je.printStackTrace();
            logger.info("JSON Exception: " + je);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.info("JSON Processing Exception: " + e);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("^^^^^^^^^^^^"+sb+"^^^^^^^^^^^^");
        this.writeTOCSV(sb);
        return sb;
    }

    public void writeTOCSV(StringBuilder str) throws IOException {
        FileWriter recipesFile = new FileWriter("C:\\Users\\pratm\\OneDrive\\Documents\\IDEA Intellij\\UBS\\ac41e4ba-375c-4bc7-b755-6ef39923aa97\\JavaTwitterScraper\\recipes.json", true);
        Gson gson = new Gson();
        String jsonString = gson.toJson(str);
        recipesFile.write(jsonString);
        recipesFile.flush();
        recipesFile.close();
    }

}
