package com.pratmodi.main.es;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.pratmodi.main.service.FirstService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Component
public class WriteToCSV {

    @Lazy
    @Autowired
    private FirstService firstService;

    public void writeTOCSV(HtmlPage page) throws IOException {
        FileWriter recipesFile = new FileWriter("C:\\Users\\pratm\\OneDrive\\Documents\\IDEA Intellij\\UBS\\ac41e4ba-375c-4bc7-b755-6ef39923aa97\\JavaTwitterScraper\\recipes.csv", true);
        recipesFile.write("id,name,link\n");

        Map<HtmlAnchor, Map<String,String>> mainMap = firstService.getMap();
        HtmlAnchor link;
        String recipeTitle;
        String recipeLink;

        Iterator<Map.Entry<HtmlAnchor, Map<String, String>>> iterator = mainMap.entrySet().iterator();

        int i=0;

        while(iterator.hasNext()){
                link = iterator.next().getKey();
                recipeTitle = iterator.next().getValue().entrySet().iterator().next().getKey();
                recipeLink = iterator.next().getValue().entrySet().iterator().next().getValue();
                recipesFile.write(i + "," + recipeTitle + "," + recipeLink + "\n");
                i++;
            }

        }

}
