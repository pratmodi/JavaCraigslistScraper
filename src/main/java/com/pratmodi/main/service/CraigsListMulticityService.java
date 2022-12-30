package com.pratmodi.main.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.pratmodi.main.dto.Item;
import org.springframework.stereotype.Component;

import javax.json.JsonException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Component
public class CraigsListMulticityService {

    String URL = "https://newyork.craigslist.org/search/moa?query=iphone%2013&postedToday=1&srchType=T";
    private Logger logger = Logger.getLogger(CraigsListService.class.getName());

    public void prepareClient() throws IOException {
        // Define the search term
        String searchQuery = "iphone 13";
        String[] cities = new String[]{"newyork", "boston", "washingtondc"};

// Instantiate the client
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);

        for (String city : cities)
        {
            // Set up the URL with the search term and send the request
            String searchUrl = "https://" + city + ".craigslist.org/search/moa?query=" + URLEncoder.encode(searchQuery, "UTF-8");
            HtmlPage page = client.getPage(searchUrl);

            // Here goes the rest of the code handling the content in "page"
        }
    }

    public void extractProductAndProperties(HtmlPage page) throws JsonProcessingException {
        String[] argv = page.getContentType().getBytes(StandardCharsets.UTF_8).toString().split(" ");
        String outputType = argv.length == 1 ? argv[0] : "";

        List<HtmlElement> items = page.getByXPath("//li[@class='result-row']");

        for(HtmlElement htmlItem : items){
            HtmlAnchor itemAnchor = ((HtmlAnchor) htmlItem.getFirstByXPath(".//p[@class='result-info']/a"));
            HtmlElement spanPrice = ((HtmlElement)htmlItem.getFirstByXPath(".//a/span[@class='result-price']"));

            // It is possible that an item doesn't have any
            // price, we set the price to 0.0 in this case
            String itemPrice = spanPrice == null ? "0.0" : spanPrice.asNormalizedText();

            switch (outputType)
            {
                case "json":
                    Item item = new Item();

                    item.setTitle(itemAnchor.asNormalizedText());
                    item.setUrl(URL + itemAnchor.getHrefAttribute());

                    item.setPrice(new BigDecimal(itemPrice.replace("$", "")));

                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = mapper.writeValueAsString(item);

                    System.out.println(jsonString);

                    break;

                case "csv":
                    // TODO: CSV-escaping
                    System.out.println(String.format("%s,%s,%s", itemAnchor.asNormalizedText(), itemPrice, URL + itemAnchor.getHrefAttribute()));

                    break;

                default:
                    System.out.println("Error: no format specified");

                    break;
            }
        }
    }


}
