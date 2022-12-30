package com.pratmodi.main.service;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import com.pratmodi.main.es.WriteToCSV;
import org.brotli.dec.BrotliInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class FirstService {

//    @Autowired
//    private WriteToCSV writeToCSV;

    private Map<HtmlAnchor,Map<String,String>> map;

    private Logger logger = Logger.getLogger(FirstService.class.getName());;

    public void initHTMLUnit(){
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);

//        try {
//
//            webClient.setWebConnection(
//                    new WebConnectionWrapper(webClient) {
//                        public WebResponse getResponse(WebRequest request) throws IOException {
//                            WebResponse response = super.getResponse(request);
//                            String content = response.getContentAsString(Charset.forName("UTF-8"));
//                            BufferedReader rd;
//                            if(response.getResponseHeaderValue(("content-encoding")).equals("br")) { // check if getting brotli compressed stream
//                                rd = new BufferedReader(new InputStreamReader(new BrotliInputStream(response.getContentAsStream())));
//                            }
//                            else {
//                                rd = new BufferedReader(new InputStreamReader(response.getContentAsStream()));
//                            }
//
//                            StringBuilder result = new StringBuilder();
//
//                            String line = "";
//                            while ((line = rd.readLine()) != null) {
//                                result.append(line);
//                            }
//
//                            if(content != null) {
//                                if(!content.contains("<body>") && content.contains("</head>")) {
//                                    content = content.replace("</head>", "</head>\n<body>");
//                                    if(!content.contains("</body>") && content.contains("</html>")) {
//                                        content = content.replace("</html>", "</body>\n</html>");
//                                    }
//                                }
//                            }
//                            logger.info("response: {}"+ content);
//                            WebResponseData data = new WebResponseData(line.getBytes(StandardCharsets.UTF_8),
//                                    response.getStatusCode(), response.getStatusMessage(), response.getResponseHeaders());
//                            response = new WebResponse(data, request, response.getLoadTime());
//                            return response;
//                        }
//                    });
//
//            HtmlPage page = webClient.getPage("https://foodnetwork.co.uk/italian-family-dinners/");
//
//            webClient.getCurrentWindow().getJobManager().removeAllJobs();
//            webClient.close();
//            this.getAnchors(page);
//        } catch (IOException e) {
//            logger.log(Level.SEVERE,"An error occurred: ",e);
//        }catch (ScriptException se){
//            logger.log(Level.SEVERE,"An JAVASCRIPT error occurred: ",se);
//        }

        try {
            HtmlPage page = webClient.getPage("https://foodnetwork.co.uk/italian-family-dinners/");
        //    HtmlPage page = webClient.getPage("https://newyork.craigslist.org/search/moa?query=cars");
            webClient.getCurrentWindow().getJobManager().removeAllJobs();
            webClient.close();

        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }catch (ScriptException se){

        }
    }

    public String extractTitle(HtmlPage page){
        String title = page.getTitleText();
        logger.info("Page Title is: "+title);
        return title;
    }

    public List<HtmlAnchor> getAnchors(HtmlPage page) throws IOException {
        List<HtmlAnchor> links = page.getAnchors();
        this.getcardLinkAttributes(page);
        return links;
    }

    public Map<HtmlAnchor,String> getAnchors(List<HtmlAnchor> anchors){

        Map<HtmlAnchor,String> map = new HashMap<HtmlAnchor,String>();

        for (HtmlAnchor link : anchors) {
            String href = link.getHrefAttribute();
            map.put(link,href);
            logger.info("Link: " + href);
        }
        return map;
    }

    public Map<HtmlAnchor,Map<String,String>> getcardLinkAttributes(HtmlPage page) throws IOException {

        Map<HtmlAnchor,Map<String,String>> outerMap = new HashMap<>();
        Map<String,String> innerMap = new HashMap<>();

        List<?> anchors = page.getByXPath("//a[@class='card-link']");
        for (int i = 0; i < anchors.size(); i++) {
            HtmlAnchor link = (HtmlAnchor) anchors.get(i);
            String recipeTitle = link.getAttribute("title").replace(',', ';');
            String recipeLink = link.getHrefAttribute();
            innerMap.put(recipeTitle,recipeLink);
            outerMap.put(link,innerMap);

        }
        this.map = outerMap;
        setMap(outerMap);
//        writeToCSV.writeTOCSV(page);
        return outerMap;
    }

    public Map<HtmlAnchor, Map<String, String>> getMap() {
        return map;
    }

    public void setMap(Map<HtmlAnchor, Map<String, String>> map) {
        this.map = map;
    }
}
