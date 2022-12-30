package com.pratmodi.main;

import com.pratmodi.main.service.CraigsListService;
import com.pratmodi.main.service.FirstService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

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
    }

}
