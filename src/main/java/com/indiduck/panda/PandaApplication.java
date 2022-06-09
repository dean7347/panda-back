package com.indiduck.panda;

//import com.indiduck.panda.batch.aCmaQuartzScheduler;
import com.indiduck.panda.Repository.ProductOptionRepository;
import com.indiduck.panda.Repository.ProductRepository;
import com.indiduck.panda.domain.Product;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


//배치프로그래밍 활성화
@EnableBatchProcessing
@SpringBootApplication
@ComponentScan({"com.indiduck.panda.*"})
@EnableScheduling
public class PandaApplication extends SpringBootServletInitializer {

    @Override protected SpringApplicationBuilder configure(SpringApplicationBuilder builder)
    { return builder.sources(PandaApplication.class); }

//    @SuppressWarnings("unused")
//    @Autowired
//    private aCmaQuartzScheduler scheduler;


    public static void main(String[] args) {
        SpringApplication.run(PandaApplication.class, args);




    }
}
