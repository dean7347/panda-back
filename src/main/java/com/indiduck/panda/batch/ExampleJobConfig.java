//package com.indiduck.panda.batch;
////
//import com.indiduck.panda.domain.UserOrder;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.*;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.database.JpaItemWriter;
//import org.springframework.batch.item.database.JpaPagingItemReader;
//import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
//import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.persistence.EntityManagerFactory;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
////https://khj93.tistory.com/entry/Spring-Batch%EB%9E%80-%EC%9D%B4%ED%95%B4%ED%95%98%EA%B3%A0-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0
//
//@Slf4j
//@Configuration
//@EnableBatchProcessing
//public class ExampleJobConfig {
//
//    @Autowired public JobBuilderFactory jobBuilderFactory;
//    @Autowired public StepBuilderFactory stepBuilderFactory;
//    @Autowired public EntityManagerFactory entityManagerFactory;
//
//    @Bean
//    public Job ExampleJob() throws Exception {
//
//        Job exampleJob = jobBuilderFactory.get("exampleJob")
//                .start(Step())
//                .build();
//
//        return exampleJob;
//    }
//
//    @Bean
//    @JobScope
//    public Step Step() throws Exception {
//        return stepBuilderFactory.get("Step")
//                .<UserOrder,UserOrder>chunk(10)
//                .reader(reader(null))
//                .processor(processor(null))
//                .writer(writer(null))
//                .build();
//    }
//
//    @Bean
//    @StepScope
//    public JpaPagingItemReader<UserOrder> reader(@Value("#{jobParameters[date]}")  String date) throws Exception {
//
//        log.info("jobParameters value : " + date);
//
////        Map<String,Object> parameterValues = new HashMap<>();
////        parameterValues.put("amount", 10000);
//
//        return new JpaPagingItemReaderBuilder<UserOrder>()
//                .pageSize(10)
////                .parameterValues(parameterValues)
//                .queryString("SELECT uo FROM UserOrder uo ORDER BY id ASC")
//                .entityManagerFactory(entityManagerFactory)
//                .name("JpaPagingItemReader")
//                .build();
//    }
//
//    @Bean
//    @StepScope
//    public ItemProcessor<UserOrder, UserOrder> processor(@Value("#{jobParameters[date]}")  String date){
//
//        return new ItemProcessor<UserOrder, UserOrder>() {
//            @Override
//            public UserOrder process(UserOrder member) throws Exception {
//
//                log.info("jobParameters value : " + date);
//                member.batchTest(LocalDateTime.now());
//                //1000원 추가 적립
////                member.setAmount(member.getAmount() + 1000);
//
//                return member;
//            }
//        };
//    }
//
//    @Bean
//    @StepScope
//    public JpaItemWriter<UserOrder> writer(@Value("#{jobParameters[date]}")  String date){
//
//        log.info("jobParameters value : " + date);
//
//        return new JpaItemWriterBuilder<UserOrder>()
//                .entityManagerFactory(entityManagerFactory)
//                .build();
//    }
//}