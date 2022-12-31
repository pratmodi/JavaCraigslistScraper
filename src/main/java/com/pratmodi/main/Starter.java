package com.pratmodi.main;

import com.google.gson.Gson;
import com.pratmodi.main.dto.Item;
import com.pratmodi.main.es.ESConfiguration;
import com.pratmodi.main.service.CraigsListService;
import com.pratmodi.main.service.FirstService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.math.BigDecimal;

@SpringBootApplication(scanBasePackages = {"com.pratmodi"},exclude = {DataSourceAutoConfiguration.class })
public class Starter {

    public static void main(String[] args) throws IOException {
//        FirstService firstService = new FirstService();
//        firstService.initHTMLUnit();

        ApplicationContext ctx=SpringApplication.run(Starter.class, args);
//                                FirstService m = (FirstService) ctx.getBean("firstService");
//                                m.initHTMLUnit();

        CraigsListService  cls = (CraigsListService) ctx.getBean("craigsListService");
                cls.prepareClient();


//        ESConfiguration esConfiguration = (ESConfiguration) ctx.getBean("esConfiguration");
//        esConfiguration.createIndex();
//        esConfiguration.givenJsonString_whenJavaObject_thenIndexDocument();
    }

}
